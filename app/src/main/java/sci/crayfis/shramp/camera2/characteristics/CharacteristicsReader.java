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

package sci.crayfis.shramp.camera2.characteristics;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;

import sci.crayfis.shramp.camera2.util.Parameter;

/**
 * Public access to discovering all abilities of a camera
 */
@TargetApi(21)
public final class CharacteristicsReader extends Tonemap_ {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // Reference to single instance of this class
    private static final CharacteristicsReader mInstance = new CharacteristicsReader();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CharacteristicsReader........................................................................
    /**
     * Disabled
     */
    private CharacteristicsReader() {}

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * Discovers the abilities of the active camera.  In some cases, filters or optimizes
     * parameter options.
     * @param cameraCharacteristics Encapsulation of camera abilities
     * @return A mapping of characteristics names to their respective parameter options
     */
    @NonNull
    public static LinkedHashMap<CameraCharacteristics.Key, Parameter> read(
                                            @NonNull CameraCharacteristics cameraCharacteristics) {

        LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap
                                                                            = new LinkedHashMap<>();
        Log.e(Thread.currentThread().getName(), "CharacteristicsReader read");
        mInstance.read(cameraCharacteristics, characteristicsMap);
        return characteristicsMap;
    }

    // write........................................................................................
    /**
     * Display all of the abilities of the camera
     * @param label (Optional) Custom title
     * @param map Details of camera abilities in terms of Parameters<T>
     * @param keychain (Optional) All keys that can be potentially set
     */
    public static void write(@Nullable String label,
                             @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> map,
                             @Nullable List<CameraCharacteristics.Key<?>> keychain) {

        if (label == null) {
            label = "CharacteristicsReader";
        }

        Log.e(Thread.currentThread().getName(), " \n\n\t\t" + label + " Camera Characteristics Summary:\n\n");
        for (Parameter parameter : map.values()) {
            Log.e(Thread.currentThread().getName(), parameter.toString());
        }

        if (keychain != null) {
            Log.e(Thread.currentThread().getName(), "Keys unset:\n");
            for (CameraCharacteristics.Key<?> key : keychain) {
                if (!map.containsKey(key)) {
                    Log.e(Thread.currentThread().getName(), key.getName());
                }
            }
        }
    }

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * Continue discovering abilities with specialized super classes
     * @param cameraCharacteristics Encapsulation of camera abilities
     * @param characteristicsMap A mapping of characteristics names to their respective parameter options
     */
    @Override
    protected void read(@NonNull CameraCharacteristics cameraCharacteristics,
                        @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap) {
        Log.e("CharacteristicsReader", "reading characteristics");
        super.read(cameraCharacteristics, characteristicsMap);
    }

}