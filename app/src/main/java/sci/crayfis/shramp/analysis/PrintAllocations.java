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
 * @updated: 24 April 2019
 */

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.util.Log;

import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: remove this whole file once I'm confident everything is working as it should
 * Used for debugging, i.e. displaying contents of Allocations
 */
@TargetApi(21)
abstract public class PrintAllocations {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PEEK_SIZE....................................................................................
    // Number of elements to print
    private static final int PEEK_SIZE = 100;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mNpixels.....................................................................................
    // Number of pixels (width * height)
    private static Integer mNpixels;
    
    // mFloatData...................................................................................
    // Data holder for Allocations to dump their content into
    private static float[] mFloatData;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // printMaxMin..................................................................................
    /**
     * Print maximum and minimum of mean and standard deviation Allocations
     */
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

    // printMeanAndErr..............................................................................
    /**
     * Print PEEK_SIZE worth of elements from mean and standard error Allocations
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

    // printStdDev..................................................................................
    /**
     * Print PEEK_SIZE worth of elements from standard deviation Allocation
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

    // printSignificance............................................................................
    /**
     * Print PEEK_SIZE worth of significance Allocation
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

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setRowsCols...................................................................................
    /**
     * @param npixels Number of pixels of sensor (width * height)
     */
    static void setNpixels(int npixels) {
        mNpixels = npixels;

        // Because this is about 25-40 MB, initialize it once and keep it around to avoid sudden
        // resource fluctuations while taking data
        mFloatData = new float[mNpixels];
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // printValues....................................................................................
    /**
     * Helper for printing values
     * @param title Title of elements to be printed
     * @param values Values to be printed
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