package sci.crayfis.shramp.camera2.capture;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Process;
import android.view.Surface;

import java.util.List;

import sci.crayfis.shramp.camera2.ShrampCamManager;
import sci.crayfis.shramp.util.HandlerManager;

public class CaptureManager extends CameraCaptureSession.StateCallback {

    //******************************************************************************************
    // Class Variables
    //----------------

    private enum Mode {Calibration, Data};

    private static final int N_CALIBRATION_FRAMES = 30;
    private static final int N_DATA_FRAMES        = 30;

    private static CaptureManager.Mode mMode = Mode.Calibration;

    private Handler mHandler;

    private CameraDevice  mCameraDevice;
    private List<Surface> mSurfaceList;


    //******************************************************************************************
    // Class Methods
    //--------------

    private CaptureManager() { super(); }

    public CaptureManager(CameraDevice cameraDevice, List<Surface> surfaceList) {
        this();
        mCameraDevice = cameraDevice;
        mSurfaceList  = surfaceList;
        mHandler = HandlerManager.newHandler("CaptureSession", Process.THREAD_PRIORITY_VIDEO);
    }

    public void createCaptureSession() {
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
        switch (mMode) {
            case Calibration: {
                CalibrationRun callback = new CalibrationRun(N_CALIBRATION_FRAMES);
                startRepeatingRequest(session, callback);
                break;
            }
            case Data: {
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
        for (Surface surface : mSurfaceList) {
            builder.addTarget(surface);
        }

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
    }

    /**
     * This method is called when the session starts actively processing capture requests.
     * @param session
     */
    @Override
    public void onActive(CameraCaptureSession session) {
        super.onActive(session);
    }

    /**
     * This method is called every time the session has no more capture requests to process.
     * @param session
     */
    @Override
    public void onReady(CameraCaptureSession session) {
        super.onReady(session);
    }

    /**
     * This method is called when camera device's input capture queue becomes empty,
     * and is ready to accept the next request.
     * @param session
     */
    @Override
    public void onCaptureQueueEmpty(CameraCaptureSession session) {
        super.onCaptureQueueEmpty(session);
    }

    /**
     * This method is called if the session cannot be configured as requested.
     * (Required)
     * @param session
     */
    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        //super.onConfigureFailed(session); is abstract
    }

    /**
     * This method is called when the session is closed.
     * @param session
     */
    @Override
    public void onClosed(CameraCaptureSession session) {
        super.onClosed(session);
        //SurfaceManager.getInstance().done();
    }


}


        /*
        CaptureOverseer.mDataPath = DataManager.createDataDirectory(CaptureOverseer.mDateEpoch);

        CaptureOverseer.mNanoEpoch   = SystemClock.elapsedRealtimeNanos();
        CaptureOverseer.mFinishEpoch = CaptureOverseer.mNanoEpoch + EXPOSURE_DURATION_NANOS;
        */
