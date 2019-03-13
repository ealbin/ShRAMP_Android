package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.wifi.aware.Characteristics;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

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
     * @param name
     * @param cameraId
     * @param cameraCharacteristics
     */
    Camera(@NonNull String name, @NonNull String cameraId,
           @NonNull CameraCharacteristics cameraCharacteristics) {
        this();

        Log.e("CameraClass", ".");
        Log.e("CameraClass", "Camera object: " + cameraId + " has been created");

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
        Log.e("CameraClass", "Camera: " + mName + " has been asked to close");
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    // getAvailableCaptureRequestKeys...............................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        return mCameraCharacteristics.getAvailableCaptureRequestKeys();
    }

    // getAvailableCharacteristicsKeys..............................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    List<CameraCharacteristics.Key<?>> getAvailableCharacteristicsKeys() {
        return mCameraCharacteristics.getKeys();
    }

    // getBitsPerPixel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    Integer getBitsPerPixel() { return mBitsPerPixel; }

    // getCameraDevice..............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    // getCameraId..................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    String getCameraId() {
        Log.e("CameraClass", "Camera: " + mName + " is sharing its ID: " + mCameraId);
        return mCameraId;
    }

    // getCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    CaptureRequest.Builder getCaptureRequestBuilder() {
        return mCaptureRequestBuilder;
    }

    // getCharacteristicsMap........................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    LinkedHashMap<CameraCharacteristics.Key, Parameter> getCharacteristicsMap() {
        return mCharacteristicsMap;
    }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    Integer getOutputFormat() { return mOutputFormat; }

    // getOutputFormat..............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    Size getOutputSize() { return mOutputSize; }

    // setCaptureRequestBuilder.....................................................................
    /**
     * TODO: description, comments and logging
     * @param builder
     */
    void setCaptureRequestBuilder(@NonNull CaptureRequest.Builder builder) {
        mCaptureRequestBuilder = builder;
    }

    // setCaptureRequestMap.........................................................................
    /**
     * TODO: description, comments and logging
     * @param map
     */
    void setCaptureRequestMap(@NonNull LinkedHashMap<CaptureRequest.Key, Parameter>  map) {
        mCaptureRequestMap = map;
    }

    // setCaptureRequestTemplate....................................................................
    /**
     * TODO: description, comments and logging
     * @param template
     */
    void setCaptureRequestTemplate(@NonNull Integer template) {
        mCaptureRequestTemplate = template;
    }

    // writeCharacteristics.........................................................................
    /**
     * TODO: description, comments and logging
     */
    void writeCharacteristics() {
        String label = mName + ", ID: " + mCameraId;
        CharacteristicsReader.write(label, mCharacteristicsMap, getAvailableCharacteristicsKeys());
    }

    // writeRequest.................................................................................
    /**
     * TODO: description, comments and logging
     */
    void writeRequest() {
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

        if (abilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
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
        CameraController.cameraHasClosed();
        Log.e("CameraClass", "Camera: " + mName + " is closed");
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
        Log.e("CameraClass", "Camera: " + mName + " is disconnected");
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

        Log.e("CameraClass", "Camera: " + mName + " has an error: " + err);
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
        mCameraDevice = camera;

        CameraController.cameraHasOpened(this);
        Log.e("CameraClass", "Camera: " + mName + " is open");
    }

}
