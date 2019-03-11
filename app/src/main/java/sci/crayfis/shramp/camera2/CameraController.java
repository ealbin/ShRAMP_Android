package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;

import java.util.HashMap;

import sci.crayfis.shramp.util.HandlerManager;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class CameraController {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Public Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Select.......................................................................................
    // TODO: description
    public enum Select {FRONT, BACK, EXTERNAL}

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = Process.THREAD_PRIORITY_DEFAULT;

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "CameraControllerThread";

    // Private Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mCameras.....................................................................................
    // TODO: description
    private static final HashMap<Select, Camera> mCameras  = new HashMap<>();

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME, PRIORITY);

    // mInstance....................................................................................
    // TODO: description
    private static final CameraController mInstance = new CameraController();

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mCameraManager...............................................................................
    // TODO: description
    private CameraManager mCameraManager;

    // mNextHandler.................................................................................
    // TODO: description
    private Handler mNextHandler;

    // mNextRunnable................................................................................
    // TODO: description
    private Runnable mNextRunnable;

    // mOpenCamera..................................................................................
    // TODO: description
    private Camera mOpenCamera;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CameraController.............................................................................
    private CameraController() {
        mOpenCamera = null;
        Log.e("CameraControllerClass", ".");
        Log.e("CameraControllerClass", "CameraController()");
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // closeCamera..................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void closeCamera() {
        Log.e("CameraControllerClass", "closeCamera()");

        if (mInstance.mOpenCamera == null) {
            return;
        }
        mInstance.mOpenCamera.close();
    }

    // discoverCameras..............................................................................
    /**
     * TODO: description, comments and logging
     * @param cameraManager
     */
    public static void discoverCameras(@NonNull CameraManager cameraManager) {
        Log.e("CameraControllerClass", "discoverCameras()");

        mInstance.mCameraManager = cameraManager;

        String[] cameraIds = null;
        try {
            cameraIds = mInstance.mCameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics cameraCharacteristics
                        = mInstance.mCameraManager.getCameraCharacteristics(id);
                Integer lens_facing  = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                assert  lens_facing != null;

                switch (lens_facing) {
                    case (CameraCharacteristics.LENS_FACING_FRONT): {
                        Camera camera = new Camera("FrontCamera", id, cameraCharacteristics);
                        mCameras.put(Select.FRONT, camera);
                        break;
                    }

                    case (CameraCharacteristics.LENS_FACING_BACK): {
                        Camera camera = new Camera("BackCamera", id, cameraCharacteristics);
                        mCameras.put(Select.BACK, camera);
                        break;
                    }

                    case (CameraCharacteristics.LENS_FACING_EXTERNAL): {
                        Camera camera = new Camera("ExternalCamera", id, cameraCharacteristics);
                        mCameras.put(Select.EXTERNAL, camera);
                        break;
                    }

                    default: {
                        // TODO: error
                        Log.e("CameraControllerClass", "start() error: lens_facing");
                    }
                }
            }
        }
        catch (CameraAccessException e) {
            // TODO: error
            Log.e("CameraControllerClass", "start() error: CameraAccessException");
        }
    }

    // getBitsPerPixel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    public static Integer getBitsPerPixel() {
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getBitsPerPixel();
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    public static Integer getOutputFormat() {
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getOutputFormat();
    }

    // getOutputSize................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    public static Size getOutputSize() {
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getOutputSize();
    }

    // logCameraCharacteristics.....................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void logCameraCharacteristics() {
        Log.e("CameraController", ".");
        for (Camera camera : mCameras.values()) {
            camera.logCharacteristics();
            Log.e("CameraController", ".");
        }
        Log.e("CameraController", ".");
    }

    // openCamera...................................................................................
    /**
     * TODO: description, comments and logging
     * @param select
     * @param runnable
     * @param handler
     * @return
     */
    public static boolean openCamera(@NonNull Select select,
                                     @Nullable Runnable runnable, @Nullable Handler handler) {
        Log.e("CameraControllerClass", "openCamera()");

        Camera camera = mCameras.get(select);
        if (camera == null) {
            return false;
        }

        mInstance.mNextRunnable = runnable;
        mInstance.mNextHandler  = handler;

        try {
            mInstance.mCameraManager.openCamera(camera.getCameraId(), camera, mHandler);
            return true;
        }
        catch (SecurityException e) {
            // TODO: error
            Log.e("CameraControllerClass", "openCamera() error: SecurityException");
        }
        catch (CameraAccessException e) {
            // TODO: error
            Log.e("CameraControllerClass", "openCamera() error: CameraAccessException");
        }
        return false;
    }

    // post.........................................................................................
    /**
     * TODO: description, comments and logging
     * @param runnable
     */
    public static void post(Runnable runnable) {
        Log.e("CameraControllerClass", "post()");
        mHandler.post(runnable);
    }

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // cameraHasOpened..............................................................................
    /**
     * TODO: description, comments and logging
     * @param camera
     */
    static void cameraHasOpened(@NonNull Camera camera) {
        mInstance.mOpenCamera = camera;
        if (mInstance.mNextRunnable != null) {
            if (mInstance.mNextHandler != null) {
                mInstance.mNextHandler.post(mInstance.mNextRunnable);
                mInstance.mNextHandler = null;
            }
            else {
                mHandler.post(mInstance.mNextRunnable);
            }
            mInstance.mNextRunnable = null;
        }
    }

    // cameraHasClosed..............................................................................
    /**
     * TODO: description, comments and logging
     */
    static void cameraHasClosed() {
        mInstance.mOpenCamera = null;
    }

}
