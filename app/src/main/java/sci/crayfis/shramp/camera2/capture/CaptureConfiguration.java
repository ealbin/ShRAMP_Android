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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Range;


/**
 * Object representing a capture sequence to perform
 */
@TargetApi(21)
public class CaptureConfiguration {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static final Integer DEFAULT_FRAME_LIMIT       = 1000;
    private static final Double  DEFAULT_TEMPERATURE_LIMIT = 40.;
    private static final Integer DEFAULT_ATTEMPT_LIMIT     = 1;

    private static final Long FPS_30 =  33333333L;
    private static final Long FPS_05 = 200000000L;

    private static final Range<Double>  TEMPERATURE_BOUNDS = new Range<>(0., 50.);
    private static final Range<Integer> FRAME_BOUNDS       = new Range<>(0, 2000);
    private static final Range<Integer> ATTEMPT_BOUNDS     = new Range<>(1, 1000);

    // Public Class Constant
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: consider adding fps to the OutputWrapper file header
    public static final Range<Long> EXPOSURE_BOUNDS = new Range<>(FPS_30, FPS_05);

    // Package-Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Mode.........................................................................................
    // capture mode category for this capture sequence
    CaptureController.Mode Mode;

    // TargetExposure...............................................................................
    // Requested sensor exposure (in nanoseconds), depending on the ability of the device the
    // actual exposure used may differ from that requested, if null, CaptureController will attempt
    // to set it to the optimal exposure that minimizes dead time if possible, otherwise it will
    // be set at EXPOSURE_BOUNDS.getLower() * 2, i.e. half the fps of maxium (usually 15 fps)
    Long TargetExposure;

    // FrameLimit...................................................................................
    // Request this number of captured frames before ending the session
    int FrameLimit;

    // TemperatureLimit.............................................................................
    // If this temperature (in Celsius) is exceeded, capture will end
    double TemperatureLimit;

    // AttemptLimit.................................................................................
    // Be it attempts at matching the duty cycle, or just repeating the capture sequence,
    // terminate this sequence once attempt limit is met
    int AttemptLimit;

    // EnableSignificance...........................................................................
    // Only applicable for data sessions, enables computation of pixel value significance
    boolean EnableSignificance;

    // Task.........................................................................................
    // For any odd-ball tasks to be done between capture sessions
    Runnable Task;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // CaptureConfiguration.........................................................................
    /**
     * Disabled
     */
    private CaptureConfiguration() {}

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // newWarmUpSession.............................................................................
    /**
     * Create a new WARMUP session.
     * @param temperatureLimit Maximum temperature to end the session
     * @param attemptLimit (Optional) Attempts to heat up (default is 1)
     * @param frameLimit (Optional) End capture after this many frames (default is 1000)
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newWarmUpSession(double temperatureLimit,
                                                        @Nullable Integer attemptLimit,
                                                        @Nullable Integer frameLimit) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.WARMUP;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getLower();
        instance.FrameLimit         = setFrameLimit(frameLimit);
        instance.TemperatureLimit   = setTemperatureLimit(temperatureLimit);
        instance.AttemptLimit       = setAttemptLimit(attemptLimit);
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newCoolDownSession...........................................................................
    /**
     * Create a new COOLDOWN session
     * @param temperatureLimit Minimum temperature to end the session
     * @param attemptLimit (Optional) Attempts to cool down (default is 1) [minutes]
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newCoolDownSession(double temperatureLimit,
                                                          @Nullable Integer attemptLimit) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.COOLDOWN;

        instance.TargetExposure     = 0L;
        instance.FrameLimit         = 0;
        instance.TemperatureLimit   = setTemperatureLimit(temperatureLimit);
        instance.AttemptLimit       = setAttemptLimit(attemptLimit);
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newColdFastCalibration.......................................................................
    /**
     * Create a new CALIBRATION_COLD_FAST session.
     * Exposure is automatically set to fastest fps, frame limit is the default (1000),
     * temperature limit is 30 Celsius and it is a single attempt.
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newColdFastCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_COLD_FAST;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getLower();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = DEFAULT_TEMPERATURE_LIMIT;
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newColdSlowCalibration.......................................................................
    /**
     * Create a new CALIBRATION_COLD_SLOW session.
     * Exposure is automatically set to slowest fps, frame limit is the default (1000),
     * temperature limit is 30 Celsius and it is a single attempt.
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newColdSlowCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_COLD_SLOW;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getUpper();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = DEFAULT_TEMPERATURE_LIMIT;
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newHotFastCalibration.......................................................................
    /**
     * Create a new CALIBRATION_HOT_FAST session.
     * Exposure is automatically set to fastest fps, frame limit is the default (1000),
     * temperature limit is 50 Celsius and it is a single attempt.
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newHotFastCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_HOT_FAST;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getLower();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = TEMPERATURE_BOUNDS.getUpper();
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newHotSlowCalibration.......................................................................
    /**
     * Create a new CALIBRATION_HOT_SLOW session.
     * Exposure is automatically set to slowest fps, frame limit is the default (1000),
     * temperature limit is 50 Celsius and it is a single attempt.
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newHotSlowCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_HOT_SLOW;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getUpper();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = TEMPERATURE_BOUNDS.getUpper();
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newOptimizationSession.......................................................................
    /**
     * Create a new OPTIMIZE_DUTY_CYCLE session.
     * Discovers sensor exposure / frame rate that maximizes the duty cycle between
     * exposure time and dead time (not possible for devices that do not support manual control)
     * @param temperatureLimit (Optional) Maximum temperature to end the session (default is 40 C)
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newOptimizationSession(@Nullable Double temperatureLimit) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.OPTIMIZE_DUTY_CYCLE;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getLower();
        instance.FrameLimit         = 100;
        instance.TemperatureLimit   = setTemperatureLimit(temperatureLimit);
        instance.AttemptLimit       = 10;
        instance.EnableSignificance = false;
        instance.Task               = null;

        return instance;
    }

    // newDataSession...............................................................................
    /**
     * Create a new DATA session
     * @param frameLimit End capture after this many frames
     * @param targetExposure (Optional) Desired sensor exposure in nanoseconds (default is optimum fps)
     * @param temperatureLimit (Optional) Maximum temperature to end the session (default is 40 C)
     * @param attemptLimit (Optional) Repeat this many times (default is 1)
     * @param enableSignificance (Optional) Enables statistical significance (default is true)
     * @return A capture configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newDataSession(int frameLimit,
                                                      @Nullable Long targetExposure,
                                                      @Nullable Double temperatureLimit,
                                                      @Nullable Integer attemptLimit,
                                                      @Nullable Boolean enableSignificance) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.DATA;

        instance.TargetExposure   = setTargetExposure(targetExposure);
        instance.FrameLimit       = setFrameLimit(frameLimit);
        instance.TemperatureLimit = setTemperatureLimit(temperatureLimit);
        instance.AttemptLimit     = setAttemptLimit(attemptLimit);
        instance.Task             = null;

        if (enableSignificance == null) {
            instance.EnableSignificance = true;
        }
        else {
            instance.EnableSignificance = enableSignificance;
        }

        return instance;
    }

    // newTaskSession...............................................................................
    /**
     * Create a new TASK session
     * @param task A Runnable to perform a task between sessions
     * @return A capture configuration object ready for use
     */
    public static CaptureConfiguration newTaskSession(Runnable task) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.TASK;

        instance.FrameLimit         = 0;
        instance.TargetExposure     = setTargetExposure(null);
        instance.TemperatureLimit   = setTemperatureLimit(null);
        instance.AttemptLimit       = 0;
        instance.EnableSignificance = false;
        instance.Task               = task;

        return instance;
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setTargetExposure............................................................................
    /**
     * Make sure requested targetExposure is within bounds
     * @param targetExposure Optionally null for default setting
     * @return Default is optimized duty fps if available, longest exposure (5 FPS) if not,
     *         otherwise clipped between EXPOSURE_BOUNDS low and high
     */
    @Nullable
    private static Long setTargetExposure(@Nullable Long targetExposure) {
        if (targetExposure == null) {
            return null;
        }

        if (targetExposure > EXPOSURE_BOUNDS.getUpper()) {
            return EXPOSURE_BOUNDS.getUpper();
        }

        if (targetExposure < EXPOSURE_BOUNDS.getLower()) {
            return EXPOSURE_BOUNDS.getLower();
        }

        return targetExposure;
    }

    // setFrameLimit................................................................................
    /**
     * Make sure requested frameLimit is within bounds
     * @param frameLimit Optionally null for default setting
     * @return Default is 1000 frames, otherwise clipped between FRAME_BOUNDS low and high
     */
    private static int setFrameLimit(@Nullable Integer frameLimit) {
        if (frameLimit == null) {
            return DEFAULT_FRAME_LIMIT;
        }

        if (frameLimit > FRAME_BOUNDS.getUpper()) {
            return FRAME_BOUNDS.getUpper();
        }

        if (frameLimit < FRAME_BOUNDS.getLower()) {
            return FRAME_BOUNDS.getLower();
        }

        return frameLimit;
    }

    // setTemperatureLimit..........................................................................
    /**
     * Make sure requested temperatureLimit is within bounds
     * @param temperatureLimit Optionally null for default setting
     * @return Default is 40 Celsius, otherwise clipped between TEMPERATURE_BOUNDS low and high
     */
    private static double setTemperatureLimit(@Nullable Double temperatureLimit) {
        if (temperatureLimit == null) {
            return DEFAULT_TEMPERATURE_LIMIT;
        }

        if (temperatureLimit > TEMPERATURE_BOUNDS.getUpper()) {
            return TEMPERATURE_BOUNDS.getUpper();
        }

        if (temperatureLimit < TEMPERATURE_BOUNDS.getLower()) {
            return TEMPERATURE_BOUNDS.getLower();
        }

        return temperatureLimit;
    }

    // setAttemptLimit..............................................................................
    /**
     * Make sure requested attemptLimit is within bounds
     * @param attemptLimit Optionally null for default setting
     * @return Default is 1 attempt, otherwise clipped between ATTEMPT_BOUNDS low and high
     */
    private static int setAttemptLimit(@Nullable Integer attemptLimit) {
        if (attemptLimit == null) {
            return DEFAULT_ATTEMPT_LIMIT;
        }

        if (attemptLimit > ATTEMPT_BOUNDS.getUpper()) {
            return ATTEMPT_BOUNDS.getUpper();
        }

        if (attemptLimit < ATTEMPT_BOUNDS.getLower()) {
            return ATTEMPT_BOUNDS.getLower();
        }

        return attemptLimit;
    }

}