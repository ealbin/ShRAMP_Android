package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step09_Lens_ extends step08_Jpeg_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step09_Lens_.................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step09_Lens_() { super(); }

    //**********************************************************************************************
    // Overriding Class Methods
    //-------------------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * TODO: description, comments and logging
     * @param builder
     * @param characteristicsMap
     * @param captureRequestMap
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void makeDefault(@NonNull CaptureRequest.Builder builder,
                               @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap,
                               @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap) {
        super.makeDefault(builder, characteristicsMap, captureRequestMap);

        Log.e("                Lens_", "setting default Lens_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Float> rKey;
            Parameter<Float> setting;

            String name;

            rKey = CaptureRequest.LENS_APERTURE;////////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Float> rKey;
            Parameter<Float> setting;

            String name;

            rKey = CaptureRequest.LENS_FILTER_DENSITY;//////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Float> rKey;
            Parameter<Float> setting;

            String name;

            rKey = CaptureRequest.LENS_FOCAL_LENGTH;////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Float> rKey;
            ParameterFormatter<Float> formatter;
            Parameter<Float> setting;

            String name;
            Float  value;
            String valueString;
            String units;

            rKey = CaptureRequest.LENS_FOCUS_DISTANCE;//////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                value = 0.f;
                valueString = "INFINITY";

                CameraCharacteristics.Key<Integer> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION;
                property = characteristicsMap.get(cKey);
                assert property != null;

                units = property.getUnits();

                formatter = new ParameterFormatter<Float>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, value, units, formatter);

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE;/////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
    }

}