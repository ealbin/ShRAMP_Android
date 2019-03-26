package recycle_bin.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.support.annotation.NonNull;
import android.util.Range;
import android.util.Rational;

import java.text.DecimalFormat;
import java.util.List;

@TargetApi(21)
abstract class Level08_AutoExposure extends Level07_AutoFocus {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer             mControlAeMode;
    private   String              mControlAeModeName;

    protected Integer             mControlAeAntibandingMode;
    private   String              mControlAeAntibandingModeName;

    protected Integer             mControlAeExposureCompensation;
    private   String              mControlAeExposureCompensationName;

    protected Boolean             mControlAeLock;
    private   String              mControlAeLockName;

    protected Integer             mControlAePrecaptureTrigger;
    private   String              mControlAePrecaptureTriggerName;

    protected Range               mControlAeTargetFpsRange;
    private   String              mControlAeTargetFpsRangeName;

    protected MeteringRectangle[] mControlAeRegions;
    private   String              mControlAeRegionsName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level08_AutoExposure(@NonNull CameraCharacteristics characteristics,
                                   @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setControlAeMode();
        setControlAeAntibandingMode();
        setControlAeExposureCompensation();
        setControlAeLock();
        setControlAePrecaptureTrigger();
        setControlAeTargetFpsRange();
        setControlAeRegions();
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
    private void setControlAeMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_MODE;
        int modeOff = CameraMetadata.CONTROL_AE_MODE_OFF;
        int modeOn  = CameraMetadata.CONTROL_AE_MODE_ON;
        /*
         * Added in API 21
         *
         * The desired mode for the camera device's auto-exposure routine.
         *
         * This control is only effective if android.control.mode is AUTO.
         *
         * When set to any of the ON modes, the camera device's auto-exposure routine is enabled,
         * overriding the application's selected exposure time, sensor sensitivity, and frame
         * duration (android.sensor.exposureTime, android.sensor.sensitivity,
         * and android.sensor.frameDuration). If one of the FLASH modes is selected, the camera
         * device's flash unit controls are also overridden.
         *
         * The FLASH modes are only available if the camera device has a flash unit
         * (android.flash.info.available is true).
         *
         * If flash TORCH mode is desired, this field must be set to ON or OFF, and
         * android.flash.mode set to TORCH.
         *
         * When set to any of the ON modes, the values chosen by the camera device auto-exposure
         * routine for the overridden fields for a given capture will be available in its
         * CaptureResult.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAeMode     = null;
            mControlAeModeName = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.CONTROL_MODE_AUTO) {
            mControlAeMode     = null;
            mControlAeModeName = "Disabled";
            return;
        }

        // Default
        mControlAeMode     = modeOn;
        mControlAeModeName = "On";
        /*
         * Added in API 21
         *
         * The camera device's autoexposure routine is active, with no flash control.
         *
         * The application's values for android.sensor.exposureTime, android.sensor.sensitivity,
         * and android.sensor.frameDuration are ignored. The application has control over the
         * various android.flash.* fields.
         */
        List<Integer> modes = super.getAvailable(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
        if (super.mHardwareLevel != HardwareLevel.LEGACY && modes.contains(modeOff)) {
            mControlAeMode     = modeOff;
            mControlAeModeName = "Off";
            /*
             * The camera device's autoexposure routine is disabled.
             *
             * The application-selected android.sensor.exposureTime, android.sensor.sensitivity and
             * android.sensor.frameDuration are used by the camera device, along with
             * android.flash.* fields, if there's a flash unit for this camera device.
             *
             * Note that auto-white balance (AWB) and auto-focus (AF) behavior is device dependent
             * when AE is in OFF mode. To have consistent behavior across different devices,
             * it is recommended to either set AWB and AF to OFF mode or lock AWB and AF before
             * setting AE to OFF. See android.control.awbMode, android.control.afMode,
             * android.control.awbLock, and android.control.afTrigger for more details.
             *
             * LEGACY devices do not support the OFF mode and will override attempts to use this
             * value to ON.
             */
        }

        super.mCaptureRequestBuilder.set(key, mControlAeMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAeAntibandingMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_ANTIBANDING_MODE;
        int modeOff  = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF;
        int mode60Hz = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ;
        int modeAuto = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO;
        /*
         * Added in API 21
         *
         * The desired setting for the camera device's auto-exposure algorithm's antibanding
         * compensation.
         *
         * Some kinds of lighting fixtures, such as some fluorescent lights, flicker at the rate of
         * the power supply frequency (60Hz or 50Hz, depending on country). While this is typically
         * not noticeable to a person, it can be visible to a camera device. If a camera sets its
         * exposure time to the wrong value, the flicker may become visible in the viewfinder as
         * flicker or in a final captured image, as a set of variable-brightness bands across the
         * image.
         *
         * Therefore, the auto-exposure routines of camera devices include antibanding routines that
         * ensure that the chosen exposure value will not cause such banding. The choice of exposure
         * time depends on the rate of flicker, which the camera device can detect automatically, or
         * the expected rate can be selected by the application using this control.
         *
         * A given camera device may not support all of the possible options for the antibanding
         * mode. The android.control.aeAvailableAntibandingModes key contains the available modes
         * for a given camera device.
         *
         * AUTO mode is the default if it is available on given camera device. When AUTO mode is
         * not available, the default will be either 50HZ or 60HZ, and both 50HZ and 60HZ will be
         * available.
         *
         * If manual exposure control is enabled (by setting android.control.aeMode or
         * android.control.mode to OFF), then this setting has no effect, and the application must
         * ensure it selects exposure times that do not cause banding issues. The
         * android.statistics.sceneFlicker key can assist the application in 
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAeAntibandingMode     = null;
            mControlAeAntibandingModeName = "Not supported";
            return;
        }

        if (super.mControlMode == CameraMetadata.CONTROL_MODE_OFF
             || mControlAeMode == CameraMetadata.CONTROL_AE_MODE_OFF) {
            mControlAeAntibandingMode     = null;
            mControlAeAntibandingModeName = "Disabled";
            return;
        }

        // Default
        mControlAeAntibandingMode     = modeAuto;
        mControlAeAntibandingModeName = "Auto";
        /*
         * Added in API 21
         *
         * The camera device will automatically adapt its antibanding routine to the current
         * illumination condition. This is the default mode if AUTO is available on given camera
         * device.
         */
        List<Integer> modes = super.getAvailable(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        if (modes.contains(modeOff)) {
            mControlAeAntibandingMode     = modeOff;
            mControlAeAntibandingModeName = "Off";
            /*
             * Added in API 21
             *
             * The camera device will not adjust exposure duration to avoid banding problems.
             */
        }
        else if (modes.contains(mode60Hz)) {
            mControlAeAntibandingMode     = mode60Hz;
            mControlAeAntibandingModeName = "60 Hz";
            /*
             * Added in API 21
             *
             * The camera device will adjust exposure duration to avoid banding problems with
             * 60Hz illumination sources.
             */
        }

        super.mCaptureRequestBuilder.set(key, mControlAeAntibandingMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAeExposureCompensation() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION;
        /*
         * Added in API 21
         *
         * Adjustment to auto-exposure (AE) target image brightness.
         *
         * The adjustment is measured as a count of steps, with the step size defined by
         * android.control.aeCompensationStep and the allowed range by
         * android.control.aeCompensationRange.
         *
         * For example, if the exposure value (EV) step is 0.333, '6' will mean an exposure
         * compensation of +2 EV; -3 will mean an exposure compensation of -1 EV.
         * One EV represents a doubling of image brightness.
         *
         * Note that this control will only be effective if android.control.aeMode != OFF.
         * This control will take effect even when android.control.aeLock == true.
         *
         * In the event of exposure compensation value being changed, camera device may take
         * several frames to reach the newly requested exposure target. During that time,
         * android.control.aeState field will be in the SEARCHING state. Once the new exposure
         * target is reached, android.control.aeState will change from SEARCHING to either
         * CONVERGED, LOCKED (if AE lock is enabled), or FLASH_REQUIRED (if the scene is too
         * dark for still capture).
         *
         * Units: Compensation steps
         *
         * This key is available on all devices
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAeExposureCompensation     = null;
            mControlAeExposureCompensationName = "Not supported";
            return;
        }

        if (mControlAeMode == null || mControlAeMode == CameraMetadata.CONTROL_AWB_MODE_OFF) {
            mControlAeExposureCompensation     = null;
            mControlAeExposureCompensationName = "Disabled";
            return;
        }

        Range<Integer> compensationRange = super.mCameraCharacteristics.get(
                                           CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        if (compensationRange == null) {
            mControlAeExposureCompensation     = null;
            mControlAeExposureCompensationName = "Not supported";
            return;
        }
        /*
         * Added in API 21
         *
         * Maximum and minimum exposure compensation values for
         * android.control.aeExposureCompensation, in counts of android.control.aeCompensationStep,
         * that are supported by this camera device.
         *
         * Range of valid values:
         *
         * Range [0,0] indicates that exposure compensation is not supported.
         *
         * For LIMITED and FULL devices, range must follow below requirements if exposure
         * compensation is supported (range != [0, 0]):
         *
         * Min.exposure compensation * android.control.aeCompensationStep <= -2 EV
         *
         * Max.exposure compensation * android.control.aeCompensationStep >= 2 EV
         *
         * LEGACY devices may support a smaller range than 
         *
         * This key is available on all devices.
         */
        if (compensationRange.equals(new Range<>(0, 0))) {
            mControlAeExposureCompensation     = null;
            mControlAeExposureCompensationName = "Not supported";
            return;
        }

        Rational controlAeCompensationStep = super.mCameraCharacteristics.get(
                                             CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
        if (controlAeCompensationStep == null) {
            mControlAeExposureCompensation     = null;
            mControlAeExposureCompensationName = "Not supported";
            return;
        }
        /*
         * Added in API 21
         *
         * Smallest step by which the exposure compensation can be changed.
         *
         * This is the unit for android.control.aeExposureCompensation. For example, if this key
         * has a value of 1/2, then a setting of -2 for android.control.aeExposureCompensation means
         * that the target EV offset for the auto-exposure routine is -1 EV.
         *
         * One unit of EV compensation changes the brightness of the captured image by a factor of
         * two. +1 EV doubles the image brightness, while -1 EV halves the image brightness.
         *
         * Units: Exposure Value (EV)
         *
         * This key is available on all devices.
         */
        mControlAeExposureCompensation = compensationRange.getLower();
        DecimalFormat df = new DecimalFormat("#.##");
        mControlAeExposureCompensationName =
                df.format(mControlAeExposureCompensation * controlAeCompensationStep.doubleValue() ) + " [EV]";

        super.mCaptureRequestBuilder.set(key, mControlAeExposureCompensation);
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     */
    private void setControlAeLock() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_LOCK;
        boolean lockOff = false;
        boolean lockOn  = true;
        /*
         * Added in API 21
         *
         * Whether auto-exposure (AE) is currently locked to its latest calculated values.
         *
         * When set to true (ON), the AE algorithm is locked to its latest parameters, and will not
         * change exposure settings until the lock is set to false (OFF).
         *
         * Note that even when AE is locked, the flash may be fired if the android.control.aeMode is
         * ON_AUTO_FLASH / ON_ALWAYS_FLASH / ON_AUTO_FLASH_REDEYE.
         *
         * When android.control.aeExposureCompensation is changed, even if the AE lock is ON, the
         * camera device will still adjust its exposure value.
         *
         * If AE precapture is triggered (see android.control.aePrecaptureTrigger) when AE is
         * already locked, the camera device will not change the exposure time
         * (android.sensor.exposureTime) and sensitivity (android.sensor.sensitivity) parameters.
         * The flash may be fired if the android.control.aeMode is
         * ON_AUTO_FLASH/ON_AUTO_FLASH_REDEYE and the scene is too dark. If the
         * android.control.aeMode is ON_ALWAYS_FLASH, the scene may become overexposed. Similarly,
         * AE precapture trigger CANCEL has no effect when AE is already locked.
         *
         * When an AE precapture sequence is triggered, AE unlock will not be able to unlock the AE
         * if AE is locked by the camera device internally during precapture metering sequence In
         * other words, submitting requests with AE unlock has no effect for an ongoing precapture
         * metering sequence. Otherwise, the precapture metering sequence will never succeed in a
         * sequence of preview requests where AE lock is always set to false.
         *
         * Since the camera device has a pipeline of in-flight requests, the settings that get
         * locked do not necessarily correspond to the settings that were present in the latest
         * capture result received from the camera device, since additional captures and AE updates
         * may have occurred even before the result was sent out. If an application is switching
         * between automatic and manual control and wishes to eliminate any flicker during the
         * switch, the following procedure is recommended:
         *
         *      Starting in auto-AE mode:
         *
         *      Lock AE
         *
         *      Wait for the first result to be output that has the AE locked
         *
         *      Copy exposure settings from that result into a request, set the request to manual AE
         *
         *      Submit the capture request, proceed to run manual AE as desired.
         *
         * See android.control.aeState for AE lock related state transition details.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAeLock     = null;
            mControlAeLockName = "Not supported";
            return;
        }

        if (mControlAeMode == null || mControlAeMode == CameraMetadata.CONTROL_AWB_MODE_OFF) {
            mControlAeLock     = null;
            mControlAeLockName = "Disabled";
            return;
        }

        // Default
        mControlAeLock     = lockOn;
        mControlAeLockName = "On";

        super.mCaptureRequestBuilder.set(key, mControlAeLock);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAePrecaptureTrigger() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER;
        mControlAePrecaptureTrigger     = null;
        mControlAePrecaptureTriggerName = "Not applicable";
        /*
         * Added in API 21
         *
         * Whether the camera device will trigger a precapture metering sequence when it processes
         * this request.
         *
         * This entry is normally set to IDLE, or is not included at all in the request settings.
         * When included and set to START, the camera device will trigger the auto-exposure (AE)
         * precapture metering sequence.
         *
         * When set to CANCEL, the camera device will cancel any active precapture metering trigger,
         * and return to its initial AE state. If a precapture metering sequence is already
         * completed, and the camera device has implicitly locked the AE for subsequent still
         * capture, the CANCEL trigger will unlock the AE and return to its initial AE state.
         *
         * The precapture sequence should be triggered before starting a high-quality still capture
         * for final metering decisions to be made, and for firing pre-capture flash pulses to
         * estimate scene brightness and required final capture flash power, when the flash is
         * enabled.
         *
         * Normally, this entry should be set to START for only a single request, and the
         * application should wait until the sequence completes before starting a new one.
         *
         * When a precapture metering sequence is finished, the camera device may lock the
         * auto-exposure routine internally to be able to accurately expose the subsequent still
         * capture image (android.control.captureIntent == STILL_CAPTURE). For this case, the AE may
         * not resume normal scan if no subsequent still capture is submitted. To ensure that the AE
         * routine restarts normal scan, the application should submit a request with
         * android.control.aeLock == true, followed by a request with
         * android.control.aeLock == false, if the application decides not to submit a still capture
         * request after the precapture sequence completes. Alternatively, for API level 23 or newer
         * devices, the CANCEL can be used to unlock the camera device internally locked AE if the
         * application doesn't submit a still capture request after the AE precapture trigger. Note
         * that, the CANCEL was added in API level 23, and must not be used in devices that have
         * earlier API levels.
         *
         * The exact effect of auto-exposure (AE) precapture trigger depends on the current AE mode
         * and state; see android.control.aeState for AE precapture state transition details.
         *
         * On LEGACY-level devices, the precapture trigger is not supported; capturing a
         * high-resolution JPEG image will automatically trigger a precapture sequence before the
         * high-resolution capture, including potentially firing a pre-capture flash.
         *
         * Using the precapture trigger and the auto-focus trigger android.control.afTrigger
         * simultaneously is allowed. However, since these triggers often require cooperation
         * between the auto-focus and auto-exposure routines (for example, the may need to be
         * enabled for a focus sweep), the camera device may delay acting on a later trigger until
         * the previous trigger has been fully handled. This may lead to longer intervals between
         * the trigger and changes to android.control.aeState indicating the start of the precapture
         * sequence, for example.
         *
         * If both the precapture and the auto-focus trigger are activated on the same request, then
         * the camera device will complete them in the optimal order for that device.
         *
         * Optional - This value may be null on some devices.
         *
         * Limited capability - Present on all camera devices that report being at least
         * HARDWARE_LEVEL_LIMITED devices in the android.info.supportedHardwareLevel key
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAeTargetFpsRange() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE;
        /*
         * Added in API 21
         *
         * Range over which the auto-exposure routine can adjust the capture frame rate to maintain
         * good exposure.
         *
         * Only constrains auto-exposure (AE) algorithm, not manual control of
         * android.sensor.exposureTime and android.sensor.frameDuration.
         *
         * Units: Frames per second (FPS)
         *
         * Range of valid values:
         * Any of the entries in android.control.aeAvailableTargetFpsRanges
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAeTargetFpsRange     = null;
            mControlAeTargetFpsRangeName = "Not supported";
            return;
        }

        if (super.mControlMode == CameraMetadata.CONTROL_MODE_OFF
                || mControlAeMode == CameraMetadata.CONTROL_AE_MODE_OFF) {
            mControlAeTargetFpsRange     = null;
            mControlAeTargetFpsRangeName = "Disabled";
            return;
        }

        Range<Integer>[] fpsRanges = mCameraCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (fpsRanges == null) {
            mControlAeTargetFpsRange     = null;
            mControlAeTargetFpsRangeName = "Not supported";
            return;
        }

        Range<Integer> fastestRange = null;
        for (Range<Integer> range : fpsRanges) {
            if (fastestRange == null) {
                fastestRange = range;
                continue;
            }
            long range_product   =        range.getLower() *        range.getLower();
            long current_product = fastestRange.getLower() * fastestRange.getUpper();
            if (range_product > current_product && range.getUpper() >= fastestRange.getUpper()) {
                fastestRange = range;
            }
        }
        mControlAeTargetFpsRange     = fastestRange;
        mControlAeTargetFpsRangeName = fastestRange.toString();

        super.mCaptureRequestBuilder.set(key, mControlAeTargetFpsRange);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAeRegions() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_REGIONS;
        mControlAeRegions      = null;
        mControlAeRegionsName  = "Not applicable";
        /*
         * Added in API 21
         *
         * List of metering areas to use for auto-exposure adjustment.
         *
         * Not available if android.control.maxRegionsAe is 0. Otherwise will always be present.
         *
         * The maximum number of regions supported by the device is determined by the value of
         * android.control.maxRegionsAe.
         *
         * For devices not supporting android.distortionCorrection.mode control, the coordinate
         * system always follows that of android.sensor.info.activeArraySize, with (0,0) being the
         * top-left pixel in the active pixel array, and
         * (android.sensor.info.activeArraySize.width - 1,
         * android.sensor.info.activeArraySize.height - 1) being the bottom-right pixel in the
         * active pixel array.
         *
         * For devices supporting android.distortionCorrection.mode control, the coordinate system
         * depends on the mode being set. When the distortion correction mode is OFF, the coordinate
         * system follows android.sensor.info.preCorrectionActiveArraySize, with (0, 0) being the
         * top-left pixel of the pre-correction active array, and
         * (android.sensor.info.preCorrectionActiveArraySize.width - 1,
         * android.sensor.info.preCorrectionActiveArraySize.height - 1) being the bottom-right pixel
         * in the pre-correction active pixel array. When the distortion correction mode is not OFF,
         * the coordinate system follows android.sensor.info.activeArraySize, with (0, 0) being the
         * top-left pixel of the active array, and (android.sensor.info.activeArraySize.width - 1,
         * android.sensor.info.activeArraySize.height - 1) being the bottom-right pixel in the
         * active pixel array.
         *
         * The weight must be within [0, 1000], and represents a weight for every pixel in the area.
         * This means that a large metering area with the same weight as a smaller area will have
         * more effect in the metering result. Metering areas can partially overlap and the camera
         * device will add the weights in the overlap region.
         *
         * The weights are relative to weights of other exposure metering regions, so if only one
         * region is used, all non-zero weights will have the same effect. A region with 0 weight is
         * ignored.
         *
         * If all regions have 0 weight, then no specific metering area needs to be used by the
         * camera device.
         *
         * If the metering region is outside the used android.scaler.cropRegion returned in capture
         * result metadata, the camera device will ignore the sections outside the crop region and
         * output only the intersection rectangle as the metering region in the result metadata. If
         * the region is entirely outside the crop region, it will be ignored and not reported in
         * the result metadata.
         *
         * Units: Pixel coordinates within android.sensor.info.activeArraySize or
         * android.sensor.info.preCorrectionActiveArraySize depending on distortion correction
         * capability and mode
         *
         * Range of valid values:
         * Coordinates must be between [(0,0), (width, height)) of
         * android.sensor.info.activeArraySize or android.sensor.info.preCorrectionActiveArraySize
         * depending on distortion correction capability and mode
         *
         * Optional - This value may be null on some devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 08 (Auto-exposure)\n";
        string += "CaptureRequest.CONTROL_AE_MODE:                  " + mControlAeModeName                 + "\n";
        string += "CaptureRequest.CONTROL_AE_ANTIBANDING_MODE:      " + mControlAeAntibandingModeName      + "\n";
        string += "CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION: " + mControlAeExposureCompensationName + "\n";
        string += "CaptureRequest.CONTROL_AE_LOCK:                  " + mControlAeLockName                 + "\n";
        string += "CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER:    " + mControlAePrecaptureTriggerName    + "\n";
        string += "CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE:      " + mControlAeTargetFpsRangeName       + "\n";
        string += "CaptureRequest.CONTROL_AE_REGIONS:               " + mControlAeRegionsName              + "\n";

        stringList.add(string);
        return stringList;
    }

}