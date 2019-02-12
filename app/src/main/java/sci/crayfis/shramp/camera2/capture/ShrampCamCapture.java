package sci.crayfis.shramp.camera2.capture;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.view.Surface;

import sci.crayfis.shramp.logging.ShrampLogger;

public class ShrampCamCapture {

    //**********************************************************************************************
    // Class Variables
    //----------------

    private final Object ACTION_LOCK = new Object();

    private StateCallback        mStateCallback;
    private CaptureCallback      mCaptureCallback;

    private CaptureRequest       mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;

    // logging
    private ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    ShrampCamCapture(CaptureRequest captureRequest) {

        mCaptureRequest = captureRequest;

        mStateCallback   = new StateCallback();
        mCaptureCallback = new CaptureCallback();

        mLogger.log("return ShrampCamCapture;");
    }

    StateCallback getStateCallback() {
        return mStateCallback;
    }

    CaptureCallback getCaptureCallback() {
        return mCaptureCallback;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested class CameraCaptureSession.StateCallback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class StateCallback extends CameraCaptureSession.StateCallback {

        //******************************************************************************************
        // Class Variables
        //----------------

        private Surface mmSurface;

        //******************************************************************************************
        // Class Methods
        //--------------

        StateCallback() {
            super();
            mLogger.log("made StateCallback");
        }

        /**
         * This method is called when the buffer pre-allocation for an output Surface is complete.
         * @param session
         * @param surface
         */
        @Override
        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            mCameraCaptureSession = session;
            mmSurface              = surface;
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

            mLogger.log("setRepeatingRequest");
            try {
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest,
                        mCaptureCallback, null);
            }
            catch (CameraAccessException e) {
                // TODO
            }
        }

        /**
         * This method is called if the session cannot be configured as requested.
         * (Required)
         * @param session
         */
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            mLogger.log("ConfigureFailed");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called when the session is closed.
         * @param session
         */
        @Override
        public void onClosed(CameraCaptureSession session) {
            mCameraCaptureSession = session;
        }


        /**
         * This method is called when the session starts actively processing capture requests.
         * @param session
         */
        @Override
        public void onActive(CameraCaptureSession session) {
            mCameraCaptureSession = session;
        }

        /**
         * This method is called every time the session has no more capture requests to process.
         * @param session
         */
        @Override
        public void onReady(CameraCaptureSession session) {
            mLogger.log("Session ready");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called when camera device's input capture queue becomes empty,
         * and is ready to accept the next request.
         * @param session
         */
        @Override
        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            mCameraCaptureSession = session;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested class CameraCaptureSession.CaptureCallback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class CaptureCallback extends CameraCaptureSession.CaptureCallback {

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

        //******************************************************************************************
        // Class Methods
        //--------------

        CaptureCallback() {
            super();
            mLogger.log("made CaptureCallback");
        }

        /**
         * This method is called if a single buffer for a capture could not be sent to its
         * destination surface.
         * @param session
         * @param request
         * @param target
         * @param frameNumber
         */
        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                                        Surface target, long frameNumber) {
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTarget              = target;
            mmFrameNumber         = frameNumber;
            mLogger.log("CaptureBufferLost");
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
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTotalCaptureResult  = result;
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
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmCaptureFailure      = failure;
            mLogger.log("CaptureFailed");
        }

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
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmCaptureResult       = partialResult;
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
            mCameraCaptureSession = session;
            mmSequenceId = sequenceId;
            mLogger.log("CaptureSequenceAborted");
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
            mCameraCaptureSession = session;
            mmSequenceId = sequenceId;
            mmFrameNumber = frameNumber;
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
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTimestamp = timestamp;
            mmFrameNumber = frameNumber;
        }
    }
}
