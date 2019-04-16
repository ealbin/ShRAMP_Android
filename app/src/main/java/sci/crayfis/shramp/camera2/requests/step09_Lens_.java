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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
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
 *    LENS_APERTURE
 *    LENS_FILTER_DENSITY
 *    LENS_FOCAL_LENGTH
 *    LENS_FOCUS_DISTANCE
 *    LENS_OPTICAL_STABILIZATION_MODE
 */
@TargetApi(21)
abstract class step09_Lens_ extends step08_Jpeg_ {

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Creating a default CaptureRequest, setting LENS_.* parameters
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

        Log.e("                Lens_", "setting default Lens_ requests");
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
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens apertures cannot be null");
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
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens filter densities cannot be null");
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
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens focal lengths cannot be null");
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
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens calibration cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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

            rKey = CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE;//////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION;
                property = characteristicsMap.get(cKey);
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens stabilization cannot be null");
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
    }

}