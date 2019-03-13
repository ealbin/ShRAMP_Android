package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.BlackLevelPattern;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class Sensor_ extends Scaler_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Sensor_......................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected Sensor_() { super(); }

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

        Log.e("              Sensor_", "reading Sensor_ characteristics");
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

            key   = CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES;//////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                assert modes != null;
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF                     = CameraMetadata.SENSOR_TEST_PATTERN_MODE_OFF;
                Integer SOLID_COLOR             = CameraMetadata.SENSOR_TEST_PATTERN_MODE_SOLID_COLOR;
                Integer COLOR_BARS              = CameraMetadata.SENSOR_TEST_PATTERN_MODE_COLOR_BARS;
                Integer COLOR_BARS_FADE_TO_GRAY = CameraMetadata.SENSOR_TEST_PATTERN_MODE_COLOR_BARS_FADE_TO_GRAY;
                Integer PN9                     = CameraMetadata.SENSOR_TEST_PATTERN_MODE_PN9;
                Integer CUSTOM1                 = CameraMetadata.SENSOR_TEST_PATTERN_MODE_CUSTOM1;

                value       =  OFF;
                valueString = "OFF (PREFERRED)";

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
            CameraCharacteristics.Key<BlackLevelPattern> key;
            ParameterFormatter<BlackLevelPattern> formatter;
            Parameter<BlackLevelPattern> property;

            String            name;
            BlackLevelPattern value;
            String            units;

            key   = CameraCharacteristics.SENSOR_BLACK_LEVEL_PATTERN;///////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<BlackLevelPattern>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull BlackLevelPattern value) {
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
            CameraCharacteristics.Key<ColorSpaceTransform> key;
            ParameterFormatter<ColorSpaceTransform> formatter;
            Parameter<ColorSpaceTransform> property;

            String              name;
            ColorSpaceTransform value;
            String              units;

            key   = CameraCharacteristics.SENSOR_CALIBRATION_TRANSFORM1;////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<ColorSpaceTransform>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
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
            CameraCharacteristics.Key<ColorSpaceTransform> key;
            ParameterFormatter<ColorSpaceTransform> formatter;
            Parameter<ColorSpaceTransform> property;

            String              name;
            ColorSpaceTransform value;
            String              units;

            key   = CameraCharacteristics.SENSOR_CALIBRATION_TRANSFORM2;////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<ColorSpaceTransform>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
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
            CameraCharacteristics.Key<ColorSpaceTransform> key;
            ParameterFormatter<ColorSpaceTransform> formatter;
            Parameter<ColorSpaceTransform> property;

            String              name;
            ColorSpaceTransform value;
            String              units;

            key   = CameraCharacteristics.SENSOR_COLOR_TRANSFORM1;//////////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<ColorSpaceTransform>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
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
            CameraCharacteristics.Key<ColorSpaceTransform> key;
            ParameterFormatter<ColorSpaceTransform> formatter;
            Parameter<ColorSpaceTransform> property;

            String              name;
            ColorSpaceTransform value;
            String              units;

            key   = CameraCharacteristics.SENSOR_COLOR_TRANSFORM2;//////////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<ColorSpaceTransform>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
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
            CameraCharacteristics.Key<ColorSpaceTransform> key;
            ParameterFormatter<ColorSpaceTransform> formatter;
            Parameter<ColorSpaceTransform> property;

            String              name;
            ColorSpaceTransform value;
            String              units;

            key   = CameraCharacteristics.SENSOR_FORWARD_MATRIX1;///////////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<ColorSpaceTransform>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
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
            CameraCharacteristics.Key<ColorSpaceTransform> key;
            ParameterFormatter<ColorSpaceTransform> formatter;
            Parameter<ColorSpaceTransform> property;

            String              name;
            ColorSpaceTransform value;
            String              units;

            key   = CameraCharacteristics.SENSOR_FORWARD_MATRIX2;///////////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<ColorSpaceTransform>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull ColorSpaceTransform value) {
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
            CameraCharacteristics.Key<Rect> key;
            ParameterFormatter<Rect> formatter;
            Parameter<Rect> property;

            String name;
            Rect   value;
            String units;

            key   = CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE;////////////////////////////
            name  = key.getName();
            units = "pixel coordinates";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Rect>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Rect value) {
                        return value.flattenToString();
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

            key   = CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT;/////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer RGGB = CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB;
                Integer GRBG = CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG;
                Integer GBRG = CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG;
                Integer BGGR = CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR;
                Integer RGB  = CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB;

                valueString = null;
                if (value.equals(RGGB)) {
                    valueString = "RGGB";
                }
                if (value.equals(GRBG)) {
                    valueString = "GRBG";
                }
                if (value.equals(GBRG)) {
                    valueString = "GBRG";
                }
                if (value.equals(BGGR)) {
                    valueString = "BGGR";
                }
                if (value.equals(RGB)) {
                    valueString = "RGB";
                }
                assert valueString != null;

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
            CameraCharacteristics.Key<Range<Long>> key;
            ParameterFormatter<Range<Long>> formatter;
            Parameter<Range<Long>> property;

            String      name;
            Range<Long> value;
            String      units;

            key   = CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE;//////////////////////////
            name  = key.getName();
            units = "nanoseconds";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Range<Long>>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Range<Long> value) {
                        DecimalFormat nanosFormatter;
                        nanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                        return "( " + nanosFormatter.format(value.getLower()) + " to "
                                    + nanosFormatter.format(value.getUpper()) + " )";
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
            CameraCharacteristics.Key<Boolean> key;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> property;

            String name;
            Boolean value;
            String units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED;/////////////////////
                name  = key.getName();
                units = null;

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
            CameraCharacteristics.Key<Long> key;
            ParameterFormatter<Long> formatter;
            Parameter<Long> property;

            String name;
            Long   value;
            String units;

            key   = CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION;///////////////////////////
            name  = key.getName();
            units = "nanoseconds";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Long>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Long value) {
                        DecimalFormat nanosFormatter;
                        nanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                        return nanosFormatter.format(value);
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
            CameraCharacteristics.Key<SizeF> key;
            ParameterFormatter<SizeF> formatter;
            Parameter<SizeF> property;

            String name;
            SizeF  value;
            String units;

            key   = CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE;////////////////////////////////
            name  = key.getName();
            units = "millimeters";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<SizeF>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull SizeF value) {
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
            CameraCharacteristics.Key<Size> key;
            ParameterFormatter<Size> formatter;
            Parameter<Size> property;

            String name;
            Size   value;
            String units;

            key   = CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE;/////////////////////////////
            name  = key.getName();
            units = "pixels";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Size>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Size value) {
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
            CameraCharacteristics.Key<Rect> key;
            ParameterFormatter<Rect> formatter;
            Parameter<Rect> property;

            String name;
            Rect value;
            String units;

            if (Build.VERSION.SDK_INT >= 23) {
                key   = CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE;/////////
                name  = key.getName();
                units = "pixel coordinates";

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    assert value != null;

                    formatter = new ParameterFormatter<Rect>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Rect value) {
                            return value.flattenToString();
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
            CameraCharacteristics.Key<Range<Integer>> key;
            ParameterFormatter<Range<Integer>> formatter;
            Parameter<Range<Integer>> property;

            String         name;
            Range<Integer> value;
            String         units;

            key   = CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE;////////////////////////////
            name  = key.getName();
            units = "ISO";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                formatter = new ParameterFormatter<Range<Integer>>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Range<Integer> value) {
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
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;
            String  units;

            key   = CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE;/////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer UNKNOWN  = CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN;
                Integer REALTIME = CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME;

                if (value.equals(UNKNOWN)) {
                    valueString = "UNKNOWN";
                }
                else {
                    valueString = "REALTIME";
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
            String  units;

            key   = CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL;//////////////////////////////////
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

            key   = CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY;////////////////////////////
            name  = key.getName();
            units = "ISO";

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
            }
            else {
                property = new Parameter<>(name);
                property.setValueString("NOT SUPPORTED");
            }
            characteristicsMap.put(key, property);
        }
        //==========================================================================================
        {
            CameraCharacteristics.Key<Rect[]> key;
            ParameterFormatter<Rect[]> formatter;
            Parameter<Rect[]> property;

            String name;
            Rect[] value;
            String units;

            if (Build.VERSION.SDK_INT >= 24) {
                key   = CameraCharacteristics.SENSOR_OPTICAL_BLACK_REGIONS;/////////////////////////
                name  = key.getName();
                units = "pixel coordinates";

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    assert value != null;

                    formatter = new ParameterFormatter<Rect[]>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Rect[] value) {
                            String out = "( ";
                            for (Rect rect : value) {
                                out += rect.flattenToString() + " ";
                            }
                            return out + ")";
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
            String  units;

            key   = CameraCharacteristics.SENSOR_ORIENTATION;///////////////////////////////////////
            name  = key.getName();
            units = "degrees clockwise";

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

            key   = CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1;/////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer DAYLIGHT               = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_DAYLIGHT;
                Integer FLUORESCENT            = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_FLUORESCENT;
                Integer TUNGSTEN               = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_TUNGSTEN;
                Integer FLASH                  = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_FLASH;
                Integer FINE_WEATHER           = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_FINE_WEATHER;
                Integer CLOUDY_WEATHER         = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_CLOUDY_WEATHER;
                Integer SHADE                  = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_SHADE;
                Integer DAYLIGHT_FLUORESCENT   = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_DAYLIGHT_FLUORESCENT;
                Integer DAY_WHITE_FLUORESCENT  = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_DAY_WHITE_FLUORESCENT;
                Integer COOL_WHITE_FLUORESCENT = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_COOL_WHITE_FLUORESCENT;
                Integer WHITE_FLUORESCENT      = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_WHITE_FLUORESCENT;
                Integer STANDARD_A             = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_A;
                Integer STANDARD_B             = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_B;
                Integer STANDARD_C             = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_C;
                Integer D55                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D55;
                Integer D65                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D65;
                Integer D75                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D75;
                Integer D50                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D50;
                Integer ISO_STUDIO_TUNGSTEN    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_ISO_STUDIO_TUNGSTEN;

                valueString = null;
                if (value.equals(DAYLIGHT)) {
                    valueString = "DAYLIGHT";
                }
                if (value.equals(FLUORESCENT)) {
                    valueString = "FLUORESCENT";
                }
                if (value.equals(TUNGSTEN)) {
                    valueString = "TUNGSTEN";
                }
                if (value.equals(FLASH)) {
                    valueString = "FLASH";
                }
                if (value.equals(FINE_WEATHER)) {
                    valueString = "FINE_WEATHER";
                }
                if (value.equals(CLOUDY_WEATHER)) {
                    valueString = "CLOUDY_WEATHER";
                }
                if (value.equals(SHADE)) {
                    valueString = "SHADE";
                }
                if (value.equals(DAYLIGHT_FLUORESCENT)) {
                    valueString = "DAYLIGHT_FLUORESCENT";
                }
                if (value.equals(DAY_WHITE_FLUORESCENT)) {
                    valueString = "DAY_WHITE_FLUORESCENT";
                }
                if (value.equals(COOL_WHITE_FLUORESCENT)) {
                    valueString = "COOL_WHITE_FLUORESCENT";
                }
                if (value.equals(WHITE_FLUORESCENT)) {
                    valueString = "WHITE_FLUORESCENT";
                }
                if (value.equals(STANDARD_A)) {
                    valueString = "STANDARD_A";
                }
                if (value.equals(STANDARD_B)) {
                    valueString = "STANDARD_B";
                }
                if (value.equals(STANDARD_C)) {
                    valueString = "STANDARD_C";
                }
                if (value.equals(D55)) {
                    valueString = "D55";
                }
                if (value.equals(D65)) {
                    valueString = "D65";
                }
                if (value.equals(D75)) {
                    valueString = "D75";
                }
                if (value.equals(D50)) {
                    valueString = "D50";
                }
                if (value.equals(ISO_STUDIO_TUNGSTEN)) {
                    valueString = "ISO_STUDIO_TUNGSTEN";
                }
                assert valueString != null;

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
            CameraCharacteristics.Key<Byte> key;
            ParameterFormatter<Byte> formatter;
            Parameter<Byte> property;

            String name;
            Byte   value;
            String valueString;
            String units;

            key   = CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT2;/////////////////////////////
            name  = key.getName();
            units = null;

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                assert value != null;

                Integer DAYLIGHT               = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_DAYLIGHT;
                Integer FLUORESCENT            = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_FLUORESCENT;
                Integer TUNGSTEN               = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_TUNGSTEN;
                Integer FLASH                  = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_FLASH;
                Integer FINE_WEATHER           = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_FINE_WEATHER;
                Integer CLOUDY_WEATHER         = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_CLOUDY_WEATHER;
                Integer SHADE                  = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_SHADE;
                Integer DAYLIGHT_FLUORESCENT   = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_DAYLIGHT_FLUORESCENT;
                Integer DAY_WHITE_FLUORESCENT  = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_DAY_WHITE_FLUORESCENT;
                Integer COOL_WHITE_FLUORESCENT = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_COOL_WHITE_FLUORESCENT;
                Integer WHITE_FLUORESCENT      = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_WHITE_FLUORESCENT;
                Integer STANDARD_A             = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_A;
                Integer STANDARD_B             = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_B;
                Integer STANDARD_C             = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_C;
                Integer D55                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D55;
                Integer D65                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D65;
                Integer D75                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D75;
                Integer D50                    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D50;
                Integer ISO_STUDIO_TUNGSTEN    = CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_ISO_STUDIO_TUNGSTEN;

                valueString = null;
                Integer valueInteger = value.intValue();
                if (valueInteger.equals(DAYLIGHT)) {
                    valueString = "DAYLIGHT";
                }
                if (valueInteger.equals(FLUORESCENT)) {
                    valueString = "FLUORESCENT";
                }
                if (valueInteger.equals(TUNGSTEN)) {
                    valueString = "TUNGSTEN";
                }
                if (valueInteger.equals(FLASH)) {
                    valueString = "FLASH";
                }
                if (valueInteger.equals(FINE_WEATHER)) {
                    valueString = "FINE_WEATHER";
                }
                if (valueInteger.equals(CLOUDY_WEATHER)) {
                    valueString = "CLOUDY_WEATHER";
                }
                if (valueInteger.equals(SHADE)) {
                    valueString = "SHADE";
                }
                if (valueInteger.equals(DAYLIGHT_FLUORESCENT)) {
                    valueString = "DAYLIGHT_FLUORESCENT";
                }
                if (valueInteger.equals(DAY_WHITE_FLUORESCENT)) {
                    valueString = "DAY_WHITE_FLUORESCENT";
                }
                if (valueInteger.equals(COOL_WHITE_FLUORESCENT)) {
                    valueString = "COOL_WHITE_FLUORESCENT";
                }
                if (valueInteger.equals(WHITE_FLUORESCENT)) {
                    valueString = "WHITE_FLUORESCENT";
                }
                if (valueInteger.equals(STANDARD_A)) {
                    valueString = "STANDARD_A";
                }
                if (valueInteger.equals(STANDARD_B)) {
                    valueString = "STANDARD_B";
                }
                if (valueInteger.equals(STANDARD_C)) {
                    valueString = "STANDARD_C";
                }
                if (valueInteger.equals(D55)) {
                    valueString = "D55";
                }
                if (valueInteger.equals(D65)) {
                    valueString = "D65";
                }
                if (valueInteger.equals(D75)) {
                    valueString = "D75";
                }
                if (valueInteger.equals(D50)) {
                    valueString = "D50";
                }
                if (valueInteger.equals(ISO_STUDIO_TUNGSTEN)) {
                    valueString = "ISO_STUDIO_TUNGSTEN";
                }
                assert valueString != null;

                formatter = new ParameterFormatter<Byte>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Byte value) {
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
    }
}