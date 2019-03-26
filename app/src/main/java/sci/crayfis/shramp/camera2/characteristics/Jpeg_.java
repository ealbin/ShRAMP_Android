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
import android.util.Size;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Jpeg_ extends Info_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Jpeg_........................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Jpeg_() { super(); }

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

        Log.e("                Jpeg_", "reading Jpeg_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<Size[]> key;
            ParameterFormatter<Size> formatter;
            Parameter<Size> property;

            String  name;
            Size    value;
            String  units;

            key   = CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES;///////////////////////////
            name  = key.getName();
            units = "pixels";

            if (keychain.contains(key)) {
                Size[] sizes  = cameraCharacteristics.get(key);
                assert sizes != null;

                Size smallest = null;
                for (Size size : sizes) {
                    if (smallest == null) {
                        smallest = size;
                        continue;
                    }
                    long thisArea     =     size.getWidth() *     size.getHeight();
                    long smallestArea = smallest.getWidth() * smallest.getHeight();
                    if (thisArea < smallestArea) {
                        smallest = size;
                    }
                }
                assert smallest != null;
                value = smallest;

                formatter = new ParameterFormatter<Size>("smallest: ") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Size value) {
                        return getValueString() + value.toString();
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