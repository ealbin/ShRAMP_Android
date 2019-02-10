package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.support.annotation.NonNull;

@TargetApi(21)
abstract class Level0_Hardware {

    static protected enum HardwareLevel {
        LEGACY,   // Added in API 21
        LIMITED,  // Added in API 21
        FULL,     // Added in API 21
        LEVEL_3,  // Added in API 24
        EXTERNAL, // Added in API 28
        UNKNOWN
    }

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected CameraCharacteristics mCameraCharacteristics;
    protected HardwareLevel         mHardwareLevel;
    private   String                mHardwareLevelName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    private Level0_Hardware() {}

    protected Level0_Hardware(@NonNull CameraCharacteristics characteristics) {
        mCameraCharacteristics = characteristics;
        getHardwareLevel();
    }

    /**
     *
     */
    private void getHardwareLevel() {
        /*
         * Added in API 21
         *
         * Generally classifies the overall set of the camera device functionality.
         *
         * The supported hardware level is a high-level description of the camera device's
         * capabilities, summarizing several capabilities into one field. Each level adds additional
         * features to the previous one, and is always a strict superset of the previous level.
         * The ordering is LEGACY < LIMITED < FULL < LEVEL_3.
         *
         * Starting from LEVEL_3, the level enumerations are guaranteed to be in increasing
         * numerical value as well.
         *
         * At a high level, the levels are:
         *
         *      LEGACY devices operate in a backwards-compatibility mode for older Android devices,
         *      and have very limited capabilities.
         *
         *      LIMITED devices represent the baseline feature set, and may also include additional
         *      capabilities that are subsets of FULL.
         *
         *      FULL devices additionally support per-frame manual control of sensor, flash, lens
         *      and post-processing settings, and image capture at a high rate.
         *
         *      LEVEL_3 devices additionally support YUV reprocessing and RAW image capture,
         *      along with additional output stream configurations.
         *
         *      EXTERNAL devices are similar to LIMITED devices with exceptions like some sensor or
         *      lens information not reorted or less stable framerates.
         *
         * See the individual level enums for full descriptions of the supported capabilities.
         * The android.request.availableCapabilities entry describes the device's capabilities at a
         * finer-grain level, if needed. In addition, many controls have their available settings or
         * ranges defined in individual entries from CameraCharacteristics.
         *
         * Some features are not part of any particular hardware level or capability and must be
         * queried separately. These include:
         *
         *      Calibrated timestamps (android.sensor.info.timestampSource == REALTIME)
         *
         *      Precision lens control (android.lens.info.focusDistanceCalibration == CALIBRATED)
         *
         *      Face detection (android.statistics.info.availableFaceDetectModes)
         *
         *      Optical or electrical image stabilization
         *      (android.lens.info.availableOpticalStabilization,
         *      android.control.availableVideoStabilizationModes)
         *
         * This key is available on all devices.
         */
        CameraCharacteristics.Key key = CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;
        Integer hardwareLevel = (Integer) mCameraCharacteristics.get(key);
        assert hardwareLevel != null;

        switch (hardwareLevel) {
            case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY): {
                mHardwareLevel     = HardwareLevel.LEGACY;
                mHardwareLevelName = "LEGACY";
                /*
                 * Added in API 21
                 *
                 * This camera device is running in backward compatibility mode.
                 *
                 * Only the stream configurations listed in the LEGACY table in the
                 * createCaptureSession documentation are supported.
                 *
                 * A LEGACY device does not support per-frame control, manual sensor control,
                 * manual post-processing, arbitrary cropping regions, and has relaxed performance
                 * constraints. No additional capabilities beyond BACKWARD_COMPATIBLE will ever be
                 * listed by a LEGACY device in android.request.availableCapabilities.
                 *
                 * In addition, the android.control.aePrecaptureTrigger is not functional on LEGACY
                 * devices. Instead, every request that includes a JPEG-format output target is
                 * treated as triggering a still capture, internally executing a precapture trigger.
                 * This may fire the flash for flash power metering during precapture, and then fire
                 * the flash for the final capture, if a flash is available on the device and the
                 * AE mode is set to enable the flash.
                 */
                break;
            }
            case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED): {
                mHardwareLevel     = HardwareLevel.LIMITED;
                mHardwareLevelName = "LIMITED";
                /*
                 * Added in API 21
                 *
                 * This camera device does not have enough capabilities to qualify as a FULL device
                 * or better.
                 *
                 * Only the stream configurations listed in the LEGACY and LIMITED tables in the
                 * createCaptureSession documentation are guaranteed to be supported.
                 *
                 * All LIMITED devices support the BACKWARDS_COMPATIBLE capability, indicating basic
                 * support for color image capture. The only exception is that the device may
                 * alternatively support only the DEPTH_OUTPUT capability, if it can only output
                 * depth measurements and not color images.
                 *
                 * LIMITED devices and above require the use of android.control.aePrecaptureTrigger
                 * to lock exposure metering (and calculate flash power, for cameras with flash)
                 * before capturing a high-quality still image.
                 *
                 * A LIMITED device that only lists the BACKWARDS_COMPATIBLE capability is only
                 * required to support full-automatic operation and post-processing (OFF is not
                 * supported for android.control.aeMode, android.control.afMode,
                 * or android.control.awbMode)
                 *
                 * Additional capabilities may optionally be supported by a LIMITED-level device,
                 * and can be checked for in android.request.availableCapabilities.
                 */
                break;
            }
            case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL): {
                mHardwareLevel     = HardwareLevel.FULL;
                mHardwareLevelName = "FULL";
                /*
                 * Added in API 21
                 *
                 * This camera device is capable of supporting advanced imaging applications.
                 *
                 * The stream configurations listed in the FULL, LEGACY and LIMITED tables in the
                 * createCaptureSession documentation are guaranteed to be supported.
                 *
                 * A FULL device will support below capabilities:
                 *
                 *      BURST_CAPTURE capability (android.request.availableCapabilities
                 *      contains BURST_CAPTURE)
                 *
                 *      Per frame control (android.sync.maxLatency == PER_FRAME_CONTROL)
                 *
                 *      Manual sensor control (android.request.availableCapabilities
                 *      contains MANUAL_SENSOR)
                 *
                 *      Manual post-processing control (android.request.availableCapabilities
                 *      contains MANUAL_POST_PROCESSING)
                 *
                 *      The required exposure time range defined in
                 *      android.sensor.info.exposureTimeRange
                 *
                 *      The required maxFrameDuration defined in
                 *      android.sensor.info.maxFrameDuration
                 *
                 *      Note: Pre-API level 23, FULL devices also supported arbitrary cropping
                 *      region (android.scaler.croppingType == FREEFORM); this requirement was
                 *      relaxed in API level 23, and FULL devices may only support CENTERED cropping.
                 */
                break;
            }

            case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3): {
                mHardwareLevel     = HardwareLevel.LEVEL_3;
                mHardwareLevelName = "LEVEL_3";
                /*
                 * Added in API 24
                 *
                 * This camera device is capable of YUV reprocessing and RAW data capture,
                 * in addition to FULL-level capabilities.
                 *
                 * The stream configurations listed in the LEVEL_3, RAW, FULL, LEGACY and
                 * LIMITED tables in the createCaptureSession documentation are guaranteed
                 * to be supported.
                 *
                 * The following additional capabilities are guaranteed to be supported:
                 *
                 *      YUV_REPROCESSING capability (android.request.availableCapabilities
                 *      contains YUV_REPROCESSING)
                 *
                 *      RAW capability (android.request.availableCapabilities contains RAW)
                 */
                break;
            }
            case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL): {
                mHardwareLevel     = HardwareLevel.EXTERNAL;
                mHardwareLevelName = "EXTERNAL";
                /*
                 * Added in API 28
                 *
                 * This camera device is backed by an external camera connected to this Android
                 * device.
                 *
                 * The device has capability identical to a LIMITED level device, with the following
                 * exceptions:
                 *
                 *      The device may not report lens/sensor related information such as
                 *          android.lens.focalLength
                 *          android.lens.info.hyperfocalDistance
                 *          android.sensor.info.physicalSize
                 *          android.sensor.info.whiteLevel
                 *          android.sensor.blackLevelPattern
                 *          android.sensor.info.colorFilterArrangement
                 *          android.sensor.rollingShutterSkew
                 *
                 *      The device will report 0 for android.sensor.orientation
                 *
                 *      The device has less guarantee on stable framerate, as the framerate partly
                 *      depends on the external camera being used.
                 */
                break;
            }
            default: {
                mHardwareLevel     = HardwareLevel.UNKNOWN;
                mHardwareLevelName = "UNKNOWN";
            }
        }
    }

    /**
     *
     * @return
     */
    @NonNull
    public String toString() {
        String string = "\n";

        string = string.concat("CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL\n");
        string = string.concat("\t" + "Hardware level: " + mHardwareLevelName + "\n");

        return string;
    }
}
