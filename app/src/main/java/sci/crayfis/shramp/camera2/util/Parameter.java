package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * TODO: description, comments and logging
 * @param <T>
 */
@TargetApi(21)
public class Parameter<T> {

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDefaultFormat...............................................................................
    // TODO: description
    private final ParameterFormatter<T> mDefaultFormat = new ParameterFormatter<T>() {
        @Override
        public String formatValue(@NonNull T value) {
            return value.toString();
        }
    };

    // Private
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

    //**********************************************************************************************
    // Constructors
    //-------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Parameter....................................................................................
    /**
     * TODO: description, comments and logging
     * @param description
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
     * @param description
     * @param value
     * @param units
     * @param parameterFormatter
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

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Parameter....................................................................................
    /**
     * TODO: description, comments and logging
     */
    private Parameter() {}

    //**********************************************************************************************
    // Class Methods
    //--------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDescription...............................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    public String getDescription() { return mDescription; }

    // getFormatter.................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    public ParameterFormatter<T> getFormatter() { return mParameterFormatter; }

    // getUnits.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    public String getUnits() { return  mUnits; }

    // getValue.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @Nullable
    public T getValue() { return  mValue; }

    // setFormatter.................................................................................
    /**
     * TODO: description, comments and logging
     * @param parameterFormatter
     */
    public void setFormatter(ParameterFormatter<T> parameterFormatter) { mParameterFormatter = parameterFormatter; }

    // setUnits.....................................................................................
    /**
     * TODO: description, comments and logging
     * @param units
     */
    public void setUnits(String units) { mUnits = units; }

    // setValueString...............................................................................
    /**
     * TODO: description, comments and logging
     * @param valueString
     */
    public void setValueString(@NonNull String valueString) { mParameterFormatter.setValueString(valueString); }

    //**********************************************************************************************
    // Overriden Methods
    //------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // toString.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    @Override
    public String toString() {
        return mParameterFormatter.toString(mDescription, mValue, mUnits);
    }


}

