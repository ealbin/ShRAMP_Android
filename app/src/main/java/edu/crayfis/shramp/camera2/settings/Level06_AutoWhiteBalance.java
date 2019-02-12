package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level06_AutoWhiteBalance extends Level05_Intent {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer             mControlAwbMode;
    private   String              mControlAwbModeName;

    protected Boolean             mControlAwbLock;
    private   String              mControlAwbLockName;

    protected MeteringRectangle[] mControlAwbRegions;
    private   String              mControlAwbRegionsName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level06_AutoWhiteBalance(@NonNull CameraCharacteristics characteristics,
                                       @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setControlAwbMode();
        setControlAwbLock();
        setControlAwbRegions();
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
    private void setControlAwbMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AWB_MODE;
        int modeOff  = CameraMetadata.CONTROL_AWB_MODE_OFF;
        int modeAuto = CameraMetadata.CONTROL_AWB_MODE_AUTO;
        /*
         * Added in API 21
         *
         * Whether auto-white balance (AWB) is currently setting the color transform fields, and
         * what its illumination target is.
         *
         * This control is only effective if android.control.mode is AUTO.
         *
         * When set to the ON mode, the camera device's auto-white balance routine is enabled,
         * overriding the application's selected android.colorCorrection.transform,
         * android.colorCorrection.gains and android.colorCorrection.mode.
         * Note that when android.control.aeMode is OFF, the behavior of AWB is device dependent.
         * It is recommened to also set AWB mode to OFF or lock AWB by using android.control.awbLock
         * before setting AE mode to OFF.
         *
         * When set to the OFF mode, the camera device's auto-white balance routine is disabled.
         * The application manually controls the white balance by android.colorCorrection.transform,
         * android.colorCorrection.gains and android.colorCorrection.mode.
         *
         * When set to any other modes, the camera device's auto-white balance routine is disabled.
         * The camera device uses each particular illumination target for white balance adjustment.
         * The application's values for android.colorCorrection.transform,
         * android.colorCorrection.gains and android.colorCorrection.mode are ignored.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAwbMode     = null;
            mControlAwbModeName = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.CONTROL_MODE_AUTO) {
            mControlAwbMode     = null;
            mControlAwbModeName = "Disabled";
            return;
        }

        // Default
        mControlAwbMode     = modeAuto;
        mControlAwbModeName = "Auto";
        /*
         * Added in API 21
         *
         * The camera device's auto-white balance routine is active.
         *
         * The application's values for android.colorCorrection.transform and
         * android.colorCorrection.gains are ignored. For devices that support
         * the MANUAL_POST_PROCESSING capability, the values used by the camera device for the
         * transform and gains will be available in the capture result for this request.
         */

        List<Integer> modes = super.getAvailable(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
        if (modes.contains(modeOff)) {
            mControlAwbMode     = modeOff;
            mControlAwbModeName = "Off";
            /*
             * Added in API 21
             *
             * The camera device's auto-white balance routine is disabled.
             *
             * The application-selected color transform matrix (android.colorCorrection.transform)
             * and gains (android.colorCorrection.gains) are used by the camera device for manual
             * white balance control.
             */
        }

        super.mCaptureRequestBuilder.set(key, mControlAwbMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAwbLock() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AWB_LOCK;
        boolean lockOff = false;
        boolean lockOn  = true;
        /*
         * Added in API 21
         *
         * Whether auto-white balance (AWB) is currently locked to its latest calculated values.
         *
         * When set to true (ON), the AWB algorithm is locked to its latest parameters, and will not
         * change color balance settings until the lock is set to false (OFF).
         *
         * Since the camera device has a pipeline of in-flight requests, the settings that get
         * locked do not necessarily correspond to the settings that were present in the latest
         * capture result received from the camera device, since additional captures and AWB updates
         * may have occurred even before the result was sent out. If an application is switching
         * between automatic and manual control and wishes to eliminate any flicker during the
         * switch, the following procedure is recommended:
         *
         *  Starting in auto-AWB mode:
         *      Lock AWB
         *
         *      Wait for the first result to be output that has the AWB locked
         *
         *      Copy AWB settings from that result into a request, set the request to manual AWB
         *
         *      Submit the capture request, proceed to run manual AWB as desired.
         *
         * Note that AWB lock is only meaningful when android.control.awbMode is in the AUTO mode;
         * in other modes, AWB is already fixed to a specific setting.
         *
         * Some LEGACY devices may not support ON; the value is then overridden to OFF.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mControlAwbLock     = null;
            mControlAwbLockName = "Not supported";
            return;
        }

        if (mControlAwbMode == null || mControlAwbMode != CameraMetadata.CONTROL_AWB_MODE_AUTO) {
            mControlAwbLock     = null;
            mControlAwbLockName = "Disabled";
            return;
        }

        // Default
        mControlAwbLock     = lockOn;
        mControlAwbLockName = "On";

        super.mCaptureRequestBuilder.set(key, mControlAwbLock);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setControlAwbRegions() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_AWB_REGIONS;
        mControlAwbRegions     = null;
        mControlAwbRegionsName = "Not applicable";
        /*
         * Added in API 21
         *
         * List of metering areas to use for auto-white-balance illuminant estimation.
         *
         * Not available if android.control.maxRegionsAwb is 0. Otherwise will always be present.
         *
         * The maximum number of regions supported by the device is determined by the value of
         * android.control.maxRegionsAwb.
         *
         * For devices not supporting android.distortionCorrection.mode control, the coordinate
         * system always follows that of android.sensor.info.activeArraySize, with (0,0) being the
         * top-left pixel in the active pixel array, and
         * (android.sensor.info.activeArraySize.width - 1,
         * android.sensor.info.activeArraySize.height - 1) being the bottom-right pixel in the
         * active pixel array.
         *
         * For devices supporting android.distortionCorrection.mode control, the coordinate system
         * depends on the mode being set. When the distortion correction mode is OFF, the
         * coordinate system follows android.sensor.info.preCorrectionActiveArraySize, with (0, 0)
         * being the top-left pixel of the pre-correction active array, and
         * (android.sensor.info.preCorrectionActiveArraySize.width - 1,
         * android.sensor.info.preCorrectionActiveArraySize.height - 1) being the bottom-right pixel
         * in the pre-correction active pixel array. When the distortion correction mode is not OFF,
         * the coordinate system follows android.sensor.info.activeArraySize, with (0, 0) being the
         * top-left pixel of the active array, and (android.sensor.info.activeArraySize.width - 1,
         * android.sensor.info.activeArraySize.height - 1) being the bottom-right pixel in the
         * active pixel array.
         *
         * The weight must range from 0 to 1000, and represents a weight for every pixel in the area.
         * This means that a large metering area with the same weight as a smaller area will have
         * more effect in the metering result. Metering areas can partially overlap and the camera
         * device will add the weights in the overlap region.
         *
         * The weights are relative to weights of other white balance metering regions, so if only
         * one region is used, all non-zero weights will have the same effect. A region with 0
         * weight is ignored.
         *
         * If all regions have 0 weight, then no specific metering area needs to be used by the
         * camera device.
         *
         * If the metering region is outside the used android.scaler.cropRegion returned in capture
         * result metadata, the camera device will ignore the sections outside the crop region and
         * output only the intersection rectangle as the metering region in the result metadata.
         * If the region is entirely outside the crop region, it will be ignored and not reported
         * in the result metadata.
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

        String string = "Level 06 (Auto-white balance)\n";
        string += "CaptureRequest.CONTROL_AWB_MODE:    " + mControlAwbModeName    + "\n";
        string += "CaptureRequest.CONTROL_AWB_LOCK:    " + mControlAwbLockName    + "\n";
        string += "CaptureRequest.CONTROL_AWB_REGIONS: " + mControlAwbRegionsName + "\n";

        stringList.add(string);
        return stringList;
    }

}
