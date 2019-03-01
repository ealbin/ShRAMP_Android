package sci.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.view.Surface;

import java.util.List;

import Trash.ShrampCamSetup;
import Trash.ShrampCamCapture;
import sci.crayfis.shramp.camera2.settings.ShrampCamSettings;
import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * ShrampCam sets up, shuts down and controls the actions (onOpened, etc)
 * of a camera on this device.
 */
@TargetApi(21) // Lollipop
class ShrampCam extends CameraDevice.StateCallback {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Passed into constructor, info about this camera and settings for its thread
    private CameraCharacteristics  mCameraCharacteristics;
    private String                 mName;
    private Integer                mPriority;

    // Thread created in constructor
    private HandlerThread          mHandlerThread;
    private Handler                mHandler;

    // Setup in onOpen()
    private CameraDevice           mCameraDevice;

    private ShrampCamSettings      mShrampCamSettings;
    private ShrampCamCapture       mShrampCamCapture;
    private CaptureRequest.Builder mCaptureRequestBuilder;


    // time to wait for threads to quit [milliseconds]
    // 0 means wait forever
    private static final Long JOIN_MAX_WAIT_IN_MS = 0L;

    // Error codes
    private static final int NO_ERROR = 0;

    // Activity lock so only one camera on the smartphone is changing state at a time
    private static final Object LOCK = new Object();

    // set in onOpened, onClosed, onDisconnected, onError
    private Integer      mError;


    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Create new ShrampCam to control a camera
     * @param characteristics the camera characteristics of the camera device of interest
     * @param name name of the HandlerThread to run the camera
     * @return ShrampCam instance
     */
    ShrampCam(@NonNull CameraCharacteristics characteristics, @NonNull String name) {
        this(characteristics, name, Process.THREAD_PRIORITY_DEFAULT);
    }

    /**
     * Create new ShrampCam to control a camera
     * @param characteristics the camera characteristics of the camera device of interest
     * @param name name of the HandlerThread to run the camera
     * @param priority custom priority for the HandlerThread (see android.os.Process)
     * @return ShrampCam instance
     */
    ShrampCam(@NonNull CameraCharacteristics characteristics,
              @NonNull String name, int priority) {
        super();

        this.mName     = name;
        this.mPriority = priority;
        this.mCameraCharacteristics = characteristics;

        ShrampCam.mLogger.log("Initializing callback and starting background thread");
        this.startHandler();

        ShrampCam.mLogger.log("return;");
    }

    /**
     * If the HandlerThread has been stopped, it can be restarted here
     */
    void startHandler() {

        ShrampCam.mLogger.log("Starting thread: " + this.mName);
        this.mHandlerThread = new HandlerThread(this.mName, this.mPriority);
        this.mHandlerThread.start();  // must start before calling .getLooper()
        this.mHandler       = new Handler(this.mHandlerThread.getLooper());

        ShrampCam.mLogger.log("Thread: " + mName + " started; return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Is the camera currently open?
     * @return true if yes, false if no
     */
    boolean isCameraOpen() { return this.mCameraDevice == null; }

    /**
     * Actions when camera is opened.
     * Configure camera for capture session.
     * @param camera CameraDevice that has been opened
     */
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        //super.onOpened(camera); method is abstract
        synchronized (LOCK) {

            this.mError        = ShrampCam.NO_ERROR;
            this.mCameraDevice = camera;

            ShrampCam.mLogger.log("Camera opened, configuring for capture");

            this.mShrampCamSettings =
                    new ShrampCamSettings(this.mCameraCharacteristics, this.mCameraDevice);

            this.mShrampCamSettings.logSettings();

            // Optional, key dump all things supported
            //shrampCamSettings.keyDump();

            this.mCaptureRequestBuilder = this.mShrampCamSettings.getCaptureRequestBuilder();

            // pass configured CaptureRequest.Builder back to manager to complete setup
            ShrampCamManager.cameraReady(this);
            ShrampCam.mLogger.log("return;");
        }
    }

    /**
     * Actions when camera is closed.
     * No actions yet.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onClosed(@NonNull CameraDevice camera) {
        super.onClosed(camera);
        synchronized (LOCK) {
            this.mCameraDevice = camera;
            ShrampCam.mLogger.log("No additional action defined; return;");
        }
    }

    /**
     * Actions when camera is disconnected.
     * Close camera.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
        //super.onDisconnected(camera); method is abstract
        synchronized (LOCK) {
            this.mCameraDevice = camera;

            ShrampCam.mLogger.log("Disconnected, closing");
            this.close();
            ShrampCam.mLogger.log("return;");
        }
    }

    /**
     * Actions when camera has erred.
     * Determine error cause and close camera.
     * @param camera CameraDevice that has erred
     */
    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
        //super.onError(camera, error); method is abstract
        synchronized (LOCK) {

            this.mCameraDevice = camera;
            this.mError = error;

            switch (this.mError) {
                case (ShrampCam.ERROR_CAMERA_DEVICE): {
                    // TODO fatal error
                    ShrampCam.mLogger.log("ERROR_CAMERA_DEVICE");
                    break;
                }
                case (ShrampCam.ERROR_CAMERA_DISABLED): {
                    // TODO disabled due to device policy
                    ShrampCam.mLogger.log("ERROR_CAMERA_DISABLED");
                    break;
                }
                case (ShrampCam.ERROR_CAMERA_IN_USE): {
                    // TODO someone else is using the camera already
                    ShrampCam.mLogger.log("ERROR_CAMERA_IN_USE");
                    break;
                }
                case (ShrampCam.ERROR_CAMERA_SERVICE): {
                    // TODO fatal error
                    ShrampCam.mLogger.log("ERROR_CAMERA_SERVICE");
                    break;
                }
                case (ShrampCam.ERROR_MAX_CAMERAS_IN_USE): {
                    // TODO too many cameras are in use
                    ShrampCam.mLogger.log("ERROR_MAX_CAMERAS_IN_USE");
                }
            }

            ShrampCam.mLogger.log("Fatal error, closing");
            this.close();
            ShrampCam.mLogger.log("return;");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Access CameraDevice
     * @return CameraDevice
     */
    CameraDevice getCameraDevice() { return this.mCameraDevice; }

    /**
     * Access Handler for this device
     * @return Handler
     */
    Handler getHandler() { return this.mHandler; }

    CaptureRequest.Builder getCaptureRequestBuilder() {return this.mCaptureRequestBuilder;}

    ShrampCamSettings getShrampCamSettings() {return this.mShrampCamSettings;}

    /**
     * Access error code
     * @return int error code
     */
    int getErrorCode() { return this.mError; }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call to properly close the camera device and stop the background thread
     */
    void close() {

        ShrampCam.mLogger.log("Closing camera device");
        if (this.mCameraDevice != null) {
            this.mShrampCamCapture = null;
            this.mCameraDevice.close();
            this.mCameraDevice = null;
        }

        ShrampCam.mLogger.log("Ending " + this.mName + " thread");
        this.mHandlerThread.quitSafely();
        try {
            this.mHandlerThread.join(ShrampCam.JOIN_MAX_WAIT_IN_MS);
        }
        catch (InterruptedException e) {
            // TODO ERROR
            ShrampCam.mLogger.log("ERROR: Interrupted Exception");
        }
        this.mHandlerThread = null;
        this.mHandler       = null;

        ShrampCam.mLogger.log("return;");
    }
}