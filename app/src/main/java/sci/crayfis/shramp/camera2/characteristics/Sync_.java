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

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Sync_ extends Statistics_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Sync_........................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Sync_() { super(); }

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

        Log.e("                Sync_", "reading Sync_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.SYNC_MAX_LATENCY;/////////////////////////////////////////
            name  = key.getName();
            units = "frame counts";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer PER_FRAME_CONTROL = CameraMetadata.SYNC_MAX_LATENCY_PER_FRAME_CONTROL;
                Integer UNKNOWN           = CameraMetadata.SYNC_MAX_LATENCY_UNKNOWN;

                if (value.equals(PER_FRAME_CONTROL)) {
                    valueString = "PER_FRAME_CONTROL";
                }
                else if (value.equals(UNKNOWN)){
                    valueString = "UNKNOWN";
                }
                else {
                    valueString = value.toString();
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @Override
                    public String formatValue(Integer value) {
                        return getValueString();
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