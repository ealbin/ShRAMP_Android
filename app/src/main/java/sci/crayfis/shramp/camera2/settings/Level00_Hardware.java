package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SizeF;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
abstract class Level00_Hardware {

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

    protected String                mInfoVersion;
    private   String                mInfoVersionName;

    protected SizeF                 mSensorInfoPhysicalSize;
    private   String                mSensorInfoPhysicalSizeName;

    protected Size                  mSensorInfoPixelArraySize;
    private   String                mSensorInfoPixelArraySizeName;

    protected Rect                  mSensorInfoPreCorrectionActiveArraySize;
    private   String                mSensorInfoPreCorrectionActiveArraySizeName;

    protected Rect                  mSensorInfoActiveArraySize;
    private   String                mSensorInfoActiveArraySizeName;

    protected Integer               mSensorInfoColorFilterArrangement;
    private   String                mSensorInfoColorFilterArrangementName;

    protected Rect[]                mSensorOpticalBlackRegions;
    private   String                mSensorOpticalBlackRegionsName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    private Level00_Hardware() {}

    protected Level00_Hardware(@NonNull CameraCharacteristics characteristics) {
        mCameraCharacteristics = characteristics;
        getHardwareLevel();
        getInfoVersion();
        getSensorInfoPhysicalSize();
        getSensorInfoPixelArraySize();
        getSensorInfoPreCorrectionActiveArraySize();
        getSensorInfoActiveArraySize();
        getSensorInfoColorFilterArrangement();
        getSensorOpticalBlackRegions();
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
        if (hardwareLevel == null) {
            mHardwareLevelName = "Not supported";
            return;
        }

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

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getInfoVersion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            mInfoVersion     = null;
            mInfoVersionName = "Not supported";
            return;
        }
        CameraCharacteristics.Key key = CameraCharacteristics.INFO_VERSION;
        /*
         * Added in API 28
         *
         * A short string for manufacturer version information about the camera device, such as
         * ISP hardware, sensors, etc.
         *
         * This can be used in TAG_IMAGE_DESCRIPTION in jpeg EXIF. This key may be absent if no
         * version information is available on the device.
         *
         * Optional - This value may be null on some devices.
         */
        mInfoVersion = (String) mCameraCharacteristics.get(key);
        if (mInfoVersion == null) {
            mInfoVersionName = "Not supported";
            return;
        }

        mInfoVersionName = mInfoVersion.replaceAll(":", "=");
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getSensorInfoPhysicalSize() {
        CameraCharacteristics.Key key = CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE;
        /*
         * Added in API 21
         *
         * The physical dimensions of the full pixel array.
         *
         * This is the physical size of the sensor pixel array defined by
         * android.sensor.info.pixelArraySize.
         *
         * Units: Millimeters
         *
         * This key is available on all devices.
         */
        mSensorInfoPhysicalSize = (SizeF) mCameraCharacteristics.get(key);
        if (mSensorInfoPhysicalSize == null) {
            mSensorInfoPhysicalSizeName = "Not supported";
            return;
        }
        mSensorInfoPhysicalSizeName = mSensorInfoPhysicalSize.toString() + " [mm]";
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getSensorInfoPixelArraySize() {
        CameraCharacteristics.Key key = CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE;
        /*
         * Added in API 21
         *
         * Dimensions of the full pixel array, possibly including black calibration pixels.
         *
         * The pixel count of the full pixel array of the image sensor, which covers
         * android.sensor.info.physicalSize area. This represents the full pixel dimensions of the
         * raw buffers produced by this sensor.
         *
         * If a camera device supports raw sensor formats, either this or
         * android.sensor.info.preCorrectionActiveArraySize is the maximum dimensions for the raw
         * output formats listed in StreamConfigurationMap (this depends on whether or not the image
         * sensor returns buffers containing pixels that are not part of the active array region
         * for blacklevel calibration or other purposes).
         *
         * Some parts of the full pixel array may not receive light from the scene, or be otherwise
         * inactive. The android.sensor.info.preCorrectionActiveArraySize key defines the rectangle
         * of active pixels that will be included in processed image formats.
         *
         * Units: Pixels
         *
         * This key is available on all devices.
         */
        mSensorInfoPixelArraySize = (Size) mCameraCharacteristics.get(key);
        if (mSensorInfoPixelArraySize == null) {
            mSensorInfoPixelArraySizeName = "Not supported";
            return;
        }
        mSensorInfoPixelArraySizeName = mSensorInfoPixelArraySize.toString() + " [pixels]";
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getSensorInfoPreCorrectionActiveArraySize() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mSensorInfoPreCorrectionActiveArraySize     = null;
            mSensorInfoPreCorrectionActiveArraySizeName = "Not supported";
            return;
        }
        CameraCharacteristics.Key key = CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE;
        /*
         * Added in API 23
         *
         * The area of the image sensor which corresponds to active pixels prior to the application
         * of any geometric distortion correction.
         *
         * This is the rectangle representing the size of the active region of the sensor (i.e. the
         * region that actually receives light from the scene) before any geometric correction has
         * been applied, and should be treated as the active region rectangle for any of the raw
         * formats. All metadata associated with raw processing (e.g. the lens shading correction
         * map, and radial distortion fields) treats the top, left of this rectangle as the origin,
         * (0,0).
         *
         * The size of this region determines the maximum field of view and the maximum number of
         * pixels that an image from this sensor can contain, prior to the application of geometric
         * distortion correction. The effective maximum pixel dimensions of a
         * post-distortion-corrected image is given by the android.sensor.info.activeArraySize
         * field, and the effective maximum field of view for a post-distortion-corrected image can
         * be calculated by applying the geometric distortion correction fields to this rectangle,
         * and cropping to the rectangle given in android.sensor.info.activeArraySize.
         *
         * E.g. to calculate position of a pixel, (x,y), in a processed YUV output image with the
         * dimensions in android.sensor.info.activeArraySize given the position of a pixel,
         * (x', y'), in the raw pixel array with dimensions give in
         * android.sensor.info.pixelArraySize:
         *
         *      Choose a pixel (x', y') within the active array region of the raw buffer given in
         *      android.sensor.info.preCorrectionActiveArraySize, otherwise this pixel is considered
         *      to be outside of the FOV, and will not be shown in the processed output image.
         *
         *      Apply geometric distortion correction to get the post-distortion pixel coordinate,
         *      (x_i, y_i). When applying geometric correction metadata, note that metadata for raw
         *      buffers is defined relative to the top, left of the
         *      android.sensor.info.preCorrectionActiveArraySize rectangle.
         *
         *      If the resulting corrected pixel coordinate is within the region given in
         *      android.sensor.info.activeArraySize, then the position of this pixel in the
         *      processed output image buffer is (x_i - activeArray.left, y_i - activeArray.top),
         *      when the top, left coordinate of that buffer is treated as (0, 0).
         *
         * Thus, for pixel x',y' = (25, 25) on a sensor where android.sensor.info.pixelArraySize is
         * (100,100), android.sensor.info.preCorrectionActiveArraySize is (10, 10, 100, 100),
         * android.sensor.info.activeArraySize is (20, 20, 80, 80), and the geometric distortion
         * correction doesn't change the pixel coordinate, the resulting pixel selected in pixel
         * coordinates would be x,y = (25, 25) relative to the top,left of the raw buffer with
         * dimensions given in android.sensor.info.pixelArraySize, and would be (5, 5) relative to
         * the top,left of post-processed YUV output buffer with dimensions given in
         * android.sensor.info.activeArraySize.
         *
         * The currently supported fields that correct for geometric distortion are:
         *      android.lens.distortion.
         *
         * If the camera device doesn't support geometric distortion correction, or all of the
         * geometric distortion fields are no-ops, this rectangle will be the same as the
         * post-distortion-corrected rectangle given in android.sensor.info.activeArraySize.
         *
         * This rectangle is defined relative to the full pixel array; (0,0) is the top-left of the
         * full pixel array, and the size of the full pixel array is given by
         * android.sensor.info.pixelArraySize.
         *
         * The pre-correction active array may be smaller than the full pixel array, since the full
         * array may include black calibration pixels or other inactive regions.
         *
         * Units: Pixel coordinates on the image sensor
         *
         * This key is available on all devices.
         */

        mSensorInfoPreCorrectionActiveArraySize = (Rect) mCameraCharacteristics.get(key);
        if (mSensorInfoPreCorrectionActiveArraySize == null) {
            mSensorInfoPreCorrectionActiveArraySizeName = "Not supported";
            return;
        }
        String width  = Integer.toString(mSensorInfoPreCorrectionActiveArraySize.width());
        String height = Integer.toString(mSensorInfoPreCorrectionActiveArraySize.height());
        mSensorInfoPreCorrectionActiveArraySizeName = width + "x" + height + " [pixels]";
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getSensorInfoActiveArraySize() {
        CameraCharacteristics.Key key = CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE;
        /*
         * Added in API 21
         *
         * The area of the image sensor which corresponds to active pixels after any geometric
         * distortion correction has been applied.
         *
         * This is the rectangle representing the size of the active region of the sensor
         * (i.e. the region that actually receives light from the scene) after any geometric
         * correction has been applied, and should be treated as the maximum size in pixels of any
         * of the image output formats aside from the raw formats.
         *
         * This rectangle is defined relative to the full pixel array; (0,0) is the top-left of the
         * full pixel array, and the size of the full pixel array is given by
         * android.sensor.info.pixelArraySize.
         *
         * The coordinate system for most other keys that list pixel coordinates, including
         * android.scaler.cropRegion, is defined relative to the active array rectangle given in
         * this field, with (0, 0) being the top-left of this rectangle.
         *
         * The active array may be smaller than the full pixel array, since the full array may
         * include black calibration pixels or other inactive regions.
         *
         * For devices that do not support android.distortionCorrection.mode control, the active
         * array must be the same as android.sensor.info.preCorrectionActiveArraySize.
         *
         * For devices that support android.distortionCorrection.mode control, the active array
         * must be enclosed by android.sensor.info.preCorrectionActiveArraySize. The difference
         * between pre-correction active array and active array accounts for scaling or cropping
         * caused by lens geometric distortion correction.
         *
         * In general, application should always refer to active array size for controls like
         * metering regions or crop region. Two exceptions are when the application is dealing with
         * RAW image buffers (RAW_SENSOR, RAW10, RAW12 etc), or when application explicitly set
         * android.distortionCorrection.mode to OFF. In these cases, application should refer to
         * android.sensor.info.preCorrectionActiveArraySize.
         *
         * Units: Pixel coordinates on the image sensor
         *
         * This key is available on all devices.
         */
        mSensorInfoActiveArraySize = (Rect) mCameraCharacteristics.get(key);
        if (mSensorInfoActiveArraySize == null) {
            mSensorInfoActiveArraySizeName = "Not supported";
            return;
        }
        String width  = Integer.toString(mSensorInfoActiveArraySize.width());
        String height = Integer.toString(mSensorInfoActiveArraySize.height());
        mSensorInfoActiveArraySizeName = width + "x" + height + " [pixels]";
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getSensorInfoColorFilterArrangement() {
        CameraCharacteristics.Key key = CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT;
        /*
         * Added in API 21
         *
         * The arrangement of color filters on sensor; represents the colors in the top-left 2x2
         * section of the sensor, in reading order.
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        mSensorInfoColorFilterArrangement = (Integer) mCameraCharacteristics.get(key);
        if (mSensorInfoColorFilterArrangement == null) {
            mSensorInfoColorFilterArrangementName = "Not supported";
            return;
        }

        switch (mSensorInfoColorFilterArrangement) {
            case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB): {
                mSensorInfoColorFilterArrangementName = "RGGB";
                break;
            }
            case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG): {
                mSensorInfoColorFilterArrangementName = "GRBG";
                break;
            }
            case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG): {
                mSensorInfoColorFilterArrangementName = "GBRG";
                break;
            }
            case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR): {
                mSensorInfoColorFilterArrangementName = "BGGR";
                break;
            }
            case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB): {
                mSensorInfoColorFilterArrangementName = "RGB";
                /*
                 * Added in API 21
                 *
                 * Sensor is not Bayer; output has 3 16-bit values for each pixel, instead of
                 * just 1 16-bit value per pixel.
                 */
                break;
            }
            default: {
                mSensorInfoColorFilterArrangementName = "Unknown";
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void getSensorOpticalBlackRegions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mSensorOpticalBlackRegions     = null;
            mSensorOpticalBlackRegionsName = "Not supported";
            return;
        }
        CameraCharacteristics.Key key = CameraCharacteristics.SENSOR_OPTICAL_BLACK_REGIONS;
        /*
         * Added in API 24
         *
         * List of disjoint rectangles indicating the sensor optically shielded black pixel regions.
         *
         * In most camera sensors, the active array is surrounded by some optically shielded pixel
         * areas. By blocking light, these pixels provides a reliable black reference for black
         * level compensation in active array region.
         *
         * This key provides a list of disjoint rectangles specifying the regions of optically
         * shielded (with metal shield) black pixel regions if the camera device is capable of
         * reading out these black pixels in the output raw images. In comparison to the fixed black
         * level values reported by android.sensor.blackLevelPattern, this key may provide a more
         * accurate way for the application to calculate black level of each captured raw images.
         *
         * When this key is reported, the android.sensor.dynamicBlackLevel and
         * android.sensor.dynamicWhiteLevel will also be reported.
         *
         * Optional - This value may be null on some devices.
         */
        mSensorOpticalBlackRegions = (Rect[]) mCameraCharacteristics.get(key);
        if (mSensorOpticalBlackRegions == null) {
            mSensorOpticalBlackRegionsName = "Not supported";
            return;
        }
        mSensorOpticalBlackRegionsName = Integer.toString(mSensorOpticalBlackRegions.length) + " regions";
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        String string = "Level 00 (Hardware)\n";
        string += "CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL:                " + mHardwareLevelName                          + "\n";
        string += "CameraCharacteristics.INFO_VERSION:                                 " + mInfoVersionName                            + "\n";
        string += "CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE:                    " + mSensorInfoPhysicalSizeName                 + "\n";
        string += "CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE:                 " + mSensorInfoPixelArraySizeName               + "\n";
        string += "CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE: " + mSensorInfoPreCorrectionActiveArraySizeName + "\n";
        string += "CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE:                " + mSensorInfoActiveArraySizeName              + "\n";
        string += "CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT:         " + mSensorInfoColorFilterArrangementName       + "\n";
        string += "CameraCharacteristics.SENSOR_OPTICAL_BLACK_REGIONS:                 " + mSensorOpticalBlackRegionsName              + "\n";

        List<String> stringList = new ArrayList<>();
        stringList.add(string);
        return stringList;
    }
}
