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
abstract public class PrintAndSave {

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

    // printExposureValueSum........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void printExposureValueSum() {
        System.gc();
        if (HeapMemory.isMemoryLow()) {
            return;
        }
        double[] doubleData = new double[mNpixels];

        // TODO: THIS WONT WORK NOW
        ImageProcessor.getValueSum().copyTo(doubleData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(doubleData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Exposure Value Sum /////////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        printValues(title, values);

        doubleData = null;
        System.gc();
    }

    // printExposureValue2Sum.......................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void printExposureValue2Sum() {
        System.gc();
        if (HeapMemory.isMemoryLow()) {
            return;
        }
        double[] doubleData = new double[mNpixels];

        // TODO: THIS WONT WORK NOW
        ImageProcessor.getValue2Sum().copyTo(doubleData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(doubleData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Exposure Value^2 Sum ///////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        printValues(title, values);

        doubleData = null;
        System.gc();
    }

    public static void printMaxMin() {
        ImageProcessor.getMean().copyTo(mFloatData);
        float meanMax = -1.f;
        float meanMin = -1.f;
        for (float val : mFloatData) {
            if (meanMax < 0.f) {
                meanMax = val;
                meanMin = val;
                continue;
            }

            if (val > meanMax) {
                meanMax = val;
            }

            if (val < meanMin) {
                meanMin = val;
            }
        }

        ImageProcessor.getStdDev().copyTo(mFloatData);
        float stdMax = -1.f;
        float stdMin = -1.f;
        for (float val : mFloatData) {
            if (stdMax < 0.f) {
                stdMax = val;
                stdMin = val;
                continue;
            }

            if (val > stdMax) {
                stdMax = val;
            }

            if (val < stdMin) {
                stdMin = val;
            }
        }

        String out = " \n";
        out += "\t" + "Mean Max: " + NumToString.number(meanMax) + "\n";
        out += "\t" + "Std  Max: " + NumToString.number(stdMax) + "\n";
        out += "\t" + "Mean Min: " + NumToString.number(meanMin) + "\n";
        out += "\t" + "Std  Min: " + NumToString.number(stdMin) + "\n";
        Log.e(Thread.currentThread().getName(), out);
    }


    // printMeanAndErr...............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void printMeanAndErr() {
        
        ImageProcessor.getMean().copyTo(mFloatData);
        String[] mean = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            mean[i] = NumToString.sci(mFloatData[i]);
        }

        ImageProcessor.getStdErr().copyTo(mFloatData);
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

    // printStdDev...................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void printStdDev() {

        ImageProcessor.getStdDev().copyTo(mFloatData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mFloatData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Standard Deviation Rate ////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        printValues(title, values);
    }

    // printSignificance.................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void printSignificance() {
        ImageProcessor.getSignificance().copyTo(mFloatData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mFloatData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Significance ///////////////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        printValues(title, values);

        mFloatData = null;
        System.gc();
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // printValues....................................................................................
    /**
     * TODO: description, comments and logging
     * @param title bla
     * @param values bla
     */
    private static void printValues(@NonNull String title, @NonNull String[] values) {
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