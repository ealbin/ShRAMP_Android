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
 * @updated: 24 April 2019
 */

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

import org.apache.commons.math3.exception.MathInternalError;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * Super-most class for default CaptureRequest creation, these parameters are set first and include:
 *    CONTROL_MODE
 *    CONTROL_CAPTURE_INTENT
 *    CONTROL_AWB_MODE
 *    CONTROL_AWB_LOCK
 *    CONTROL_AWB_REGIONS
 *    CONTROL_AF_MODE
 *    CONTROL_AF_REGIONS
 *    CONTROL_AF_TRIGGER
 *    CONTROL_AE_MODE
 *    CONTROL_AE_LOCK
 *    CONTROL_AE_REGIONS
 *    CONTROL_AE_PRECAPTURE_TRIGGER
 *    CONTROL_AE_ANTIBANDING_MODE
 *    CONTROL_AE_EXPOSURE_COMPENSATION
 *    CONTROL_AE_TARGET_FPS_RANGE
 *    CONTROL_EFFECT_MODE
 *    CONTROL_ENABLE_ZSL
 *    CONTROL_POST_RAW_SENSITIVITY_BOOST
 *    CONTROL_SCENE_MODE
 *    CONTROL_VIDEO_STABILIZATION_MODE
 */
@TargetApi(21)
abstract class step01_Control_ {

    // Protected Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Creating a default CaptureRequest, setting CONTROL_.* parameters
     * @param builder CaptureRequest.Builder in progress
     * @param characteristicsMap Parameter map of characteristics
     * @param captureRequestMap Parameter map of capture request settings
     */
    @SuppressWarnings("unchecked")
    protected void makeDefault(@NonNull CaptureRequest.Builder builder,
                               @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap,
                               @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap) {

        Log.e("             Control_", "setting default Control_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        if (supportedKeys == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Supported keys cannot be null");
            MasterController.quitSafely();
            return;
        }

        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;

            rKey = CaptureRequest.CONTROL_MODE;/////////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Integer OFF            = CameraMetadata.CONTROL_MODE_OFF;
                Integer AUTO           = CameraMetadata.CONTROL_MODE_AUTO;
                //Integer USE_SCENE_MODE = CameraMetadata.CONTROL_MODE_USE_SCENE_MODE;
                //Integer OFF_KEEP_STATE = CameraMetadata.CONTROL_MODE_OFF_KEEP_STATE;

                if (GlobalSettings.FORCE_CONTROL_MODE_AUTO) {
                    value = AUTO;
                    valueString = "AUTO (FORCED)";
                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, value, null, formatter);
                }
                else if (Build.VERSION.SDK_INT >= 23) {
                    CameraCharacteristics.Key<int[]> cKey;
                    cKey = CameraCharacteristics.CONTROL_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Control available modes cannot null");
                        MasterController.quitSafely();
                        return;
                    }

                    setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                         property.getFormatter());
                }
                else {
                    CameraCharacteristics.Key<Integer> cKey;
                    cKey = CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Supported hardware level cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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
                    setting = new Parameter<>(name, value, null, formatter);
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

            rKey = CaptureRequest.CONTROL_CAPTURE_INTENT;///////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer[]> properties;

                cKey = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
                properties = characteristicsMap.get(cKey);
                if (properties == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Available capabilities cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                Integer[] capabilities = properties.getValue();
                if (capabilities == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Capabilities cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> abilities = ArrayToList.convert(capabilities);

                //Integer CUSTOM           = CameraMetadata.CONTROL_CAPTURE_INTENT_CUSTOM;
                Integer PREVIEW          = CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW;
                //Integer STILL_CAPTURE    = CameraMetadata.CONTROL_CAPTURE_INTENT_STILL_CAPTURE;
                //Integer VIDEO_RECORD     = CameraMetadata.CONTROL_CAPTURE_INTENT_VIDEO_RECORD;
                //Integer VIDEO_SNAPSHOT   = CameraMetadata.CONTROL_CAPTURE_INTENT_VIDEO_SNAPSHOT;
                //Integer ZERO_SHUTTER_LAG = CameraMetadata.CONTROL_CAPTURE_INTENT_ZERO_SHUTTER_LAG;
                Integer MANUAL           = CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL;
                //Integer MOTION_TRACKING  = CameraMetadata.CONTROL_CAPTURE_INTENT_MOTION_TRACKING;

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
                setting = new Parameter<>(name, value, null, formatter);

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
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;

            rKey = CaptureRequest.CONTROL_AWB_MODE;/////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Control mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (GlobalSettings.FORCE_CONTROL_MODE_AUTO) {
                    value = CameraMetadata.CONTROL_AWB_MODE_AUTO;
                    valueString = "AUTO (FORCED)";
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
                else if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AWB modes cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            rKey = CaptureRequest.CONTROL_AWB_LOCK;/////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AWB_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AWB mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (!mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
                }
                else if (Build.VERSION.SDK_INT >= 23) {
                    CameraCharacteristics.Key<Boolean> cKey;
                    Parameter<Boolean> property;

                    cKey     = CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AWB lock cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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
                    setting = new Parameter<>(name, property.getValue(), null, formatter);
                }
                else {
                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            return "LOCK ATTEMPTED BUT UNCONFIRMED";
                        }
                    };
                    setting = new Parameter<>(name, true, null, formatter);
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
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;

            rKey = CaptureRequest.CONTROL_AF_MODE;//////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Control mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (GlobalSettings.FORCE_CONTROL_MODE_AUTO) {
                    value = CameraMetadata.CONTROL_AF_MODE_AUTO;
                    valueString = "AUTO (FORCED)";
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
                else if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AF modes cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            rKey = CaptureRequest.CONTROL_AF_TRIGGER;///////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<Integer>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, null, formatter);
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
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;

            rKey = CaptureRequest.CONTROL_AE_MODE;//////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Control mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (GlobalSettings.FORCE_CONTROL_MODE_AUTO) {
                    value = CameraMetadata.CONTROL_AWB_MODE_AUTO;
                    valueString = "AUTO (FORCED)";
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
                else if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AE modes cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            rKey = CaptureRequest.CONTROL_AE_LOCK;//////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (!mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (PREFERRED)");
                }
                else if (Build.VERSION.SDK_INT >= 23) {
                    CameraCharacteristics.Key<Boolean> cKey;
                    Parameter<Boolean> property;

                    cKey     = CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AE lock cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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
                    setting = new Parameter<>(name, property.getValue(), null, formatter);
                }
                else {
                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            return "LOCK ATTEMPTED BUT UNCONFIRMED";
                        }
                    };
                    setting = new Parameter<>(name, true, null, formatter);
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

            rKey = CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER;////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                formatter = new ParameterFormatter<Integer>("NOT APPLICABLE") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, null, formatter);
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

            rKey = CaptureRequest.CONTROL_AE_ANTIBANDING_MODE;//////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<int[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AE antibanding modes cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            rKey = CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION;/////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<Range<Integer>> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AE compensation range cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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
            ParameterFormatter<Range<Integer>> formatter;
            Parameter<Range<Integer>> setting;

            String name;
            Range<Integer> value;

            rKey = CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE;//////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Range<Integer>[]> property;

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (mode.toString().contains("AUTO")) {
                    CameraCharacteristics.Key<Range<Integer>[]> cKey;

                    cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
                    property = characteristicsMap.get(cKey);
                    if (property == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AE target FPS ranges cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    Range<Integer>[] ranges = property.getValue();
                    if (ranges == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "FPS ranges cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    // Select fastest range
                    value = ranges[ranges.length - 1];

                    formatter = new ParameterFormatter<Range<Integer>>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Range<Integer> value) {
                            return value.toString();
                        }
                    };
                    setting = new Parameter<>(name, value, property.getUnits(), formatter);

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

            rKey = CaptureRequest.CONTROL_EFFECT_MODE;//////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> properties;

                cKey = CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS;
                properties = characteristicsMap.get(cKey);
                if (properties == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Available effects cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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

            if (Build.VERSION.SDK_INT >= 26) {

                rKey = CaptureRequest.CONTROL_ENABLE_ZSL;///////////////////////////////////////////
                name = rKey.getName();

                if (supportedKeys.contains(rKey)) {

                    formatter = new ParameterFormatter<Boolean>("DISABLED (PREFERRED)") {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, false, null, formatter);

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
                    if (properties == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Sensitivity boost range cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            rKey = CaptureRequest.CONTROL_SCENE_MODE;///////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> properties;

                cKey = CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES;
                properties = characteristicsMap.get(cKey);
                if (properties == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Available scene modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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

            rKey = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE;/////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> properties;

                cKey = CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES;
                properties = characteristicsMap.get(cKey);
                if (properties == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Video stabilization modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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