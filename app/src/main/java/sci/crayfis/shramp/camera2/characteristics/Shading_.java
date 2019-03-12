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
abstract class Shading_ extends Sensor_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Shading_.....................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Shading_() { super(); }

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

        Log.e("             Shading_", "reading Shading_ characteristics");
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

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.SHADING_AVAILABLE_MODES;//////////////////////////////
                name  = key.getName();
                units = null;

                if (keychain.contains(key)) {
                    int[]  modes  = cameraCharacteristics.get(key);
                    assert modes != null;
                    List<Integer> options = ArrayToList.convert(modes);

                    Integer OFF          = CameraMetadata.SHADING_MODE_OFF;
                    Integer FAST         = CameraMetadata.SHADING_MODE_FAST;
                    Integer HIGH_QUALITY = CameraMetadata.SHADING_MODE_HIGH_QUALITY;

                    if (options.contains(OFF)) {
                        value       =  OFF;
                        valueString = "OFF";
                    }
                    else {
                        value       =  FAST;
                        valueString = "FAST";
                    }

                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
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