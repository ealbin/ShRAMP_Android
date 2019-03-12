package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
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
abstract class Flash_ extends Edge_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Flash_.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Flash_() { super(); }

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

        Log.e("               Flash_", "reading Flash_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Boolean> key;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> property;

            String  name;
            Boolean value;
            String  units;

            key   = CameraCharacteristics.FLASH_INFO_AVAILABLE;/////////////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Boolean>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Boolean value) {
                        if (value) {
                            return "YES";
                        }
                        return "NO";
                    }
                };
                property = new Parameter<>(name, value, units, formatter);
            } else {
                property = new Parameter<>(name);
            }
            characteristicsMap.put(key, property);
        }
        //==========================================================================================
    }
}