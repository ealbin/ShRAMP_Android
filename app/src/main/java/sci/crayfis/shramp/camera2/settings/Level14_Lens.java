package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.List;

@TargetApi(21)
abstract class Level14_Lens extends Level13_JPEG {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Float   mLensAperture;
    private   String  mLensApertureName;

    protected Float   mLensFilterDensity;
    private   String  mLensFilterDensityName;

    protected Float   mLensFocalLength;
    private   String  mLensFocalLengthName;

    protected Float   mLensFocusDistance;
    private   String  mLensFocusDistanceName;

    protected Integer mLensOpticalStabilizationMode;
    private   String  mLensOpticalStabilizationModeName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level14_Lens(@NonNull CameraCharacteristics characteristics,
                           @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setLensAperture();
        setLensFilterDensity();
        setLensFocalLength();
        setLensFocusDistance();
        setLensOpticalStabilizationMode();
    }

    //----------------------------------------------------------------------------------------------

    /*
     * Documentation provided by:
     * https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
     * https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
     */

    private void setLensAperture() {
        CaptureRequest.Key key = CaptureRequest.LENS_APERTURE;
        /*
         * Added in API 21
         *
         * The desired lens aperture size, as a ratio of lens focal length to the effective aperture
         * diameter.
         *
         * Setting this value is only supported on the camera devices that have a variable aperture
         * lens.
         *
         * When this is supported and android.control.aeMode is OFF, this can be set along with
         * android.sensor.exposureTime, android.sensor.sensitivity, and android.sensor.frameDuration
         * to achieve manual exposure control.
         *
         * The requested aperture value may take several frames to reach the requested value; the
         * camera device will report the current (intermediate) aperture size in capture result
         * metadata while the aperture is changing. While the aperture is still changing,
         * android.lens.state will be set to MOVING.
         *
         * When this is supported and android.control.aeMode is one of the ON modes, this will be
         * overridden by the camera device auto-exposure algorithm, the overridden values are then
         * provided back to the user in the corresponding result.
         *
         * Units: The f-number (f/N)
         *
         * Range of valid values:
         * android.lens.info.availableApertures
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mLensAperture     = null;
            mLensApertureName = "Not supported";
            return;
        }

        if (super.mControlAeMode != null
                && super.mControlAeMode != CameraMetadata.CONTROL_AE_MODE_OFF) {
            mLensAperture     = null;
            mLensApertureName = "Disabled";
            return;
        }

        float[] lensApertures = super.mCameraCharacteristics.get(
                                CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
        if (lensApertures == null) {
            mLensAperture     = null;
            mLensApertureName = "Not supported";
            return;
        }

        Float minAperture = null;
        for (float aperture : lensApertures) {
            if (minAperture == null) {
                minAperture = aperture;
                continue;
            }

            if (aperture < minAperture) {
                minAperture = aperture;
            }
        }

        mLensAperture     = minAperture;
        DecimalFormat df  = new DecimalFormat("#.##");
        mLensApertureName = df.format(mLensAperture) + " [f/N]";

        super.mCaptureRequestBuilder.set(key, mLensAperture);
    }

    //----------------------------------------------------------------------------------------------

    private void setLensFilterDensity() {
        CaptureRequest.Key key = CaptureRequest.LENS_FILTER_DENSITY;
        /*
         * Added in API 21
         *
         * The desired setting for the lens neutral density filter(s).
         *
         * This control will not be supported on most camera devices.
         *
         * Lens filters are typically used to lower the amount of light the sensor is exposed to
         * (measured in steps of EV). As used here, an EV step is the standard logarithmic
         * representation, which are non-negative, and inversely proportional to the amount of light
         * hitting the sensor. For example, setting this to 0 would result in no reduction of the
         * incoming light, and setting this to 2 would mean that the filter is set to reduce
         * incoming light by two stops (allowing 1/4 of the prior amount of light to the sensor).
         *
         * It may take several frames before the lens filter density changes to the requested value.
         * While the filter density is still changing, android.lens.state will be set to MOVING.
         *
         * Units: Exposure Value (EV)
         *
         * Range of valid values:
         * android.lens.info.availableFilterDensities
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mLensFilterDensity     = null;
            mLensFilterDensityName = "Not supported";
            return;
        }

        float[] lensDensities = super.mCameraCharacteristics.get(
                                CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES);
        if (lensDensities == null) {
            mLensFilterDensity     = null;
            mLensFilterDensityName = "Not supported";
            return;
        }

        Float maxDensity = null;
        for (float density : lensDensities) {
            if (maxDensity == null) {
                maxDensity = density;
                continue;
            }

            if (density > maxDensity) {
                maxDensity = density;
            }
        }

        mLensFilterDensity      = maxDensity;
        DecimalFormat df        = new DecimalFormat("#.##");
        mLensFilterDensityName  = df.format(mLensFilterDensity) + " [EV]";

        super.mCaptureRequestBuilder.set(key, mLensFilterDensity);
    }

    //----------------------------------------------------------------------------------------------

    private void setLensFocalLength() {
        CaptureRequest.Key key = CaptureRequest.LENS_FOCAL_LENGTH;
        /*
         * Added in API 21
         *
         * The desired lens focal length; used for optical zoom.
         *
         * This setting controls the physical focal length of the camera device's lens. Changing the
         * focal length changes the field of view of the camera device, and is usually used for
         * optical zoom.
         *
         * Like android.lens.focusDistance and android.lens.aperture, this setting won't be applied
         * instantaneously, and it may take several frames before the lens can change to the
         * requested focal length. While the focal length is still changing, android.lens.state will
         * be set to MOVING.
         *
         * Optical zoom will not be supported on most devices.
         *
         * Units: Millimeters
         *
         * Range of valid values:
         * android.lens.info.availableFocalLengths
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mLensFocalLength     = null;
            mLensFocalLengthName = "Not supported";
            return;
        }

        float[] focalLengths = super.mCameraCharacteristics.get(
                               CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        if (focalLengths == null) {
            mLensFocalLength     = null;
            mLensFocalLengthName = "Not supported";
            return;
        }

        Float maxFocalLength = null;
        for (float length : focalLengths) {
            if (maxFocalLength == null) {
                maxFocalLength = length;
                continue;
            }

            if (length > maxFocalLength) {
                maxFocalLength = length;
            }
        }

        mLensFocalLength     = maxFocalLength;
        DecimalFormat df     = new DecimalFormat("#.##");
        mLensFocalLengthName = df.format(mLensFocalLength) + " [mm]";

        super.mCaptureRequestBuilder.set(key, mLensFocalLength);
    }

    //----------------------------------------------------------------------------------------------

    private void setLensFocusDistance() {
        CaptureRequest.Key key = CaptureRequest.LENS_FOCUS_DISTANCE;
        /*
         * Added in API 21
         *
         * Desired distance to plane of sharpest focus, measured from frontmost surface of the lens.
         *
         * This control can be used for setting manual focus, on devices that support the
         * MANUAL_SENSOR capability and have a variable-focus lens
         * (see android.lens.info.minimumFocusDistance).
         *
         * A value of 0.0f means infinity focus. The value set will be clamped to
         * [0.0f, android.lens.info.minimumFocusDistance].
         *
         * Like android.lens.focalLength, this setting won't be applied instantaneously, and it may
         * take several frames before the lens can move to the requested focus distance. While the
         * lens is still moving, android.lens.state will be set to MOVING.
         *
         * LEGACY devices support at most setting this to 0.0f for infinity focus.
         *
         * Units: See android.lens.info.focusDistanceCalibration for details
         *
         * Range of valid values:
         * >= 0
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key) || !super.mIsManualSensorAble) {
            mLensFocusDistance     = null;
            mLensFocusDistanceName = "Not supported";
            return;
        }

        float focusInfinity = 0.f;

        mLensFocusDistance     = focusInfinity;
        DecimalFormat df       = new DecimalFormat("#.##");
        mLensFocusDistanceName = "Infinity";

        super.mCaptureRequestBuilder.set(key, mLensFocusDistance);
    }

    //----------------------------------------------------------------------------------------------

    private void setLensOpticalStabilizationMode() {
        CaptureRequest.Key key = CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE;
        /*
         * Added in API 21
         *
         * Sets whether the camera device uses optical image stabilization (OIS) when capturing
         * images.
         *
         * OIS is used to compensate for motion blur due to small movements of the camera during
         * capture. Unlike digital image stabilization (android.control.videoStabilizationMode),
         * OIS makes use of mechanical elements to stabilize the camera sensor, and thus allows for
         * longer exposure times before camera shake becomes apparent.
         *
         * Switching between different optical stabilization modes may take several frames to
         * initialize, the camera device will report the current mode in capture result metadata.
         * For example, When "ON" mode is requested, the optical stabilization modes in the first
         * several capture results may still be "OFF", and it will become "ON" when the
         * initialization is done.
         *
         * If a camera device supports both OIS and digital image stabilization
         * (android.control.videoStabilizationMode), turning both modes on may produce undesirable
         * interaction, so it is recommended not to enable both at the same time.
         *
         * Not all devices will support OIS; see android.lens.info.availableOpticalStabilization for
         * available controls.
         *
         * Available values for this device:
         * android.lens.info.availableOpticalStabilization
         *
         * Optional - This value may be null on some devices.
         *
         * Limited capability - Present on all camera devices that report being at least
         * HARDWARE_LEVEL_LIMITED devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mLensOpticalStabilizationMode     = null;
            mLensOpticalStabilizationModeName = "Not supported";
            return;
        }

        mLensOpticalStabilizationMode     = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF;
        mLensOpticalStabilizationModeName = "Off";
        /*
         * Added in API 21
         *
         * Optical stabilization is unavailable.
         */

        super.mCaptureRequestBuilder.set(key, mLensOpticalStabilizationMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 14 (Lens)\n";
        string += "CaptureRequest.LENS_APERTURE:                   " + mLensApertureName                 + "\n";
        string += "CaptureRequest.LENS_FILTER_DENSITY:             " + mLensFilterDensityName            + "\n";
        string += "CaptureRequest.LENS_FOCAL_LENGTH:               " + mLensFocalLengthName              + "\n";
        string += "CaptureRequest.LENS_FOCUS_DISTANCE:             " + mLensFocusDistanceName            + "\n";
        string += "CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE: " + mLensOpticalStabilizationModeName + "\n";

        stringList.add(string);
        return stringList;
    }

}
