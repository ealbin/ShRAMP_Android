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
 * @updated: 3 May 2019
 */

package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.camera2.capture.CaptureConfiguration;
import sci.crayfis.shramp.camera2.capture.CaptureController;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * The device will run the operations listed in FlightPlan()
 */
@TargetApi(21)
public final class FlightPlan {

    // TODO: in the future, this will be a state machine
    private static final List<CaptureConfiguration> mFlightPlan = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::>>    EDIT FlightPlan()   <<::::::::::::::::::::::::::::::::::::::
    //vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    /**
     * The device will run the operations listed.
     * e.g. mFlightPlan.add( CaptureConfiguration.newXXX() )
     *      where XXX can be "CoolDownSession", "WarmUpSession", "DataSession", etc...
     *      See CaptureConfiguration for what's available
     */
    public FlightPlan() {

        // Example cycle - turn off by setting if(false)
        if (true) {
            // Calibrate if needed (if mean/stddev/mask files cannot be found)
            if (AnalysisController.needsCalibration()) {
                addCalibrationCycle();
            }

            // Optimize FPS if needed (part of the calibration cycle if it's run)
            if (!CaptureController.isOptimalExposureSet()) {
                mFlightPlan.add(CaptureConfiguration.newOptimizationSession(null));
            }

            // Take a data run (see sci.crayfis.shramp.camera2.capture.CaptureConfiguration for more)
            mFlightPlan.add(CaptureConfiguration.newDataSession(1000,
                    null, null, 1, true));
        }

        // TESTING / WORK IN PROGRESS
        //----------------------------
        //mFlightPlan.add(CaptureConfiguration.newColdFastCalibration());
        //mFlightPlan.add(CaptureConfiguration.newColdSlowCalibration());

        //mFlightPlan.add(CaptureConfiguration.newHotFastCalibration());
        //mFlightPlan.add(CaptureConfiguration.newHotSlowCalibration());

        /*
        // Compute mask and import calibration
        Runnable task = new Runnable() {
            @Override
            public void run() {
                AnalysisController.makePixelMask();

                // Wait for writing to finish
                synchronized (this) {
                    while (StorageMedia.isBusy()) {
                        try {
                            Log.e(Thread.currentThread().getName(), "Waiting for writing to finish..");
                            this.wait(5 * GlobalSettings.DEFAULT_WAIT_MS);
                        }
                        catch (InterruptedException e) {
                            // TODO: error
                        }
                    }
                }
                AnalysisController.importLatestCalibration();
            }
        };
        mFlightPlan.add(CaptureConfiguration.newTaskSession(task));
        */
    }
    //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return The next operation to execute
     */
    @Nullable
    public CaptureConfiguration getNext() {
        if (mFlightPlan.size() > 0) {
            return mFlightPlan.remove(0);
        }
        else {
            return null;
        }
    }

    /**
     * A complete calibration cycle typically takes around 30 minutes
     */
    private void addCalibrationCycle() {
        int heatUpTime   = 10; // minutes
        int coolDownTime = 15; // minutes

        double temperature_low = Math.min(GlobalSettings.TEMPERATURE_START, GlobalSettings.TEMPERATURE_GOAL);
        temperature_low = Math.max(GlobalSettings.TEMPERATURE_LOW, temperature_low);

        // Warm up if the phone is too cold
        mFlightPlan.add(CaptureConfiguration.newWarmUpSession(temperature_low, heatUpTime, 1000));

        // Cool down if the phone is too hot
        mFlightPlan.add(CaptureConfiguration.newCoolDownSession(temperature_low, coolDownTime));

        // Calibrate Cold-Fast/Slow
        mFlightPlan.add(CaptureConfiguration.newColdFastCalibration());
        mFlightPlan.add(CaptureConfiguration.newColdSlowCalibration());

        // Warm up to Hot
        mFlightPlan.add(CaptureConfiguration.newWarmUpSession(GlobalSettings.TEMPERATURE_HIGH, heatUpTime, 1000));

        // Calibrate Hot-Fast/Slow
        mFlightPlan.add(CaptureConfiguration.newHotFastCalibration());
        mFlightPlan.add(CaptureConfiguration.newHotSlowCalibration());

        // Cool down to data taking temperature
        mFlightPlan.add(CaptureConfiguration.newCoolDownSession(GlobalSettings.TEMPERATURE_GOAL, coolDownTime));

        // Compute mask and import calibration
        Runnable task = new Runnable() {
            @Override
            public void run() {
                AnalysisController.makePixelMask();

                // Wait for writing to finish
                synchronized (this) {
                    while (StorageMedia.isBusy()) {
                        try {
                            Log.e(Thread.currentThread().getName(), "Waiting for writing to finish..");
                            this.wait(5 * GlobalSettings.DEFAULT_WAIT_MS);
                        }
                        catch (InterruptedException e) {
                            // TODO: error
                        }
                    }
                }
                AnalysisController.importLatestCalibration();
            }
        };
        mFlightPlan.add(CaptureConfiguration.newTaskSession(task));

        // Discover optimal frame rate for data taking
        mFlightPlan.add(CaptureConfiguration.newOptimizationSession(null));
    }

}