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

package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step13_Sensor_ extends step12_Scaler_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step13_Sensor_...............................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step13_Sensor_() { super(); }

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * TODO: description, comments and logging
     * @param builder bla
     * @param characteristicsMap bla
     * @param captureRequestMap bla
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void makeDefault(@NonNull CaptureRequest.Builder builder,
                               @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap,
                               @NonNull LinkedHashMap<CaptureRequest.Key, Parameter> captureRequestMap) {
        super.makeDefault(builder, characteristicsMap, captureRequestMap);

        Log.e("              Sensor_", "setting default Sensor_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Long> rKey;
            ParameterFormatter<Long> formatter;
            Parameter<Long> setting;

            String name;
            Long   value;
            String units;

            rKey  = CaptureRequest.SENSOR_FRAME_DURATION;///////////////////////////////////////////
            name  = rKey.getName();
            units = "nanoseconds";

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (FALLBACK)");
                }
                else {
                    CameraCharacteristics.Key<StreamConfigurationMap> cKey;
                    Parameter<StreamConfigurationMap> property;

                    cKey = CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    StreamConfigurationMap streamConfigurationMap;
                    streamConfigurationMap = property.getValue();
                    assert streamConfigurationMap != null;

                    Integer imageFormat = CameraController.getOutputFormat();
                    Size    imageSize   = CameraController.getOutputSize();
                    assert imageFormat != null;
                    assert imageSize   != null;

                    value = streamConfigurationMap.getOutputMinFrameDuration(imageFormat, imageSize);

                    formatter = new ParameterFormatter<Long>("minimum: ") {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Long value) {
                            DecimalFormat nanosFormatter;
                            nanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                            return getValueString() + nanosFormatter.format(value);
                        }
                    };
                    setting = new Parameter<>(name, value, units, formatter);

                    builder.set(rKey, setting.getValue());
                }
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Long> rKey;
            Parameter<Long> setting;

            String name;

            rKey = CaptureRequest.SENSOR_EXPOSURE_TIME;/////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (FALLBACK)");
                }
                else {
                    Parameter<Long> frameDuration;
                    frameDuration = captureRequestMap.get(CaptureRequest.SENSOR_FRAME_DURATION);
                    assert frameDuration != null;

                    setting = new Parameter<>(name, frameDuration.getValue(), frameDuration.getUnits(),
                                                                              frameDuration.getFormatter());

                    builder.set(rKey, setting.getValue());
                }
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            ParameterFormatter<Integer> formatter;
            Parameter<Integer> setting;

            String  name;
            Integer value;
            String  units;

            rKey  = CaptureRequest.SENSOR_SENSITIVITY;//////////////////////////////////////////////
            name  = rKey.getName();
            units = "ISO";

            if (supportedKeys.contains(rKey)) {

                Parameter<Integer> mode;
                mode = captureRequestMap.get(CaptureRequest.CONTROL_AE_MODE);
                assert mode != null;

                if (mode.toString().contains("AUTO")) {
                    setting = new Parameter<>(name);
                    setting.setValueString("DISABLED (FALLBACK)");
                }
                else {
                    CameraCharacteristics.Key<Range<Integer>> cKey;
                    Parameter<Range<Integer>> property;

                    cKey = CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE;
                    property = characteristicsMap.get(cKey);
                    assert property != null;

                    Range<Integer> range = property.getValue();
                    assert range != null;
                    value = range.getUpper();

                    formatter = new ParameterFormatter<Integer>("maximum: ") {
                        @NonNull
                        @Override
                        public String formatValue(@NonNull Integer value) {
                            return getValueString() + value.toString();
                        }
                    };
                    setting = new Parameter<>(name, value, units, formatter);

                    builder.set(rKey, setting.getValue());
                }
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.SENSOR_TEST_PATTERN_MODE;/////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES;
                property = characteristicsMap.get(cKey);
                assert property != null;

                setting = new Parameter<>(name, property.getValue(), property.getUnits(),
                                                                     property.getFormatter());

                builder.set(rKey, setting.getValue());
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<int[]> rKey;
            ParameterFormatter<int[]> formatter;
            Parameter<int[]> setting;

            String name;
            int[]  value;
            String valueString;
            String units;

            rKey  = CaptureRequest.SENSOR_TEST_PATTERN_DATA;////////////////////////////////////////
            name  = rKey.getName();
            units = null;

            if (supportedKeys.contains(rKey)) {

                value = null;
                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<int[]>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull int[] value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, value, units, formatter);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
    }

}