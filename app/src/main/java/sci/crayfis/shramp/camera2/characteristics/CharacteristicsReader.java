/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

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
 * TODO: description, comments and logging
 */
@TargetApi(21)
public final class CharacteristicsReader extends Tonemap_ {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final CharacteristicsReader mInstance = new CharacteristicsReader();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CharacteristicsReader........................................................................
    /**
     * TODO: description, comments and logging
     */
    private CharacteristicsReader() {
        super();
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param cameraCharacteristics bla
     * @return bla
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
     * TODO: description, comments and logging
     *
     * @param label bla
     * @param map bla
     * @param keychain bla
     */
    public static void write(@Nullable String label,
                             @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> map,
                             @Nullable List<CameraCharacteristics.Key<?>> keychain) {

        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        String tag = "CharacteristicsReader";
        if (label != null) {
            tag = label;
        }

        Log.e(tag, "Camera Characteristics Summary:\n");
        for (Parameter parameter : map.values()) {
            Log.e(tag, parameter.toString());
        }

        if (keychain != null) {
            Log.e(tag, "Keys unset:\n");
            for (CameraCharacteristics.Key<?> key : keychain) {
                if (!map.containsKey(key)) {
                    Log.e(tag, key.getName());
                }
            }
        }
        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
    }

    // Protected Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param cameraCharacteristics bla
     * @param characteristicsMap bla
     */
    @Override
    protected void read(@NonNull CameraCharacteristics cameraCharacteristics,
                        @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap) {
        Log.e("CharacteristicsReader", "reading characteristics");
        super.read(cameraCharacteristics, characteristicsMap);
    }

}