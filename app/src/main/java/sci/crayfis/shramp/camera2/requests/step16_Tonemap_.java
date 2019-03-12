package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Build;
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
abstract class step16_Tonemap_ extends step15_Statistics_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step16_Tonemap_..............................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step16_Tonemap_() { super(); }

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

        Log.e("             Tonemap_", "setting default Tonemap_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.TONEMAP_MODE;/////////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES;
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
            CaptureRequest.Key<TonemapCurve> rKey;
            ParameterFormatter<TonemapCurve> formatter;
            Parameter<TonemapCurve> setting;

            String  name;
            TonemapCurve value;
            String  valueString;
            String  units;

            rKey  = CaptureRequest.TONEMAP_CURVE;///////////////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode = captureRequestMap.get(CaptureRequest.TONEMAP_MODE);
                assert mode != null;

                if (mode.toString().equals("CONTRAST_CURVE")) {
                    float[] linear_response = {0, 0, 1, 1};
                    value = new TonemapCurve(linear_response, linear_response, linear_response);
                    valueString = "LINEAR RESPONSE";

                    formatter = new ParameterFormatter<TonemapCurve>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull TonemapCurve value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, value, units, formatter);

                    builder.set(rKey, setting.getValue());
                    captureRequestMap.put(rKey, setting);
                }
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

            if (Build.VERSION.SDK_INT >= 23) {
                rKey  = CaptureRequest.TONEMAP_GAMMA;///////////////////////////////////////////////
                name  = rKey.getName();
                units = null;

                if (supportedKeys.contains(rKey)) {

                    Parameter<Integer> mode = captureRequestMap.get(CaptureRequest.TONEMAP_MODE);
                    assert mode != null;

                    if (mode.toString().equals("GAMMA_VALUE")) {
                        value = 5.f;
                        valueString = "pow(val, 1./5.)";

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
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            if (Build.VERSION.SDK_INT >= 23) {
                rKey  = CaptureRequest.TONEMAP_PRESET_CURVE;////////////////////////////////////////
                name  = rKey.getName();
                units = null;

                if (supportedKeys.contains(rKey)) {

                    Parameter<Integer> mode = captureRequestMap.get(CaptureRequest.TONEMAP_MODE);
                    assert mode != null;

                    if (mode.toString().equals("FAST") || mode.toString().equals("HIGH_QUALITY")) {

                        Integer SRGB   = CameraMetadata.TONEMAP_PRESET_CURVE_SRGB;
                        Integer REC709 = CameraMetadata.TONEMAP_PRESET_CURVE_REC709;

                        value = REC709;
                        valueString = "REC709";

                        formatter = new ParameterFormatter<Integer>(valueString) {
                            @NonNull
                            @Override
                            public String formatValue(@NonNull Integer value) {
                                return getValueString();
                            }
                        };
                        setting = new Parameter<>(name, value, units, formatter);

                        builder.set(rKey, setting.getValue());
                        captureRequestMap.put(rKey, setting);
                    }
                }
            }
        }
        //==========================================================================================
    }

}