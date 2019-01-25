package edu.crayfis.shramp;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

public class CameraThreads {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "CameraThreads";
    private final static String DIVIDER = "---------------------------------------------";

    // callbacks
    public CameraDevice.StateCallback           Camera_state;
    public CameraCaptureSession.StateCallback   Capture_state;
    public CameraCaptureSession.CaptureCallback Capture_callback;

    // camera
    public CameraDevice  Camera_device;
    public Handler       Camera_handler;
    public HandlerThread Camera_thread;
    public final static String THREAD_NAME = "Camera_Thread";

    public Camera Camera;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * TODO:  Entry point, edit this
     */
    CameraThreads(Camera camera) {
        final String LOCAL_TAG = TAG.concat(".CameraThreads()");
        Log.e(LOCAL_TAG, DIVIDER);

        Camera = camera;

        Log.e(LOCAL_TAG, "Instantiating callbacks");

        Camera_state = new CameraDevice.StateCallback() {
            final String LOCAL_TAG = TAG.concat(".CameraDevice.StateCallback");

            @Override
            public void onOpened(@NonNull CameraDevice camera_device) {
                Log.e(LOCAL_TAG, "onOpened()");

                Camera_device = camera_device;
                if (Camera_device == null) {
                    Log.e(LOCAL_TAG, "FUCK");
                }
                else {
                    Log.e(LOCAL_TAG, "Camera_device should be good");
                }
                Camera.configureCamera();
                //createStillSession();
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

        Capture_state = new CameraCaptureSession.StateCallback() {
            final String LOCAL_TAG = TAG.concat(".CameraCaptureSession.StateCallback");

            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Log.e(LOCAL_TAG, "Session configured");
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(LOCAL_TAG, "Session configuration **FAILED**");
            }
        };

        Capture_callback = new CameraCaptureSession.CaptureCallback() {
            final String LOCAL_TAG = TAG.concat(".CameraCaptureSession.CaptureCallback");

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

        Log.e(LOCAL_TAG, "Instantiating threads");

        Camera_thread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        Camera_thread.start();
        Camera_handler = new Handler(Camera_thread.getLooper());
    }

    public void shutdown() {
        final String LOCAL_TAG = TAG.concat(".shutdown()");
        Log.e(LOCAL_TAG, DIVIDER);

        Camera_device.close();
        Camera_device = null;

        Camera_thread.quitSafely();
        try {
            Camera_thread.join(10000);  // wait 10 sec for thread to die
        }
        catch (Exception e ) {
            Log.e(LOCAL_TAG, "EXCEPTION: " + e.getLocalizedMessage());
        }
            Camera_thread = null;

        Camera_handler = null;
    }
}
