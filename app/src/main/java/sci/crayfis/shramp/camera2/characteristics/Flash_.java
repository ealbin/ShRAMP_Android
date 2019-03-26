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
abstract class Flash_ extends Edge_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Flash_.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Flash_() { super(); }

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

        Log.e("               Flash_", "reading Flash_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Boolean> key;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> property;

            String  name;
            Boolean value;
            key  = CameraCharacteristics.FLASH_INFO_AVAILABLE;//////////////////////////////////////
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
        //==========================================================================================
    }
}