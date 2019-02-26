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
import android.view.Surface;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
            Log.e("Tag", "Surface prepared");
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

            Log.e("Tag", "Ready to start repeating request..");
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
            Log.e("Tag", "Capture session is active and processing requests!");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called every time the session has no more capture requests to process.
         * @param session
         */
        @Override
        public void onReady(CameraCaptureSession session) {
            Log.e("Tag", "Session is ready for capture requests");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called when camera device's input capture queue becomes empty,
         * and is ready to accept the next request.
         * @param session
         */
        @Override
        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            Log.e("Tag", "Capture queue is empty");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called if the session cannot be configured as requested.
         * (Required)
         * @param session
         */
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.e("Tag", "Capture session configuration failed!");
            mCameraCaptureSession = session;
        }

        /**
         * This method is called when the session is closed.
         * @param session
         */
        @Override
        public void onClosed(CameraCaptureSession session) {
            Log.e("Tag", "Capture session is closed.");
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

        private final long CAPTURE_LIMIT = 1000;

        private Surface            mmTarget;

        private CaptureResult      mmCaptureResult;
        private CaptureFailure     mmCaptureFailure;
        private TotalCaptureResult mmTotalCaptureResult;

        private long               mmTimestamp;
        private long               mmFrameNumber;
        private int                mmSequenceId;

        private long mmCaptureCount = 0;
        private final long COUNT_LIMIT = 10;

        private long mmProgressed2ProgressedNanoSec = 0;
        private long mmStarted2StartedNanoSec       = 0;
        private long mmCompleted2CompletedNanoSec   = 0;
        private long mmProgressed2StartedNanoSec    = 0;
        private long mmStarted2CompletedNanoSec     = 0;
        private long mmCompleted2ProgressedNanoSec  = 0;

        private List<Long> mmElapsedP2P = new ArrayList<>();
        private List<Long> mmElapsedS2S = new ArrayList<>();
        private List<Long> mmElapsedC2C = new ArrayList<>();
        private List<Long> mmElapsedP2S = new ArrayList<>();
        private List<Long> mmElapsedS2C = new ArrayList<>();
        private List<Long> mmElapsedC2P = new ArrayList<>();

        private long       mmLastTimestamp    = 0;
        private List<Long> mmElapsedTimestamp = new ArrayList<>();

        private List<Long> mmStartTimestamp =  new ArrayList<>();
        private List<Long> mmCompTimestamp = new ArrayList<>();

        private int progress = 0;

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
            long time = SystemClock.elapsedRealtimeNanos();
            mmProgressed2StartedNanoSec = time;

            // remove first element!
            long elapsed = (time - mmCompleted2ProgressedNanoSec);
            mmElapsedC2P.add(elapsed);
            //Log.e("Tag", "Complete-to-Progressed [sec]: " + Double.toString(elapsed*1e-9));

            mmCaptureCount += 1;
            //Log.e("Tag", Long.toString(mmCaptureCount));
            if (mmCaptureCount <= COUNT_LIMIT) {
                Log.e("Tag", "Capture making some forward progress, count = " + Long.toString(mmCaptureCount));
                if (mmCaptureCount == COUNT_LIMIT) {
                    Log.e("Tag", "Silencing further notifications");
                }
            }

            if (mmProgressed2ProgressedNanoSec == 0) {
                mmProgressed2ProgressedNanoSec = time;
            }
            else {
                mmElapsedP2P.add(time - mmProgressed2ProgressedNanoSec);
                mmProgressed2ProgressedNanoSec = time;
            }

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmCaptureResult       = partialResult;

            progress += 1;
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
            long time = SystemClock.elapsedRealtimeNanos();
            mmStarted2CompletedNanoSec = time;

            mmStartTimestamp.add(time - timestamp);
            mmElapsedP2S.add(time - mmProgressed2StartedNanoSec);

            if (mmCaptureCount <= COUNT_LIMIT) {
                Log.e("Tag", "Capture has started!");
                if (mmCaptureCount == COUNT_LIMIT) {
                    Log.e("Tag", "Silencing further notifications");
                }
            }

            if (mmStarted2StartedNanoSec == 0) {
                mmStarted2StartedNanoSec = time;
            }
            else {
                mmElapsedS2S.add(time - mmStarted2StartedNanoSec);
                mmStarted2StartedNanoSec = time;
            }

            if (mmLastTimestamp != 0) {
                mmElapsedTimestamp.add(timestamp - mmLastTimestamp);
            }
            mmLastTimestamp = timestamp;

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTimestamp           = timestamp;
            mmFrameNumber         = frameNumber;

            progress += 1;
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
            long time = SystemClock.elapsedRealtimeNanos();
            mmCompleted2ProgressedNanoSec = time;

            mmCompTimestamp.add(time - mmTimestamp);
            mmElapsedS2C.add(time - mmStarted2CompletedNanoSec);

            if (mmCaptureCount <= COUNT_LIMIT) {
                Log.e("Tag","One capture successful");
                if (mmCaptureCount == COUNT_LIMIT) {
                    Log.e("Tag","Silencing further notifications");
                }
            }

            if (mmCompleted2CompletedNanoSec == 0) {
                mmCompleted2CompletedNanoSec = time;
            }
            else {
                mmElapsedC2C.add(time - mmCompleted2CompletedNanoSec);
                mmCompleted2CompletedNanoSec = time;
            }

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmTotalCaptureResult  = result;

            if (mmCaptureCount == CAPTURE_LIMIT) {
                try {
                    session.stopRepeating();
                }
                catch (CameraAccessException e) {
                    mLogger.log("ERROR: Camera Access Exception");
                }
            }

            if (progress != 2) {
                Log.e("Tag", "Dropped, index: " + Long.toString(mmCaptureCount));
            }
            progress = 0;
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

            // remove garbage first and last elements
            for (int i = 0; i < 4; i++ ) {
                mmElapsedP2S.remove(0);
                mmElapsedS2C.remove(0);
                mmElapsedC2P.remove(0);
                mmStartTimestamp.remove(0);
                mmCompTimestamp.remove(0);
            }
            long max = 0;
            long max_index = 0;
            long index = 0;
            long min = -1;
            for (long val : mmElapsedC2P) {
                if (val > max) {
                    max = val;
                    max_index = index;
                }
                if (min == -1) {
                    min = val;
                }
                if (val < min) {
                    min = val;
                }
                index += 1;
            }
            Log.e("Tag", " \n max val @ index: " + Long.toString(max) + " @ " + Long.toString(max_index));
            Log.e("Tag", " \n min val: " + Long.toString(min));

            double p2pFpsAve = getAve(mmElapsedP2P);
            double s2sFpsAve = getAve(mmElapsedS2S);
            double c2cFpsAve = getAve(mmElapsedC2C);
            double p2sFpsAve = getAve(mmElapsedP2S);
            double s2cFpsAve = getAve(mmElapsedS2C);
            double c2pFpsAve = getAve(mmElapsedC2P);
            double timestampFpsAve = getAve(mmElapsedTimestamp);
            double startTimeAve = getAve(mmStartTimestamp);
            double compTimeAve = getAve(mmCompTimestamp);

            //

            double p2pFpsStdDev = getStdDev(mmElapsedP2P, p2pFpsAve) * 1e-9;
            double s2sFpsStdDev = getStdDev(mmElapsedS2S, s2sFpsAve) * 1e-9;
            double c2cFpsStdDev = getStdDev(mmElapsedC2C, c2cFpsAve) * 1e-9;
            double p2sFpsStdDev = getStdDev(mmElapsedP2S, p2sFpsAve) * 1e-9;
            double s2cFpsStdDev = getStdDev(mmElapsedS2C, s2cFpsAve) * 1e-9;
            double c2pFpsStdDev = getStdDev(mmElapsedC2P, c2pFpsAve) * 1e-9;
            double timestampFpsStdDev = getStdDev(mmElapsedTimestamp, timestampFpsAve) * 1e-9;
            double startTimeStdDev = getStdDev(mmStartTimestamp, startTimeAve);
            double compTimeStdDev = getStdDev(mmCompTimestamp, compTimeAve);

            //

            p2pFpsAve *= 1e-9;
            s2sFpsAve *= 1e-9;
            c2cFpsAve *= 1e-9;
            p2sFpsAve *= 1e-9;
            s2cFpsAve *= 1e-9;
            c2pFpsAve *= 1e-9;
            timestampFpsAve *= 1e-9;

            double p2pFpsError = (1. / (p2pFpsAve * p2pFpsAve)) * p2pFpsStdDev;
            double s2sFpsError = (1. / (s2sFpsAve * s2sFpsAve)) * s2sFpsStdDev;
            double c2cFpsError = (1. / (c2cFpsAve * c2cFpsAve)) * c2cFpsStdDev;
            double p2sFpsError = (1. / (p2sFpsAve * p2sFpsAve)) * p2sFpsStdDev;
            double s2cFpsError = (1. / (s2cFpsAve * s2cFpsAve)) * s2cFpsStdDev;
            double c2pFpsError = (1. / (c2pFpsAve * c2pFpsAve)) * c2pFpsStdDev;
            double timestampError = (1. / (timestampFpsAve * timestampFpsAve)) * timestampFpsStdDev;

            p2pFpsAve = 1. / p2pFpsAve;
            s2sFpsAve = 1. / s2sFpsAve;
            c2cFpsAve = 1. / c2cFpsAve;
            p2sFpsAve = 1. / p2sFpsAve;
            s2cFpsAve = 1. / s2cFpsAve;
            c2pFpsAve = 1. / c2pFpsAve;
            timestampFpsAve = 1. / timestampFpsAve;


            DecimalFormat df = new DecimalFormat("#.##");

            String statistics = " \n"
                    + "Capture completed, statistics: \n"
                    + "--------------------------------------------------------------- \n"
                    + "\t Progressed-to-Progressed FPS: " + df.format(p2pFpsAve) + " +/- " + df.format(p2pFpsError) + "\n"
                    + "\t Started-to-Started FPS:       " + df.format(s2sFpsAve) + " +/- " + df.format(s2sFpsError) + "\n"
                    + "\t Completed-to-Completed FPS:   " + df.format(c2cFpsAve) + " +/- " + df.format(c2cFpsError) + "\n"
                    + "\t Timestamp-to-Timestamp FPS:   " + df.format(timestampFpsAve) + " +/- " + df.format(timestampError) + "\n"
                    + "\n"
                    + "\t Progressed-to-Started FPS:   " + df.format(p2sFpsAve) + " +/- " + df.format(p2sFpsError) + "\n"
                    + "\t Started-to-Completed FPS:    " + df.format(s2cFpsAve) + " +/- " + df.format(s2cFpsError) + "\n"
                    + "\t Completed-to-Progressed FPS: " + df.format(c2pFpsAve) + " +/- " + df.format(c2pFpsError) + "\n"
                    + "\n"
                    + "\t Started - Timestamp [sec]:   " + df.format(startTimeAve) + " +/- " + df.format(startTimeStdDev) + "\n"
                    + "\t Completed - Timestamp [sec]: " + df.format(compTimeAve) + " +/- " + df.format(compTimeStdDev) + "\n";

            //mLogger.log(statistics);
            Log.e("tag", statistics);

            session.close();
            CameraManager    cameraManager    = CaptureOverseer.getCameraManager();
            ShrampCamManager shrampCamManager = ShrampCamManager.getInstance(cameraManager);
            assert shrampCamManager != null;
            shrampCamManager.closeBackCamera();
        }

        private double getAve(List<Long> list) {
            long sum = 0;
            for (long val : list) {
                sum += val;
            }
            return sum / (double) list.size();
        }

        private double getStdDev(List<Long> list, double mean) {
            double sum = 0.;
            for (long val : list) {
                sum += (val - mean) * (val - mean);
            }
            return Math.sqrt(sum / (double) list.size());
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
            Log.e("Tag", "Capture sequence has been aborted");
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
            Log.e("Tag", "A capture buffer has been lost and not sent to its destination surface");
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
            Log.e("Tag", "Camera device failed to produce a CaptureResult");
            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mmCaptureFailure      = failure;
        }
    }
}
