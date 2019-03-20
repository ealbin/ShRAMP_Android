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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.camera2.characteristics.CharacteristicsReader;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.ArrayToList;
import sci.crayfis.shramp.util.SizeSortedSet;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final class Camera extends CameraDevice.StateCallback{

    //**********************************************************************************************
    // Static Class Fields
    //--------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // DISABLE_RAW_OUTPUT...........................................................................
    // TODO: description
    private static final Boolean DISABLE_RAW_OUTPUT = GlobalSettings.DISABLE_RAW_OUTPUT;

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
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

    //**********************************************************************************************
    // Constructors
    //-------------

    // Package-private
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

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Camera.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    private Camera() { super(); }

    //**********************************************************************************************
    // Class Methods
    //--------------

    // Package-private
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
        //Log.e(Thread.currentThread().getName(), "Camera getAvailableCaptureRequestKeys: " + mName);
        return mCameraCharacteristics.getAvailableCaptureRequestKeys();
    }

    // getAvailableCharacteristicsKeys..............................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    List<CameraCharacteristics.Key<?>> getAvailableCharacteristicsKeys() {
        //Log.e(Thread.currentThread().getName(), "Camera getAvailableCharacteristicsKeys: " + mName);
        return mCameraCharacteristics.getKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    Integer getBitsPerPixel() {
        //Log.e(Thread.currentThread().getName(), "Camera getBitsPerPixel: " + mName);
        return mBitsPerPixel;
    }

    // getCameraDevice..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    CameraDevice getCameraDevice() {
        //Log.e(Thread.currentThread().getName(), "Camera getCameraDevice: " + mName);
        return mCameraDevice;
    }

    // getCameraId..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    String getCameraId() {
        //Log.e(Thread.currentThread().getName(), "Camera getCameraId: " + mName);
        return mCameraId;
    }

    // getCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    CaptureRequest.Builder getCaptureRequestBuilder() {
        //Log.e(Thread.currentThread().getName(), "Camera getCaptureRequestBuilder: " + mName);
        return mCaptureRequestBuilder;
    }

    // getCharacteristicsMap........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    LinkedHashMap<CameraCharacteristics.Key, Parameter> getCharacteristicsMap() {
        //Log.e(Thread.currentThread().getName(), "Camera getCharacteristicsMap: " + mName);
        return mCharacteristicsMap;
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    Integer getOutputFormat() {
        //Log.e(Thread.currentThread().getName(), "Camera getOutputFormat: " + mName);
        return mOutputFormat;
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    Size getOutputSize() {
        //Log.e(Thread.currentThread().getName(), "Camera getOutputSize: " + mName);
        return mOutputSize;
    }

    // setCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @param builder bla
     */
    void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        //Log.e(Thread.currentThread().getName(), "Camera setCaptureRequestBuilder: " + mName);
        mCaptureRequestBuilder = builder;
    }

    // setCaptureRequestMap.........................................................................
    /**
     * TODO: description, comments and logging
     * @param map bla
     */
    void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        //Log.e(Thread.currentThread().getName(), "Camera setCaptureRequestMap: " + mName);
        mCaptureRequestMap = map;
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * TODO: description, comments and logging
     * @param template bla
     */
    void setCaptureRequestTemplate(@NonNull Integer template) {
        //Log.e(Thread.currentThread().getName(), "Camera setCaptureRequestTemplate: " + mName);
        mCaptureRequestTemplate = template;
    }

    // writeBuilder.................................................................................

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
            Log.e(mName + ", ID: " + mCameraId, "Frame Duration: " + Double.toString(fps) + " [frames per second]");
        }

        if (exposureTime != null && frameDuration != null) {
            double duty = Math.round(100. * exposureTime / (double) frameDuration);
            Log.e(mName + ", ID: " + mCameraId, "Exposure Duty: " + Double.toString(duty) + " [%]");
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

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // establishOutputFormatting....................................................................
    /**
     * TODO: description, comments and logging
     */
    private void establishOutputFormatting() {

        //Log.e(Thread.currentThread().getName(), "Camera establishOutputFormatting: " + mName);

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

        if (!DISABLE_RAW_OUTPUT && abilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
            mOutputFormat = ImageFormat.RAW_SENSOR;
        }
        else {
            mOutputFormat = ImageFormat.YUV_420_888;
        }

        mBitsPerPixel = ImageFormat.getBitsPerPixel(mOutputFormat);

        // Find the largest output size supported by all output surfaces
        SizeSortedSet outputSizes = new SizeSortedSet();

        Size[] streamOutputSizes = (Size[]) streamConfigurationMap.getOutputSizes(mOutputFormat);
        Collections.addAll(outputSizes, streamOutputSizes);

        List<Class> outputClasses = SurfaceManager.getOutputSurfaceClasses();
        for (Class klass : outputClasses) {
            Size[] classOutputSizes = (Size[]) streamConfigurationMap.getOutputSizes(klass);
            assert classOutputSizes != null;
            for (Size s : classOutputSizes) {
                if (!outputSizes.contains(s)) {
                    outputSizes.remove(s);
                }
            }
        }

        mOutputSize = outputSizes.last();
    }

    //**********************************************************************************************
    // Overriding Methods
    //-------------------

    // Public
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
        String err = "";
        switch (error) {
            case (ERROR_CAMERA_DEVICE): {
                err = "ERROR_CAMERA_DEVICE";
                break;
            }
            case (ERROR_CAMERA_DISABLED): {
                err = "ERROR_CAMERA_DISABLED";
                break;
            }
            case (ERROR_CAMERA_IN_USE): {
                err = "ERROR_CAMERA_IN_USE";
                break;
            }
            case (ERROR_CAMERA_SERVICE): {
                err = "ERROR_CAMERA_SERVICE";
                break;
            }
            case (ERROR_MAX_CAMERAS_IN_USE): {
                err = "ERROR_MAX_CAMERAS_IN_USE";
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
