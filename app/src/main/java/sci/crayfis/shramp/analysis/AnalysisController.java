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

import org.apache.commons.math3.special.Erf;
import org.jetbrains.annotations.Contract;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_PostProcessing;
import sci.crayfis.shramp.ScriptC_LiveProcessing;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.capture.CaptureController;
import sci.crayfis.shramp.util.NumToString;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public abstract class AnalysisController {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // WAIT.........................................................................................
    // TODO: description
    private final static Object WAIT = new Object();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mRS..........................................................................................
    // TODO: description
    private static RenderScript mRS;

    // mUCharType...................................................................................
    // TODO: description
    private static Type mUCharType;

    // mUShortType..................................................................................
    // TODO: description
    private static Type mUShortType;

    // mUIntType....................................................................................
    // TODO: description
    private static Type mUIntType;

    // mFloatType...................................................................................
    // TODO: description
    private static Type mFloatType;

    // mDoubleType..................................................................................
    // TODO: description
    private static Type mDoubleType;

    // mSimpleLongType..............................................................................
    // TODO: description
    private static Type mSimpleLongType;

    // mNpixels.....................................................................................
    // TODO: description
    private static int mNpixels;

    private static boolean mNeedsCalibration;

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

        Element ucharElement  = Element.U8(mRS);
        Element ushortElement = Element.U16(mRS);
        Element uintElement   = Element.U32(mRS);
        Element floatElement  = Element.F32(mRS);
        Element doubleElement = Element.F64(mRS);

        Element simpleLongElement = Element.I64(mRS);

        Size outputSize = CameraController.getOutputSize();
        assert outputSize != null;
        int width  = outputSize.getWidth();
        int height = outputSize.getHeight();
        mNpixels = width * height;

        ImageWrapper.setNpixels(height, width);
        PrintAndSave.setNpixels(mNpixels);

        mUCharType  = new Type.Builder(mRS, ucharElement ).setX(width).setY(height).create();
        mUShortType = new Type.Builder(mRS, ushortElement).setX(width).setY(height).create();
        mUIntType   = new Type.Builder(mRS, uintElement  ).setX(width).setY(height).create();
        mFloatType  = new Type.Builder(mRS, floatElement ).setX(width).setY(height).create();
        mDoubleType = new Type.Builder(mRS, doubleElement).setX(width).setY(height).create();

        mSimpleLongType = new Type.Builder(mRS, simpleLongElement).setX(1).setY(1).create();

        Integer outputFormat = CameraController.getOutputFormat();
        assert outputFormat != null;
        switch (outputFormat) {
            case (ImageFormat.YUV_420_888): {
                ImageWrapper.setAs8bitData();
                ImageProcessor.setImageAllocation(newUCharAllocation());
                break;
            }
            case (ImageFormat.RAW_SENSOR): {
                ImageWrapper.setAs16bitData();
                ImageProcessor.setImageAllocation(newUShortAllocation());
                break;
            }

            default: {
                // TODO: error
            }
        }

        // Must happen after ImageWrapper is set up (above)
        OutputWrapper.configure();

        // Check for existing calibrations
        // if none, set need calibration flag
        mNeedsCalibration = true;
        ImageProcessor.setStatistics(newFloatAllocation(), newFloatAllocation(), newFloatAllocation());

        ImageProcessor.setSignificanceAllocation(newFloatAllocation());
        ImageProcessor.setCountAboveThresholdAllocation(newSimpleLongAllocation());
        ImageProcessor.setAnomalousStdDevAllocation(newSimpleLongAllocation());
        ImageProcessor.disableSignificance();
        ImageProcessor.resetTotals();
    }

    public static boolean needsCalibration() {
        return mNeedsCalibration;
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
    @Contract(pure = true)
    public static boolean isSignificanceEnabled() {
        return ImageProcessor.isSignificanceEnabled();
    }

    // setSignificanceThreshold.....................................................................
    /**
     * TODO: description, comments and logging
     * @param n_frames bla
     */
    public static void setSignificanceThreshold(int n_frames) {
        double n_samples = (double) mNpixels * n_frames;
        double n_chanceAboveThreshold = 1.;

        double probabilityThreshold = n_chanceAboveThreshold / n_samples;

        double threshold = Math.sqrt(2.) * Erf.erfInv(1. - 2. * probabilityThreshold);
        threshold += mThresholdOffset;

        ImageProcessor.setSignificanceThreshold((float) threshold);
        Log.e(Thread.currentThread().getName(), "__________Threshold: " + NumToString.decimal(threshold));
    }

    private static double mThresholdOffset = 0.;

    static void increaseSignificanceThreshold() {
        mThresholdOffset += GlobalSettings.THRESHOLD_STEP;
        CaptureController.resetCaptureSession();
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
    public static void runStatistics(String filename) {
        synchronized (WAIT) {

            DataQueue.purge();
            while (!DataQueue.isEmpty() || ImageProcessor.isBusy()) {
                try {
                    WAIT.wait(GlobalSettings.DEFAULT_WAIT_MS);
                    Log.e(Thread.currentThread().getName(), "Waiting for queue to empty/processor to finish");
                    DataQueue.purge();
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }

            ImageProcessor.runStatistics(filename);

            while (ImageProcessor.isBusy()) {
                try {
                    WAIT.wait(GlobalSettings.DEFAULT_WAIT_MS);
                    Log.e(Thread.currentThread().getName(), "Waiting for processor to finish with statistics");
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }
        }
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // newUCharAllocation...........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newUCharAllocation() {
        return Allocation.createTyped(mRS, mUCharType, Allocation.USAGE_SCRIPT);
    }

    // newUShortAllocation..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newUShortAllocation() {
        return Allocation.createTyped(mRS, mUShortType, Allocation.USAGE_SCRIPT);
    }

    // newUIntAllocation............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    static Allocation newUIntAllocation() {
        return Allocation.createTyped(mRS, mUIntType, Allocation.USAGE_SCRIPT);
    }

    // newFloatAllocation...........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newFloatAllocation() {
        return Allocation.createTyped(mRS, mFloatType, Allocation.USAGE_SCRIPT);
    }

    // newDoubleAllocation..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    static Allocation newDoubleAllocation() {
        return Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
    }

    // newSimpleLongAllocation
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    static Allocation newSimpleLongAllocation() {
        return Allocation.createTyped(mRS, mSimpleLongType, Allocation.USAGE_SCRIPT);
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

}