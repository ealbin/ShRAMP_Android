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
 * @updated: 20 April 2019
 */

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

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * Public access to building a CaptureRequest using optimal settings for the current hardware
 */
@TargetApi(21)
final public class RequestMaker extends step16_Tonemap_ {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // Reference to single instance of this class
    private final static RequestMaker mInstance = new RequestMaker();

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
     * Disabled
     */
    private RequestMaker() {}

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Loads an optimized CaptureRequest into the active Camera
     */
    // Quiet compiler -- TODO: not sure what causes this
    @SuppressWarnings("unchecked")
    public static void makeDefault() {

        LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap = new LinkedHashMap<>();

        CameraDevice cameraDevice = CameraController.getOpenedCameraDevice();
        if (cameraDevice == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Camera device cannot be null");
            MasterController.quitSafely();
            return;
        }

        LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap;
        characteristicsMap = CameraController.getOpenedCharacteristicsMap();
        if (characteristicsMap == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Characteristics map cannot be null");
            MasterController.quitSafely();
            return;
        }

        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        if (supportedKeys == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Supported keys cannot be null");
            MasterController.quitSafely();
            return;
        }

        //==========================================================================================

        int template;///////////////////////////////////////////////////////////////////////////////
        {
            CameraCharacteristics.Key<int[]> key;
            Parameter<Integer[]> parameter;

            key = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;

            parameter = characteristicsMap.get(key);
            if (parameter == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES cannot be null");
                MasterController.quitSafely();
                return;
            }

            Integer[] capabilities = parameter.getValue();
            if (capabilities == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Capabilities array cannot be null");
                MasterController.quitSafely();
                return;
            }
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
            Log.e(Thread.currentThread().getName(), "Camera cannot be accessed");
            MasterController.quitSafely();
            return;
        }

        //==========================================================================================

        // Pass to superclasses to complete the build
        mInstance.makeDefault(builder, characteristicsMap, captureRequestMap);

        CameraController.setCaptureRequestTemplate(template);
        CameraController.setCaptureRequestBuilder(builder);
        CameraController.setCaptureRequestMap(captureRequestMap);
    }

    // write........................................................................................
    /**
     * Display the CaptureRequest details, called from Camera
     * @param label (Optional) Custom title
     * @param map Details of CaptureRequest in terms of Parameters<T>
     * @param keychain (Optional) All keys that potentially can be set
     */
    public static void write(@Nullable String label,
                             @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> map,
                             @Nullable List<CaptureRequest.Key<?>> keychain) {

        if (label == null) {
            label = "RequestMaker";
        }

        Log.e(Thread.currentThread().getName(), " \n\n\t\t" + label + " Camera Capture Request Summary:\n\n");
        for (Parameter parameter : map.values()) {
            Log.e(Thread.currentThread().getName(), parameter.toString());
        }

        if (keychain != null) {
            Log.e(Thread.currentThread().getName(), "Keys unset:\n");
            for (CaptureRequest.Key<?> key : keychain) {
                if (!map.containsKey(key)) {
                    Log.e(Thread.currentThread().getName(), key.getName());
                }
            }
        }
        Log.e(Thread.currentThread().getName(), " \n\n ");
    }

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Continue creating a default CaptureRequest with specialized super classes
     * @param builder CaptureRequest.Builder in progress
     * @param characteristicsMap Parameter map of characteristics
     * @param captureRequestMap Parameter map of capture request settings
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void makeDefault(@NonNull CaptureRequest.Builder builder,
                               @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap,
                               @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap) {
        super.makeDefault(builder, characteristicsMap, captureRequestMap);
    }

}