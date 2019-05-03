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
 * @updated: 3 May 2019
 */

package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
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
 *    REPROCESS_MAX_CAPTURE_STALL
 */
@TargetApi(21)
abstract class Reprocess_ extends Noise_ {

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
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Max capture stall cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    formatter = new ParameterFormatter<Integer>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return value.toString();
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
        }
        //==========================================================================================
    }

}