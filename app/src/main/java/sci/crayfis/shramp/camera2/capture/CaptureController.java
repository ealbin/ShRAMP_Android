/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.view.Surface;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.analysis.PrintAllocations;
import sci.crayfis.shramp.battery.BatteryController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceController;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class CaptureController extends CameraCaptureSession.StateCallback {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Mode.........................................................................................
    // TODO: description
    private enum Mode {
        WARMUP,
        COOLDOWN,
        CALIBRATION_COLD_FAST,
        CALIBRATION_COLD_SLOW,
        CALIBRATION_HOT_FAST,
        CALIBRATION_HOT_SLOW,
        DATA
    }

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "CaptureManagerThread";

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
            GlobalSettings.CAPTURE_MANAGER_THREAD_PRIORITY);

    // mInstance....................................................................................
    // TODO: description
    private static final CaptureController mInstance = new CaptureController();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mFpsLockAttempts.............................................................................
    // TODO: description
    private static int mFpsLockAttempts;

    // mRunMode........................................................................................
    // TODO: description
    private static Mode mRunMode;

    private static Mode mNextMode;

    // mDataRunAttempts.............................................................................
    // TODO: description
    private static int mDataRunAttempts;

    // mTarget......................................................................................
    // TODO: description
    abstract private static class mTarget {

        // TODO: description
        static long ExposureNanos;
        static int FrameLimit;
        static double HighTemperatureLimit;

        /**
         * TODO: description, comments and logging
         */
        static void reset() {
            ExposureNanos = GlobalSettings.DEFAULT_EXPOSURE_NANOS;
            FrameLimit = GlobalSettings.FPS_LOCK_N_FRAMES;
            HighTemperatureLimit = GlobalSettings.TEMPERATURE_HIGH;
        }
    }

    // mCurrentSession..............................................................................
    // TODO: description
    abstract private static class mCurrentSession {

        // TODO: description
        enum State {RUNNING, PAUSED, OPEN, CLOSED};

        // TODO: description
        static CameraCaptureSession captureSession;
        static CaptureRequest       captureRequest;
        static CaptureStream        captureStream;
        static List<Surface>        surfaceList;
        static State                state;

        // newSession...............................................................................
        /**
         * TODO: description, comments and logging
         * @param session bla
         */
        static void newSession(@NonNull CameraCaptureSession session) {
            captureSession = session;
            renewSession();
        }

        // renewSession.............................................................................
        /**
         * TODO: description, comments and logging
         */
        static void renewSession() {
            captureRequest = buildCaptureRequest();
            captureStream  = new CaptureStream(mTarget.FrameLimit, mTarget.HighTemperatureLimit);
            state          = State.OPEN;
        }

        // refreshSurface...........................................................................
        /**
         * TODO: description, comments and logging
         */
        static void refreshSurfaces() {
            surfaceList = SurfaceController.getOpenSurfaces();
        }

        // pause....................................................................................
        /**
         * TODO: description, comments and logging
         * @return bla
         */
        static boolean pause() {
            if (state == State.RUNNING) {
                try {
                    captureSession.stopRepeating();
                    state = State.PAUSED;
                    return true;
                } catch (CameraAccessException e) {
                    // TODO:  error
                    return false;
                }
            }

            if (state == State.OPEN || state == State.CLOSED) {
                return false;
            }

            return (state == State.PAUSED);
        }

        // restart..................................................................................
        /**
         * TODO: description, comments and logging
         * @return bla
         */
        static boolean restart() {

            if (state == State.PAUSED || state == State.OPEN) {
                HeapMemory.logAvailableMiB();
                if (!HeapMemory.isMemoryAmple()) {
                    DataQueue.purge();
                    System.gc();
                    if (!DataQueue.isEmpty() || AnalysisController.isBusy() || StorageMedia.isBusy()) {
                        return false;
                    }
                    Log.e(Thread.currentThread().getName(), "Forcing Restart");
                }

                try {
                    captureSession.setRepeatingRequest(captureRequest, captureStream, mHandler);
                    state = State.RUNNING;
                    return true;
                } catch (CameraAccessException e) {
                    // TODO: handle this
                    Log.e(Thread.currentThread().getName(), "CAMERA ACCESS EXCEPTION");
                    MasterController.quitSafely();
                }
            }

            if (state == State.RUNNING) {
                return true;
            }

            return !(state == State.CLOSED);
        }

        // reset....................................................................................
        /**
         * TODO: description, comments and logging
         */
        static void reset() {
            if (captureSession != null) {
                captureSession.close();
            }
            captureSession = null;
            captureRequest = null;
            captureStream  = null;
            surfaceList    = null;
            refreshSurfaces();

            state = State.CLOSED;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureController...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private CaptureController() {
        super();
        reset();
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // startCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void startCaptureSession() {
        Log.e(Thread.currentThread().getName(), "CaptureController startCaptureSession");

        mCurrentSession.reset();
        AnalysisController.resetRunningTotals();

        // ask if calibration needed
        // if not, start data
        if (AnalysisController.needsCalibration() && !GlobalSettings.DEBUG_DISABLE_CALIBRATION) {
            mRunMode = Mode.CALIBRATION_COLD_FAST;
            mTarget.FrameLimit = GlobalSettings.CALIBRATION_N_FRAMES;
            mTarget.ExposureNanos = GlobalSettings.DEFAULT_FAST_FPS;
        }
        else {
            // check temperature and decide if warm up, cool down, or data
            mRunMode = Mode.DATA;
            mTarget.FrameLimit = GlobalSettings.DATARUN_N_FRAMES;
            mTarget.ExposureNanos = GlobalSettings.DEFAULT_EXPOSURE_NANOS;

            // for now,
            //MasterController.quitSafely();
        }
        // execution continues in onConfigured
        CameraController.createCaptureSession(mCurrentSession.surfaceList, mInstance, mHandler);
    }

    // onConfigured.................................................................................
    /**
     * This method is called when the camera device has finished configuring itself,
     * and the session can start processing capture requests.
     * (Required)
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onConfigured(@NonNull CameraCaptureSession session) {
        //super.onConfigured(session); is abstract
        Log.e(Thread.currentThread().getName(), "CaptureController onConfigured");
        mCurrentSession.newSession(session);
        restartCaptureSession();
    }

    static void coolDown() {
        synchronized (mInstance) {
            Double temperature = BatteryController.getCurrentTemperature();
            if (temperature == null) {
                Log.e(Thread.currentThread().getName(), "Temperature is unknown, shutting down for safety");
                MasterController.quitSafely();
                return;
            }

            while (temperature > mTarget.HighTemperatureLimit) {
                try {
                    Log.e(Thread.currentThread().getName(), "Cooling down: " + NumToString.number(temperature)
                    + " > " + NumToString.number(mTarget.HighTemperatureLimit) + " [Celsius]");
                    mInstance.wait(GlobalSettings.DEFAULT_LONG_WAIT);
                }
                catch (InterruptedException e) {
                    // TODO: err
                }
            }
        }
    }

    // restartCaptureSession........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void restartCaptureSession() {
        synchronized (mInstance) {
            while (!mCurrentSession.restart()) {
                try {
                    Log.e(Thread.currentThread().getName(), "Waiting to restart capture session");
                    mInstance.wait(GlobalSettings.DEFAULT_WAIT_MS);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }
        }
        Log.e(Thread.currentThread().getName(), "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
        Log.e(Thread.currentThread().getName(), "STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE");
    }

    // pauseCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void pauseCaptureSession() {
        mCurrentSession.pause();
    }

    public static void resetCaptureSession() {
        pauseCaptureSession();
    }

    static void sessionReset() {
        AnalysisController.resetRunningTotals();
        AnalysisController.setSignificanceThreshold(mTarget.FrameLimit);
        mCurrentSession.renewSession();
        restartCaptureSession();
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // sessionFinished..............................................................................
    /**
     * TODO: description, comments and logging
     * @param averageFps bla
     * @param averageDuty bla
     */
    static void sessionFinished(double averageFps, double averageDuty) {
        Log.e(Thread.currentThread().getName(), "CaptureController sessionFinished");

        String string = " \n";
        string += "Session performance: \n";
        string += "\t Session Average FPS:  " + NumToString.decimal(averageFps) + " [frames / sec] \n";
        string += "\t Session Average Duty: " + NumToString.decimal(averageDuty * 100.) + " % \n";
        Log.e(Thread.currentThread().getName(), string);

        if (!GlobalSettings.CONSTANT_FPS) {
            mTarget.ExposureNanos = (long) (Math.floor(1e9 / averageFps));
        }
        Log.e(Thread.currentThread().getName(), "Start next session with frame rate target: "
                + NumToString.decimal(1. / ( mTarget.ExposureNanos * 1e-9) )
                + " [frames / sec]");

        switch (mRunMode) {

            case CALIBRATION_COLD_FAST: {
                AnalysisController.runStatistics("cold_fast");
                PrintAllocations.printMeanAndErr();

                if (false) {
                    MasterController.quitSafely();
                    return;
                }

                mRunMode = Mode.CALIBRATION_COLD_SLOW;
                mTarget.ExposureNanos = GlobalSettings.DEFAULT_SLOW_FPS;
                sessionReset();
                break;
            }

            case CALIBRATION_COLD_SLOW: {
                AnalysisController.runStatistics("cold_slow");
                PrintAllocations.printMeanAndErr();

                mRunMode = Mode.WARMUP;
                mNextMode = Mode.CALIBRATION_HOT_FAST;
                mTarget.ExposureNanos = GlobalSettings.DEFAULT_FAST_FPS;
                mTarget.HighTemperatureLimit = GlobalSettings.TEMPERATURE_HIGH;
                sessionReset();
                break;
            }

            case WARMUP: {
                if (mNextMode == null) {
                    Log.e(Thread.currentThread().getName(), "Crap, I don't know what to do now, quitting");
                    MasterController.quitSafely();
                    return;
                }

                Double temperature = BatteryController.getCurrentTemperature();
                if (temperature == null) {
                    Log.e(Thread.currentThread().getName(), "Temperature is unknown, shutting down for safety");
                    MasterController.quitSafely();
                    return;
                }

                if (temperature < mTarget.HighTemperatureLimit) {
                    sessionReset();
                    return;
                }

                switch (mNextMode) {
                    case CALIBRATION_HOT_FAST: {
                        mRunMode  = Mode.CALIBRATION_HOT_FAST;
                        mNextMode = null;
                        mTarget.ExposureNanos = GlobalSettings.DEFAULT_FAST_FPS;
                        mTarget.HighTemperatureLimit = temperature + GlobalSettings.OVER_TEMPERATURE;
                        sessionReset();
                        break;
                    }
                    // TODO: Data
                }
                break;
            }

            case CALIBRATION_HOT_FAST: {
                AnalysisController.runStatistics("hot_fast");
                PrintAllocations.printMeanAndErr();

                mRunMode = Mode.CALIBRATION_HOT_SLOW;
                mTarget.ExposureNanos = GlobalSettings.DEFAULT_SLOW_FPS;
                sessionReset();
                break;
            }

            case CALIBRATION_HOT_SLOW: {
                AnalysisController.runStatistics("hot_slow");
                PrintAllocations.printMeanAndErr();

                mRunMode = Mode.COOLDOWN;
                coolDown();

                // AnalysisController.quality cut? filter pixels? whatever we'll call it
                // then take data
                mRunMode = Mode.DATA;
                mTarget.ExposureNanos = GlobalSettings.DEFAULT_EXPOSURE_NANOS;
                //sessionReset();

                // quit for now
                MasterController.quitSafely();
                break;
            }

            case DATA: {
                mDataRunAttempts += 1;
                if (mDataRunAttempts < GlobalSettings.DATA_ATTEMPT_LIMIT) {
                    sessionReset();
                }
                else {
                    reset();
                    MasterController.quitSafely();
                    return;
                }
                break;
            }
        }
    }


            /*

                mFpsLockAttempts += 1;

                CaptureRequest.Builder builder = CameraController.getCaptureRequestBuilder();
                assert builder != null;

                Integer mode = builder.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (averageDuty >= GlobalSettings.DUTY_THRESHOLD
                                    || mFpsLockAttempts >= GlobalSettings.FPS_ATTEMPT_LIMIT
                                    || mode == CameraMetadata.CONTROL_AE_MODE_ON) {

                    if (GlobalSettings.CALIBRATION_N_FRAMES > 0) {
                        mRunMode = Mode.CALIBRATION;
                        mTarget.FrameLimit = GlobalSettings.CALIBRATION_N_FRAMES;
                    }
                    else if (GlobalSettings.DATARUN_N_FRAMES > 0 && GlobalSettings.DATA_ATTEMPT_LIMIT > 0){
                        mRunMode = Mode.DATA;
                        mTarget.FrameLimit = GlobalSettings.DATARUN_N_FRAMES;
                    }
                    else {
                        reset();
                        MasterController.quitSafely();
                        return;
                    }
                }
                sessionReset();
             */

                /*
                AnalysisController.enableSignificance();

                if (GlobalSettings.DATARUN_N_FRAMES > 0 && GlobalSettings.DATA_ATTEMPT_LIMIT > 0) {
                    mRunMode = Mode.DATA;
                    mTarget.FrameLimit = GlobalSettings.DATARUN_N_FRAMES;
                    sessionReset();
                }
                else {
                    reset();
                    MasterController.quitSafely();
                    return;
                }
                break;
            }*/


    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // buildCaptureRequest..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    private static CaptureRequest buildCaptureRequest() {
        Log.e(Thread.currentThread().getName(), "CaptureController buildCaptureRequest");
        StopWatch stopWatch = new StopWatch();

        RequestMaker.makeDefault();
        CaptureRequest.Builder builder = CameraController.getCaptureRequestBuilder();
        assert builder != null;

        for (Surface surface : mCurrentSession.surfaceList) {
            builder.addTarget(surface);
        }

        Integer mode = builder.get(CaptureRequest.CONTROL_AE_MODE);
        assert mode != null;

        if (mode == CameraMetadata.CONTROL_AE_MODE_ON) {
            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getAeTargetFpsRange());
        }
        else {
            builder.set(CaptureRequest.SENSOR_FRAME_DURATION, mTarget.ExposureNanos);
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,  mTarget.ExposureNanos);
        }
        CameraController.setCaptureRequestBuilder(builder);
        CameraController.writeFPS();

        CaptureRequest request = builder.build();
        Log.e(Thread.currentThread().getName(), "<buildCaptureRequest()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
        return request;
    }

    // getAeTargetFpsRange..........................................................................
    /**
     * TODO: description, comments and logging
     */
    @SuppressWarnings("unchecked")
    @NonNull
    private static Range<Integer> getAeTargetFpsRange() {
        StopWatch stopWatch = new StopWatch();
        // Set FPS range closest to target FPS
        LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap;
        characteristicsMap = CameraController.getOpenedCharacteristicsMap();
        assert characteristicsMap != null;

        CameraCharacteristics.Key<Range<Integer>[]> cKey;
        Parameter<Range<Integer>[]> property;

        cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
        property = characteristicsMap.get(cKey);
        assert property != null;

        Range<Integer>[] ranges = property.getValue();
        assert ranges != null;

        int target = (int) Math.round(1e9 / mTarget.ExposureNanos);
        Range<Integer> closest = null;
        for (Range<Integer> range : ranges) {
            if (closest == null) {
                closest = range;
                continue;
            }

            int diff = Math.min(Math.abs(range.getUpper() - target),
                                Math.abs(range.getLower() - target));

            int closestDiff = Math.min(Math.abs(closest.getUpper() - target),
                                       Math.abs(closest.getLower() - target));

            if (diff < closestDiff) {
                closest = range;
            }
        }
        assert closest != null;
        Log.e(Thread.currentThread().getName(), "<getAeTargetFpsRange()> time: " + NumToString.number(stopWatch.stop()) + " [ns");
        return closest;
    }

    // reset........................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void reset() {
        Log.e(Thread.currentThread().getName(), "CaptureController reset");
        mFpsLockAttempts = 0;
        mDataRunAttempts = 0;
        mTarget.reset();
        mCurrentSession.reset();
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


    // onClosed.....................................................................................
    /**
     * This method is called when the session is closed.
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onClosed(@NonNull CameraCaptureSession session) {
        super.onClosed(session);
        Log.e(Thread.currentThread().getName(), "CaptureController onClosed");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: IGNORE ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // onReady......................................................................................
    /**
     * This method is called every time the session has no more capture requests to process.
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onReady(@NonNull CameraCaptureSession session) {
        super.onReady(session);
        //Log.e(Thread.currentThread().getName(), "CaptureController onReady");
    }

    // onActive.....................................................................................
    /**
     * This method is called when the session starts actively processing capture requests.
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onActive(@NonNull CameraCaptureSession session) {
        super.onActive(session);
        //Log.e(Thread.currentThread().getName(), "CaptureController onActive");
    }

    // onCaptureQueueEmpty..........................................................................
    /**
     * This method is called when camera device's input capture queue becomes empty,
     * and is ready to accept the next request.
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onCaptureQueueEmpty(@NonNull CameraCaptureSession session) {
        super.onCaptureQueueEmpty(session);
        //Log.e(Thread.currentThread().getName(), "CaptureController onCaptureQueueEmpty");
    }

    // onSurfacePrepared............................................................................
    /**
     * This method is called when the buffer pre-allocation for an output Surface is complete.
     *  TODO: documentation, comments and logging
     * @param session bla
     * @param surface bla
     */
    @Override
    public void onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface) {
        super.onSurfacePrepared(session, surface);
        //Log.e(Thread.currentThread().getName(), "CaptureController onSurfacePrepared: " + surface.toString());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: SHUTDOWN //////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // onConfiguredFailed...........................................................................
    /**
     * This method is called if the session cannot be configured as requested.
     * (Required)
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        //super.onConfigureFailed(session); is abstract
        Log.e(Thread.currentThread().getName(), "CaptureController onConfigureFailed");
        // TODO: SHUTDOWN
    }

}