package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import sci.crayfis.shramp.camera2.ShrampCamManager;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.DataManager;

/**
 * Oversees the setup of surfaces, cameras and capture session
 */
@TargetApi(21)
public final class CaptureOverseer extends Activity {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    // Static reference to single instance of this class.
    // Static methods below use this reference to access the CameraManager, which cannot be static.
    private static CaptureOverseer mInstance;

    // Built and configured by a ShrampCam via the ShrampCamManager
    private static CameraDevice           mCameraDevice;
    private static CaptureRequest         mCaptureRequest;
    private static CaptureRequest.Builder mCaptureRequestBuilder;

    // List of output target surfaces provided by SurfaceManager
    private static List<Surface>  mSurfaces;

    //..............................................................................................

    // Exposure block time window [nano seconds]
    private static long EXPOSURE_DURATION_NANOS = (long) (10 * 1e9);
    private static long mFinishEpoch;

    // Date epoch for data folder
    private static String mDateEpoch;

    // Full path of data folder
    private static String mDataPath;

    // Nanosecond reference epoch for capture filenames
    private static long mNanoEpoch;

    // Inner class StateCallback to handle CameraCaptureSession.StateCallback
    private static StateCallback mStateCallback;

    // Session is created by the system from a CameraDevice,
    // and returned via inner class, StateCallback.onConfigured()
    private static CameraCaptureSession mCameraCaptureSession;

    //..............................................................................................

    // Inner class CaptureCallback to handle CameraCaptureSession.CaptureCallback
    private static CaptureCallback mCaptureCallback;

    //..............................................................................................

    // Android rules, cannot be static (formally, that's a potential memory leak)
    private CameraManager mCameraManager;

    //..............................................................................................

    // Keep track of which camera we're using
    private enum WhichCamera {FRONT, BACK, EXTERNAL};
    private static WhichCamera mWhichCamera;


    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Helper for static methods to get CameraManager
     * @return CameraManager
     */
    private static CameraManager getCameraManager() {return CaptureOverseer.mInstance.mCameraManager;}

    //----------------------------------------------------------------------------------------------

    /**
     * Entry point for this activity.
     * Set up surfaces - execution continues in surfacesReady()
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up ShRAMP data directory
        DataManager.setUpShrampDirectory();

        // TODO: REMOVE IN THE FUTURE
        // start fresh
        DataManager.clean();

        // For static access of CameraManager
        CaptureOverseer.mInstance = this;
        this.mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Inner class capture session callbacks
        CaptureOverseer.mStateCallback   = new StateCallback();
        CaptureOverseer.mCaptureCallback = new CaptureCallback();

        // Open UI surfaces while still on main thread
        // Execution continues in uiSurfacesReady()
        SurfaceManager.getInstance().openUiSurfaces(CaptureOverseer.mInstance);

        // Turning control over to the ShrampCamManager to set up the camera
        CameraManager    cameraManager    = CaptureOverseer.getCameraManager();
        ShrampCamManager shrampCamManager = ShrampCamManager.getInstance(cameraManager);
        assert shrampCamManager != null;

        // shrampCamManager.openXxxxCamera() opens a camera (returns true if successful).
        // When the camera opens, execution will continue in cameraReady()
        // NOTE: subsequent execution will be on the camera's thread
        mWhichCamera = WhichCamera.BACK;
        if (!shrampCamManager.openBackCamera()) {
            mLogger.log("WARNING: BACK CAMERA DID NOT OPEN, TRYING FRONT");
            mWhichCamera = WhichCamera.FRONT;
            if (!shrampCamManager.openFrontCamera()) {
                mLogger.log("WARNING: FRONT CAMERA DID NOT OPEN, TRYING EXTERNAL");
                mWhichCamera = WhichCamera.EXTERNAL;
                if (!shrampCamManager.openExternalCamera()) {
                    mLogger.log("ERROR: NO CAMERAS");
                }
            }
        }

        CaptureOverseer.mLogger.log("return;");
    }

    /**
     * Camera is ready for capture, create surfaces to output to.
     * Execution continues surfacesReady()
     * @param cameraDevice configured and ready for capture
     * @param captureRequestBuilder associated camera capture request builder
     */
    public static void cameraReady(CameraDevice cameraDevice,
                                   CaptureRequest.Builder captureRequestBuilder) {
        CaptureOverseer.mLogger.log("Camera ready, setting up surfaces");

        CaptureOverseer.mCameraDevice          = cameraDevice;
        CaptureOverseer.mCaptureRequestBuilder = captureRequestBuilder;

        int imageFormat  = ShrampCamManager.getImageFormat();
        int bitsPerPixel = ShrampCamManager.getImageBitsPerPixel();
        Size imageSize   = ShrampCamManager.getImageSize();

        // Turning control over to the SurfaceManager to set up output surfaces.
        // Execution continues in surfacesReady()
        CaptureOverseer.mLogger.log("Setting up surfaces");
        SurfaceManager.getInstance().openImageSurfaces(imageFormat, bitsPerPixel, imageSize);
        CaptureOverseer.mLogger.log("return;");
    }

    /**
     * After surfaces have been successfully configured, prepare camera.
     * Execution continues in innerclass StateCallback.onConfigured()
     * @param surfaces surfaces ready for output
     */
    public static void surfacesReady(List<Surface> surfaces) {
        mSurfaces = surfaces;
        mLogger.log("All surfaces are ready, finishing capture request");

        for (Surface surface : mSurfaces) {
            CaptureOverseer.mCaptureRequestBuilder.addTarget(surface);
        }
        CaptureOverseer.mLogger.log("Building capture request");
        CaptureOverseer.mCaptureRequest = CaptureOverseer.mCaptureRequestBuilder.build();

        try {
            CaptureOverseer.mLogger.log("Creating capture session");
            // Execution continues in inner class StateCallback.onConfigured()
            CaptureOverseer.mCameraDevice.createCaptureSession(
                    CaptureOverseer.mSurfaces, CaptureOverseer.mStateCallback,
                    ShrampCamManager.getCameraHandler());
        }
        catch (CameraAccessException e) {
            CaptureOverseer.mLogger.log("ERROR: Camera Access Exception");
        }
        CaptureOverseer.mLogger.log("return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of inner class CameraCaptureSession.StateCallback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Implements the state callbacks for a CameraCaptureSession.
     * Works in conjunction with CaptureCallback below.
     */
    private static final class StateCallback extends CameraCaptureSession.StateCallback {

        //******************************************************************************************
        // Class Variables
        //----------------

        // Updated with each surface as it is being prepared for capture
        private Surface mSurface;

        //******************************************************************************************
        // Class Methods
        //--------------

        /**
         * This method is called when the buffer pre-allocation for an output Surface is complete.
         * @param session
         * @param surface
         */
        @Override
        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            super.onSurfacePrepared(session, surface);
            Log.e("Tag", "Surface prepared");
            CaptureOverseer.mCameraCaptureSession = session;
            this.mSurface = surface;
        }

        /**
         * This method is called when the camera device has finished configuring itself,
         * and the session can start processing capture requests.
         * (Required)
         * @param session
         */
        @Override
        public void onConfigured(CameraCaptureSession session) {
            //super.onConfigured(session); is abstract
            Log.e("Tag", "Ready to start repeating request..");

            CaptureOverseer.mCameraCaptureSession = session;

            // Make sure time zone is Pacific Standard Time (no daylight savings)
            TimeZone pst = TimeZone.getTimeZone("Etc/GMT+8");
            if (pst.useDaylightTime()) {
                // TODO: quit
                mLogger.log("ERROR: Using daylight savings time");
            }
            TimeZone.setDefault(pst);

            // Get time
            Calendar calendar = Calendar.getInstance(pst, Locale.US);
            int year          = calendar.get(Calendar.YEAR);
            int month         = calendar.get(Calendar.MONDAY);
            int day           = calendar.get(Calendar.DAY_OF_MONTH);
            int hour          = calendar.get(Calendar.HOUR_OF_DAY);
            int minute        = calendar.get(Calendar.MINUTE);
            int second        = calendar.get(Calendar.SECOND);
            int millisecond   = calendar.get(Calendar.MILLISECOND);

            CaptureOverseer.mDateEpoch = Integer.toString(year)   + "-"
                    + Integer.toString(month)  + "-"
                    + Integer.toString(day)    + "-"
                    + Integer.toString(hour)   + "-"
                    + Integer.toString(minute) + "-"
                    + Integer.toString(second) + "-"
                    + Integer.toString(millisecond);
            CaptureOverseer.mDataPath = DataManager.createDataDirectory(CaptureOverseer.mDateEpoch);

            CaptureOverseer.mNanoEpoch   = SystemClock.elapsedRealtimeNanos();
            CaptureOverseer.mFinishEpoch = CaptureOverseer.mNanoEpoch + EXPOSURE_DURATION_NANOS;

            try {
                // Execution continues in CaptureCallback.onCaptureStarted()
                // Handler is implicitly the camera's thread
                CaptureOverseer.mCameraCaptureSession.setRepeatingRequest(
                        CaptureOverseer.mCaptureRequest, CaptureOverseer.mCaptureCallback, null);
            }
            catch (CameraAccessException e) {
                CaptureOverseer.mLogger.log("ERROR: Camera Access Exception");
            }
        }

        /**
         * This method is called when the session starts actively processing capture requests.
         * @param session
         */
        @Override
        public void onActive(CameraCaptureSession session) {
            super.onActive(session);
            Log.e("Tag", "Capture session is active and processing requests!");
            CaptureOverseer.mCameraCaptureSession = session;
        }

        /**
         * This method is called every time the session has no more capture requests to process.
         * @param session
         */
        @Override
        public void onReady(CameraCaptureSession session) {
            super.onReady(session);
            Log.e("Tag", "Session is ready for capture requests");
            CaptureOverseer.mCameraCaptureSession = session;
        }

        /**
         * This method is called when camera device's input capture queue becomes empty,
         * and is ready to accept the next request.
         * @param session
         */
        @Override
        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            super.onCaptureQueueEmpty(session);
            Log.e("Tag", "Capture queue is empty");
            CaptureOverseer.mCameraCaptureSession = session;
        }

        /**
         * This method is called if the session cannot be configured as requested.
         * (Required)
         * @param session
         */
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            //super.onConfigureFailed(session); is abstract
            Log.e("Tag", "Capture session configuration failed!");
            CaptureOverseer.mCameraCaptureSession = session;
        }

        /**
         * This method is called when the session is closed.
         * @param session
         */
        @Override
        public void onClosed(CameraCaptureSession session) {
            super.onClosed(session);
            Log.e("Tag", "Capture session is closed.");
            CaptureOverseer.mCameraCaptureSession = session;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of inner class CameraCaptureSession.CaptureCallback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Implements the capture callbacks for a CameraCaptureSession.
     * Works in conjunction with StateCallback above.
     */
    private static final class CaptureCallback extends CameraCaptureSession.CaptureCallback {

        //******************************************************************************************
        // Class Variables
        //----------------

        private Surface mTarget;

        private CaptureResult      mCaptureResult;
        private CaptureFailure     mCaptureFailure;
        private TotalCaptureResult mTotalCaptureResult;

        private long mTimestamp;
        private long mFrameNumber;
        private int  mSequenceId;

        private long elapsedTime  = SystemClock.elapsedRealtimeNanos();

        //******************************************************************************************
        // Class Methods
        //--------------

        /**
         * This method is called when an image capture makes partial forward progress;
         * some (but not all) results from an image capture are available.
         * @param session
         * @param request
         * @param partialResult
         */
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            CaptureOverseer.mCameraCaptureSession = session;
            CaptureOverseer.mCaptureRequest       = request;
            this.mCaptureResult = partialResult;
        }

        /**
         * This method is called when the camera device has started capturing the output image
         * for the request, at the beginning of image exposure, or when the camera device has
         * started processing an input image for a reprocess request.
         * @param session
         * @param request
         * @param timestamp
         * @param frameNumber
         */
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                     long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            CaptureOverseer.mCameraCaptureSession = session;
            CaptureOverseer.mCaptureRequest       = request;
            this.mTimestamp   = timestamp;
            this.mFrameNumber = frameNumber;
        }

        /**
         * This method is called when an image capture has fully completed and all the result
         * metadata is available.
         * @param session
         * @param request
         * @param result
         */
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            CaptureOverseer.mCameraCaptureSession = session;
            CaptureOverseer.mCaptureRequest       = request;
            this.mTotalCaptureResult = result;

            long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
            long exposure  = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            long frame     = result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW);
            long nanosSinceEpoch = timestamp - CaptureOverseer.mNanoEpoch;
            // TODO: check if timestamp uses SystemClock or not
            // TODO: a work around is basing all timestamps off the first timestamp

            long msSinceEpoch = Math.round(nanosSinceEpoch * 1e-9 * 1e3);
            long msExposure   = Math.round(exposure * 1e-9 * 1e3);
            long msFrame      = Math.round(frame * 1e-9 * 1e3);

            long frameNumber = result.getFrameNumber();

            String filename = CaptureOverseer.mDataPath
                    + "/" + String.format("%03d", frameNumber)
                    + "-" + Long.toString(msSinceEpoch)
                    + "-" + Long.toString(msExposure)
                    + "-" + Long.toString(msFrame) + ".data";
            DataManager.saveData(timestamp, filename);

            long time = SystemClock.elapsedRealtimeNanos();
            DecimalFormat df = new DecimalFormat("##.#");
            String fps = df.format(1e9 / (double)(time - elapsedTime));
            elapsedTime = time;
            Log.e(Thread.currentThread().getName(),"image captured: " + Long.toString(mFrameNumber) + " realtime fps: " + fps);

            // End exposure block after EXPOSURE_DURATION_NANOS time
            if (SystemClock.elapsedRealtimeNanos() >= CaptureOverseer.mFinishEpoch) {
                try {
                    session.stopRepeating();
                }
                catch (CameraAccessException e) {
                    CaptureOverseer.mLogger.log("ERROR: Camera Access Exception");
                }
            }
        }

        /**
         * This method is called independently of the others in CaptureCallback, when a capture
         * sequence finishes and all CaptureResult or CaptureFailure for it have been
         * returned via this listener.
         * @param session
         * @param sequenceId
         * @param frameNumber
         */
        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId,
                                               long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            CaptureOverseer.mLogger.log("Capture sequence completed");
            CaptureOverseer.mCameraCaptureSession = session;
            this.mSequenceId  = sequenceId;
            this.mFrameNumber = frameNumber;

            // TODO: dump mTotalCaptureResult info

            session.close();

            DataManager.flush();

            CameraManager    cameraManager    = CaptureOverseer.getCameraManager();
            ShrampCamManager shrampCamManager = ShrampCamManager.getInstance(cameraManager);
            assert shrampCamManager != null;
            switch (mWhichCamera) {
                case BACK: {
                    shrampCamManager.closeBackCamera();
                    break;
                }
                case FRONT: {
                    shrampCamManager.closeFrontCamera();
                    break;
                }
                case EXTERNAL: {
                    shrampCamManager.closeExternalCamera();
                    break;
                }
                default:
                    // TODO: this is an error
            }
        }

        /**
         * This method is called independently of the others in CaptureCallback, when a capture
         * sequence aborts before any CaptureResult or CaptureFailure for it have been returned
         * via this listener.
         * @param session
         * @param sequenceId
         */
        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.e("Tag", "Capture sequence has been aborted");
            CaptureOverseer.mCameraCaptureSession = session;
            this.mSequenceId = sequenceId;
        }


        /**
         * This method is called if a single buffer for a capture could not be sent to its
         * destination surfaces.
         * @param session
         * @param request
         * @param target
         * @param frameNumber
         */
        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                                        Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.e("Tag", "A capture buffer has been lost and not sent to its destination surface");
            CaptureOverseer.mCameraCaptureSession = session;
            CaptureOverseer.mCaptureRequest       = request;
            this.mTarget      = target;
            this.mFrameNumber = frameNumber;
        }


        /**
         * This method is called instead of onCaptureCompleted(CameraCaptureSession, CaptureRequest,
         * TotalCaptureResult) when the camera device failed to produce a CaptureResult for the request.
         * @param session
         * @param request
         * @param failure
         */
        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);

            String reason = null;
            if (failure.getReason() == CaptureFailure.REASON_ERROR) {
                reason = "Dropped frame due to error in framework";
            }
            else {
                reason = "Failure due to CameraCaptureSession.abortCaptures()";
            }
            String errInfo = "Camera device failed to produce a CaptureResult\n"
                    + "\t Reason:         " + reason                                       + "\n"
                    + "\t Frame number:   " + Long.toString(failure.getFrameNumber())      + "\n"
                    + "\t Sequence ID:    " + Integer.toString(failure.getSequenceId())    + "\n"
                    + "\t Image captured: " + Boolean.toString(failure.wasImageCaptured()) + "\n";
            Log.e("Tag", errInfo);
            CaptureOverseer.mCameraCaptureSession = session;
            CaptureOverseer.mCaptureRequest       = request;
            this.mCaptureFailure = failure;

            // End exposure block after EXPOSURE_DURATION_NANOS time
            if (SystemClock.elapsedRealtimeNanos() >= CaptureOverseer.mFinishEpoch) {
                try {
                    session.stopRepeating();
                }
                catch (CameraAccessException e) {
                    CaptureOverseer.mLogger.log("ERROR: Camera Access Exception");
                }
            }
        }
    }
}
