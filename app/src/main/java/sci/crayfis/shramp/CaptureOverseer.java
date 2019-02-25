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
import android.view.Surface;

import java.util.List;

import sci.crayfis.shramp.camera2.ShrampCamManager;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.surfaces.SurfaceManager;

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

    // List of output target surfaces provided by SurfaceManager
    private static List<Surface>  mSurfaces;

    // Built and configured by a ShrampCam via the ShrampCamManager
    private static CaptureRequest       mCaptureRequest;

    //..............................................................................................

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


    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Helper for static methods to get CameraManager
     * @return CameraManager
     */
    private static CameraManager getCameraManager() {return mInstance.mCameraManager;}

    //----------------------------------------------------------------------------------------------

    /**
     * Entry point for this activity.
     * Set up surfaces - execution continues in surfacesReady()
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For static access of CameraManager
        mInstance      = this;
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Inner class capture session callbacks
        mStateCallback   = new StateCallback();
        mCaptureCallback = new CaptureCallback();

        // Turning control over to the SurfaceManager to set up output surfaces.
        // Execution continues in surfacesReady()
        mLogger.log("Setting up surfaces");
        SurfaceManager.getInstance().openSurfaces(this);
        mLogger.log("return;");
    }

    /**
     * After surfaces have been successfully configured, prepare camera.
     * Execution continues in cameraReady()
     * @param surfaces surfaces ready for output
     */
    public static void surfacesReady(List<Surface> surfaces) {
        mSurfaces = surfaces;
        mLogger.log("All surfaces are ready, setting up cameras");

        CameraManager    cameraManager    = CaptureOverseer.getCameraManager();
        ShrampCamManager shrampCamManager = ShrampCamManager.getInstance(cameraManager);
        assert shrampCamManager != null;

        // shrampCamManager.openXxxxCamera() opens a camera (returns true if successful).
        // When the camera opens, execution will continue in cameraReady()
        if (!shrampCamManager.openBackCamera()) {
            mLogger.log("WARNING: BACK CAMERA DID NOT OPEN, TRYING FRONT");
            if (!shrampCamManager.openFrontCamera()) {
                mLogger.log("WARNING: FRONT CAMERA DID NOT OPEN, TRYING EXTERNAL");
                if (!shrampCamManager.openExternalCamera()) {
                    mLogger.log("ERROR: NO CAMERAS");
                }
            }
        }
        mLogger.log("return;");
    }

    /**
     * Camera is ready for capture, initiate the capture request.
     * Execution continues in innerclass StateCallback.onConfigured()
     * @param cameraDevice configured and ready for capture
     * @param captureRequestBuilder associated camera capture request builder
     */
    public static void cameraReady(CameraDevice cameraDevice,
                                   CaptureRequest.Builder captureRequestBuilder) {
        mLogger.log("Camera ready, finishing capture request");
        for (Surface surface : mSurfaces) {
            captureRequestBuilder.addTarget(surface);
        }

        mLogger.log("Building capture request");
        mCaptureRequest = captureRequestBuilder.build();

        try {
            mLogger.log("Creating capture session");
            // execution continues in inner class StateCallback.onConfigured()
            cameraDevice.createCaptureSession(mSurfaces, mStateCallback, null);
        }
        catch (CameraAccessException e) {
            mLogger.log("ERROR: Camera Access Exception");
        }
        mLogger.log("return;");
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
        private Surface mmSurface;

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
            mLogger.log("Surface prepared");
            mCameraCaptureSession = session;
            mmSurface             = surface;
        }

        /**
         * This method is called when the camera device has finished configuring itself,
         * and the session can start processing capture requests.
         * (Required)
         * @param session
         */
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mCameraCaptureSession = session;

            mLogger.log("Ready to start repeating request..");
            try {
                // execution continues in CaptureCallback.onCaptureStarted()
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest,
                        mCaptureCallback, null);
            }
            catch (CameraAccessException e) {
                mLogger.log("ERROR: Camera Access Exception");
            }
        }

        /**
         * This method is called when the session starts actively processing capture requests.
         * @param session
         */
        @Override
        public void onActive(CameraCaptureSession session) {
            mLogger.log("Capture session is active and processing requests!");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called every time the session has no more capture requests to process.
         * @param session
         */
        @Override
        public void onReady(CameraCaptureSession session) {
            mLogger.log("Session is ready for capture requests");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called when camera device's input capture queue becomes empty,
         * and is ready to accept the next request.
         * @param session
         */
        @Override
        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            mLogger.log("Capture queue is empty");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called if the session cannot be configured as requested.
         * (Required)
         * @param session
         */
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            mLogger.log("Capture session configuration failed!");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called when the session is closed.
         * @param session
         */
        @Override
        public void onClosed(CameraCaptureSession session) {
            mLogger.log("Capture session is closed.");
            mCameraCaptureSession = session;
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

        private Surface            mmTarget;

        private CaptureResult      mmCaptureResult;
        private CaptureFailure     mmCaptureFailure;
        private TotalCaptureResult mmTotalCaptureResult;

        private long               mmTimestamp;
        private long               mmFrameNumber;
        private int                mmSequenceId;

        private long mmCaptureCount = 0;
        private final long COUNT_LIMIT = 10;

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
            mmCaptureCount += 1;
            mLogger.log(Long.toString(mmCaptureCount));
            if (mmCaptureCount <= COUNT_LIMIT) {
                mLogger.log("Capture making some forward progress, count = " + Long.toString(mmCaptureCount));
                if (mmCaptureCount == COUNT_LIMIT) {
                    mLogger.log("Silencing further notifications");
                }
            }

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmCaptureResult       = partialResult;
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
            if (mmCaptureCount <= COUNT_LIMIT) {
                mLogger.log("Capture has started!");
                if (mmCaptureCount == COUNT_LIMIT) {
                    mLogger.log("Silencing further notifications");
                }
            }

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTimestamp           = timestamp;
            mmFrameNumber         = frameNumber;
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
            if (mmCaptureCount <= COUNT_LIMIT) {
                mLogger.log("One capture successful");
                if (mmCaptureCount == COUNT_LIMIT) {
                    mLogger.log("Silencing further notifications");
                }
            }

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTotalCaptureResult  = result;
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
            mLogger.log("Capture sequence completed");
            mCameraCaptureSession = session;
            mmSequenceId          = sequenceId;
            mmFrameNumber         = frameNumber;
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
            mLogger.log("Capture sequence has been aborted");
            mCameraCaptureSession = session;
            mmSequenceId          = sequenceId;
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
            mLogger.log("A capture buffer has been lost and not sent to its destination surface");
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTarget              = target;
            mmFrameNumber         = frameNumber;
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
            mLogger.log("Camera device failed to produce a CaptureResult");
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmCaptureFailure      = failure;
        }
    }
}
