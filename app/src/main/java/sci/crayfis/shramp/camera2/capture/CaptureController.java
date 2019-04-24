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
 * @updated: 24 April 2019
 */

package sci.crayfis.shramp.camera2.capture;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Range;
import android.view.Surface;

import org.jetbrains.annotations.Contract;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.battery.BatteryController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.requests.RequestMaker;
import sci.crayfis.shramp.camera2.util.Parameter;
import sci.crayfis.shramp.surfaces.SurfaceController;
import sci.crayfis.shramp.util.Datestamp;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * Oversees the set up of captureMonitor sessions and what to do between them
 */
@TargetApi(21)
final public class CaptureController extends CameraCaptureSession.StateCallback {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Mode.........................................................................................
    // Available captureMonitor session modes
    public enum Mode {
        WARMUP,                 // Stress the device to heat it up
        COOLDOWN,               // Idle the device to cool it down
        CALIBRATION_COLD_FAST,  // Perform a calibration run
        CALIBRATION_COLD_SLOW,  // Perform a calibration run
        CALIBRATION_HOT_FAST,   // Perform a calibration run
        CALIBRATION_HOT_SLOW,   // Perform a calibration run
        OPTIMIZE_DUTY_CYCLE,    // Discover fps for optimum duty cycle
        DATA,                   // Perform a data run
        TASK                   // For tasks between runs
    }

    // THREAD_NAME..................................................................................
    // Control over captureMonitor and its internal actions run on this thread
    private static final String THREAD_NAME = "CaptureThread";

    // mHandler.....................................................................................
    // Reference to the CaptureManagerThread Handler
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                GlobalSettings.CAPTURE_MANAGER_THREAD_PRIORITY);

    // mInstance....................................................................................
    // Reference to single instance of CaptureController
    private static final CaptureController mInstance = new CaptureController();

    // mFlightPlan..................................................................................
    // Capture sequence to execute
    private static final GlobalSettings.FlightPlan mFlightPlan = new GlobalSettings.FlightPlan();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mOptimalExposure.............................................................................
    // Exposure time for minimal dead time in capture
    private static Long mOptimalExposure;

    // mSession.....................................................................................
    // Encapsulation of captureMonitor session objects and group actions on them
    abstract private static class mSession {

        // Current state of the captureMonitor session
        enum State {OPEN, RUNNING, PAUSED, CLOSED};

        // captureMonitor session objects
        static CaptureConfiguration configuration;  // conditions to end captureMonitor
        static CameraCaptureSession captureSession; // the actual session
        static CaptureRequest       captureRequest; // the session request parameters
        static List<Surface>        surfaceList;    // output surfaces
        static CaptureMonitor       captureMonitor; // frame-wise capture callback
        static State                state;          // current state of captureMonitor session
        static int                  attemptCount;   // attempts so far for the same configuration

        // reset....................................................................................
        /**
         * Clear all fields, close any open session and reload output surface list
         */
        static void reset() {
            configuration = null;

            if (captureSession != null) {
                captureSession.close();
            }
            captureSession = null;
            state = State.CLOSED;

            captureRequest = null;
            surfaceList = SurfaceController.getOpenSurfaces();

            captureMonitor = null;
        }

        // newSession...............................................................................
        /**
         * Opens a new session (builds capture request, etc), but does not begin it
         * @param session bla
         */
        static void newSession(@NonNull CameraCaptureSession session) {
            captureSession = session;
            renewSession();
        }

        // renewSession.............................................................................
        /**
         * Reset capture request and configure a new capture monitor for the next capture session
         */
        static void renewSession() {

            // Get next programmed capture session
            configuration = mFlightPlan.getNext();
            attemptCount  = 0;

            // Quit the app successfully condition
            if (configuration == null) {
                Log.e(Thread.currentThread().getName(), " \n\n\t\t\tMission Accomplished.  Shutting down..\n ");
                reset();
                MasterController.quitSafely();
                return;
            }

            switch (configuration.Mode) {
                case COOLDOWN: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING COOL-DOWN SESSION <<\n ");
                    break;
                }
                case WARMUP: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING WARM-UP SESSION <<\n ");
                    break;
                }
                case CALIBRATION_COLD_FAST: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING COLD-FAST CALIBRATION SESSION <<\n ");
                    break;
                }
                case CALIBRATION_COLD_SLOW: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING COLD-SLOW CALIBRATION SESSION <<\n ");
                    break;
                }
                case CALIBRATION_HOT_FAST: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING HOT-FAST CALIBRATION SESSION <<\n ");
                    break;
                }
                case CALIBRATION_HOT_SLOW: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING HOT-SLOW CALIBRATION SESSION <<\n ");
                    break;
                }
                case OPTIMIZE_DUTY_CYCLE: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING EXPOSURE OPTIMIZATION SESSION <<\n ");
                    break;
                }
                case DATA: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING DATA SESSION <<\n ");
                    break;
                }
                case TASK: {
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t >> STARTING TASK SESSION <<\n ");
                    break;
                }
            }

            if (configuration.Mode == Mode.COOLDOWN) {
                coolDown(configuration.TemperatureLimit, configuration.AttemptLimit);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        renewSession();
                    }
                });
            }
            else if (configuration.Mode == Mode.TASK) {
                mHandler.post(configuration.Task);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        renewSession();
                    }
                });
            }
            else {
                AnalysisController.resetRunningTotals();
                if (configuration.EnableSignificance) {
                    AnalysisController.enableSignificance();
                    AnalysisController.setSignificanceThreshold(configuration.FrameLimit);
                }
                else {
                    AnalysisController.disableSignificance();
                }
                if (configuration.Mode == Mode.DATA && configuration.TargetExposure == null) {
                    if (mOptimalExposure == null) {
                        configuration.TargetExposure = CaptureConfiguration.EXPOSURE_BOUNDS.getLower() * 2;
                    }
                    else {
                        configuration.TargetExposure = mOptimalExposure;
                    }
                }
                captureRequest = buildCaptureRequest();
                captureMonitor = new CaptureMonitor(configuration.FrameLimit, configuration.TemperatureLimit);
                state = State.OPEN;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startCapture();
                    }
                });
            }
        }

        // repeatSession............................................................................
        /**
         * Repeat last capture session
         */
        static void repeatSession() {
            AnalysisController.resetRunningTotals();
            if (configuration.EnableSignificance) {
                AnalysisController.enableSignificance();
                AnalysisController.setSignificanceThreshold(configuration.FrameLimit);
            }
            else {
                AnalysisController.disableSignificance();
            }
            captureMonitor = new CaptureMonitor(configuration.FrameLimit, configuration.TemperatureLimit);
            state = State.OPEN;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startCapture();
                }
            });
        }

        // startCapture.............................................................................
        /**
         * Repeatedly tries to kick-off a capture session until it finally goes through
         */
        static void startCapture() {
            synchronized (mInstance) {
                while (!hasStarted()) {
                    try {
                        Log.e(Thread.currentThread().getName(), "Waiting to start capture session");
                        mInstance.wait(GlobalSettings.DEFAULT_WAIT_MS);
                    }
                    catch (InterruptedException e) {
                        // TODO: error
                    }
                }
            }
            Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> STARTING CAPTURE <<\n ");
        }

        // hasStarted...............................................................................
        /**
         * Attempts to send a repeating capture request if there is sufficient memory and all
         * other app jobs are idling
         * @return True if capture has started, false if conditions were not right to start yet
         */
        static boolean hasStarted() {

            if (state == State.RUNNING) {
                return true;
            }

            if (state == State.CLOSED) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Session cannot be closed");
                MasterController.quitSafely();
                return true;
            }

            if (state == State.OPEN || state == State.PAUSED) {
                HeapMemory.logAvailableMiB();
                if (!HeapMemory.isMemoryAmple()) {
                    DataQueue.purge();
                    System.gc();
                    if (!DataQueue.isEmpty() || AnalysisController.isBusy() || StorageMedia.isBusy()) {
                        return false;
                    }

                    // Sometimes the garbage collector just needs a kick
                    Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> Forcing Restart <<\n ");
                }

                try {
                    captureSession.setRepeatingRequest(captureRequest, captureMonitor, mHandler);
                    state = State.RUNNING;
                    return true;
                }
                catch (CameraAccessException e) {
                    // TODO: handle this
                    Log.e(Thread.currentThread().getName(), "Cannot access camera");
                    MasterController.quitSafely();
                    return true;
                }
            }

            // Should never get to this point, silence compiler error for lack of return
            Log.e(Thread.currentThread().getName(), "Something is really wrong, unknown capture state?");
            MasterController.quitSafely();
            return true;
        }

        // pause....................................................................................
        /**
         * Pause the capture session
         */
        static void pause() {
            if (state == State.RUNNING) {
                try {
                    captureSession.stopRepeating();
                    state = State.PAUSED;
                }
                catch (CameraAccessException e) {
                    // TODO:  error
                    Log.e(Thread.currentThread().getName(), "Cannot access camera");
                    MasterController.quitSafely();
                }
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CaptureController...............................................................................
    /**
     * Disabled
     */
    private CaptureController() { super(); }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // startCaptureSequence.........................................................................
    /**
     * Opens a new capture session with the opened camera.  This happens asynchronously, but when
     * opened, execution continues in onConfigured()
     */
    public static void startCaptureSequence() {
        mSession.reset();

        // execution continues in onConfigured
        CameraController.createCaptureSession(mSession.surfaceList, mInstance, mHandler);
    }

    // isOptimalExposureSet.........................................................................
    /**
     * @return True if optimal exposure is known, false if not
     */
    @Contract(pure = true)
    public static boolean isOptimalExposureSet() { return mOptimalExposure != null; }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onConfigured.................................................................................
    /**
     * This method is called when the camera device has finished configuring itself,
     * and the session can start processing capture requests.
     * @param session Reference to the now opened capture session
     */
    @Override
    public void onConfigured(@NonNull CameraCaptureSession session) {
        //super.onConfigured(session); is abstract, nothing to call
        Log.e(Thread.currentThread().getName(), "Capture session is now open for business");
        mSession.newSession(session);
    }

    // onClosed.....................................................................................
    /**
     * This method is called when the session is closed.
     * @param session Reference to capture session
     */
    @Override
    public void onClosed(@NonNull CameraCaptureSession session) {
        super.onClosed(session);
        Log.e(Thread.currentThread().getName(), "Capture session has been closed");
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // coolDown.....................................................................................
    /**
     * Idle the smartphone with minimal activity to decrease device temperature
     * @param coolTemperature Temperature to cool to [Celsius]
     * @param attemptLimit Maximum idle attempts (minutes) to cool
     */
    static void coolDown(double coolTemperature, int attemptLimit) {
        synchronized (mInstance) {
            Double temperature = BatteryController.getCurrentTemperature();
            if (temperature == null) {
                Log.e(Thread.currentThread().getName(), "Temperature is unknown, shutting down for safety");
                MasterController.quitSafely();
                return;
            }

            int attemptCount = 0;
            while (temperature > coolTemperature) {
                try {
                    Log.e(Thread.currentThread().getName(), "Cooling down: " + NumToString.number(temperature)
                            + " > " + NumToString.number(coolTemperature) + " [Celsius], update in 1 minute..");
                    mInstance.wait(GlobalSettings.DEFAULT_LONG_WAIT);

                    temperature = BatteryController.getCurrentTemperature();
                    if (temperature == null) {
                        Log.e(Thread.currentThread().getName(), "Temperature is unknown, shutting down for safety");
                        MasterController.quitSafely();
                        return;
                    }

                    attemptCount += 1;
                    if (attemptCount >= mSession.configuration.AttemptLimit) {
                        Log.e(Thread.currentThread().getName(), "Cool down cycle exceeding attempt limit: "
                                + NumToString.number(attemptCount) + ", breaking from cool down");
                        Log.e(Thread.currentThread().getName(), "Ending temperature: " + NumToString.number(temperature)
                                + " [Celsius]");
                        break;
                    }
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }
        }
    }

    // pauseSession.................................................................................
    /**
     * Pause the current capture session
     */
    static void pauseSession() {
        mSession.pause();
    }

    // restartSession...............................................................................
    /**
     * Restart a paused capture session
     */
    static void restartSession() {
        mSession.startCapture();
    }

    // getOptimalExposure...........................................................................
    /**
     * @return Optimal exposure for minimal dead time, null if optimize duty cycle session has not been run
     */
    @Nullable
    @Contract(pure = true)
    static Long getOptimalExposure() {
        return mOptimalExposure;
    }

    // sessionFinished..............................................................................
    /**
     * Called by CaptureMonitor when the session has finished.
     * @param averageFps Overall average frames-per-second (i.e. total frames / total session time)
     * @param averageDuty Overall average duty (i.e. total exposure / total frame duration)
     */
    static void sessionFinished(double averageFps, double averageDuty) {

        mSession.attemptCount += 1;

        String string = " \n\nCapture session has finished\n\n";
        string += "Session effective performance: \n";
        string += "\t Overall Average FPS:  " + NumToString.decimal(averageFps) + " [frames / sec] \n";
        string += "\t Overall Average Duty: " + NumToString.decimal(averageDuty * 100.) + " % \n";
        string += "\t Attempt count:        " + NumToString.number(mSession.attemptCount)
                + " out of " + NumToString.number(mSession.configuration.AttemptLimit) + "\n";
        Log.e(Thread.currentThread().getName(), string);

        if (mSession.configuration.Mode == Mode.OPTIMIZE_DUTY_CYCLE) {
            mOptimalExposure = (long) (Math.floor(1e9 / averageFps));
            Log.e(Thread.currentThread().getName(), "New optimal fps: "
                    + NumToString.decimal(1. / ( mOptimalExposure * 1e-9) )
                    + " [frames / sec]");
            mSession.configuration.TargetExposure = mOptimalExposure;
            mSession.captureRequest = buildCaptureRequest();

            Integer mode = mSession.captureRequest.get(CaptureRequest.CONTROL_AE_MODE);
            if (mode == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
                MasterController.quitSafely();
                return;
            }

            if ( (averageDuty >= GlobalSettings.OPTIMAL_DUTY_THRESHOLD)
                    || (mode == CameraMetadata.CONTROL_AE_MODE_ON && mSession.attemptCount > 3)) {
                Log.e(Thread.currentThread().getName(), " \n\n\t\t>> Ending Attempts Early, Goals Met <<\n ");
                Log.e(Thread.currentThread().getName(), " \n" + StopWatch.getLabeledPerformances());
                StopWatch.resetLabeled();
                mSession.renewSession();
                return;
            }
        }

        if (mSession.configuration.Mode == Mode.WARMUP) {
            Double currentTemperature = BatteryController.getCurrentTemperature();
            if (currentTemperature == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Cannot get temperature, shutting down for safety");
                MasterController.quitSafely();
                return;
            }

            if (currentTemperature >= mSession.configuration.TemperatureLimit) {
                Log.e(Thread.currentThread().getName(), " \n\n\t\t>> Ending Attempts Early, Goals Met <<\n ");
                Log.e(Thread.currentThread().getName(), " \n" + StopWatch.getLabeledPerformances());
                StopWatch.resetLabeled();
                mSession.renewSession();
                return;
            }
        }

        if (mSession.configuration.Mode == Mode.DATA
                && mSession.attemptCount < mSession.configuration.AttemptLimit) {
            Double currentTemperature = BatteryController.getCurrentTemperature();
            if (currentTemperature == null) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Cannot get temperature, shutting down for safety");
                MasterController.quitSafely();
                return;
            }

            if (currentTemperature >= mSession.configuration.TemperatureLimit) {
                Log.e(Thread.currentThread().getName(), " \n\n\t\t>> Over Temperature, Cooling Down <<\n ");
                Log.e(Thread.currentThread().getName(), " \n" + StopWatch.getLabeledPerformances());
                StopWatch.resetLabeled();
                coolDown(GlobalSettings.TEMPERATURE_GOAL, 10);
                Log.e(Thread.currentThread().getName(), " \n\n\t\t>> Reducing FPS by 80% To Avoid Over Temperature <<\n ");
                mSession.configuration.TargetExposure = (long) Math.round(mSession.configuration.TargetExposure / 0.8);
                mSession.captureRequest = buildCaptureRequest();
                mSession.repeatSession();
                return;
            }
        }

        if (mSession.attemptCount < mSession.configuration.AttemptLimit) {
            Log.e(Thread.currentThread().getName(), " \n" + StopWatch.getLabeledPerformances());
            StopWatch.resetLabeled();
            mSession.repeatSession();
            return;
        }

        if (mSession.configuration.Mode == Mode.CALIBRATION_HOT_SLOW) {
            AnalysisController.runStatistics("hot_slow_" + Datestamp.getDate());
            // PrintAllocations.printMeanAndErr();
        }
        if (mSession.configuration.Mode == Mode.CALIBRATION_HOT_FAST) {
            AnalysisController.runStatistics("hot_fast_" + Datestamp.getDate());
            // PrintAllocations.printMeanAndErr();
        }
        if (mSession.configuration.Mode == Mode.CALIBRATION_COLD_SLOW) {
            AnalysisController.runStatistics("cold_slow_" + Datestamp.getDate());
            // PrintAllocations.printMeanAndErr();
        }
        if (mSession.configuration.Mode == Mode.CALIBRATION_COLD_FAST) {
            AnalysisController.runStatistics("cold_fast_" + Datestamp.getDate());
            // PrintAllocations.printMeanAndErr();
        }

        Log.e(Thread.currentThread().getName(), " \n" + StopWatch.getLabeledPerformances());
        StopWatch.resetLabeled();
        mSession.renewSession();

    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // buildCaptureRequest..........................................................................
    /**
     * @return A new capture request for the session (the only time it will be null is a critical failure)
     */
    @Nullable
    private static CaptureRequest buildCaptureRequest() {

        RequestMaker.makeDefault();
        CaptureRequest.Builder builder = CameraController.getCaptureRequestBuilder();
        if (builder == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Request builder cannot be null");
            MasterController.quitSafely();
            return null;
        }

        for (Surface surface : mSession.surfaceList) {
            builder.addTarget(surface);
        }

        Integer mode = builder.get(CaptureRequest.CONTROL_AE_MODE);
        if (mode == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "AE mode cannot be null");
            MasterController.quitSafely();
            return null;
        }

        if (mode == CameraMetadata.CONTROL_AE_MODE_ON) {
            Log.e(Thread.currentThread().getName(), "Cannot set exact exposure, finding closest option");
            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getAeTargetFpsRange());
        }
        else {
            builder.set(CaptureRequest.SENSOR_FRAME_DURATION, mSession.configuration.TargetExposure);
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,  mSession.configuration.TargetExposure);
        }
        CameraController.setCaptureRequestBuilder(builder);
        CameraController.writeFPS();

        return builder.build();
    }

    // getAeTargetFpsRange..........................................................................
    /**
     * When sensor cannot be manually controlled, find an fps-range closest to that desired
     */
    @SuppressWarnings("unchecked")
    @NonNull
    private static Range<Integer> getAeTargetFpsRange() {

        // Set FPS range closest to target FPS
        LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap;
        characteristicsMap = CameraController.getOpenedCharacteristicsMap();
        if (characteristicsMap == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Characteristics map cannot be null");
            MasterController.quitSafely();
            return new Range<Integer>(0, 0);  // garbage
        }

        CameraCharacteristics.Key<Range<Integer>[]> cKey;
        Parameter<Range<Integer>[]> property;

        cKey = CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
        property = characteristicsMap.get(cKey);
        if (property == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Available target FPS ranges cannot be null");
            MasterController.quitSafely();
            return new Range<Integer>(0,0);  // garbage
        }

        Range<Integer>[] ranges = property.getValue();
        if (ranges == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "FPS ranges cannot be null");
            MasterController.quitSafely();
            return new Range<Integer>(0,0);  // garbage
        }

        int target = (int) Math.round(1e9 / mSession.configuration.TargetExposure);
        Range<Integer> closest = null;
        for (Range<Integer> range : ranges) {
            if (closest == null) {
                closest = range;
                continue;
            }

            int diff = Math.min(Math.abs(range.getUpper() - target),
                                Math.abs(range.getLower() - target));

            int closestDiff = Math.min(Math.abs(closest.getUpper() - target),
                                       Math.abs(closest.getLower() - target));

            if (diff < closestDiff) {
                closest = range;
            }
        }
        if (closest == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Closest FPS range cannot be null");
            MasterController.quitSafely();
            return new Range<Integer>(0,0);  // garbage
        }
        return closest;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // IGNORE //////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // onReady......................................................................................
    /**
     * This method is called every time the session has no more capture requests to process.
     * @param session Reference to capture session
     */
    @Override
    public void onReady(@NonNull CameraCaptureSession session) {
        super.onReady(session);
        Log.e(Thread.currentThread().getName(), "Capture session ready");
    }

    // onActive.....................................................................................
    /**
     * This method is called when the session starts actively processing captureMonitor requests.
     * @param session Reference to capture session
     */
    @Override
    public void onActive(@NonNull CameraCaptureSession session) {
        super.onActive(session);
        Log.e(Thread.currentThread().getName(), "Capture session active");
    }

    // onCaptureQueueEmpty..........................................................................
    /**
     * This method is called when camera device's input captureMonitor queue becomes empty,
     * and is ready to accept the next request.
     * @param session Reference to capture session
     */
    @Override
    public void onCaptureQueueEmpty(@NonNull CameraCaptureSession session) {
        super.onCaptureQueueEmpty(session);
        Log.e(Thread.currentThread().getName(), "Capture queue is empty");
    }

    // onSurfacePrepared............................................................................
    /**
     * This method is called when the buffer pre-allocation for an output Surface is complete.
     * @param session Reference to capture session
     * @param surface Reference to output surface
     */
    @Override
    public void onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface) {
        super.onSurfacePrepared(session, surface);
        Log.e(Thread.currentThread().getName(), "Output surface: " + surface.toString() + " is ready");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SHUTDOWN ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // onConfiguredFailed...........................................................................
    /**
     * This method is called if the session cannot be configured as requested.
     * @param session Reference to capture session
     */
    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        //super.onConfigureFailed(session); is abstract
        // TODO: error
        Log.e(Thread.currentThread().getName(), "Capture configuration failed");
        MasterController.quitSafely();
    }

}