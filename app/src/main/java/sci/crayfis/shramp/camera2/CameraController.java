package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
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
    private static final Integer PRIORITY = GlobalSettings.CAMERA_CONTROLLER_THREAD_PRIORITY;

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
        //Log.e(Thread.currentThread().getName(), "CameraController CameraController");
        mOpenCamera = null;
        //Log.e("CameraControllerClass", ".");
        //Log.e("CameraControllerClass", "CameraController()");
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
        Log.e(Thread.currentThread().getName(), "CameraController closeCamera");

        if (mInstance.mOpenCamera == null) {
            return;
        }
        mInstance.mOpenCamera.close();
    }

    // createCaptureSession.........................................................................
    /**
     * TODO: description, comments and logging
     * @param surfaceList bla
     * @param stateCallback bla
     * @param handler bla
     */
    public static void createCaptureSession(@NonNull List<Surface> surfaceList,
                                            @NonNull CameraCaptureSession.StateCallback stateCallback,
                                            @NonNull Handler handler) {
        Log.e(Thread.currentThread().getName(), "CameraController createCaptureSession");
        if (mInstance.mOpenCamera != null) {
            CameraDevice cameraDevice = mInstance.mOpenCamera.getCameraDevice();
            assert cameraDevice != null;
            try {
                cameraDevice.createCaptureSession(surfaceList, stateCallback, handler);
            }
            catch (CameraAccessException e) {
                // TODO: error
            }
        }
    }

    // discoverCameras..............................................................................
    /**
     * TODO: description, comments and logging
     * @param cameraManager bla
     */
    public static void discoverCameras(@NonNull CameraManager cameraManager) {

        //Log.e(Thread.currentThread().getName(), "CameraController discoverCameras");
        mInstance.mCameraManager = cameraManager;

        String[] cameraIds;
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
        }
    }

    // getAvailableCaptureRequestKeys...............................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        //Log.e(Thread.currentThread().getName(), "CameraController getAvailableCaptureRequestKeys");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getAvailableCaptureRequestKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public static Integer getBitsPerPixel() {
        //Log.e(Thread.currentThread().getName(), "CameraController getBitsPerPixel");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getBitsPerPixel();
    }

    // getCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static CaptureRequest.Builder getCaptureRequestBuilder() {
        //Log.e(Thread.currentThread().getName(), "CameraController getCaptureRequestBuilder");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getCaptureRequestBuilder();
    }

    // getOpenedCharacteristicsMap..................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public static LinkedHashMap<CameraCharacteristics.Key, Parameter> getOpenedCharacteristicsMap() {
        //Log.e(Thread.currentThread().getName(), "CameraController getOpenedCharacteristicsMap");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getCharacteristicsMap();
    }

    // getOpenedCamera..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public static CameraDevice getOpenedCameraDevice() {
        //Log.e(Thread.currentThread().getName(), "CameraController getOpenedCameraDevice");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getCameraDevice();
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public static Integer getOutputFormat() {
        //Log.e(Thread.currentThread().getName(), "CameraController getOutputFormat");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getOutputFormat();
    }

    // getOutputSize................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public static Size getOutputSize() {
        //Log.e(Thread.currentThread().getName(), "CameraController getOutputSize");
        if (mInstance.mOpenCamera == null) {
            return null;
        }
        return mInstance.mOpenCamera.getOutputSize();
    }

    // openCamera...................................................................................
    /**
     * TODO: description, comments and logging
     * @param select bla
     * @param runnable bla
     * @param handler bla
     * @return bla
     */
    public static boolean openCamera(@NonNull Select select,
                                     @Nullable Runnable runnable, @Nullable Handler handler) {
        Log.e(Thread.currentThread().getName(), "CameraController openCamera");

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
     * @param runnable bla
     */
    public synchronized static void post(Runnable runnable) {
        Log.e(Thread.currentThread().getName(), "CameraController post");
        mHandler.post(runnable);
    }

    // setCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @param builder bla
     */
    public static void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        //Log.e(Thread.currentThread().getName(), "CameraController setCaptureRequestBuilder");
        if (mInstance.mOpenCamera == null) {
            return;
        }
        mInstance.mOpenCamera.setCaptureRequestBuilder(builder);
    }

    // setCaptureRequestMap.........................................................................
    /**
     * TODO: description, comments and logging
     * @param map bla
     */
    public static void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        //Log.e(Thread.currentThread().getName(), "CameraController setCaptureRequestMap");
        if (mInstance.mOpenCamera == null) {
            return;
        }
        mInstance.mOpenCamera.setCaptureRequestMap(map);
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * TODO: description, comments and logging
     * @param template bla
     */
    public static void setCaptureRequestTemplate(@NonNull Integer template) {
        //Log.e(Thread.currentThread().getName(), "CameraController setCaptureRequestTemplate");
        if (mInstance.mOpenCamera == null) {
            return;
        }
        mInstance.mOpenCamera.setCaptureRequestTemplate(template);
    }

    // writeBuilder.................................................................................

    /**
     * TODO: description, comments and logging
     */
    public static void writeFPS() {
        //Log.e(Thread.currentThread().getName(), "CameraController writeBuilder");
        if (mInstance.mOpenCamera != null) {
            mInstance.mOpenCamera.writeFPS();
        }
    }

    // writeCaptureRequest..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void writeCaptureRequest() {
        //Log.e(Thread.currentThread().getName(), "CameraController writeCaptureRequest");
        if (mInstance.mOpenCamera != null) {
            //Log.e("CameraController", ".");
            mInstance.mOpenCamera.writeRequest();
            //Log.e("CameraController", ".");
        }
    }

    // writeCameraCharacteristics...................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void writeCameraCharacteristics() {
        //Log.e(Thread.currentThread().getName(), "CameraController writeCameraCharacteristics");
        //Log.e("CameraController", ".");
        for (Camera camera : mCameras.values()) {
            camera.writeCharacteristics();
            Log.e("CameraController", ".");
        }
        //Log.e("CameraController", ".");
    }

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // cameraHasOpened..............................................................................
    /**
     * TODO: description, comments and logging
     * @param camera bla
     */
    static void cameraHasOpened(@NonNull Camera camera) {
        Log.e(Thread.currentThread().getName(), "CameraController cameraHasOpened");
        mInstance.mOpenCamera = camera;
        RequestMaker.makeDefault();
        //Log.e("CameraController", ".");
        camera.writeRequest();
        //Log.e("CameraController", ".");
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
        Log.e(Thread.currentThread().getName(), "CameraController cameraHasClosed");
        mInstance.mOpenCamera = null;
    }

}
