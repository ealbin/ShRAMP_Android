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
 * @updated: 29 April 2019
 */

package sci.crayfis.shramp.sensor;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.support.annotation.NonNull;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Low priority

/**
 * Ambient Humidity Sensors
 */
@TargetApi(21)
final class Humidity extends BasicSensor {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private final static String mDescription = "Ambient relative humidity";
    private final static String mUnits       = "%";

    // Humidity is a scalar quantity (dimensionality = 1)
    private final static int mDimensions = 1;

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Humidity.....................................................................................
    /**
     * Create new humidity sensor
     * @param sensor System hardware reference
     * @param saveHistory True to enable saving pressure history, false to disable
     */
    Humidity(@NonNull Sensor sensor, boolean saveHistory) {
        super(sensor, mDescription, mUnits, mDimensions, saveHistory);
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDewPointTemperature.......................................................................
    /**
     * Compute the dew-point temperature
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

    // getAbsoluteHumidity..........................................................................
    /**
     * Compute the absolute humidity
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