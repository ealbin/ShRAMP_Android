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
 * @updated: 3 May 2019
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
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * Public interface to the analysis (ImageProcessor) code
 * TODO: char is 16 bits in Java and 8 bits in RenderScript!  Double check stuff.. checks out
 * TODO: triple check it
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

    // mLiveProcessing..............................................................................
    // Reference to LiveProcessing.rs RenderScript
    private static ScriptC_LiveProcessing mLiveProcessing;

    // mPostProcessing..............................................................................
    // Reference to PostProcessing.rs RenderScript
    private static ScriptC_PostProcessing mPostProcessing;

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

        mLiveProcessing = new ScriptC_LiveProcessing(mRS);
        mPostProcessing = new ScriptC_PostProcessing(mRS);
        ImageProcessor.setLiveProcessor(mLiveProcessing);
        ImageProcessor.setPostProcessor(mPostProcessing);

        Element ucharElement  = Element.U8(mRS);
        Element ushortElement = Element.U16(mRS);
        Element uintElement   = Element.U32(mRS);
        Element ulongElement  = Element.U64(mRS);
        Element floatElement  = Element.F32(mRS);
        Element doubleElement = Element.F64(mRS);

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

        mSimpleLongType = new Type.Builder(mRS, ulongElement).setX(1).setY(1).create();

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

        importLatestCalibration();

        ImageProcessor.setSignificanceAllocation(newFloatAllocation());
        ImageProcessor.setCountAboveThresholdAllocation(newSimpleLongAllocation());
        ImageProcessor.setAnomalousStdDevAllocation(newSimpleLongAllocation());
        ImageProcessor.disableSignificance();
        ImageProcessor.resetTotals();
    }

    // importLatestCalibration......................................................................
    /**
     * Check for existing calibration data and import it
     */
    public static void importLatestCalibration() {

        String meanPath   = StorageMedia.findRecentCalibration("mean",   GlobalSettings.MEAN_FILE);
        String stddevPath = StorageMedia.findRecentCalibration("stddev", GlobalSettings.STDDEV_FILE);
        String stderrPath = StorageMedia.findRecentCalibration("stderr", GlobalSettings.STDERR_FILE);
        String maskPath   = StorageMedia.findRecentCalibration("mask",   GlobalSettings.MASK_FILE);

        Allocation mean   = newFloatAllocation();
        Allocation stddev = newFloatAllocation();
        Allocation stderr = newFloatAllocation();
        Allocation mask   = newUCharAllocation();

        boolean hasMean = false;
        if (meanPath != null) {
            mean.copyFrom( new InputWrapper(meanPath).getStatisticsData() );
            hasMean = true;
        }
        else {
            mLiveProcessing.forEach_zeroFloatAllocation(mean);
        }

        boolean hasStdDev = false;
        if (stddevPath != null) {
            stddev.copyFrom( new InputWrapper(stddevPath).getStatisticsData() );
            hasStdDev = true;
        }
        else {
            mLiveProcessing.forEach_oneFloatAllocation(stddev);
        }

        boolean hasStdErr = false;
        if (stderrPath != null) {
            stderr.copyFrom( new InputWrapper(stderrPath).getStatisticsData() );
            hasStdErr = true;
        }
        else {
            mLiveProcessing.forEach_zeroFloatAllocation(stderr);
        }

        boolean hasMask = false;
        if (maskPath != null) {
            mask.copyFrom( new InputWrapper(maskPath).getMaskData() );
            hasMask = true;
        }
        else {
            mLiveProcessing.forEach_oneCharAllocation(mask);
        }

        // Doesn't formally need stderr
        mNeedsCalibration = !(hasMean && hasStdDev && hasMask);
        ImageProcessor.setStatistics(mean, stddev, stderr, mask);
        ImageProcessor.resetTotals();
    }

    // makePixelMask................................................................................
    /**
     * Loads most recent calibration files from ShRAMP/Calibrations, and generates/saves a pixel mask
     * of what pixels should be used in significance computation.
     * Also computes/saves an estimate for the mean, stddev and stderr at 10 fps and 35 Celsius.
     * Note: assumes "hot" is hotter than "cold" and "fast" is faster than "slow"
     * TODO: return true if successful, false if not
     */
    public static void makePixelMask() {
        ApplyCuts.makePixelMask();
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

        // TODO: threshold still a work in progress
        //double threshold = Math.sqrt(2.) * Erf.erfInv(1. - 2. * probabilityThreshold);
        double threshold = Math.sqrt(2.) * Erf.erfInv(1. - probabilityThreshold) + 1.;

        // TODO: remove in the future
        //threshold += mThresholdOffset;

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