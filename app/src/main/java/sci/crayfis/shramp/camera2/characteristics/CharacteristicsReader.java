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

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final CharacteristicsReader mInstance = new CharacteristicsReader();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // CharacteristicsReader........................................................................
    /**
     * TODO: description, comments and logging
     */
    private CharacteristicsReader() {
        super();
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param cameraCharacteristics
     * @return
     */
    @NonNull
    public static LinkedHashMap<CameraCharacteristics.Key, Parameter> read(
                                            @NonNull CameraCharacteristics cameraCharacteristics) {

        LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap
                                                                            = new LinkedHashMap<>();
        mInstance.read(cameraCharacteristics, characteristicsMap);
        return characteristicsMap;
    }

    // write........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param label
     * @param map
     * @param keychain
     */
    public static void write(@Nullable String label,
                             @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> map,
                             @Nullable List<CameraCharacteristics.Key<?>> keychain) {

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
    }

    //**********************************************************************************************
    // Overriden Class Methods
    //------------------------

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // read.........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param cameraCharacteristics
     * @return
     */
    @Override
    protected void read(@NonNull CameraCharacteristics cameraCharacteristics,
                        @NonNull LinkedHashMap<CameraCharacteristics.Key, Parameter> characteristicsMap) {
        Log.e("CharacteristicsReader", "reading characteristics");
        super.read(cameraCharacteristics, characteristicsMap);
    }

}
