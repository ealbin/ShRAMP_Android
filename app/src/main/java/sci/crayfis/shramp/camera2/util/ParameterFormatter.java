package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class ParameterFormatter<T> {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PADDING_SIZE.................................................................................
    // TODO: description
    private final static int PADDING_SIZE = 40;

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mValueString.................................................................................
    // TODO: description
    private String mValueString = "ERROR: VALUE NOT SET";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ParameterFormatter...........................................................................
    /**
     * TODO: description, comments and logging
     */
    public ParameterFormatter() { }

    // ParameterFormatter...........................................................................

    /**
     * TODO: description, comments and logging
     * @param valueString
     */
    public ParameterFormatter(@NonNull String valueString) {
        mValueString = valueString;
    }

    //**********************************************************************************************
    // Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // toString.....................................................................................
    /**
     * TODO: description, comments and logging
     * @param description
     * @param value
     * @param units
     * @return
     */
    public String toString(@NonNull String description, @Nullable T value,
                               @Nullable String units) {
        String out = description + ":  ";
        int length = out.length();
        for (int i = length; i <= PADDING_SIZE; i++) {
            out += " ";
        }

        if (value == null) {
            out += mValueString;
        }
        else {
            out += formatValue(value);
        }

        if (units == null) {
            return out;
        }
        return out + "  [" + units + "]";
    }

    // Protected
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getValueString...............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    protected String getValueString() {
        return mValueString;
    }

    // setValueString...............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    protected void setValueString(@NonNull String valueString) {
        mValueString = valueString;
    }

    //**********************************************************************************************
    // Abstract Class Methods
    //-----------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // formatValue..................................................................................
    /**
     * TODO: description, comments and logging
     * @param value
     * @return
     */
    @NonNull
    public abstract String formatValue(@NonNull T value);

}
