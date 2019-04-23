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
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.ScriptC_PostProcessing;
import sci.crayfis.shramp.ScriptC_LiveProcessing;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.capture.CaptureController;
import sci.crayfis.shramp.util.NumToString;

/**
 * Public interface to the analysis (ImageProcessor) code
 * TODO: char is 16 bits in Java and 8 bits in RenderScript!  Double check stuff
 */
@TargetApi(21)
public abstract class AnalysisController {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // WAIT.........................................................................................
    // Dummy object for calling wait()
    private final static Object WAIT = new Object();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mRS..........................................................................................
    // System RenderScript object
    private static RenderScript mRS;

    // mUCharType...................................................................................
    // RenderScript Allocation unsigned char type [width x height pixels]
    private static Type mUCharType;

    // mUShortType..................................................................................
    // RenderScript Allocation unsigned short type [width x height pixels]
    private static Type mUShortType;

    // mUIntType....................................................................................
    // RenderScript Allocation unsigned int type [width x height pixels]
    private static Type mUIntType;

    // mFloatType...................................................................................
    // RenderScript Allocation float type [width x height pixels]
    private static Type mFloatType;

    // mDoubleType..................................................................................
    // RenderScript Allocation double type [width x height pixels]
    private static Type mDoubleType;

    // mSimpleLongType..............................................................................
    // RenderScript Allocation signed long type [1 x 1]
    private static Type mSimpleLongType;

    // mNpixels.....................................................................................
    // Total number of pixels [width * height pixels]
    private static int mNpixels;

    // mNeedsCalibration............................................................................
    // TODO: probably remove in the future, a switch for doing calibration
    private static boolean mNeedsCalibration;

    // mThresholdOffset.............................................................................
    // TODO: probably remove in the future, a fudge factor for controlling the significance rate
    private static double mThresholdOffset = 0.;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // initialize...................................................................................
    /**
     * Set up RenderScript things
     * @param activity Reference to main activity
     */
    public static void initialize(@NonNull Activity activity) {

        mRS = RenderScript.create(activity, RenderScript.ContextType.NORMAL,
                                            GlobalSettings.RENDER_SCRIPT_FLAGS);
        mRS.setPriority(GlobalSettings.RENDER_SCRIPT_PRIORITY);

        ScriptC_LiveProcessing liveProcessing = new ScriptC_LiveProcessing(mRS);
        ScriptC_PostProcessing postProcessing = new ScriptC_PostProcessing(mRS);
        ImageProcessor.setLiveProcessor(liveProcessing);
        ImageProcessor.setPostProcessor(postProcessing);

        Element ucharElement  = Element.U8(mRS);
        Element ushortElement = Element.U16(mRS);
        Element uintElement   = Element.U32(mRS);
        Element floatElement  = Element.F32(mRS);
        Element doubleElement = Element.F64(mRS);

        Element simpleLongElement = Element.I64(mRS);

        Size outputSize = CameraController.getOutputSize();
        if (outputSize == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Output size cannot be null");
            MasterController.quitSafely();
            return;
        }
        int width  = outputSize.getWidth();
        int height = outputSize.getHeight();
        mNpixels = width * height;

        ImageWrapper.setRowsCols(height, width);

        // TODO: remove
        PrintAllocations.setNpixels(mNpixels);

        mUCharType  = new Type.Builder(mRS, ucharElement ).setX(width).setY(height).create();
        mUShortType = new Type.Builder(mRS, ushortElement).setX(width).setY(height).create();
        mUIntType   = new Type.Builder(mRS, uintElement  ).setX(width).setY(height).create();
        mFloatType  = new Type.Builder(mRS, floatElement ).setX(width).setY(height).create();
        mDoubleType = new Type.Builder(mRS, doubleElement).setX(width).setY(height).create();

        mSimpleLongType = new Type.Builder(mRS, simpleLongElement).setX(1).setY(1).create();

        Integer outputFormat = CameraController.getOutputFormat();
        if (outputFormat == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Output format cannot be null");
            MasterController.quitSafely();
            return;
        }
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
                Log.e(Thread.currentThread().getName(), "Output format is neither YUV_420_888 or RAW_SENSOR");
                MasterController.quitSafely();
                return;
            }
        }

        // Must happen after ImageWrapper is set up (above)
        // TODO: maybe make it so it can be set at the same time?
        OutputWrapper.configure();

        // TODO: Check for existing calibrations
        // if none, set need calibration flag and load empty Allocations into statistics
        mNeedsCalibration = true;

        Allocation mean   = newFloatAllocation();
        Allocation stddev = newFloatAllocation();
        Allocation stderr = newFloatAllocation();
        Allocation mask   = newUCharAllocation();

        liveProcessing.forEach_zeroFloatAllocation(mean);
        liveProcessing.forEach_oneFloatAllocation(stddev);
        liveProcessing.forEach_zeroFloatAllocation(stderr);
        liveProcessing.forEach_oneCharAllocation(mask);

        ImageProcessor.setStatistics(mean, stddev, stderr, mask);

        ImageProcessor.setSignificanceAllocation(newFloatAllocation());
        ImageProcessor.setCountAboveThresholdAllocation(newSimpleLongAllocation());
        ImageProcessor.setAnomalousStdDevAllocation(newSimpleLongAllocation());
        ImageProcessor.disableSignificance();
        ImageProcessor.resetTotals();
    }

    // needsCalibration.............................................................................
    /**
     * @return True if calibration run is needed, false if calibrations were successfully loaded
     */
    @Contract(pure = true)
    public static boolean needsCalibration() {
        return mNeedsCalibration;
    }

    // enableSignificance...........................................................................
    /**
     * Enable live significance measurement: (pixel value - mean) / stddev
     */
    public static void enableSignificance() {
        ImageProcessor.enableSignificance();
    }

    // disableSignificance..........................................................................
    /**
     * Disable live significance measurement
     */
    public static void disableSignificance() {
        ImageProcessor.disableSignificance();
    }

    // isSignificanceEnabled........................................................................
    /**
     * @return True if significance is being computed, false if not
     */
    @Contract(pure = true)
    public static boolean isSignificanceEnabled() {
        return ImageProcessor.isSignificanceEnabled();
    }

    // setSignificanceThreshold.....................................................................
    /**
     * Figure out what the threshold should be for declaring a recorded pixel value significant
     * @param n_frames The number of frames that will be processed in this run
     */
    public static void setSignificanceThreshold(int n_frames) {
        double n_samples = (double) mNpixels * n_frames;
        double n_chanceAboveThreshold = 1.;

        double probabilityThreshold = n_chanceAboveThreshold / n_samples;

        double threshold = Math.sqrt(2.) * Erf.erfInv(1. - 2. * probabilityThreshold);

        // TODO: remove in the future
        threshold += mThresholdOffset;

        ImageProcessor.setSignificanceThreshold((float) threshold);
        Log.e(Thread.currentThread().getName(), "Significance threshold level: "
                + NumToString.decimal(threshold));
    }

    // isBusy.......................................................................................
    /**
     * @return True if image processor is working, false if in idle
     */
    public static boolean isBusy() {
        return ImageProcessor.isBusy();
    }

    // resetRunningTotals...........................................................................
    /**
     * Reset running totals in ImageProcessor
     */
    public static void resetRunningTotals() {
        ImageProcessor.resetTotals();
    }

    // runStatistics................................................................................
    /**
     * Post process a run and compute run statistics
     */
    public static void runStatistics(String filename) {
        synchronized (WAIT) {

            DataQueue.purge();
            while (!DataQueue.isEmpty() || ImageProcessor.isBusy()) {
                try {
                    Log.e(Thread.currentThread().getName(), "Waiting for queue to empty/processor to finish before running statistics");
                    DataQueue.purge();
                    WAIT.wait(GlobalSettings.DEFAULT_WAIT_MS);
                }
                catch (InterruptedException e) {
                    // TODO: error
                }
            }

            ImageProcessor.runStatistics(filename);

            while (ImageProcessor.isBusy()) {
                try {
                    Log.e(Thread.currentThread().getName(), "Waiting for processor to finish with statistics");
                    WAIT.wait(GlobalSettings.DEFAULT_WAIT_MS);
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
     * @return Empty unsigned char Allocation [width x height pixels]
     */
    @NonNull
    static Allocation newUCharAllocation() {
        return Allocation.createTyped(mRS, mUCharType, Allocation.USAGE_SCRIPT);
    }

    // newUShortAllocation..........................................................................
    /**
     * @return Empty unsigned short Allocation [width x height pixels]
     */
    @NonNull
    static Allocation newUShortAllocation() {
        return Allocation.createTyped(mRS, mUShortType, Allocation.USAGE_SCRIPT);
    }

    // newUIntAllocation............................................................................
    /**
     * @return Empty unsigned integer Allocation [width x height pixels]
     */
    static Allocation newUIntAllocation() {
        return Allocation.createTyped(mRS, mUIntType, Allocation.USAGE_SCRIPT);
    }

    // newFloatAllocation...........................................................................
    /**
     * @return Empty float Allocation [width x height pixels]
     */
    @NonNull
    static Allocation newFloatAllocation() {
        return Allocation.createTyped(mRS, mFloatType, Allocation.USAGE_SCRIPT);
    }

    // newDoubleAllocation..........................................................................
    /**
     * @return Empty double Allocation [width x height pixels]
     */
    @NonNull
    static Allocation newDoubleAllocation() {
        return Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
    }

    // newSimpleLongAllocation
    /**
     * @return Empty signed long Allocation [1 x 1]
     */
    static Allocation newSimpleLongAllocation() {
        return Allocation.createTyped(mRS, mSimpleLongType, Allocation.USAGE_SCRIPT);
    }

    // destroyAllocation............................................................................
    /**
     * TODO: might not be needed, still not completely sure about freeing Allocations
     * @param allocation Allocation to be destroyed
     */
    static void destroyAllocation(@Nullable Allocation allocation) {
        if (allocation == null) {
            return;
        }
        allocation.destroy();
        allocation = null;
    }

    /**
     * TODO: remove in the future, fudge-factor for controlling significance rate
     */
    //static void increaseSignificanceThreshold() {
        //mThresholdOffset += GlobalSettings.THRESHOLD_STEP;
        //CaptureController.resetSession();
    //}

}