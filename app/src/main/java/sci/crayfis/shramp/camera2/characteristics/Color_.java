package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Color_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Color_.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Color_() {}

    //**********************************************************************************************
    // Class Methods
    //--------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     * @param cameraCharacteristics
     * @param characteristicsMap
     */
    protected void read(@NonNull CameraCharacteristics cameraCharacteristics,
                        @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap) {

        Log.e("               Color_", "reading Color_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES;//////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                assert modes != null;
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF          = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF;
                Integer FAST         = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_FAST;
                Integer HIGH_QUALITY = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY;

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF";
                }
                else {
                    value       =  FAST;
                    valueString = "FAST";
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @Override
                    public String formatValue(Integer value) {
                        return getValueString();
                    }
                };
                property = new Parameter<>(name, value, units, formatter);
            }
            else {
                property = new Parameter<>(name);
            }
            characteristicsMap.put(key, property);
        }
        //==========================================================================================
    }
}
