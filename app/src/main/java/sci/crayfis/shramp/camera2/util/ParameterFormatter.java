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

package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

/**
 * The purpose of this class is to format the value of a Parameter<T> for printing as a string
 */
@TargetApi(21)
abstract public class ParameterFormatter<T> {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PADDING_SIZE.................................................................................
    // Whitespace after Parameter<T>'s name and before the string formatted by this class
    private final static int PADDING_SIZE = 55;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mValueString.................................................................................
    // In case the Parameter<T> value is unset
    private String mValueString = "ERROR: VALUE NOT SET";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ParameterFormatter...........................................................................
    /**
     * Option 1) formatted string can be produced directly from Parameter<T> value
     */
    public ParameterFormatter() {}

    // ParameterFormatter...........................................................................
    /**
     * Option 2) formatted string cannot be produced directly, or a custom string is desired
     * Note: Parameter<T> value must be null
     * @param valueString String to display when toString() is called
     */
    public ParameterFormatter(@NonNull String valueString) {
        mValueString = valueString;
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // toString.....................................................................................
    /**
     * Make a human-friendly displayable string describing this Parameter<T>
     * @param description Description provided by Parameter<T>
     * @param value Value provided by Parameter<T> (uses value string if null)
     * @param units Units provided by Parameter<T>
     * @return The formatted string
     */
    String toString(@NonNull String description, @Nullable T value,
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
     * @return Value string set at construction
     */
    @NonNull
    @Contract(pure = true)
    protected String getValueString() {
        return mValueString;
    }

    // setValueString...............................................................................
    /**
     * @param valueString Value string to display if Parameter<T> value is null
     */
    protected void setValueString(@NonNull String valueString) {
        mValueString = valueString;
    }

    // Public Abstract Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // formatValue..................................................................................
    /**
     * User must implement a custom formatting routine for each Parameter<T>
     * @param value Value to format
     * @return Formatted value
     */
    @NonNull
    abstract public String formatValue(@NonNull T value);

}