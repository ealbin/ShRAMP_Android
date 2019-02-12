package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level07_AutoFocus extends Level06_AutoWhiteBalance {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer             mControlAfMode;
    private   String              mControlAfModeName;

    protected MeteringRectangle[] mControlAfRegions;
    private   String              mControlAfRegionsName;

    protected Integer             mControlAfTrigger;
    private   String              mControlAfTriggerName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level07_AutoFocus(@NonNull CameraCharacteristics characteristics,
                                @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setControlAfMode();
        setControlAfRegions();
        setControlAfTrigger();
    }

    //----------------------------------------------------------------------------------------------

    /*
     * Documentation provided by:
     * https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
     * https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
     */

    private void setControlAfMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AF_MODE;
        int modeOff  = CameraMetadata.CONTROL_AF_MODE_OFF;
        int modeAuto = CameraMetadata.CONTROL_AF_MODE_AUTO;
        /*
         * Added in Api 21
         *
         * Whether auto-focus (AF) is currently enabled, and what mode it is set to.
         *
         * Only effective if android.control.mode = AUTO and the lens is not fixed focus
         * (i.e. android.lens.info.minimumFocusDistance > 0). Also note that when
         * android.control.aeMode is OFF, the behavior of AF is device dependent. It is recommended
         * to lock AF by using android.control.afTrigger before setting android.control.aeMode to
         * OFF, or set AF mode to OFF when AE is OFF.
         *
         * If the lens is controlled by the camera device auto-focus algorithm, the camera device
         * will report the current AF status in android.control.afState in result metadata.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAfMode     = null;
            mControlAfModeName = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.CONTROL_MODE_AUTO) {
            mControlAfMode     = null;
            mControlAfModeName = "Disabled";
            return;
        }

        // Default
        mControlAfMode     = modeAuto;
        mControlAfModeName = "Auto";
        /*
         * Added in API 21
         *
         * Basic automatic focus mode.
         *
         * In this mode, the lens does not move unless the autofocus trigger action is called.
         * When that trigger is activated, AF will transition to ACTIVE_SCAN, then to the outcome
         * of the scan (FOCUSED or NOT_FOCUSED).
         *
         * Always supported if lens is not fixed focus.
         *
         * Use android.lens.info.minimumFocusDistance to determine if lens is fixed-focus.
         *
         * Triggering AF_CANCEL resets the lens position to default, and sets the AF state
         * to INACTIVE.
         */
        List<Integer> modes = super.getAvailable(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        if (modes.contains(modeOff)) {
            mControlAfMode     = modeOff;
            mControlAfModeName = "Off";
            /*
             * Added in API 21
             *
             * The auto-focus routine does not control the lens; android.lens.focusDistance is
             * controlled by the application.
             */
        }

        super.mCaptureRequestBuilder.set(key, mControlAfMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAfRegions() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AF_REGIONS;
        mControlAfRegions      = null;
        mControlAfRegionsName  = "Not applicable";
        /*
         * Added in API 21
         *
         * List of metering areas to use for auto-focus.
         *
         * Not available if android.control.maxRegionsAf is 0. Otherwise will always be present.
         *
         * The maximum number of focus areas supported by the device is determined by the value of
         * android.control.maxRegionsAf.
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
         * The weights are relative to weights of other metering regions, so if only one region is
         * used, all non-zero weights will have the same effect. A region with 0 weight is ignored.
         *
         * If all regions have 0 weight, then no specific metering area needs to be used by the
         * camera device. The capture result will either be a zero weight region as well, or the
         * region selected by the camera device as the focus area of interest.
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
         *      Coordinates must be between [(0,0), (width, height)) of
         *      android.sensor.info.activeArraySize or
         *      android.sensor.info.preCorrectionActiveArraySize depending on distortion correction
         *      capability and mode
         *
         * Optional - This value may be null on some devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAfTrigger() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AF_TRIGGER;
        mControlAfTrigger      = null;
        mControlAfTriggerName  = "Not applicable";
        /*
         * Added in API 21
         *
         * Whether the camera device will trigger autofocus for this request.
         *
         * This entry is normally set to IDLE, or is not included at all in the request settings.
         *
         * When included and set to START, the camera device will trigger the autofocus algorithm.
         * If autofocus is disabled, this trigger has no effect.
         *
         * When set to CANCEL, the camera device will cancel any active trigger, and return to its
         * initial AF state.
         *
         * Generally, applications should set this entry to START or CANCEL for only a single
         * capture, and then return it to IDLE (or not set at all). Specifying START for multiple
         * captures in a row means restarting the AF operation over and over again.
         *
         * See android.control.afState for what the trigger means for each AF mode.
         *
         * Using the autofocus trigger and the precapture trigger
         * android.control.aePrecaptureTrigger simultaneously is allowed. However, since these
         * triggers often require cooperation between the auto-focus and auto-exposure routines
         * (for example, the may need to be enabled for a focus sweep), the camera device may delay
         * acting on a later trigger until the previous trigger has been fully handled. This may
         * lead to longer intervals between the trigger and changes to android.control.afState,
         * for example.
         *
         * This key is available on all devices.
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

        String string = "Level 07 (Auto-focus)\n";
        string += "CaptureRequest.CONTROL_AF_MODE:    " + mControlAfModeName    + "\n";
        string += "CaptureRequest.CONTROL_AF_REGIONS: " + mControlAfRegionsName + "\n";
        string += "CaptureRequest.CONTROL_AF_TRIGGER: " + mControlAfTriggerName + "\n";

        stringList.add(string);
        return stringList;
    }
}

