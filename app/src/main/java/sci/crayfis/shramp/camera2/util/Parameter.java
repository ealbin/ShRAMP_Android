package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * TODO: description, comments and logging
 * @param <T>
 */
@TargetApi(21)
public class Parameter<T> {

    // Private Instance Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDefaultFormat...............................................................................
    // TODO: description
    private final ParameterFormatter<T> mDefaultFormat = new ParameterFormatter<T>() {
        @Override
        public String formatValue(@NonNull T value) {
            return value.toString();
        }
    };

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDescription.................................................................................
    // TODO: description
    private String mDescription;

    // mValue.......................................................................................
    // TODO: description
    private T mValue;

    // mUnits.......................................................................................
    // TODO: description
    private String mUnits;

    // mParameterFormatter..........................................................................
    // TODO: description
    private ParameterFormatter<T> mParameterFormatter;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Parameter....................................................................................
    /**
     * TODO: description, comments and logging
     * @param description bla
     */
    public Parameter(@NonNull String description) {
        mValue       = null;
        mDescription = description;
        mUnits       = null;
        mParameterFormatter = mDefaultFormat;
    }

    // Parameter....................................................................................
    /**
     * TODO: description, comments and logging
     * @param description bla
     * @param value bla
     * @param units bla
     * @param parameterFormatter bla
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
     * TODO: description, comments and logging
     */
    private Parameter() {}

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDescription...............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    public String getDescription() { return mDescription; }

    // getFormatter.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    public ParameterFormatter<T> getFormatter() { return mParameterFormatter; }

    // getUnits.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public String getUnits() { return  mUnits; }

    // getValue.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    public T getValue() { return  mValue; }

    // setFormatter.................................................................................
    /**
     * TODO: description, comments and logging
     * @param parameterFormatter bla
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
     * TODO: description, comments and logging
     * @param units bla
     */
    public void setUnits(@Nullable String units) { mUnits = units; }

    // setValueString...............................................................................
    /**
     * TODO: description, comments and logging
     * @param valueString bla
     */
    public void setValueString(@NonNull String valueString) { mParameterFormatter.setValueString(valueString); }

    // Public Overriding Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // toString.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    @Override
    public String toString() {
        return mParameterFormatter.toString(mDescription, mValue, mUnits);
    }

}