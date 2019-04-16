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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Encapsulation of a parameter's description, value and units
 * @param <T> Parameter value type
 */
@TargetApi(21)
public class Parameter<T> {

    // Private Instance Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDefaultFormat...............................................................................
    // If a ParameterFormatter<T> is not provided, this is used as default
    private final ParameterFormatter<T> mDefaultFormat = new ParameterFormatter<T>() {
        @NonNull
        @Override
        public String formatValue(@NonNull T value) {
            return value.toString();
        }
    };

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDescription.................................................................................
    // A short description of the Parameter
    private String mDescription;

    // mValue.......................................................................................
    // The value associated with the Parameter
    private T mValue;

    // mUnits.......................................................................................
    // The units associated with the parameter
    private String mUnits;

    // mParameterFormatter..........................................................................
    // The ParameterFormatter to use when displaying
    private ParameterFormatter<T> mParameterFormatter;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Parameter....................................................................................
    /**
     * Option 1) create a blank Parameter with a description at minimum
     * @param description A short description of the Parameter
     */
    public Parameter(@NonNull String description) {
        mValue       = null;
        mDescription = description;
        mUnits       = null;
        mParameterFormatter = mDefaultFormat;
    }

    // Parameter....................................................................................
    /**
     * Option 2) create a complete Parameter object
     * @param description A short description of the parameter
     * @param value Of type <T>, the value associated with the Parameter (Optional)
     * @param units The units associated with the value of the Parameter (Optional)
     * @param parameterFormatter The formatter for this Parameter (Optional)
     */
    public Parameter(@NonNull String description, @Nullable T value,
                     @Nullable String units, @Nullable ParameterFormatter<T> parameterFormatter) {
        mValue       = value;
        mDescription = description;
        mUnits       = units;
        if (parameterFormatter == null) {
            mParameterFormatter = mDefaultFormat;
        }
        else {
            mParameterFormatter = parameterFormatter;
        }
    }

    // Parameter....................................................................................
    /**
     * Disable the default constructor option
     */
    private Parameter() {}

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDescription...............................................................................
    /**
     * @return A short description of the Parameter
     */
    @NonNull
    public String getDescription() { return mDescription; }

    // getFormatter.................................................................................
    /**
     * @return The formatter being used for this Parameter
     */
    @NonNull
    public ParameterFormatter<T> getFormatter() { return mParameterFormatter; }

    // getUnits.....................................................................................
    /**
     * @return The units associated with the value of this Parameter
     */
    @Nullable
    public String getUnits() { return  mUnits; }

    // getValue.....................................................................................
    /**
     * @return The value associated with this Parameter
     */
    @Nullable
    public T getValue() { return  mValue; }

    // setFormatter.................................................................................
    /**
     * @param parameterFormatter ParameterFormatter to be used
     */
    public void setFormatter(@Nullable ParameterFormatter<T> parameterFormatter) {
        if (parameterFormatter == null) {
            mParameterFormatter = mDefaultFormat;
        }
        else {
            mParameterFormatter = parameterFormatter;
        }
    }

    // setUnits.....................................................................................
    /**
     * @param units Units of the value
     */
    public void setUnits(@Nullable String units) { mUnits = units; }

    // setValueString...............................................................................
    /**
     * @param valueString A String representation of the value (used if value is null)
     */
    public void setValueString(@NonNull String valueString) { mParameterFormatter.setValueString(valueString); }

    // Public Overriding Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // toString.....................................................................................
    /**
     * @return A formatted String representation of this Parameter<T>
     */
    @NonNull
    @Override
    public String toString() {
        return mParameterFormatter.toString(mDescription, mValue, mUnits);
    }

}