/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 20 April 2019
 */

package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.os.Process;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.capture.CaptureConfiguration;

/**
 * Settings that effect all aspects of this application, the most important one is "FlightPlan"
 * in "Performance and Data Capture Control"
 *
 * TODO: A general app comment, passing image metadata is no longer necessary, in the future
 * TODO: consider removing DataQueue and updating ImageProcessor.  However, leaving it in does not
 * TODO: seem to effect performance.
 */
@TargetApi(21)
abstract public class GlobalSettings {


    // Performance and Data Capture Control
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * The device will run the operations listed in FlightPlan()
     */
    public static class FlightPlan {

        // TODO: in the future, this will be a state machine
        private static final List<CaptureConfiguration> mFlightPlan = new ArrayList<>();

        /**
         * ___________________________________________________________________
         * !!!!!!!!!!!!!!!!!!!!!!    >> Edit this <<    !!!!!!!!!!!!!!!!!!!!!!
         * *******************************************************************
         * The device will run the operations listed.
         * e.g. mFlightPlan.add( CaptureConfiguration.newXXX() )
         *      where XXX can be "CoolDownSession", "WarmUpSession", "DataSession", etc...
         *      See CaptureConfiguration for what's available
         */
        public FlightPlan() {
            addCalibrationCycle();
            //mFlightPlan.add(CaptureConfiguration.newDataSession(FPS_30, 100, TEMPERATURE_HIGH, 0., 0, false));
        }

        /**
         * @return The next operation to execute
         */
        @Nullable
        public CaptureConfiguration getNext() {
            if (mFlightPlan.size() > 0) {
                return mFlightPlan.remove(0);
            }
            else {
                return null;
            }
        }

        /**
         * A complete calibration cycle typically takes around 30 minutes
         */
        private void addCalibrationCycle() {
            double temperature_low = Math.min(TEMPERATURE_START, TEMPERATURE_GOAL);
            temperature_low = Math.max(TEMPERATURE_LOW, temperature_low);

            // Warm up if the phone is too cold
            mFlightPlan.add(CaptureConfiguration.newWarmUpSession(temperature_low, 10, 1000));

            // Cool down if the phone is too hot
            mFlightPlan.add(CaptureConfiguration.newCoolDownSession(temperature_low, 10));

            // Calibrate Cold-Fast/Slow
            mFlightPlan.add(CaptureConfiguration.newColdFastCalibration());
            mFlightPlan.add(CaptureConfiguration.newColdSlowCalibration());

            // Warm up to Hot
            mFlightPlan.add(CaptureConfiguration.newWarmUpSession(TEMPERATURE_HIGH, 10, 1000));

            // Calibrate Hot-Fast/Slow
            mFlightPlan.add(CaptureConfiguration.newHotFastCalibration());
            mFlightPlan.add(CaptureConfiguration.newHotSlowCalibration());

            // Cool down to data taking temperature
            mFlightPlan.add(CaptureConfiguration.newCoolDownSession(TEMPERATURE_GOAL, 10));

            // Discover optimal frame rate for data taking
            mFlightPlan.add(CaptureConfiguration.newOptimizationSession(null));
        }
    }


    // Feature Locks
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Force device to use YUV_420_888 output format, and/or automatic exposure/white balance/focus
    // FYI, max effective fps for RAW_SENSOR is normally around 15 fps depending on the phone (hardware limited),
    //      max effective fps for YUV_420_888 is normal around 20 fps (buffering limited)
    //      also FYI, RenderScript runs around 15 fps or so for both
    public static final Boolean DISABLE_RAW_OUTPUT      = false;
    public static final Boolean FORCE_CONTROL_MODE_AUTO = false;

    // TODO: FORCE_WORST_CONFIGURATION


    // Useful Definitions
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Convenient exposure times in nanoseconds
    public static final Long FPS_30 =   33333333L;
    public static final Long FPS_20 =   50000000L;
    public static final Long FPS_15 =   66666666L;
    public static final Long FPS_10 =  100000000L;
    public static final Long FPS_05 =  200000000L;
    public static final Long FPS_01 = 1000000000L;

    // Convenient temperatures in Celsius
    public static final Double TEMPERATURE_LOW    = 20.;
    public static final Double TEMPERATURE_GOAL   = 30.;
    public static final Double TEMPERATURE_HIGH   = 40.;
    public static       Double TEMPERATURE_START; // set on app start by MasterController


    // ShRAMP data folder
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Erases everything at start if true
    public static final boolean START_FROM_SCRATCH = true;


    // Camera Preference
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public static final CameraController.Select PREFERRED_CAMERA = CameraController.Select.BACK;
    public static final CameraController.Select SECONDARY_CAMERA = CameraController.Select.FRONT;


    // Output Surface Use
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Enable live preview on screen by setting TEXTURE_VIEW_SURFACE_ENABLED to true
    public static final Boolean TEXTURE_VIEW_SURFACE_ENABLED = false;
    public static final Boolean IMAGE_READER_SURFACE_ENABLED = true;  // always true, never false


    // Resource Limits
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Memory and ImageReader buffer limits
    public static final Long    AMPLE_MEMORY_MiB        = 200L;
    public static final Long    LOW_MEMORY_MiB          = 100L;
    public static final Integer MAX_SIMULTANEOUS_IMAGES = 1;

    // RenderScript
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // RenderScript can be run in "low power" mode and "low" priority without sacrificing performance
    public static final Integer RENDER_SCRIPT_FLAGS = RenderScript.CREATE_FLAG_LOW_LATENCY & RenderScript.CREATE_FLAG_LOW_POWER;
    public static final RenderScript.Priority RENDER_SCRIPT_PRIORITY = RenderScript.Priority.LOW;


    // Thread Priorities
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Priorities of all co-running threads of the app, optimized for best performance
    public static final Integer CAPTURE_MANAGER_THREAD_PRIORITY   = Process.THREAD_PRIORITY_URGENT_AUDIO;
    public static final Integer CAMERA_CONTROLLER_THREAD_PRIORITY = Process.THREAD_PRIORITY_LESS_FAVORABLE;
    public static final Integer DATA_QUEUE_THREAD_PRIORITY        = Process.THREAD_PRIORITY_AUDIO;
    public static final Integer IMAGE_READER_THREAD_PRIORITY      = Process.THREAD_PRIORITY_URGENT_AUDIO;
    public static final Integer IMAGE_PROCESSOR_THREAD_PRIORITY   = Process.THREAD_PRIORITY_LESS_FAVORABLE;
    public static final Integer STORAGE_MEDIA_THREAD_PRIORITY     = Process.THREAD_PRIORITY_LESS_FAVORABLE;

    // Delays
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Default wait time for wait() calls, 20 milliseconds
    public static final Long DEFAULT_WAIT_MS = FPS_05 / 1000000;

    // Long wait time for wait() calls, 1 minute
    public static final Long DEFAULT_LONG_WAIT = 60 * 1000L;


    // Time-Codes
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // If true, time-code characters are chosen to allow a chance at the occasional vulgarity
    public static final boolean ENABLE_VULGARITY = true;


    // FPS Range (only effective for auto exposure/white-balance/focus mode)
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Maximum FPS this app will support
    public static final int MAX_FPS = 30;

    // Maximum FPS range acceptable for this app, e.g. FPS range [10,12] has a range of 2
    public static final int MAX_FPS_DIFF = 2;


    // File extensions
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public static final String MEAN_FILE   = ".mean";
    public static final String STDDEV_FILE = ".stddev";
    public static final String STDERR_FILE = ".stderr";
    public static final String MASK_FILE   = ".mask";
    public static final String SIGNIF_FILE = ".signif";
    public static final String IMAGE_FILE  = ".frame";


    // Debugging
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Prevent queuing anything (all image data and metadata are dropped instantly).
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_QUEUE = false;

    // Prevent image processing with RenderScript from occurring.
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_PROCESSING = false;

    // Prevent any and all file saving.
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_ALL_SAVING = true;

    // Save full image data every INTERVAL (provided DISABLE_ALL_SAVING isn't true).
    // False for normal operation.
    public static final Boolean DEBUG_ENABLE_IMAGE_SAVING   = false;
    public static final Integer DEBUG_IMAGE_SAVING_INTERVAL = 10;

    // Save a frame's pixel significance every INTERVAL (provided DISABLE_ALL_SAVING isn't true).
    // False for normal operation.
    public static final Boolean DEBUG_SAVE_SIGNIFICANCE            = false;
    public static final Integer DEBUG_SIGNIFICANCE_SAVING_INTERVAL = 10;

    // Save new statistics (provided DISABLE_ALL_SAVING isn't true).
    // True for normal operation.
    public static final Boolean DEBUG_SAVE_MEAN   = true;
    public static final Boolean DEBUG_SAVE_STDDEV = true;

    // Allow significance threshold to increase.
    // TODO: threshold and its increase are still under investigation
    //public static final Boolean DEBUG_ENABLE_THRESHOLD_INCREASE = false;

}