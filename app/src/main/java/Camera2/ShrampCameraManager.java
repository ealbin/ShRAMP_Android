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
public class ShrampCameraManager {

    /**
     * Enum for selecting Front, Back, or External cameras
     */
    private enum Select { FRONT, BACK, EXTERNAL }

    /**
     * There should be only one ShrampCameraManager instance in existence.
     * This is it's reference, access it with getInstance(Context)
     */
    private static ShrampCameraManager mInstance;

    // set in getInstance(Context)
    private static CameraManager                            mCameraManager;
    private static TreeMap<Select, String>                  mCameraIds;
    private static TreeMap<Select, CameraCharacteristics>   mCameraCharacteristics;
    private static TreeMap<Select, ShrampCameraDevice>      mShrampCameraDevices;

    /**
     * Logging object
     */
    private static ShrampLogger mLogger;
    private static final String TAG = "ShrampCameraManager";


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
    public static synchronized ShrampCameraManager getInstance(@NonNull Context context) {

        if (mInstance != null) { return mInstance; }

        mLogger = new ShrampLogger(OutStream.LOG_E);
        mLogger.divider();
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
                lensFacing            = cameraCharacteristics.get(
                        CameraCharacteristics.LENS_FACING);

                if (lensFacing == null) {
                    // TODO report anomaly
                    mLogger.log(TAG, "ERROR:  lensFacing == null");
                    continue;
                }

                Select cameraKey = null;
                switch (lensFacing) {
                    case (CameraCharacteristics.LENS_FACING_FRONT) : {
                        cameraKey = Select.FRONT;
                        mLogger.log(TAG, "found front camera");
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_BACK) : {
                        cameraKey = Select.BACK;
                        mLogger.log(TAG, "found back camera");
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_EXTERNAL) : {
                        cameraKey = Select.EXTERNAL;
                        mLogger.log(TAG, "found external camera");
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

        mLogger.log(TAG, "return mInstance;");
        return mInstance;
    }

    /**
     * Does this device have a front-facing camera (same side as the screen)
     * @return true if yes, false if no
     */
    public static boolean hasFrontCamera() { return mShrampCameraDevices.containsKey(Select.FRONT); }

    /**
     * Does this device have a back-facing camera (opposite side as the screen)
     * @return true if yes, false if no
     */
    public static boolean hasBackCamera() { return mShrampCameraDevices.containsKey(Select.BACK); }

    /**
     * Does this device have an external camera plugged in
     * @return true if yes, false if no
     */
    public static boolean hasExternalCamera() { return mShrampCameraDevices.containsKey(Select.EXTERNAL); }

    /**
     * Open front camera device and configure it for capture
     */
    public static synchronized void openFrontCamera() {
        final String LTAG = TAG.concat("openFrontCamera()");
        mLogger.divider();
        mLogger.logTrace();

        if (!hasFrontCamera()) {
            // TODO no front camera
            mLogger.log(LTAG, "ERROR:  no front camera");
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
     * Open back camera device and configure it for capture
     */
    public static synchronized void openBackCamera() {
        final String LTAG = TAG.concat("openBackCamera()");
        mLogger.divider();
        mLogger.logTrace();

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
     * Open external camera device and configure it for capture
     */
    public static synchronized void openExternalCamera() {
        final String LTAG = TAG.concat("openExternalCamera()");
        mLogger.divider();
        mLogger.logTrace();

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
     * Create ShrampCameraDevice, instantiate camera callbacks, and open the camera
     * @param cameraKey Select which camera to open
     */
    private static synchronized void openCamera(Select cameraKey) {
        final String LTAG = TAG.concat("openCamera(Select)");
        mLogger.divider();
        mLogger.logTrace();

        String                     cameraId            = mCameraIds.get(cameraKey);
        ShrampCameraDevice         shrampCameraDevice  = mShrampCameraDevices.get(cameraKey);
        assert                     shrampCameraDevice != null;
        CameraDevice.StateCallback stateCallback       = shrampCameraDevice.getStateCallback();
        Handler                    handler             = shrampCameraDevice.getHandler();

        try {
            assert cameraId != null;
            mCameraManager.openCamera(cameraId, stateCallback, handler);
        }
        catch (SecurityException e ) {
            // TODO user hasn't granted permissions
            mLogger.log(LTAG, "ERROR:  Security Exception");
        }
        catch (CameraAccessException e ) {
            // TODO ERROR
            mLogger.log(LTAG, "ERROR:  Camera Access Exception");
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
    public CameraManager.AvailabilityCallback getmAvailabilityCallback() {
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
