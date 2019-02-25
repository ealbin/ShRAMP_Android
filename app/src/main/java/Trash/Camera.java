package Trash;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import sci.crayfis.shramp.MaineShRAMP;

@TargetApi(Build.VERSION_CODES.LOLLIPOP) // 21
public class Camera extends CameraSetup {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "Camera";
    private final static String DIVIDER = "---------------------------------------------";

    protected final static String THREAD_NAME = "Camera_Thread";

    // callbacks
    protected CameraDevice.StateCallback           mDevice_state_callback;
    protected CameraCaptureSession.StateCallback   mSession_state_callback;
    protected CameraCaptureSession.CaptureCallback mSession_capture_callback;

    // camera
    protected Handler       mHandler;
    protected HandlerThread mHandler_thread;

    // capture
    protected CaptureRequest mCapture_request;
    protected CameraCaptureSession mCapture_session;
    protected CaptureProcessing mCapture_processing;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Create Camera object for controlling a physical camera device
     * @param main_activity reference to MaineShRAMP
     * @param quit_action reference to method to run if fatal error is encountered
     */
    Camera(@NonNull MaineShRAMP main_activity, @NonNull Runnable quit_action) {
        super(main_activity, quit_action);

        final String LOCAL_TAG = TAG.concat(".Camera(MaineShRAMP, Runnable)");
        Log.e(LOCAL_TAG, DIVIDER);

        Log.e(LOCAL_TAG, "Instantiating callbacks");
        instantiateCallbacks();

        Log.e(LOCAL_TAG, "Instantiating handlers");
        mHandler_thread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mHandler_thread.start();
        mHandler = new Handler(mHandler_thread.getLooper());

        Log.e(LOCAL_TAG, "Opening camera");
        try {
//            mManager.openCamera(mBack_camera_id, mDevice_state_callback, mHandler);
        }
        catch (Exception e) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
        }
        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Instantiate camera callback functions
     */
    private void instantiateCallbacks() {
        final String LOCAL_TAG = TAG.concat(".instantiateCallbacks()");
        Log.e(LOCAL_TAG, DIVIDER);

        instantiateDeviceState();
        instantiateSessionState();
        instantiateSessionCapture();
        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Instantiate CameraDevice.StateCallback
     * Define camera actions for onOpened, onDisconnected and onError
     * mDevice is set in onOpened
     * configureCamera() is called in onOpened
     */
    private void instantiateDeviceState() {
        final String LOCAL_TAG = TAG.concat(".instantiateDeviceState()");

        mDevice_state_callback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera_device) {
                Log.e(LOCAL_TAG, "onOpened(CameraDevice)");

                mDevice = camera_device;

                Log.e(LOCAL_TAG, "Configuring camera");
                configureCamera();

                Log.e(LOCAL_TAG, "Setting up capture processing");
//                mCapture_processing = new CaptureProcessing(mMaine_shramp, mImage_size);
                mCapture_builder.addTarget(mCapture_processing.mSurface);

                Log.e(LOCAL_TAG, "Shutting down for now");
                shutdown();
                //createStillSession();
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera_device) {
                Log.e(LOCAL_TAG, "onDisconnected(CameraDevice)");
                shutdown();
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onError(@NonNull CameraDevice camera_device, int error) {
                Log.e(LOCAL_TAG, "onError(CameraDevice, int)");
                // TODO process error
                shutdown();
                Log.e(LOCAL_TAG, "RETURN");
            }
        };
    }

    /**
     * Instantiate CameraCaptureSession.StateCallback
     * Define session actions for onConfigured and onConfiguredFailed
     */
    private void instantiateSessionState() {
        final String LOCAL_TAG = TAG.concat(".instantiateSessionState()");

        mSession_state_callback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Log.e(LOCAL_TAG, "onConfigured(CameraCaptureSession)");
                mCapture_request = mCapture_builder.build();
                mCapture_session = session;
                try {
                    mCapture_session.setRepeatingRequest(mCapture_request, null, mHandler);
                }
                catch (Exception e) {
                    Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
                }
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(LOCAL_TAG, "onConfigureFailed(CameraCaptureSession)");
                Log.e(LOCAL_TAG, "RETURN");
            }
        };
    }

    /**
     * Instantiate CameraCaptureSession.CaptureCallback
     * Define session actions for onCaptureStarted, onCaptureProgressed, onCaptureCompleted,
     * onCaptureFailed, onCaptureSequenceCompleted, onCaptureSequenceAborted and onCaptureBufferLost
     */
    private void instantiateSessionCapture() {
        final String LOCAL_TAG = TAG.concat(".instantiateSessionCapture()");

        mSession_capture_callback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                         @NonNull CaptureRequest request,
                                         long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
                Log.e(LOCAL_TAG, "onCaptureStarted(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
                Log.e(LOCAL_TAG, "onCaptureProgressed(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG,"RETURN");
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Log.e(LOCAL_TAG, "onCaptureCompleted(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Log.e(LOCAL_TAG, "onCaptureFailed(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                                   int sequenceId, long frameNumber) {
                super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                Log.e(LOCAL_TAG, "onCaptureSequenceCompleted(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session,
                                                 int sequenceId) {
                super.onCaptureSequenceAborted(session, sequenceId);
                Log.e(LOCAL_TAG, "onCaptureSequenceAborted(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onCaptureBufferLost(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull Surface target, long frameNumber) {
                super.onCaptureBufferLost(session, request, target, frameNumber);
                Log.e(LOCAL_TAG, "onCaptureBufferLost(CameraCaptureSession, ...)");
                Log.e(LOCAL_TAG, "RETURN");
            }
        };
    }

    /**
     * Safely shut down physical camera device and free handlers
     */
    private void shutdown() {
        final String LOCAL_TAG = TAG.concat(".shutdown()");
        Log.e(LOCAL_TAG, DIVIDER);

        Log.e(LOCAL_TAG, "Closing camera");
        mDevice.close();
        mDevice= null;

        Log.e(LOCAL_TAG, "Quitting handler");
        mHandler_thread.quitSafely();
        try {
            mHandler_thread.join(10000);  // wait 10 sec for thread to die
        }
        catch (Exception e ) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
        }
        mHandler_thread = null;

        mHandler= null;
        Log.e(LOCAL_TAG, "RETURN");
    }
}
