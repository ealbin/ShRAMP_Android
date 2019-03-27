package sci.crayfis.shramp.battery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public class BatteryController {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final BatteryController mInstance = new BatteryController();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBatteryManager..............................................................................
    // TODO: description
    private BatteryManager mBatteryManager;

    // mBatteryChanged..............................................................................
    // TODO: description
    private BatteryChanged mBatteryChanged;

    /*
    private static Intent mBatteryOkay;
    private static Intent mBatteryLow;
    private static Intent mPowerConnected;
    private static Intent mPowerDisconnected;
    private static Intent mPowerSummary;
    */

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private BatteryController() {}

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // initialize...................................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     */
    public static void initialize(@NonNull Activity activity) {
        mInstance.mBatteryManager = (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);

        mInstance.mBatteryChanged = new BatteryChanged(activity);

        /*
        mBatteryOkay       = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        mBatteryLow        = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        mPowerConnected    = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        mPowerDisconnected = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        //mPowerSummary      = activity.registerReceiver(this, new IntentFilter(Intent.ACTION_POWER_USAGE_SUMMARY));
        */
    }

    // refresh......................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void refresh() {
        assert mInstance.mBatteryChanged != null;
        mInstance.mBatteryChanged.refresh();
    }

    // getRemainingCapacity.........................................................................
    /**
     * TODO: description, comments and logging
     * @return as a percent with no decimal part
     */
    public static int getRemainingCapacity() {
        assert mInstance.mBatteryManager != null;
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    // getBatteryCapacity...........................................................................
    /**
     * TODO: description, comments and logging
     * @return capacity in milli-amp-hours
     */
    public static double getBatteryCapacity() {
        assert mInstance.mBatteryManager != null;
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1e3;
    }

    // getInstantaneousCurrent......................................................................
    /**
     * TODO: description, comments and logging
     * @return current current in milli-amps
     */
    public static double getInstantaneousCurrent() {
        assert mInstance.mBatteryManager != null;
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1e3;
    }

    // getAverageCurrent............................................................................
    /**
     * TODO: description, comments and logging
     * @return average current in milli-amps
     */
    public static double getAverageCurrent() {
        assert mInstance.mBatteryManager != null;
        return mInstance.mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) / 1e3;
    }

    // getRemainingTime.............................................................................
    /**
     * TODO: description, comments and logging
     * @return hours remaining
     */
    public static double getRemainingTime() {
        return getBatteryCapacity() / getAverageCurrent();
    }

    // getRemainingEnergy...........................................................................
    /**
     * TODO: description, comments and logging
     * @return remaining power in milli-watt-hours
     */
    public static double getRemainingEnergy() {
        assert mInstance.mBatteryManager != null;
        return mInstance.mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) / 1e6;
    }

    // getRemainingPower............................................................................
    /**
     * TODO: description, comments and logging
     * @return average continuous milli-watts of power remaining
     */
    public static double getRemainingPower() {
        return getRemainingEnergy() / getRemainingTime();
    }

    // getInstantaneousPower........................................................................
    /**
     * TODO: description, comments and logging
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
     * TODO: description, comments and logging
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
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static Integer getCurrentIcon() {
        return BatteryChanged.getCurrentIcon();
    }

    // getTechnology................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static String getTechnology() {
        return BatteryChanged.getTechnology();
    }

    // isBatteryPresent.............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    public static Boolean isBatteryPresent() {
        return BatteryChanged.isBatteryPresent();
    }

    // getCurrentHealth.............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static String getCurrentHealth() {
        return BatteryChanged.getCurrentHealth();
    }

    // getCurrentStatus.............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static String getCurrentStatus() {
        return BatteryChanged.getCurrentStatus();
    }

    // getCurrentPowerSource........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static String getCurrentPowerSource() {
        return BatteryChanged.getCurrentPowerSource();
    }

    // getCurrentVoltage............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static Double getCurrentVoltage() {
        return BatteryChanged.getCurrentVoltage();
    }

    // mBatteryTemperature..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static Double getCurrentTemperature() {
        return BatteryChanged.getCurrentTemperature();
    }

    // getCurrentLevel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static Integer getCurrentLevel() {
        return BatteryChanged.getCurrentLevel();
    }

    // getScale.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static Integer getScale() {
        return BatteryChanged.getScale();
    }

    // getCurrentPercent............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    public static Double getCurrentPercent() { return BatteryChanged.getCurrentPercent(); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getString....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
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
     * TODO: description, comments and logging
     */
    public static void shutdown() {
        mInstance.mBatteryChanged.shutdown();
    }

}