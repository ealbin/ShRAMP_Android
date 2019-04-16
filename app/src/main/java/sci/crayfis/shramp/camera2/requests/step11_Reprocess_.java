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
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * Configuration class for default CaptureRequest creation, the parameters set here include:
 *    REPROCESS_EFFECTIVE_EXPOSURE_FACTOR
 */
@TargetApi(21)
abstract class step11_Reprocess_ extends step10_Noise_ {

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Creating a default CaptureRequest, setting REPROCESS_.* parameters
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

        Log.e("           Reprocess_", "setting default Reprocess_ requests");
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
            ParameterFormatter<Float> formatter;
            Parameter<Float> setting;

            String name;
            Float  value;
            String units;

            if (Build.VERSION.SDK_INT < 23) {
                return;
            }

            rKey = CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR;//////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer[]> property;

                cKey = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
                property = characteristicsMap.get(cKey);
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Available capabilites cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                Integer[] capabilities = property.getValue();
                if (capabilities == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Capabilities cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> abilities = ArrayToList.convert(capabilities);

                if (!abilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING)) {
                    return;
                }

                value = 1.f;
                units = "relative exposure time increase factor";

                formatter = new ParameterFormatter<Float>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
                        return value.toString();
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
    }

}