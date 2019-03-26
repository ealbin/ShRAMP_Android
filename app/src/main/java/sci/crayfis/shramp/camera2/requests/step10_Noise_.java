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

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step10_Noise_ extends step09_Lens_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step10_Noise_................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step10_Noise_() { super(); }

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

        Log.e("               Noise_", "setting default Noise_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.NOISE_REDUCTION_MODE;/////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES;
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
    }

}