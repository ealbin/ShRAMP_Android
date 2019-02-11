package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Range;

@TargetApi(21)
abstract class Level8_MiscControls extends Level7_AutoExposure {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer mControlEffectMode;
    private   String  mControlEffectModeName;

    protected Boolean mControlEnableZsl;
    private   String  mControlEnableZslName;

    protected Integer mControlPostRawSensitivityBoost;
    private   String  mControlPostRawSensitivityBoostName;

    protected Integer mControlSceneMode;
    private   String  mControlSceneModeName;

    protected Integer mControlVideoStabilizationMode;
    private   String  mControlVideoStabilizationModeName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level8_MiscControls(@NonNull CameraCharacteristics characteristics,
                                  @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setControlEffectMode();
        setControlEnableZSL();
        setControlPostRawSensitivityBoost();
        setControlSceneMode();
        setControlVideoStabilizationMode();
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
    private void setControlEffectMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_EFFECT_MODE;
        /*
         * Added in API 21
         *
         * A special color effect to apply.
         *
         * When this mode is set, a color effect will be applied to images produced by the camera
         * device. The interpretation and implementation of these color effects is left to the
         * implementor of the camera device, and should not be depended on to be consistent
         * (or present) across all devices.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlEffectMode     = null;
            mControlEffectModeName = "Not supported";
            return;
        }

        mControlEffectMode     = CameraMetadata.CONTROL_EFFECT_MODE_OFF;
        mControlEffectModeName = "Off";
        /*
         * Added in API 21
         *
         * No color effect will be applied.
         */

        super.mCaptureRequestBuilder.set(key, mControlEffectMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlEnableZSL() {
        //CaptureRequest.Key key = CaptureRequest.CONTROL_ENABLE_ZSL;
        mControlEnableZsl     = null;
        mControlEnableZslName = "Not applicable";
        /*
         * Added in API 26
         *
         * Allow camera device to enable zero-shutter-lag mode for requests with
         * android.control.captureIntent == STILL_CAPTURE.
         *
         * If enableZsl is true, the camera device may enable zero-shutter-lag mode for requests
         * with STILL_CAPTURE capture intent. The camera device may use images captured in the past
         * to produce output images for a zero-shutter-lag request. The result metadata including
         * the android.sensor.timestamp reflects the source frames used to produce output images.
         * Therefore, the contents of the output images and the result metadata may be out of order
         * compared to previous regular requests. enableZsl does not affect requests with other
         * capture intents.
         *
         * For example, when requests are submitted in the following order: Request A: enableZsl is
         * ON, android.control.captureIntent is PREVIEW Request B: enableZsl is ON,
         * android.control.captureIntent is STILL_CAPTURE
         *
         * The output images for request B may have contents captured before the output images for
         * request A, and the result metadata for request B may be older than the result metadata
         * for request A.
         *
         * Note that when enableZsl is true, it is not guaranteed to get output images captured in
         * the past for requests with STILL_CAPTURE capture intent.
         *
         * For applications targeting SDK versions O and newer, the value of enableZsl in
         * TEMPLATE_STILL_CAPTURE template may be true. The value in other templates is always
         * false if present.
         *
         * For applications targeting SDK versions older than O, the value of enableZsl in all
         * capture templates is always false if present.
         *
         * For application-operated ZSL, use CAMERA3_TEMPLATE_ZERO_SHUTTER_LAG template.
         *
         * Optional - This value may be null on some devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlPostRawSensitivityBoost() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mControlPostRawSensitivityBoost     = null;
            mControlPostRawSensitivityBoostName = "Not supported";
            return;
        }
        CaptureRequest.Key key = CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST;
        /*
         * Added in API 24
         *
         * The amount of additional sensitivity boost applied to output images after RAW sensor data
         * is captured.
         *
         * Some camera devices support additional digital sensitivity boosting in the camera
         * processing pipeline after sensor RAW image is captured. Such a boost will be applied to
         * YUV/JPEG format output images but will not have effect on RAW output formats like
         * RAW_SENSOR, RAW10, RAW12 or RAW_OPAQUE.
         *
         * This key will be null for devices that do not support any RAW format outputs. For devices
         * that do support RAW format outputs, this key will always present, and if a device does
         * not support post RAW sensitivity boost, it will list 100 in this key.
         *
         * If the camera device cannot apply the exact boost requested, it will reduce the boost to
         * the nearest supported value. The final boost value used will be available in the output
         * capture result.
         *
         * For devices that support post RAW sensitivity boost, the YUV/JPEG output images of such
         * device will have the total sensitivity of
         * android.sensor.sensitivity * android.control.postRawSensitivityBoost / 100
         * The sensitivity of RAW format images will always be android.sensor.sensitivity
         *
         * This control is only effective if android.control.aeMode or android.control.mode is set
         * to OFF; otherwise the auto-exposure algorithm will override this value.
         *
         * Units: ISO arithmetic units, the same as android.sensor.sensitivity
         *
         * Range of valid values:
         * android.control.postRawSensitivityBoostRange
         *
         * Optional - This value may be null on some devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlPostRawSensitivityBoost     = null;
            mControlPostRawSensitivityBoostName = "Not supported";
            return;
        }

        if (super.mControlMode == CameraMetadata.CONTROL_MODE_OFF
                || super.mControlAeMode == CameraMetadata.CONTROL_AE_MODE_OFF) {
            mControlPostRawSensitivityBoost     = null;
            mControlPostRawSensitivityBoostName = "Disabled";
            return;
        }

        Range<Integer> boostRange = super.mCameraCharacteristics.get(
                                    CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE);
        /*
         * Added in API 24
         *
         * Range of boosts for android.control.postRawSensitivityBoost supported by this camera
         * device.
         *
         * Devices support post RAW sensitivity boost will advertise
         * android.control.postRawSensitivityBoost key for controling post RAW sensitivity boost.
         *
         * This key will be null for devices that do not support any RAW format outputs.
         * For devices that do support RAW format outputs, this key will always present, and if a
         * device does not support post RAW sensitivity boost, it will list (100, 100) in this key.
         *
         * Units: ISO arithmetic units, the same as android.sensor.sensitivity
         *
         * Optional - This value may be null on some devices.
         */
        if (boostRange == null) {
            mControlPostRawSensitivityBoost     = null;
            mControlPostRawSensitivityBoostName = "Not supported";
            return;
        }

        mControlPostRawSensitivityBoost     = 100;
        mControlPostRawSensitivityBoostName = "ISO 100";

        super.mCaptureRequestBuilder.set(key, mControlPostRawSensitivityBoost);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlSceneMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_SCENE_MODE;
        /*
         * Added in API 21
         *
         * Control for which scene mode is currently active.
         *
         * Scene modes are custom camera modes optimized for a certain set of conditions and capture
         * settings.
         *
         * This is the mode that that is active when android.control.mode == USE_SCENE_MODE. Aside
         * from FACE_PRIORITY, these modes will disable android.control.aeMode,
         * android.control.awbMode, and android.control.afMode while in use.
         *
         * The interpretation and implementation of these scene modes is left to the implementor of
         * the camera device. Their behavior will not be consistent across all devices, and any
         * given device may only implement a subset of these modes.
         *
         * Available values for this device:
         * android.control.availableSceneModes
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlSceneMode     = null;
            mControlSceneModeName = "Not supported";
            return;
        }

        mControlSceneMode     = CameraMetadata.CONTROL_SCENE_MODE_DISABLED;
        mControlSceneModeName = "Disabled";
        /*
         * Added in API 21
         *
         * Indicates that no scene modes are set for a given capture request.
         */

        super.mCaptureRequestBuilder.set(key, mControlSceneMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlVideoStabilizationMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE;
        /*
         * Added in API 21
         *
         * Whether video stabilization is active.
         *
         * Video stabilization automatically warps images from the camera in order to stabilize
         * motion between consecutive frames.
         *
         * If enabled, video stabilization can modify the android.scaler.cropRegion to keep the
         * video stream stabilized.
         *
         * Switching between different video stabilization modes may take several frames to
         * initialize, the camera device will report the current mode in capture result metadata.
         * For example, When "ON" mode is requested, the video stabilization modes in the first
         * several capture results may still be "OFF", and it will become "ON" when the
         * initialization is done.
         *
         * In addition, not all recording sizes or frame rates may be supported for stabilization
         * by a device that reports stabilization support. It is guaranteed that an output targeting
         * a MediaRecorder or MediaCodec will be stabilized if the recording resolution is less than
         * or equal to 1920 x 1080 (width less than or equal to 1920, height less than or
         * equal to 1080), and the recording frame rate is less than or equal to 30fps.
         * At other sizes, the CaptureResult android.control.videoStabilizationMode field will
         * return OFF if the recording output is not stabilized, or if there are no output Surface
         * types that can be stabilized.
         *
         * If a camera device supports both this mode and OIS
         * (android.lens.opticalStabilizationMode), turning both modes on may produce undesirable
         * interaction, so it is recommended not to enable both at the same time.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlVideoStabilizationMode     = null;
            mControlVideoStabilizationModeName = "Not supported";
            return;
        }

        mControlVideoStabilizationMode     = CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF;
        mControlVideoStabilizationModeName = "Off";
        /*
         * Added in API 21
         *
         * Video stabilization is disabled
         */

        super.mCaptureRequestBuilder.set(key, mControlVideoStabilizationMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public String toString() {
        String string = super.toString() + "\n";

        string = string.concat("CaptureRequest.CONTROL_EFFECT_MODE: " + mControlEffectModeName + "\n");
        string = string.concat("CaptureRequest.CONTROL_ENABLE_ZSL: " + mControlEnableZslName + "\n");
        string = string.concat("CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST: " + mControlPostRawSensitivityBoostName + "\n");
        string = string.concat("CaptureRequest.CONTROL_SCENE_MODE: " + mControlSceneModeName + "\n");
        string = string.concat("CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE: " + mControlVideoStabilizationModeName + "\n");

        return string;
    }

}