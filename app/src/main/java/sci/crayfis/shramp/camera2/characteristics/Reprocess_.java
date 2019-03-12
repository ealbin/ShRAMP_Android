package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Reprocess_ extends Noise_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Reprocess_...................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Reprocess_() { super(); }

    //**********************************************************************************************
    // Overriding Methods
    //-------------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     * @param cameraCharacteristics
     * @param characteristicsMap
     */
    @Override
    protected void read(@NonNull CameraCharacteristics cameraCharacteristics,
                        @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap) {
        super.read(cameraCharacteristics, characteristicsMap);

        Log.e("           Reprocess_", "reading Reprocess_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.REPROCESS_MAX_CAPTURE_STALL;//////////////////////////
                name  = key.getName();
                units = "number of frames";

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    assert value != null;

                    formatter = new ParameterFormatter<Integer>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return value.toString();
                        }
                    };
                    property = new Parameter<>(name, value, units, formatter);
                } else {
                    property = new Parameter<>(name);
                }
                characteristicsMap.put(key, property);
            }
        }
        //==========================================================================================
    }
}