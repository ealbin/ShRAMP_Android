package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;

@TargetApi(21)
abstract class Level2_Template extends Level1_Abilities {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected CaptureRequest.Builder mCaptureRequestBuilder;

    private String mTemplateName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     *
     * @param characteristics
     */
    protected Level2_Template(@NonNull CameraCharacteristics characteristics,
                              @NonNull CameraDevice cameraDevice) {
        super(characteristics);
        getCaptureRequestBuilder(cameraDevice);
    }

    /**
     *
     * @param cameraDevice
     */
    private void getCaptureRequestBuilder(CameraDevice cameraDevice) {
        /*
         * Documentation provided by:
         * https://developer.android.com/reference/android/hardware/camera2/CameraDevice
         */
        try {
            int template;
            if (super.mIsManualSensorAble) {

                template      = CameraDevice.TEMPLATE_MANUAL;
                mTemplateName = "TEMPLATE_MANUAL";
                /*
                 * Added in API 21
                 *
                 * A basic template for direct application control of capture parameters. All
                 * automatic control is disabled (auto-exposure, auto-white balance, auto-focus),
                 * and post-processing parameters are set to preview quality. The manual capture
                 * parameters(exposure, sensitivity, and so on) are set to reasonable defaults,
                 * but should be overriden by the application depending on the intended use case.
                 * This template is guaranteed to be supported on camera devices that support
                 * the MANUAL_SENSOR capability.
                 */
            }
            else {
                template      = CameraDevice.TEMPLATE_PREVIEW;
                mTemplateName = "TEMPLATE_PREVIEW";
                /*
                 * Added in API 21
                 *
                 * Create a request suitable for a camera preview window. Specifically, this means
                 * that high frame rate is given priority over the highest-quality post-processing.
                 * These requests would normally be used with the
                 * CameraCaptureSession.setRepeatingRequest(CaptureRequest,
                 *                           CameraCaptureSession.CaptureCallback, Handler) method.
                 * This template is guaranteed to be supported on all camera devices.
                 */
            }
            mCaptureRequestBuilder = cameraDevice.createCaptureRequest(template);
        }
        catch (CameraAccessException e) {
            // TODO Exception
        }
    }

    /**
     *
     * @return
     */
    @NonNull
    public String toString() {
        String string = super.toString() + "\n";

        string = string.concat("CameraDevice.createCaptureRequest\n");
        string = string.concat("\t" + "Template type: " + mTemplateName + "\n");

        return string;
    }
}