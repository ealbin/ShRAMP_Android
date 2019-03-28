/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

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

import org.jetbrains.annotations.Contract;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceController;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class CaptureController extends CameraCaptureSession.StateCallback {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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

    // mDataRunAttempts.............................................................................
    // TODO: description
    private static int mDataRunAttempts;

    // mTarget......................................................................................
    // TODO: description
    abstract private static class mTarget {

        // TODO: description
        static long FrameExposureNanos;
        static int  TotalFrames;

        /**
         * TODO: description, comments and logging
         */
        static void reset() {
            FrameExposureNanos = GlobalSettings.DEFAULT_FRAME_EXPOSURE_NANOS;
            TotalFrames        = GlobalSettings.DEFAULT_N_FRAMES;
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
            captureStream  = new CaptureStream(mTarget.TotalFrames);
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
                    if (!DataQueue.isEmpty() || AnalysisController.isBusy()) {
                        return false;
                    }
                    Log.e(Thread.currentThread().getName(), "Forcing Restart");
                }

                try {
                    captureSession.setRepeatingRequest(captureRequest, captureStream, mHandler);
                    state = State.RUNNING;
                    return true;
                } catch (CameraAccessException e) {
                    // TODO: error
                    return false;
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
        // execution continues in onConfigured
        CameraController.createCaptureSession(mCurrentSession.surfaceList, mInstance, mHandler);
    }

    // pauseCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void pauseCaptureSession() {
        mCurrentSession.pause();
    }

    // restartCaptureSession........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void restartCaptureSession() {
        synchronized (mInstance) {
            while (!mCurrentSession.restart()) {
                try {
                    mInstance.wait(3 * mTarget.FrameExposureNanos / 1000 / 1000);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }
        }
        Log.e(Thread.currentThread().getName(), "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
        Log.e(Thread.currentThread().getName(), "STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE");
    }

    // getTargetFrameNanos..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public static long getTargetFrameNanos() {
        return mTarget.FrameExposureNanos;
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
        string += "\t Average FPS:  " + NumToString.decimal(averageFps) + " [frames / sec] \n";
        string += "\t Average Duty: " + NumToString.decimal(averageDuty * 100.) + " % \n";
        Log.e(Thread.currentThread().getName(), string);

        mTarget.FrameExposureNanos = (long) ( Math.floor(1e9 / averageFps) );
        Log.e(Thread.currentThread().getName(), "Start next session with frame rate target: "
                + NumToString.decimal(1. / ( mTarget.FrameExposureNanos * 1e-9) )
                + " [frames / sec]");

        AnalysisController.runStatistics();

        AnalysisController.peekMeanAndErr();
        //AnalysisController.peekStdDev();
        if (AnalysisController.isSignificanceEnabled()) {
            AnalysisController.peekSignificance();
        }

        AnalysisController.resetRunningTotals();

        if (averageDuty < GlobalSettings.DUTY_THRESHOLD && mFpsLockAttempts < GlobalSettings.FPS_ATTEMPT_LIMIT) {
            mFpsLockAttempts += 1;

            // TODO: update mean, std dev
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            mCurrentSession.renewSession();
            restartCaptureSession();
        }
        else if (mDataRunAttempts < GlobalSettings.DATA_ATTEMPT_LIMIT) {
            mDataRunAttempts += 1;
            mTarget.TotalFrames = GlobalSettings.DATARUN_N_FRAMES;

            AnalysisController.enableSignificance();
            AnalysisController.setSignificanceThreshold(mTarget.TotalFrames);

            // TODO: enable saving
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            Log.e(Thread.currentThread().getName(), "*******************************************************************************");
            mCurrentSession.renewSession();
            restartCaptureSession();
        }
        else {
            reset();
            MasterController.quitSafely();
        }
    }

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
            builder.set(CaptureRequest.SENSOR_FRAME_DURATION, mTarget.FrameExposureNanos);
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,  mTarget.FrameExposureNanos);
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

        int target = (int) Math.round(1e9 / mTarget.FrameExposureNanos);
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
        mFpsLockAttempts = 1;
        mDataRunAttempts = 0;
        mTarget.reset();
        mCurrentSession.reset();
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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