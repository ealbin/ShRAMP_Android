package sci.crayfis.shramp.sensor;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract class BasicSensor implements SensorEventListener {

    // Protected Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Accuracy.....................................................................................
    // TODO: description
    protected enum Accuracy {LOW, MEDIUM, HIGH, UNRELIABLE}

    // ReportingMode................................................................................
    // TODO: description
    protected enum ReportingMode {CONTINUOUS, ON_CHANGE, ONE_SHOT, SPECIAL_TRIGGER}

    // Protected Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mMetaData....................................................................................
    // TODO: description
    protected class MetaData {
        Integer id;
        String  name;
        String  type;
        String  vendor;
        Integer version;
        Float   current; // mA
        String  description;

        ReportingMode reportingMode;
        String  reportingModeString;
        Integer maxDelay; // microseconds
        Integer minDelay; // microseconds

        Float   maximumRange; // sensor's units
        Float   resolution;   // sensor's units

        Accuracy accuracy;
        String accuracyString;

    }
    protected final MetaData mMetaData = new MetaData();

    // mSensor......................................................................................
    // TODO: description
    protected Sensor mSensor;

    // mHistory.....................................................................................
    // TODO: description
    protected final List<SensorEvent> mHistory = new ArrayList<>();

    // mSaveHistory.................................................................................
    // TODO: description
    protected boolean mSaveHistory;


    private static String mUnits;
    private static Integer mDimensions;

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // BasicSensor..................................................................................
    /**
     * TODO: description, comments and logging
     */
    private BasicSensor() {}

    // BasicSensor..................................................................................
    /**
     * TODO: description, comments and logging
     * @param sensor bla
     * @param description bla
     * @param units bla
     * @param dimensions bla
     * @param saveHistory bla
     */
    BasicSensor(@NonNull Sensor sensor, @Nullable String description, @NonNull String units,
                int dimensions, boolean saveHistory) {

        mSensor      = sensor;
        mSaveHistory = saveHistory;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mMetaData.id = null;
        }
        else {
            mMetaData.id = sensor.getId();
            if (mMetaData.id == 0) {
                mMetaData.id = null;
            }
            // if mId == -1, it means this sensor can be uniquely identified in system by
            // combination of its type and name.
        }

        mMetaData.name    = sensor.getName();
        mMetaData.type    = sensor.getStringType();
        mMetaData.vendor  = sensor.getVendor();
        mMetaData.version = sensor.getVersion();
        mMetaData.current = sensor.getPower();

        if (description == null) {
            mMetaData.description = "N/A";
        }
        else {
            mMetaData.description = description;
        }

        switch (sensor.getReportingMode()) {
            case (Sensor.REPORTING_MODE_CONTINUOUS): {
                mMetaData.reportingMode = ReportingMode.CONTINUOUS;
                mMetaData.reportingModeString = "CONTINUOUS";
                break;
            }
            case (Sensor.REPORTING_MODE_ON_CHANGE): {
                mMetaData.reportingMode = ReportingMode.ON_CHANGE;
                mMetaData.reportingModeString = "ON_CHANGE";
                break;
            }
            case (Sensor.REPORTING_MODE_ONE_SHOT): {
                mMetaData.reportingMode = ReportingMode.ONE_SHOT;
                mMetaData.reportingModeString = "ONE_SHOT";
                break;
            }
            case (Sensor.REPORTING_MODE_SPECIAL_TRIGGER): {
                mMetaData.reportingMode = ReportingMode.SPECIAL_TRIGGER;
                mMetaData.reportingModeString = "SPECIAL_TRIGGER";
                break;
            }
            default: {
                // TODO: error
            }
        }

        // aka lowest frequency of reporting is 1 / mMaxDelay [MHz]
        mMetaData.maxDelay = sensor.getMaxDelay(); // microseconds
        if (mMetaData.maxDelay <= 0) {
            mMetaData.maxDelay = null;
        }

        // aka fastest frequency of reporting is 1 / mMinDelay [MHz]
        mMetaData.minDelay = sensor.getMinDelay(); // microseconds
        if (mMetaData.minDelay == 0) {
            // this sensor only returns a value when the data it's measuring changes.
            mMetaData.minDelay = null;
        }

        // In sensor's units, whatever they may be
        mDimensions   = dimensions;
        mUnits        = units;
        mMetaData.maximumRange = sensor.getMaximumRange();
        mMetaData.resolution   = sensor.getResolution();

        mMetaData.accuracy = null;
        mMetaData.accuracyString = "UNKNOWN";
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDimensions................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public static int getDimensions() {
        return mDimensions;
    }

    public static String getUnits() { return mUnits; }

    // getHistory...................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    List<SensorEvent> getHistory() {
        return mHistory;
    }

    // getLast......................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    SensorEvent getLast() {
        if (mHistory.size() == 0) {
            return null;
        }
        return mHistory.get( mHistory.size() - 1 );
    }

    // mSensor......................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    Sensor getSensor() {
        return mSensor;
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onAccuracyChanged............................................................................
    /**
     * TODO: description, comments and logging
     * @param sensor bla
     * @param accuracy bla
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // For now, I don't care

        switch (accuracy) {
            case (SensorManager.SENSOR_STATUS_ACCURACY_LOW): {
                mMetaData.accuracy = Accuracy.LOW;
                mMetaData.accuracyString = "LOW";
                break;
            }
            case (SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM): {
                mMetaData.accuracy = Accuracy.MEDIUM;
                mMetaData.accuracyString = "MEDIUM";
                break;
            }
            case (SensorManager.SENSOR_STATUS_ACCURACY_HIGH): {
                mMetaData.accuracy = Accuracy.HIGH;
                mMetaData.accuracyString = "HIGH";
                break;
            }
            case (SensorManager.SENSOR_STATUS_UNRELIABLE): {
                mMetaData.accuracy = Accuracy.UNRELIABLE;
                mMetaData.accuracyString = "UNRELIABLE";
                break;
            }
            default: {
                // TODO: error
            }
        }
    }

    // onSensorChanged..............................................................................
    /**
     * TODO: description, comments and logging
     * @param event bla
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mHistory.size() == 0) {
            onAccuracyChanged(event.sensor, event.accuracy);
            mHistory.add(event);
            return;
        }

        if (mSaveHistory) {
            mHistory.add(event);
        }
        else {
            mHistory.set(0, event);
        }
    }

    // toString.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Override
    @NonNull
    public String toString() {
        String out = " \n";

        out += "\t" + "Sensor ID:                        ";
        if (mMetaData.id == null) {
            out += "NOT SUPPORTED";
        }
        else if (mMetaData.id == -1) {
            out += "N/A";
        }
        else {
            out += NumToString.number(mMetaData.id);
        }
        out += "\n";

        out += "\t" + "Sensor Name:                      " + mMetaData.name + "\n";
        out += "\t" + "Sensor Type:                      " + mMetaData.type + "\n";
        out += "\t" + "Sensor Vendor:                    " + mMetaData.vendor + "\n";
        out += "\t" + "Sensor Version:                   " + NumToString.number(mMetaData.version) + "\n";
        out += "\t" + "Sensor Current:                   " + NumToString.decimal(mMetaData.current) + " [mA]\n";

        out += "\t" + "Sensor Reporting Mode:            " + mMetaData.reportingModeString + "\n";

        out += "\t" + "Sensor Lowest Sampling Frequency: ";
        if (mMetaData.maxDelay == null) {
            out += "N/A";
        }
        else {
            float MHz = 1.f / mMetaData.maxDelay;
            out += NumToString.decimal(MHz) + " [MHz]\n";
        }

        out += "\t" + "Sensor Maximum Sampling Frequency: ";
        if (mMetaData.minDelay == null) {
            out += "N/A";
        }
        else {
            float MHz = 1.f / mMetaData.minDelay;
            out += NumToString.decimal(MHz) + " [MHz]\n";
        }

        out += "\t" + "Sensor Output Dimensionality:      " + NumToString.number(mDimensions) + "\n";
        out += "\t" + "Sensor Maximum Value:              " + NumToString.decimal(mMetaData.maximumRange) + " [" + mUnits + "]\n";
        out += "\t" + "Sensor Resolution:                 " + NumToString.decimal(mMetaData.resolution) + " [" + mUnits + "]\n";

        out += "\t" + "Sensor Current Accuracy:           " + mMetaData.accuracyString + "\n";

        return out;
    }

}