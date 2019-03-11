package Trash.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import Trash.camera2.settings.ShrampCamSettings;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HandlerManager;

/**
 * ShrampCam sets up, shuts down and controls the actions (onOpened, etc)
 * of a camera on this device.
 */
@TargetApi(21) // Lollipop
class ShrampCam extends CameraDevice.StateCallback {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Info about this camera
    private CameraCharacteristics  mCameraCharacteristics;

    // Thread created in constructor
    private Handler                mHandler;

    // Setup in onOpen()
    private CameraDevice           mCameraDevice;

    private ShrampCamSettings      mShrampCamSettings;
    private CaptureRequest.Builder mCaptureRequestBuilder;


    // time to wait for threads to quit [milliseconds]
    // 0 means wait forever
    private static final Long JOIN_MAX_WAIT_IN_MS = 0L;

    // Error codes
    private static final int NO_ERROR = 0;

    // set in onOpened, onClosed, onDisconnected, onError
    private Integer mError;

    // logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

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
        this(characteristics, name, Process.THREAD_PRIORITY_MORE_FAVORABLE);
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
        mCameraCharacteristics = characteristics;
        mHandler = HandlerManager.newHandler(name, priority);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Is the camera currently open?
     * @return true if yes, false if no
     */
    boolean isCameraOpen() { return mCameraDevice == null; }

    /**
     * Actions when camera is opened.
     * Configure camera for capture session.
     * @param camera CameraDevice that has been opened
     */
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        //super.onOpened(camera); method is abstract
        long startTime = SystemClock.elapsedRealtimeNanos();

        mError        = ShrampCam.NO_ERROR;
        mCameraDevice = camera;

        mLogger.log("Camera opened, configuring for capture");

        mShrampCamSettings = new ShrampCamSettings(mCameraCharacteristics, mCameraDevice);
        mShrampCamSettings.logSettings();


        // Optional, key dump all things supported
        //shrampCamSettings.keyDump();

        mCaptureRequestBuilder = mShrampCamSettings.getCaptureRequestBuilder();

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return via ShrampCamManager.cameraReady(); elapsed = " + elapsed + " [ns]");

        // pass configured CaptureRequest.Builder back to manager to complete setup
        ShrampCamManager.cameraReady(this);
    }

    /**
     * Actions when camera is closed.
     * No actions yet.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onClosed(@NonNull CameraDevice camera) {
        super.onClosed(camera);
        mLogger.log("Camera is confirmed closed");
    }

    /**
     * Actions when camera is disconnected.
     * Close camera.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
        //super.onDisconnected(camera); method is abstract
        mLogger.log("Camera is disconnected, closing");
        close();
    }

    /**
     * Actions when camera has erred.
     * Determine error cause and close camera.
     * @param camera CameraDevice that has erred
     */
    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
        //super.onError(camera, error); method is abstract
       mError = error;

        switch (this.mError) {
            case (ERROR_CAMERA_DEVICE): {
                // TODO: fatal error
                mLogger.log("ERROR_CAMERA_DEVICE");
                break;
            }
            case (ERROR_CAMERA_DISABLED): {
                // TODO: disabled due to device policy
                mLogger.log("ERROR_CAMERA_DISABLED");
                break;
            }
            case (ShrampCam.ERROR_CAMERA_IN_USE): {
                // TODO: someone else is using the camera already
                mLogger.log("ERROR_CAMERA_IN_USE");
                break;
            }
            case (ShrampCam.ERROR_CAMERA_SERVICE): {
                // TODO: fatal error
                mLogger.log("ERROR_CAMERA_SERVICE");
                break;
            }
            case (ShrampCam.ERROR_MAX_CAMERAS_IN_USE): {
                // TODO: too many cameras are in use
                mLogger.log("ERROR_MAX_CAMERAS_IN_USE");
            }
        }

        mLogger.log("Fatal error, closing");
        close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Access CameraDevice
     * @return CameraDevice
     */
    CameraDevice getCameraDevice() { return mCameraDevice; }

    /**
     * Access Handler for this device
     * @return Handler
     */
    Handler getHandler() { return mHandler; }

    CaptureRequest.Builder getCaptureRequestBuilder() {return mCaptureRequestBuilder;}

    ShrampCamSettings getShrampCamSettings() {return mShrampCamSettings;}

    /**
     * Access error code
     * @return int error code
     */
    int getErrorCode() { return mError; }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call to properly close the camera device and stop the background thread
     */
    void close() {
        long startTime = SystemClock.elapsedRealtimeNanos();

        mLogger.log("Closing camera device");
        if (this.mCameraDevice != null) {
           mCameraDevice.close();
           mCameraDevice = null;
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }
}