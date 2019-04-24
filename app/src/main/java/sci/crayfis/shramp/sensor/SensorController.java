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

package sci.crayfis.shramp.sensor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Low priority

/**
 * Public interface to all sensors available
 */
@TargetApi(21)
abstract public class SensorController {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Collections of various sensors that might be present (device dependant)
    private static final List<Temperature> mTemperatureSensors = new ArrayList<>();
    private static final List<Light>       mLightSensors       = new ArrayList<>();
    private static final List<Pressure>    mPressureSensors    = new ArrayList<>();
    private static final List<Humidity>    mHumiditySensors    = new ArrayList<>();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mSensorManager...............................................................................
    // System sensor manager reference
    private static SensorManager mSensorManager;

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // initializeAll................................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     * @param saveAllHistory bla
     */
    public static void initializeAll(@NonNull Activity activity, boolean saveAllHistory) {
        initializeTemperature(activity, saveAllHistory);
        initializeLight(activity, saveAllHistory);
        initializePressure(activity, saveAllHistory);
        initializeHumidity(activity, saveAllHistory);

        // TODO: sensor list
        /*
        List<Sensor> accelerometerSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> geomagneticRotationSensors = mSensorManager.getSensorList(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        List<Sensor> gravitySensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        List<Sensor> gyroscopicSensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        List<Sensor> linearAccelerometerSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        List<Sensor> magneticFieldSensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        //List<Sensor> position6DofSensors = mSensorManager.getSensorList(Sensor.TYPE_POSE_6DOF);

        List<Sensor> rotationSensors = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        List<Sensor> significantMotionSensors = mSensorManager.getSensorList(Sensor.TYPE_SIGNIFICANT_MOTION);

        //SensorManager.getAltitude()
        //SensorManager.getInclination()
        //SensorManager.getOrientation()
        */

        onResume();
    }

    // initializeTemperature........................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     * @param saveHistory bla
     */
    public static void initializeTemperature(@NonNull Activity activity, boolean saveHistory) {
        getSensorManager(activity);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        for (Sensor sensor : sensors ) {
            mTemperatureSensors.add(new Temperature(sensor, saveHistory));
        }

    }

    // initializeLight..............................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     * @param saveHistory bla
     */
    public static void initializeLight(@NonNull Activity activity, boolean saveHistory) {
        getSensorManager(activity);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_LIGHT);
        for (Sensor sensor : sensors ) {
            mLightSensors.add(new Light(sensor, saveHistory));
        }
    }

    // initializePressure...........................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     * @param saveHistory bla
     */
    public static void initializePressure(@NonNull Activity activity, boolean saveHistory) {
        getSensorManager(activity);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        for (Sensor sensor : sensors ) {
            mPressureSensors.add(new Pressure(sensor, saveHistory));
        }
    }

    // initializeHumidity...........................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     * @param saveHistory bla
     */
    public static void initializeHumidity(@NonNull Activity activity, boolean saveHistory) {
        getSensorManager(activity);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
        for (Sensor sensor : sensors) {
            mHumiditySensors.add(new Humidity(sensor, saveHistory));
        }
    }

    // onResume.....................................................................................
    /**
     * Register sensor listeners with the system
     */
    public static void onResume() {

        for (Temperature sensor : mTemperatureSensors) {
            mSensorManager.registerListener(sensor, sensor.getSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        }

        for (Light sensor : mLightSensors) {
            mSensorManager.registerListener(sensor, sensor.getSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        }

        for (Pressure sensor : mPressureSensors) {
            mSensorManager.registerListener(sensor, sensor.getSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        }

        for (Humidity sensor : mHumiditySensors) {
            mSensorManager.registerListener(sensor, sensor.getSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        }

        // TODO: registerListener()
    }

    // onPause......................................................................................
    /**
     * Release sensor listeners from the system to conserve power and ...
     */
    public static void onPause() {
        for (Temperature sensor : mTemperatureSensors) {
            mSensorManager.unregisterListener(sensor);
        }

        for (Light sensor : mLightSensors) {
            mSensorManager.unregisterListener(sensor);
        }

        for (Pressure sensor : mPressureSensors) {
            mSensorManager.unregisterListener(sensor);
        }

        for (Humidity sensor : mHumiditySensors) {
            mSensorManager.unregisterListener(sensor);
        }
        // TODO: unregisterListener()
    }

    // getLatestTemperature.........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static List<SensorEvent> getLatestTemperature() {
        List<SensorEvent> latest = new ArrayList<>();
        for (Temperature sensor : mTemperatureSensors) {
            latest.add(sensor.getLast());
        }
        return latest;
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getSensorManager.............................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     */
    private static void getSensorManager(@NonNull Activity activity) {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        }
    }

}