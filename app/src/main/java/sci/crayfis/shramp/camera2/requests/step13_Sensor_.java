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
abstract class step13_Sensor_ extends step12_Scaler_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step13_Sensor_...............................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step13_Sensor_() { super(); }

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

        Log.e("              Sensor_", "setting default Sensor_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Long> rKey;
            Parameter<Long> setting;

            String name;

            rKey = CaptureRequest.SENSOR_EXPOSURE_TIME;/////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                // TODO: finish after Control_

                /*
                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
                */
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Long> rKey;
            Parameter<Long> setting;

            String name;

            rKey = CaptureRequest.SENSOR_FRAME_DURATION;////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                // TODO: finish after Control_

                /*
                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
                */
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.SENSOR_SENSITIVITY;///////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                // TODO: finish after Control_

                /*
                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
                */
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.SENSOR_TEST_PATTERN_MODE;/////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES;
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
            CaptureRequest.Key<int[]> rKey;
            Parameter<int[]> setting;

            String name;

            rKey = CaptureRequest.SENSOR_TEST_PATTERN_DATA;/////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                // TODO: finish after Control_

                /*
                CameraCharacteristics.Key<float[]> cKey;
                Parameter<Float> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
                */
            }
        }
        //==========================================================================================
    }

}