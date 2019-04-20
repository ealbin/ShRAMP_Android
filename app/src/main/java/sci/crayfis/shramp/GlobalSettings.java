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
import sci.crayfis.shramp.camera2.capture.CaptureController;

/**
 * Settings that effect all aspects of this application
 */
@TargetApi(21)
abstract public class GlobalSettings {

    // Performance and Data Capture Control
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


    public static class FlightPlan {
        private static final List<CaptureConfiguration> mFlightPlan = new ArrayList<>();

        public FlightPlan() {
            //addCalibrationCycle();
            mFlightPlan.add(CaptureConfiguration.newDataSession(FPS_10, 10, TEMPERATURE_HIGH, 0., 1, false));
        }

        @Nullable
        public CaptureConfiguration getNext() {
            if (mFlightPlan.size() > 0) {
                return mFlightPlan.remove(0);
            }
            else {
                return null;
            }
        }


        private void addCalibrationCycle() {
            double temperature_low = Math.min(TEMPERATURE_START, 25.);
            temperature_low = Math.max(TEMPERATURE_LOW, temperature_low);
            mFlightPlan.add(CaptureConfiguration.newCoolDownSession(temperature_low, 10));
            mFlightPlan.add(CaptureConfiguration.newColdFastCalibration());
            mFlightPlan.add(CaptureConfiguration.newColdSlowCalibration());
            mFlightPlan.add(CaptureConfiguration.newWarmUpSession(TEMPERATURE_HIGH, 10, 1000));
            mFlightPlan.add(CaptureConfiguration.newHotFastCalibration());
            mFlightPlan.add(CaptureConfiguration.newHotSlowCalibration());
            mFlightPlan.add(CaptureConfiguration.newCoolDownSession(TEMPERATURE_GOAL, 10));
        }
    }





    // TODO: description
    public static final Integer FPS_LOCK_N_FRAMES    = 0;
    public static final Integer CALIBRATION_N_FRAMES = 0;
    public static final Integer DATARUN_N_FRAMES     = 10;

    // TODO: description
    public static final Double  DUTY_THRESHOLD     = 0.98;
    public static final Integer FPS_ATTEMPT_LIMIT  = 0;
    public static final Integer DATA_ATTEMPT_LIMIT = 1;//10;

    // TODO: description
    public static final Long FPS_30 =   33333333L;
    public static final Long FPS_20 =   50000000L;
    public static final Long FPS_15 =   66666666L;
    public static final Long FPS_10 =  100000000L;
    public static final Long FPS_05 =  200000000L;
    public static final Long FPS_01 = 1000000000L;
    public static final Long DEFAULT_EXPOSURE_NANOS = FPS_20;
    public static final Long DEFAULT_SLOW_FPS = FPS_05;
    public static final Long DEFAULT_FAST_FPS = FPS_30;
    public static final Long DEFAULT_WAIT_MS = FPS_05 / 1000000;
    public static final Long DEFAULT_LONG_WAIT = 60 * 1000L; // 1 minute
    public static final Boolean CONSTANT_FPS = true;

    public static final Double TEMPERATURE_LOW    = 20.; // C
    public static final Double TEMPERATURE_GOAL   = 30.; // C
    public static final Double TEMPERATURE_HIGH   = 40.; // C
    public static       Double TEMPERATURE_START; // set on app start, Celsius

    // TODO: description
    public static final Integer MAX_FRAMES_ABOVE_THRESHOLD = Math.min(
            Math.max(10, DATARUN_N_FRAMES / 20), 400 ); // TODO: min( max_number_with_disk_space, max(abs min, fraction of frames) )
    public static final Double THRESHOLD_STEP = 0.5;

    // ShRAMP data folder
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public static final boolean START_FROM_SCRATCH = true;


    // Feature Locks
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Boolean DISABLE_RAW_OUTPUT      = false;
    public static final Boolean FORCE_CONTROL_MODE_AUTO = false;

    // Surface Use
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Boolean TEXTURE_VIEW_SURFACE_ENABLED = false;
    public static final Boolean IMAGE_READER_SURFACE_ENABLED = true;

    // Resource Limits
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Long    AMPLE_MEMORY_MiB        = 200L;
    public static final Long    LOW_MEMORY_MiB          = 100L;
    public static final Integer MAX_SIMULTANEOUS_IMAGES = 1;

    // RenderScript
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Integer RENDER_SCRIPT_FLAGS                  = RenderScript.CREATE_FLAG_NONE;
    public static final RenderScript.Priority RENDER_SCRIPT_PRIORITY = RenderScript.Priority.NORMAL;

    // Thread Priorities
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Integer CAPTURE_MANAGER_THREAD_PRIORITY   = Process.THREAD_PRIORITY_URGENT_AUDIO;
    public static final Integer CAMERA_CONTROLLER_THREAD_PRIORITY = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer DATA_QUEUE_THREAD_PRIORITY        = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer IMAGE_READER_THREAD_PRIORITY      = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer IMAGE_PROCESSOR_THREAD_PRIORITY   = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer STORAGE_MEDIA_THREAD_PRIORITY     = Process.THREAD_PRIORITY_BACKGROUND;

    // Camera Preference
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final CameraController.Select PREFERRED_CAMERA = CameraController.Select.BACK;
    public static final CameraController.Select SECONDARY_CAMERA = CameraController.Select.FRONT;

    // Debugging
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Prevent a calibration cycle.
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_CALIBRATION = true;

    // Prevent queuing anything (all image data and metadata are dropped instantly).
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_QUEUE = false;

    // Prevent image processing with RenderScript from occurring.
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_PROCESSING = false;

    // Prevent any and all file saving.
    // False for normal operation.
    public static final Boolean DEBUG_DISABLE_ALL_SAVING = false;

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
    public static final Boolean DEBUG_SAVE_MEAN   = false;
    public static final Boolean DEBUG_SAVE_STDDEV = false;

    // Allow significance threshold to increase.
    // TODO: not sure what's normal operation.
    public static final Boolean DEBUG_ENABLE_THRESHOLD_INCREASE = false;

    // TODO: vulgarity, fps max, fps step
    // TODO: passing image metadata is no longer necessary


}