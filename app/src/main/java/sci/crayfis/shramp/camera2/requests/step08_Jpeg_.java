package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step08_Jpeg_ extends step07_Hot_ {

    //**********************************************************************************************
    // Constructors
    //-------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step08_Jpeg_.................................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step08_Jpeg_() { super(); }

    //**********************************************************************************************
    // Overriding Class Methods
    //-------------------------

    // Protected
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

        Log.e("                Jpeg_", "setting default Jpeg_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Location> rKey;
            ParameterFormatter<Location> formatter;
            Parameter<Location> setting;

            String name;
            String valueString;

            rKey = CaptureRequest.JPEG_GPS_LOCATION;////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<Location>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Location value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, null, formatter);
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

            String name;
            String valueString;
            String units;

            rKey  = CaptureRequest.JPEG_ORIENTATION;////////////////////////////////////////////////
            name  = rKey.getName();
            units = "degrees clockwise";

            if (supportedKeys.contains(rKey)) {

                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<Integer>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Integer value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Byte> rKey;
            ParameterFormatter<Byte> formatter;
            Parameter<Byte> setting;

            String name;
            String valueString;
            String units;

            rKey  = CaptureRequest.JPEG_QUALITY;////////////////////////////////////////////////////
            name  = rKey.getName();
            units = "%";

            if (supportedKeys.contains(rKey)) {

                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<Byte>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Byte value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Byte> rKey;
            ParameterFormatter<Byte> formatter;
            Parameter<Byte> setting;

            String name;
            String valueString;
            String units;

            rKey  = CaptureRequest.JPEG_THUMBNAIL_QUALITY;//////////////////////////////////////////
            name  = rKey.getName();
            units = "%";

            if (supportedKeys.contains(rKey)) {

                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<Byte>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Byte value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
            }
            else {
                setting = new Parameter<>(name);
                setting.setValueString("NOT SUPPORTED");
            }
            captureRequestMap.put(rKey, setting);
        }
        //==========================================================================================
        {
            CaptureRequest.Key<Size> rKey;
            ParameterFormatter<Size> formatter;
            Parameter<Size> setting;

            String name;
            String valueString;
            String units;

            rKey  = CaptureRequest.JPEG_THUMBNAIL_SIZE;/////////////////////////////////////////////
            name  = rKey.getName();
            units = "pixels";

            if (supportedKeys.contains(rKey)) {

                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<Size>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Size value) {
                        return getValueString();
                    }
                };
                setting = new Parameter<>(name, null, units, formatter);
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