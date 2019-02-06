package Camera2;

import android.content.Context;

import Logging.DividerStyle;
import Logging.ShrampLogger;

public class MaineShrampCamera {

    private ShrampCameraManager mCameraManager;

    // Logging object
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final String TAG = "MaineShrampCamera";

    public MaineShrampCamera(Context context) {

        mLogger.divider(DividerStyle.Strong);
        mLogger.logTrace();

        mLogger.log(TAG, "Loading ShrampCameraManager");
        mCameraManager = ShrampCameraManager.getInstance(context);
        mLogger.divider(DividerStyle.Weak);

        assert mCameraManager != null;

        if (mCameraManager.hasFrontCamera()) {
            mLogger.log(TAG, "Opening front camera");
            mCameraManager.openFrontCamera();

            mLogger.log(TAG, "Closing front camera..");
            mCameraManager.closeFrontCamera();
        }
        else {
            mLogger.log(TAG, "No front camera detected");
        }

        if (mCameraManager.hasBackCamera()) {
            mLogger.log(TAG, "Back camera discovered");
            mLogger.log(TAG, "Opening back camera..");
            mCameraManager.openBackCamera();

            mLogger.log(TAG, "Closing back camera..");
            mCameraManager.closeBackCamera();
        }
        else {
            mLogger.log(TAG, "No back camera detected");
        }

        if (mCameraManager.hasExternalCamera()) {
            mLogger.log(TAG, "External camera discovered");
            mLogger.log(TAG, "Opening external camera..");
            mCameraManager.openExternalCamera();

            mLogger.log(TAG, "Closing external camera..");
            mCameraManager.closeExternalCamera();
        }
        else {
            mLogger.log(TAG, "No external camera detected");
        }

        mLogger.log(TAG, "return;");
    }
}
