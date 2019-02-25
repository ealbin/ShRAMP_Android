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
            mLogger.log(" \n\n" + string + " \n");
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    public void keyDump() {

         mLogger.log(" \n\nCameraCharacteristics.getKeys(): \n\n");
         List<CameraCharacteristics.Key<?>> characteristicKeys = mCameraCharacteristics.getKeys();
         for (CameraCharacteristics.Key key : characteristicKeys) {
            mLogger.log(key.toString());
         }

         mLogger.log(" \n\nCameraCharacteristics.getAvailableCaptureRequestKeys(): \n\n");
         List<CaptureRequest.Key<?>> requestKeys  = mCameraCharacteristics.getAvailableCaptureRequestKeys();
         for (CaptureRequest.Key key : requestKeys) {
            mLogger.log(key.toString());
         }

         mLogger.log(" \n\nCameraCharacteristics.getAvailableSessionKeys(): \n\n");
         List<CaptureRequest.Key<?>> sessionKeys  = mCameraCharacteristics.getAvailableSessionKeys();
         for (CaptureRequest.Key key : sessionKeys) {
            mLogger.log(key.toString());
         }

         mLogger.log(" \n\nCameraCharacteristics.getAvailablePhysicalCameraRequestKeys(): \n\n");
         List<CaptureRequest.Key<?>> physicalKeys = mCameraCharacteristics.getAvailablePhysicalCameraRequestKeys();
         if (physicalKeys != null) {
            for (CaptureRequest.Key key : physicalKeys) {
                mLogger.log(key.toString());
            }
         }

         mLogger.log(" \n\nCameraCharacteristics.getPhysicalCameraIds(): \n\n");
         Set<String> physicalIds = mCameraCharacteristics.getPhysicalCameraIds();
         for (String id : physicalIds) {
                mLogger.log(id);
         }

         mLogger.log(" \n\nCameraCharacteristics.getAvailableCaptureResultKeys(): \n\n");
         List<CaptureResult.Key<?>>  resultKeys = mCameraCharacteristics.getAvailableCaptureResultKeys();
         for (CaptureResult.Key key : resultKeys) {
             mLogger.log(key.toString());
         }
    }

}
