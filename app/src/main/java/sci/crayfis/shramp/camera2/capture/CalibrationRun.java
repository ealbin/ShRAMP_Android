package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.text.DecimalFormat;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.logging.ShrampLogger;

@TargetApi(21)
class CalibrationRun extends CameraCaptureSession.CaptureCallback {


    //******************************************************************************************
    // Class Variables
    //----------------

    private int mFrameLimit;
    private int mFrameCount;

    private long mLastCompleted;
    private long mElapsedTime;
    private long mTotalExposure;

    private long mFirstTimestamp;
    private long mLastTimestamp;

    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mFormatter = new DecimalFormat("#.##");

    //******************************************************************************************
    // Class Methods
    //--------------

    private CalibrationRun() { super(); }

    CalibrationRun(int frameLimit) {
        this();
        mFrameLimit     = frameLimit;
        mFrameCount     = 0;
        mLastCompleted  = 0;
        mElapsedTime    = 0;
        mTotalExposure  = 0;
        mLogger.log("Calibration run frame limit: " + Integer.toString(frameLimit));
    }

    /**
     * This method is called when an image capture makes partial forward progress;
     * some (but not all) results from an image capture are available.
     *
     * @param session
     * @param request
     * @param partialResult
     */
    @Override
    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureResult partialResult) {
        super.onCaptureProgressed(session, request, partialResult);
    }

    /**
     * This method is called when the camera device has started capturing the output image
     * for the request, at the beginning of image exposure, or when the camera device has
     * started processing an input image for a reprocess request.
     *
     * @param session
     * @param request
     * @param timestamp
     * @param frameNumber
     */
    @Override
    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                 long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
    }

    /**
     * This method is called when an image capture has fully completed and all the result
     * metadata is available.
     *
     * @param session
     * @param request
     * @param result
     */
    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                   TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        long now = SystemClock.elapsedRealtimeNanos();
        CaptureOverseer.processImage(result);

        Long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
        assert timestamp != null;
        mLastTimestamp = timestamp;

        if (mLastCompleted == 0) {
            mLastCompleted = now;
            mFirstTimestamp = timestamp;
        }
        else {
            Long exposure = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            assert exposure != null;
            mTotalExposure += exposure;

            mElapsedTime   = now - mLastCompleted;
            mLastCompleted = now;

            double fps  = 1. / (mElapsedTime * 1e-9);
            double duty = 100. * exposure / (double) mElapsedTime;

            Log.e(Thread.currentThread().getName(), "Capture FPS: " + mFormatter.format(fps)
                    + ", Exposure duty: " + mFormatter.format(duty) + "%");
        }
        checkIfDone(session);
    }

    private void checkIfDone(CameraCaptureSession session) {
        mFrameCount += 1;
        if (mFrameCount >= mFrameLimit) {
            try {
                session.stopRepeating();
            }
            catch (CameraAccessException e) {
                // TODO:  error
            }
        }
    }

    /**
     * This method is called independently of the others in CaptureCallback, when a capture
     * sequence finishes and all CaptureResult or CaptureFailure for it have been
     * returned via this listener.
     *
     * @param session
     * @param sequenceId
     * @param frameNumber
     */
    @Override
    public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId,
                                           long frameNumber) {
        super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        mLogger.log("Capture session completed");

        long   totalElapsed = mLastTimestamp - mFirstTimestamp;
        double averageFps   = mFrameCount / ( totalElapsed * 1e-9 );
        double averageDuty  = mTotalExposure / (double) totalElapsed;

        CaptureManager.sessionFinished(session, this, averageFps, averageDuty);

        // TODO: dump mTotalCaptureResult info
    }

    /**
     * This method is called independently of the others in CaptureCallback, when a capture
     * sequence aborts before any CaptureResult or CaptureFailure for it have been returned
     * via this listener.
     *
     * @param session
     * @param sequenceId
     */
    @Override
    public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
        super.onCaptureSequenceAborted(session, sequenceId);
    }


    /**
     * This method is called if a single buffer for a capture could not be sent to its
     * destination surfaces.
     *
     * @param session
     * @param request
     * @param target
     * @param frameNumber
     */
    @Override
    public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                                    Surface target, long frameNumber) {
        super.onCaptureBufferLost(session, request, target, frameNumber);
        checkIfDone(session);
    }


    /**
     * This method is called instead of onCaptureCompleted(CameraCaptureSession, CaptureRequest,
     * TotalCaptureResult) when the camera device failed to produce a CaptureResult for the request.
     *
     * @param session
     * @param request
     * @param failure
     */
    @Override
    public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                CaptureFailure failure) {
        super.onCaptureFailed(session, request, failure);
        checkIfDone(session);

        /*
        String reason = null;
        if (failure.getReason() == CaptureFailure.REASON_ERROR) {
            reason = "Dropped frame due to error in framework";
        } else {
            reason = "Failure due to CameraCaptureSession.abortCaptures()";
        }
        String errInfo = "Camera device failed to produce a CaptureResult\n"
                + "\t Reason:         " + reason + "\n"
                + "\t Frame number:   " + Long.toString(failure.getFrameNumber()) + "\n"
                + "\t Sequence ID:    " + Integer.toString(failure.getSequenceId()) + "\n"
                + "\t Image captured: " + Boolean.toString(failure.wasImageCaptured()) + "\n";
        Log.e("Tag", errInfo);

        // End exposure block after EXPOSURE_DURATION_NANOS time
        if (SystemClock.elapsedRealtimeNanos() >= CaptureOverseer.mFinishEpoch) {
            try {
                session.stopRepeating();
            } catch (CameraAccessException e) {
                CaptureOverseer.mLogger.log("ERROR: Camera Access Exception");
            }
        }
        */
    }
}
