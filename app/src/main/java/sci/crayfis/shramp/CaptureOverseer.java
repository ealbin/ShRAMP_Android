package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.CameraController;
import Trash.camera2.ShrampCamManager;
import sci.crayfis.shramp.camera2.capture.CaptureManager;
import sci.crayfis.shramp.logging.DividerStyle;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.DataManager;
import sci.crayfis.shramp.util.HandlerManager;



////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      UNDER CONSTRUCTION
////////////////////////////////////////////////////////////////////////////////////////////////////



/**
 * Oversees the setup of surfaces, cameras and capture session
 */
@TargetApi(21)
public final class CaptureOverseer extends Activity {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Keep track of which camera we're using
    private static final CameraController.Select PREFERRED_CAMERA = CameraController.Select.BACK;
    private static final CameraController.Select SECONDARY_CAMERA = CameraController.Select.FRONT;

    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);


    // Static reference to single instance of this class.
    // Static methods below use this reference to access the CameraController, which cannot be static.
    private static CaptureOverseer mInstance;

    // Built and configured by a ShrampCam via the ShrampCamManager
    private static CameraDevice mCameraDevice;

    private static ImageProcessor mImageProcessor;

    private static CaptureManager mCaptureManager;

    private static ShrampCamManager mShrampCamManager;

    //..............................................................................................

    // Date epoch for data folder
    private static String mDateEpoch;

    // Full path of data folder
    private static String mDataPath;

    //..............................................................................................

    private static Handler mHandler;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Entry point for this activity.
     * Set up surfaces - execution continues in surfacesReady()
     * @param savedInstanceState bla bla
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For access in static methods
        mInstance = this;

        mHandler = new Handler(getMainLooper());

        mLogger.divider(DividerStyle.Strong);
        mLogger.log("Capture Overseer has begun");
        long startTime = SystemClock.elapsedRealtimeNanos();

        // Set up ShRAMP data directory
        DataManager.setUpShrampDirectory();

        // TODO: REMOVE IN THE FUTURE
        // start fresh
        mLogger.log("Clearing ShRAMP data directory, starting from scratch");
        DataManager.clean();

        //==========================================================================================

        // Turning control over to the ShrampCamManager to set up the camera
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        assert cameraManager != null;

        CameraController.discoverCameras(cameraManager);
        CameraController.logCameraCharacteristics();

        Runnable next = new Runnable() {
            @Override
            public void run() {
                prepareSurfaces();
            }
        };
        if (!CameraController.openCamera(PREFERRED_CAMERA, next, mHandler)) {
             CameraController.openCamera(SECONDARY_CAMERA, next, mHandler);
        }

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }

    public static void prepareSurfaces() {
        Runnable next = new Runnable() {
            @Override
            public void run() {
                prepareImageProcessing();
            }
        };
        SurfaceManager.openSurfaces(mInstance, next, mHandler);
    }

    public static void prepareImageProcessing() {
        mImageProcessor = new ImageProcessor(mInstance);

    }

//    public static void defaultCapture() {
//    }

    /**
     * Camera is ready for capture, create surfaces to output to.
     * Execution continues surfacesReady()
     * @param cameraDevice configured and ready for capture
     */
    /*
    public static void cameraReady(CameraDevice cameraDevice) {
        long startTime = SystemClock.elapsedRealtimeNanos();

        mLogger.log("Camera ready, setting up surfaces");

        mCameraDevice = cameraDevice;

        final int imageFormat  = ShrampCamManager.getImageFormat();
        final int bitsPerPixel = ShrampCamManager.getImageBitsPerPixel();
        final Size imageSize   = ShrampCamManager.getImageSize();
         */
        // Set image processor
        //mImageProcessor = new ImageProcessor(mInstance, imageFormat, bitsPerPixel, imageSize);

        /*
        final Runnable openSurfaces = new Runnable() {
            @Override
            public void run() {
                SurfaceManager.getInstance().openSurfaces(mInstance, imageFormat,
                                                          bitsPerPixel, imageSize);
            }
        };
        */
        // Turning control over to the SurfaceManager to set up output surfaces.
        // Execution continues in surfacesReady()
        // Open surfaces on the activity thread
        //new Handler(mInstance.getMainLooper()).post(openSurfaces);

        //String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        //mLogger.log("return; elapsed = " + elapsed + " [ns]");
    //}

    /**
     * After surfaces have been successfully configured, prepare camera.
     * Execution continues in CaptureManager
     * @param surfaces surfaces ready for output
     */
    /*
    public static void surfacesReady(List<Surface> surfaces) {
        long startTime = SystemClock.elapsedRealtimeNanos();

        mLogger.log("All surfaces are ready, starting capture");

        // Begin data taking
        mCaptureManager = new CaptureManager(mCameraDevice, surfaces);
        mCaptureManager.createCaptureSession();

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }
    */
    //----------------------------------------------------------------------------------------------

    /*
    public static void processImage(TotalCaptureResult result) {
        Log.e(Thread.currentThread().getName(), "CaptureOverseer.processImage(TotalCaptureResult)");
        mImageProcessor.processImage(result);
    }

    public static void processImage(byte[] imageBytes) {
        Log.e(Thread.currentThread().getName(), "CaptureOverseer.processImage(byte[])");
        mImageProcessor.processImage(imageBytes);
    }

    public static void quitSafely() {
        mShrampCamManager.closeBackCamera();
        HandlerManager.finish();
        mInstance.finish();
    }

    public static void post(Runnable runnable) {
        mHandler.post(runnable);
    }
    */























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

            CameraController    cameraManager    = CaptureOverseer.getCameraManager();
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
