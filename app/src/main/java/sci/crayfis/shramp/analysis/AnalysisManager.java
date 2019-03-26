/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_PostProcessing;
import sci.crayfis.shramp.ScriptC_LiveProcessing;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.capture.CaptureManager;
import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class AnalysisManager {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // WAIT.........................................................................................
    // TODO: description
    private final static Object WAIT = new Object();

    // PEEK_SIZE....................................................................................
    // TODO: description
    private static final int PEEK_SIZE = 100;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mRS..........................................................................................
    // TODO: description
    private static RenderScript mRS;

    // mShortType...................................................................................
    // TODO: description
    private static Type mCharType;

    // mShortType...................................................................................
    // TODO: description
    private static Type mShortType;

    // mFloatType...................................................................................
    // TODO: description
    private static Type mFloatType;

    // mFloatData...................................................................................
    // TODO: description
    private static float[] mFloatData;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // initialize...................................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     */
    public static void initialize(@NonNull Activity activity) {

        mRS = RenderScript.create(activity, RenderScript.ContextType.NORMAL,
                                            GlobalSettings.RENDER_SCRIPT_FLAGS);
        mRS.setPriority(GlobalSettings.RENDER_SCRIPT_PRIORITY);

        ImageProcessor.setLiveProcessor(new ScriptC_LiveProcessing(mRS));
        ImageProcessor.setPostProcessor(new ScriptC_PostProcessing(mRS));

        Element charElement   = Element.U8(mRS);
        Element shortElement  = Element.U16(mRS);
        Element floatElement  = Element.F32(mRS);

        Size outputSize = CameraController.getOutputSize();
        assert outputSize != null;
        int width  = outputSize.getWidth();
        int height = outputSize.getHeight();
        ImageWrapper.setNpixels(width * height);

        mFloatData = new float[width * height];

        mCharType  = new Type.Builder(mRS, charElement ).setX(width).setY(height).create();
        mShortType = new Type.Builder(mRS, shortElement).setX(width).setY(height).create();
        mFloatType = new Type.Builder(mRS, floatElement).setX(width).setY(height).create();

        Integer outputFormat = CameraController.getOutputFormat();
        assert outputFormat != null;
        switch (outputFormat) {
            case (ImageFormat.YUV_420_888): {
                ImageWrapper.setAs8bitData();
                ImageProcessor.setImageAllocation(newCharAllocation());
                break;
            }
            case (ImageFormat.RAW_SENSOR): {
                ImageWrapper.setAs16bitData();
                ImageProcessor.setImageAllocation(newShortAllocation());
                break;
            }

            default: {
                // TODO: error
            }
        }

        ImageProcessor.setStatistics(newFloatAllocation(), newFloatAllocation(), newFloatAllocation());
        ImageProcessor.setSignificanceAllocation(newFloatAllocation());
        ImageProcessor.disableSignificance();
        ImageProcessor.resetTotals();
    }

    // enableSignificance...........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void enableSignificance() {
        ImageProcessor.enableSignificance();
    }

    // disableSignificance..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void disableSignificance() {
        ImageProcessor.disableSignificance();
    }

    // isSignificanceEnabled........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static boolean isSignificanceEnabled() {
        return ImageProcessor.isSignificanceEnabled();
    }

    // isBusy.......................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static boolean isBusy() {
        return ImageProcessor.isBusy();
    }

    // resetRunningTotals...........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void resetRunningTotals() {
        ImageProcessor.resetTotals();
    }

    // runStatistics................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void runStatistics() {
        synchronized (WAIT) {

            DataQueue.purge();
            while (!DataQueue.isEmpty() || ImageProcessor.isBusy()) {
                try {
                    WAIT.wait(3 * CaptureManager.getTargetFrameNanos() / 1000 / 1000);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }

            ImageProcessor.runStatistics();

            while (ImageProcessor.isBusy()) {
                try {
                    WAIT.wait(3 * CaptureManager.getTargetFrameNanos() / 1000 / 1000);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }
        }
    }

    // peekMeanAndErr...............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void peekMeanAndErr() {
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

    // peekStdDev...................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void peekStdDev() {
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

    // peekExposureValue............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void peekExposureValue() {
        ImageProcessor.getExposureValueSum().copyTo(mFloatData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mFloatData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Exposure Value Sum /////////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);
    }

    // peekExposureValue2...........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void peekExposureValue2() {
        ImageProcessor.getExposureValue2Sum().copyTo(mFloatData);
        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mFloatData[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// Exposure Value^2 Sum ///////////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);
    }

    // peekSignificance.............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void peekSignificance() {
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
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // newCharAllocation............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newCharAllocation() {
        return Allocation.createTyped(mRS, mCharType, Allocation.USAGE_SCRIPT);
    }

    // newShortAllocation...........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newShortAllocation() {
        return Allocation.createTyped(mRS, mShortType, Allocation.USAGE_SCRIPT);
    }

    // newFloatAllocation
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newFloatAllocation() {
        return Allocation.createTyped(mRS, mFloatType, Allocation.USAGE_SCRIPT);
    }

    // resetAllocation..............................................................................
    /**
     * TODO: descripion, comments and logging
     * @param allocation bla
     */
    static void resetAllocation(@Nullable Allocation allocation) {
        if (allocation == null) {
            return;
        }
        allocation.destroy();
        allocation = null;
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
            int tmp = NumToString.sci(mFloatData[i]).length();
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