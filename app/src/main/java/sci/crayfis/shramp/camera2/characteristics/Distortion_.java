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
 * @updated: 20 April 2019
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
import sci.crayfis.shramp.util.ArrayToList;

/**
 * A specialized class for discovering camera abilities, the parameters searched for include:
 *    DISTORTION_CORRECTION_AVAILABLE_MODES
 */
@TargetApi(21)
abstract class Distortion_ extends Depth_ {

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

        Log.e("          Distortion_", "reading Distortion_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            if (Build.VERSION.SDK_INT >= 28) {
                key  = CameraCharacteristics.DISTORTION_CORRECTION_AVAILABLE_MODES;/////////////////
                name = key.getName();

                if (keychain.contains(key)) {
                    int[]  modes  = cameraCharacteristics.get(key);
                    if (modes == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Distortion modes cannot be null");
                        MasterController.quitSafely();
                        return;
                    }
                    List<Integer> options = ArrayToList.convert(modes);

                    Integer OFF          = CameraMetadata.DISTORTION_CORRECTION_MODE_OFF;
                    Integer FAST         = CameraMetadata.DISTORTION_CORRECTION_MODE_FAST;
                    //Integer HIGH_QUALITY = CameraMetadata.DISTORTION_CORRECTION_MODE_HIGH_QUALITY;

                    if (options.contains(OFF)) {
                        value       =  OFF;
                        valueString = "OFF (PREFERRED)";
                    }
                    else {
                        value       =  FAST;
                        valueString = "FAST (FALLBACK)";
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
        }
        //==========================================================================================
    }

}