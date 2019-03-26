package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class NumToString {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDecimal.....................................................................................
    // TODO: description
    private static final DecimalFormat mDecimal = new DecimalFormat("#.##");

    // mSci.........................................................................................
    // TODO: description
    private static final DecimalFormat mSci = new DecimalFormat("0.00E00");

    // mNumber......................................................................................
    // TODO: description
    private static final DecimalFormat mNumber = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Decimal Conversions
    // TODO: description
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // float
    @NonNull
    public static String decimal(float number) {
        return mDecimal.format(number);
    }

    // double
    @NonNull
    public static String decimal(double number) {
        return mDecimal.format(number);
    }

    // Public Class Scientific Notation Conversions
    // TODO: description
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // int
    @NonNull
    public static String sci(int number) {
        return mSci.format(number);
    }

    // long
    @NonNull
    public static String sci(long number) {
        return mSci.format(number);
    }

    // float
    @NonNull
    public static String sci(float number) {
        return mSci.format(number);
    }

    // double
    @NonNull
    public static String sci(double number) {
        return mSci.format(number);
    }

    // Public Class General Number Conversions
    // TODO: description
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // short
    @NonNull
    public static String number(short number) {
        return mNumber.format(number);
    }

    // int
    @NonNull
    public static String number(int number) {
        return mNumber.format(number);
    }

    // long
    @NonNull
    public static String number(long number) {
        return mNumber.format(number);
    }

    // float
    @NonNull
    public static String number(float number) {
        return mNumber.format(number);
    }

    // double
    @NonNull
    public static String number(double number) {
        return mNumber.format(number);
    }

}