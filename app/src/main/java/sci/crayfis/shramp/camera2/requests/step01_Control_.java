package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step01_Control_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step01_Control_.....................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step01_Control_() {}

    //**********************************************************************************************
    // Class Methods
    //--------------

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
    protected void makeDefault(@NonNull CaptureRequest.Builder builder,
                               @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap,
                               @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap) {

        Log.e("             Control_", "setting default Control_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            rKey  = CaptureRequest.CONTROL_MODE;////////////////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                if (Build.VERSION.SDK_INT >= 23) {
                    CameraCharacteristics.Key<int[]> cKey;
                    cKey = CameraCharacteristics.CONTROL_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                         property.getFormatter());
                }
                else {
                    CameraCharacteristics.Key<Integer> cKey;
                    cKey = CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    Integer OFF            = CameraMetadata.CONTROL_MODE_OFF;
                    Integer AUTO           = CameraMetadata.CONTROL_MODE_AUTO;
                    Integer USE_SCENE_MODE = CameraMetadata.CONTROL_MODE_USE_SCENE_MODE;
                    Integer OFF_KEEP_STATE = CameraMetadata.CONTROL_MODE_OFF_KEEP_STATE;

                    if (property.toString().equals("LEGACY")
                            || property.toString().equals("EXTERNAL")) {

                        value = AUTO;
                        valueString = "AUTO (FALLBACK)";
                    } else {
                        value = OFF;
                        valueString = "OFF (PREFERRED)";
                    }
                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, value, units, formatter);
                }
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
            CaptureRequest.Key<Integer> rKey;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            rKey  = CaptureRequest.CONTROL_CAPTURE_INTENT;//////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer[]> properties;

                cKey = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
                properties = characteristicsMap.get(cKey);
                assert properties != null;

                Integer[] capabilities = properties.getValue();
                assert capabilities != null;
                List<Integer> abilities = ArrayToList.convert(capabilities);

                Integer CUSTOM           = CameraMetadata.CONTROL_CAPTURE_INTENT_CUSTOM;
                Integer PREVIEW          = CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW;
                Integer STILL_CAPTURE    = CameraMetadata.CONTROL_CAPTURE_INTENT_STILL_CAPTURE;
                Integer VIDEO_RECORD     = CameraMetadata.CONTROL_CAPTURE_INTENT_VIDEO_RECORD;
                Integer VIDEO_SNAPSHOT   = CameraMetadata.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT;
                Integer ZERO_SHUTTER_LAG = CameraMetadata.CONTROL_CAPTURE_INTENT_ZERO_SHUTTER_LAG;
                Integer MANUAL           = CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL;
                Integer MOTION_TRACKING  = CameraMetadata.CONTROL_CAPTURE_INTENT_MOTION_TRACKING;

                if (abilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                    value = MANUAL;
                    valueString = "MANUAL (PREFERRED)";
                }
                else {
                    value = PREVIEW;
                    valueString = "PREVIEW (FALLBACK)";
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, value, units, formatter);

                builder.set(rKey, setting.getValue());
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        //                                 Auto-white Balance
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.CONTROL_AWB_MODE;////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                         property.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
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
            CaptureRequest.Key<Boolean> rKey;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> setting;

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AWB_LOCK;////////////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AWB_MODE);
                assert mode != null;

                if (!mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
                }
                else if (Build.VERSION.SDK_INT >= 23) {
                    CameraCharacteristics.Key<Boolean> cKey;
                    Parameter<Boolean> property;

                    cKey     = CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            if (value) {
                                return "LOCKED (PREFERRED)";
                            }
                            return "NOT LOCKED (FALLBACK)";
                        }
                    };
                    setting = new Parameter<>(name, property.getValue(), units, formatter);
                }
                else {
                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            return "LOCK ATTEMPTED BUT UNCONFIRMED";
                        }
                    };
                    setting = new Parameter<>(name, true, units, formatter);
                }

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
            CaptureRequest.Key<MeteringRectangle[]> rKey;
            ParameterFormatter<MeteringRectangle[]> formatter;
            Parameter<MeteringRectangle[]> setting;

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AWB_REGIONS;/////////////////////////////////////////////
            name  = rKey.getName();
            units = "pixel coordinates";

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<MeteringRectangle[]>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull MeteringRectangle[] value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        //                                 Auto Focus
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.CONTROL_AF_MODE;/////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                            property.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
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
            CaptureRequest.Key<MeteringRectangle[]> rKey;
            ParameterFormatter<MeteringRectangle[]> formatter;
            Parameter<MeteringRectangle[]> setting;

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AF_REGIONS;//////////////////////////////////////////////
            name  = rKey.getName();
            units = "pixel coordinates";

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<MeteringRectangle[]>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull MeteringRectangle[] value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
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

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AF_TRIGGER;//////////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<Integer>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        //                               Auto Exposure
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.CONTROL_AE_MODE;/////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                            property.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
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
            CaptureRequest.Key<Boolean> rKey;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> setting;

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AE_LOCK;/////////////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (!mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
                }
                else if (Build.VERSION.SDK_INT >= 23) {
                    CameraCharacteristics.Key<Boolean> cKey;
                    Parameter<Boolean> property;

                    cKey     = CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            if (value) {
                                return "LOCKED (PREFERRED)";
                            }
                            return "NOT LOCKED (FALLBACK)";
                        }
                    };
                    setting = new Parameter<>(name, property.getValue(), units, formatter);
                }
                else {
                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            return "LOCK ATTEMPTED BUT UNCONFIRMED";
                        }
                    };
                    setting = new Parameter<>(name, true, units, formatter);
                }

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
            CaptureRequest.Key<MeteringRectangle[]> rKey;
            ParameterFormatter<MeteringRectangle[]> formatter;
            Parameter<MeteringRectangle[]> setting;

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AE_REGIONS;//////////////////////////////////////////////
            name  = rKey.getName();
            units = "pixel coordinates";

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<MeteringRectangle[]>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull MeteringRectangle[] value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
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

            String name;
            String units;

            rKey  = CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER;///////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<Integer>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
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
            Parameter<Integer> setting;

            String name;

            rKey  = CaptureRequest.CONTROL_AE_ANTIBANDING_MODE;/////////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                            property.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
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
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey  = CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION;////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<Range<Integer>> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                            property.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
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
            CaptureRequest.Key<Range<Integer>> rKey;
            Parameter<Range<Integer>> setting;

            String name;

            rKey  = CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE;/////////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Range<Integer>> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<Range<Integer>[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                            property.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
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
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            rKey  = CaptureRequest.CONTROL_EFFECT_MODE;/////////////////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> properties;

                cKey = CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS;
                properties = characteristicsMap.get(cKey);
                assert properties != null;

                setting = new Parameter<>(name, properties.getValue(), properties.getUnits(),
                                                                       properties.getFormatter());

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
            CaptureRequest.Key<Boolean> rKey;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> setting;

            String name;
            String units;

            if (Build.VERSION.SDK_INT >= 26) {

                rKey  = CaptureRequest.CONTROL_ENABLE_ZSL;//////////////////////////////////////////
                name  = rKey.getName();
                units = null;

                if (supportedKeys.contains(rKey)) {

                    formatter = new ParameterFormatter<Boolean>("DISABLED (PREFERRED)") {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, false, units, formatter);

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("NOT SUPPORTED");
                }
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            if (Build.VERSION.SDK_INT >= 24) {
                rKey = CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST;///////////////////////////
                name = rKey.getName();

                if (supportedKeys.contains(rKey)) {

                    CameraCharacteristics.Key<Range<Integer>> cKey;
                    Parameter<Integer> properties;

                    cKey = CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE;
                    properties = characteristicsMap.get(cKey);
                    assert properties != null;

                    setting = new Parameter<>(name, properties.getValue(), properties.getUnits(),
                                                                           properties.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("NOT SUPPORTED");
                }
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            rKey  = CaptureRequest.CONTROL_SCENE_MODE;//////////////////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> properties;

                cKey = CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES;
                properties = characteristicsMap.get(cKey);
                assert properties != null;

                setting = new Parameter<>(name, properties.getValue(), properties.getUnits(),
                                                                       properties.getFormatter());

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
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            rKey  = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE;////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> properties;

                cKey = CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES;
                properties = characteristicsMap.get(cKey);
                assert properties != null;

                setting = new Parameter<>(name, properties.getValue(), properties.getUnits(),
                        properties.getFormatter());

                builder.set(rKey, setting.getValue());
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
    }

}