package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
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
abstract class Noise_ extends Logical_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Noise_.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Noise_() { super(); }

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

        Log.e("               Noise_", "reading Noise_ characteristics");
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

            key   = CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES;//////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                assert modes != null;
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF              = CameraMetadata.NOISE_REDUCTION_MODE_OFF;
                Integer FAST             = CameraMetadata.NOISE_REDUCTION_MODE_FAST;
                Integer HIGH_QUALITY     = CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY;
                Integer MINIMAL          = null;
                Integer ZERO_SHUTTER_LAG = null;
                if (Build.VERSION.SDK_INT >= 23) {
                    MINIMAL          = CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL;
                    ZERO_SHUTTER_LAG = CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG;
                }

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF (PREFERRED)";
                }
                else if (MINIMAL != null && options.contains(MINIMAL)) {
                    value       =  MINIMAL;
                    valueString = "MINIMAL (FALLBACK)";
                }
                else {
                    value       =  FAST;
                    valueString = "FAST (LAST CHOICE)";
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                property = new Parameter<>(name, value, units, formatter);
            }
            else {
                property = new Parameter<>(name);
                property.setValueString("NOT SUPPORTED");
            }
            characteristicsMap.put(key, property);
        }
        //==========================================================================================
    }
}