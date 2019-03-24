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

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public class CaptureManager extends CameraCaptureSession.StateCallback {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "CaptureManagerThread";

    // Private Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME, GlobalSettings.CAPTURE_MANAGER_THREAD_PRIORITY);

    // mInstance....................................................................................
    // TODO: description
    private static final CaptureManager mInstance = new CaptureManager();

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static int mFpsLockAttempts;

    abstract private static class mTarget {
        static long FrameExposureNanos;
        static int  TotalFrames;

        static void reset() {
            FrameExposureNanos = GlobalSettings.DEFAULT_FRAME_EXPOSURE_NANOS;
            TotalFrames        = GlobalSettings.DEFAULT_N_FRAMES;
        }
    }

    abstract private static class mCurrentSession {
        enum State {RUNNING, PAUSED, OPEN, CLOSED};

        static CameraCaptureSession captureSession;
        static CaptureRequest       captureRequest;
        static CaptureStream        captureStream;
        static List<Surface>        surfaceList;
        static State                state;

        static void newSession(CameraCaptureSession session) {
            captureSession = session;
            captureRequest = buildCaptureRequest();
            captureStream  = new CaptureStream(mTarget.TotalFrames);
            state          = State.OPEN;
        }

        static void refreshSurfaces() {
            surfaceList = SurfaceManager.getOpenSurfaces();
        }

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

        static boolean restart() {

            if (state == State.PAUSED || state == State.OPEN) {
                HeapMemory.logAvailableMiB();
                if (!HeapMemory.isMemoryGood()) {
                    DataQueue.purge();
                    System.gc();
                    if (!DataQueue.isEmpty() || ImageProcessor.isBusy()) {
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Private Constructor
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureManager...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private CaptureManager() {
        super();
        reset();
    }

    // Public Static Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // startCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void startCaptureSession() {
        Log.e(Thread.currentThread().getName(), "CaptureManager startCaptureSession");
        mCurrentSession.reset();
        // execution continues in onConfigured
        CameraController.createCaptureSession(mCurrentSession.surfaceList, mInstance, mHandler);
    }

    public static void pauseCaptureSession() {
        mCurrentSession.pause();
    }

    // restartCaptureSession........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void restartCaptureSession() {
        synchronized (mInstance) {
            while (!mCurrentSession.restart()) {
                try {
                    mInstance.wait(2 * mTarget.FrameExposureNanos / 1000 / 1000);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }
        }
        Log.e(Thread.currentThread().getName(), "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
        Log.e(Thread.currentThread().getName(), "STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE - STARTING CAPTURE");
    }

    public static long getTargetFrameNanos() {
        return mTarget.FrameExposureNanos;
    }

    // Package-private Static Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


    // sessionFinished..............................................................................
    /**
     * TODO: description, comments and logging
     * @param averageFps bla
     * @param averageDuty bla
     */
    static void sessionFinished(double averageFps, double averageDuty) {
        Log.e(Thread.currentThread().getName(), "CaptureManager sessionFinished");

        // TODO: data queue purge
        DataQueue.purge();
        synchronized (mInstance) {
            try {
                mInstance.wait(1000);
            } catch (InterruptedException e) {

            }
        }
        String string = " \n";
        string += "Session performance: \n";
        string += "\t Average FPS:  " + NumToString.decimal(averageFps) + " [frames / sec] \n";
        string += "\t Average Duty: " + NumToString.decimal(averageDuty * 100.) + " % \n";
        Log.e(Thread.currentThread().getName(), string);

        if (averageDuty < GlobalSettings.DUTY_THRESHOLD && mFpsLockAttempts < GlobalSettings.FPS_ATTEMPT_LIMIT) {
            mFpsLockAttempts += 1;
            mTarget.FrameExposureNanos = (long) ( Math.floor(1e9 / averageFps) );

            Log.e(Thread.currentThread().getName(), "Start next session with frame rate target: "
                    + NumToString.decimal(1. / ( mTarget.FrameExposureNanos * 1e-9) )
                    + " [frames / sec]");

            // TODO: do this:
            //mCurrentSession.newSession(mCurrentSession.captureSession);
            //restartCaptureSession();

            //Runnable startAgain = new Runnable() {
            //    @Override
            //    public void run() {
            //        startCaptureSession();
            //    }
            //};
            //Log.e(Thread.currentThread().getName(), "CaptureManager Posting startAgain");
            //CaptureOverseer.setNextAction(startAgain);

            //return;
        }
        CaptureOverseer.quitSafely();

        /*
        if (true) { // TODO: if take another run
            Runnable doStatistics;
            doStatistics = new Runnable() {
                @Override
                public void run() {
                    //00000000000000000000000000000000000000000000000000000000000000000000000000
                    //ImageProcessorOld.processStatistics();
                    startCaptureSession();
                }
            };
            Log.e(Thread.currentThread().getName(), "CaptureManager Posting statistics then startAgain");
            CaptureOverseer.setNextAction(doStatistics);
            return;
        }

        if (true) { // TODO: if quitting time
            mCurrentSession.reset();
            Runnable doStatistics;
            doStatistics = new Runnable() {
                @Override
                public void run() {
                    //00000000000000000000000000000000000000000000000000000000000000000000000000
                    //ImageProcessorOld.processStatistics();
                    CaptureOverseer.quitSafely();
                }
            };
            Log.e(Thread.currentThread().getName(), "CaptureManager Posting statistics and quit");
            CaptureOverseer.setNextAction(doStatistics);
            return;
        }
        */
    }

    // Private Static Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // buildCaptureRequest..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    private static CaptureRequest buildCaptureRequest() {
        Log.e(Thread.currentThread().getName(), "CaptureManager buildCaptureRequest");
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
    @SuppressWarnings("unchecked")
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
        Log.e(Thread.currentThread().getName(), "CaptureManager reset");
        mFpsLockAttempts = 0;
        mTarget.reset();
        mCurrentSession.reset();
    }

    // Public Overriding Methods
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
        Log.e(Thread.currentThread().getName(), "CaptureManager onConfigured");
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
        Log.e(Thread.currentThread().getName(), "CaptureManager onClosed");
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
        //Log.e(Thread.currentThread().getName(), "CaptureManager onReady");
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
        //Log.e(Thread.currentThread().getName(), "CaptureManager onActive");
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
        //Log.e(Thread.currentThread().getName(), "CaptureManager onCaptureQueueEmpty");
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
        //Log.e(Thread.currentThread().getName(), "CaptureManager onSurfacePrepared: " + surface.toString());
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
        Log.e(Thread.currentThread().getName(), "CaptureManager onConfigureFailed");
        // TODO: SHUTDOWN
    }

}