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
 * @updated: 24 April 2019
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
 *    LENS_DISTORTION
 *    LENS_FACING
 *    LENS_INFO_AVAILABLE_APERTURES
 *    LENS_INFO_AVAILABLE_FILTER_DENSITIES
 *    LENS_INFO_AVAILABLE_FOCAL_LENGTHS
 *    LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION
 *    LENS_INFO_FOCUS_DISTANCE_CALIBRATION
 *    LENS_INFO_HYPERFOCAL_DISTANCE
 *    LENS_INFO_MINIMUM_FOCUS_DISTANCE
 *    LENS_INTRINSIC_CALIBRATION
 *    LENS_POSE_REFERENCE
 *    LENS_POSE_ROTATION
 *    LENS_POSE_TRANSLATION
 */
@SuppressWarnings("unchecked")
@TargetApi(21)
abstract class Lens_ extends Jpeg_ {

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
                key   = CameraCharacteristics.LENS_DISTORTION;//////////////////////////////////////
                name  = key.getName();
                units = "unitless correction coefficients";

                if (keychain.contains(key)) {
                    float[] coefficients  = cameraCharacteristics.get(key);
                    if ( coefficients == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens distortion cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    value = (Float[]) ArrayToList.convert(coefficients).toArray(new Float[0]);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens distortion coefficients cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            key  = CameraCharacteristics.LENS_FACING;///////////////////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens facing cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                if (apertures == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens apertures cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                if (smallest == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "There must be a smallest aperture");
                    MasterController.quitSafely();
                    return;
                }
                value = smallest;

                formatter = new ParameterFormatter<Float>("smallest: ") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
                if (densities == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Filter densities cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                if (biggest == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "There must be a biggest density");
                    MasterController.quitSafely();
                    return;
                }
                value = biggest;

                formatter = new ParameterFormatter<Float>("biggest: ") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<float[]> key;
            ParameterFormatter<Float> formatter;
            Parameter<Float> property;

            String name;
            Float  value;
            String units;

            key   = CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS;////////////////////////
            name  = key.getName();
            units = "millimeters";

            if (keychain.contains(key)) {
                float[] lengths = cameraCharacteristics.get(key);
                if (lengths == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens focal lengths cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                if (longest == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Longest focal length must exist");
                    MasterController.quitSafely();
                    return;
                }
                value = longest;

                formatter = new ParameterFormatter<Float>("longest: ") {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Float value) {
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
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION;/////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Optical stabilization cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF;
                Integer ON  = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON;

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF (PREFERRED)";
                }
                else {
                    value       =  ON;
                    valueString = "ON (FALLBACK)";
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
            String  valueString;

            key  = CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION;//////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens calibration cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                    Parameter<Integer> calibration;
                    calibration = characteristicsMap.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
                    if (calibration == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens hyperfocal distances cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    Integer calValue = calibration.getValue();
                    if (calValue == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens calibration cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    if (!calValue.equals(CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED)){
                        units = "diopters";
                    }
                    else {
                        units = "uncalibrated diopters";
                    }
                }

                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens hyperfocal distances cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                    Parameter<Integer> calibration;
                    calibration = characteristicsMap.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
                    if (calibration == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens calibration cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    Integer calValue = calibration.getValue();
                    if (calValue == null) {
                        units = "uncalibrated diopters";
                    }
                    else if (!calValue.equals(CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED)){
                        units = "diopters";
                    }
                    else {
                        units = "uncalibrated diopters";
                    }
                }

                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Lens minimum focus cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
                key   = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION;///////////////////////////
                name  = key.getName();
                units = "pixels";

                if (keychain.contains(key)) {
                    float[] coefficients  = cameraCharacteristics.get(key);
                    if ( coefficients == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens calibration cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    value = ArrayToList.convert(coefficients).toArray(new Float[0]);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens coefficients cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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

            if (Build.VERSION.SDK_INT >= 28) {
                key  = CameraCharacteristics.LENS_POSE_REFERENCE;///////////////////////////////////
                name = key.getName();

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens reference cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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
                    if ( coefficients == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens rotation cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    value = ArrayToList.convert(coefficients).toArray(new Float[0]);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens coefficients cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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
                    if ( coefficients == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens translation cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    value = ArrayToList.convert(coefficients).toArray(new Float[0]);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Lens coefficients cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

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