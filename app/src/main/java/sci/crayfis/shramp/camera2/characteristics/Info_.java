/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

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

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Info_ extends Hot_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Info_........................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Info_() { super(); }

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

        Log.e("                Info_", "reading Info_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;/////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                Integer level  = cameraCharacteristics.get(key);
                assert  level != null;

                value = null;
                valueString = null;
                switch (level) {
                    case (CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY): {
                        value = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
                        valueString = "LEGACY";
                        break;
                    }

                    case (CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED): {
                        value = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;
                        valueString = "LIMITED";
                        break;
                    }

                    case (CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL): {
                        value = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
                        valueString = "FULL";
                        break;
                    }

                    case (CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3): {
                        if (Build.VERSION.SDK_INT >= 24) {
                            value = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3;
                            valueString = "LEVEL_3";
                        }
                        break;
                    }

                    case (CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL): {
                        value = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL;
                        valueString = "EXTERNAL";
                        break;
                    }
                }
                assert value != null;

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
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
        //==========================================================================================
        {
            CameraCharacteristics.Key<String> key;
            ParameterFormatter<String> formatter;
            Parameter<String> property;

            String name;
            String value;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                key  = CameraCharacteristics.INFO_VERSION;//////////////////////////////////////////
                name = key.getName();

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    assert value != null;

                    formatter = new ParameterFormatter<String>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull String value) {
                            return value;
                        }
                    };
                    property = new Parameter<>(name, value, null, formatter);
                } else {
                    property = new Parameter<>(name);
                    property.setValueString("NOT SUPPORTED");
                }
                characteristicsMap.put(key, property);
            }
        }
        //==========================================================================================
    }
}