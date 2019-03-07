package Trash;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Process;
import android.view.Surface;

import java.util.List;

import sci.crayfis.shramp.camera2.ShrampCamManager;
import sci.crayfis.shramp.camera2.capture.CaptureManager;
import sci.crayfis.shramp.util.HandlerManager;

class SessionManager {

    private CaptureManager mCaptureManager;
    private CameraDevice  mCameraDevice;
    private List<Surface> mSurfaceList;
    private Handler       mHandler;

    private CaptureRequest.Builder  mCaptureRequestBuilder;
    private CameraCaptureSession    mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;

    private SessionManager() {}

    SessionManager(CaptureManager captureManager, CameraDevice cameraDevice,
                   List<Surface> surfaceList) {
        mCaptureManager = captureManager;
        mCameraDevice = cameraDevice;
        mSurfaceList  = surfaceList;
        mHandler      = HandlerManager.newHandler("CaptureSession", Process.THREAD_PRIORITY_VIDEO);

        mCaptureRequestBuilder = ShrampCamManager.getCaptureRequestBuilder();
    }

    void createCaptureSession() {
        try {
            // Execution continues in mCaptureManager.onConfigured()
            mCameraDevice.createCaptureSession(mSurfaceList,
                    mCaptureManager,
                    mHandler);
        }
        catch (CameraAccessException e) {
            // TODO: error
        }
    }

    void startRepeatingRequest(CameraCaptureSession session,
                               CameraCaptureSession.CaptureCallback callback) {

        mCameraCaptureSession = session;

        for (Surface surface : mSurfaceList) {
            mCaptureRequestBuilder.addTarget(surface);
        }

        CaptureRequest captureRequest = mCaptureRequestBuilder.build();
        try {
            // Execution continues in CaptureCallback.onCaptureStarted()
            mCameraCaptureSession.setRepeatingRequest(captureRequest, mCaptureCallback, mHandler);
        }
        catch (CameraAccessException e) {
            // TODO: "ERROR: Camera Access Exception"
        }
    }

}
