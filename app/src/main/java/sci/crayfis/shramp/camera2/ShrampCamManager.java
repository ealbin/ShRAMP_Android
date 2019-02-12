package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;

import java.util.List;
import java.util.TreeMap;

import sci.crayfis.shramp.MaineShRAMP;
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
class ShrampCamManager {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Enum
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Enum for selecting Front, Back, or External cameras
    private enum Select { FRONT, BACK, EXTERNAL }

    //**********************************************************************************************
    // Class Variables
    //----------------

     // There should be only one ShrampCamManager instance in existence.
     // This is it's reference, access it with getInstance(Context)
    private static ShrampCamManager mInstance;

    private static MaineShRAMP mActivity;
    private static Callback mCallback;

    // set in getInstance(Context)
    private static CameraManager                            mCameraManager;
    private static TreeMap<Select, String>                  mCameraIds;
    private static TreeMap<Select, CameraCharacteristics>   mCameraCharacteristics;
    private static TreeMap<Select, ShrampCam>      mShrampCameraDevices;

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
    private ShrampCamManager() {}

    /**
     * Get access to single instance camera manager.
     * Manages all camera devices (front, back, or external) present.
     * @param activity Context to provide CAMERA_SERVICE and access to a CameraManager object.
     * @return The single instance of ShrampCamManager, or null if something doesn't exist.
     */
    @Nullable
    static synchronized ShrampCamManager getInstance(@NonNull MaineShRAMP activity) {

        if (mInstance != null) { return mInstance; }

        mCallback = new Callback();

        mLogger.log("Creating CameraManager");
        mInstance      = new ShrampCamManager();
        mActivity = activity;
        mCameraManager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);

        if (mCameraManager == null) {
            // TODO report anomally
            mLogger.log("ERROR: mCameraManager == null; return null;");
            return null;
        }

        mCameraIds             = new TreeMap<>();
        mCameraCharacteristics = new TreeMap<>();
        mShrampCameraDevices   = new TreeMap<>();

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static class Callback {

        static TextureView mmTextureView;
        static List<Surface> mmSurfaces;


        static void cameraReady(ShrampCam shrampCam) {

            mLogger.log("mmSurface is " + mmSurfaces.size());
            mLogger.log("Ready to start capture");
            shrampCam.startCapture(mmSurfaces);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Open front camera device and configure it for capture
     */
    synchronized void openFrontCamera() {

        if (!hasFrontCamera()) {
            // TODO no front camera
            mLogger.log("ERROR: No front camera; return;");
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.FRONT);
        assert characteristics != null;

        mLogger.log("Creating CameraDevice");
        mShrampCameraDevices.put(Select.FRONT,
                new ShrampCam(characteristics, "shramp_front_cam"));

        openCamera(Select.FRONT);
        mLogger.log("return;");
    }

    /**
     * Open back camera device and configure it for capture
     */
    synchronized void openBackCamera() {

        if (!hasBackCamera()) {
            // TODO no back camera
            mLogger.log("ERROR: No back camera; return;");
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.BACK);
        assert characteristics != null;

        mLogger.log("Creating CameraDevice");
        mShrampCameraDevices.put(Select.BACK,
                new ShrampCam(characteristics, "shramp_back_cam"));

        openCamera(Select.BACK);
        mLogger.log("return;");
    }

    /**
     * Open external camera device and configure it for capture
     */
    synchronized void openExternalCamera() {

        if (!hasExternalCamera()) {
            // TODO no external camera
            mLogger.log("ERROR: No external camera; return;");
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.EXTERNAL);
        assert characteristics != null;

        mLogger.log("Creating CameraDevice");
        mShrampCameraDevices.put(Select.EXTERNAL,
                new ShrampCam(characteristics, "shramp_external_cam"));

        openCamera(Select.EXTERNAL);
        mLogger.log("return;");
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
    synchronized void closeFrontCamera() {

        mLogger.log("Close front camera");
        closeCamera(Select.FRONT);
        mLogger.log("return;");
    }

    /**
     * Close back camera device and background threads
     */
    synchronized void closeBackCamera() {

        mLogger.log("Close back camera");
        closeCamera(Select.BACK);
        mLogger.log("return;");
    }

    /**
     * Close back camera device and background threads
     */
    synchronized void closeExternalCamera() {

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

    ////////////////////////////////////////////////////////////////////////////////////////////////


}
