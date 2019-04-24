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
 * @updated: 24 April 2019
 */

package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import org.jetbrains.annotations.Contract;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.characteristics.CharacteristicsReader;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceController;
import sci.crayfis.shramp.util.ArrayToList;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.SizeSortedSet;

/**
 * Encapsulation of CameraDevice, its characteristics, abilities and configuration for capture
 */
// TODO: figure out who is giving the unchecked warning
@SuppressWarnings("unchecked")
@TargetApi(21)
final class Camera extends CameraDevice.StateCallback{

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBitsPerPixel................................................................................
    // Output format bits per pixel
    private Integer mBitsPerPixel;

    // mCameraCharacteristics.......................................................................
    // Encapsulation of camera's features
    private CameraCharacteristics mCameraCharacteristics;

    // mCameraDevice................................................................................
    // Reference to the camera device hardware
    private CameraDevice mCameraDevice;

    // mCameraId....................................................................................
    // System-assigned camera ID
    private String mCameraId;

    // mCaptureRequestBuilder.......................................................................
    // Current capture request builder
    private CaptureRequest.Builder mCaptureRequestBuilder;

    // mCaptureRequestMap...........................................................................
    // Current full configuration of camera for capture
    private LinkedHashMap<CaptureRequest.Key, Parameter> mCaptureRequestMap;

    // mCaptureRequestTemplate......................................................................
    // Camera capture template
    private Integer mCaptureRequestTemplate;

    // mCharacteristicsMap..........................................................................
    // Encapsulation of all camera abilities and features
    private LinkedHashMap<CameraCharacteristics.Key, Parameter> mCharacteristicsMap;

    // mName........................................................................................
    // Human-friendly camera name
    private String mName;

    // mOutputFormat................................................................................
    // Output format (ImageFormat.YUV_420_888 or RAW_SENSOR)
    private Integer mOutputFormat;

    // mOutputSize..................................................................................
    // Output size (width and height in pixels)
    private Size mOutputSize;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Camera.......................................................................................
    /**
     * Public access disabled
     */
    private Camera() { super(); }

    // Camera.......................................................................................
    /**
     * Create a new Camera
     * @param name Human-friendly name for camera
     * @param cameraId System-assigned camera ID
     * @param cameraCharacteristics Encapsulation of camera features
     */
    Camera(@NonNull String name, @NonNull String cameraId,
           @NonNull CameraCharacteristics cameraCharacteristics) {
        this();

        Log.e(Thread.currentThread().getName(), " \n\n\t\t\tNew camera created: " + name + " with ID: " + cameraId + "\n ");

        mName                  = name;
        mCameraId              = cameraId;
        mCameraCharacteristics = cameraCharacteristics;
        mCharacteristicsMap    = CharacteristicsReader.read(mCameraCharacteristics);

        establishOutputFormatting();
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // establishOutputFormatting....................................................................
    /**
     * Figure out optimal output format for capture
     */
    private void establishOutputFormatting() {
        Parameter parameter;

        parameter = mCharacteristicsMap.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (parameter == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Stream configuration map cannot be null");
            MasterController.quitSafely();
            return;
        }

        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) parameter.getValue();
        if (streamConfigurationMap == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Stream configuration map cannot be null");
            MasterController.quitSafely();
            return;
        }

        parameter = mCharacteristicsMap.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        if (parameter == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Available capabilities cannot be null");
            MasterController.quitSafely();
            return;
        }

        Integer[] capabilities = (Integer[]) parameter.getValue();
        if (capabilities == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Capabilities cannot be null");
        }
        List<Integer> abilities = ArrayToList.convert(capabilities);

        if (!GlobalSettings.DISABLE_RAW_OUTPUT && abilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
            mOutputFormat = ImageFormat.RAW_SENSOR;
        }
        else {
            mOutputFormat = ImageFormat.YUV_420_888;
        }

        mBitsPerPixel = ImageFormat.getBitsPerPixel(mOutputFormat);

        // Find the largest output size supported by all output surfaces
        SizeSortedSet outputSizes = new SizeSortedSet();

        Size[] streamOutputSizes = streamConfigurationMap.getOutputSizes(mOutputFormat);
        Collections.addAll(outputSizes, streamOutputSizes);

        List<Class> outputClasses = SurfaceController.getOutputSurfaceClasses();
        for (Class klass : outputClasses) {
            Size[] classOutputSizes = streamConfigurationMap.getOutputSizes(klass);
            if (classOutputSizes == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Class output size cannot be null");
                MasterController.quitSafely();
                return;
            }
            for (Size s : classOutputSizes) {
                if (!outputSizes.contains(s)) {
                    outputSizes.remove(s);
                }
            }
        }

        mOutputSize = outputSizes.last();
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // close........................................................................................
    /**
     * Close this camera
     */
    void close() {
        Log.e(Thread.currentThread().getName(), "Closing camera: " + mName + " with ID: " + mCameraId);
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getAvailableCaptureRequestKeys...............................................................
    /**
     * @return Current capture request keys
     */
    @NonNull
    List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        return mCameraCharacteristics.getAvailableCaptureRequestKeys();
    }

    // getAvailableCharacteristicsKeys..............................................................
    /**
     * @return All camera characteristics and abilities
     */
    @NonNull
    List<CameraCharacteristics.Key<?>> getAvailableCharacteristicsKeys() {
        return mCameraCharacteristics.getKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * @return Output format bits per pixel
     */
    @Contract(pure = true)
    @Nullable
    Integer getBitsPerPixel() {
        return mBitsPerPixel;
    }

    // getCameraDevice..............................................................................
    /**
     * @return Reference to CameraDevice contained by this object
     */
    @Contract(pure = true)
    @Nullable
    CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    // getCameraId..................................................................................
    /**
     * @return Get system-assigned camera ID
     */
    @Contract(pure = true)
    @NonNull
    String getCameraId() {
        return mCameraId;
    }

    // getCaptureRequestBuilder.....................................................................
    /**
     * @return Current capture request builder
     */
    @Contract(pure = true)
    @Nullable
    CaptureRequest.Builder getCaptureRequestBuilder() {
        return mCaptureRequestBuilder;
    }

    // getCharacteristicsMap........................................................................
    /**
     * @return Encapsulation of camera features
     */
    @Contract(pure = true)
    @NonNull
    LinkedHashMap<CameraCharacteristics.Key, Parameter> getCharacteristicsMap() {
        return mCharacteristicsMap;
    }

    // getOutputFormat..............................................................................
    /**
     * @return Camera output format (ImageFormat.YUV_420_888 or RAW_SENSOR)
     */
    @Contract(pure = true)
    @Nullable
    Integer getOutputFormat() {
        return mOutputFormat;
    }

    // getOutputSize................................................................................
    /**
     * @return Output size (width and height in pixels)
     */
    @Contract(pure = true)
    @NonNull
    Size getOutputSize() {
        return mOutputSize;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // setCaptureRequestBuilder.....................................................................
    /**
     * @param builder Set camera to use CaptureRequest.Builder for capture
     */
    void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        mCaptureRequestBuilder = builder;
    }

    // setCaptureRequestMap.........................................................................
    /**
     * @param map Set full camera request mapping
     */
    void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        mCaptureRequestMap = map;
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * @param template Set camera request template for capture
     */
    void setCaptureRequestTemplate(@NonNull Integer template) {
        mCaptureRequestTemplate = template;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // writeFPS.....................................................................................
    /**
     * Display current Camera FPS settings
     */
    void writeFPS() {

        Log.e(Thread.currentThread().getName(), " \n\n" + mName + ", ID: " + mCameraId);

        Integer mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_MODE);
        if (mode == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
            MasterController.quitSafely();
            return;
        }

        if (mOutputFormat == ImageFormat.YUV_420_888) {
            Log.e(Thread.currentThread().getName(), ">>>>>>>>  Output format is YUV_420_888");
        }
        else { // mOutputFormat == ImageFormat.RAW_SENSOR
            Log.e(Thread.currentThread().getName(), ">>>>>>>>  Output format is RAW_SENSOR");
        }

        if (mode == CameraMetadata.CONTROL_AE_MODE_ON) {
            Range<Integer> fpsRange = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);

            if (fpsRange == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "FPS range cannot be null");
                MasterController.quitSafely();
                return;
            }

            Log.e(Thread.currentThread().getName(), ">>>>>>>>  FPS Range: " + fpsRange.toString() + " [frames per second]");
        }
        else {
            Long frameDuration = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_FRAME_DURATION);
            Long exposureTime  = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME);

            if (frameDuration == null || exposureTime == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Sensor exposure time and frame duration cannot be null");
                MasterController.quitSafely();
                return;
            }

            double fps = Math.round(1e9 / (double) frameDuration);
            Log.e(Thread.currentThread().getName(), ">>>>>>>>  Frame Duration: " + NumToString.decimal(fps) + " [frames per second]");

            double duty = Math.round(100. * exposureTime / (double) frameDuration);
            Log.e(Thread.currentThread().getName(), ">>>>>>>>  Exposure Duty: " + NumToString.decimal(duty) + " [%]");
        }
    }

    // writeCharacteristics.........................................................................
    /**
     * Display full camera features
     */
    void writeCharacteristics() {
        String label = mName + ", ID: " + mCameraId;
        CharacteristicsReader.write(label, mCharacteristicsMap, getAvailableCharacteristicsKeys());
    }

    // writeRequest.................................................................................
    /**
     * Display current capture request
     */
    void writeRequest() {
        String label = mName + ", ID: " + mCameraId;
        RequestMaker.write(label, mCaptureRequestMap, getAvailableCaptureRequestKeys());
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onOpened.....................................................................................
    /**
     * Called by the system when camera comes online, execution continues in CameraController.cameraHasOpened()
     * @param camera CameraDevice that has been opened
     */
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        Log.e(Thread.currentThread().getName(), " \n\n\t\tCamera: " + mName + " has opened\n\n");
        mCameraDevice = camera;

        CameraController.cameraHasOpened(this);
    }

    // onClosed.....................................................................................
    /**
     * Called by the system when the camera is closing.
     * Execution continues in CameraController.cameraHasClosed()
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onClosed(@NonNull CameraDevice camera) {
        Log.e(Thread.currentThread().getName(), "Camera: " + mName + " has closed");
        CameraController.cameraHasClosed();
    }

    // onDisconnected...............................................................................
    /**
     * Called by the system when the camera has been disconnected
     * @param camera CameraDevice that has been disconnected
     */
    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
        // TODO: error
        Log.e(Thread.currentThread().getName(), "Camera: " + mName + " has been disconnected");
        MasterController.quitSafely();
    }

    // onError......................................................................................
    /**
     * Called by the system when an error occurs with the camera
     * @param camera CameraDevice that has erred
     */
    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
        // TODO: figure out why the compiler says there are missing options for the switch-case
        String err;
        switch (error) {
            case (CameraDevice.StateCallback.ERROR_CAMERA_DEVICE): {
                err = "ERROR_CAMERA_DEVICE";
                break;
            }
            case (CameraDevice.StateCallback.ERROR_CAMERA_DISABLED): {
                err = "ERROR_CAMERA_DISABLED";
                break;
            }
            case (CameraDevice.StateCallback.ERROR_CAMERA_IN_USE): {
                err = "ERROR_CAMERA_IN_USE";
                break;
            }
            case (CameraDevice.StateCallback.ERROR_CAMERA_SERVICE): {
                err = "ERROR_CAMERA_SERVICE";
                break;
            }
            case (CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE): {
                err = "ERROR_MAX_CAMERAS_IN_USE";
                break;
            }
            default: {
                err = "UNKNOWN_ERROR";
            }
        }

        // TODO: error
        Log.e(Thread.currentThread().getName(), "Camera error: " + mName + " err: " + err);
        MasterController.quitSafely();
    }

}