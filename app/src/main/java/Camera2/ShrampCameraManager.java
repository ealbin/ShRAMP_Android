package Camera2;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.TreeMap;

import Logging.DividerStyle;
import Logging.OutStream;
import Logging.ShrampLogger;

/**
 * The ShrampCameraManager class augments/wraps the Android CameraManager.
 * Usage:
 *      // from an Activity or Frangment "this"
 *      ShrampCameraManager myManager = ShrampCameraManager.getInstance(this)
 *      if (myManager.hasBackCamera()) {
 *          myManager.openBackCamera();
 *      }
 */
@TargetApi(21) // Lollipop
class ShrampCameraManager {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Enum
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Enum for selecting Front, Back, or External cameras
    private enum Select { FRONT, BACK, EXTERNAL }


    //**********************************************************************************************
    // Class Variables
    //----------------


     // There should be only one ShrampCameraManager instance in existence.
     // This is it's reference, access it with getInstance(Context)
    private static ShrampCameraManager mInstance;

    // set in getInstance(Context)
    private static CameraManager                            mCameraManager;
    private static TreeMap<Select, String>                  mCameraIds;
    private static TreeMap<Select, CameraCharacteristics>   mCameraCharacteristics;
    private static TreeMap<Select, ShrampCameraDevice>      mShrampCameraDevices;

    // Logging object
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final String TAG = "ShrampCameraManager";

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Disable default constructor to limit access to getInstance(Context)
     */
    private ShrampCameraManager() {}

    /**
     * Get access to single instance camera manager.
     * Manages all camera devices (front, back, or external) present.
     * @param context Context to provide CAMERA_SERVICE and access to a CameraManager object.
     * @return The single instance of ShrampCameraManager, or null if something doesn't exist.
     */
    @Nullable
    static synchronized ShrampCameraManager getInstance(@NonNull Context context) {

        if (mInstance != null) { return mInstance; }

        mLogger.divider(DividerStyle.Strong);
        mLogger.logTrace();

        mInstance      = new ShrampCameraManager();
        mCameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);

        if (mCameraManager == null) {
            // TODO report anomally
            mLogger.log(TAG, "ERROR: mCameraManager == null");
            return null;
        }

        mCameraIds             = new TreeMap<>();
        mCameraCharacteristics = new TreeMap<>();
        mShrampCameraDevices   = new TreeMap<>();

        String[] cameraIds;
        try {
            cameraIds = mCameraManager.getCameraIdList();
        }
        catch (CameraAccessException e) {
            // TODO ERROR
            mLogger.log(TAG, "ERROR: mCameraManager.getCameraIdList() Access Exception");
            return null;
        }

        for (String id : cameraIds) {
            CameraCharacteristics cameraCharacteristics;
            Integer               lensFacing;

            try {
                cameraCharacteristics = mCameraManager.getCameraCharacteristics(id);
                lensFacing            = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);

                if (lensFacing == null) {
                    // TODO report anomaly
                    mLogger.log(TAG, "ERROR:  lensFacing == null");
                    continue;
                }

                Select cameraKey = null;
                switch (lensFacing) {
                    case (CameraCharacteristics.LENS_FACING_FRONT) : {
                        cameraKey = Select.FRONT;
                        mLogger.log(TAG, "Found front camera");
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_BACK) : {
                        cameraKey = Select.BACK;
                        mLogger.log(TAG, "Found back camera");
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_EXTERNAL) : {
                        cameraKey = Select.EXTERNAL;
                        mLogger.log(TAG, "Found external camera");
                        break;
                    }
                }
                if (cameraKey == null) {
                    // TODO report anomally
                    mLogger.log(TAG, "ERROR:  cameraKey == null");
                    continue;
                }

                mCameraIds.put(cameraKey, id);
                mCameraCharacteristics.put(cameraKey, cameraCharacteristics);
            }
            catch (CameraAccessException e) {
                // TODO ERROR
                mLogger.log(TAG, "ERROR:  mCameraManager.getCameraCharacteristics(id) "
                        + "Access Exception");
                return null;
            }
        }

        mLogger.log(TAG, "return ShrampCameraManager;");
        return mInstance;
    }

    /**
     * Does this device have a front-facing camera (same side as the screen)
     * @return true if yes, false if no
     */
    boolean hasFrontCamera() { return mCameraCharacteristics.containsKey(Select.FRONT); }

    /**
     * Does this device have a back-facing camera (opposite side as the screen)
     * @return true if yes, false if no
     */
    boolean hasBackCamera() { return mCameraCharacteristics.containsKey(Select.BACK); }

    /**
     * Does this device have an external camera plugged in
     * @return true if yes, false if no
     */
    boolean hasExternalCamera() { return mCameraCharacteristics.containsKey(Select.EXTERNAL); }

    /**
     * Open front camera device and configure it for capture
     */
    synchronized void openFrontCamera() {
        final String LTAG = TAG.concat("openFrontCamera()");

        if (!hasFrontCamera()) {
            // TODO no front camera
            mLogger.log(LTAG, "ERROR:  no front camera");
            mLogger.log(LTAG, "return;");
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.FRONT);
        assert characteristics != null;
        mShrampCameraDevices.put(Select.FRONT,
                new ShrampCameraDevice(characteristics, "shramp_front_cam"));

        mLogger.log(LTAG, "openCamera(Select.FRONT)");
        openCamera(Select.FRONT);
        mLogger.log(LTAG, "return;");
    }

    /**
     * Close front camera device and background threads
     */
    synchronized void closeFrontCamera() {
        final String LTAG = TAG.concat("closeFrontCamera()");
        mLogger.divider(DividerStyle.Weak);
        mLogger.logTrace();

        closeCamera(Select.FRONT);
        mLogger.log(LTAG, "return;");
    }

    /**
     * Open back camera device and configure it for capture
     */
    synchronized void openBackCamera() {
        final String LTAG = TAG.concat("openBackCamera()");

        if (!hasBackCamera()) {
            // TODO no back camera
            mLogger.log(LTAG, "ERROR:  no back camera");
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.BACK);
        assert characteristics != null;
        mShrampCameraDevices.put(Select.BACK,
                new ShrampCameraDevice(characteristics, "shramp_back_cam"));

        mLogger.log(LTAG, "openCamera(Select.BACK)");
        openCamera(Select.BACK);
        mLogger.log(LTAG, "return;");
    }

    /**
     * Close back camera device and background threads
     */
    synchronized void closeBackCamera() {
        final String LTAG = TAG.concat("closeBackCamera()");
        mLogger.divider(DividerStyle.Weak);
        mLogger.logTrace();

        closeCamera(Select.BACK);
        mLogger.log(LTAG, "return;");
    }

    /**
     * Open external camera device and configure it for capture
     */
    synchronized void openExternalCamera() {
        final String LTAG = TAG.concat("openExternalCamera()");

        if (!hasExternalCamera()) {
            // TODO no external camera
            mLogger.log(LTAG, "ERROR:  no external camera");
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.EXTERNAL);
        assert characteristics != null;
        mShrampCameraDevices.put(Select.EXTERNAL,
                new ShrampCameraDevice(characteristics, "shramp_external_cam"));

        mLogger.log(LTAG, "openCamera(Select.EXTERNAL)");
        openCamera(Select.EXTERNAL);
        mLogger.log(LTAG, "return;");
    }

    /**
     * Close back camera device and background threads
     */
    synchronized void closeExternalCamera() {
        final String LTAG = TAG.concat("closeExternalCamera()");
        mLogger.divider(DividerStyle.Weak);
        mLogger.logTrace();

        closeCamera(Select.EXTERNAL);
        mLogger.log(LTAG, "return;");
    }

    /**
     * Create ShrampCameraDevice, instantiate camera callbacks, and open the camera
     * @param cameraKey Select which camera to open
     */
    private synchronized void openCamera(Select cameraKey) {
        final String LTAG = TAG.concat("openCamera(Select)");
        mLogger.logTrace();

        String                     cameraId            = mCameraIds.get(cameraKey);
        assert                     cameraId           != null;
        ShrampCameraDevice         shrampCameraDevice  = mShrampCameraDevices.get(cameraKey);
        assert                     shrampCameraDevice != null;

        CameraDevice.StateCallback stateCallback       = shrampCameraDevice.getStateCallback();
        Handler                    handler             = shrampCameraDevice.getHandler();

        mLogger.log(LTAG, "Opening camera now");
        try {
            mCameraManager.openCamera(cameraId, stateCallback, handler);
        }
        catch (SecurityException e) {
            // TODO user hasn't granted permissions
            mLogger.log(LTAG, "ERROR:  Security Exception");
        }
        catch (CameraAccessException e) {
            // TODO ERROR
            mLogger.log(LTAG, "ERROR:  Camera Access Exception");
        }
        mLogger.log(LTAG, "return;");
    }

    /**
     * Close camera device and background threads
     * @param cameraKey Select which camera to close
     */
    private synchronized void closeCamera(Select cameraKey) {
        final String LTAG = TAG.concat("closeCamera(Select)");
        mLogger.divider(DividerStyle.Weak);
        mLogger.logTrace();

        if (!mShrampCameraDevices.containsKey(cameraKey)) {
            mLogger.log(LTAG, "There was no camera to close");
            return;
        }

        try {
            mShrampCameraDevices.get(cameraKey).close();
        }
        catch (NullPointerException e) {
            mLogger.log(LTAG, "ERROR:  null pointer exception");
        }

        mLogger.log(LTAG, "return;");
    }

    // unfinished / unused:
    //---------------------

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested static abstract AvailabilityCallback class /////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private CameraManager.AvailabilityCallback mAvailabilityCallback
            = new CameraManager.AvailabilityCallback() {

        // set in onCameraAvailable, onCameraUnavailable
        private String mCameraId;

        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            super.onCameraAvailable(cameraId);

            mCameraId = cameraId;
        }

        @Override
        public void onCameraUnavailable(@NonNull String cameraId) {
            super.onCameraUnavailable(cameraId);

            mCameraId = cameraId;
        }
    };

    // access mAvailabilityCallback
    CameraManager.AvailabilityCallback getmAvailabilityCallback() {
        return mAvailabilityCallback;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested static abstract TorchCallback class ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // (API 23)
    /*
    private CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {

        // set in onTorchModeUnavailable, onTorchModeChanged
        private String  mCameraId;

        // set in onTorchModeChanged
        private boolean mEnabled;

        @Override
        public void onTorchModeUnavailable(@NonNull String cameraId) {
            super.onTorchModeUnavailable(cameraId);

            mCameraId = cameraId;
        }

        @Override
        public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);

            mCameraId = cameraId;
            mEnabled  = enabled;
        }
    };

    // access mTorchCallback
    public CameraManager.TorchCallback getmTorchCallback() {
        return mTorchCallback;
    }
    */
}
