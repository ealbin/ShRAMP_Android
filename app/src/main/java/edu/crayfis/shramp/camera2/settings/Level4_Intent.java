package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;

@TargetApi(21)
abstract class Level4_Intent extends Level3_Mode {

    //**********************************************************************************************
    // Class Variables
    //----------------

    private String mIntentName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level4_Intent(@NonNull CameraCharacteristics characteristics,
                            @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setControlCaptureIntent();
    }

    /**
     *
     */
    private void setControlCaptureIntent() {
        /*
         * Documentation provided by:
         * https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
         * https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
         */

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
        CaptureRequest.Key key = CaptureRequest.CONTROL_CAPTURE_INTENT;
        if (mRequestKeys.contains(key)) {
            int intent;
            if (super.mIsManualSensorAble) {
                intent      = CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL;
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
                intent      = CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW;
                mIntentName = "PREVIEW";
                /*
                 * Added in API 21
                 *
                 * This request is for a preview-like use case.
                 *
                 * The precapture trigger may be used to start off a metering w/flash sequence.
                 */
            }
            mCaptureRequestBuilder.set(key, intent);
        }
    }

    /**
     *
     * @return
     */
    @NonNull
    public String toString() {
        String string = super.toString() + "\n";

        string = string.concat("CaptureRequest.CONTROL_CAPTURE_INTENT\n");
        string = string.concat("\t" + "Capture intent is: " + mIntentName + "\n");

        return string;
    }

}