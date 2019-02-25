package Trash;

import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;

import sci.crayfis.shramp.camera2.ShrampCamManager;
import sci.crayfis.shramp.logging.ShrampLogger;

/**
 *
 */
public class MaineShrampCam {

    //**********************************************************************************************
    // Class Variables
    //----------------

    private ShrampCamManager mCameraManager;

    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    public MaineShrampCam(@NonNull CameraManager cameraManager) {

        mLogger.log("Loading ShrampCamManager");
        mCameraManager = ShrampCamManager.getInstance(cameraManager);
        assert mCameraManager != null;

        //ShrampCamManager.Callback.mmSurfaces = new ArrayList<>();
        //ShrampCamManager.Callback.mmSurfaces.add(surface);

        if (mCameraManager.hasFrontCamera()) {
            // do nothing for now
        }

        if (mCameraManager.hasBackCamera()) {
            // try to open it and do capture
            mCameraManager.openBackCamera();
        }

        if (mCameraManager.hasExternalCamera()) {
            // do nothing for now
        }
    }

    private void createCaptureSession() {
        // configure camera to go here after onOpened()
        // make the surfaces and handlers you want
        // tell camera device to create capture session
    }

    private void startRepeatingRequest() {
        // configure cameracapturesession to come here when configured
    }

    private void startFrontCapture() {
        if (mCameraManager.hasFrontCamera()) {
            mLogger.log("Opening front camera");
            mCameraManager.openFrontCamera();
        } else {
            mLogger.log("No front camera detected");
        }
    }

    private void stopFrontCapture() {
        mLogger.log("Closing front camera");
        mCameraManager.closeFrontCamera();
    }

    private void startBackCapture() {
        if (mCameraManager.hasBackCamera()) {
            mLogger.log("Opening back camera");
            mCameraManager.openBackCamera();
        }
        else {
            mLogger.log("No back camera detected");
        }
    }

    private void stopBackCapture() {
        mLogger.log("Closing back camera");
        mCameraManager.closeBackCamera();
    }

    private void startExternalCapture() {
        if (mCameraManager.hasExternalCamera()) {
            mLogger.log("Opening external camera");
            mCameraManager.openExternalCamera();
        }
        else {
            mLogger.log("No external camera detected");
        }
        mLogger.log("return;");
    }

    private void stopExternalCapture() {
        mLogger.log("Closing external camera");
        mCameraManager.closeExternalCamera();
    }

}
