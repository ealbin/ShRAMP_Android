package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.camera2.util.ParameterFormatter;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class step12_Scaler_ extends step11_Reprocess_ {

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // step12_Scaler_...............................................................................
    /**
     * TODO: description, comments and logging
     */
    protected step12_Scaler_() { super(); }

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

        Log.e("              Scaler_", "setting default Scaler_ requests");
        List<CaptureRequest.Key<?>> supportedKeys;
        supportedKeys = CameraController.getAvailableCaptureRequestKeys();
        assert supportedKeys != null;

        //==========================================================================================
        {
            CaptureRequest.Key<Rect> rKey;
            ParameterFormatter<Rect> formatter;
            Parameter<Rect> setting;

            String name;
            String valueString;
            String units;

            rKey  = CaptureRequest.SCALER_CROP_REGION;//////////////////////////////////////////////
            name  = rKey.getName();
            units = "pixel coordinates";

            if (supportedKeys.contains(rKey)) {

                valueString = "NOT APPLICABLE";

                formatter = new ParameterFormatter<Rect>(valueString) {
                    @NonNull
                    @Override
                    public String formatValue(@NonNull Rect value) {
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