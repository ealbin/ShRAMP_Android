package recycle_bin.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Size;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TreeMap;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * The ShrampCamManager class augments/wraps the Android CameraController.
 * Usage:
 *      // from an Activity or Frangment "this"
 *      ShrampCamManager myManager = ShrampCamManager.getInstance(this)
 *      if (myManager.hasBackCamera()) {
 *          myManager.openBackCamera();
 *      }
 */
@TargetApi(21) // Lollipop
public final class ShrampCamManager {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Enum
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Enum for selecting Front, Back, or External cameras
    private enum mSelect { FRONT, BACK, EXTERNAL }

    //**********************************************************************************************
    // Class Variables
    //----------------

     // There is only one ShrampCamManager instance in existence.
     // This is it's reference, access it with getInstance(Context)
    private static ShrampCamManager mInstance;

    // set in getInstance(Context)
    private static CameraManager                            mCameraManager;

    private static TreeMap<mSelect, String>                  mCameraIds;
    private static TreeMap<mSelect, CameraCharacteristics>   mCameraCharacteristics;
    private static TreeMap<mSelect, ShrampCam>               mShrampCameraDevices;

    private static ShrampCam mActiveCamera;

    // Lock to prevent multiple threads from opening a 2nd camera before closing the first
    private static final Object CAMERA_ACCESS_LOCK = new Object();

    // logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);


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
     * @param cameraManager Context to provide CAMERA_SERVICE and access to a CameraController object.
     * @return The single instance of ShrampCamManager, or null if something doesn't exist.
     */
    @Nullable
    public static synchronized ShrampCamManager getInstance(@NonNull CameraManager cameraManager) {

        if (mInstance != null) { return mInstance; }

        long startTime = SystemClock.elapsedRealtimeNanos();

        mLogger.log("Creating CameraController");
        mInstance      = new ShrampCamManager();
        mCameraManager = cameraManager;

        mLogger.log("Discovering cameras");
        String[] cameraIds;
        try {
            cameraIds = ShrampCamManager.mCameraManager.getCameraIdList();
        }
        catch (CameraAccessException e) {
            // TODO: ERROR
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
                    // TODO: report anomaly
                    mLogger.log("ERROR: lensFacing == null; continue;");
                    continue;
                }

                mSelect cameraKey = null;
                switch (lensFacing) {
                    case (CameraCharacteristics.LENS_FACING_FRONT) : {
                        cameraKey = mSelect.FRONT;
                        mLogger.log("Found front camera, ID: " + id);
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_BACK) : {
                        cameraKey = mSelect.BACK;
                        mLogger.log("Found back camera, ID: " + id);
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_EXTERNAL) : {
                        cameraKey = mSelect.EXTERNAL;
                        mLogger.log("Found external camera, ID: " + id);
                        break;
                    }
                }
                if (cameraKey == null) {
                    // TODO: report anomally
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

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return ShrampCamManager; elapsed = " + elapsed + " [ns]");
        return ShrampCamManager.mInstance;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Does this device have a front-facing camera (same side as the screen)
     * @return true if yes, false if no
     */
    public boolean hasFrontCamera() {
        boolean hasCamera = mCameraCharacteristics.containsKey(mSelect.FRONT);
        mLogger.log("return: " + Boolean.toString(hasCamera));
        return hasCamera;
    }

    /**
     * Does this device have a back-facing camera (opposite side as the screen)
     * @return true if yes, false if no
     */
    public boolean hasBackCamera() {
        boolean hasCamera = mCameraCharacteristics.containsKey(mSelect.BACK);
        mLogger.log("return: " + Boolean.toString(hasCamera));
        return hasCamera;
    }

    /**
     * Does this device have an external camera plugged in
     * @return true if yes, false if no
     */
    public boolean hasExternalCamera() {
        boolean hasCamera = mCameraCharacteristics.containsKey(mSelect.EXTERNAL);
        mLogger.log("return: " + Boolean.toString(hasCamera));
        return hasCamera;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static void cameraReady(ShrampCam shrampCam) {
        // if multiple cameras, wait for all to check in and collect their builders
        // when ready, send it over to the master manager
        mLogger.log("All cameras reporting ready");
        mActiveCamera = shrampCam;
        CameraDevice cameraDevice = shrampCam.getCameraDevice();
        mLogger.log("return via CaptureOverseer.cameraReady();");
        //CaptureOverseer.cameraReady(cameraDevice);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Open front camera device and configure it for capture
     */
    public synchronized boolean openFrontCamera() {
        long startTime = SystemClock.elapsedRealtimeNanos();

        if (!hasFrontCamera()) {
            // TODO no front camera
            mLogger.log("ERROR: No front camera; return false;");
            return false;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(mSelect.FRONT);
        assert characteristics != null;

        if (mShrampCameraDevices.containsKey(mSelect.FRONT)) {
            // TODO: camera already open
            mLogger.log("Camera already open");
        }
        else {
            mLogger.log("Creating CameraDevice");
            mShrampCameraDevices.put(mSelect.FRONT,
                                     new ShrampCam(characteristics, "shramp_front_cam"));
           openCamera(mSelect.FRONT);
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return true; elapsed = " + elapsed + " [ns]");
        return true;
    }

    /**
     * Open back camera device and configure it for capture
     */
    public synchronized boolean openBackCamera() {

        long startTime = SystemClock.elapsedRealtimeNanos();

        if (!hasBackCamera()) {
            // TODO: no back camera
            mLogger.log("ERROR: No back camera; return false;");
            return false;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(mSelect.BACK);
        assert characteristics != null;

        if (mShrampCameraDevices.containsKey(mSelect.BACK)) {
            // TODO: camera already open
            mLogger.log("Camera already open");
        }
        else {
            mLogger.log("Creating CameraDevice");
            mShrampCameraDevices.put(mSelect.BACK,
                                     new ShrampCam(characteristics, "shramp_back_cam"));
           openCamera(mSelect.BACK);
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return true; elapsed = " + elapsed + " [ns]");
        return true;
    }

    /**
     * Open external camera device and configure it for capture
     */
    public synchronized boolean openExternalCamera() {

        long startTime = SystemClock.elapsedRealtimeNanos();

        if (!hasExternalCamera()) {
            // TODO: no external camera
            mLogger.log("ERROR: No external camera; return false;");
            return false;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(mSelect.EXTERNAL);
        assert characteristics != null;

        if (mShrampCameraDevices.containsKey(mSelect.EXTERNAL)) {
            // TODO: camera already open
            mLogger.log("Camera already open");
        }
        else {
            mLogger.log("Creating CameraDevice");
            mShrampCameraDevices.put(mSelect.EXTERNAL,
                                     new ShrampCam(characteristics, "shramp_external_cam"));
           openCamera(mSelect.EXTERNAL);
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return true; elapsed = " + elapsed + " [ns]");
        return true;
    }

    /**
     * Create ShrampCam, instantiate camera callbacks, and open the camera
     * @param cameraKey mSelect which camera to open
     */
    private void openCamera(mSelect cameraKey) {

        long startTime = SystemClock.elapsedRealtimeNanos();

        synchronized (CAMERA_ACCESS_LOCK) {
            mLogger.log("Opening camera now");
            String    cameraId   = mCameraIds.get(cameraKey);
            assert    cameraId  != null;
            ShrampCam shrampCam  = mShrampCameraDevices.get(cameraKey);
            assert    shrampCam != null;

            try {
                mCameraManager.openCamera(cameraId, shrampCam, shrampCam.getHandler());
            }
            catch (SecurityException e) {
                // TODO: user hasn't granted permissions
                mLogger.log("ERROR: Security Exception; return;");
                return;
            }
            catch (CameraAccessException e) {
                // TODO: ERROR
                mLogger.log("ERROR: Camera Access Exception; return;");
                return;
            }
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Close front camera device and background threads
     */
    public synchronized void closeFrontCamera() {
        mLogger.log("Close front camera");
        closeCamera(mSelect.FRONT);
    }

    /**
     * Close back camera device and background threads
     */
    public synchronized void closeBackCamera() {
        mLogger.log("Close back camera");
        closeCamera(mSelect.BACK);
    }

    /**
     * Close back camera device and background threads
     */
    public synchronized void closeExternalCamera() {
        mLogger.log("Close external camera");
        closeCamera(mSelect.EXTERNAL);
    }

    /**
     * Close camera device and background threads
     * @param cameraKey mSelect which camera to close
     */
    private void closeCamera(mSelect cameraKey) {
        long startTime = SystemClock.elapsedRealtimeNanos();

        mActiveCamera = null;

        if (!mShrampCameraDevices.containsKey(cameraKey)) {
            mLogger.log("There was no camera to close; return;");
            return;
        }

        synchronized (CAMERA_ACCESS_LOCK) {
            mLogger.log("Closing camera now");
            try {
                mShrampCameraDevices.get(cameraKey).close();
            }
            catch (NullPointerException e) {
                mLogger.log("ERROR: null pointer exception; return;");
                return;
            }
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////\

    /**
     * @return ImageFormat constant int (either YUV_420_888 or RAW_SENSOR)
     */
    public static int getImageFormat() {
        return mActiveCamera.getShrampCamSettings().getOutputFormat();
    }

    public static int getImageBitsPerPixel() {
        return mActiveCamera.getShrampCamSettings().getBitsPerPixel();
    }

    public static Size getImageSize() {
        return mActiveCamera.getShrampCamSettings().getOutputSize();
    }

    public static Handler getCameraHandler() {
        return mActiveCamera.getHandler();
    }

    public static CaptureRequest.Builder getCaptureRequestBuilder() {
        return mActiveCamera.getCaptureRequestBuilder();
    }



}
