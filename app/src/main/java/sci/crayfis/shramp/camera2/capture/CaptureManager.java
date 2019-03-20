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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;

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

    // DEFAULT_FRAME_EXPOSURE_NANOS.................................................................
    // TODO: description
    private static final Long DEFAULT_FRAME_EXPOSURE_NANOS = GlobalSettings.DEFAULT_FRAME_EXPOSURE_NANOS;

    // DEFAULT_N_FRAMES.............................................................................
    // TODO: description
    private static final Integer DEFAULT_N_FRAMES = GlobalSettings.DEFAULT_N_FRAMES;

    // DUTY_THRESHOLD...............................................................................
    // TODO: description
    private static final Double DUTY_THRESHOLD = GlobalSettings.DUTY_THRESHOLD;

    // FPS_ATTEMPT_LIMIT............................................................................
    // TODO: description
    private static final Integer FPS_ATTEMPT_LIMIT = GlobalSettings.FPS_ATTEMPT_LIMIT;

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = GlobalSettings.CAPTURE_MANAGER_THREAD_PRIORITY;

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "CaptureManagerThread";

    // Private Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME, PRIORITY);

    // mInstance....................................................................................
    // TODO: description
    private static final CaptureManager mInstance = new CaptureManager();

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mFpsAttempts.................................................................................
    // TODO: description
    private static Integer mFpsAttempts;

    // mFrameExposureNanos..........................................................................
    // TODO: description
    private static Long mFrameExposureNanos;

    // mNframes.....................................................................................
    // TODO: description
    private static Integer mNframes;

    // mSurfaceList.................................................................................
    // TODO: description
    private static List<Surface> mSurfaceList;


    //==============================================================================================
    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    private static final DecimalFormat mFormatter = new DecimalFormat("##.#");
    //==============================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureManager...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private CaptureManager() {
        super();
        //Log.e(Thread.currentThread().getName(), "CaptureManager CaptureManager");
        clear();
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // startCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void startCaptureSession() {
        Log.e(Thread.currentThread().getName(), "CaptureManager startCaptureSession");
        refreshSurfaces();
        CameraController.createCaptureSession(mSurfaceList, mInstance, mHandler);
    }

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // sessionFinished..............................................................................
    /**
     * TODO: description, comments and logging
     * @param session bla
     * @param averageFps bla
     * @param averageDuty bla
     */
    static void sessionFinished(@NonNull CameraCaptureSession session,
                                double averageFps, double averageDuty) {
        Log.e(Thread.currentThread().getName(), "CaptureManager sessionFinished");
        mCurrentCaptureRequest = null;
        String string = " \n";
        string += "Session performance: \n";
        string += "\t Average FPS:  " + mFormatter.format(averageFps) + " [frames / sec] \n";
        string += "\t Average Duty: " + mFormatter.format(averageDuty * 100.) + " % \n";
        mLogger.log(string);

        if (GlobalSettings.DEBUG_NO_DATA_POSTING) {
            CaptureOverseer.quitSafely();
            return;
        }

        // If observed exposure duty is less than DUTY_THRESHOLD, try again with fps at the average
        mFpsAttempts += 1;
        if (averageDuty < DUTY_THRESHOLD && mFpsAttempts < FPS_ATTEMPT_LIMIT) {
            mFrameExposureNanos = (long) (Math.floor(1e9 / averageFps) );

            mLogger.log("Start next session with frame rate target: "
                    + mFormatter.format(1. / ( mFrameExposureNanos * 1e-9) )
                    + " [frames / sec]");

            Runnable startAgain = new Runnable() {
                @Override
                public void run() {
                    startCaptureSession();
                }
            };
            Log.e(Thread.currentThread().getName(), "CaptureManager Posting startAgain");
            CaptureOverseer.setNextAction(startAgain);
        }
        else {
            // ready for data


            mInstance.quit = true;
            Runnable doStatistics;
            if (!mInstance.quit) {
                doStatistics = new Runnable() {
                    @Override
                    public void run() {
                        ImageProcessor.processStatistics();
                        startCaptureSession();
                    }
                };
                Log.e(Thread.currentThread().getName(), "CaptureManager Posting statistics then startAgain");
            }
            else {
                session.close();

                doStatistics = new Runnable() {
                    @Override
                    public void run() {
                        ImageProcessor.processStatistics();
                        CaptureOverseer.quitSafely();
                    }
                };
                Log.e(Thread.currentThread().getName(), "CaptureManager Posting statistics and quit");
            }
            CaptureOverseer.setNextAction(doStatistics);

        }
    }

    boolean quit = false;

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // buildCaptureRequest..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    private static CaptureRequest buildCaptureRequest() {
        Log.e(Thread.currentThread().getName(), "CaptureManager buildCaptureRequest");
        RequestMaker.makeDefault();
        CaptureRequest.Builder builder = CameraController.getCaptureRequestBuilder();
        assert builder != null;

        for (Surface surface : mSurfaceList) {
            builder.addTarget(surface);
        }

        Integer mode = builder.get(CaptureRequest.CONTROL_AE_MODE);
        assert mode != null;

        if (mode == CameraMetadata.CONTROL_AE_MODE_ON) {

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

            int target = (int) Math.round(1e9 / mFrameExposureNanos);
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

            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, closest);
        }
        else {
            builder.set(CaptureRequest.SENSOR_FRAME_DURATION, mFrameExposureNanos);
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,  mFrameExposureNanos);
        }
        CameraController.setCaptureRequestBuilder(builder);
        CameraController.writeFPS();
        return builder.build();
    }

    // clear........................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void clear() {
        Log.e(Thread.currentThread().getName(), "CaptureManager clear");
        mFpsAttempts = 0;
        mNframes = DEFAULT_N_FRAMES;
        mFrameExposureNanos = DEFAULT_FRAME_EXPOSURE_NANOS;
        mSurfaceList = null;
        mCurrentCaptureRequest = null;
    }

    // refreshSurfaces..............................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void refreshSurfaces() {
        Log.e(Thread.currentThread().getName(), "CaptureManager refreshSurfaces");
        mSurfaceList = SurfaceManager.getOpenSurfaces();
    }

    // startRepeatingRequest........................................................................
    /**
     * TODO: description, comments and logging
     * @param session bla
     * @param callback bla
     */
    private void startRepeatingRequest(CameraCaptureSession session,
                                       CameraCaptureSession.CaptureCallback callback) {

        Log.e(Thread.currentThread().getName(), "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
        Log.e(Thread.currentThread().getName(), "CaptureManager startRepeatingRequest");
        ImageProcessor.reset();
        mCurrentCaptureRequest = buildCaptureRequest();

        try {
            // Execution continues in CaptureCallback.onCaptureStarted()
            session.setRepeatingRequest(mCurrentCaptureRequest, callback, mHandler);
        }
        catch (CameraAccessException e) {
            // TODO: "ERROR: Camera Access Exception"
        }
    }

    private static CaptureRequest mCurrentCaptureRequest;
    private static CameraCaptureSession mCurrentSession;
    private static CaptureSession mCurrentCaptureSession;

    public static boolean pauseRepeatingRequest() {
        return CaptureSession.pauseRepeatingRequest();
    }

    static void pauseRepeatingRequest(CameraCaptureSession session,
                                 CaptureSession captureSession) {

        mCurrentSession = session;
        mCurrentCaptureSession = captureSession;
    }

    public static boolean requestIsPaused() {
        return (mCurrentSession != null && mCurrentCaptureSession != null);
    }

    public static void restartRepeatingRequest() {
        if (mCurrentCaptureRequest == null || mCurrentCaptureSession == null
        || HeapMemory.getAvailableMiB() < GlobalSettings.LOW_MEMORY_MiB) {
            return;
        }
        Log.e(Thread.currentThread().getName(), "CaptureManager *** restart *** restart *** restart *** restart *** restart *** restart *** restart *** restart *** restart");

        try {
            // Execution continues in CaptureCallback.onCaptureStarted()
            CaptureSession.resumeRepeatingRequest();
            mCurrentSession.setRepeatingRequest(mCurrentCaptureRequest, mCurrentCaptureSession, mHandler);
            mCurrentSession = null;
            mCurrentCaptureSession = null;
        }
        catch (CameraAccessException e) {
            // TODO: "ERROR: Camera Access Exception"
        }
    }

    //**********************************************************************************************
    // Overriding Methods
    //-------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onActive.....................................................................................
    /**
     * This method is called when the session starts actively processing capture requests.
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onActive(@NonNull CameraCaptureSession session) {
        super.onActive(session);
        Log.e(Thread.currentThread().getName(), "CaptureManager onActive");
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
        Log.e(Thread.currentThread().getName(), "CaptureManager onCaptureQueueEmpty");
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


        if (GlobalSettings.DEBUG_DISABLE_CAPTURE) {
            session.close();
            CaptureOverseer.quitSafely();
            return;
        }

        CaptureSession captureSession = new CaptureSession(mNframes);
        startRepeatingRequest(session, captureSession);

    }

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
    }

    // onReady......................................................................................
    /**
     * This method is called every time the session has no more capture requests to process.
     *  TODO: documentation, comments and logging
     * @param session bla
     */
    @Override
    public void onReady(@NonNull CameraCaptureSession session) {
        super.onReady(session);
        Log.e(Thread.currentThread().getName(), "CaptureManager onReady");
        if (GlobalSettings.DEBUG_STOP_CAPTURE_IMMEDIATELY) {
            try {
                session.abortCaptures();
            }
            catch (CameraAccessException e) {
            }
            CaptureOverseer.quitSafely();
        }
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
        Log.e(Thread.currentThread().getName(), "CaptureManager onSurfacePrepared: " + surface.toString());
    }

}