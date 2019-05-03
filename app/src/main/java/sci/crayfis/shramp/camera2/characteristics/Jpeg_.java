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
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * A specialized class for discovering camera abilities, the parameters searched for include:
 *    JEPG_AVAILABLE_THUMBNAIL_SIZES
 */
@TargetApi(21)
abstract class Jpeg_ extends Info_ {

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
                if (sizes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Thumbnail sizes cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                if (smallest == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "There must be a smallest thumbnail size");
                    MasterController.quitSafely();
                    return;
                }
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