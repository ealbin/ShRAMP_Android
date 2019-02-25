package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.TreeMap;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * The ShrampCamManager class augments/wraps the Android CameraManager.
 * Usage:
 *      // from an Activity or Frangment "this"
 *      ShrampCamManager myManager = ShrampCamManager.getInstance(this)
 *      if (myManager.hasBackCamera()) {
 *          myManager.openBackCamera();
 *      }
 */
@TargetApi(21) // Lollipop
public class ShrampCamManager {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Enum
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Enum for selecting Front, Back, or External cameras
    private enum Select { FRONT, BACK, EXTERNAL }

    //**********************************************************************************************
    // Class Variables
    //----------------

     // There is only one ShrampCamManager instance in existence.
     // This is it's reference, access it with getInstance(Context)
    private static ShrampCamManager mInstance;

    //private static MaineShRAMP mActivity;
    //private static Callback mCallback;

    // set in getInstance(Context)
    private static CameraManager                            mCameraManager;
    private static TreeMap<Select, String>                  mCameraIds;
    private static TreeMap<Select, CameraCharacteristics>   mCameraCharacteristics;
    private static TreeMap<Select, ShrampCam>               mShrampCameraDevices;

    // Lock to prevent multiple threads from opening a 2nd camera before closing the first
    private static final Object ACTION_LOCK = new Object();

    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Disable default constructor to limit access to getInstance(Context)
     */
    private ShrampCamManager() {
        mCameraIds             = new TreeMap<>();
        mCameraCharacteristics = new TreeMap<>();
        mShrampCameraDevices   = new TreeMap<>();
    }

    /**
     * Get access to single instance camera manager.
     * Manages all camera devices (front, back, or external) present.
     * @param cameraManager Context to provide CAMERA_SERVICE and access to a CameraManager object.
     * @return The single instance of ShrampCamManager, or null if something doesn't exist.
     */
    @Nullable
    public static synchronized ShrampCamManager getInstance(@NonNull CameraManager cameraManager) {

        if (mInstance != null) { return mInstance; }

        //mCallback = new Callback();

        mLogger.log("Creating CameraManager");
        mInstance      = new ShrampCamManager();
        mCameraManager = cameraManager;

        mLogger.log("Discovering cameras");
        String[] cameraIds;
        try {
            cameraIds = mCameraManager.getCameraIdList();
        }
        catch (CameraAccessException e) {
            // TODO ERROR
            mLogger.log("ERROR: Camera Access Exception; return null;");
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
                    mLogger.log("ERROR: lensFacing == null; continue;");
                    continue;
                }

                Select cameraKey = null;
                switch (lensFacing) {
                    case (CameraCharacteristics.LENS_FACING_FRONT) : {
                        cameraKey = Select.FRONT;
                        mLogger.log("Found front camera, ID: " + id);
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_BACK) : {
                        cameraKey = Select.BACK;
                        mLogger.log("Found back camera, ID: " + id);
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_EXTERNAL) : {
                        cameraKey = Select.EXTERNAL;
                        mLogger.log("Found external camera, ID: " + id);
                        break;
                    }
                }
                if (cameraKey == null) {
                    // TODO report anomally
                    mLogger.log("ERROR: cameraKey == null; continue;");
                    continue;
                }

                mCameraIds.put(cameraKey, id);
                mCameraCharacteristics.put(cameraKey, cameraCharacteristics);
            }
            catch (CameraAccessException e) {
                // TODO ERROR
                mLogger.log("ERROR: Camera Access Exception; return null;");
                return null;
            }
        }

        mLogger.log("return ShrampCamManager;");
        return mInstance;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Does this device have a front-facing camera (same side as the screen)
     * @return true if yes, false if no
     */
    public boolean hasFrontCamera() {
        boolean hasCamera = mCameraCharacteristics.containsKey(Select.FRONT);
        mLogger.log("return: " + Boolean.toString(hasCamera));
        return hasCamera;
    }

    /**
     * Does this device have a back-facing camera (opposite side as the screen)
     * @return true if yes, false if no
     */
    public boolean hasBackCamera() {
        boolean hasCamera = mCameraCharacteristics.containsKey(Select.BACK);
        mLogger.log("return: " + Boolean.toString(hasCamera));
        return hasCamera;
    }

    /**
     * Does this device have an external camera plugged in
     * @return true if yes, false if no
     */
    public boolean hasExternalCamera() {
        boolean hasCamera = mCameraCharacteristics.containsKey(Select.EXTERNAL);
        mLogger.log("return: " + Boolean.toString(hasCamera));
        return hasCamera;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static void cameraReady(ShrampCam shrampCam) {
        // if multiple cameras, wait for all to check in and collect their builders
        // when ready, send it over to the master manager
        mLogger.log("All cameras reporting ready");
        CameraDevice cameraDevice = shrampCam.getCameraDevice();
        CaptureRequest.Builder captureRequestBuilder = shrampCam.getCaptureRequestBuilder();
        CaptureOverseer.cameraReady(cameraDevice, captureRequestBuilder);
        mLogger.log("return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Open front camera device and configure it for capture
     */
    public synchronized boolean openFrontCamera() {

        if (!hasFrontCamera()) {
            // TODO no front camera
            mLogger.log("ERROR: No front camera; return false;");
            return false;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.FRONT);
        assert characteristics != null;

        if (mShrampCameraDevices.containsKey(Select.FRONT)) {
            // TODO: camera already open
            mLogger.log("Camera already open");
        }
        else {
            mLogger.log("Creating CameraDevice");
            mShrampCameraDevices.put(Select.FRONT,
                    new ShrampCam(characteristics, "shramp_front_cam"));
            openCamera(Select.FRONT);
        }
        mLogger.log("return true;");
        return true;
    }

    /**
     * Open back camera device and configure it for capture
     */
    public synchronized boolean openBackCamera() {

        if (!hasBackCamera()) {
            // TODO no back camera
            mLogger.log("ERROR: No back camera; return false;");
            return false;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.BACK);
        assert characteristics != null;

        if (mShrampCameraDevices.containsKey(Select.BACK)) {
            // TODO: camera already open
            mLogger.log("Camera already open");
        }
        else {
            mLogger.log("Creating CameraDevice");
            mShrampCameraDevices.put(Select.BACK,
                    new ShrampCam(characteristics, "shramp_back_cam"));
            openCamera(Select.BACK);
        }
        mLogger.log("return true;");
        return true;
    }

    /**
     * Open external camera device and configure it for capture
     */
    public synchronized boolean openExternalCamera() {

        if (!hasExternalCamera()) {
            // TODO no external camera
            mLogger.log("ERROR: No external camera; return false;");
            return false;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.EXTERNAL);
        assert characteristics != null;

        if (mShrampCameraDevices.containsKey(Select.EXTERNAL)) {
            // TODO: camera already open
            mLogger.log("Camera already open");
        }
        else {
            mLogger.log("Creating CameraDevice");
            mShrampCameraDevices.put(Select.EXTERNAL,
                    new ShrampCam(characteristics, "shramp_external_cam"));

            openCamera(Select.EXTERNAL);
        }
        mLogger.log("return true;");
        return true;
    }

    /**
     * Create ShrampCam, instantiate camera callbacks, and open the camera
     * @param cameraKey Select which camera to open
     */
    private void openCamera(Select cameraKey) {

        synchronized (ACTION_LOCK) {
            mLogger.log("Opening camera now");
            String                     cameraId   = mCameraIds.get(cameraKey);
            assert                     cameraId  != null;
            ShrampCam shrampCam = mShrampCameraDevices.get(cameraKey);
            assert                     shrampCam != null;

            try {
                mCameraManager.openCamera(cameraId, shrampCam, shrampCam.getHandler());
            }
            catch (SecurityException e) {
                // TODO user hasn't granted permissions
                mLogger.log("ERROR: Security Exception; return;");
                return;
            }
            catch (CameraAccessException e) {
                // TODO ERROR
                mLogger.log("ERROR: Camera Access Exception; return;");
                return;
            }
        }

        mLogger.log("return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Close front camera device and background threads
     */
    public synchronized void closeFrontCamera() {

        mLogger.log("Close front camera");
        closeCamera(Select.FRONT);
        mLogger.log("return;");
    }

    /**
     * Close back camera device and background threads
     */
    public synchronized void closeBackCamera() {

        mLogger.log("Close back camera");
        closeCamera(Select.BACK);
        mLogger.log("return;");
    }

    /**
     * Close back camera device and background threads
     */
    public synchronized void closeExternalCamera() {

        mLogger.log("Close external camera");
        closeCamera(Select.EXTERNAL);
        mLogger.log("return;");
    }

    /**
     * Close camera device and background threads
     * @param cameraKey Select which camera to close
     */
    private void closeCamera(Select cameraKey) {

        if (!mShrampCameraDevices.containsKey(cameraKey)) {
            mLogger.log("There was no camera to close; return;");
            return;
        }

        synchronized (ACTION_LOCK) {
            mLogger.log("Closing camera now");
            try {
                mShrampCameraDevices.get(cameraKey).close();
                Thread.sleep(100);
            }
            catch (NullPointerException e) {
                mLogger.log("ERROR: null pointer exception; return;");
                return;
            }
            catch (InterruptedException e) {
                mLogger.log("ERROR: sleep interrupted exception; return;");
                return;
            }
        }

        mLogger.log("return;");
    }

}
