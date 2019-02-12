package edu.crayfis.shramp.camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
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
import edu.crayfis.shramp.camera2.settings.MaineShrampSettings;
import edu.crayfis.shramp.logging.ShrampLogger;

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


    private ShrampCamSetup mShrampCamSetup;
    private ShrampCaptureSession   mShrampCaptureSession;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    // output surfaces linked with this camera
    private List<Surface>        mOutputSurfaces;

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

        mName     = name;
        mPriority = priority;
        mCameraCharacteristics = characteristics;

        mLogger.log("Initializing callback and starting background thread");
        startHandler();

        mLogger.log("return;");
    }

    /**
     * If the HandlerThread has been stopped, it can be restarted here
     */
    void startHandler() {

        mLogger.log("Starting thread: " + mName);
        mHandlerThread = new HandlerThread(mName, mPriority);
        mHandlerThread.start();  // must start before calling .getLooper()
        mHandler       = new Handler(mHandlerThread.getLooper());

        mLogger.log("Thread: " + mName + " started; return;");
    }

    /**
     * Access Handler for this device
     * @return Handler
     */
    Handler getHandler() { return mHandler; }

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
        synchronized (LOCK) {

            mError        = NO_ERROR;
            mCameraDevice = camera;

            mLogger.log("Camera opened, configuring for capture");

            MaineShrampSettings maineShrampSettings =
                    new MaineShrampSettings(mCameraCharacteristics, mCameraDevice);

            maineShrampSettings.logSettings();
            //maineShrampSettings.keyDump();

            mCaptureRequestBuilder = maineShrampSettings.getCaptureRequestBuilder();

            ShrampCamManager.Callback.cameraReady(this);
            mLogger.log("return;");
        }
    }

    /**
     * Actions when camera is closed.
     * No actions yet.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onClosed(@NonNull CameraDevice camera) {
        synchronized (LOCK) {

            mCameraDevice = camera;
            mLogger.log("No additional action defined; return;");
        }
    }

    /**
     * Actions when camera is disconnected.
     * Close camera.
     * @param camera CameraDevice that has been closed
     */
    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
        synchronized (LOCK) {
            mCameraDevice = camera;

            mLogger.log("Disconnected, closing");
            close();
            mLogger.log("return;");
        }
    }

    /**
     * Actions when camera has erred.
     * Determine error cause and close camera.
     * @param camera CameraDevice that has erred
     */
    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
        synchronized (LOCK) {

            mCameraDevice = camera;
            mError = error;

            switch (mError) {
                case (ERROR_CAMERA_DEVICE): {
                    // TODO fatal error
                    mLogger.log("ERROR_CAMERA_DEVICE");
                    break;
                }
                case (ERROR_CAMERA_DISABLED): {
                    // TODO disabled due to device policy
                    mLogger.log("ERROR_CAMERA_DISABLED");
                    break;
                }
                case (ERROR_CAMERA_IN_USE): {
                    // TODO someone else is using the camera already
                    mLogger.log("ERROR_CAMERA_IN_USE");
                    break;
                }
                case (ERROR_CAMERA_SERVICE): {
                    // TODO fatal error
                    mLogger.log("ERROR_CAMERA_SERVICE");
                    break;
                }
                case (ERROR_MAX_CAMERAS_IN_USE): {
                    // TODO too many cameras are in use
                    mLogger.log("ERROR_MAX_CAMERAS_IN_USE");
                }
            }

            mLogger.log("Fatal error, closing");
            close();
            mLogger.log("return;");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Access CameraDevice
     * @return CameraDevice
     */
    CameraDevice getCameraDevice() { return mCameraDevice; }

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

        mLogger.log("Closing camera device");
        if (mCameraDevice != null) {
            mShrampCaptureSession = null;
            mCameraDevice.close();
            mCameraDevice = null;
        }

        mLogger.log("Ending " + mName + " thread");
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join(JOIN_MAX_WAIT_IN_MS);
        }
        catch (InterruptedException e) {
            // TODO ERROR
            mLogger.log("ERROR: Interrupted Exception");
        }
        mHandlerThread = null;
        mHandler       = null;

        mLogger.log("return;");
    }


















    ////////////////////////////////////////////////////////////////////////////////////////////////

    void startCapture(List<Surface> outputSurfaces) {
        /*
        CaptureRequest.Builder builder;
        try {
             builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
             builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);

        }
        catch (Exception e) {
            return;
        }
        */
        mLogger.log("Creating capture session");
        if (mCameraDevice != null) {
            try {
                for (Surface surface : outputSurfaces) {
                    mCaptureRequestBuilder.addTarget(surface);
                    //builder.addTarget(surface);
                }
                CaptureRequest captureRequest = mCaptureRequestBuilder.build();
                //CaptureRequest request = builder.build();

                mShrampCaptureSession = new ShrampCaptureSession(captureRequest);
                //mShrampCaptureSession = new ShrampCaptureSession(request);
                mLogger.log(mShrampCaptureSession.toString());
                CameraCaptureSession.StateCallback callback = mShrampCaptureSession.getStateCallback();

                mLogger.log("createCaptureSession()");
                // use same thread/handler as camera device, set handler = null
                mCameraDevice.createCaptureSession(outputSurfaces, callback, null);
            }
            catch (CameraAccessException e ) {
                mLogger.log("ERROR: Camera Access Exception");
            }
        }

        mLogger.log("return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}