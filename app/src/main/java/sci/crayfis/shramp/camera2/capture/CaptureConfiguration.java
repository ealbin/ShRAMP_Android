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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Range;

import org.jetbrains.annotations.Contract;

/**
 * Object representing a captureMonitor sequence to perform
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
    private static final Range<Long>    EXPOSURE_BOUNDS    = new Range<>(FPS_30, FPS_05);
    private static final Range<Double>  DUTY_BOUNDS        = new Range<>(0., 100.);
    private static final Range<Integer> ATTEMPT_BOUNDS     = new Range<>(0, 1000);

    // Package-Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Mode.........................................................................................
    // captureMonitor mode category for this captureMonitor sequence
    CaptureController.Mode Mode;

    // TargetExposure...............................................................................
    // Requested sensor exposure (in nanoseconds), depending on the ability of the device the
    // actual exposure used may differ from that requested
    long TargetExposure;

    // FrameLimit...................................................................................
    // Request this number of captured frames before ending the session
    int FrameLimit;

    // TemperatureLimit.............................................................................
    // If this temperature (in Celsius) is exceeded, captureMonitor will end
    double TemperatureLimit;

    // DutyThreshold................................................................................
    // Automatically adjust sensor exposure / frame rate until an effective duty cycle is met between
    // exposure time and dead time (not possible for devices that do not support manual control)
    Double DutyThreshold;

    // AttemptLimit.................................................................................
    // Be it attempts at matching the duty cycle, or just repeating the captureMonitor sequence,
    // terminate this sequence once attempt limit is met
    int AttemptLimit;

    // EnableSignificance...........................................................................
    // Only applicable for data sessions, enables computation of pixel value significance
    boolean EnableSignificance;

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
     * @param frameLimit (Optional) End captureMonitor after this many frames (default is 1000)
     * @return A captureMonitor configuration object ready for use
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
        instance.DutyThreshold      = null;
        instance.AttemptLimit       = setAttemptLimit(attemptLimit);
        instance.EnableSignificance = false;

        return instance;
    }

    // newCoolDownSession...........................................................................
    /**
     * Create a new COOLDOWN session
     * @param temperatureLimit Minimum temperature to end the session
     * @param attemptLimit (Optional) Attempts to cool down (default is 1) [minutes]
     * @return A captureMonitor configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newCoolDownSession(double temperatureLimit,
                                                          @Nullable Integer attemptLimit) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.COOLDOWN;

        instance.TargetExposure     = 0;
        instance.FrameLimit         = 0;
        instance.TemperatureLimit   = setTemperatureLimit(temperatureLimit);
        instance.DutyThreshold      = null;
        instance.AttemptLimit       = setAttemptLimit(attemptLimit);
        instance.EnableSignificance = false;

        return instance;
    }

    // newColdFastCalibration.......................................................................
    /**
     * Create a new CALIBRATION_COLD_FAST session.
     * Exposure is automatically set to fastest fps, frame limit is the default (1000),
     * temperature limit is 30 Celsius and it is a single attempt.
     * @return A captureMonitor configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newColdFastCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_COLD_FAST;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getLower();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = 30.;
        instance.DutyThreshold      = null;
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;

        return instance;
    }

    // newColdSlowCalibration.......................................................................
    /**
     * Create a new CALIBRATION_COLD_SLOW session.
     * Exposure is automatically set to slowest fps, frame limit is the default (1000),
     * temperature limit is 30 Celsius and it is a single attempt.
     * @return A captureMonitor configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newColdSlowCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_COLD_SLOW;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getUpper();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = 30.;
        instance.DutyThreshold      = null;
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;

        return instance;
    }

    // newHotFastCalibration.......................................................................
    /**
     * Create a new CALIBRATION_HOT_FAST session.
     * Exposure is automatically set to fastest fps, frame limit is the default (1000),
     * temperature limit is 50 Celsius and it is a single attempt.
     * @return A captureMonitor configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newHotFastCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_HOT_FAST;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getLower();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = 50.;
        instance.DutyThreshold      = null;
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;

        return instance;
    }

    // newHotSlowCalibration.......................................................................
    /**
     * Create a new CALIBRATION_HOT_SLOW session.
     * Exposure is automatically set to slowest fps, frame limit is the default (1000),
     * temperature limit is 50 Celsius and it is a single attempt.
     * @return A captureMonitor configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newHotSlowCalibration() {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.CALIBRATION_HOT_SLOW;

        instance.TargetExposure     = EXPOSURE_BOUNDS.getUpper();
        instance.FrameLimit         = DEFAULT_FRAME_LIMIT;
        instance.TemperatureLimit   = 50.;
        instance.DutyThreshold      = null;
        instance.AttemptLimit       = 1;
        instance.EnableSignificance = false;

        return instance;
    }

    // newDataSession...............................................................................
    /**
     * Create a new DATA session
     * @param targetExposure Desired sensor exposure in nanoseconds
     * @param frameLimit End captureMonitor after this many frames
     * @param temperatureLimit (Optional) Maximum temperature to end the session (default is 40 C)
     * @param dutyThreshold (Optional) Automatically adjust targetExposure to meet duty threshold
     * @param attemptLimit (Optional) Repeat this many times (default is 1)
     * @param enableSignificance (Optional) Enables statistical significance (default is true)
     * @return A captureMonitor configuration object ready for use
     */
    @NonNull
    public static CaptureConfiguration newDataSession(long targetExposure, int frameLimit,
                                                      @Nullable Double temperatureLimit,
                                                      @Nullable Double dutyThreshold,
                                                      @Nullable Integer attemptLimit,
                                                      @Nullable Boolean enableSignificance) {
        CaptureConfiguration instance = new CaptureConfiguration();
        instance.Mode = CaptureController.Mode.DATA;

        instance.TargetExposure   = setTargetExposure(targetExposure);
        instance.FrameLimit       = setFrameLimit(frameLimit);
        instance.TemperatureLimit = setTemperatureLimit(temperatureLimit);
        instance.DutyThreshold    = setDutyThreshold(dutyThreshold);
        instance.AttemptLimit     = setAttemptLimit(attemptLimit);

        if (enableSignificance == null) {
            instance.EnableSignificance = true;
        }
        else {
            instance.EnableSignificance = enableSignificance;
        }

        return instance;
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setTargetExposure............................................................................
    /**
     * Make sure requested targetExposure is within bounds
     * @param targetExposure Optionally null for default setting
     * @return Default is longest exposure (5 FPS), otherwise clipped between EXPOSURE_BOUNDS low and high
     */
    private static long setTargetExposure(@Nullable Long targetExposure) {
        if (targetExposure == null) {
            return EXPOSURE_BOUNDS.getUpper();
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

    // setDutyThreshold.............................................................................
    /**
     * Make sure requested dutyThreshold is within bounds
     * @param dutyThreshold Optionally null for default setting
     * @return Default is disabled, otherwise clipped between DUTY_BOUNDS low and high
     */
    @Nullable
    @Contract("null -> null")
    private static Double setDutyThreshold(@Nullable Double dutyThreshold) {
        if (dutyThreshold == null) {
            return null;
        }

        if (dutyThreshold > DUTY_BOUNDS.getUpper()) {
            return DUTY_BOUNDS.getUpper();
        }

        if (dutyThreshold < DUTY_BOUNDS.getLower()) {
            return DUTY_BOUNDS.getLower();
        }

        return dutyThreshold;
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