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
abstract class Request_ extends Reprocess_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Request_.....................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Request_() { super(); }

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

        Log.e("             Request_", "reading Request_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer[]> formatter;
            Parameter<Integer[]> property;

            String    name;
            Integer[] value;
            String    valueString;
            String    units;

            key   = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;///////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                int[]  capabilities  = cameraCharacteristics.get(key);
                assert capabilities != null;
                List<Integer> available = ArrayToList.convert(capabilities);

                Integer BACKWARD_COMPATIBLE          = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE;
                Integer MANUAL_SENSOR                = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR;
                Integer MANUAL_POST_PROCESSING       = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING;
                Integer RAW                          = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW;
                Integer PRIVATE_REPROCESSING         = null;
                Integer READ_SENSOR_SETTINGS         = null;
                Integer BURST_CAPTURE                = null;
                Integer YUV_REPROCESSING             = null;
                Integer DEPTH_OUTPUT                 = null;
                Integer CONSTRAINED_HIGH_SPEED_VIDEO = null;
                Integer MOTION_TRACKING              = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING;
                Integer LOGICAL_MULTI_CAMERA         = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA;
                Integer MONOCHROME                   = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME;

                if (Build.VERSION.SDK_INT >= 22) {
                    READ_SENSOR_SETTINGS         = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS;
                    BURST_CAPTURE                = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE;
                }

                if (Build.VERSION.SDK_INT >= 23) {
                    PRIVATE_REPROCESSING         = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING;
                    YUV_REPROCESSING             = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING;
                    DEPTH_OUTPUT                 = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT;
                    CONSTRAINED_HIGH_SPEED_VIDEO = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO;
                }

                valueString = "( ";
                if (available.contains(BACKWARD_COMPATIBLE)) {
                    valueString += "BACKWARD_COMPATIBLE ";
                }
                if (available.contains(MANUAL_SENSOR)) {
                    valueString += "MANUAL_SENSOR ";
                }
                if (available.contains(MANUAL_POST_PROCESSING)) {
                    valueString += "MANUAL_POST_PROCESSING ";
                }
                if (available.contains(RAW)) {
                    valueString += "RAW ";
                }
                if (PRIVATE_REPROCESSING != null && available.contains(PRIVATE_REPROCESSING)) {
                    valueString += "PRIVATE_REPROCESSING ";
                }
                if (READ_SENSOR_SETTINGS != null && available.contains(READ_SENSOR_SETTINGS)) {
                    valueString += "READ_SENSOR_SETTINGS ";
                }
                if (BURST_CAPTURE != null && available.contains(BURST_CAPTURE)) {
                    valueString += "BURST_CAPTURE ";
                }
                if (YUV_REPROCESSING != null && available.contains(YUV_REPROCESSING)) {
                    valueString += "YUV_REPROCESSING ";
                }
                if (DEPTH_OUTPUT != null && available.contains(DEPTH_OUTPUT)) {
                    valueString += "DEPTH_OUTPUT ";
                }
                if (CONSTRAINED_HIGH_SPEED_VIDEO != null && available.contains(CONSTRAINED_HIGH_SPEED_VIDEO)) {
                    valueString += "CONSTRAINED_HIGH_SPEED_VIDEO ";
                }
                if (available.contains(MOTION_TRACKING)) {
                    valueString += "MOTION_TRACKING ";
                }
                if (available.contains(LOGICAL_MULTI_CAMERA)) {
                    valueString += "LOGICAL_MUTLI_CAMERA ";
                }
                if (available.contains(MONOCHROME)) {
                    valueString += "MONOCHROME ";
                }
                valueString += ")";

                value = (Integer[]) available.toArray(new Integer[0]);
                assert value != null;

                formatter = new ParameterFormatter<Integer[]>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer[] value) {
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
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.REQUEST_MAX_NUM_INPUT_STREAMS;////////////////////////
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
        }
        //==========================================================================================
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC;//////////////////////////////
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
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING;/////////////////////
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
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW;///////////////////////////////
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
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT;/////////////////////////////
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
        {
            CameraCharacteristics.Key<Byte> key;
            ParameterFormatter<Byte> formatter;
            Parameter<Byte> property;

            String name;
            Byte   value;
            String units;

            key   = CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH;///////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Byte>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Byte value) {
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