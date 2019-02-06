package Camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;

import Logging.DividerStyle;
import Logging.OutStream;
import Logging.ShrampLogger;

/**
 * ShrampCameraDevice sets up, shuts down and controls the actions (onOpened, etc)
 * of a camera on this device.
 */
@TargetApi(21) // Lollipop
class ShrampCameraDevice {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // passed into constructor
    private CameraCharacteristics mCameraCharacteristics;

    // created in constructor
    private HandlerThread       mHandlerThread;
    private Handler             mHandler;
    private String              mName;
    private Integer             mPriority;
    private ShrampStateCallback mStateCallback; // nested class defined at bottom

    // created in ShrampStateCallback.onOpen()
    private ShrampCameraCaptureSession mShrampCameraCaptureSession;

    // time to wait for threads to quit [miliseconds]
    // 0 means wait forever
    private static final Long JOIN_MAX_WAIT_IN_MS = 0L;

     // Logging object
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final String TAG = "ShrampCameraDevice";

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Create new ShrampCameraDevice to control a camera
     * @param characteristics the camera characteristics of the camera device of interest
     * @param name name of the HandlerThread to run the camera
     * @return ShrampCameraDevice instance
     */
    ShrampCameraDevice(@NonNull CameraCharacteristics characteristics, @NonNull String name) {
        this(characteristics, name, Process.THREAD_PRIORITY_DEFAULT);
    }

    /**
     * Create new ShrampCameraDevice to control a camera
     * @param characteristics the camera characteristics of the camera device of interest
     * @param name name of the HandlerThread to run the camera
     * @param priority custom priority for the HandlerThread (see android.os.Process)
     * @return ShrampCameraDevice instance
     */
    ShrampCameraDevice(@NonNull CameraCharacteristics characteristics,
                       @NonNull String name, int priority) {

        mLogger.divider(DividerStyle.Strong);

        mName     = name;
        mPriority = priority;
        mCameraCharacteristics = characteristics;

        mLogger.log(TAG, "Initializing callback and starting background thread");
        restart();

        mLogger.log(TAG, "return;");
    }

    /**
     * If the HandlerThread has been stopped, it can be restarted here
     */
    void restart() {
        final String LTAG = TAG.concat("restart()");
        mLogger.logTrace();

        mStateCallback = new ShrampStateCallback();
        mHandlerThread = new HandlerThread(mName, mPriority);
        mHandlerThread.start();  // must start before calling .getLooper()
        mHandler       = new Handler(mHandlerThread.getLooper());

        mLogger.log(LTAG, "Callback initialized and background thread " + mName + "started");
        mLogger.log(LTAG, "return;");
    }

    /**
     * Call to properly close the camera device and stop the background thread
     */
    void close() {
        final String LTAG = TAG.concat("close()");
        mLogger.divider(DividerStyle.Weak);
        mLogger.logTrace();

        mLogger.log(LTAG, "Closing camera now");
        mStateCallback.close();
        mStateCallback = null;

        mLogger.log(LTAG, "Ending " + mName + " thread");
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join(JOIN_MAX_WAIT_IN_MS);
        }
        catch (InterruptedException e) {
            // TODO ERROR
            mLogger.log(LTAG, "ERROR:  Interrupted Exception");
        }
        mHandlerThread = null;
        mHandler       = null;

        mLogger.log(LTAG, "return;");
    }

    /**
     * Access ShrampStateCallback for this device
     * @return ShrampStateCallback
     */
    ShrampStateCallback getStateCallback() { return mStateCallback; }

    /**
     * Access Handler for this device
     * @return Handler
     */
    Handler getHandler() { return mHandler; }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested class ShrampStateCallback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Class for handling asynchronous callbacks from camera device
     */
    class ShrampStateCallback extends CameraDevice.StateCallback {
        final String LTAG = TAG.concat(".ShrampStateCallback");

        // Error codes
        private static final int NO_ERROR = 0;

        // Activity lock
        private final Object LOCK = new Object();

        // set in onOpened, onClosed, onDisconnected, onError
        private CameraDevice mCameraDevice;
        private Integer      mError;

        ShrampStateCallback() { super(); }

        /**
         * Actions when camera is opened.
         * Configure camera for capture session.
         * @param camera CameraDevice that has been opened
         */
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            synchronized (LOCK) {
                mLogger.divider(DividerStyle.Normal);
                mLogger.logTrace();

                mCameraDevice = camera;
                mError        = NO_ERROR;

                mLogger.log(LTAG, "onOpened: creating ShrampCameraCaptureSession");
                mShrampCameraCaptureSession =
                        new ShrampCameraCaptureSession(mCameraDevice, mCameraCharacteristics);
                mLogger.log(LTAG, "onOpened: return;");
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
                mLogger.divider(DividerStyle.Normal);
                mLogger.logTrace();

                mCameraDevice = camera;
                mLogger.log(LTAG, "onClosed: return;");
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
                mLogger.divider(DividerStyle.Normal);
                mLogger.logTrace();

                mCameraDevice = camera;

                mLogger.log(LTAG, "onDisconnected: closing");
                close();
                mLogger.log(LTAG, "onDisconnected: return;");
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
                mLogger.divider(DividerStyle.Normal);
                mLogger.logTrace();

                mCameraDevice = camera;
                mError = error;

                switch (mError) {
                    case (ERROR_CAMERA_DEVICE): {
                        // TODO fatal error
                        mLogger.log(LTAG, "onError: ERROR_CAMERA_DEVICE");
                        break;
                    }
                    case (ERROR_CAMERA_DISABLED): {
                        // TODO disabled due to device policy
                        mLogger.log(LTAG, "onError: ERROR_CAMERA_DISABLED");
                        break;
                    }
                    case (ERROR_CAMERA_IN_USE): {
                        // TODO someone else is using the camera already
                        mLogger.log(LTAG, "onError: ERROR_CAMERA_IN_USE");
                        break;
                    }
                    case (ERROR_CAMERA_SERVICE): {
                        // TODO fatal error
                        mLogger.log(LTAG, "onError: ERROR_CAMERA_SERVICE");
                        break;
                    }
                    case (ERROR_MAX_CAMERAS_IN_USE): {
                        // TODO too many cameras are in use
                        mLogger.log(LTAG, "onError: ERROR_MAX_CAMERAS_IN_USE");
                    }
                }

                mLogger.log(LTAG, "onError: closing");
                close();
                mLogger.log(LTAG, "onError: return;");
            }
        }

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

        /**
         * Close camera device
         */
        void close() {
            synchronized (LOCK) {
                mLogger.divider(DividerStyle.Normal);
                mLogger.logTrace();

                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                mLogger.log(LTAG, "close: return;");
            }
        }
    }

}