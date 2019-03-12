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

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step11_Reprocess_ extends step10_Noise_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step11_Reprocess_............................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step11_Reprocess_() { super(); }

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

        Log.e("           Reprocess_", "setting default Reprocess_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

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

            rKey  = CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR;/////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer[]> property;

                cKey = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                Integer[] capabilities = property.getValue();
                assert capabilities != null;
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
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
    }

}