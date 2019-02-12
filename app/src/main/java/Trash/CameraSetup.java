package Trash;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.RggbChannelVector;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import sci.crayfis.shramp.ChatterBox;
import sci.crayfis.shramp.MaineShRAMP;


@TargetApi(Build.VERSION_CODES.LOLLIPOP) // 21
public abstract class CameraSetup {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "CameraSetup";
    private final static String DIVIDER = "---------------------------------------------";

    // int to denote front vs back camera
    protected final static int  BACK_CAMERA = CameraCharacteristics.LENS_FACING_BACK;
    protected final static int FRONT_CAMERA = CameraCharacteristics.LENS_FACING_FRONT;

    // output image format
    protected final static String IMAGE_TYPE = "RAW_SENSOR";
    protected final static int IMAGE_FORMAT  = ImageFormat.RAW_SENSOR;

    // output image size -- set in configureCamera()
    protected Size mImage_size;

    // reference to MaineShRAMP activity and what to do with total failure
    protected MaineShRAMP mMaine_shramp;
    protected Runnable    mQuit_action;

    // essential objects for configuring/operating camera
    protected String                   mBack_camera_id;
    protected String                   mFront_camera_id;
    protected CameraManager            mManager;
    protected CameraCharacteristics    mCharacteristics;
    protected CaptureRequest.Builder   mCapture_builder;
    protected CameraDevice             mDevice;
    protected StreamConfigurationMap   mStream_config;


    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Create CameraSetup object for configuring the physical device to operation parameters
     * @param main_activity reference to MaineShRAMP
     * @param quit_action reference to method to run if fatal error is encountered
     */
    CameraSetup(@NonNull MaineShRAMP main_activity, @NonNull Runnable quit_action) {
        final String LOCAL_TAG = TAG.concat(".CameraSetup()");
        Log.e(LOCAL_TAG, DIVIDER);

        mMaine_shramp = main_activity;
        mQuit_action  = quit_action;

        mManager = (CameraManager) mMaine_shramp.getSystemService(Context.CAMERA_SERVICE);
        if (mManager == null) {
            Log.e(LOCAL_TAG, "Camera service unreachable");
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Uh oh";
            String message = "Something odd has occurred. \n"
                            + "The camera service is not available. \n"
                            + "Sorry, but there is no chance of survival now. \n"
                            + ".... rose......bud...blaugghhhhhhhh";
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }

        getCameraIDs();

        try {
            mCharacteristics = mManager.getCameraCharacteristics(mBack_camera_id);
        } catch (Exception e) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Runtime Exception!";
            String message = "CameraSetup.CameraSetup()\n"
                            + e.getLocalizedMessage();
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }
        mStream_config = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (outdatedHardware()) {
            Log.e(LOCAL_TAG, "CameraSetup hardware is outdated, exiting");
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Womp womp";
            String message = "Camera hardware level 3 required.\n"
                            + "Sorry, but you don't cut the mustard.";
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Figure out camera IDs for front and back if they exist
     */
    protected void getCameraIDs() {
        final String LOCAL_TAG = TAG.concat(".getCameraIDs()");
        Log.e(LOCAL_TAG, DIVIDER);

        String[] camera_id_list;
        try {
            camera_id_list = mManager.getCameraIdList();
            for (String camera_id : camera_id_list) {
                mCharacteristics = mManager.getCameraCharacteristics(camera_id);

                Integer lens_facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (lens_facing == null) {
                    Log.e(LOCAL_TAG, "CameraCharacteristics.LENS_FACING is null, skipping");
                    continue;
                }

                if (lens_facing == BACK_CAMERA) {
                    mBack_camera_id = camera_id;
                    Log.e(LOCAL_TAG, "Back camera found, ID = " + mBack_camera_id);
                }
                else if (lens_facing == FRONT_CAMERA) {
                    mFront_camera_id = camera_id;
                    Log.e(LOCAL_TAG, "Front camera found, ID = " + mFront_camera_id);
                }
                else {
                    Log.e(LOCAL_TAG, "Unknown camera found, ID = " + camera_id);
                }
            }

            if (mBack_camera_id == null) {
                Log.e(LOCAL_TAG, "Back camera cannot be found");
//                ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
                String title = "Uh oh";
                String message = "Something odd has occurred. \n"
                        + "Cannot connect to the back camera.. \n"
                        + "Sorry, but there is no chance of survival now. \n"
                        + ".... rose......bud...blaugghhhhhhhh";
                String button = "Accept defeat";
//                chatterbox.displayBasicAlert(title, message, button, mQuit_action);
                Log.e(LOCAL_TAG, "RETURN");
                return;
            }
        }
        catch (Exception e) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Runtime Exception!";
            String message = "CameraSetup.getCameraIDs()\n"
                    + e.getLocalizedMessage();
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Checks that camera hardware is up to snuff
     * @return true if the camera is too old, false if it's modern
     */
    protected boolean outdatedHardware() {
        final String LOCAL_TAG = TAG.concat(".outdatedHardware()");
        Log.e(LOCAL_TAG, DIVIDER);

        Integer hardware_level = mCharacteristics.get(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

        if (hardware_level == null) {
            Log.e(LOCAL_TAG, "Hardware level unobtainable");
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Uh oh";
            String message = "Something odd has occurred. \n"
                    + "The camera hardware level is not available. \n"
                    + "Sorry, but there is no chance of survival now. \n"
                    + ".... rose......bud...blaugghhhhhhhh";
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return true;
        }

        if (hardware_level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
            Log.e(LOCAL_TAG, "Level 3 CameraSetup Hardware");
            Log.e(LOCAL_TAG, "RETURN");
            return false;
        }

        switch (hardware_level) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY :
                Log.e(LOCAL_TAG, "Level Legacy CameraSetup Hardware");
                break;

            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED :
                Log.e(LOCAL_TAG, "Level Limited CameraSetup Hardware");
                break;

            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL :
                Log.e(LOCAL_TAG, "Level External CameraSetup Hardware");
                break;

            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL :
                Log.e(LOCAL_TAG, "Level Full CameraSetup Hardware");
                break;

            default :
                Log.e(LOCAL_TAG, "Unknown CameraSetup Hardware Abilities");
                break;
        }

        Log.e(LOCAL_TAG, "RETURN");
        return true;
    }

    /**
     * Configure camera for data taking -- called from subclass Camera
     */
    protected void configureCamera() {
        final String LOCAL_TAG = TAG.concat(".configureCamera()");
        Log.e(LOCAL_TAG, DIVIDER);

        try {
            mCapture_builder = mDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
        }
        catch (Exception e) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Runtime Exception!";
            String message = "CameraSetup.configureCamera()\n"
                            + e.getLocalizedMessage();
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }

        if (!mStream_config.isOutputSupportedFor(IMAGE_FORMAT)) {
            Log.e(LOCAL_TAG, "output format is not supported :-(");
//            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Womp womp";
            String message = "The desired output image format is \n"
                    + "not supported :-( \n"
                    + "Sorry, but there is no chance of survival now. \n"
                    + ".... rose......bud...blaugghhhhhhhh";
            String button = "Accept defeat";
//            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }
        // Find maximum output size (area)
        Size[] output_sizes = mStream_config.getOutputSizes(IMAGE_FORMAT);
        mImage_size = output_sizes[0];
        for (Size size : output_sizes) {
            Log.e(LOCAL_TAG, "Supported Size: " + size.toString());
            long area     =        size.getWidth() * size.getHeight();
            long area_max = mImage_size.getWidth() * mImage_size.getHeight();
            if (area > area_max) {
                mImage_size = size;
            }
        }
        Log.e(LOCAL_TAG, "Image format: " + IMAGE_TYPE);
        Log.e(LOCAL_TAG, "Maximum image size: " + mImage_size.toString());

        // Disable the flash
        mCapture_builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        Log.e(LOCAL_TAG, "Disabled flash");

        // Set to maximum analog sensitivity (no digital gain)
        Integer max_sensitivity = mCharacteristics.get(
                CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);
        if (max_sensitivity == null ) {
            Log.e(LOCAL_TAG, "***Max analog sensitivity is unknown***");
        }
        else {
            mCapture_builder.set(CaptureRequest.SENSOR_SENSITIVITY, max_sensitivity);
            Log.e(LOCAL_TAG, "Max analog sensitivity = " + Integer.toString(max_sensitivity));
        }

        Log.e(LOCAL_TAG, "Configuring other aspects");
        configureControl();
        configureCorrections();
        configureLens();
        configureStatistics();
        configureTiming();

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures camera controls to manual
     * (called from configureCamera)
     */
    private void configureControl() {
        final String LOCAL_TAG = TAG.concat(".configureControl()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Turn off auto-exposure, auto-white-balance, and auto-focus control routines
        mCapture_builder.set(CaptureRequest.CONTROL_CAPTURE_INTENT,
                CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL);
        mCapture_builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);
        Log.e(LOCAL_TAG, "Manual control engaged, automatic actions disabled");

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures camera corrections and processing options to off, disabled, or linear
     * (called from configureCamera)
     */
    private void configureCorrections() {
        final String LOCAL_TAG = TAG.concat(".configureCorrections()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Lock black-level compensation to its current value for stable performance
        mCapture_builder.set(CaptureRequest.BLACK_LEVEL_LOCK, true);
        Log.e(LOCAL_TAG, "Black level locked");

        // Disable chromatic aberration correction
        mCapture_builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF);
        Log.e(LOCAL_TAG, "Color aberration correction disabled");

        // Treat red, green and blue the same (no weighting)
        mCapture_builder.set(CaptureRequest.COLOR_CORRECTION_GAINS,
                new RggbChannelVector(1,1,1,1));
        mCapture_builder.set(CaptureRequest.COLOR_CORRECTION_MODE,
                CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
        mCapture_builder.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM,
                new ColorSpaceTransform(new int[] {
                        1, 1,  0, 1,  0, 1,    // numerator1, denominator1, num2, den2, num3, den3
                        0, 1,  1, 1,  0, 1,    // numerator4, denominator4, num5, den5, num6, den6
                        0, 1,  0, 1,  1, 1     // numerator7, denominator7, num8, den8, num9, den9
                }));
        Log.e(LOCAL_TAG, "Color gains/transformations unity/linear");

        // Disable color effects
        mCapture_builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                CameraMetadata.CONTROL_EFFECT_MODE_OFF);
        Log.e(LOCAL_TAG, "Effects disabled");

        // Disable video stabilization
        mCapture_builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        Log.e(LOCAL_TAG, "Video stabilization disabled");

        // Disable edge enhancement
        mCapture_builder.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_OFF);
        Log.e(LOCAL_TAG, "Edge enhancement disabled");

        // Disable hotpixel correction
        mCapture_builder.set(CaptureRequest.HOT_PIXEL_MODE, CameraMetadata.HOT_PIXEL_MODE_OFF);
        Log.e(LOCAL_TAG, "Hot-pixel correction disabled");

        // Disable noise reduction processing
        mCapture_builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                CameraMetadata.NOISE_REDUCTION_MODE_OFF);
        Log.e(LOCAL_TAG, "Noise reduction disabled");

        // Disable lens shading correction
        mCapture_builder.set(CaptureRequest.SHADING_MODE, CameraMetadata.SHADING_MODE_OFF);
        Log.e(LOCAL_TAG, "Lens shading correction disabled");

        // Set contrast curve (i.e. gamma) to linear
        float[] linear_response = {0, 0, 1, 1};
        mCapture_builder.set(CaptureRequest.TONEMAP_CURVE,
                new TonemapCurve(linear_response, linear_response, linear_response));
        mCapture_builder.set(CaptureRequest.TONEMAP_MODE,
                CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE);
        Log.e(LOCAL_TAG, "Gamma/Contrast curve gain set to linear");

        // Not necissary if using RAW images:
        //------------------------------------
        // Disable post-raw sensitivity boost = raw-sensitivity * this-value / 100 (API 24)
        //Capture_builder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 100);
        // Disable post-raw distortion correction (API 28)
        //Capture_builder.set(CaptureRequest.DISTORTION_CORRECTION_MODE,
        //        CameraMetadata.DISTORTION_CORRECTION_MODE_OFF);

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures camera mechanical lens optics for darkest aperture conditions
     * (called from configureCamera)
     */
    private void configureLens() {
        final String LOCAL_TAG = TAG.concat(".configureLens()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Set focus distance to infinity
        mCapture_builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f);
        Log.e(LOCAL_TAG, "Focus set to infinity");

        // Use minimal (darkest) lens aperture
        float[] lens_apertures = mCharacteristics.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
        if (lens_apertures == null) {
            Log.e(LOCAL_TAG, "***Lens apertures unavailable***");
        }
        else {
            float lens_aperture_min = lens_apertures[0];
            mCapture_builder.set(CaptureRequest.LENS_APERTURE, lens_aperture_min);
            Log.e(LOCAL_TAG, "Aperture (f-number) set to minimum = " +
                    Float.toString(lens_aperture_min));
        }

        // Use maximal (darkest) lens filtering
        float[] lens_filters = mCharacteristics.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES);
        if (lens_filters == null) {
            Log.e(LOCAL_TAG, "***Lens filters unavailable***");
        }
        else {
            for (float filter : lens_filters) {
                Log.e(LOCAL_TAG, "Lens filter option [EV] = " + Float.toString(filter));
            }
            float lens_filter_max = lens_filters[ lens_filters.length - 1 ];
            mCapture_builder.set(CaptureRequest.LENS_FILTER_DENSITY, lens_filter_max);
            Log.e(LOCAL_TAG, "Filter density set to maximum [EV] = " +
                    Float.toString(lens_filter_max));
        }

        // Use maximal focal length
        float[] lens_focal_lengths = mCharacteristics.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        if (lens_focal_lengths == null) {
            Log.e(LOCAL_TAG, "***Lens focal lengths unavailable***");
        }
        else {
            float lens_focal_max = lens_focal_lengths[ lens_focal_lengths.length - 1 ];
            mCapture_builder.set(CaptureRequest.LENS_FOCAL_LENGTH, lens_focal_max);
            Log.e(LOCAL_TAG, "Focal length set to maximum [mm] = " +
                    Float.toString(lens_focal_max));
        }

        // Disable mechanical lens stabilization
        mCapture_builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
        Log.e(LOCAL_TAG, "Optical stabilization disabled");

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Configures (disables) statistical metadata abilities of the camera
     * (called from configureCamera)
     */
    private void configureStatistics() {
        final String LOCAL_TAG = TAG.concat(".configureStatistics()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Disable face detection
        mCapture_builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF);
        Log.e(LOCAL_TAG, "Face detection disabled");

        // Disable hot-pixel map mode (possibly enable this for comparisons)
        mCapture_builder.set(CaptureRequest.STATISTICS_HOT_PIXEL_MAP_MODE, false);
        Log.e(LOCAL_TAG, "Hot-pixel map disabled");

        // Disable lens shading map mode
        mCapture_builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE,
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
     * (called from configureCamera)
     */
    private void configureTiming() {
        final String LOCAL_TAG = TAG.concat(".configureTiming()");
        Log.e(LOCAL_TAG, DIVIDER);

        // Set exposure time to minimum
        Range<Long> exposure_range = mCharacteristics.get(
                CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        if (exposure_range == null) {
            Log.e(LOCAL_TAG, "***Exposure range unknown***");
        }
        else {
            Long exposure_min = exposure_range.getLower();
            mCapture_builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure_min);
            Log.e(LOCAL_TAG, "Minimum exposure time [ns] = " + Long.toString(exposure_min));
        }

        // Set frame fastest frame duration
        long frame_min = mStream_config.getOutputMinFrameDuration(IMAGE_FORMAT, mImage_size);
        mCapture_builder.set(CaptureRequest.SENSOR_FRAME_DURATION, frame_min);
        Log.e(LOCAL_TAG, "Minimum frame duration [ns] = " + Long.toString(frame_min));

        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Never used except for debug purposes.
     * Print supported image formats to the Log.
     */
    private void debugGetFormats() {
        final String LOCAL_TAG = TAG.concat(".debugGetFormats()");
        Log.e(LOCAL_TAG, DIVIDER);

        int[] supported = mStream_config.getOutputFormats();
        for (int format : supported) {
            switch (format) {
                case ImageFormat.DEPTH16 :
                    Log.e(LOCAL_TAG, "Depth16 supported"); break;
                case ImageFormat.DEPTH_POINT_CLOUD :
                    Log.e(LOCAL_TAG, "Depth point cloud supported"); break;
                case ImageFormat.FLEX_RGB_888 :
                    Log.e(LOCAL_TAG, "Flex RGB 888 supported"); break;
                case ImageFormat.JPEG :
                    Log.e(LOCAL_TAG, "JPEG supported"); break;
                case ImageFormat.NV16 :
                    Log.e(LOCAL_TAG, "NV 16 supported"); break;
                case ImageFormat.NV21 :
                    Log.e(LOCAL_TAG, "NV 21 supported"); break;
                case ImageFormat.PRIVATE :
                    Log.e(LOCAL_TAG, "Private supported"); break;
                case ImageFormat.RAW10 :
                    Log.e(LOCAL_TAG, "Raw 10 supported"); break;
                case ImageFormat.RAW12 :
                    Log.e(LOCAL_TAG, "Raw 12 supported"); break;
                case ImageFormat.RAW_PRIVATE :
                    Log.e(LOCAL_TAG, "Raw private supported"); break;
                case ImageFormat.RAW_SENSOR :
                    Log.e(LOCAL_TAG, "Raw sensor supported"); break;
                case ImageFormat.RGB_565 :
                    Log.e(LOCAL_TAG, "RGB 586 supported"); break;
                case ImageFormat.UNKNOWN :
                    Log.e(LOCAL_TAG, "Unknown supported"); break;
                case ImageFormat.YUV_420_888 :
                    Log.e(LOCAL_TAG, "YUV 420 888 supported"); break;
                case ImageFormat.YUV_422_888 :
                    Log.e(LOCAL_TAG, "YUV 422 888 supported"); break;
                case ImageFormat.YUV_444_888 :
                    Log.e(LOCAL_TAG, "YUV 444 888 supported"); break;
                case ImageFormat.YUY2 :
                    Log.e(LOCAL_TAG, "YUY2 supported"); break;
                case ImageFormat.YV12 :
                    Log.e(LOCAL_TAG, "YV12 supported"); break;
                default :
                    Log.e(LOCAL_TAG, "Something else supported: " + Integer.toString(format));
                    break;
            }
        }
        Log.e(LOCAL_TAG, "RETURN");
    }

}
