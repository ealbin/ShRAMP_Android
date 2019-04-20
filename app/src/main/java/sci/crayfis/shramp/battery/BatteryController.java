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

package sci.crayfis.shramp.battery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// This class is basically fine, the to-do is adding additional broadcast listeners (low-priority)

/**
 * Public interface to battery functions
 */
@TargetApi(21)
public class BatteryController {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // Reference to single instance of BatteryController
    private static final BatteryController mInstance = new BatteryController();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBatteryManager..............................................................................
    // Reference to system battery manager
    private BatteryManager mBatteryManager;

    // mBatteryChanged..............................................................................
    // Reference to battery broadcast listener
    private BatteryChanged mBatteryChanged;

    /*
    // TODO: other broadcast listeners that may or may not be added soon
    private static Intent mBatteryOkay;
    private static Intent mBatteryLow;
    private static Intent mPowerConnected;
    private static Intent mPowerDisconnected;
    private static Intent mPowerSummary;
    */

    // mStopWatch1..................................................................................
    // For now, monitoring performance for getting temperature -- (TODO) to be removed later
    private static final StopWatch mStopWatch = new StopWatch("BatteryController.getCurrentTemperature()");

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Disable ability to create multiple instances
     */
    private BatteryController() {}

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // initialize...................................................................................
    /**
     * Start up battery monitoring
     * @param activity Main activity that is controlling the app
     */
    public static void initialize(@NonNull Activity activity) {
        mInstance.mBatteryManager = (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);

        mInstance.mBatteryChanged = new BatteryChanged(activity);

        /*
        // TODO: other broadcast listeners that may or may not be added soon
        mBatteryOkay       = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        mBatteryLow        = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        mPowerConnected    = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        mPowerDisconnected = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        //mPowerSummary      = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_POWER_USAGE_SUMMARY));
        */
    }

    // refresh......................................................................................
    /**
     * Refresh battery information to latest values
     */
    public static void refresh() {
        if (mInstance.mBatteryChanged == null) {
            return;
        }
        mInstance.mBatteryChanged.refresh();
    }

    // getRemainingCapacity.........................................................................
    /**
     * @return remaining battery level as a percent with no decimal part
     */
    public static int getRemainingCapacity() {
        if (mInstance.mBatteryManager == null) {
            return -1;
        }
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    // getBatteryCapacity...........................................................................
    /**
     * Warning: could be garbage
     * @return capacity in milli-amp-hours
     */
    public static double getBatteryCapacity() {
        if (mInstance.mBatteryManager == null) {
            return Double.NaN;
        }
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1e3;
    }

    // getInstantaneousCurrent......................................................................
    /**
     * Warning: could be net current (out - in) or out only
     * @return current current in milli-amps
     */
    public static double getInstantaneousCurrent() {
        if (mInstance.mBatteryManager == null) {
            return Double.NaN;
        }
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1e3;
    }

    // getAverageCurrent............................................................................
    /**
     * Warning: could be garbage
     * @return average current in milli-amps
     */
    public static double getAverageCurrent() {
        if (mInstance.mBatteryManager == null) {
            return Double.NaN;
        }
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) / 1e3;
    }

    // getRemainingTime.............................................................................
    /**
     * Warning: garbage if getAverageCurrent() is garbage
     * @return hours remaining
     */
    public static double getRemainingTime() {
        return getBatteryCapacity() / getAverageCurrent();
    }

    // getRemainingEnergy...........................................................................
    /**
     * Warning: usually garbage
     * @return remaining power in milli-watt-hours
     */
    public static double getRemainingEnergy() {
        if (mInstance.mBatteryManager == null) {
            return Double.NaN;
        }
        return mInstance.mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) / 1e6;
    }

    // getRemainingPower............................................................................
    /**
     * Warning: garbage if either getRemainingEnergy() or getRemainingTime() is garbage,
     * i.e. most likely garbage
     * @return average continuous milli-watts of power remaining
     */
    public static double getRemainingPower() {
        return getRemainingEnergy() / getRemainingTime();
    }

    // getInstantaneousPower........................................................................
    /**
     * Warning: either net power (out - in) or out power
     * @return instantaneous power in milli-watts
     */
    @Nullable
    public static Double getInstantaneousPower() {
        Double voltage = BatteryChanged.getCurrentVoltage();
        if (voltage == null) {
            return null;
        }
        double current = getInstantaneousCurrent();
        return voltage * current;
    }

    // getAveragePower..............................................................................
    /**
     * Warning: garbage if getAverageCurrent() is garbage
     * @return average power in milli-watts
     */
    @Nullable
    public static Double getAveragePower() {
        Double voltage = BatteryChanged.getCurrentVoltage();
        if (voltage == null) {
            return null;
        }
        double current = getAverageCurrent();
        return voltage * current;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getCurrentIcon...............................................................................
    /**
     * TODO: No idea what the hell this is
     * @return it's an integer, that's all I know
     */
    @Contract(pure = true)
    @Nullable
    public static Integer getCurrentIcon() {
        return BatteryChanged.getCurrentIcon();
    }

    // getTechnology................................................................................
    /**
     * Warning: most devices don't have this
     * @return Likely a null string, otherwise it's text describing the technology (e.g. Li-ion)
     */
    @Contract(pure = true)
    @Nullable
    public static String getTechnology() {
        return BatteryChanged.getTechnology();
    }

    // isBatteryPresent.............................................................................
    /**
     * @return True if battery is identified by the system, false if not
     */
    @Contract(pure = true)
    @NonNull
    public static Boolean isBatteryPresent() {
        return BatteryChanged.isBatteryPresent();
    }

    // getCurrentHealth.............................................................................
    /**
     * @return "GOOD", "COLD", "DEAD", "OVERHEAT", "OVER VOLTAGE", "UNKNOWN", "UNSPECIFIED FAILURE",
     *          "UNKNOWN CONDITION OR NOT AVAILABLE"
     */
    @Contract(pure = true)
    @Nullable
    public static String getCurrentHealth() {
        return BatteryChanged.getCurrentHealth();
    }

    // getCurrentStatus.............................................................................
    /**
     * @return "CHARGING", "DISCHARGING", "FULLY CHARGED", "NOT CHARGING", "CHARGING STATUS UNKNOWN",
     *          "UNKNOWN STATUS"
     */
    @Contract(pure = true)
    @Nullable
    public static String getCurrentStatus() {
        return BatteryChanged.getCurrentStatus();
    }

    // getCurrentPowerSource........................................................................
    /**
     * @return "USING BATTERY POWER ONLY", "USING AC ADAPTER POWER", "USING USB POWER",
     *         "USING WIRELESS POWER", "UNKNOWN POWER SOURCE"
     */
    @Contract(pure = true)
    @Nullable
    public static String getCurrentPowerSource() {
        return BatteryChanged.getCurrentPowerSource();
    }

    // getCurrentVoltage............................................................................
    /**
     * @return Battery voltage in volts
     */
    @Contract(pure = true)
    @Nullable
    public static Double getCurrentVoltage() {
        return BatteryChanged.getCurrentVoltage();
    }

    // mBatteryTemperature..........................................................................
    /**
     * @return Battery temperature in degrees Celsius
     */
    @Contract(pure = true)
    @Nullable
    public static Double getCurrentTemperature() {
        mStopWatch.start();
        Double temperature = BatteryChanged.getCurrentTemperature();
        mStopWatch.addTime();
        return temperature;
    }

    // getCurrentLevel..............................................................................
    /**
     * @return Usually the same as getRemainingCapacity(), but could be energy or charge units
     */
    @Contract(pure = true)
    @Nullable
    public static Integer getCurrentLevel() {
        return BatteryChanged.getCurrentLevel();
    }

    // getScale.....................................................................................
    /**
     * @return Maximal value of getCurrentLevel(), usually 100 as in percent, but could be
     *         energy or charge or something..
     */
    @Contract(pure = true)
    @Nullable
    public static Integer getScale() {
        return BatteryChanged.getScale();
    }

    // getCurrentPercent............................................................................
    /**
     * @return Same as getRemainingCapacity(), but possibly (not often) higher precision
     */
    @Contract(pure = true)
    @Nullable
    public static Double getCurrentPercent() { return BatteryChanged.getCurrentPercent(); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getString....................................................................................
    /**
     * @return Status string of current battery conditions
     */
    @NonNull
    public static String getString() {
        refresh();

        String out = " \n";
        out += "\t" + "Battery charge level:       " + NumToString.number(getRemainingCapacity())     + "%\n";
        out += "\t" + "Battery capacity:           " + NumToString.number(getBatteryCapacity())       + " [mA hr]\n";
        out += "\t" + "Instantaneous current:      " + NumToString.number(getInstantaneousCurrent()) + " [mA]\n";
        out += "\t" + "Average current:            " + NumToString.number(getAverageCurrent())       + " [mA]\n";
        out += "\t" + "Time until drained:         " + NumToString.number(getRemainingTime())        + " [hr]\n";
        out += "\t" + "Remaining energy:           " + NumToString.number(getRemainingEnergy())      + " [mW hr]\n";
        out += "\t" + "Remaining continuous power: " + NumToString.number(getRemainingPower())       + " [mW]\n";

        Double power = getInstantaneousPower();
        String powerString;
        if (power == null) {
            powerString = "UNKNOWN\n";
        }
        else {
            powerString = NumToString.number(power) + " [mW]\n";
        }
        out += "\t" + "Instantaneous power:        " + powerString;

        power = getAveragePower();
        if (power == null) {
            powerString = "UNKNOWN\n";
        }
        else {
            powerString = NumToString.number(power) + " [mW]\n";
        }
        out += "\t" + "Average power:              " + powerString;

        out += "\n";

        out += mInstance.mBatteryChanged.getString();

        return out;
    }

    // shutdown.....................................................................................
    /**
     * Disable battery broadcast listening
     */
    public static void shutdown() {
        mInstance.mBatteryChanged.shutdown();
    }

}