package sci.crayfis.shramp.battery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class BatteryChanged extends BatteryReceiver {

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBatteryIcon.................................................................................
    // TODO: description
    private static Integer mBatteryIcon;

    // mBatteryTechnology...........................................................................
    // TODO: description
    private static String mBatteryTechnology;

    // mBatteryPresent..............................................................................
    // TODO: description
    private static Boolean mBatteryPresent;

    // mBatteryHealth...............................................................................
    // TODO: description
    private static String mBatteryHealth;

    // mBatteryStatus...............................................................................
    // TODO: description
    private static String mBatteryStatus;

    // mBatteryPlugged..............................................................................
    // TODO: description
    private static String mBatteryPlugged;

    // mBatteryVoltage..............................................................................
    // TODO: description
    private static Double mBatteryVoltage;

    // mBatteryTemperature..........................................................................
    // TODO: description
    private static Double mBatteryTemperature;

    // mBatteryLevel................................................................................
    // TODO: description
    private static Integer mBatteryLevel;

    // mBatteryScale................................................................................
    // TODO: description
    private static Integer mBatteryScale;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // BatteryChanged...............................................................................
    /**
     * TODO: description, comments and logging
     */
    public BatteryChanged() {
        super();
    }

    // BatteryChanged...............................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     */
    BatteryChanged(@NonNull Activity activity) {
        super(activity, Intent.ACTION_BATTERY_CHANGED);
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getCurrentIcon...............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static Integer getCurrentIcon() {
        return mBatteryIcon;
    }

    // getTechnology................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static String getTechnology() {
        return mBatteryTechnology;
    }

    // isBatteryPresent.............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Boolean isBatteryPresent() {
        return mBatteryPresent;
    }

    // getCurrentHealth.............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static String getCurrentHealth() {
        return mBatteryHealth;
    }

    // getCurrentStatus.............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static String getCurrentStatus() {
        return mBatteryStatus;
    }

    // getCurrentPowerSource........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static String getCurrentPowerSource() {
        return mBatteryPlugged;
    }

    // getCurrentVoltage............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static Double getCurrentVoltage() {
        return mBatteryVoltage;
    }

    // mBatteryTemperature..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static Double getCurrentTemperature() {
        return mBatteryTemperature;
    }

    // getCurrentLevel..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static Integer getCurrentLevel() {
        return mBatteryLevel;
    }

    // getScale.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static Integer getScale() {
        return mBatteryScale;
    }

    // getCurrentPercent............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @Nullable
    static Double getCurrentPercent() {
        if (mBatteryLevel == null || mBatteryScale == null) {
            return null;
        }
        return 100. * mBatteryLevel / (double) mBatteryScale;
    }

    // getString....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Override
    @NonNull
    String getString() {
        final String nullString = "NOT AVAILABLE";

        String batteryIcon;
        if (mBatteryIcon == null) {
            batteryIcon = nullString;
        }
        else {
            batteryIcon = NumToString.number(mBatteryIcon) + " [TODO: units]";
        }

        String batteryTechnology;
        if (mBatteryTechnology == null) {
            batteryTechnology = nullString;
        }
        else {
            batteryTechnology = mBatteryTechnology;
        }

        String batteryPresent;
        if (mBatteryPresent == null) {
            batteryPresent = nullString;
        }
        else {
            if (mBatteryPresent) {
                batteryPresent = "YES";
            }
            else {
                batteryPresent = "NO";
            }
        }

        String batteryHealth;
        if (mBatteryHealth == null) {
            batteryHealth = nullString;
        }
        else {
            batteryHealth = mBatteryHealth;
        }

        String batteryStatus;
        if (mBatteryStatus == null) {
            batteryStatus = nullString;
        }
        else {
            batteryStatus = mBatteryStatus;
        }

        String batteryPlugged;
        if (mBatteryPlugged == null) {
            batteryPlugged = nullString;
        }
        else {
            batteryPlugged = mBatteryPlugged;
        }

        String batteryVoltage;
        if (mBatteryVoltage == null) {
            batteryVoltage = nullString;
        }
        else {
            batteryVoltage = NumToString.number(mBatteryVoltage) + " [Volts]";
        }

        String batteryTemperature;
        if (mBatteryTemperature == null) {
            batteryTemperature = nullString;
        }
        else {
            batteryTemperature = NumToString.number(mBatteryTemperature) + " [Celsius]";
        }

        String batteryLevel;
        if (mBatteryLevel == null) {
            batteryLevel = nullString;
        }
        else {
            batteryLevel = NumToString.number(mBatteryLevel) + " [level units]";
        }

        String batteryScale;
        if (mBatteryScale == null) {
            batteryScale = nullString;
        }
        else {
            batteryScale = NumToString.number(mBatteryScale) + " [level units]";
        }

        String batteryPercent;
        Double percent = getCurrentPercent();
        if (percent == null) {
            batteryPercent = nullString;
        }
        else {
            batteryPercent = NumToString.number(percent) + "%";
        }

        String out = "";
        out += "\t" + "Battery icon:         " + batteryIcon        + "\n";
        out += "\t" + "Battery technology:   " + batteryTechnology  + "\n";
        out += "\t" + "Is battery present:   " + batteryPresent     + "\n";
        out += "\t" + "Battery health:       " + batteryHealth      + "\n";
        out += "\t" + "Battery status:       " + batteryStatus      + "\n";
        out += "\t" + "Battery power source: " + batteryPlugged     + "\n";
        out += "\t" + "Battery voltage:      " + batteryVoltage     + "\n";
        out += "\t" + "Battery temperature:  " + batteryTemperature + "\n";
        out += "\t" + "Battery level:        " + batteryLevel       + "\n";
        out += "\t" + "Battery scale:        " + batteryScale       + "\n";
        out += "\t" + "Battery percent:      " + batteryPercent     + "\n";
        return out;
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onReceive....................................................................................
    /**
     * TODO: description, comments and logging
     * @param context bla
     * @param intent bla
     */
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (!super.isOkToProceed(context, intent)) {
            return;
        }

        // Icon
        //------------------------------------------------------------------------------------------

        int icon = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1);
        if (icon == -1) {
            mBatteryIcon = null;
        }
        else {
            mBatteryIcon = icon;
        }

        // Technology
        //------------------------------------------------------------------------------------------

        mBatteryTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        // Present
        //------------------------------------------------------------------------------------------

        mBatteryPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);

        // Health
        //------------------------------------------------------------------------------------------

        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        switch (health) {
            case (BatteryManager.BATTERY_HEALTH_COLD): {
                mBatteryHealth = "COLD";
                break;
            }
            case (BatteryManager.BATTERY_HEALTH_DEAD): {
                mBatteryHealth = "DEAD";
                break;
            }
            case (BatteryManager.BATTERY_HEALTH_GOOD): {
                mBatteryHealth = "GOOD";
                break;
            }
            case (BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE): {
                mBatteryHealth = "OVER VOLTAGE";
                break;
            }
            case (BatteryManager.BATTERY_HEALTH_OVERHEAT): {
                mBatteryHealth = "OVERHEAT";
                break;
            }
            case (BatteryManager.BATTERY_HEALTH_UNKNOWN): {
                mBatteryHealth = "UNKNOWN";
                break;
            }
            case (BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE): {
                mBatteryHealth = "UNSPECIFIED FAILURE";
                break;
            }
            default:
                mBatteryHealth = "UNKNOWN CONDITION OR NOT AVAILABLE";
        }

        // Status
        //------------------------------------------------------------------------------------------

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        switch (status) {
            case (-1): {
                mBatteryStatus = null;
                break;
            }
            case (BatteryManager.BATTERY_STATUS_CHARGING): {
                mBatteryStatus = "CHARGING";
                break;
            }
            case (BatteryManager.BATTERY_STATUS_DISCHARGING): {
                mBatteryStatus = "DISCHARGING";
                break;
            }
            case (BatteryManager.BATTERY_STATUS_FULL): {
                mBatteryStatus = "FULLY CHARGED";
                break;
            }
            case (BatteryManager.BATTERY_STATUS_NOT_CHARGING): {
                mBatteryStatus = "NOT CHARGING";
                break;
            }
            case (BatteryManager.BATTERY_STATUS_UNKNOWN): {
                mBatteryStatus = "CHARGING STATUS UNKNOWN";
                break;
            }
            default: {
                mBatteryStatus = "UNKNOWN STATUS";
            }
        }

        // Plugged
        //------------------------------------------------------------------------------------------

        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        switch (plugged) {
            case (-1): {
                mBatteryPlugged = null;
                break;
            }
            case (0): {
                mBatteryPlugged = "USING BATTERY POWER ONLY";
                break;
            }
            case (BatteryManager.BATTERY_PLUGGED_AC): {
                mBatteryPlugged = "USING AC ADAPTER POWER";
                break;
            }
            case (BatteryManager.BATTERY_PLUGGED_USB): {
                mBatteryPlugged = "USING USB POWER";
                break;
            }
            case (BatteryManager.BATTERY_PLUGGED_WIRELESS): {
                mBatteryPlugged = "USING WIRELESS POWER";
                break;
            }
            default: {
                mBatteryPlugged = "UNKNOWN POWER SOURCE";
            }
        }

        // Voltage
        //------------------------------------------------------------------------------------------

        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        if (voltage == -1) {
            mBatteryVoltage = null;
        }
        else {
            mBatteryVoltage = voltage / 1e3;
        }

        // Temperature
        //------------------------------------------------------------------------------------------

        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        if (temperature == -1) {
            mBatteryTemperature = null;
        }
        else {
            mBatteryTemperature = temperature / 10.;
        }

        // Level
        //------------------------------------------------------------------------------------------

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        if (level == -1) {
            mBatteryLevel = null;
        }
        else {
            mBatteryLevel = level;
        }

        // Scale
        //------------------------------------------------------------------------------------------

        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (scale == -1) {
            mBatteryScale = null;
        }
        else {
            mBatteryScale = scale;
        }
    }

}