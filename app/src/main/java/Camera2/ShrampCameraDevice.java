package Camera2;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;

@TargetApi(21) // Lollipop
public class ShrampCameraDevice implements AutoCloseable {

    // set in constructor
    private HandlerThread       mHandlerThread;
    private Handler             mHandler;
    private String              mName;
    private Integer             mPriority;
    private ShrampStateCallback mStateCallback; // nested class defined below

    private CameraCharacteristics mCameraCharacteristics;
    private ShrampCameraCaptureSession mShrampCameraCaptureSession;

    private static final Long   JOIN_MAX_WAIT_IN_MS = 0L;  // 0 means wait forever

    ShrampCameraDevice(@NonNull CameraCharacteristics characteristics, @NonNull String name) {
        new ShrampCameraDevice(characteristics, name, Process.THREAD_PRIORITY_DEFAULT);
    }

    ShrampCameraDevice(@NonNull CameraCharacteristics characteristics,
                       @NonNull String name, int priority) {
        mName     = name;
        mPriority = priority;
        mCameraCharacteristics = characteristics;
        restart();
    }

    public void restart() {
        mStateCallback = new ShrampStateCallback();
        mHandlerThread = new HandlerThread(mName, mPriority);
        mHandlerThread.start();  // must start before calling .getLooper()
        mHandler       = new Handler(mHandlerThread.getLooper());
    }

    public void close() {
        mStateCallback.close();
        mStateCallback = null;

        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join(JOIN_MAX_WAIT_IN_MS);
        }
        catch (InterruptedException e) {
            // TODO ERROR
        }
        mHandlerThread = null;
        mHandler       = null;
    }

    // access mStateCallback
    public CameraDevice.StateCallback getStateCallback() {
        return mStateCallback;
    }

    public Handler getHandler() { return mHandler; }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested static abstract StateCallback class ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class ShrampStateCallback extends CameraDevice.StateCallback implements AutoCloseable {
        private static final int NO_ERROR = 0;

        // set in onOpened, onClosed, onDisconnected, onError
        private CameraDevice mCameraDevice;
        private Integer      mError;

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            mError        = NO_ERROR;
            mShrampCameraCaptureSession =
                    new ShrampCameraCaptureSession(mCameraDevice, mCameraCharacteristics);
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice = camera;
            mError        = error;

            switch (mError) {
                case (ERROR_CAMERA_DEVICE) : {
                    // TODO fatal error
                    break;
                }
                case (ERROR_CAMERA_DISABLED) : {
                    // TODO disabled due to device policy
                    break;
                }
                case (ERROR_CAMERA_IN_USE) : {
                    // TODO someone else is using the camera already
                    break;
                }
                case (ERROR_CAMERA_SERVICE) : {
                    // TODO fatal error
                    break;
                }
                case (ERROR_MAX_CAMERAS_IN_USE) : {
                    // TODO too many cameras are in use
                }
            }

            close();
        }

        public CameraDevice getCameraDevice() {
            return mCameraDevice;
        }

        public int getErrorCode() {
            return mError;
        }

        public void close() {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    }

}
