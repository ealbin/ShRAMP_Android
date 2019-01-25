package edu.crayfis.shramp;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.RggbChannelVector;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.media.Image;
import android.util.Log;
import android.util.Range;
import android.util.Size;


public class Camera {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "Camera";
    private final static String DIVIDER = "---------------------------------------------";


    // int to denote front vs back camera
    public final static int FRONT_CAMERA = CameraCharacteristics.LENS_FACING_FRONT;
    public final static int  BACK_CAMERA = CameraCharacteristics.LENS_FACING_BACK;

    // output image format
    public final static String IMAGE_TYPE = "RAW12";
    public final static int IMAGE_FORMAT = ImageFormat.RAW12;

    // output image size -- set in configureCamera()
    public Size Image_size;

    public CameraCharacteristics    Camera_attributes;
    public CameraDevice             Camera_device;
    public CaptureRequest           Capture_request;
    public CaptureRequest.Builder   Capture_builder;
    public CameraCaptureSession     Capture_session;
    public StreamConfigurationMap   Stream_config;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Checks that camera hardware is up to snuff
     * @return true if the camera is too old, false if it's modern
     */
    protected boolean outdatedHardware() {
        final String LOCAL_TAG = TAG.concat(".outdatedHardware()");
        Log.e(LOCAL_TAG, DIVIDER);

        int hardware_level = Camera_attributes.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (hardware_level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
            Log.e(LOCAL_TAG, "Level 3 Camera Hardware");
            Log.e(LOCAL_TAG, "RETURN");
            return false;
        }

        switch (hardware_level) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY :
                Log.e(LOCAL_TAG, "Level Legacy Camera Hardware");
                break;

            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED :
                Log.e(LOCAL_TAG, "Level Limited Camera Hardware");
                break;

            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL :
                Log.e(LOCAL_TAG, "Level External Camera Hardware");
                break;

            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL :
                Log.e(LOCAL_TAG, "Level Full Camera Hardware");
                break;

            default :
                Log.e(LOCAL_TAG, "Unknown Camera Hardware Abilities");
                break;
        }

        Log.e(LOCAL_TAG, "RETURN");
        return true;
    }
    
    /**
     * Configure camera for data taking
     */
    protected void configureCamera() {
        final String LOCAL_TAG = TAG.concat(".configureCamera()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Find maximum output size (area)
        Size[] output_sizes = Stream_config.getOutputSizes(IMAGE_FORMAT);
        Image_size = output_sizes[0];
        for (Size size : output_sizes) {
            long area     =       size.getWidth() *       size.getHeight();
            long area_max = Image_size.getWidth() * Image_size.getHeight();
            if (area > area_max) {
                Image_size = size;
            }
        }
        Log.e(LOCAL_TAG, "Image format: " + IMAGE_TYPE);
        Log.e(LOCAL_TAG, "Maximum image size: " + Image_size.toString());

        // Disable the flash
        Capture_builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        Log.e(LOCAL_TAG, "Disabled flash");

        // Set to maximum analog sensitivity (no digital gain)
        int max_sensitivity = Camera_attributes.get(
                CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);
        Capture_builder.set(CaptureRequest.SENSOR_SENSITIVITY, max_sensitivity);
        Log.e(LOCAL_TAG, "Max analog sensitivity = " + Integer.toString(max_sensitivity));

        configureControl();
        configureCorrections();
        configureLens();
        configureStatistics();
        configureTiming();
    }

    /**
     * Configures camera controls to manual
     */
    private void configureControl() {
        final String LOCAL_TAG = TAG.concat(".configureControl()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Turn off auto-exposure, auto-white-balance, and auto-focus control routines
        Capture_builder.set(CaptureRequest.CONTROL_CAPTURE_INTENT,
                CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL);
        Capture_builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);
        Log.e(LOCAL_TAG, "Manual control engaged, automatic actions disabled");

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures camera corrections and processing options to off, disabled, or linear
     */
    private void configureCorrections() {
        final String LOCAL_TAG = TAG.concat(".configureCorrections()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Lock black-level compensation to its current value for stable performance
        Capture_builder.set(CaptureRequest.BLACK_LEVEL_LOCK, true);
        Log.e(LOCAL_TAG, "Black level locked");

        // Disable chromatic aberration correction
        Capture_builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF);
        Log.e(LOCAL_TAG, "Color aberration correction disabled");

        // Treat red, green and blue the same (no weighting)
        Capture_builder.set(CaptureRequest.COLOR_CORRECTION_GAINS,
                new RggbChannelVector(1,1,1,1));
        Capture_builder.set(CaptureRequest.COLOR_CORRECTION_MODE,
                CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
        Capture_builder.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM,
                new ColorSpaceTransform(new int[] {
                        1, 0, 0,
                        0, 1, 0,
                        0, 0, 1
                }));
        Log.e(LOCAL_TAG, "Color gains/transformations unity/linear");

        // Disable color effects
        Capture_builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                CameraMetadata.CONTROL_EFFECT_MODE_OFF);
        Log.e(LOCAL_TAG, "Effects disabled");

        // Disable video stabilization
        Capture_builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        Log.e(LOCAL_TAG, "Video stabilization disabled");

        // Disable edge enhancement
        Capture_builder.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_OFF);
        Log.e(LOCAL_TAG, "Edge enhancement disabled");

        // Disable hotpixel correction
        Capture_builder.set(CaptureRequest.HOT_PIXEL_MODE, CameraMetadata.HOT_PIXEL_MODE_OFF);
        Log.e(LOCAL_TAG, "Hot-pixel correction disabled");

        // Disable noise reduction processing
        Capture_builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                CameraMetadata.NOISE_REDUCTION_MODE_OFF);
        Log.e(LOCAL_TAG, "Noise reduction disabled");

        // Disable lens shading correction
        Capture_builder.set(CaptureRequest.SHADING_MODE, CameraMetadata.SHADING_MODE_OFF);
        Log.e(LOCAL_TAG, "Lens shading correction disabled");

        // Set contrast curve (i.e. gamma) to linear
        float[] linear_response = {0, 0, 1, 1};
        Capture_builder.set(CaptureRequest.TONEMAP_CURVE,
                new TonemapCurve(linear_response, linear_response, linear_response));
        Capture_builder.set(CaptureRequest.TONEMAP_MODE,
                CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE);
        Log.e(LOCAL_TAG, "Gamma/Contrast curve gain set to linear");

        // Not necissary if using RAW images:
        // Disable post-raw sensitivity boost = raw-sensitivity * this-value / 100 (API 24)
        //Capture_builder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 100);
        // Disable post-raw distortion correction (API 28)
        //Capture_builder.set(CaptureRequest.DISTORTION_CORRECTION_MODE,
        //        CameraMetadata.DISTORTION_CORRECTION_MODE_OFF);

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures camera mechanical lens optics for darkest aperture conditions
     */
    private void configureLens() {
        final String LOCAL_TAG = TAG.concat(".configureLens()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Set focus distance to inifinity
        Capture_builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f);
        Log.e(LOCAL_TAG, "Focus set to infinity");

        // Use minimal (darkest) lens aperture
        float[] lens_apertures = Camera_attributes.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
        float lens_aperture_min = lens_apertures[0];
        Capture_builder.set(CaptureRequest.LENS_APERTURE, lens_aperture_min);
        Log.e(LOCAL_TAG, "Aperture (f-number) set to minimum = " +
                Float.toString(lens_aperture_min));

        // Use maximal (darkest) lens filtering
        float[] lens_filters = Camera_attributes.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES);
        float lens_filter_max = lens_filters[ lens_filters.length - 1 ];
        Capture_builder.set(CaptureRequest.LENS_FILTER_DENSITY, lens_filter_max);
        Log.e(LOCAL_TAG, "Filter density set to maximum [EV] = " +
                Float.toString(lens_filter_max));

        // Use maximal focal length
        float[] lens_focal_lengths = Camera_attributes.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        float lens_focal_max = lens_focal_lengths[ lens_focal_lengths.length - 1 ];
        Capture_builder.set(CaptureRequest.LENS_FOCAL_LENGTH, lens_focal_max);
        Log.e(LOCAL_TAG, "Focal length set to maximum [mm] = " +
                Float.toString(lens_focal_max));

        // Disable mechanical lens stabilization
        Capture_builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
        Log.e(LOCAL_TAG, "Optical stabilization disabled");

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures (disables) statistical metadata abilities of the camera
     */
    private void configureStatistics() {
        final String LOCAL_TAG = TAG.concat(".configureStatistics()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Disable face detection
        Capture_builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF);
        Log.e(LOCAL_TAG, "Face detection disabled");

        // Disable hot-pixel map mode (possibly enable this for comparisons)
        Capture_builder.set(CaptureRequest.STATISTICS_HOT_PIXEL_MAP_MODE, false);
        Log.e(LOCAL_TAG, "Hot-pixel map disabled");

        // Disable lens shading map mode
        Capture_builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE,
                CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_OFF);
        Log.e(LOCAL_TAG, "Lens shading map disabled");

        // API 28
        // Disable optical stabilization position information
        //Capture_builder.set(CaptureRequest.STATISTICS_OIS_DATA_MODE,
        //        CameraMetadata.STATISTICS_OIS_DATA_MODE_OFF);

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures exposure and frame duration times to minimum (Image_size must be initialized
     * prior to calling this method)
     */
    private void configureTiming() {
        final String LOCAL_TAG = TAG.concat(".configureTiming()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Set exposure time to minimum
        Range<Long> exposure_range = Camera_attributes.get(
                CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        Long exposure_min = exposure_range.getLower();
        Capture_builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure_min);
        Log.e(LOCAL_TAG, "Minimum exposure time [ns] = " + Long.toString(exposure_min));

        // Set frame fastest frame duration
        long frame_min = Stream_config.getOutputMinFrameDuration(IMAGE_FORMAT, Image_size);
        Capture_builder.set(CaptureRequest.SENSOR_FRAME_DURATION, frame_min);
        Log.e(LOCAL_TAG, "Minimum frame duration [ns] = " + Long.toString(frame_min));

        Log.e(LOCAL_TAG, "RETURN");
    }

}
