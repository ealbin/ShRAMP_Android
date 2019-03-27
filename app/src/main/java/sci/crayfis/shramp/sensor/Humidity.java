package sci.crayfis.shramp.sensor;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.support.annotation.NonNull;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final class Humidity extends BasicSensor {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private final static String mDescription = "Ambient relative humidity";
    private final static String mUnits       = "%";

    // TODO: description
    private final static int mDimensions = 1;

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Humidity.....................................................................................
    /**
     * TODO: description, comments and logging
     * @param sensor bla
     * @param saveHistory bla
     */
    Humidity(@NonNull Sensor sensor, boolean saveHistory) {
        super(sensor, mDescription, mUnits, mDimensions, saveHistory);
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDewPointTemperature.......................................................................
    /**
     * TODO: description, comments and logging
     * @param temperature [celsius]
     * @param relativeHumidity [%]
     * @return [celsius]
     */
    public static float getDewPointTemperature(float temperature, float relativeHumidity) {
        double m  = 17.62;  // [unitless]
        double Tn = 243.12; // [Celsius]

        double group1 = (float) Math.log(relativeHumidity);
        double group2 = m * temperature / (Tn + temperature);

        double numerator = group1 + group2;
        double denominator = m - numerator;

        return (float) ( Tn * numerator / denominator );
    }

    /**
     * TODO: description, comments and logging
     * @param temperature [celsius]
     * @param relativeHumidity [%]
     * @return [grams / meter^3]
     */
    public static float getAbsoluteHumidity(float temperature, float relativeHumidity) {
        double m  = 17.62;  // [unitless]
        double Tn = 243.12; // [Celsius]
        double A  = 6.112;  // [hectoPascals]

        double group1 = m * temperature / (Tn + temperature);

        double numerator = relativeHumidity * A * Math.exp(group1);
        double denominator = 273.15 + temperature;

        return (float) (216.7 * numerator / denominator);
    }

}