package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.os.Process;
import android.renderscript.RenderScript;

import sci.crayfis.shramp.camera2.CameraController;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class GlobalSettings {

    // Performance and Control
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Integer DEFAULT_N_FRAMES = 100;
    public static final Integer DATARUN_N_FRAMES = 100;//1000;

    // TODO: description
    public static final Double  DUTY_THRESHOLD     = 2.0;//0.97;
    public static final Integer FPS_ATTEMPT_LIMIT  = 1;//3; // minimum is 1
    public static final Integer DATA_ATTEMPT_LIMIT = 2;//10;

    // TODO: description
    private static final Long FPS_30 =  33333333L;
    private static final Long FPS_20 =  50000000L;
    private static final Long FPS_10 = 100000000L;
    public  static final Long DEFAULT_FRAME_EXPOSURE_NANOS = FPS_20;

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
    public static final Long AMPLE_MEMORY_MiB = 200L;
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

    // Camera Preference
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final CameraController.Select PREFERRED_CAMERA = CameraController.Select.BACK;
    public static final CameraController.Select SECONDARY_CAMERA = CameraController.Select.FRONT;

    // Debugging
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    public static final Boolean DEBUG_DISABLE_QUEUE      = false;
    public static final Boolean DEBUG_DISABLE_PROCESSING = false;

}