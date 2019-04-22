/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 20 April 2019
 */

package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.battery.BatteryController;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;
import sci.crayfis.shramp.util.StorageMedia;
import sci.crayfis.shramp.util.Datestamp;

/**
 * Monitors capture stream on a frame by frame basis, receiving capture metadata
 */
@TargetApi(21)
final class CaptureMonitor extends CameraCaptureSession.CaptureCallback {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // state........................................................................................
    // state of the capture session
    private enum State {ACTIVE, PAUSED, FINISHED}

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mState.......................................................................................
    // Current state
    private State mState;

    // mFrame.......................................................................................
    // Encapsulation of frame count and limit, responsible for determining when to stop capture
    private class Frame {

        int FrameLimit;
        int FrameCount;

        // setLimit.................................................................................
        /**
         * Set condition to end capture
         * @param limit Maximum number of frames to capture before stopping
         */
        void setLimit(int limit) {
            FrameLimit = limit;
            FrameCount = 0;
        }

        // raiseFrameCount..........................................................................
        /**
         * Increase frame capture count, and stop capture if frame count has exceeded the limit
         */
        void raiseFrameCount() {
            FrameCount += 1;

            String dots = ".......................................................................";
            Log.e(Thread.currentThread().getName(), " \n" + dots + "\n"
                    + "Captured " + Integer.toString(FrameCount) + " of "
                    + Integer.toString(FrameLimit) + " frames" + "\n" + dots);

            if (FrameCount >= FrameLimit) {
                Log.e(Thread.currentThread().getName(), "Frame count met, ending capture");
                mState = State.FINISHED;
                CaptureController.pauseSession();
            }
        }
    }
    private final Frame mFrame = new Frame();

    // mTemperature.................................................................................
    // Encapsulation of battery temperature statistics, stop capture if temperature exceeds limit
    class Temperature {
        Double First;
        Double Last;
        Double Max;
        Double Min;
        Double Sum;
        Long   Count;
        Double Limit;

        // setLimit.................................................................................
        /**
         * Set temperature limit to end capture
         * @param temperatureLimit maximum temperature for capture
         */
        void setLimit(double temperatureLimit) {
            Limit = temperatureLimit;
        }

        // logTemperature...........................................................................
        /**
         * Log current battery temperature
         */
        void logTemperature() {
            Last = BatteryController.getCurrentTemperature();
            if (Last == null) {
                return;
            }

            if (First == null) {
                First = Last;
                Max   = Last;
                Min   = Last;
                Sum   = 0.;
                Count = 0L;
            }

            if (Max < Last) {
                Max = Last;
            }
            if (Min > Last) {
                Min = Last;
            }

            Sum   += Last;
            Count += 1;

            if (Last >= Limit) {
                Log.e(Thread.currentThread().getName(), "Temperature limit met, ending capture");
                mState = State.FINISHED;
                CaptureController.pauseSession();
            }
        }

        // getMean..................................................................................
        /**
         * @return mean temperature recorded
         */
        @Nullable
        Double getMean() {
            if (Sum == null) {
                return null;
            }
            return Sum / (double) Count;
        }

        // getLastString............................................................................
        /**
         * @return a string representation of the last temperature recorded
         */
        @Nullable
        String getLastString() {
            if (Last == null) {
                return null;
            }
            return NumToString.number(Last) + " [Celsius]";
        }

        // getString................................................................................
        /**
         * @return a string of temperature statistics
         */
        @Nullable
        String getString() {
            if (Count == null) {
                return null;
            }
            String out = " \n";
            out += "Temperature [Celsius] \n";
            out += "\t" + "Start: " + NumToString.number(First) + "\n";
            out += "\t" + "Last:  " + NumToString.number(Last) + "\n";
            out += "\t" + "Low:   " + NumToString.number(Min) + "\n";
            out += "\t" + "High:  " + NumToString.number(Max) + "\n";
            out += "\t" + "Mean:  " + NumToString.number(getMean()) + "\n";
            return out;
        }
    }
    private final Temperature mTemperature = new Temperature();

    // mTimestamp...................................................................................
    // Encapsulation of timestamp information
    class Timestamp {

        long First   = 0L;
        long Last    = 0L;
        long Elapsed = 0L;

        // add......................................................................................
        /**
         * Add current sensor timestamp to the record
         * @param result latest capture result
         */
        void add(TotalCaptureResult result) {
            Long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
            if (timestamp == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Sensor timestamp cannot be null");
                MasterController.quitSafely();
                return;
            }

            if (First == 0L) {
                First = timestamp;
                Datestamp.resetElapsedNanos(timestamp);
            }
            else {
                Elapsed = timestamp - Last;
            }
            Last = timestamp;
        }
    }
    private final Timestamp mTimestamp = new Timestamp();

    // mDeadtime....................................................................................
    // Encapsulation of dead time statistics
    class Deadtime {
        long Sum = 0L;
        long Min = -1L;
        long Max = -1L;
        long Count = 0;

        // add......................................................................................
        /**
         * Add dead time to record
         * @param deadtime time between frames in nanoseconds
         */
        void add(long deadtime) {
            if (Min == -1L) {
                Min = deadtime;
            }
            if (Max == -1L) {
                Max = deadtime;
            }
            if (Min > deadtime) {
                Min = deadtime;
            }
            if (Max < deadtime) {
                Max = deadtime;
            }
            Sum   += deadtime;
            Count += 1;
        }

        // getMean..................................................................................
        /**
         * @return mean dead time
         */
        double getMean() {
            return Sum / (double) Count;
        }

        // getString................................................................................
        /**
         * @return a string of dead time statistics
         */
        @NonNull
        String getString() {
            String out = " \n";
            out += "Deadtime [ns] \n";
            out += "\t" + "Min:   " + NumToString.number(Min) + "\n";
            out += "\t" + "Max:   " + NumToString.number(Max) + "\n";
            out += "\t" + "Total: " + NumToString.number(Sum) + "\n";
            out += "\t" + "Mean:  " + NumToString.number(getMean()) + "\n";
            return out;
        }
    }
    private final Deadtime mDeadtime = new Deadtime();

    // mExposure....................................................................................
    // Encapsulation of sensor exposure statistics
    class Exposure {

        long Total = 0L;
        long Last  = 0L;
        long Min   = -1L;
        long Max   = -1L;
        long Count = 0;

        // add......................................................................................
        /**
         * Add frame exposure to the record
         * @param result capture result to add
         */
        void add(TotalCaptureResult result) {
            Long exposure = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            if (exposure == null) {
                Log.e(Thread.currentThread().getName(), "Sensor exposure time is not available");
                Last = 0L;
            }
            else {
                Last = exposure;
            }

            Total += Last;
            Count += 1;

            if (Count == 1) {
                Min = Last;
                Max = Last;
            }

            if (Min > Last) {
                Min = Last;
            }
            if (Max < Last) {
                Max = Last;
            }
        }

        // getMean..................................................................................
        /**
         * @return mean exposure
         */
        double getMean() {
            return Total / (double) Count;
        }

        // getString................................................................................
        /**
         * @return a string of exposure statistics
         */
        @NonNull
        String getString() {
            String out = " \n";
            out += "Exposure [ns] \n";
            out += "\t" + "Min:   " + NumToString.number(Min) + "\n";
            out += "\t" + "Max:   " + NumToString.number(Max) + "\n";
            out += "\t" + "Total: " + NumToString.number(Total) + "\n";
            out += "\t" + "Mean:  " + NumToString.number(getMean()) + "\n";
            return out;
        }
    }
    private final Exposure mExposure = new Exposure();

    // For now, monitor performance (TODO: remove in the future)
    private abstract static class StopWatches {
        final static StopWatch ProgressedNotification = new StopWatch("captureMonitor.progressedNotification()");
        final static StopWatch CompletedNotification  = new StopWatch("captureMonitor.completedNotification()");
        final static StopWatch OnCaptureProgressed    = new StopWatch("captureMonitor.onCaptureProgressed()");
        final static StopWatch OnCaptureCompleted     = new StopWatch("captureMonitor.onCaptureCompleted()");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // captureMonitor...............................................................................
    /**
     * Effectively disabled
     */
    private CaptureMonitor() {
        super();
    }

    // captureMonitor...............................................................................
    /**
     * Set parameters for ending capture
     * @param frameLimit Maximum number of frames to capture before stopping
     * @param temperatureLimit Maximum temperature before stopping
     */
    CaptureMonitor(int frameLimit, double temperatureLimit) {
        this();
        mState = State.ACTIVE;
        mFrame.setLimit(frameLimit);
        mTemperature.setLimit(temperatureLimit);
        Log.e(Thread.currentThread().getName(), "Capture Frame Limit: " + NumToString.number(frameLimit)
                + ", Capture Temperature Limit: " + NumToString.number(temperatureLimit) + " [Celsius]");
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // completedNotification........................................................................
    /**
     * Displays information about a completed capture
     * @param completedResult Completed capture result
     */
    private void completedNotification(@NonNull TotalCaptureResult completedResult) {
        StopWatches.CompletedNotification.start();

        Log.e(Thread.currentThread().getName(), "Capture completed with time-code: " + TimeCode.toString(mTimestamp.Last));

        Long duration = completedResult.get(CaptureResult.SENSOR_FRAME_DURATION);
        if (duration == null) {
            Log.e(Thread.currentThread().getName(), "Frame duration time is not available, cannot compute FPS/Duty/Dead time");
        }
        else {
            double duty     = 100. * mExposure.Last / (double) duration;
            long   deadTime = mTimestamp.Elapsed - duration;
            mDeadtime.add(deadTime);
            Log.e(Thread.currentThread().getName(), "Frame FPS: " + NumToString.decimal(1. / (duration * 1e-9))
                    + ", Frame Exposure: " + Long.toString(mExposure.Last) + " [ns]"
                    + ", Frame Duty: " + NumToString.decimal(duty) + "%"
                    + ", Frame Dead time: " + NumToString.number(deadTime) + " [ns]");
        }

        String tempString = mTemperature.getLastString();
        if (tempString == null) {
            tempString = "UNAVAILABLE";
        }
        else {
            tempString += " [Celsius]";
        }

        Double power = BatteryController.getInstantaneousPower();
        String powerString;
        if (power == null) {
            powerString = "UNAVAILABLE";
        }
        else {
            powerString = NumToString.number(power) + " [mW]";
        }

        double fps = 1. / (mTimestamp.Elapsed * 1e-9);
        Log.e(Thread.currentThread().getName(), "Consecutive-frame effective FPS: " + NumToString.decimal(fps)
                + ", Temperature: " + tempString + ", Power: " + powerString);

        StopWatches.CompletedNotification.addTime();
    }

    // Public Overriding Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onCaptureProgressed..........................................................................
    /**
     * This method is called when an image capture makes partial forward progress;
     * some (but not all) results from an image capture are available.
     * @param session Reference to camera capture session
     * @param request Reference to capture request
     * @param partialResult Reference to the partial capture result
     */
    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
        StopWatches.OnCaptureProgressed.start();

        super.onCaptureProgressed(session, request, partialResult);

        HeapMemory.logAvailableMiB();
        Log.e(Thread.currentThread().getName(), "Capture in progress..");

        if (HeapMemory.isMemoryLow()) {
            Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>>DANGER LOW MEMORY<<\t\t>>REQUESTING PAUSE<<\n ");
            mState = State.PAUSED;
            CaptureController.pauseSession();
        }

        StopWatches.OnCaptureProgressed.addTime();
    }

    // onCaptureCompleted...........................................................................
    /**
     * This method is called when an image capture has fully completed and all the result
     * metadata is available.
     * @param session Reference to camera capture session
     * @param request Reference to capture request
     * @param result Reference to completed capture result (capture metadata)
     */
    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
        StopWatches.OnCaptureCompleted.start();

        super.onCaptureCompleted(session, request, result);

        DataQueue.add(result);
        mTimestamp.add(result);
        mExposure.add(result);
        mTemperature.logTemperature();
        mFrame.raiseFrameCount();

        completedNotification(result);

        StopWatches.OnCaptureCompleted.addTime();
    }

    // onCaptureSequenceCompleted...................................................................
    /**
     * This method is called independently of the others in CaptureCallback, when a capture
     * sequence finishes and all CaptureResult or CaptureFailure for it have been
     * returned via this listener.
     * @param session Reference to camera capture session
     * @param sequenceId Capture sequence ID
     * @param frameNumber Ending frame number
     */
    @Override
    public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                           int sequenceId,
                                           long frameNumber) {
        super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);

        if (mState == State.PAUSED) {
            Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> Capture Stream has Paused <<\n ");
            CaptureController.restartSession();
        }
        else {
            Log.e(Thread.currentThread().getName(), "Capture sequence has completed a total of "
                                            + NumToString.number(mFrame.FrameCount) + " frames");

            // Wait briefly for stragglers to come in
            synchronized (this) {
                try {
                    this.wait(5 * GlobalSettings.DEFAULT_WAIT_MS);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }

            DataQueue.purge();
            synchronized (this) {
                while (!DataQueue.isEmpty() || AnalysisController.isBusy() || StorageMedia.isBusy()) {
                    try {
                        String waitingOn = "";
                        if (!DataQueue.isEmpty()) {
                            waitingOn += " Data Queue is not empty";
                        }
                        if (AnalysisController.isBusy()) {
                            waitingOn += " Analysis Controller is busy";
                        }
                        if (StorageMedia.isBusy()) {
                            waitingOn += " Storage Media is busy";
                        }
                        if (!waitingOn.equals("")) {
                            Log.e(Thread.currentThread().getName(), "Waiting on: " + waitingOn);
                        }

                        if (!DataQueue.isEmpty() && !AnalysisController.isBusy() && !StorageMedia.isBusy()) {
                            Log.e(Thread.currentThread().getName(), ">> Anomalous Situation! Clearing Queues! <<");
                            Log.e(Thread.currentThread().getName(), "*******************************************");
                            DataQueue.logQueueSizes();
                            DataQueue.logQueueContents();
                            DataQueue.clear();
                        }

                        this.wait(GlobalSettings.DEFAULT_WAIT_MS);
                    }
                    catch (InterruptedException e) {
                        // TODO: error
                    }
                }
            }

            if (mState == State.FINISHED) {
                long totalElapsed  = mTimestamp.Last - mTimestamp.First;
                double averageFps  = mFrame.FrameCount / (totalElapsed * 1e-9);
                double averageDuty = mExposure.Total/ (double) totalElapsed;

                Log.e(Thread.currentThread().getName(), mExposure.getString());
                Log.e(Thread.currentThread().getName(), mDeadtime.getString());
                Log.e(Thread.currentThread().getName(), mTemperature.getString());
                CaptureController.sessionFinished(averageFps, averageDuty);
            }
            else { // mState == state.ACTIVE
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Something caused this session to end prematurely");
                MasterController.quitSafely();
            }
            // TODO: dump mTotalCaptureResult info
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Not Needed //////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // onCaptureStarted.............................................................................
    /**
     * This method is called when the camera device has started capturing the output image
     * for the request, at the beginning of image exposure, or when the camera device has
     * started processing an input image for a reprocess request.
     * @param session Reference to capture session
     * @param request Reference to capture request
     * @param timestamp Sensor timestamp of capture in progress
     * @param frameNumber Frame number of capture in progress
     */
    @Override
    public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                 @NonNull CaptureRequest request,
                                 long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
    }

    // onCaptureBufferLost..........................................................................
    /**
     * This method is called if a single buffer for a capture could not be sent to its
     * destination surfaces.
     * @param session Reference to capture session
     * @param request Reference to capture request
     * @param target Reference to intended output surface
     * @param frameNumber Frame number of capture in progress
     */
    @Override
    public void onCaptureBufferLost(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull Surface target, long frameNumber) {
        super.onCaptureBufferLost(session, request, target, frameNumber);
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> CAPTURE BUFFER LOST <<"
                + " >> Frame Number: " + NumToString.number(frameNumber) + " <<\n ");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Shutdown Conditions /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // onCaptureFailed..............................................................................
    /**
     * This method is called instead of onCaptureCompleted(CameraCaptureSession, captureRequest,
     * TotalCaptureResult) when the camera device failed to produce a CaptureResult for the request.
     * @param session Reference to capture session
     * @param request Reference to capture request
     * @param failure Reference to failure mode
     */
    @Override
    public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                @NonNull CaptureRequest request,
                                @NonNull CaptureFailure failure) {
        super.onCaptureFailed(session, request, failure);
        Log.e(Thread.currentThread().getName(), ">> Capture Failed <<");

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
        Log.e(Thread.currentThread().getName(), errInfo);

        // TODO: error
        MasterController.quitSafely();
    }

    // onCaptureSequenceAborted.....................................................................
    /**
     * This method is called independently of the others in CaptureCallback, when a capture
     * sequence aborts before any CaptureResult or CaptureFailure before it has been returned
     * via this listener.
     * @param session Reference to capture session
     * @param sequenceId capture sequence ID
     */
    @Override
    public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
        super.onCaptureSequenceAborted(session, sequenceId);
        Log.e(Thread.currentThread().getName(), ">> Capture Sequence Aborted <<");
        MasterController.quitSafely();
    }

}