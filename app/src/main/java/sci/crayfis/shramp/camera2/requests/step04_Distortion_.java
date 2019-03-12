package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
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
abstract class step04_Distortion_ extends step03_Color_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step04_Distortion_...........................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step04_Distortion_() { super(); }

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

        Log.e("          Distortion_", "setting default Distortion_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            if (Build.VERSION.SDK_INT < 28) {
                return;
            }

            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String  name;

            rKey  = CaptureRequest.DISTORTION_CORRECTION_MODE;//////////////////////////////////////
            name  = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.DISTORTION_CORRECTION_AVAILABLE_MODES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());
                builder.set(rKey, setting.getValue());
                captureRequestMap.put(rKey, setting);
            }
        }
        //==========================================================================================
    }

}