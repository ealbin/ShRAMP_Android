package recycle_bin.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level05_Intent extends Level04_Mode {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer mIntent;
    private   String  mIntentName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level05_Intent(@NonNull CameraCharacteristics characteristics,
                             @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setControlCaptureIntent();
    }

    //----------------------------------------------------------------------------------------------

    /*
     * Documentation provided by:
     * https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
     * https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
     */

    /**
     *
     */
    private void setControlCaptureIntent() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_CAPTURE_INTENT;
        /*
         * Added in API 21
         *
         * Information to the camera device 3A (auto-exposure, auto-focus, auto-white balance)
         * routines about the purpose of this capture, to help the camera device to decide
         * optimal 3A strategy.
         *
         * This control (except for MANUAL) is only effective if android.control.mode != OFF and
         * any 3A routine is active.
         *
         * All intents are supported by all devices, except that:
         *      ZERO_SHUTTER_LAG will be supported if android.request.availableCapabilities
         *      contains PRIVATE_REPROCESSING or YUV_REPROCESSING.
         *
         *      MANUAL will be supported if android.request.availableCapabilities
         *      contains MANUAL_SENSOR.
         *
         *      MOTION_TRACKING will be supported if android.request.availableCapabilities
         *      contains MOTION_TRACKING.
         *
         * This key is available on all devices.
         */
        if (super.mRequestKeys.contains(key)) {
            if (super.mIsManualSensorAble) {
                mIntent     = CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL;
                mIntentName = "MANUAL";
                /*
                 * Added in API 21
                 *
                 * This request is for manual capture use case where the applications want to
                 * directly control the capture parameters.
                 *
                 * For example, the application may wish to manually control
                 * android.sensor.exposureTime, android.sensor.sensitivity, etc.
                 */
            }
            else {
                mIntent     = CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW;
                mIntentName = "PREVIEW";
                /*
                 * Added in API 21
                 *
                 * This request is for a preview-like use case.
                 *
                 * The precapture trigger may be used to start off a metering w/flash sequence.
                 */
            }

            // Patch:
            // Override INTENT_MANUAL
            // Seems to cause CAMERA_DEVICE_ERROR when CaptureRequest.Builder is built
            mIntent     = CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW;
            mIntentName = "PREVIEW";
            mCaptureRequestBuilder.set(key, mIntent);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 05 (Intent)\n";
        string += "CaptureRequest.CONTROL_CAPTURE_INTENT: " + mIntentName + "\n";

        stringList.add(string);
        return stringList;
    }

}