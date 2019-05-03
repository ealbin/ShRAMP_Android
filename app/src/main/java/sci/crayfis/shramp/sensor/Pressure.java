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

package sci.crayfis.shramp.sensor;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.support.annotation.NonNull;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Low priority

/**
 * Ambient Pressure Sensors
 */
@TargetApi(21)
final class Pressure extends BasicSensor {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private final static String mDescription = "Ambient air pressure";
    private final static String mUnits       = "millibar";

    // Pressure is a scalar quantity (dimensionality = 1)
    private final static int mDimensions = 1;

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Pressure.....................................................................................
    /**
     * Create new pressure sensor
     * @param sensor System hardware reference
     * @param saveHistory True to enable saving pressure history, false to disable
     */
    Pressure(@NonNull Sensor sensor, boolean saveHistory) {
        super(sensor, mDescription, mUnits, mDimensions, saveHistory);
    }

}