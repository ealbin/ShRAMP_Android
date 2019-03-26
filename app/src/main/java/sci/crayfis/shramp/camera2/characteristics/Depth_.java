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
abstract class Depth_ extends Control_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Depth_.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Depth_() { super(); }

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     * @param cameraCharacteristics bla
     * @param characteristicsMap bla
     */
    @Override
    protected void read(@NonNull CameraCharacteristics cameraCharacteristics,
                        @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap) {
        super.read(cameraCharacteristics, characteristicsMap);

        Log.e("               Depth_", "reading Depth_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Boolean> key;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> property;

            String  name;
            Boolean value;

            if (Build.VERSION.SDK_INT >= 23) {
                key  = CameraCharacteristics.DEPTH_DEPTH_IS_EXCLUSIVE;//////////////////////////////
                name = key.getName();

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
                    property = new Parameter<>(name, value, null, formatter);
                }
                else {
                    property = new Parameter<>(name);
                    property.setValueString("NOT SUPPORTED");
                }
                characteristicsMap.put(key, property);
            }
        }
        //==========================================================================================
    }
}