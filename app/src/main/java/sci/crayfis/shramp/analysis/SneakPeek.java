package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.util.Log;

import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class SneakPeek {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PEEK_SIZE....................................................................................
    // TODO: description
    private static final int PEEK_SIZE = 100;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mNpixels.....................................................................................
    // TODO: description
    private static Integer mNpixels;
    
    // mFloatData...................................................................................
    // TODO: description
    private static float[] mFloatData;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setNpixels...................................................................................
    /**
     * TODO: description, comments and logging
     * @param npixels bla
     */
    static void setNpixels(int npixels) {
        mNpixels = npixels;
        mFloatData = new float[mNpixels];
    }

    // exposureValueSum.............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void exposureValueSum() {
        System.gc();
        if (HeapMemory.isMemoryLow()) {
            return;
        }
        double[] doubleData = new double[mNpixels];

        // TODO: THIS WONT WORK NOW
        ImageProcessor.getExposureValueSum().copyTo(doubleData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(doubleData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Exposure Value Sum /////////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);

        doubleData = null;
        System.gc();
    }

    // exposureValue2Sum............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void exposureValue2Sum() {
        System.gc();
        if (HeapMemory.isMemoryLow()) {
            return;
        }
        double[] doubleData = new double[mNpixels];

        // TODO: THIS WONT WORK NOW
        ImageProcessor.getExposureValue2Sum().copyTo(doubleData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(doubleData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Exposure Value^2 Sum ///////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);

        doubleData = null;
        System.gc();
    }


    // meanAndErr...................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void meanAndErr() {
        
        ImageProcessor.getMeanRate().copyTo(mFloatData);
        String[] mean = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            mean[i] = NumToString.sci(mFloatData[i]);
        }

        ImageProcessor.getStdErrRate().copyTo(mFloatData);
        String[] err = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            err[i] = NumToString.sci(mFloatData[i]);
        }

        int len = 0;
        for (int i = 0; i < PEEK_SIZE; i++) {
            if (mean[i].length() > len) {
                len = mean[i].length();
            }
            if (err[i].length() > len) {
                len = err[i].length();
            }
        }

        for (int i = 0; i < PEEK_SIZE; i++) {
            if (mean[i].length() < len) {
                for (int j = mean[i].length(); j < len; j++) {
                    mean[i] += ".";
                }
            }
            if (err[i].length() < len) {
                for (int j = err[i].length(); j < len; j++) {
                    err[i] += ".";
                }
            }
        }

        String out = " \n\n";
        out += "-----------------------------------------------------------------------------------\n";
        out += "//  Mean +/- Error Rates  /////////////////////////////////////////////////////////\n";
        out += "-----------------------------------------------------------------------------------\n";
        out += "\n";
        for (int i = 0; i < PEEK_SIZE; i++) {
            out += mean[i] + " +/- " + err[i] + "...";
            if ((i + 1) % 10 == 0) {
                out += "\n";
            }
        }
        out += " \n";
        Log.e(Thread.currentThread().getName(), out);
    }

    // stdDev.......................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void stdDev() {

        ImageProcessor.getStdDevRate().copyTo(mFloatData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mFloatData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Standard Deviation Rate ////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);
    }

    // significance.................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void significance() {
        
        ImageProcessor.getSignificance().copyTo(mFloatData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mFloatData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Significance ///////////////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);

        mFloatData = null;
        System.gc();
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // logValues....................................................................................
    /**
     * TODO: description, comments and logging
     * @param title bla
     * @param values bla
     */
    private static void logValues(@NonNull String title, @NonNull String[] values) {
        int len = 0;
        for (int i = 0; i < PEEK_SIZE; i++) {
            int tmp = values[i].length();
            if (tmp > len) {
                len = tmp;
            }
        }

        String out = " \n\n";
        out += title + "\n";
        for (int i = 0; i < PEEK_SIZE; i++) {
            if (values[i].length() < len) {
                for (int j = values[i].length(); j < len; j++) {
                    values[i] += ".";
                }
            }
            out += values[i] + "...";
            if ((i + 1) % 10 == 0) {
                out += "\n";
            }
        }
        out += "\n\n";
        Log.e(Thread.currentThread().getName(), out);
    }

}