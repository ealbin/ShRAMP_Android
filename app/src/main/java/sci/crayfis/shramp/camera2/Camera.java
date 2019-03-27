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
import sci.crayfis.shramp.camera2.characteristics.CharacteristicsReader;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceController;
import sci.crayfis.shramp.util.ArrayToList;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.SizeSortedSet;

/**
 * TODO: description, comments and logging
 */
// TODO: figure out who is giving the unchecked warning
@SuppressWarnings("unchecked")
@TargetApi(21)
final class Camera extends CameraDevice.StateCallback{

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBitsPerPixel................................................................................
    // TODO: description
    private Integer mBitsPerPixel;

    // mCameraCharacteristics.......................................................................
    // TODO: description
    private CameraCharacteristics mCameraCharacteristics;

    // mCameraDevice................................................................................
    // TODO: description
    private CameraDevice mCameraDevice;

    // mCameraId....................................................................................
    // TODO: description
    private String mCameraId;

    // mCaptureRequestBuilder.......................................................................
    // TODO: description
    private CaptureRequest.Builder mCaptureRequestBuilder;

    // mCaptureRequestMap...........................................................................
    // TODO: description
    private LinkedHashMap<CaptureRequest.Key, Parameter> mCaptureRequestMap;

    // mCaptureRequestTemplate......................................................................
    // TODO: description
    private Integer mCaptureRequestTemplate;

    // mCharacteristicsMap..........................................................................
    // TODO: description
    private LinkedHashMap<CameraCharacteristics.Key, Parameter> mCharacteristicsMap;

    // mName........................................................................................
    // TODO: description
    private String mName;

    // mOutputFormat................................................................................
    // TODO: description
    private Integer mOutputFormat;

    // mOutputSize..................................................................................
    // TODO: description
    private Size mOutputSize;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Camera.......................................................................................
    /**
     * TODO: description, comments and logging
     * @param name bla
     * @param cameraId bla
     * @param cameraCharacteristics bla
     */
    Camera(@NonNull String name, @NonNull String cameraId,
           @NonNull CameraCharacteristics cameraCharacteristics) {
        this();

        Log.e(Thread.currentThread().getName(), "Camera Camera: " + name + ", ID: " + cameraId);

        mName                  = name;
        mCameraId              = cameraId;
        mCameraCharacteristics = cameraCharacteristics;
        mCharacteristicsMap    = CharacteristicsReader.read(mCameraCharacteristics);

        establishOutputFormatting();
    }

    // Camera.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    private Camera() { super(); }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // close........................................................................................
    /**
     * TODO: description, comments and logging
     */
    void close() {
        Log.e(Thread.currentThread().getName(), "Camera close: " + mName + ", ID: " + mCameraId);
        Log.e("CameraClass", "Camera: " + mName + " has been asked to close");
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    // getAvailableCaptureRequestKeys...............................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        return mCameraCharacteristics.getAvailableCaptureRequestKeys();
    }

    // getAvailableCharacteristicsKeys..............................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    List<CameraCharacteristics.Key<?>> getAvailableCharacteristicsKeys() {
        return mCameraCharacteristics.getKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    Integer getBitsPerPixel() {
        return mBitsPerPixel;
    }

    // getCameraDevice..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    // getCameraId..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    String getCameraId() {
        return mCameraId;
    }

    // getCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    CaptureRequest.Builder getCaptureRequestBuilder() {
        return mCaptureRequestBuilder;
    }

    // getCharacteristicsMap........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    LinkedHashMap<CameraCharacteristics.Key, Parameter> getCharacteristicsMap() {
        return mCharacteristicsMap;
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    Integer getOutputFormat() {
        return mOutputFormat;
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    Size getOutputSize() {
        return mOutputSize;
    }

    // setCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @param builder bla
     */
    void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        mCaptureRequestBuilder = builder;
    }

    // setCaptureRequestMap.........................................................................
    /**
     * TODO: description, comments and logging
     * @param map bla
     */
    void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        mCaptureRequestMap = map;
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * TODO: description, comments and logging
     * @param template bla
     */
    void setCaptureRequestTemplate(@NonNull Integer template) {
        mCaptureRequestTemplate = template;
    }

    // writeFPS.....................................................................................
    /**
     * TODO: description, comments and logging
     */
    void writeFPS() {
        Log.e(Thread.currentThread().getName(), "Camera writeBuilder: " + mName);

        Long frameDuration = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_FRAME_DURATION);
        Long exposureTime  = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
        Range<Integer> fpsRange = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);

        if (frameDuration != null) {
            double fps = Math.round(1e9 / (double) frameDuration);
            Log.e(mName + ", ID: " + mCameraId, "Frame Duration: " + NumToString.decimal(fps) + " [frames per second]");
        }

        if (exposureTime != null && frameDuration != null) {
            double duty = Math.round(100. * exposureTime / (double) frameDuration);
            Log.e(mName + ", ID: " + mCameraId, "Exposure Duty: " + NumToString.decimal(duty) + " [%]");
        }

        if (fpsRange != null) {
            Log.e(mName + ", ID: " + mCameraId, "FPS Range: " + fpsRange.toString() + " [frames per second]");
        }
    }

    // writeCharacteristics.........................................................................
    /**
     * TODO: description, comments and logging
     */
    void writeCharacteristics() {
        Log.e(Thread.currentThread().getName(), "Camera writeCharacteristics: " + mName);
        String label = mName + ", ID: " + mCameraId;
        CharacteristicsReader.write(label, mCharacteristicsMap, getAvailableCharacteristicsKeys());
    }

    // writeRequest.................................................................................
    /**
     * TODO: description, comments and logging
     */
    void writeRequest() {
        Log.e(Thread.currentThread().getName(), "Camera writeRequest: " + mName);
        String label = mName + ", ID: " + mCameraId;
        RequestMaker.write(label, mCaptureRequestMap, getAvailableCaptureRequestKeys());
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // establishOutputFormatting....................................................................
    /**
     * TODO: description, comments and logging
     */
    private void establishOutputFormatting() {
        Parameter parameter;

        parameter = mCharacteristicsMap.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        assert parameter != null;
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) parameter.getValue();
        assert streamConfigurationMap != null;

        parameter = mCharacteristicsMap.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        assert parameter != null;
        Integer[] capabilities = (Integer[]) parameter.getValue();
        assert capabilities != null;
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
            assert classOutputSizes != null;
            for (Size s : classOutputSizes) {
                if (!outputSizes.contains(s)) {
                    outputSizes.remove(s);
                }
            }
        }

        mOutputSize = outputSizes.last();
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onClosed.....................................................................................
    /**
     * TODO: description, comments and logging
     * Actions when camera is closed.
     * No actions yet.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onClosed(@NonNull CameraDevice camera) {
        Log.e(Thread.currentThread().getName(), "Camera onClosed: " + mName);
        CameraController.cameraHasClosed();
    }

    // onDisconnected...............................................................................
    /**
     * TODO: description, comments and logging
     * Actions when camera is disconnected.
     * Close camera.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
        Log.e(Thread.currentThread().getName(), "Camera onDisconnected: " + mName);
    }

    // onError......................................................................................
    /**
     * TODO: description, comments and logging
     * Actions when camera has erred.
     * Determine error cause and close camera.
     * @param camera CameraDevice that has erred
     */
    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
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

        Log.e(Thread.currentThread().getName(), "Camera onError: " + mName + " err: " + err);
    }

    // onOpened.....................................................................................
    /**
     * TODO: description, comments and logging
     * Actions when camera is opened.
     * Configure camera for capture session.
     * @param camera CameraDevice that has been opened
     */
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        Log.e(Thread.currentThread().getName(), "Camera onOpened: " + mName);
        mCameraDevice = camera;

        CameraController.cameraHasOpened(this);
    }

}