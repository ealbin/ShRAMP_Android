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
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step02_Black_ extends step01_Control_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step02_Black_................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step02_Black_() { super(); }

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

        Log.e("               Black_", "setting default Black_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Boolean> rKey;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> setting;

            String  name;
            Boolean value;
            String  valueString;

            rKey = CaptureRequest.BLACK_LEVEL_LOCK;/////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                //Boolean OFF = false;
                Boolean ON  = true;

                value = ON;
                valueString = "ON BUT UNCONFIRMED";

                formatter = new ParameterFormatter<Boolean>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Boolean value) {
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
    }

}