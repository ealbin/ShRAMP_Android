/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 15 April 2019
 */

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

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.util.HandlerManager;

/**
 * Public access to cameras and camera actions
 */
@TargetApi(21)
abstract public class CameraController {

    // Public Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Select.......................................................................................
    // Camera selection, FRONT is the same side as the screen
    public enum Select {FRONT, BACK, EXTERNAL}

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: the camera controller probably does not need its own thread -- remove in the future
    private static final String THREAD_NAME = "CameraControllerThread";

    // mHandler.....................................................................................
    // Reference to the Handler for the camera controller thread
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                 GlobalSettings.CAMERA_CONTROLLER_THREAD_PRIORITY);

    // mCameras.....................................................................................
    // Collection of cameras on this device
    private static final HashMap<Select, Camera> mCameras = new HashMap<>();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mCameraManager...............................................................................
    // Reference to system camera manager
    private static CameraManager mCameraManager;

    // mOpenCamera..................................................................................
    // Reference to the currently opened camera
    private static Camera mOpenCamera;

    // mNextRunnable................................................................................
    // Action to perform following the camera's asynchronous opening
    private static Runnable mNextRunnable;

    // mNextHandler.................................................................................
    // Thread to continue execution on via mNextRunnable
    private static Handler mNextHandler;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CameraController.............................................................................
    private CameraController() {
        mOpenCamera = null;
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // discoverCameras..............................................................................
    /**
     * Discover all cameras on this device
     * @param cameraManager Reference to the system camera manager
     */
    public static void discoverCameras(@NonNull CameraManager cameraManager) {
        mCameraManager = cameraManager;

        String[] cameraIds;
        try {
            cameraIds = mCameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics cameraCharacteristics
                        = mCameraManager.getCameraCharacteristics(id);
                Integer lens_facing  = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (lens_facing == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens facing cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                        Log.e(Thread.currentThread().getName(), "Unknown camera lens facing");
                        MasterController.quitSafely();
                        return;
                    }
                }
            }
        }
        catch (CameraAccessException e) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Camera is not accessible");
            MasterController.quitSafely();
        }
    }

    // openCamera...................................................................................
    /**
     * Open camera for capture.  Camera opens asynchronously, therefore to wait for the camera to
     * open before continuing execution, pass in a runnable and its thread to run on.
     * @param select Which camera (FRONT, BACK, or EXTERNAL)
     * @param runnable (Optional) Execution continues with this Runnable
     * @param handler (Optional) Runnable is executed on this thread (camera controller thread default)
     * @return True if camera is opening, false if request is unsuccessful
     */
    public static boolean openCamera(@NonNull Select select,
                                     @Nullable Runnable runnable, @Nullable Handler handler) {

        Camera camera = mCameras.get(select);
        if (camera == null) {
            return false;
        }

        mNextRunnable = runnable;
        mNextHandler  = handler;

        try {
            mCameraManager.openCamera(camera.getCameraId(), camera, mHandler);
            return true;
        }
        catch (SecurityException e) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Camera permissions have not been granted");
            MasterController.quitSafely();
            return false;
        }
        catch (CameraAccessException e) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Camera cannot be accessed");
            MasterController.quitSafely();
            return false;
        }
    }

    // createCaptureSession.........................................................................
    /**
     * Initialize capture session on currently opened camera, no action if no camera is open.
     * Upon successful setup, stateCallback.on(TODO: I forgot) is called
     * TODO: return boolean for success/fail, maybe default configuration if parameters are null..
     * @param surfaceList Output surface list
     * @param stateCallback Callback for capture session state
     * @param handler Capture session state callback thread
     */
    public static void createCaptureSession(@NonNull List<Surface> surfaceList,
                                            @NonNull CameraCaptureSession.StateCallback stateCallback,
                                            @NonNull Handler handler) {
        if (mOpenCamera != null) {
            CameraDevice cameraDevice = mOpenCamera.getCameraDevice();
            if (cameraDevice == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Camera in unknown state");
                MasterController.quitSafely();
                return;
            }
            try {
                // TODO: execution continues asynchronously in (forgot what)
                cameraDevice.createCaptureSession(surfaceList, stateCallback, handler);
            }
            catch (CameraAccessException e) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Camera cannot be accessed");
                MasterController.quitSafely();
            }
        }
    }

    // closeCamera..................................................................................
    /**
     * Close any opened cameras, execution continues asynchronously in cameraHasClosed()
     */
    public static void closeCamera() {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getAvailableCaptureRequestKeys...............................................................
    /**
     * @return Open camera's available capture request keys, or null if no camera is open
     */
    @Nullable
    public static List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getAvailableCaptureRequestKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * @return Open camera's output format bits per pixel, or null if no camera is open
     */
    @Nullable
    @Contract(pure = true)
    public static Integer getBitsPerPixel() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getBitsPerPixel();
    }

    // getCaptureRequestBuilder.....................................................................
    /**
     * @return Open camera's current CaptureRequest.Builder, or null if no camera is open
     */
    @Nullable
    @Contract(pure = true)
    public static CaptureRequest.Builder getCaptureRequestBuilder() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getCaptureRequestBuilder();
    }

    // getOpenedCharacteristicsMap..................................................................
    /**
     * @return Open camera's characteristics map, or null if no camera is open
     */
    @Nullable
    @Contract(pure = true)
    public static LinkedHashMap<CameraCharacteristics.Key, Parameter> getOpenedCharacteristicsMap() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getCharacteristicsMap();
    }

    // getOpenedCamera..............................................................................
    /**
     * @return Reference to camera device if open, null if not open
     */
    @Nullable
    @Contract(pure = true)
    public static CameraDevice getOpenedCameraDevice() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getCameraDevice();
    }

    // getOutputFormat..............................................................................
    /**
     * @return Open camera's output format (ImageFormat.YUV_420_888 or RAW_SENSOR), or null if not open
     */
    @Nullable
    @Contract(pure = true)
    public static Integer getOutputFormat() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getOutputFormat();
    }

    // getOutputSize................................................................................
    /**
     * @return Open camera's output size (width, height), or null if no camera open
     */
    @Nullable
    @Contract(pure = true)
    public static Size getOutputSize() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getOutputSize();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // setCaptureRequestBuilder.....................................................................
    /**
     * @param builder Set open camera CaptureRequest.Builder, no action if no camera open
     */
    public static void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.setCaptureRequestBuilder(builder);
    }

    // setCaptureRequestMap.........................................................................
    /**
     * @param map Set open camera capture request parameter map, no action if no camera open
     */
    public static void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.setCaptureRequestMap(map);
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * @param template Set open camera capture request template, no action if no camera open
     */
    public static void setCaptureRequestTemplate(@NonNull Integer template) {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.setCaptureRequestTemplate(template);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // writeFPS.....................................................................................
    /**
     * Display open camera's configured FPS, no action if no camera open
     */
    public static void writeFPS() {
        if (mOpenCamera != null) {
            mOpenCamera.writeFPS();
        }
    }

    // writeCaptureRequest..........................................................................
    /**
     * Display open camera's full capture request, no action if no camera open
     */
    public static void writeCaptureRequest() {
        if (mOpenCamera != null) {
            mOpenCamera.writeRequest();
        }
    }

    // writeCameraCharacteristics...................................................................
    /**
     * Display all camera's full characteristics and abilities, camera does not need to be open
     */
    public static void writeCameraCharacteristics() {
        for (Camera camera : mCameras.values()) {
            camera.writeCharacteristics();
            Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        }
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // cameraHasOpened..............................................................................
    /**
     * Called by Camera asynchronously once it has opened, execution continues with mNextRunnable
     * if supplied
     * @param camera Reference to opened Camera object
     */
    static void cameraHasOpened(@NonNull Camera camera) {
        Log.e(Thread.currentThread().getName(), "Camera is open");

        mOpenCamera = camera;
        RequestMaker.makeDefault();
        camera.writeRequest();

        if (mNextRunnable != null) {
            if (mNextHandler != null) {
                mNextHandler.post(mNextRunnable);
                mNextHandler = null;
            }
            else {
                mHandler.post(mNextRunnable);
            }
            mNextRunnable = null;
        }
    }

    // cameraHasClosed..............................................................................
    /**
     * Called asynchronously by previously open camera upon closing
     */
    static void cameraHasClosed() {
        Log.e(Thread.currentThread().getName(), "Camera has closed");
        mOpenCamera = null;
    }

}