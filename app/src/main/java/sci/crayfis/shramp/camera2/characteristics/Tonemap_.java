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
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * A specialized class for discovering camera abilities, the parameters searched for include:
 *    TONEMAP_AVAILABLE_TONE_MAP_MODES
 *    TONEMAP_MAX_CURVE_POINTS
 */
@TargetApi(21)
abstract class Tonemap_ extends Sync_ {

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

        Log.e("            Tonemap_", "reading Tonemap_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES;//////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Tone map modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer CONTRAST_CURVE = CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE;
                Integer FAST           = CameraMetadata.TONEMAP_MODE_FAST;
                //Integer HIGH_QUALITY   = CameraMetadata.TONEMAP_MODE_HIGH_QUALITY;
                //Integer GAMMA_VALUE    = null;
                //Integer PRESET_CURVE   = null;
                //if (Build.VERSION.SDK_INT >= 23) {
                //    GAMMA_VALUE  = CameraMetadata.TONEMAP_MODE_GAMMA_VALUE;
                //    PRESET_CURVE = CameraMetadata.TONEMAP_MODE_PRESET_CURVE;
                //}

                if (options.contains(CONTRAST_CURVE)) {
                    value = CONTRAST_CURVE;
                    valueString = "CONTRAST_CURVE (PREFERRED)";
                }
                else {
                    value = FAST;
                    valueString = "FAST (FALLBACK)";
                }

                if (GlobalSettings.FORCE_WORST_CONFIGURATION) {
                    value = FAST;
                    valueString = "FAST (WORST CONFIGURATION)";
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
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS;/////////////////////////////////
            name  = key.getName();
            units = "curve points";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Max curve points cannot be null");
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
        //==========================================================================================
    }

}