package edu.crayfis.shramp;

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

    //**********************************************************************************************
    // Class Methods
    //--------------

    Camera(@NonNull MaineShRAMP main_activity, @NonNull Runnable quit_action) {
        super(main_activity, quit_action);

        final String LOCAL_TAG = TAG.concat(".Camera()");
        Log.e(LOCAL_TAG, DIVIDER);

        Log.e(LOCAL_TAG, "Instantiating callbacks");
        instantiateCallbacks();

        Log.e(LOCAL_TAG, "Instantiating threads");
        mHandler_thread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mHandler_thread.start();
        mHandler = new Handler(mHandler_thread.getLooper());

        Log.e(LOCAL_TAG, "RETURN");
    }

    private void instantiateCallbacks() {
        final String LOCAL_TAG = TAG.concat(".instantiateCallbacks()");
        Log.e(LOCAL_TAG, DIVIDER);

        instantiateDeviceState();
        instantiateSessionState();
        instantiateSessionCapture();
    }

    private void instantiateDeviceState() {
        final String LOCAL_TAG = TAG.concat(".instantiateDeviceState()");

        mDevice_state_callback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera_device) {
                Log.e(LOCAL_TAG, "onOpened()");

                Camera.super.mDevice = camera_device;

                Log.e(LOCAL_TAG, "Configuring camera");
                Camera.super.configureCamera();
                Log.e(LOCAL_TAG, "Shutting down for now");
                shutdown();
                //createStillSession();
                Log.e(LOCAL_TAG, "RETURN");
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera_device) {
                Log.e(LOCAL_TAG, "onDisconnected");

                shutdown();
            }

            @Override
            public void onError(@NonNull CameraDevice camera_device, int error) {
                Log.e(LOCAL_TAG, "onError");

                // TODO process error
                shutdown();
            }
        };
    }

    private void instantiateSessionState() {
        final String LOCAL_TAG = TAG.concat(".instantiateSessionState()");

        mSession_state_callback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Log.e(LOCAL_TAG, "Session configured");
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(LOCAL_TAG, "Session configuration **FAILED**");
            }
        };
    }

    private void instantiateSessionCapture() {
        final String LOCAL_TAG = TAG.concat(".instantiateSessionCapture()");

        mSession_capture_callback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                         @NonNull CaptureRequest request,
                                         long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
                Log.e(LOCAL_TAG, "Capture started");
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
                Log.e(LOCAL_TAG, "Capture progressing");
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Log.e(LOCAL_TAG, "Capture completed");
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Log.e(LOCAL_TAG, "Capture **FAILED**");
            }

            @Override
            public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                                   int sequenceId, long frameNumber) {
                super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                Log.e(LOCAL_TAG, "Capture sequence completed");
            }

            @Override
            public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session,
                                                 int sequenceId) {
                super.onCaptureSequenceAborted(session, sequenceId);
                Log.e(LOCAL_TAG, "Capture sequence aborted");
            }

            @Override
            public void onCaptureBufferLost(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull Surface target, long frameNumber) {
                super.onCaptureBufferLost(session, request, target, frameNumber);
                Log.e(LOCAL_TAG, "Capture buffer lost");
            }
        };
    }


    public void shutdown() {
        final String LOCAL_TAG = TAG.concat(".shutdown()");
        Log.e(LOCAL_TAG, DIVIDER);

        Camera.super.mDevice.close();
        Camera.super.mDevice= null;

        mHandler_thread.quitSafely();
        try {
            mHandler_thread.join(10000);  // wait 10 sec for thread to die
        }
        catch (Exception e ) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
        }
        mHandler_thread = null;

        mHandler= null;
    }
}
