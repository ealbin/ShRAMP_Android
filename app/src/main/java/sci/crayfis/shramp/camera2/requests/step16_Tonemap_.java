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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * Configuration class for default CaptureRequest creation, the parameters set here include:
 *    TONEMAP_MODE
 *    TONEMAP_CURVE
 *    TONEMAP_GAMMA
 *    TONEMAP_PRESET_CURVE
 */
@TargetApi(21)
abstract class step16_Tonemap_ extends step15_Statistics_ {

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Creating a default CaptureRequest, setting TONEMAP_.* parameters
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

        Log.e("             Tonemap_", "setting default Tonemap_ requests");
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
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.TONEMAP_MODE;/////////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES;
                property = characteristicsMap.get(cKey);
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Tone map modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
            CaptureRequest.Key<TonemapCurve> rKey;
            ParameterFormatter<TonemapCurve> formatter;
            Parameter<TonemapCurve> setting;

            String  name;
            TonemapCurve value;
            String  valueString;

            rKey = CaptureRequest.TONEMAP_CURVE;////////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode = captureRequestMap.get(CaptureRequest.TONEMAP_MODE);
                if (mode == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Tone map mode cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                if (mode.toString().contains("CONTRAST_CURVE")) {
                    float[] linear_response = {0, 0, 1, 1};
                    value = new TonemapCurve(linear_response, linear_response, linear_response);
                    valueString = "LINEAR RESPONSE (PREFERRED)";

                    formatter = new ParameterFormatter<TonemapCurve>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull TonemapCurve value) {
                            return getValueString();
                        }
                    };
                    setting = new Parameter<>(name, value, null, formatter);

                    builder.set(rKey, setting.getValue());
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED");
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
            CaptureRequest.Key<Float> rKey;
            ParameterFormatter<Float> formatter;
            Parameter<Float> setting;

            String name;
            Float  value;
            String valueString;

            if (Build.VERSION.SDK_INT >= 23) {
                rKey = CaptureRequest.TONEMAP_GAMMA;////////////////////////////////////////////////
                name = rKey.getName();

                if (supportedKeys.contains(rKey)) {

                    Parameter<Integer> mode = captureRequestMap.get(CaptureRequest.TONEMAP_MODE);
                    if (mode == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Tone map mode cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    if (mode.toString().contains("GAMMA_VALUE")) {
                        value = 5.f;
                        valueString = "pow(val, 1./5.) (FALLBACK)";

                        formatter = new ParameterFormatter<Float>(valueString) {
                            @NonNull
                            @Override
                            public String formatValue(@NonNull Float value) {
                                return getValueString();
                            }
                        };
                        setting = new Parameter<>(name, value, null, formatter);

                        builder.set(rKey, setting.getValue());
                    }
                    else {
                        setting = new Parameter<>(name);
                        setting.setValueString("DISABLED");
                    }
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
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  valueString;

            if (Build.VERSION.SDK_INT >= 23) {
                rKey = CaptureRequest.TONEMAP_PRESET_CURVE;/////////////////////////////////////////
                name = rKey.getName();

                if (supportedKeys.contains(rKey)) {

                    Parameter<Integer> mode = captureRequestMap.get(CaptureRequest.TONEMAP_MODE);
                    if (mode == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Tone map mode cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    if (mode.toString().contains("FAST") || mode.toString().contains("HIGH_QUALITY")) {

                        //Integer SRGB   = CameraMetadata.TONEMAP_PRESET_CURVE_SRGB;
                        Integer REC709 = CameraMetadata.TONEMAP_PRESET_CURVE_REC709;

                        value = REC709;
                        valueString = "REC709 (LAST CHOICE)";

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
                        setting.setValueString("DISABLED");
                    }
                }
                else {
                    setting = new Parameter<>(name);
                    setting.setValueString("NOT SUPPORTED");
                }
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
    }

}