package recycle_bin.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level01_Abilities extends Level00_Hardware {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected boolean mIsBackwardCompatibleAble        = false; // Added in API 21
    protected boolean mIsManualSensorAble              = false; // Added in API 21
    protected boolean mIsManualPostProcessingAble      = false; // Added in API 21
    protected boolean mIsRawAble                       = false; // Added in API 21

    protected boolean mIsReadSensorSettingsAble        = false; // Added in API 22
    protected boolean mIsBurstCaptureAble              = false; // Added in API 22

    protected boolean mIsPrivateReprocessingAble       = false; // Added in API 23
    protected boolean mIsYuvReprocessingAble           = false; // Added in API 23
    protected boolean mIsDepthOutputAble               = false; // Added in API 23
    protected boolean mIsConstrainedHighSpeedVideoAble = false; // Added in API 23

    protected boolean mIsMotionTrackingAble            = false; // Added in API 28
    protected boolean mIsLogicalMultiCameraAble        = false; // Added in API 28
    protected boolean mIsMonochromeAble                = false; // Added in API 28

    protected boolean mHasUnknownAbility               = false; // Catch all

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level01_Abilities(@NonNull CameraCharacteristics characteristics) {
        super(characteristics);
        mCameraCharacteristics = characteristics;
        getCapabilities();
    }

    //----------------------------------------------------------------------------------------------

    /*
     * Documentation provided by:
     * https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html
     * https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
     */

    /**
     *
     */
    private void getCapabilities() {
        CameraCharacteristics.Key key = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
        int[] capabilities = (int[]) mCameraCharacteristics.get(key);

        assert capabilities != null;
        for (int code : capabilities) {
            switch (code) {

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE): {
                    mIsBackwardCompatibleAble = true;
                    /*
                     * Added in API 21
                     *
                     * The minimal set of capabilities that every camera device (regardless of
                     * android.info.supportedHardwareLevel) supports.
                     * This capability is listed by all normal devices, and indicates that the
                     * camera device has a feature set that's comparable to the baseline
                     * requirements for the older android.hardware.Camera API.
                     *
                     * Devices with the DEPTH_OUTPUT capability might not list this capability,
                     * indicating that they support only depth measurement,
                     * not standard color output.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR): {
                    mIsManualSensorAble = true;
                    /*
                     * Added in API 21
                     *
                     * The camera device can be manually controlled
                     * (3A algorithms such as auto-exposure, and auto-focus can be bypassed).
                     * The camera device supports basic manual control of the sensor image
                     * acquisition related stages. This means the following controls are
                     * guaranteed to be supported:
                     *
                     *      Manual frame duration control:
                     *          android.sensor.frameDuration
                     *          android.sensor.info.maxFrameDuration
                     *
                     *      Manual exposure control:
                     *          android.sensor.exposureTime
                     *          android.sensor.info.exposureTimeRange
                     *
                     *      Manual sensitivity control:
                     *          android.sensor.sensitivity
                     *          android.sensor.info.sensitivityRange
                     *
                     *      Manual lens control (if the lens is adjustable):
                     *          android.lens.*
                     *
                     *      Manual flash control (if a flash unit is present):
                     *          android.flash.*
                     *
                     *      Manual black level locking:
                     *          android.blackLevel.lock
                     *
                     *      Auto exposure lock:
                     *          android.control.aeLock
                     *
                     * If any of the above 3A algorithms are enabled, then the camera device
                     * will accurately report the values applied by 3A in the result.
                     *
                     * A given camera device may also support additional manual sensor controls,
                     * but this capability only covers the above list of controls.
                     *
                     * If this is supported, android.scaler.streamConfigurationMap will
                     * additionally return a min frame duration that is greater than zero
                     * for each supported size-format combination.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING): {
                    mIsManualPostProcessingAble = true;
                    /*
                     * Added in API 21
                     *
                     * The camera device post-processing stages can be manually controlled.
                     * The camera device supports basic manual control of the image
                     * post-processing stages.
                     * This means the following controls are guaranteed to be supported:
                     *
                     *      Manual tonemap control:
                     *          android.tonemap.curve
                     *          android.tonemap.mode
                     *          android.tonemap.maxCurvePoints
                     *          android.tonemap.gamma
                     *          android.tonemap.presetCurve
                     *
                     *      Manual white balance control:
                     *          android.colorCorrection.transform
                     *          android.colorCorrection.gains
                     *
                     *      Manual lens shading map control:
                     *          android.shading.mode
                     *          android.statistics.lensShadingMapMode
                     *          android.statistics.lensShadingMap
                     *          android.lens.info.shadingMapSize
                     *
                     *      Manual aberration correction control
                     *      (if aberration correction is supported):
                     *          android.colorCorrection.aberrationMode
                     *          android.colorCorrection.availableAberrationModes
                     *
                     *      Auto white balance lock:
                     *          android.control.awbLock
                     *
                     * If auto white balance is enabled, then the camera device will accurately
                     * report the values applied by AWB in the result.
                     *
                     * A given camera device may also support additional post-processing
                     * controls, but this capability only covers the above list of controls.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW): {
                    mIsRawAble = true;
                    /*
                     * Added in API 21
                     *
                     * The camera device supports outputting RAW buffers and metadata for
                     * interpreting them.
                     *
                     * Devices supporting the RAW capability allow both for saving DNG files,
                     * and for direct application processing of raw sensor images.
                     *
                     *      RAW_SENSOR is supported as an output format.
                     *
                     *      The maximum available resolution for RAW_SENSOR streams will match
                     *      either the value in android.sensor.info.pixelArraySize or
                     *      android.sensor.info.preCorrectionActiveArraySize.
                     *
                     *      All DNG-related optional metadata entries are provided
                     *      by the camera device.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS): {
                    mIsReadSensorSettingsAble = true;
                    /*
                     * Added in API 22
                     *
                     * The camera device supports accurately reporting the sensor settings for
                     * many of the sensor controls while the built-in 3A algorithm is running.
                     * This allows reporting of sensor settings even when these settings
                     * cannot be manually changed.
                     *
                     * The values reported for the following controls are guaranteed to be
                     * available in the CaptureResult, including when 3A is enabled:
                     *
                     *      Exposure control:
                     *          android.sensor.exposureTime
                     *
                     *      Sensitivity control:
                     *          android.sensor.sensitivity
                     *
                     *      Lens controls (if the lens is adjustable):
                     *          android.lens.focusDistance
                     *          android.lens.aperture
                     *
                     * This capability is a subset of the MANUAL_SENSOR control capability,
                     * and will always be included if the MANUAL_SENSOR capability is available.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE): {
                    mIsBurstCaptureAble = true;
                    /*
                     * Added in API 22
                     *
                     * The camera device supports capturing high-resolution images at
                     * >= 20 frames per second, in at least the uncompressed YUV format,
                     * when post-processing settings are set to FAST.
                     * Additionally, maximum-resolution images can be captured at
                     * >= 10 frames per second. Here, 'high resolution' means
                     * at least 8 megapixels, or the maximum resolution of the device,
                     * whichever is smaller.
                     *
                     * More specifically, this means that a size matching the camera device's
                     * active array size is listed as a supported size for the
                     * ImageFormat.YUV_420_888 format in either
                     * StreamConfigurationMap.getOutputSizes(int) or
                     * StreamConfigurationMap.getHighResolutionOutputSizes(int),
                     * with a minimum frame duration for that format and size of
                     * either <= 1/20 s, or <= 1/10 s, respectively; and
                     * the android.control.aeAvailableTargetFpsRanges entry lists at least
                     * one FPS range where the minimum FPS is >= 1 / minimumFrameDuration
                     * for the maximum-size YUV_420_888 format.
                     * If that maximum size is listed in
                     * StreamConfigurationMap.getHighResolutionOutputSizes(int),
                     * then the list of resolutions for YUV_420_888 from
                     * StreamConfigurationMap.getOutputSizes(int)
                     * contains at least one resolution >= 8 megapixels,
                     * with a minimum frame duration of <= 1/20 s.
                     *
                     * If the device supports the ImageFormat.RAW10, ImageFormat.RAW12,
                     * then those can also be captured at the same rate as
                     * the maximum-size YUV_420_888 resolution is.
                     *
                     * If the device supports the PRIVATE_REPROCESSING capability,
                     * then the same guarantees as for the YUV_420_888 format also
                     * apply to the ImageFormat.PRIVATE format.
                     *
                     * In addition, the android.sync.maxLatency field is guaranted to
                     * have a value between 0 and 4, inclusive.
                     * android.control.aeLockAvailable and
                     * android.control.awbLockAvailable are also guaranteed to be true so
                     * burst capture with these two locks ON yields consistent image output.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING): {
                    mIsPrivateReprocessingAble = true;
                    /*
                     * Added in API 23
                     *
                     * The camera device supports the Zero Shutter Lag reprocessing use case.
                     *
                     *      One input stream is supported, that is,
                     *      android.request.maxNumInputStreams == 1.
                     *
                     *      ImageFormat.PRIVATE is supported as an output/input format, that is,
                     *      ImageFormat.PRIVATE is included in the lists of formats returned by
                     *      StreamConfigurationMap.getInputFormats() and
                     *      StreamConfigurationMap.getOutputFormats().
                     *
                     *      StreamConfigurationMap.getValidOutputFormatsForInput(int)
                     *      returns non empty int[] for each supported input format returned by
                     *      StreamConfigurationMap.getInputFormats().
                     *
                     *      Each size returned by getInputSizes(ImageFormat.PRIVATE) is also
                     *      included in getOutputSizes(ImageFormat.PRIVATE)
                     *
                     *      Using ImageFormat.PRIVATE does not cause a frame rate drop
                     *      relative to the sensor's maximum capture rate (at that resolution).
                     *
                     *      ImageFormat.PRIVATE will be reprocessable into both
                     *      ImageFormat.YUV_420_888 and ImageFormat.JPEG formats.
                     *
                     *      The maximum available resolution for PRIVATE streams
                     *      (both input/output) will match the maximum available resolution
                     *      of JPEG streams.
                     *
                     *      Static metadata android.reprocess.maxCaptureStall.
                     *
                     *      Only below controls are effective for reprocessing requests and
                     *      will be present in capture results, other controls in reprocess
                     *      requests will be ignored by the camera device.
                     *          android.jpeg.*
                     *          android.noiseReduction.mode
                     *          android.edge.mode
                     *
                     *      android.noiseReduction.availableNoiseReductionModes and
                     *      android.edge.availableEdgeModes will both list ZERO_SHUTTER_LAG as
                     *      a supported mode.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING): {
                    mIsYuvReprocessingAble = true;
                    /*
                     * Added in API 23
                     *
                     * The camera device supports the YUV_420_888 reprocessing use case,
                     * similar as PRIVATE_REPROCESSING, This capability requires the camera
                     * device to support the following:
                     *
                     *      One input stream is supported, that is,
                     *      android.request.maxNumInputStreams == 1.
                     *
                     *      ImageFormat.YUV_420_888 is supported as an output/input format,
                     *      that is, YUV_420_888 is included in the lists of formats returned
                     *      by StreamConfigurationMap.getInputFormats() and
                     *      StreamConfigurationMap.getOutputFormats().
                     *
                     *      StreamConfigurationMap.getValidOutputFormatsForInput(int) returns
                     *      non-empty int[] for each supported input format returned by
                     *      StreamConfigurationMap.getInputFormats().
                     *
                     *      Each size returned by getInputSizes(YUV_420_888) is also included
                     *      in getOutputSizes(YUV_420_888)
                     *
                     *      Using ImageFormat.YUV_420_888 does not cause a frame rate drop
                     *      relative to the sensor's maximum capture rate (at that resolution).
                     *
                     *      ImageFormat.YUV_420_888 will be reprocessable into both
                     *      ImageFormat.YUV_420_888 and ImageFormat.JPEG formats.
                     *
                     *      The maximum available resolution for ImageFormat.YUV_420_888 streams
                     *      (both input/output) will match the maximum available resolution
                     *      of ImageFormat.JPEG streams.
                     *
                     *      Static metadata android.reprocess.maxCaptureStall.
                     *
                     *      Only the below controls are effective for reprocessing requests and
                     *      will be present in capture results. The reprocess requests are
                     *      from the original capture results that are associated with the
                     *      intermediate ImageFormat.YUV_420_888 output buffers.
                     *      All other controls in the reprocess requests will be
                     *      ignored by the camera device:
                     *          android.jpeg.*
                     *          android.noiseReduction.mode
                     *          android.edge.mode
                     *          android.reprocess.effectiveExposureFactor
                     *          android.noiseReduction.availableNoiseReductionModes and
                     *              android.edge.availableEdgeModes will both list ZERO_SHUTTER_LAG
                     *              as a supported mode.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT): {
                    mIsDepthOutputAble = true;
                    /*
                     * Added in API 23
                     *
                     * The camera device can produce depth measurements from its field of view.
                     *
                     * This capability requires the camera device to support the following:
                     *
                     *      ImageFormat.DEPTH16 is supported as an output format.
                     *
                     *      ImageFormat.DEPTH_POINT_CLOUD is optionally supported as an
                     *      output format.
                     *
                     *      This camera device, and all camera devices with the
                     *              same android.lens.facing, will list the following
                     *              calibration metadata entries in both CameraCharacteristics
                     *              and CaptureResult:
                     *
                     *              android.lens.poseTranslation
                     *              android.lens.poseRotation
                     *              android.lens.intrinsicCalibration
                     *              android.lens.distortion
                     *
                     *      The android.depth.depthIsExclusive entry is listed by this device.
                     *
                     *      As of Android P, the android.lens.poseReference entry is listed
                     *      by this device.
                     *
                     *      A LIMITED camera with only the DEPTH_OUTPUT capability does not
                     *              have to support normal YUV_420_888, JPEG,
                     *              and PRIV-format outputs. It only has to support
                     *              the DEPTH16 format.
                     *
                     * Generally, depth output operates at a slower frame rate than standard
                     * color capture, so the DEPTH16 and DEPTH_POINT_CLOUD formats will
                     * commonly have a stall duration that should be accounted for
                     * (see StreamConfigurationMap.getOutputStallDuration(int, Size)).
                     * On a device that supports both depth and color-based output, to
                     * enable smooth preview, using a repeating burst is recommended,
                     * where a depth-output target is only included once every N frames,
                     * where N is the ratio between preview output rate and depth output rate,
                     * including depth stall time.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO): {
                    mIsConstrainedHighSpeedVideoAble = true;
                    /*
                     * Added in API 23
                     *
                     * The device supports constrained high speed video recording
                     * (frame rate >=120fps) use case. The camera device will support
                     * high speed capture session created by
                     * CameraDevice.createConstrainedHighSpeedCaptureSession(List,
                     *                          CameraCaptureSession.StateCallback, Handler),
                     * which only accepts high speed request lists created by
                     * CameraConstrainedHighSpeedCaptureSession.createHighSpeedRequestList(
                     *                          CaptureRequest).
                     *
                     * A camera device can still support high speed video streaming by
                     * advertising the high speed FPS ranges in
                     * android.control.aeAvailableTargetFpsRanges.
                     * For this case, all normal capture request per frame control and
                     * synchronization requirements will apply to the high speed fps ranges,
                     * the same as all other fps ranges. This capability describes the
                     * capability of a specialized operating mode with many limitations
                     * (see below), which is only targeted at high speed video recording.
                     *
                     * The supported high speed video sizes and fps ranges are specified in
                     * StreamConfigurationMap.getHighSpeedVideoFpsRanges().
                     * To get desired output frame rates, the application is only allowed
                     * to select video size and FPS range combinations provided by
                     * StreamConfigurationMap.getHighSpeedVideoSizes().
                     * The fps range can be controlled via android.control.aeTargetFpsRange.
                     *
                     * In this capability, the camera device will override
                     * aeMode, awbMode, and afMode to ON, AUTO, and CONTINUOUS_VIDEO,
                     * respectively. All post-processing block mode controls will be
                     * overridden to be FAST.
                     * Therefore, no manual control of capture and post-processing
                     * parameters is possible.
                     * All other controls operate the same as when android.control.mode == AUTO.
                     * This means that all other android.control.* fields
                     * continue to work, such as:
                     *
                     *      android.control.aeTargetFpsRange
                     *      android.control.aeExposureCompensation
                     *      android.control.aeLock
                     *      android.control.awbLock
                     *      android.control.effectMode
                     *      android.control.aeRegions
                     *      android.control.afRegions
                     *      android.control.awbRegions
                     *      android.control.afTrigger
                     *      android.control.aePrecaptureTrigger
                     *
                     * Outside of android.control.*, the following controls will work:
                     *
                     *      android.flash.mode
                     *          (TORCH mode only, automatic flash for still capture will
                     *          not work since aeMode is ON)
                     *      android.lens.opticalStabilizationMode (if it is supported)
                     *      android.scaler.cropRegion
                     *      android.statistics.faceDetectMode (if it is supported)
                     *
                     * For high speed recording use case, the actual maximum supported
                     * frame rate may be lower than what camera can output, depending on
                     * the destination Surfaces for the image data.
                     * For example, if the destination surfaces is from video encoder,
                     * the application need check if the video encoder is capable of
                     * supporting the high frame rate for a given video size, or it will end
                     * up with lower recording frame rate.
                     * If the destination surfaces is from preview window, the actual preview
                     * frame rate will be bounded by the screen refresh rate.
                     *
                     * The camera device will only support up to 2 high speed simultaneous
                     * output surfaces (preview and recording surfaces) in this mode.
                     * Above controls will be effective only
                     * if all of below conditions are true:
                     *
                     *      The application creates a camera capture session with no more
                     *          than 2 surfaces via
                     *          CameraDevice.createConstrainedHighSpeedCaptureSession(List,
                     *                          CameraCaptureSession.StateCallback, Handler).
                     *          The targeted surfaces must be preview surfaces
                     *          (either from SurfaceView or SurfaceTexture) or
                     *          recording surfaces(either from MediaRecorder.getSurface() or
                     *          MediaCodec.createInputSurface()).
                     *
                     *      The stream sizes are selected from the sizes reported by
                     *          StreamConfigurationMap.getHighSpeedVideoSizes().
                     *
                     *      The FPS ranges are selected from
                     *          StreamConfigurationMap.getHighSpeedVideoFpsRanges().
                     *
                     * When above conditions are NOT satistied,
                     * CameraDevice.createConstrainedHighSpeedCaptureSession(List,
                     *                  CameraCaptureSession.StateCallback, Handler) will fail.
                     *
                     * Switching to a FPS range that has different maximum FPS may trigger
                     * some camera device reconfigurations, which may introduce extra latency.
                     * It is recommended that the application avoids unnecessary maximum
                     * target FPS changes as much as possible during high speed streaming.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING): {
                    mIsMotionTrackingAble = true;
                    /*
                     * Added in API 28
                     *
                     * The camera device supports the MOTION_TRACKING value for
                     * android.control.captureIntent, which limits maximum exposure time to 20 ms.
                     *
                     * This limits the motion blur of capture images, resulting in better image
                     * tracking results for use cases such as image stabilization or
                     * augmented reality.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA): {
                    mIsLogicalMultiCameraAble = true;
                    /*
                     * Added in API 28
                     *
                     * The camera device is a logical camera backed by two or more physical
                     * cameras that are also exposed to the application.
                     *
                     * Camera application shouldn't assume that there are at most 1 rear
                     * camera and 1 front camera in the system.
                     * For an application that switches between front and back cameras,
                     * the recommendation is to switch between the first rear camera and
                     * the first front camera in the list of supported camera devices.
                     *
                     * This capability requires the camera device to support the following:
                     *
                     *      The IDs of underlying physical cameras are returned via
                     *      CameraCharacteristics.getPhysicalCameraIds().
                     *
                     *      This camera device must list static metadata
                     *      android.logicalMultiCamera.sensorSyncType in CameraCharacteristics.
                     *
                     *      The underlying physical cameras' static metadata must list the
                     *      following entries, so that the application can correlate
                     *      pixels from the physical streams:
                     *
                     *          android.lens.poseReference
                     *          android.lens.poseRotation
                     *          android.lens.poseTranslation
                     *          android.lens.intrinsicCalibration
                     *          android.lens.distortion
                     *
                     *      The SENSOR_INFO_TIMESTAMP_SOURCE of the logical device and physical
                     *      devices must be the same.
                     *
                     *      The logical camera device must be LIMITED or higher device.
                     *
                     * Both the logical camera device and its underlying physical devices
                     * support the mandatory stream combinations required for their device levels.
                     *
                     * Additionally, for each guaranteed stream combination,
                     * the logical camera supports:
                     *
                     *      For each guaranteed stream combination, the logical camera supports
                     *      replacing one logical YUV_420_888 or raw stream with two physical
                     *      streams of the same size and format, each from a separate physical
                     *      camera, given that the size and format are supported by both
                     *      physical cameras.
                     *
                     *      If the logical camera doesn't advertise RAW capability, but the
                     *      underlying physical cameras do, the logical camera will support
                     *      guaranteed stream combinations for RAW capability, except that
                     *      the RAW streams will be physical streams, each from a separate
                     *      physical camera. This is usually the case when the physical
                     *      cameras have different sensor sizes.
                     *
                     * Using physical streams in place of a logical stream of the same size
                     * and format will not slow down the frame rate of the capture,
                     * as long as the minimum frame duration of the physical and logical
                     * streams are the same.
                     */
                    break;
                }

                case (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME): {
                    mIsMonochromeAble = true;
                    /*
                     * Added in API 28
                     *
                     * The camera device is a monochrome camera that doesn't contain a color
                     * filter array, and the pixel values on U and V planes are all 128.
                     */
                    break;
                }

                default : {
                    mHasUnknownAbility = true;
                }
            }
        }

    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @param bool
     * @return
     */
    protected String bool2YN(boolean bool) {
        if (bool) {
            return "Yes";
        }
        else {
            return "No";
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

        String string = "Level 01 (Abilities)\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE:          " + bool2YN(mIsBackwardCompatibleAble)        + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR:                " + bool2YN(mIsManualSensorAble)              + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING:       " + bool2YN(mIsManualPostProcessingAble)      + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW:                          " + bool2YN(mIsRawAble)                       + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS:         " + bool2YN(mIsReadSensorSettingsAble)        + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE:                " + bool2YN(mIsBurstCaptureAble)              + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING:         " + bool2YN(mIsPrivateReprocessingAble)       + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING:             " + bool2YN(mIsYuvReprocessingAble)           + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT:                 " + bool2YN(mIsDepthOutputAble)               + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO: " + bool2YN(mIsConstrainedHighSpeedVideoAble) + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING:              " + bool2YN(mIsMotionTrackingAble)            + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA:         " + bool2YN(mIsLogicalMultiCameraAble)        + "\n";
        string += "CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME:                   " + bool2YN(mIsMonochromeAble)                + "\n";
        string += "Unknown capabilities:                                                              " + bool2YN(mHasUnknownAbility)               + "\n";

        stringList.add(string);
        return stringList;
    }

}