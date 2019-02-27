package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Set;

import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * Root class for configuring the camera
 */
@TargetApi(21)
public final class ShrampCamSettings extends Level17_Misc{

    //**********************************************************************************************
    // Class Variables
    //----------------

    // logging
    static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     *
     * @param characteristics
     * @param cameraDevice
     */
    public ShrampCamSettings(@NonNull CameraCharacteristics characteristics,
                             @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    public CaptureRequest.Builder getCaptureRequestBuilder() {
        return super.mCaptureRequestBuilder;
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    public void logSettings() {
        List<String> stringList = super.getString();
        for (String string : stringList) {
            ShrampCamSettings.mLogger.log(" \n\n" + string + " \n");
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    public void keyDump() {

         ShrampCamSettings.mLogger.log(" \n\nCameraCharacteristics.getKeys(): \n\n");
         List<CameraCharacteristics.Key<?>> characteristicKeys = super.mCameraCharacteristics.getKeys();
         for (CameraCharacteristics.Key key : characteristicKeys) {
            ShrampCamSettings.mLogger.log(key.toString());
         }

         ShrampCamSettings.mLogger.log(" \n\nCameraCharacteristics.getAvailableCaptureRequestKeys(): \n\n");
         List<CaptureRequest.Key<?>> requestKeys  = super.mCameraCharacteristics.getAvailableCaptureRequestKeys();
         for (CaptureRequest.Key key : requestKeys) {
            ShrampCamSettings.mLogger.log(key.toString());
         }

         ShrampCamSettings.mLogger.log(" \n\nCameraCharacteristics.getAvailableSessionKeys(): \n\n");
         List<CaptureRequest.Key<?>> sessionKeys  = super.mCameraCharacteristics.getAvailableSessionKeys();
         for (CaptureRequest.Key key : sessionKeys) {
            ShrampCamSettings.mLogger.log(key.toString());
         }

         ShrampCamSettings.mLogger.log(" \n\nCameraCharacteristics.getAvailablePhysicalCameraRequestKeys(): \n\n");
         List<CaptureRequest.Key<?>> physicalKeys = super.mCameraCharacteristics.getAvailablePhysicalCameraRequestKeys();
         if (physicalKeys != null) {
            for (CaptureRequest.Key key : physicalKeys) {
                ShrampCamSettings.mLogger.log(key.toString());
            }
         }

         ShrampCamSettings.mLogger.log(" \n\nCameraCharacteristics.getPhysicalCameraIds(): \n\n");
         Set<String> physicalIds = super.mCameraCharacteristics.getPhysicalCameraIds();
         for (String id : physicalIds) {
                ShrampCamSettings.mLogger.log(id);
         }

         ShrampCamSettings.mLogger.log(" \n\nCameraCharacteristics.getAvailableCaptureResultKeys(): \n\n");
         List<CaptureResult.Key<?>>  resultKeys = super.mCameraCharacteristics.getAvailableCaptureResultKeys();
         for (CaptureResult.Key key : resultKeys) {
             ShrampCamSettings.mLogger.log(key.toString());
         }
    }

}
