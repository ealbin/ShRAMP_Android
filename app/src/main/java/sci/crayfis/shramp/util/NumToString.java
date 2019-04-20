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
 * @updated: 20 April 2019
 */

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Convenient numeric to string formatting
 */
@TargetApi(21)
abstract public class NumToString {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mDecimal.....................................................................................
    // Format decimal numbers to two digits past zero, e.g. 9384857.23
    private static final DecimalFormat mDecimal = new DecimalFormat("#.##");

    // mSci.........................................................................................
    // Format decimal numbers into scientific notation with 3 significant figures, e.g. 6.02E23
    private static final DecimalFormat mSci = new DecimalFormat("0.00E00");

    // mNumber......................................................................................
    // General number format e.g. 1,234,567.8901
    private static final DecimalFormat mNumber = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Decimal Conversions
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // decimal......................................................................................
    /**
     * @param number Float number to convert to string
     * @return a two-digits-past-zero decimal, e.g. 23456.78
     */
    @NonNull
    public static String decimal(float number) {
        return mDecimal.format(number);
    }

    // decimal......................................................................................
    /**
     * @param number Double number to convert to string
     * @return a two-digits-past-zero decimal, e.g. 23456.78
     */
    @NonNull
    public static String decimal(double number) {
        return mDecimal.format(number);
    }

    // Public Class Scientific Notation Conversions
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // sci..........................................................................................
    /**
     * @param number Integer number to convert to string
     * @return a 3-significant-digit scientific notation String, e.g. 3.14E15
     */
    @NonNull
    public static String sci(int number) {
        return mSci.format(number);
    }

    // sci..........................................................................................
    /**
     * @param number Long integer number to convert to string
     * @return a 3-significant-digit scientific notation String, e.g. 3.14E15
     */
    @NonNull
    public static String sci(long number) {
        return mSci.format(number);
    }

    // sci..........................................................................................
    /**
     * @param number Floating point number to convert to string
     * @return a 3-significant-digit scientific notation String, e.g. 3.14E15
     */
    @NonNull
    public static String sci(float number) {
        return mSci.format(number);
    }

    // sci..........................................................................................
    /**
     * @param number Double floating point number to convert to string
     * @return a 3-significant-digit scientific notation String, e.g. 3.14E15
     */
    @NonNull
    public static String sci(double number) {
        return mSci.format(number);
    }

    // Public Class General Number Conversions
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // number.......................................................................................
    /**
     * @param number Short integer number to convert to string
     * @return a general number formatted string, e.g. 1,234,567.8910
     */
    @NonNull
    public static String number(short number) {
        return mNumber.format(number);
    }

    // number.......................................................................................
    /**
     * @param number Integer number to convert to string
     * @return a general number formatted string, e.g. 1,234,567.8910
     */
    @NonNull
    public static String number(int number) {
        return mNumber.format(number);
    }

    // number.......................................................................................
    /**
     * @param number Long integer number to convert to string
     * @return a general number formatted string, e.g. 1,234,567.8910
     */
    @NonNull
    public static String number(long number) {
        return mNumber.format(number);
    }

    // number.......................................................................................
    /**
     * @param number Floating point number to convert to string
     * @return a general number formatted string, e.g. 1,234,567.8910
     */
    @NonNull
    public static String number(float number) {
        return mNumber.format(number);
    }

    // number.......................................................................................
    /**
     * @param number Double floating point number to convert to string
     * @return a general number formatted string, e.g. 1,234,567.8910
     */
    @NonNull
    public static String number(double number) {
        return mNumber.format(number);
    }

}