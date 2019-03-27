package sci.crayfis.shramp.sensor;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.support.annotation.NonNull;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final class AmbientTemperature extends BasicSensor {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private final static String mDescription = "Ambient air temperature";
    private final static String mUnits       = "Celsius";

    // TODO: description
    private final static int mDimensions = 1;

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // AmbientTemperature...........................................................................
    /**
     * TODO: description, comments and logging
     * @param sensor bla
     * @param saveHistory bla
     */
    AmbientTemperature(@NonNull Sensor sensor, boolean saveHistory) {
        super(sensor, mDescription, mUnits, mDimensions, saveHistory);
    }

}