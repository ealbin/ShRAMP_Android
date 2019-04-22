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
 * @updated: 20 April 2019
 */

package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Rational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;
import sci.crayfis.shramp.util.ArrayToList;

/**
 * A specialized class for discovering camera abilities, the parameters searched for include:
 *    CONTROL_AE_AVAILABLE_ANTIBANDING_MODES
 *    CONTROL_AE_AVAILABLE_MODES
 *    CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES
 *    CONTROL_AE_COMPENSATION_RANGE
 *    CONTROL_AE_COMPENSATION_STEP
 *    CONTROL_AE_LOCK_AVAILABLE
 *    CONTROL_AF_AVAILABLE_MODES
 *    CONTROL_AVAILABLE_EFFECTS
 *    CONTROL_AVAILABLE_MODES
 *    CONTROL_AVAILABLE_SCENE_MODES
 *    CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
 *    CONTROL_AWB_AVAILABLE_MODES
 *    CONTROL_AWB_LOCK_AVAILABLE
 *    CONTROL_MAX_REGIONS_AE
 *    CONTROL_MAX_REGIONS_AF
 *    CONTROL_MAX_REGIONS_AWB
 */
@TargetApi(21)
@SuppressWarnings("unchecked")
abstract class Control_ extends Color_ {

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

        Log.e("             Control_", "reading Control_ characteristics");
        List<CameraCharacteristics.Key<?>> keychain = cameraCharacteristics.getKeys();

        //==========================================================================================
        {
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES;////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE antibanding modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF   = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF;
                //Integer _50HZ = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_50HZ;
                Integer _60HZ = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ;
                Integer AUTO  = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO;

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF (PREFERRED)";
                }
                else if (options.contains(AUTO)) {
                    value       =  AUTO;
                    valueString = "AUTO (FALLBACK)";
                }
                else {
                    value       = _60HZ;
                    valueString = "60HZ (LAST CHOICE)";
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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF                  = CameraMetadata.CONTROL_AE_MODE_OFF;
                Integer ON                   = CameraMetadata.CONTROL_AE_MODE_ON;
                //Integer ON_AUTO_FLASH        = CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH;
                //Integer ON_ALWAYS_FLASH      = CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
                //Integer ON_AUTO_FLASH_REDEYE = CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE;
                //Integer ON_EXTERNAL_FLASH    = CameraMetadata.CONTROL_AE_MODE_ON_EXTERNAL_FLASH;

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
            CameraCharacteristics.Key<Range<Integer>[]> key;
            ParameterFormatter<Range<Integer>[]> formatter;
            Parameter<Range<Integer>[]> property;

            String name;
            Range<Integer>[] value;
            String units;

            key   = CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;///////////////////
            name  = key.getName();
            units = "frames per second";

            if (keychain.contains(key)) {

                // Sort by upper FPS limit
                class SortByUpper implements Comparator<Range<Integer>> {
                    public int compare( Range<Integer> a, Range<Integer> b) {
                        return a.getUpper() - b.getUpper();
                    }
                }

                Range<Integer>[] options = cameraCharacteristics.get(key);
                if (options == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "FPS range cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                List<Range<Integer>> fpsRanges = ArrayToList.convert(options);
                Collections.sort(fpsRanges, new SortByUpper());

                List<Range<Integer>> keep = new ArrayList<>();
                for (Range<Integer> range : fpsRanges) {
                    if (range.getUpper() - range.getLower() <= GlobalSettings.MAX_FPS_DIFF
                        && range.getUpper() <= GlobalSettings.MAX_FPS) {
                        keep.add(range);
                    }
                }

                // TODO: figure out how to do toArray(new Range<Integer>[])
                value = (Range<Integer>[]) keep.toArray(new Range[0]);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "FPS range cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                formatter = new ParameterFormatter<Range<Integer>[]>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Range<Integer>[] value) {
                        String out = "{ ";
                        for (Range<Integer> val : value) {
                            out += val.toString() + " ";
                        }
                        return out + "}";
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
            CameraCharacteristics.Key<Range<Integer>> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            key   = CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE;////////////////////////////
            name  = key.getName();
            units = "compensation steps";

            if (keychain.contains(key)) {
                Range<Integer> range = cameraCharacteristics.get(key);
                if (range == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE compensation range cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                value = range.getUpper();

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
            CameraCharacteristics.Key<Rational> key;
            ParameterFormatter<Rational> formatter;
            Parameter<Rational> property;

            String   name;
            Rational value;
            String   units;

            key   = CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP;/////////////////////////////
            name  = key.getName();
            units = "exposure value";

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE compensation step cannot be null");
                    MasterController.quitSafely();
                    return;
                }

                formatter = new ParameterFormatter<Rational>() {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Rational value) {
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
            CameraCharacteristics.Key<Boolean> key;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> property;

            String  name;
            Boolean value;

            if (Build.VERSION.SDK_INT >= 23) {
                key  = CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE;/////////////////////////////
                name = key.getName();

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AE lock cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            if (value) {
                                return "YES (PREFERRED)";
                            }
                            return "NO (FALLBACK)";
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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AF modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF                = CameraMetadata.CONTROL_AF_MODE_OFF;
                Integer AUTO               = CameraMetadata.CONTROL_AF_MODE_AUTO;
                //Integer MACRO              = CameraMetadata.CONTROL_AF_MODE_MACRO;
                //Integer CONTINUOUS_VIDEO   = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
                //Integer CONTINUOUS_PICTURE = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                //Integer EDOF               = CameraMetadata.CONTROL_AF_MODE_EDOF;

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF (PREFERRED)";
                }
                else {
                    value       =  AUTO;
                    valueString = "AUTO (FALLBACK)";
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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS;/////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Effects cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF        = CameraMetadata.CONTROL_EFFECT_MODE_OFF;
                //Integer MONO       = CameraMetadata.CONTROL_EFFECT_MODE_MONO;
                //Integer NEGATIVE   = CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE;
                //Integer SOLARIZE   = CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE;
                //Integer SEPIA      = CameraMetadata.CONTROL_EFFECT_MODE_SEPIA;
                //Integer POSTERIZE  = CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE;
                //Integer WHITEBOARD = CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD;
                //Integer BLACKBOARD = CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD;
                //Integer AQUA       = CameraMetadata.CONTROL_EFFECT_MODE_AQUA;

                value       =  OFF;
                valueString = "OFF (PREFERRED)";

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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            if (Build.VERSION.SDK_INT >= 23) {
                key  = CameraCharacteristics.CONTROL_AVAILABLE_MODES;///////////////////////////////
                name = key.getName();

                if (keychain.contains(key)) {
                    int[] modes = cameraCharacteristics.get(key);
                    if (modes == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Available modes cannot be null");
                        MasterController.quitSafely();
                        return;
                    }
                    List<Integer> options = ArrayToList.convert(modes);

                    Integer OFF            = CameraMetadata.CONTROL_MODE_OFF;
                    Integer AUTO           = CameraMetadata.CONTROL_MODE_AUTO;
                    //Integer USE_SCENE_MODE = CameraMetadata.CONTROL_MODE_USE_SCENE_MODE;
                    //Integer OFF_KEEP_STATE = CameraMetadata.CONTROL_MODE_OFF_KEEP_STATE;

                    if (options.contains(OFF)) {
                        value = OFF;
                        valueString = "OFF (PREFERRED)";
                    }
                    else {
                        value = AUTO;
                        valueString = "AUTO (FALLBACK)";
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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES;/////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Scene modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer DISABLED         = CameraMetadata.CONTROL_SCENE_MODE_DISABLED;
                //Integer FACE_PRIORITY    = CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY;
                //Integer ACTION           = CameraMetadata.CONTROL_SCENE_MODE_ACTION;
                //Integer PORTRAIT         = CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT;
                //Integer LANDSCAPE        = CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE;
                //Integer NIGHT            = CameraMetadata.CONTROL_SCENE_MODE_NIGHT;
                //Integer NIGHT_PORTRAIT   = CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT;
                //Integer THEATRE          = CameraMetadata.CONTROL_SCENE_MODE_THEATRE;
                //Integer BEACH            = CameraMetadata.CONTROL_SCENE_MODE_BEACH;
                //Integer SNOW             = CameraMetadata.CONTROL_SCENE_MODE_SNOW;
                //Integer SUNSET           = CameraMetadata.CONTROL_SCENE_MODE_SUNSET;
                //Integer STEADYPHOTO      = CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO;
                //Integer FIREWORKS        = CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS;
                //Integer SPORTS           = CameraMetadata.CONTROL_SCENE_MODE_SPORTS;
                //Integer PARTY            = CameraMetadata.CONTROL_SCENE_MODE_PARTY;
                //Integer CANDLELIGHT      = CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT;
                //Integer BARCODE          = CameraMetadata.CONTROL_SCENE_MODE_BARCODE;
                //Integer HIGH_SPEED_VIDEO = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
                //Integer HDR              = null;
                //if (Build.VERSION.SDK_INT >= 22) {
                //    HDR = CameraMetadata.CONTROL_SCENE_MODE_HDR;
                //}

                value       =  DISABLED;
                valueString = "DISABLED (PREFERRED)";

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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES;///////////////
            name = key.getName();

            if (keychain.contains(key)) {
                //int[]  modes  = cameraCharacteristics.get(key);
                // a$$ert modes != null;
                //List<Integer> options = ArrayToList.convert(modes);

                Integer OFF = CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF;
                //Integer ON  = CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON;

                value       =  OFF;
                valueString = "OFF (PREFERRED)";

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
            CameraCharacteristics.Key<int[]> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  valueString;

            key  = CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;///////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                int[]  modes  = cameraCharacteristics.get(key);
                if (modes == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AWB modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }
                List<Integer> options = ArrayToList.convert(modes);

                Integer OFF              = CameraMetadata.CONTROL_AWB_MODE_OFF;
                Integer AUTO             = CameraMetadata.CONTROL_AWB_MODE_AUTO;
                //Integer INCANDESCENT     = CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT;
                //Integer FLUORESCENT      = CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT;
                //Integer WARM_FLUORESCENT = CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT;
                //Integer DAYLIGHT         = CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT;
                //Integer CLOUDY_DAYLIGHT  = CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
                //Integer TWILIGHT         = CameraMetadata.CONTROL_AWB_MODE_TWILIGHT;
                //Integer SHADE            = CameraMetadata.CONTROL_AWB_MODE_SHADE;

                if (options.contains(OFF)) {
                    value       =  OFF;
                    valueString = "OFF (PREFERRED)";
                }
                else {
                    value       =  AUTO;
                    valueString = "AUTO (FALLBACK)";
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
            CameraCharacteristics.Key<Boolean> key;
            ParameterFormatter<Boolean> formatter;
            Parameter<Boolean> property;

            String  name;
            Boolean value;

            if (Build.VERSION.SDK_INT >= 23) {
                key  = CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE;////////////////////////////
                name = key.getName();

                if (keychain.contains(key)) {
                    value = cameraCharacteristics.get(key);
                    if (value == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "AWB lock cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    formatter = new ParameterFormatter<Boolean>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Boolean value) {
                            if (value) {
                                return "YES (PREFERRED)";
                            }
                            return "NO (FALLBACK)";
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
            CameraCharacteristics.Key<Integer> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;

            key  = CameraCharacteristics.CONTROL_MAX_REGIONS_AE;////////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AE regions cannot be null");
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

            key  = CameraCharacteristics.CONTROL_MAX_REGIONS_AF;////////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AF regions cannot be null");
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

            key  = CameraCharacteristics.CONTROL_MAX_REGIONS_AWB;///////////////////////////////////
            name = key.getName();

            if (keychain.contains(key)) {
                value = cameraCharacteristics.get(key);
                if (value == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "AWB regions cannot be null");
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
            CameraCharacteristics.Key<Range<Integer>> key;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> property;

            String  name;
            Integer value;
            String  units;

            if (Build.VERSION.SDK_INT >= 24) {
                key   = CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE;/////////////
                name  = key.getName();
                units = "ISO";

                if (keychain.contains(key)) {
                    Range<Integer> range = cameraCharacteristics.get(key);
                    if (range == null) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), "Sensitivity boost cannot be null");
                        MasterController.quitSafely();
                        return;
                    }

                    Integer UNITY = 100;

                    if (range.contains(UNITY)) {
                        value = UNITY;
                    }
                    else {
                        value = range.getUpper();
                    }

                    formatter = new ParameterFormatter<Integer>() {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return value.toString() + " / 100";
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