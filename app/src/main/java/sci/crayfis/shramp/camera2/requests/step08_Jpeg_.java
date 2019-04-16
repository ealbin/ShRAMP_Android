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
 * @updated: 15 April 2019
 */

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

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * Configuration class for default CaptureRequest creation, the parameters set here include:
 *    JPEG_GPS_LOCATION
 *    JPEG_ORIENTATION
 *    JPEG_QUALITY
 *    JPEG_THUMBNAIL_QUALITY
 *    JPEG_THUMBNAIL_SIZE
 */
@TargetApi(21)
abstract class step08_Jpeg_ extends step07_Hot_ {

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Creating a default CaptureRequest, setting JPEG_.* parameters
     * @param builder CaptureRequest.Builder in progress
     * @param characteristicsMap Parameter map of characteristics
     * @param captureRequestMap Parameter map of capture request settings
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
        if (supportedKeys == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Supported keys cannot be null");
            MasterController.quitSafely();
            return;
        }

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