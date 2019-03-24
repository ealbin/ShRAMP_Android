package sci.crayfis.shramp;

import android.os.Process;
import android.renderscript.RenderScript;

import sci.crayfis.shramp.camera2.CameraController;

public abstract class GlobalSettings {

    public static final Integer CAPTURE_MANAGER_THREAD_PRIORITY   = Process.THREAD_PRIORITY_URGENT_AUDIO;
    public static final Integer CAMERA_CONTROLLER_THREAD_PRIORITY = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer IMAGE_READER_THREAD_PRIORITY      = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer IMAGE_PROCESSOR_THREAD_PRIORITY   = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer DATA_QUEUE_THREAD_PRIORITY        = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer STREAM_PROCESSOR_THREAD_PRIORITY  = Process.THREAD_PRIORITY_FOREGROUND;

    public static final RenderScript.Priority RENDER_SCRIPT_PRIORITY = RenderScript.Priority.NORMAL;

    public static final CameraController.Select PREFERRED_CAMERA = CameraController.Select.BACK;
    public static final CameraController.Select SECONDARY_CAMERA = CameraController.Select.FRONT;

    public static final Boolean TEXTURE_VIEW_SURFACE_ENABLED = false;
    public static final Boolean IMAGE_READER_SURFACE_ENABLED = true;

    public static final Boolean DISABLE_RAW_OUTPUT      = false;
    public static final Boolean FORCE_CONTROL_MODE_AUTO = false;

    private static final Long FPS_30 =  33333333L;
    private static final Long FPS_20 =  50000000L;
    private static final Long FPS_10 = 100000000L;

    public static final Long    DEFAULT_FRAME_EXPOSURE_NANOS = FPS_10;
    public static final Integer DEFAULT_N_FRAMES             = 1000;
    public static final Double  DUTY_THRESHOLD               = 0.97;
    public static final Integer FPS_ATTEMPT_LIMIT            = 2;

    public static final Integer RENDER_SCRIPT_FLAGS   = RenderScript.CREATE_FLAG_NONE;
    public static final Integer MAX_IMAGES            = 1;
    public static final Long    LOW_MEMORY_MiB        = 100L;
    public static final Long    SUFFICIENT_MEMORY_MiB = 200L;
    public static final Integer MAX_BACKLOG           = 20;
    public static final Integer ACCEPTABLE_BACKLOG    = 5;


    // Debugging
    public static final Boolean DEBUG_DISABLE_QUEUE      = false;
    public static final Boolean DEBUG_DISABLE_PROCESSING = false;

}
