package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.text.DecimalFormat;

import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
class CaptureStream extends CameraCaptureSession.CaptureCallback {

    enum State {ACTIVE, PAUSED, FINISHED}

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private State mState;

    private class mFrame {
        int FrameLimit;
        int FrameCount;

        void setLimit(int limit) {
            FrameLimit = limit;
            FrameCount = 0;
        }

        void raiseFrameCount() {
            FrameCount += 1;

            Log.e(Thread.currentThread().getName(), "CaptureStream completed " + Integer.toString(FrameCount)
                    + " of " + Integer.toString(FrameLimit) + " frames");
            Log.e("Frame Break", ".......................................................................");


            if (FrameCount >= FrameLimit) {
                mState = State.FINISHED;
                CaptureManager.pauseCaptureSession();
            }
        }
    }
    private final mFrame mFrame = new mFrame();

    class mTimestamp {
        long First   = 0L;
        long Last    = 0L;
        long Elapsed = 0L;

        void add(TotalCaptureResult result) {
            Long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
            assert timestamp != null;

            if (First == 0L) {
                First = timestamp;
            }
            else {
                Elapsed = timestamp - Last;
            }
            Last = timestamp;
        }
    }
    private final mTimestamp mTimestamp = new mTimestamp();

    class mExposure {
        long Total = 0L;
        long Last  = 0L;

        void add(TotalCaptureResult result) {
            Long exposure = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            assert exposure != null;
            Last   = exposure;
            Total += exposure;
        }
    }
    private final mExposure mExposure = new mExposure();


    //==============================================================================================
    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mFormatter = new DecimalFormat("#.##");
    //==============================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureStream................................................................................
    /**
     * TODO: description, comments and logging
     */
    private CaptureStream() {
        super();
    }

    // CaptureStream................................................................................
    /**
     * TODO: description, comments and logging
     * @param frameLimit bla
     */
    CaptureStream(int frameLimit) {
        this();
        mState = State.ACTIVE;
        mFrame.setLimit(frameLimit);
        Log.e(Thread.currentThread().getName(), "captureStream captureStream frameLimit: " + Integer.toString(frameLimit));
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private void progressedNotification(CaptureResult partialResult) {
        HeapMemory.logAvailableMiB();
        Long timestamp = partialResult.get(CaptureResult.SENSOR_TIMESTAMP);
        if (timestamp != null) {
            Log.e(Thread.currentThread().getName(), "captureStream onCaptureProgressed, working on " + TimeCode.toString(timestamp));
        }
        else {
            Log.e(Thread.currentThread().getName(), "captureStream onCaptureProgressed (precapture)");
        }
    }

    private void completedNotification(TotalCaptureResult completedResult) {
        Log.e(Thread.currentThread().getName(), "CaptureStream just posted the completed capture of " + TimeCode.toString(mTimestamp.Last));

        Long duration = completedResult.get(CaptureResult.SENSOR_FRAME_DURATION);
        assert duration != null;

        Log.e(Thread.currentThread().getName(), "Frame duration: " + Long.toString(duration)
                + ",  Exposure: " + Long.toString(mExposure.Last));


        double fps      = 1. / (mTimestamp.Elapsed * 1e-9);
        double duty     = 100. * mExposure.Last / (double) duration;
        long   deadTime = mTimestamp.Elapsed - duration;

        Log.e(Thread.currentThread().getName(), "Capture FPS: " + mFormatter.format(fps)
                + ", Duty: " + mFormatter.format(duty) + "%"
                + ", Dead time: "    + Long.toString(deadTime)   + " [ns]");

    }


    // Public Overriding Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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
        StopWatch stopWatch = new StopWatch();
        progressedNotification(partialResult);
        if (HeapMemory.isMemoryLow()) {
            Log.e("DANGER LOW MEMORY", "DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER DANGER");
            HeapMemory.logAvailableMiB();
            mState = State.PAUSED;
            CaptureManager.pauseCaptureSession();
        }
        Log.e(Thread.currentThread().getName(), "<onCaptureProgressed()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
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
        StopWatch stopWatch = new StopWatch();
        DataQueue.add(result);
        mTimestamp.add(result);
        mExposure.add(result);
        completedNotification(result);
        mFrame.raiseFrameCount();
        Log.e(Thread.currentThread().getName(), "<onCaptureCompleted()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
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
        StopWatch stopWatch = new StopWatch();

        if (mState == State.PAUSED) {
            Log.e(Thread.currentThread().getName(), "captureStream *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause *** pause");
            CaptureManager.restartCaptureSession();
            Log.e(Thread.currentThread().getName(), "<onCaptureSequenceCompleted()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
            return;
        }

        if (mState == State.FINISHED) {
            Log.e(Thread.currentThread().getName(), "captureStream onCaptureSequenceCompleted, N Frames = " + Integer.toString(mFrame.FrameCount));

            long totalElapsed = mTimestamp.Last - mTimestamp.First;
            double averageFps = mFrame.FrameCount / (totalElapsed * 1e-9);
            double averageDuty = mExposure.Total/ (double) totalElapsed;

            Log.e(Thread.currentThread().getName(), "<onCaptureSequenceCompleted()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");

            DataQueue.purge();
            synchronized (this) {
                while (!DataQueue.isEmpty() || ImageProcessor.isBusy()) {
                    try {
                        this.wait(2 * CaptureManager.getTargetFrameNanos() / 1000 / 1000);
                    }
                    catch (InterruptedException e) {
                        // TODO: error
                    }
                }
            }

            CaptureManager.sessionFinished(averageFps, averageDuty);

            // TODO: dump mTotalCaptureResult info

        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: IGNORE ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        //Log.e(Thread.currentThread().getName(), "CaptureSessionOld onCaptureStarted for: "
        //        + TimeCode.toString(timestamp) + ", frame number: " + Long.toString(frameNumber));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: SHUTDOWN //////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        Log.e(Thread.currentThread().getName(), "captureStream onCaptureBufferLost");
        // TODO: shutdown
        mFrame.raiseFrameCount();
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
        mFrame.raiseFrameCount();
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
        // TODO: shutdown
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
        Log.e(Thread.currentThread().getName(), "captureStream onCaptureSequenceAborted");
        // TODO: shutdown
    }

}


