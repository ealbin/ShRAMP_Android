package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Process;
import android.view.Surface;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.HandlerManager;

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
    private static final Long DEFAULT_FRAME_EXPOSURE_NANOS = 33333333L;

    // DEFAULT_N_FRAMES.............................................................................
    // TODO: description
    private static final Integer DEFAULT_N_FRAMES = 30;

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = Process.THREAD_PRIORITY_DEFAULT;

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
        refreshSurfaces();
        CameraController.createCaptureSession(mSurfaceList, mInstance, mHandler);
    }

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // sessionFinished..............................................................................
    /**
     * TODO: description, comments and logging
     * @param session
     * @param callback
     * @param averageFps
     * @param averageDuty
     */
    static void sessionFinished(CameraCaptureSession session,
                                CameraCaptureSession.CaptureCallback callback,
                                double averageFps, double averageDuty) {
        String string = " \n";
        string += "Session performance: \n";
        string += "\t Average FPS:  " + mFormatter.format(averageFps) + " [frames / sec] \n";
        string += "\t Average Duty: " + mFormatter.format(averageDuty * 100.) + " % \n";
        mLogger.log(string);

        // TODO: if duty is less than 95%, try again with fps at the average

        if (averageDuty < 0.98) {
            mFrameExposureNanos = (long) (Math.floor(1e9 / averageFps));

            mLogger.log("Restarting session with new frame rate target: "
                    + mFormatter.format(1. / ( mFrameExposureNanos * 1e-9) )
                    + " [frames / sec]");


            Runnable waitForImageProcessor = new Runnable() {
                Runnable restartCapture = new Runnable() {
                    @Override
                    public void run() {
                        startCaptureSession();
                    }
                };

                @Override
                public void run() {
                    // TODO: reset image processor
                    CaptureOverseer.post(restartCapture);
                }
            };

            ImageProcessor.post(waitForImageProcessor);
        }
        else {
            // ready for data

            mLogger.log("Quitting");
            session.close();
            CaptureOverseer.quitSafely();
        }
    }

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // buildCaptureRequest..........................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    private static CaptureRequest buildCaptureRequest() {
        RequestMaker.makeDefault();
        CaptureRequest.Builder builder = CameraController.getCaptureRequestBuilder();
        assert builder != null;

        for (Surface surface : mSurfaceList) {
            builder.addTarget(surface);
        }

        // NOTE: applied times might not alter the capture parameters if not manual sensor able
        builder.set(CaptureRequest.SENSOR_FRAME_DURATION, mFrameExposureNanos);
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,  mFrameExposureNanos);

        CameraController.setCaptureRequestBuilder(builder);
        return builder.build();
    }

    // clear........................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void clear() {
        mNframes = DEFAULT_N_FRAMES;
        mFrameExposureNanos = DEFAULT_FRAME_EXPOSURE_NANOS;
        mSurfaceList = null;
    }

    // refreshSurfaces..............................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void refreshSurfaces() {
        mSurfaceList = SurfaceManager.getOpenSurfaces();
    }

    // startRepeatingRequest........................................................................
    /**
     * TODO: description, comments and logging
     * @param session
     * @param callback
     */
    private void startRepeatingRequest(CameraCaptureSession session,
                                       CameraCaptureSession.CaptureCallback callback) {

        CaptureRequest captureRequest = buildCaptureRequest();

        try {
            // Execution continues in CaptureCallback.onCaptureStarted()
            session.setRepeatingRequest(captureRequest, callback, mHandler);
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
     * @param session
     */
    @Override
    public void onActive(CameraCaptureSession session) {
        super.onActive(session);
        mLogger.log("Session active");
    }

    // onCaptureQueueEmpty..........................................................................
    /**
     * This method is called when camera device's input capture queue becomes empty,
     * and is ready to accept the next request.
     * @param session
     */
    @Override
    public void onCaptureQueueEmpty(CameraCaptureSession session) {
        super.onCaptureQueueEmpty(session);
        mLogger.log("Session queue is empty");
    }

    // onClosed.....................................................................................
    /**
     * This method is called when the session is closed.
     * @param session
     */
    @Override
    public void onClosed(CameraCaptureSession session) {
        super.onClosed(session);
        mLogger.log("Session is closed");
        //SurfaceManager.getInstance().done();
    }

    // onConfigured.................................................................................
    /**
     * This method is called when the camera device has finished configuring itself,
     * and the session can start processing capture requests.
     * (Required)
     * @param session
     */
    @Override
    public void onConfigured(CameraCaptureSession session) {
        //super.onConfigured(session); is abstract

        // bla bla bla set frame and exposure time etc

        // then call startrepeatingrequest
        CaptureSession captureSession = new CaptureSession(mNframes);
        startRepeatingRequest(session, captureSession);
        /*

        switch (mMSessionMode) {
            case Calibration: {
                mLogger.log("Beginning calibration run");
                CaptureSession callback = new CaptureSession(CustomSession.nCalibrationFrames);
                startRepeatingRequest(session, callback);
                break;
            }
            case Data: {
                mLogger.log("Beginning data run");
                //DataRun callback = new DataRun();
                //mSessionManager.startRepeatingRequest(session, callback);
                break;
            }
            default: {
                // TODO: error
            }
        }
        */
    }

    // onConfiguredFailed...........................................................................
    /**
     * This method is called if the session cannot be configured as requested.
     * (Required)
     * @param session
     */
    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        //super.onConfigureFailed(session); is abstract
        mLogger.log("Session configuration failed");
    }

    // onReady......................................................................................
    /**
     * This method is called every time the session has no more capture requests to process.
     * @param session
     */
    @Override
    public void onReady(CameraCaptureSession session) {
        super.onReady(session);
        mLogger.log("Session ready");
    }

    // onSurfacePrepared............................................................................
    /**
     * This method is called when the buffer pre-allocation for an output Surface is complete.
     * @param session
     * @param surface
     */
    @Override
    public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
        super.onSurfacePrepared(session, surface);
        mLogger.log("Surface prepared: " + surface.toString());
    }

}


        /*
        CaptureOverseer.mDataPath = DataManager.createDataDirectory(CaptureOverseer.mDateEpoch);

        CaptureOverseer.mNanoEpoch   = SystemClock.elapsedRealtimeNanos();
        CaptureOverseer.mFinishEpoch = CaptureOverseer.mNanoEpoch + EXPOSURE_DURATION_NANOS;
        */
