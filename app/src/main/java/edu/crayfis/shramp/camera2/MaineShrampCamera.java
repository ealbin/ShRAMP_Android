package edu.crayfis.shramp.camera2;

import android.content.Context;

import edu.crayfis.shramp.logging.ShrampLogger;

public class MaineShrampCamera {

    private ShrampCameraManager mCameraManager;

    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    public MaineShrampCamera(Context context) {

        mLogger.log("Loading ShrampCameraManager");
        mCameraManager = ShrampCameraManager.getInstance(context);
        assert mCameraManager != null;

        if (mCameraManager.hasFrontCamera()) {
            mLogger.log("Opening front camera");
            mCameraManager.openFrontCamera();

            mLogger.log("Closing front camera");
            mCameraManager.closeFrontCamera();
        }
        else {
            mLogger.log("No front camera detected");
        }

        if (mCameraManager.hasBackCamera()) {
            mLogger.log("Opening back camera");
            mCameraManager.openBackCamera();

            mLogger.log("Closing back camera");
            mCameraManager.closeBackCamera();
        }
        else {
            mLogger.log("No back camera detected");
        }

        if (mCameraManager.hasExternalCamera()) {
            mLogger.log("Opening external camera");
            mCameraManager.openExternalCamera();

            mLogger.log("Closing external camera");
            mCameraManager.closeExternalCamera();
        }
        else {
            mLogger.log("No external camera detected");
        }

        mLogger.log("return;");
    }
}
