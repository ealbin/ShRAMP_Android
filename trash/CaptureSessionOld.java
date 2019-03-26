package recycle_bin;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.text.DecimalFormat;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HeapMemory;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
class CaptureSessionOld extends CameraCaptureSession.CaptureCallback {

    //**********************************************************************************************
    // Class Fields
    //---------------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


    private int mFrameLimit;
    private int mFrameCount;

    private long mLastCompleted;
    private long mElapsedTime;
    private long mTotalExposure;

    private long mFirstTimestamp;
    private long mLastTimestamp;

    private static CaptureSessionOld mInstance;

    /**
     * TODO: description, comments and logging
     */
    class QueueCaptureResult implements Runnable {
        private TotalCaptureResult nResult;

        private QueueCaptureResult() { assert false; }

        QueueCaptureResult(TotalCaptureResult result) {
            nResult = result;
        }

        @Override
        public void run() {
            ImageProcessorOld.processImage(nResult);
        }
    }

    //==============================================================================================
    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mFormatter = new DecimalFormat("#.##");
    //==============================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureSessionOld...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private CaptureSessionOld() {
        super();
        //Log.e(Thread.currentThread().getName(), "CaptureSessionOld CaptureSessionOld");
    }

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureSessionOld...............................................................................
    /**
     * TODO: description, comments and logging
     * @param frameLimit bla
     */
    CaptureSessionOld(int frameLimit) {
        this();
        mFrameLimit     = frameLimit;
        mFrameCount     = 0;
        mLastCompleted  = 0;
        mElapsedTime    = 0;
        mTotalExposure  = 0;
        mInstance = this;
        Log.e(Thread.currentThread().getName(), "CaptureSessionOld CaptureSessionOld frameLimit: " + Integer.toString(frameLimit));
    }

    //**********************************************************************************************
    // Class Methods
    //--------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // checkIfDone..................................................................................
    /**
     * TODO: description, comments and logging
     * @param session bla
     */
    private synchronized void checkIfDone(CameraCaptureSession session) {
        //Log.e(Thread.currentThread().getName(), "CaptureSessionOld checkIfDone");
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

    private static boolean mPauseCapture = false;
    static synchronized boolean pauseRepeatingRequest() {
        mPauseCapture = true;
        return true;
    }

    static void resumeRepeatingRequest() {
        mPauseCapture = false;
    }


    //**********************************************************************************************
    // Overriding Methods
    //-------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onCaptureBufferLost..........................................................................
    /**
     * This method is called if a single buffer for a capture could not be sent to its
     * destination surfaces.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param request bla
     * @param target bla
     * @param frameNumber bla
     */
    @Override
    public void onCaptureBufferLost(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull Surface target, long frameNumber) {
        super.onCaptureBufferLost(session, request, target, frameNumber);
        Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureBufferLost");
        checkIfDone(session);
    }

    // onCaptureCompleted...........................................................................
    /**
     * This method is called when an image capture has fully completed and all the result
     * metadata is available.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param request bla
     * @param result bla
     */
    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        //Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureCompleted");
        long now = SystemClock.elapsedRealtimeNanos();
        //if (!GlobalSettings.DEBUG_NO_DATA_POSTING) {
            //ImageProcessorOld.post(new QueueCaptureResult(result));
            DataQueue.add(result);
        //}
        Long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
        assert timestamp != null;
        Log.e(Thread.currentThread().getName(), "CaptureSessionOld just posted the completed capture of " + TimeCode.toString(timestamp));

        if (mLastCompleted == 0) {
            mLastCompleted = now;
            mFirstTimestamp = timestamp;
        }
        else {
            Long exposure = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            assert exposure != null;
            mTotalExposure += exposure;

            Long duration = result.get(CaptureResult.SENSOR_FRAME_DURATION);
            assert duration != null;

            Log.e(Thread.currentThread().getName(), "Frame durration: " + Long.toString(duration)
            + ",  Exposure: " + Long.toString(exposure));

            mLastCompleted = now;

            mElapsedTime = timestamp - mLastTimestamp;

            double fps  = 1. / (mElapsedTime * 1e-9);
            double frameDuty = 100. * exposure / (double) duration;
            double lagDuty   = 100. * exposure / (double) mElapsedTime;

            Log.e(Thread.currentThread().getName(), "Capture FPS: " + mFormatter.format(fps)
                    + ", Exposure / Duration duty: " + mFormatter.format(frameDuty) + "%"
                    + ", Exposure / Frame duty: "    + mFormatter.format(lagDuty)   + "%");
        }
        mLastTimestamp = timestamp;

        Log.e(Thread.currentThread().getName(), "CaptureSessionOld completed " + Integer.toString(mFrameCount)
                + " of " + Integer.toString(mFrameLimit) + " frames");
        Log.e("Frame Break", ".......................................................................");

        checkIfDone(session);
    }

    // onCaptureFailed..............................................................................
    /**
     * This method is called instead of onCaptureCompleted(CameraCaptureSession, captureRequest,
     * TotalCaptureResult) when the camera device failed to produce a CaptureResult for the request.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param request bla
     * @param failure bla
     */
    @Override
    public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                @NonNull CaptureRequest request,
                                @NonNull CaptureFailure failure) {
        super.onCaptureFailed(session, request, failure);
        Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureFailed");
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

    // onCaptureProgressed..........................................................................
    /**
     * This method is called when an image capture makes partial forward progress;
     * some (but not all) results from an image capture are available.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param request bla
     * @param partialResult bla
     */
    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
        super.onCaptureProgressed(session, request, partialResult);

        Long timestamp = partialResult.get(CaptureResult.SENSOR_TIMESTAMP);
        if (timestamp != null) {
            Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureProgressed, working on " + TimeCode.toString(timestamp));
        }
        else {
            Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureProgressed");
        }
        long freeMiB = HeapMemory.getAvailableMiB();

        Log.e(Thread.currentThread().getName(), "CaptureSessionOld Free Heap Memory: " + Long.toString(freeMiB) + " [MiB]");

        // override
        if (freeMiB < ImageProcessorOld.LOW_MEMORY) {
            //mPauseCapture = CaptureManager.pauseRepeatingRequest();
            Log.e("DANGER LOW MEMORY", "DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER");
        }

        //if (GlobalSettings.DEBUG_START_STOP_CAPTURE && mFrameCount < mFrameLimit) {
            //mPauseCapture = CaptureManager.pauseRepeatingRequest();
        //}

        if (mPauseCapture) {
            try {
                session.stopRepeating();
            }
            catch (CameraAccessException e) {
                // TODO:  error
            }
        }
    }

    // onCaptureSequenceAborted.....................................................................
    /**
     * This method is called independently of the others in CaptureCallback, when a capture
     * sequence aborts before any CaptureResult or CaptureFailure for it have been returned
     * via this listener.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param sequenceId bla
     */
    @Override
    public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
        super.onCaptureSequenceAborted(session, sequenceId);
        Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureSequenceAborted");
    }

    // onCaptureSequenceCompleted...................................................................
    /**
     * This method is called independently of the others in CaptureCallback, when a capture
     * sequence finishes and all CaptureResult or CaptureFailure for it have been
     * returned via this listener.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param sequenceId bla
     * @param frameNumber bla
     */
    @Override
    public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                           int sequenceId,
                                           long frameNumber) {
        super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);

        if (mPauseCapture) {
            Log.e(Thread.currentThread().getName(), "CaptureSessionOld *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause");
            //CaptureManager.pauseRepeatingRequest(session, this);

            //if (GlobalSettings.DEBUG_START_STOP_CAPTURE) {
                //CaptureManager.restartRepeatingRequest();
            //}
        }
        else {
            Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureSequenceCompleted, N Frames = " + Integer.toString(mFrameCount));

            long totalElapsed = mLastTimestamp - mFirstTimestamp;
            double averageFps = mFrameCount / (totalElapsed * 1e-9);
            double averageDuty = mTotalExposure / (double) totalElapsed;

            //CaptureManager.sessionFinished(session, averageFps, averageDuty);

            // TODO: dump mTotalCaptureResult info
        }
    }

    // onCaptureStarted.............................................................................
    /**
     * This method is called when the camera device has started capturing the output image
     * for the request, at the beginning of image exposure, or when the camera device has
     * started processing an input image for a reprocess request.
     * TODO: documentation, comments and logging
     * @param session bla
     * @param request bla
     * @param timestamp bla
     * @param frameNumber bla
     */
    @Override
    public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                 @NonNull CaptureRequest request,
                                 long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
        Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureStarted for: "
                + TimeCode.toString(timestamp) + ", frame number: " + Long.toString(frameNumber));
    }

}
