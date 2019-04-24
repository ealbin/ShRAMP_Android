/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 24 April 2019
 */

package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * A specialized class for discovering camera abilities, the parameters searched for include:
 *    INFO_SUPPORTED_HARDWARE_LEVEL
 *    INFO_VERSION
 */
@TargetApi(21)
abstract class Info_ extends Hot_ {

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * Continue discovering abilities with specialized classes
     * @param cameraCharacteristics Encapsulation of camera abilities
     * @param characteristicsMap A mapping of characteristics names to their respective parameter options
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
                if ( level == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Hardware level cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Unknown hardware level");
                    MasterController.quitSafely();
                    return;
                }

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
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Version info cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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