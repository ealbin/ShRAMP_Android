package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.RggbChannelVector;
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
abstract class step03_Color_ extends step02_Black_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step03_Color_................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step03_Color_() { super(); }

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

        Log.e("               Color_", "setting default Color_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            rKey  = CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE;////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES;
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
            CaptureRequest.Key<RggbChannelVector> key;
            Parameter<RggbChannelVector> setting;
            ParameterFormatter<RggbChannelVector> formatter;

            String name;
            RggbChannelVector value;
            String units;

            key   = CaptureRequest.COLOR_CORRECTION_GAINS;//////////////////////////////////////////
            name  = key.getName();
            value = new RggbChannelVector(1, 1, 1, 1);
            units = "unitless gain factor";

            if (supportedKeys.contains(key)) {

                formatter = new ParameterFormatter<RggbChannelVector>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull RggbChannelVector value) {
                        return value.toString();
                    }
                };
                setting = new Parameter<>(name, value, units, formatter);

                builder.set(key, value);
                captureRequestMap.put(key, setting);
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

            rKey  = CaptureRequest.COLOR_CORRECTION_MODE;///////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                Integer TRANSFORM_MATRIX = CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX;
                Integer FAST             = CameraMetadata.COLOR_CORRECTION_MODE_FAST;
                Integer HIGH_QUALITY     = CameraMetadata.COLOR_CORRECTION_MODE_HIGH_QUALITY;

                // TODO: do this after you do control

                //builder.set(rKey, setting.getValue());
                //captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<ColorSpaceTransform> key;
            Parameter<ColorSpaceTransform> setting;
            ParameterFormatter<ColorSpaceTransform> formatter;

            String name;
            ColorSpaceTransform value;
            String valueString;
            String units;

            key   = CaptureRequest.COLOR_CORRECTION_TRANSFORM;//////////////////////////////////////
            name  = key.getName();
            value = new ColorSpaceTransform(new int[]{
                                                1, 1, 0, 1, 0, 1,    // 1/1 , 0/1 , 0/1 = 1 0 0
                                                0, 1, 1, 1, 0, 1,    // 0/1 , 1/1 , 0/1 = 0 1 0
                                                0, 1, 0, 1, 1, 1     // 0/1 , 0/1 , 1/1 = 0 0 1
                                            });
            valueString = "(1 0 0),(0 1 0),(0 0 1)";
            units = null;

            if (supportedKeys.contains(key)) {

                formatter = new ParameterFormatter<ColorSpaceTransform>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, value, units, formatter);

                builder.set(key, value);
                captureRequestMap.put(key, setting);
            }
        }
        //==========================================================================================
    }

}