package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class ParameterFormatter<T> {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PADDING_SIZE.................................................................................
    // TODO: description
    private final static int PADDING_SIZE = 55;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mValueString.................................................................................
    // TODO: description
    private String mValueString = "ERROR: VALUE NOT SET";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ParameterFormatter...........................................................................
    /**
     * TODO: description, comments and logging
     */
    public ParameterFormatter() {}

    // ParameterFormatter...........................................................................
    /**
     * TODO: description, comments and logging
     * @param valueString bla
     */
    public ParameterFormatter(@NonNull String valueString) {
        mValueString = valueString;
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // toString.....................................................................................
    /**
     * TODO: description, comments and logging
     * @param description bla
     * @param value bla
     * @param units bla
     * @return bla
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

    // Protected Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getValueString...............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    protected String getValueString() {
        return mValueString;
    }

    // setValueString...............................................................................
    /**
     * TODO: description, comments and logging
     * @param  valueString bla
     */
    protected void setValueString(@NonNull String valueString) {
        mValueString = valueString;
    }

    // Public Abstract Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // formatValue..................................................................................
    /**
     * TODO: description, comments and logging
     * @param value bla
     * @return bla
     */
    @NonNull
    public abstract String formatValue(@NonNull T value);

}