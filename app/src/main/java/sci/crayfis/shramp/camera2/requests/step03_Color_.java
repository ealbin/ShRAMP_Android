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

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step03_Color_................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step03_Color_() { super(); }

    // Protected Overriding Instance Methods
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

        Log.e("               Color_", "setting default Color_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            rKey = CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE;/////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());
                builder.set(rKey, setting.getValue());
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<RggbChannelVector> rKey;
            Parameter<RggbChannelVector> setting;
            ParameterFormatter<RggbChannelVector> formatter;

            String name;
            RggbChannelVector value;
            String units;

            rKey  = CaptureRequest.COLOR_CORRECTION_GAINS;//////////////////////////////////////////
            name  = rKey.getName();
            value = new RggbChannelVector(1, 1, 1, 1);
            units = "unitless gain factor";

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<RggbChannelVector>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull RggbChannelVector value) {
                        return value.toString();
                    }
                };
                setting = new Parameter<>(name, value, units, formatter);

                builder.set(rKey, value);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;

            rKey = CaptureRequest.COLOR_CORRECTION_MODE;////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AWB_MODE);
                assert mode != null;

                if (mode.toString().contains("DISABLED")) {

                    Integer TRANSFORM_MATRIX = CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX;
                    //Integer FAST             = CameraMetadata.COLOR_CORRECTION_MODE_FAST;
                    //Integer HIGH_QUALITY     = CameraMetadata.COLOR_CORRECTION_MODE_HIGH_QUALITY;

                    value = TRANSFORM_MATRIX;
                    valueString = "TRANSFORM_MATRIX (PREFERRED)";

                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, value, null, formatter);

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (FALLBACK)");
                }
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<ColorSpaceTransform> key;
            Parameter<ColorSpaceTransform> setting;
            ParameterFormatter<ColorSpaceTransform> formatter;

            String name;
            ColorSpaceTransform value;
            String valueString;

            key   = CaptureRequest.COLOR_CORRECTION_TRANSFORM;//////////////////////////////////////
            name  = key.getName();
            value = new ColorSpaceTransform(new int[]{
                                                1, 1, 0, 1, 0, 1,    // 1/1 , 0/1 , 0/1 = 1 0 0
                                                0, 1, 1, 1, 0, 1,    // 0/1 , 1/1 , 0/1 = 0 1 0
                                                0, 1, 0, 1, 1, 1     // 0/1 , 0/1 , 1/1 = 0 0 1
                                            });
            valueString = "(1 0 0),(0 1 0),(0 0 1)";

            if (supportedKeys.contains(key)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.COLOR_CORRECTION_MODE);
                assert mode != null;

                if (mode.toString().contains("DISABLED")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (FALLBACK)");
                }
                else {
                    formatter = new ParameterFormatter<ColorSpaceTransform>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull ColorSpaceTransform value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, value, null, formatter);

                    builder.set(key, value);
                }
                captureRequestMap.put(key, setting);
            }
        }
        //==========================================================================================
    }

}