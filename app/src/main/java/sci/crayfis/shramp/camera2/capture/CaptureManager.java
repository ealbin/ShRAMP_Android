package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
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
import Trash.camera2.ShrampCamManager;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HandlerManager;

@TargetApi(21)
public class CaptureManager extends CameraCaptureSession.StateCallback {

    //******************************************************************************************
    // Class Variables
    //----------------

    private enum mSessionMode {Calibration, Data};

    private static CaptureManager mInstance;

    private static mSessionMode mMSessionMode = mSessionMode.Calibration;

    private static Handler mHandler;

    private CameraDevice  mCameraDevice;
    private List<Surface> mSurfaceList;

    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    private static final DecimalFormat mFormatter = new DecimalFormat("##.#");

    private static class CustomSession {
        static final int N_CALIBRATION_FRAMES = 30;
        static final int N_DATA_FRAMES        = 30;

        static boolean isCustomSession;
        static int     nCalibrationFrames;
        static int     nDataFrames;
        static Long    frameExposureNanos;

        static void customizeBuilder(CaptureRequest.Builder builder) {
            if (frameExposureNanos != null) {
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, frameExposureNanos);
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, frameExposureNanos);
            }
        }

        static void clear() {
            isCustomSession    = false;
            nCalibrationFrames = N_CALIBRATION_FRAMES;
            nDataFrames        = N_DATA_FRAMES;
            frameExposureNanos = null;
        }
    }


    //******************************************************************************************
    // Class Methods
    //--------------

    private CaptureManager() {
        super();
        CustomSession.clear();
    }

    public CaptureManager(CameraDevice cameraDevice, List<Surface> surfaceList) {
        this();
        mInstance = this;
        mCameraDevice = cameraDevice;
        mSurfaceList  = surfaceList;
        mHandler = HandlerManager.newHandler("CaptureSession", Process.THREAD_PRIORITY_VIDEO);
    }

    public void createCaptureSession() {
        mLogger.log("Configuring capture session");
        try {
            // Execution continues in onConfigured()
            mCameraDevice.createCaptureSession(mSurfaceList, this, mHandler);
        }
        catch (CameraAccessException e) {
            // TODO: error
        }
    }

    /**
     * This method is called when the camera device has finished configuring itself,
     * and the session can start processing capture requests.
     * (Required)
     * @param session
     */
    @Override
    public void onConfigured(CameraCaptureSession session) {
        //super.onConfigured(session); is abstract
        switch (mMSessionMode) {
            case Calibration: {
                mLogger.log("Beginning calibration run");
                CalibrationRun callback = new CalibrationRun(CustomSession.nCalibrationFrames);
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
    }

    private void startRepeatingRequest(CameraCaptureSession session,
                                       CameraCaptureSession.CaptureCallback callback) {

        CaptureRequest.Builder builder = ShrampCamManager.getCaptureRequestBuilder();
        CustomSession.customizeBuilder(builder);

        if (!CustomSession.isCustomSession) {
            for (Surface surface : mSurfaceList) {
                builder.addTarget(surface);
            }
        }
        CustomSession.clear();

        CaptureRequest captureRequest = builder.build();

        try {
            // Execution continues in CaptureCallback.onCaptureStarted()
            session.setRepeatingRequest(captureRequest, callback, mHandler);
        }
        catch (CameraAccessException e) {
            // TODO: "ERROR: Camera Access Exception"
        }
    }

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

    /**
     * This method is called when the session starts actively processing capture requests.
     * @param session
     */
    @Override
    public void onActive(CameraCaptureSession session) {
        super.onActive(session);
        mLogger.log("Session active");
    }

    /**
     * This method is called every time the session has no more capture requests to process.
     * @param session
     */
    @Override
    public void onReady(CameraCaptureSession session) {
        super.onReady(session);
        mLogger.log("Session ready");
    }

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

    public static void sessionFinished(CameraCaptureSession session,
                                       CameraCaptureSession.CaptureCallback callback,
                                       double averageFps, double averageDuty) {
        String string = " \n";
        string += "Session performance: \n";
        string += "\t Average FPS:  " + mFormatter.format(averageFps) + " [frames / sec] \n";
        string += "\t Average Duty: " + mFormatter.format(averageDuty * 100.) + " % \n";
        mLogger.log(string);

        // TODO: if duty is less than 95%, try again with fps at the average
        if (averageDuty < 0.95) {
            CustomSession.isCustomSession = true;
            CustomSession.frameExposureNanos = Double.doubleToLongBits(Math.floor(1e9 / averageFps));

            mLogger.log("Restarting session with new frame rate target: "
                    + mFormatter.format(1. / ( CustomSession.frameExposureNanos * 1e-9) )
                    + " [frames / sec]");

            Runnable waitForImageProcessor = new Runnable() {
                Runnable restartCapture = new Runnable() {
                    @Override
                    public void run() {
                        mInstance.createCaptureSession();
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
            mLogger.log("Quitting");
            session.close();
            CaptureOverseer.quitSafely();
        }
    }


}


        /*
        CaptureOverseer.mDataPath = DataManager.createDataDirectory(CaptureOverseer.mDateEpoch);

        CaptureOverseer.mNanoEpoch   = SystemClock.elapsedRealtimeNanos();
        CaptureOverseer.mFinishEpoch = CaptureOverseer.mNanoEpoch + EXPOSURE_DURATION_NANOS;
        */
