/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.os.Process;
import android.renderscript.RenderScript;

import sci.crayfis.shramp.battery.BatteryController;
import sci.crayfis.shramp.camera2.CameraController;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class GlobalSettings {

    // Performance and Control
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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
    public static final Long DEFAULT_LONG_WAIT = 20 * 1000L;
    public static final Boolean CONSTANT_FPS = true;

    public static final Double TEMPERATURE_LOW    = 20.; // C
    public static final Double TEMPERATURE_GOAL   = 30.; // C
    public static final Double TEMPERATURE_HIGH   = 40.; // C
    public static final Double OVER_TEMPERATURE   = 10.; // C
    public static       Double TEMPERATURE_START;

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
    public static final Boolean DISABLE_RAW_OUTPUT      = true;
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

    // TODO: description
    public static final Boolean DEBUG_DISABLE_CALIBRATION = true;
    public static final Boolean DEBUG_DISABLE_QUEUE       = false;
    public static final Boolean DEBUG_DISABLE_PROCESSING  = false;
    public static final Boolean DEBUG_DISABLE_SAVING      = false;
    public static final Boolean DEBUG_SAVE_SIGNIFICANCE   = false;
    public static final Boolean DEBUG_SAVE_MEAN           = false;
    public static final Boolean DEBUG_SAVE_STDDEV         = false;

}