package recycle_bin;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;

import sci.crayfis.shramp.util.NumToString;

// TODO: Intent.ACTION_BATTERY_xxx broadcast

// TODO: figure out what the integer codes are

@TargetApi(21)
public abstract class BatteryControllerOld {

    private static BatteryManager mBatteryManager;
    private static Intent mBatteryChanged;
    private static Intent mBatteryOkay;
    private static Intent mBatteryLow;
    private static Intent mPowerConnected;
    private static Intent mPowerDisconnected;
    private static Intent mPowerSummary;

    public static void initialize(@NonNull Activity activity) {
        mBatteryManager = (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);

        mBatteryChanged = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mBatteryOkay    = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        mBatteryLow     = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_LOW));

        mPowerConnected    = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        mPowerDisconnected = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        mPowerSummary      = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_POWER_USAGE_SUMMARY));
    }

    public static int isBatteryCold() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_COLD);
    }

    public static int isBatteryDead() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_DEAD);
    }

    public static int isBatteryGood() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_GOOD);
    }

    public static int isBatteryOverheating() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_OVERHEAT);
    }

    public static int isBatteryOverVoltage() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE);
    }

    public static int isBatteryHealthUnknown() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_UNKNOWN);
    }

    public static int isBatteryUnspecifiedFailure() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE);
    }

    public static int isBatteryAcCharging() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PLUGGED_AC);
    }

    public static int isBatteryUsbCharging() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PLUGGED_USB);
    }

    public static int isBatteryWirelessCharging() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PLUGGED_WIRELESS);
    }

    // returns as a percent with no decimal part
    public static int getRemainingCapacity() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    // capacity in micro-amp-hours
    public static int getBatteryCapacity() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
    }

    // average current in micro-amps
    public static int getAverageCurrent() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
    }

    // hours remaining
    public static float getRemainingTime() {
        int capacity = getBatteryCapacity();
        int current  = getAverageCurrent();
        return capacity / (float) current;
    }

    // current current in micro-amps
    public static int getInstantaneousCurrent() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
    }

    // remaining power in nano-watt-hours
    public static long getRemainingEnergy() {
        assert mBatteryManager != null;
        return mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
    }

    // average continuous nano-watts of power remaining
    public static float getRemainingPower() {
        long energy = getRemainingEnergy();
        float time  = getRemainingTime();
        return energy / time;
    }

    // average voltage for remaining activity
    public static float getRemainingVoltage() {
        float power = getRemainingPower() / 1e3f;
        int current = getAverageCurrent();
        return power / (float) current;
    }

    public static int isCharging() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_STATUS_CHARGING);
    }

    public static int isDischarging() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_STATUS_DISCHARGING);
    }

    public static int isFullyCharged() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_STATUS_FULL);
    }

    public static int isNotCharging() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_STATUS_NOT_CHARGING);
    }

    public static int isStatusUnknown() {
        assert mBatteryManager != null;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_STATUS_UNKNOWN);
    }

    @NonNull
    public static String getAllInfo() {
        String out = " \n";

        out += "\t" + "Battery health cold:            " + NumToString.number(isBatteryCold()) + "\n";
        out += "\t" + "Battery health dead:            " + NumToString.number(isBatteryDead()) + "\n";
        out += "\t" + "Battery health good:            " + NumToString.number(isBatteryGood()) + "\n";
        out += "\t" + "Battery is overheating:         " + NumToString.number(isBatteryOverheating()) + "\n";
        out += "\t" + "Battery is over voltage:        " + NumToString.number(isBatteryOverVoltage()) + "\n";
        out += "\t" + "Battery unspecified failure:    " + NumToString.number(isBatteryUnspecifiedFailure()) + "\n";
        out += "\t" + "Battery charging via AC:        " + NumToString.number(isBatteryAcCharging()) + "\n";
        out += "\t" + "Battery charging via USB:       " + NumToString.number(isBatteryUsbCharging()) + "\n";
        out += "\t" + "Battery charging via wireless:  " + NumToString.number(isBatteryWirelessCharging()) + "\n";
        out += "\n";
        out += "\t" + "Remaining capacity:             " + NumToString.number(getRemainingCapacity()) + "%\n";
        out += "\t" + "Battery capacity:               " + NumToString.number(getBatteryCapacity()) + " [uA hr]\n";
        out += "\t" + "Average battery current:        " + NumToString.number(getAverageCurrent()) + " [uA]\n";
        out += "\t" + "Est. time until discharged:     " + NumToString.decimal(getRemainingTime()) + " [hr]\n";
        out += "\t" + "Instantaneous current:          " + NumToString.number(getInstantaneousCurrent()) + " [uA]\n";
        out += "\t" + "Remaining energy:               " + NumToString.number(getRemainingEnergy()) + " [nW hr]\n";
        out += "\t" + "Est. remaining average power:   " + NumToString.decimal(getRemainingPower()) + " [nW]\n";
        out += "\t" + "Est. remaining average voltage: " + NumToString.decimal(getRemainingVoltage()) + " [V]\n";
        out += "\n";
        out += "\t" + "Charging:                       " + NumToString.number(isCharging()) + "\n";
        out += "\t" + "Discharging:                    " + NumToString.number(isDischarging()) + "\n";
        out += "\t" + "Fully charged:                  " + NumToString.number(isFullyCharged()) + "\n";
        out += "\t" + "Not charging:                   " + NumToString.number(isNotCharging()) + "\n";
        out += "\t" + "Status unknown:                 " + NumToString.number(isStatusUnknown()) + "\n";

        return out + "\n";
    }

}