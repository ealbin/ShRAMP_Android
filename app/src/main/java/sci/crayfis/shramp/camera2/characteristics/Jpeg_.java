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

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Jpeg_........................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Jpeg_() { super(); }

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

            key   = CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES;////////////////////////////
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
                        smallestArea = thisArea;
                    }
                }
                assert smallest != null;
                value = smallest;

                formatter = new ParameterFormatter<Size>() {
                    @Override
                    public String formatValue(Size value) {
                        return value.toString();
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