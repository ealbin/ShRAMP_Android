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
 * @updated: 3 May 2019
 */

package sci.crayfis.shramp.camera2.requests;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.Parameter;

/**
 * Configuration class for default CaptureRequest creation, the parameters set here include:
 *    EDGE_MODE
 */
@TargetApi(21)
abstract class step05_Edge_ extends step04_Distortion_ {

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makeDefault..................................................................................
    /**
     * Creating a default CaptureRequest, setting EDGE_.* parameters
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

        Log.e("                Edge_", "setting default Edge_ requests");
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
            CaptureRequest.Key<Integer> rKey;
            Parameter<Integer> setting;

            String name;

            rKey = CaptureRequest.EDGE_MODE;////////////////////////////////////////////////////////
            name = rKey.getName();

            if (supportedKeys.contains(rKey)) {

                CameraCharacteristics.Key<int[]> cKey;
                Parameter<Integer> property;

                cKey = CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES;
                property = characteristicsMap.get(cKey);
                if (property == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Edge modes cannot be null");
                    MasterController.quitSafely();
                    return;
                }

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
    }

}