package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class RequestMaker extends step16_Tonemap_ {

    //**********************************************************************************************
    // Static Class Fields
    //--------------------

    // Private Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static RequestMaker mInstance = new RequestMaker();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // RequestMaker.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private RequestMaker() {}

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * TODO: description, comments and logging
     */
    @SuppressWarnings("unchecked")
    public static void makeDefault() {
        Log.e(Thread.currentThread().getName(), "RequestMaker makeDefault");

        LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap = new LinkedHashMap<>();

        CameraDevice cameraDevice = CameraController.getOpenedCameraDevice();
        assert cameraDevice != null;

        LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap;
        characteristicsMap = CameraController.getOpenedCharacteristicsMap();
        assert characteristicsMap != null;

        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================

        int template;///////////////////////////////////////////////////////////////////////////////
        {
            CameraCharacteristics.Key<int[]> key;
            Parameter<Integer[]> parameter;

            key = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;

            parameter = characteristicsMap.get(key);
            assert parameter != null;

            Integer[] capabilities  = parameter.getValue();
            assert capabilities    != null;
            List<Integer> abilities = ArrayToList.convert(capabilities);

            if (abilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                template = CameraDevice.TEMPLATE_MANUAL;
            } else {
                template = CameraDevice.TEMPLATE_PREVIEW;
            }
        }

        //==========================================================================================

        CaptureRequest.Builder builder = null;//////////////////////////////////////////////////////
        try {
            builder = cameraDevice.createCaptureRequest(template);
        }
        catch (CameraAccessException e) {
            // TODO: error
        }
        assert builder != null;

        mInstance.makeDefault(builder, characteristicsMap, captureRequestMap);

        CameraController.setCaptureRequestTemplate(template);
        CameraController.setCaptureRequestBuilder(builder);
        CameraController.setCaptureRequestMap(captureRequestMap);
    }

    // write........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param label bla
     * @param map bla
     * @param keychain bla
     */
    public static void write(@Nullable String label,
                             @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> map,
                             @Nullable List<CaptureRequest.Key<?>> keychain) {

        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        String tag = "RequestMaker";
        if (label != null) {
            tag = label;
        }

        Log.e(tag, "Camera Capture Request Summary:\n");
        for (Parameter parameter : map.values()) {
            Log.e(tag, parameter.toString());
        }

        if (keychain != null) {
            Log.e(tag, "Keys unset:\n");
            for (CaptureRequest.Key<?> key : keychain) {
                if (!map.containsKey(key)) {
                    Log.e(tag, key.getName());
                }
            }
        }
        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
    }

    //**********************************************************************************************
    // Overriding Class Methods
    //-------------------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * TODO: description, comments and logging
     * @param builder bla
     * @param characteristicsMap bla
     * @param captureRequestMap bla
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void makeDefault(@NonNull CaptureRequest.Builder builder,
                               @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap,
                               @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap) {
        super.makeDefault(builder, characteristicsMap, captureRequestMap);
    }

}
