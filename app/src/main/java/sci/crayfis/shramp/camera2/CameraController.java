/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

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
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.util.HandlerManager;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class CameraController {

    // Public Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Select.......................................................................................
    // TODO: description
    public enum Select {FRONT, BACK, EXTERNAL}

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "CameraControllerThread";

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
            GlobalSettings.CAMERA_CONTROLLER_THREAD_PRIORITY);

    // mCameras.....................................................................................
    // TODO: description
    private static final HashMap<Select, Camera> mCameras = new HashMap<>();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mCameraManager...............................................................................
    // TODO: description
    private static CameraManager mCameraManager;

    // mOpenCamera..................................................................................
    // TODO: description
    private static Camera mOpenCamera;

    // mNextRunnable................................................................................
    // TODO: description
    private static Runnable mNextRunnable;

    // mNextHandler.................................................................................
    // TODO: description
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
        if (mOpenCamera != null) {
            CameraDevice cameraDevice = mOpenCamera.getCameraDevice();
            assert cameraDevice != null;
            try {
                cameraDevice.createCaptureSession(surfaceList, stateCallback, handler);
            }
            catch (CameraAccessException e) {
                // TODO: error
            }
        }
    }

    // closeCamera..................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void closeCamera() {
        Log.e(Thread.currentThread().getName(), "CameraController closeCamera");

        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.close();
    }

    // discoverCameras..............................................................................
    /**
     * TODO: description, comments and logging
     * @param cameraManager bla
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
    @Nullable
    public static List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getAvailableCaptureRequestKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    @Contract(pure = true)
    public static Size getOutputSize() {
        if (mOpenCamera == null) {
            return null;
        }
        return mOpenCamera.getOutputSize();
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

        mNextRunnable = runnable;
        mNextHandler  = handler;

        try {
            mCameraManager.openCamera(camera.getCameraId(), camera, mHandler);
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

    // setCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @param builder bla
     */
    public static void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.setCaptureRequestBuilder(builder);
    }

    // setCaptureRequestMap.........................................................................
    /**
     * TODO: description, comments and logging
     * @param map bla
     */
    public static void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.setCaptureRequestMap(map);
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * TODO: description, comments and logging
     * @param template bla
     */
    public static void setCaptureRequestTemplate(@NonNull Integer template) {
        if (mOpenCamera == null) {
            return;
        }
        mOpenCamera.setCaptureRequestTemplate(template);
    }

    // writeFPS.....................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void writeFPS() {
        if (mOpenCamera != null) {
            mOpenCamera.writeFPS();
        }
    }

    // writeCaptureRequest..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void writeCaptureRequest() {
        if (mOpenCamera != null) {
            mOpenCamera.writeRequest();
        }
    }

    // writeCameraCharacteristics...................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void writeCameraCharacteristics() {
        for (Camera camera : mCameras.values()) {
            camera.writeCharacteristics();
            Log.e("CameraController", ".");
        }
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // cameraHasOpened..............................................................................
    /**
     * TODO: description, comments and logging
     * @param camera bla
     */
    static void cameraHasOpened(@NonNull Camera camera) {
        Log.e(Thread.currentThread().getName(), "CameraController cameraHasOpened");

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
     * TODO: description, comments and logging
     */
    static void cameraHasClosed() {
        Log.e(Thread.currentThread().getName(), "CameraController cameraHasClosed");
        mOpenCamera = null;
    }

}