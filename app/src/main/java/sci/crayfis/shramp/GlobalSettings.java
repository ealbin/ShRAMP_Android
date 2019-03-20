package sci.crayfis.shramp;

import android.os.Process;
import android.renderscript.RenderScript;

import sci.crayfis.shramp.camera2.CameraController;

public abstract class GlobalSettings {

    public static final Integer CAPTURE_MANAGER_THREAD_PRIORITY   = Process.THREAD_PRIORITY_URGENT_AUDIO;
    public static final Integer CAMERA_CONTROLLER_THREAD_PRIORITY = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer IMAGE_READER_THREAD_PRIORITY      = Process.THREAD_PRIORITY_FOREGROUND;
    public static final Integer IMAGE_PROCESSOR_THREAD_PRIORITY   = Process.THREAD_PRIORITY_FOREGROUND;

    public static final CameraController.Select PREFERRED_CAMERA = CameraController.Select.BACK;
    public static final CameraController.Select SECONDARY_CAMERA = CameraController.Select.FRONT;

    public static final Boolean TEXTURE_VIEW_SURFACE_ENABLED = true;
    public static final Boolean IMAGE_READER_SURFACE_ENABLED = true;

    public static final Boolean DISABLE_RAW_OUTPUT      = false;
    public static final Boolean FORCE_CONTROL_MODE_AUTO = false;

    public static final Long    DEFAULT_FRAME_EXPOSURE_NANOS = 50000000L; // 20 fps
    public static final Integer DEFAULT_N_FRAMES             = 100;
    public static final Double  DUTY_THRESHOLD               = 0.97;
    public static final Integer FPS_ATTEMPT_LIMIT            = 3;

    public static final Integer RENDER_SCRIPT_FLAGS   = RenderScript.CREATE_FLAG_NONE;
    public static final Integer MAX_IMAGES            = 1;
    public static final Long    LOW_MEMORY_MiB        = 100L;
    public static final Long    SUFFICIENT_MEMORY_MiB = 200L;
    public static final Integer MAX_BACKLOG           = 20;
    public static final Integer ACCEPTABLE_BACKLOG    = 5;


    // Debugging
    public static final Boolean DEBUG_DISABLE_CAPTURE          = false;
    public static final Boolean DEBUG_STOP_CAPTURE_IMMEDIATELY = false;
    public static final Boolean DEBUG_NO_DATA_POSTING          = false;
    public static final Boolean DEBUG_START_STOP_CAPTURE       = false;
    public static final Boolean DEBUG_DISABLE_PROCESSING       = false;
    public static final Boolean DEBUG_RENDERSCRIPT_UPDATE      = false;
    public static final Boolean DEBUG_DISABLE_STATISTICS       = false;
    public static final Boolean DEBUG_STATISTICS_CHECK         = false;


}
