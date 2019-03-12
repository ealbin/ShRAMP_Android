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
abstract class Lens_ extends Jpeg_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Lens_........................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Lens_() { super(); }

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

        Log.e("                Lens_", "reading Lens_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float[]> formatter;
            Parameter<Float[]> property;

            String  name;
            Float[] value;
            String  units;

            if (Build.VERSION.SDK_INT >= 28) {
                key   = CameraCharacteristics.LENS_DISTORTION;////////////////////////////////////////
                name  = key.getName();
                units = "unitless correction coefficients";

                if (keychain.contains(key)) {
                    float[] coefficients  = cameraCharacteristics.get(key);
                    assert  coefficients != null;

                    value = (Float[]) ArrayToList.convert(coefficients).toArray(new Float[0]);
                    assert value != null;

                    formatter = new ParameterFormatter<Float[]>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Float[] value) {
                            String out = "( ";
                            int length = value.length;
                            for (int i = 0; i < length; i++) {
                                out += value[i];
                                if (i < length - 1) {
                                    out += ", ";
                                }
                            }
                            return out + " )";
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
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.LENS_FACING;//////////////////////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer FRONT    = CameraMetadata.LENS_FACING_FRONT;
                Integer BACK     = CameraMetadata.LENS_FACING_BACK;
                Integer EXTERNAL = null;
                if (Build.VERSION.SDK_INT >= 23 ) {
                    EXTERNAL = CameraMetadata.LENS_FACING_EXTERNAL;
                }

                if (value.equals(FRONT)) {
                    valueString = "FRONT";
                }
                else if (value.equals(BACK)) {
                    valueString = "BACK";
                }
                else {
                    valueString = "EXTERNAL";
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float> formatter;
            Parameter<Float> property;

            String name;
            Float  value;
            String units;

            key   = CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES;////////////////////////////
            name  = key.getName();
            units = "aperture f-number";

            if (keychain.contains(key)) {
                float[] apertures = cameraCharacteristics.get(key);
                assert apertures != null;

                Float smallest = null;
                for (Float val : apertures) {
                    if (smallest == null) {
                        smallest = val;
                        continue;
                    }
                    if (val < smallest) {
                        smallest = val;
                    }
                }
                assert smallest != null;
                value = smallest;

                formatter = new ParameterFormatter<Float>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float> formatter;
            Parameter<Float> property;

            String name;
            Float  value;
            String units;

            key   = CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES;/////////////////////
            name  = key.getName();
            units = "exposure value";

            if (keychain.contains(key)) {
                float[] densities = cameraCharacteristics.get(key);
                assert densities != null;

                Float biggest = null;
                for (Float val : densities) {
                    if (biggest == null) {
                        biggest = val;
                        continue;
                    }
                    if (val > biggest) {
                        biggest = val;
                    }
                }
                assert biggest != null;
                value = biggest;

                formatter = new ParameterFormatter<Float>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float> formatter;
            Parameter<Float> property;

            String name;
            Float  value;
            String units;

            key   = CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS;////////////////////////
            name  = key.getName();
            units = "exposure value";

            if (keychain.contains(key)) {
                float[] lengths = cameraCharacteristics.get(key);
                assert lengths != null;

                Float longest = null;
                for (Float val : lengths) {
                    if (longest == null) {
                        longest = val;
                        continue;
                    }
                    if (val > longest) {
                        longest = val;
                    }
                }
                assert longest != null;
                value = longest;

                formatter = new ParameterFormatter<Float>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION;////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                assert modes != null;
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF;
                Integer ON  = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON;

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF";
                }
                else {
                    value       =  ON;
                    valueString = "ON";
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
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
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION;/////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer UNCALIBRATED = CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED;
                Integer APPROXIMATE  = CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE;
                Integer CALIBRATED   = CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED;

                if (value.equals(UNCALIBRATED)) {
                    valueString = "UNCALIBRATED";
                }
                else if (value.equals(APPROXIMATE)) {
                    valueString = "APPROXIMATE";
                }
                else {
                    valueString = "CALIBRATED";
                }

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
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
        {
            CameraCharacteristics.Key<Float> key;
            ParameterFormatter<Float> formatter;
            Parameter<Float> property;

            String name;
            Float  value;
            String units;

            key   = CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE;////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {

                if (characteristicsMap.containsKey(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)) {
                    Integer calibration = (Integer) characteristicsMap.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION).getValue();
                    assert calibration != null;

                    if (!calibration.equals(CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED)){
                        units = "diopters";
                    }
                    else {
                        units = "uncalibrated diopters";
                    }
                }

                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Float>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<Float> key;
            ParameterFormatter<Float> formatter;
            Parameter<Float> property;

            String name;
            Float  value;
            String units;

            key   = CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE;/////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {

                if (characteristicsMap.containsKey(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)) {
                    Integer calibration = (Integer) characteristicsMap.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION).getValue();
                    assert calibration != null;

                    if (!calibration.equals(CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED)){
                        units = "diopters";
                    }
                    else {
                        units = "uncalibrated diopters";
                    }
                }

                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Float>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float[]> formatter;
            Parameter<Float[]> property;

            String  name;
            Float[] value;
            String  units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION;/////////////////////////////
                name  = key.getName();
                units = "pixels";

                if (keychain.contains(key)) {
                    float[] coefficients  = cameraCharacteristics.get(key);
                    assert  coefficients != null;

                    value = (Float[]) ArrayToList.convert(coefficients).toArray(new Float[0]);
                    assert value != null;

                    formatter = new ParameterFormatter<Float[]>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Float[] value) {
                            String out = "( ";
                            int length = value.length;
                            for (int i = 0; i < length; i++ ) {
                                out += value[i];
                                if (i < length - 1) {
                                    out += ", ";
                                }
                            }
                            return out + " )";
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
        {
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            if (Build.VERSION.SDK_INT >= 28) {
                key   = CameraCharacteristics.LENS_POSE_REFERENCE;//////////////////////////////////
                name  = key.getName();
                units = null;

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    assert value != null;

                    Integer PRIMARY_CAMERA = CameraMetadata.LENS_POSE_REFERENCE_PRIMARY_CAMERA;
                    Integer GYROSCOPE      = CameraMetadata.LENS_POSE_REFERENCE_GYROSCOPE;

                    if (value.equals(PRIMARY_CAMERA)) {
                        valueString = "PRIMARY_CAMERA";
                    } else {
                        valueString = "GYROSCOPE";
                    }

                    formatter = new ParameterFormatter<Integer>(valueString) {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString();
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float[]> formatter;
            Parameter<Float[]> property;

            String  name;
            Float[] value;
            String  units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.LENS_POSE_ROTATION;///////////////////////////////////
                name  = key.getName();
                units = "quaternion coefficients";

                if (keychain.contains(key)) {
                    float[] coefficients  = cameraCharacteristics.get(key);
                    assert  coefficients != null;

                    value = (Float[]) ArrayToList.convert(coefficients).toArray(new Float[0]);
                    assert value != null;

                    formatter = new ParameterFormatter<Float[]>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Float[] value) {
                            String out = "( ";
                            int length = value.length;
                            for (int i = 0; i < length; i++ ) {
                                out += value[i];
                                if (i < length - 1) {
                                    out += ", ";
                                }
                            }
                            return out + " )";
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float[]> formatter;
            Parameter<Float[]> property;

            String  name;
            Float[] value;
            String  units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.LENS_POSE_TRANSLATION;////////////////////////////////
                name  = key.getName();
                units = "meters";

                if (keychain.contains(key)) {
                    float[] coefficients  = cameraCharacteristics.get(key);
                    assert  coefficients != null;

                    value = (Float[]) ArrayToList.convert(coefficients).toArray(new Float[0]);
                    assert value != null;

                    formatter = new ParameterFormatter<Float[]>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Float[] value) {
                            String out = "( ";
                            int length = value.length;
                            for (int i = 0; i < length; i++ ) {
                                out += value[i];
                                if (i < length - 1) {
                                    out += ", ";
                                }
                            }
                            return out + " )";
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