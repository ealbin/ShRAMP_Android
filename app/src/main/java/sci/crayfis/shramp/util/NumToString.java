package sci.crayfis.shramp.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class NumToString {

    private static final DecimalFormat mDecimal = new DecimalFormat("#.##");
    private static final DecimalFormat mSci     = new DecimalFormat("0.00E00");
    private static final DecimalFormat mNumber  = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String decimal(float number) {
        return mDecimal.format(number);
    }

    public static String decimal(double number) {
        return mDecimal.format(number);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String sci(int number) {
        return mSci.format(number);
    }

    public static String sci(long number) {
        return mSci.format(number);
    }

    public static String sci(float number) {
        return mSci.format(number);
    }

    public static String sci(double number) {
        return mSci.format(number);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String number(short number) {
        return mNumber.format(number);
    }

    public static String number(int number) {
        return mNumber.format(number);
    }

    public static String number(long number) {
        return mNumber.format(number);
    }

    public static String number(float number) {
        return mNumber.format(number);
    }

    public static String number(double number) {
        return mNumber.format(number);
    }

}
