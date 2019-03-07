package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.List;

import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.ShrampCamManager;
import sci.crayfis.shramp.camera2.capture.CaptureManager;
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
    private static CameraDevice mCameraDevice;

    private static ImageProcessor mImageProcessor;

    private static CaptureManager mCaptureManager;

    //..............................................................................................

    // Date epoch for data folder
    private static String mDateEpoch;

    // Full path of data folder
    private static String mDataPath;

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
        mInstance = this;
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Turning control over to the ShrampCamManager to set up the camera
        CameraManager    cameraManager    = getCameraManager();
        ShrampCamManager shrampCamManager = ShrampCamManager.getInstance(cameraManager);
        assert shrampCamManager != null;

        // TODO: update camera manager etc
        // shrampCamManager.openXxxxCamera() opens a camera (returns true if successful).
        // When the camera opens, execution will continue in cameraReady()
        // NOTE: execution of cameraReady() will be on the camera's thread
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

        mLogger.log("return;");
    }

    /**
     * Camera is ready for capture, create surfaces to output to.
     * Execution continues surfacesReady()
     * @param cameraDevice configured and ready for capture
     */
    public static void cameraReady(CameraDevice cameraDevice) {
        mLogger.log("Camera ready, setting up surfaces");

        mCameraDevice = cameraDevice;

        final int imageFormat  = ShrampCamManager.getImageFormat();
        final int bitsPerPixel = ShrampCamManager.getImageBitsPerPixel();
        final Size imageSize   = ShrampCamManager.getImageSize();

        // Set image processor
        mImageProcessor = new ImageProcessor(mInstance, imageFormat, bitsPerPixel, imageSize);

        final Runnable openSurfaces = new Runnable() {
            @Override
            public void run() {
                SurfaceManager.getInstance().openSurfaces(mInstance, imageFormat,
                                                          bitsPerPixel, imageSize);
            }
        };

        // Turning control over to the SurfaceManager to set up output surfaces.
        // Execution continues in surfacesReady()
        // Open surfaces on the activity thread
        new Handler().post(openSurfaces);
        mLogger.log("return;");
    }

    /**
     * After surfaces have been successfully configured, prepare camera.
     * Execution continues in CaptureManager
     * @param surfaces surfaces ready for output
     */
    public static void surfacesReady(List<Surface> surfaces) {
        mLogger.log("All surfaces are ready, finishing capture request");

        // Begin data taking
        mCaptureManager = new CaptureManager(mCameraDevice, surfaces);
    }

    //----------------------------------------------------------------------------------------------

    public static void processImage(TotalCaptureResult result) {
        mImageProcessor.processImage(result);
    }

    public static void processImage(ByteBuffer imageBytes) {
        mImageProcessor.processImage(imageBytes);
    }





























    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of inner class CameraCaptureSession.CaptureCallback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Implements the capture callbacks for a CameraCaptureSession.
     * Works in conjunction with StateCallback above.
     */
    //private static final class CaptureCallback extends CameraCaptureSession.CaptureCallback {

        //******************************************************************************************
        // Class Variables
        //----------------
        /*
        private Surface mTarget;

        private CaptureResult      mCaptureResult;
        private CaptureFailure     mCaptureFailure;
        private TotalCaptureResult mTotalCaptureResult;

        private long mTimestamp;
        private long mFrameNumber;
        private int  mSequenceId;

        private long elapsedTime  = SystemClock.elapsedRealtimeNanos();
        private DecimalFormat df = new DecimalFormat("##.#");
        */
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
        /*
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            //CaptureOverseer.mCameraCaptureSession = session;
            //CaptureOverseer.mCaptureRequest       = request;
            //mCaptureResult = partialResult;
        }
        */
        /**
         * This method is called when the camera device has started capturing the output image
         * for the request, at the beginning of image exposure, or when the camera device has
         * started processing an input image for a reprocess request.
         * @param session
         * @param request
         * @param timestamp
         * @param frameNumber
         */
        /*
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                     long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            //CaptureOverseer.mCameraCaptureSession = session;
            //CaptureOverseer.mCaptureRequest       = request;
            //mTimestamp   = timestamp;
            mFrameNumber = frameNumber;
        }
        */
        /**
         * This method is called when an image capture has fully completed and all the result
         * metadata is available.
         * @param session
         * @param request
         * @param result
         */
        /*
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //CaptureOverseer.mCameraCaptureSession = session;
            //CaptureOverseer.mCaptureRequest       = request;
            //mTotalCaptureResult = result;

            long time = SystemClock.elapsedRealtimeNanos();

            SurfaceManager.getInstance().onCaptureCompleted(result);

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
        */
        /**
         * This method is called independently of the others in CaptureCallback, when a capture
         * sequence finishes and all CaptureResult or CaptureFailure for it have been
         * returned via this listener.
         * @param session
         * @param sequenceId
         * @param frameNumber
         */
        /*
        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId,
                                               long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            CaptureOverseer.mLogger.log("Capture sequence completed");
            CaptureOverseer.mCameraCaptureSession = session;
           mSequenceId  = sequenceId;
           mFrameNumber = frameNumber;

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
        */
        /**
         * This method is called independently of the others in CaptureCallback, when a capture
         * sequence aborts before any CaptureResult or CaptureFailure for it have been returned
         * via this listener.
         * @param session
         * @param sequenceId
         */
        /*
        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.e("Tag", "Capture sequence has been aborted");
            CaptureOverseer.mCameraCaptureSession = session;
           mSequenceId = sequenceId;
        }
        */

        /**
         * This method is called if a single buffer for a capture could not be sent to its
         * destination surfaces.
         * @param session
         * @param request
         * @param target
         * @param frameNumber
         */
        /*
        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                                        Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.e("Tag", "A capture buffer has been lost and not sent to its destination surface");
            CaptureOverseer.mCameraCaptureSession = session;
            CaptureOverseer.mCaptureRequest       = request;
           mTarget      = target;
           mFrameNumber = frameNumber;
        }
        */

        /**
         * This method is called instead of onCaptureCompleted(CameraCaptureSession, CaptureRequest,
         * TotalCaptureResult) when the camera device failed to produce a CaptureResult for the request.
         * @param session
         * @param request
         * @param failure
         */
        /*
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
           mCaptureFailure = failure;

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
        */
    //}
}
