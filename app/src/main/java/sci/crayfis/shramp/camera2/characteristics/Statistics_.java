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
import sci.crayfis.shramp.util.ArrayToList;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Statistics_ extends Shading_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Statistics_..................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Statistics_() { super(); }

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

        Log.e("          Statistics_", "reading Statistics_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES;//////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                assert modes != null;
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF    = CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF;
                Integer SIMPLE = CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE;
                Integer FULL   = CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL;

                value       =  OFF;
                valueString = "OFF";

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                property = new Parameter<>(name, value, units, formatter);
            } else {
                property = new Parameter<>(name);
            }
            characteristicsMap.put(key, property);
        }
        //==========================================================================================
        {
            CameraCharacteristics.Key<boolean[]> key;
            ParameterFormatter<Boolean[]> formatter;
            Parameter<Boolean[]> property;

            String    name;
            Boolean[] value;
            String    units;

            key   = CameraCharacteristics.STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES;////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                boolean[] modes  = cameraCharacteristics.get(key);
                assert    modes != null;

                value = (Boolean[]) ArrayToList.convert(modes).toArray(new Boolean[0]);
                assert value != null;

                formatter = new ParameterFormatter<Boolean[]>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Boolean[] value) {
                        String out = "( ";
                        int length = value.length;
                        for (int i = 0; i < length; i++) {
                            if (value[i]) {
                                out += "YES";
                            }
                            else {
                                out += "NO";
                            }
                            if (i < length - 1) {
                                out += ", ";
                            }
                        }
                        return out + " )";
                    }
                };
                property = new Parameter<>(name, value, units, formatter);
            } else {
                property = new Parameter<>(name);
            }
            characteristicsMap.put(key, property);
        }
        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String name;
            Integer value;
            String valueString;
            String units;

            if (Build.VERSION.SDK_INT >= 23) {
                key = CameraCharacteristics.STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES;///////
                name = key.getName();
                units = null;

                if (keychain.contains(key)) {
                    int[] modes = cameraCharacteristics.get(key);
                    assert modes != null;
                    List<Integer> options = ArrayToList.convert(modes);

                    Integer OFF = CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_OFF;
                    Integer ON  = CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_ON;

                    value = OFF;
                    valueString = "OFF";

                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
                        }
                    };
                    property = new Parameter<>(name, value, units, formatter);
                } else {
                    property = new Parameter<>(name);
                }
                characteristicsMap.put(key, property);
            }
        }
        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String name;
            Integer value;
            String valueString;
            String units;

            if (Build.VERSION.SDK_INT >= 28) {
                key = CameraCharacteristics.STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES;///////////////
                name = key.getName();
                units = null;

                if (keychain.contains(key)) {
                    int[] modes = cameraCharacteristics.get(key);
                    assert modes != null;
                    List<Integer> options = ArrayToList.convert(modes);

                    Integer OFF = CameraMetadata.STATISTICS_OIS_DATA_MODE_OFF;
                    Integer ON  = CameraMetadata.STATISTICS_OIS_DATA_MODE_ON;

                    value = OFF;
                    valueString = "OFF";

                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
                        }
                    };
                    property = new Parameter<>(name, value, units, formatter);
                } else {
                    property = new Parameter<>(name);
                }
                characteristicsMap.put(key, property);
            }
        }
        //==========================================================================================
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT;///////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Integer>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
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