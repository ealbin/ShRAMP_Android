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
import android.renderscript.Allocation;
import android.util.Log;
import android.util.Range;

import sci.crayfis.shramp.GlobalSettings;

import sci.crayfis.shramp.camera2.capture.CaptureConfiguration;
import sci.crayfis.shramp.util.Datestamp;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * Given calibration files, applies cuts to determine trustworthy pixels.
 * This could be performed in RenderScript for a substantial performance boost, but as doing so would
 * be quite cumbersome and the app can afford to take a little time on this calculation without
 * sacrificing data capture abilities, it's done in Java for simplicity / ease in changing.
 * TODO: fine tune cuts
 * TODO: make cut return successful or fail
 * TODO: (PRIORITY) update InputWrapper/this code to process bytes from files instead of whole file
 */
@TargetApi(21)
abstract class ApplyCuts {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // FPS..........................................................................................
    // When generating estimated values for statistics, use this frames-per-second
    // TODO: consider making the estimates based on 10 fps (raw) and 15-20 fps (yuv)?
    private static final float FPS = 10.f;

    // TEMPERATURE..................................................................................
    // When generating estimates values for statistics, use this temperature [Celsius]
    private static final float TEMPERATURE = 35.f;

    // HISTOGRAM_BOUNDS
    // Low and high bound for histograms (pixel value)
    private static final Range<Integer> HISTOGRAM_BOUNDS = new Range<Integer>(-100, 100);

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Allocations..................................................................................
    // For transferring the findings of this class over to ImageProcessor
    private static Allocation mMeanAlloc;
    private static Allocation mStdDevAlloc;
    private static Allocation mStdErrAlloc;
    private static Allocation mMaskAlloc;

    // mMask........................................................................................
    // Array to hold the masking bits (1 or 0) while cuts are being made
    private static byte[] mMask;

    // mCutStatistic................................................................................
    // A general slush array for pixel-wise statistics used for making cuts
    private static float[] mCutStatistic;

    // mTotalMeanFrames.............................................................................
    // The total number of frames used across all "mean" files
    private static Long mTotalMeanFrames;

    // mTotalStdDevFrames...........................................................................
    // The total number of frames used across all "stddev" files
    private static Long mTotalStdDevFrames;

    // mMaxPixelValue...............................................................................
    // The maximum value a pixel can have (255 for 8-bit YUV, 1023 for 16-bit RAW)
    private static int mMaxPixelValue;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makePixelMask................................................................................
    /**
     * Loads most recent calibration files from ShRAMP/Calibrations, and generates/saves a pixel mask
     * of what pixels should be used in significance computation.
     * Also computes/saves an estimate for the mean, stddev and stderr at FPS fps and TEMPERATURE Celsius
     * Note: assumes "hot" is hotter than "cold" and "fast" is faster than "slow"
     */
    static void makePixelMask() {

        // TODO: possibly a bug if settings change between writes / runs
        if (OutputWrapper.mBitsPerPixel == 8) {
            mMaxPixelValue = 255;
        } else { // OutputWrapper.mBitsPerPixel == 16
            mMaxPixelValue = 1023;
        }

        // Apply cuts
        if (!applyMeanCuts()) {
            return;
        }
        System.gc();

        if (!applyStdDevCuts()) {
            return;
        }
        System.gc();

        HeapMemory.logAvailableMiB();

        // Update statistics in ImageProcessor
        ImageProcessor.setStatistics(mMeanAlloc, mStdDevAlloc, mStdErrAlloc, mMaskAlloc);

        // Save statistics to disk
        String date = Datestamp.getDate();
        StorageMedia.writeCalibration(new OutputWrapper("mean_"   + date + GlobalSettings.MEAN_FILE,   mMeanAlloc,   mTotalMeanFrames,  35.f));
        StorageMedia.writeCalibration(new OutputWrapper("stddev_" + date + GlobalSettings.STDDEV_FILE, mStdDevAlloc, mTotalStdDevFrames,35.f));
        StorageMedia.writeCalibration(new OutputWrapper("stderr_" + date + GlobalSettings.STDERR_FILE, mStdErrAlloc, mTotalStdDevFrames,35.f));
        StorageMedia.writeCalibration(new OutputWrapper("mask_"   + date + GlobalSettings.MASK_FILE,   mMask));
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Apply cuts based on "mean" files, e.g. Temperature and Exposure-based cuts
     * @return True if cuts were applied, false if cuts could not be made
     */
    private static boolean applyMeanCuts() {

        HeapMemory.logAvailableMiB();

        String coldFastMeanPath = StorageMedia.findRecentCalibration("cold_fast", GlobalSettings.MEAN_FILE);
        String coldSlowMeanPath = StorageMedia.findRecentCalibration("cold_slow", GlobalSettings.MEAN_FILE);
        String hotFastMeanPath  = StorageMedia.findRecentCalibration("hot_fast",  GlobalSettings.MEAN_FILE);
        String hotSlowMeanPath  = StorageMedia.findRecentCalibration("hot_slow",  GlobalSettings.MEAN_FILE);

        boolean allFilesPresent = true;

        if (coldFastMeanPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing cold-fast-mean calibration file, cannot continue");
            allFilesPresent = false;
        }
        if (coldSlowMeanPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing cold-slow-mean calibration file, cannot continue");
            allFilesPresent = false;
        }
        if (hotFastMeanPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing hot-fast-mean calibration file, cannot continue");
            allFilesPresent = false;
        }
        if (hotSlowMeanPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing hot-slow-mean calibration file, cannot continue");
            allFilesPresent = false;
        }

        if (!allFilesPresent) {
            return false;
        }

        // Initialize mMask
        //=================
        int npixels = ImageWrapper.getNpixels();
        mMask = new byte[npixels];
        for (int i = 0; i < npixels; i++) {
            mMask[i] = 1;
        }

        // Reading in 4 calibration files is going to take ~200 MB of heap memory
        if (!HeapMemory.isMemoryAmple()) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Not enough memory to apply cuts");
            HeapMemory.logAvailableMiB();
            return false;
        }

        // Please don't run out of memory, please don't run out of memory, please don't run out of..
        HeapMemory.logAvailableMiB();
        InputWrapper coldFast = new InputWrapper(coldFastMeanPath);
        HeapMemory.logAvailableMiB();
        InputWrapper coldSlow = new InputWrapper(coldSlowMeanPath);
        HeapMemory.logAvailableMiB();
        InputWrapper hotFast = new InputWrapper(hotFastMeanPath);
        HeapMemory.logAvailableMiB();
        InputWrapper hotSlow = new InputWrapper(hotSlowMeanPath);
        HeapMemory.logAvailableMiB();

        if (HeapMemory.isMemoryLow()) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Not enough memory to apply cuts");
            HeapMemory.logAvailableMiB();
            coldFast = null;
            coldSlow = null;
            hotFast = null;
            hotSlow = null;
            System.gc();
            return false;
        }

        // Checks
        //=======

        float[] cf = coldFast.getStatisticsData();
        float[] cs = coldSlow.getStatisticsData();
        float[] hf = hotFast.getStatisticsData();
        float[] hs = hotSlow.getStatisticsData();

        if (cf == null || cs == null || hf == null || hs == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing statistical data, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast = null;
            hotSlow = null;
            System.gc();
            return false;
        }

        Long coldFastFrames = coldFast.getNframes();
        Long coldSlowFrames = coldSlow.getNframes();
        Long hotFastFrames  = hotFast.getNframes();
        Long hotSlowFrames  = hotSlow.getNframes();

        if (coldFastFrames == null || coldSlowFrames == null || hotFastFrames == null || hotSlowFrames == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing number of frames, cannot continue");
            return false;
        }

        mTotalMeanFrames = coldFastFrames + coldSlowFrames + hotFastFrames + hotSlowFrames;

        mCutStatistic = new float[npixels];
        Histogram histogram = new Histogram(HISTOGRAM_BOUNDS);
        HeapMemory.logAvailableMiB();

        // Temperature-based cut
        ////////////////////////////////////////////////////////////////////////////////////////////
        Log.e(Thread.currentThread().getName(), "Applying temperature-based cut..");

        for (int i = 0; i < npixels; i++) {
            mCutStatistic[i] = mMaxPixelValue * ((hf[i] + hs[i]) - (cf[i] + cs[i])) / 2.f;
            histogram.add(mCutStatistic[i]);
        }

        double maxValue = histogram.getBinCenter(histogram.getMaxBin());
        double stddev = histogram.getMaxStdDev();
        double upperLimit = maxValue + Math.max(1., 3. * stddev);
        double lowerLimit = maxValue - Math.max(1., 3. * stddev);

        String status = "Max value: " + NumToString.decimal(maxValue)
                + ", Max std dev: " + NumToString.decimal(stddev)
                + ", upper/lower limit: " + NumToString.decimal(upperLimit)
                + "/" + NumToString.decimal(lowerLimit);
        Log.e(Thread.currentThread().getName(), status);

        String filename = "hot-cold_" + Datestamp.getDate() + GlobalSettings.HISTOGRAM_FILE;
        StorageMedia.writeCalibration(new OutputWrapper(filename, histogram,
                                        new Range<Float>((float) lowerLimit, (float) upperLimit)));

        int kept = 0;
        for (int i = 0; i < npixels; i++) {
            float val = mCutStatistic[i];
            if (val < lowerLimit || val > upperLimit) {
                mMask[i] = 0;
            } else {
                kept++;
            }
        }

        String efficiency = NumToString.number(100. * kept / (float) npixels);
        String cut = "cut " + NumToString.number(npixels - kept) + " out of " + NumToString.number(npixels);
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\tTemperature cut efficiency: " + cut + " = " + efficiency + "%\n ");

        // Exposure-based cut
        ////////////////////////////////////////////////////////////////////////////////////////////
        Log.e(Thread.currentThread().getName(), "Applying exposure-based cut..");

        histogram.reset();
        for (int i = 0; i < npixels; i++) {
            mCutStatistic[i] = mMaxPixelValue * ((hs[i] + cs[i]) - (hf[i] + cf[i])) / 2.f;
            histogram.add(mCutStatistic[i]);
        }

        maxValue = histogram.getBinCenter(histogram.getMaxBin());
        stddev = histogram.getMaxStdDev();
        upperLimit = maxValue + Math.max(1., 3. * stddev);
        lowerLimit = maxValue - Math.max(1., 3. * stddev);

        status = "Max value: " + NumToString.decimal(maxValue)
                + ", Max std dev: " + NumToString.decimal(stddev)
                + ", upper/lower limit: " + NumToString.decimal(upperLimit)
                + "/" + NumToString.decimal(lowerLimit);
        Log.e(Thread.currentThread().getName(), status);

        filename = "slow-fast_" + Datestamp.getDate() + GlobalSettings.HISTOGRAM_FILE;
        StorageMedia.writeCalibration(new OutputWrapper(filename, histogram,
                                        new Range<Float>((float) lowerLimit, (float) upperLimit)));

        kept = 0;
        for (int i = 0; i < npixels; i++) {
            float val = mCutStatistic[i];
            if (val < lowerLimit || val > upperLimit) {
                mMask[i] = 0;
            } else {
                kept++;
            }
        }

        HeapMemory.logAvailableMiB();
        efficiency = NumToString.number(100. * kept / (float) npixels);
        cut = "cut " + NumToString.number(npixels - kept) + " out of " + NumToString.number(npixels);
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\tExposure cut efficiency: " + cut + " = " + efficiency + "%\n ");

        // Estimate the mean for FPS fps at TEMPERATURE deg Celsius
        // coordinate system:
        //      x-axis:  temperature (cold to hot)
        //      y-axis:  exposure (short to long)
        ////////////////////////////////////////////////////////////////////////////////////////////

        Log.e(Thread.currentThread().getName(), "Estimating mean value for " + NumToString.number(FPS)
                + " fps at " + NumToString.number(TEMPERATURE) + " Celsius ..");

        Float coldFastTemp = coldFast.getTemperature();
        Float coldSlowTemp = coldSlow.getTemperature();
        Float hotFastTemp = hotFast.getTemperature();
        Float hotSlowTemp = hotSlow.getTemperature();

        if (coldFastTemp == null || coldSlowTemp == null || hotFastTemp == null || hotSlowTemp == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "At least one temperature is null, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast = null;
            hotSlow = null;
            System.gc();
            return false;
        }

        float coldTemp = (coldFastTemp + coldSlowTemp) / 2.f;
        float hotTemp = (hotFastTemp + hotSlowTemp) / 2.f;
        float tempRange = hotTemp - coldTemp;
        float temp = TEMPERATURE;
        float x = (temp - coldTemp) / tempRange;

        Long coldFastExp = CaptureConfiguration.EXPOSURE_BOUNDS.getLower();
        Long coldSlowExp = CaptureConfiguration.EXPOSURE_BOUNDS.getUpper();
        Long hotFastExp  = CaptureConfiguration.EXPOSURE_BOUNDS.getLower();
        Long hotSlowExp  = CaptureConfiguration.EXPOSURE_BOUNDS.getUpper();

        float shortExp = (coldFastExp + hotFastExp) / 2.f;
        float longExp = (coldSlowExp + hotSlowExp) / 2.f;
        float expRange = longExp - shortExp;
        float exp = (float) 1e9 / FPS;
        float y = (exp - shortExp) / expRange;

        for (int i = 0; i < npixels; i++) {
            float f00 = cf[i];
            float f10 = hf[i];
            float f01 = cs[i];
            float f11 = hs[i];

            mCutStatistic[i] = f00 * (1.f - x) * (1.f - y) + f10 * x * (1.f - y) + f01 * (1.f - x) * y + f11 * x * y;
        }

        // Store in allocation
        mMeanAlloc = AnalysisController.newFloatAllocation();
        mMeanAlloc.copyFrom(mCutStatistic);

        return true;
    }

    /**
     * Apply cuts based on "stddev" files, e.g. Standard Deviation-based cuts
     * @return True if cuts were applied, false if cuts could not be made
     */
    private static boolean applyStdDevCuts() {

        HeapMemory.logAvailableMiB();

        String coldFastStdDevPath = StorageMedia.findRecentCalibration("cold_fast", GlobalSettings.STDDEV_FILE);
        String coldSlowStdDevPath = StorageMedia.findRecentCalibration("cold_slow", GlobalSettings.STDDEV_FILE);
        String hotFastStdDevPath  = StorageMedia.findRecentCalibration("hot_fast",  GlobalSettings.STDDEV_FILE);
        String hotSlowStdDevPath  = StorageMedia.findRecentCalibration("hot_slow",  GlobalSettings.STDDEV_FILE);

        boolean allFilesPresent = true;

        if (coldFastStdDevPath== null) {
            Log.e(Thread.currentThread().getName(), "Missing cold-fast-stddev calibration file, cannot continue");
            allFilesPresent = false;
        }
        if (coldSlowStdDevPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing cold-slow-stddev calibration file, cannot continue");
            allFilesPresent = false;
        }
        if (hotFastStdDevPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing hot-fast-stddev calibration file, cannot continue");
            allFilesPresent = false;
        }
        if (hotSlowStdDevPath == null) {
            Log.e(Thread.currentThread().getName(), "Missing hot-slow-stddev calibration file, cannot continue");
            allFilesPresent = false;
        }

        if (!allFilesPresent) {
            return false;
        }

        // Please don't run out of memory, please don't run out of memory, please don't run out of..
        InputWrapper coldFast = new InputWrapper(coldFastStdDevPath);
        HeapMemory.logAvailableMiB();
        InputWrapper coldSlow = new InputWrapper(coldSlowStdDevPath);
        HeapMemory.logAvailableMiB();
        InputWrapper hotFast  = new InputWrapper(hotFastStdDevPath);
        HeapMemory.logAvailableMiB();
        InputWrapper hotSlow  = new InputWrapper(hotSlowStdDevPath);
        HeapMemory.logAvailableMiB();

        float[] cf = coldFast.getStatisticsData();
        float[] cs = coldSlow.getStatisticsData();
        float[] hf = hotFast.getStatisticsData();
        float[] hs = hotSlow.getStatisticsData();

        HeapMemory.logAvailableMiB();

        if (cf == null || cs == null || hf == null || hs == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing statistical data, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return false;
        }

        int npixels = ImageWrapper.getNpixels();

        // Standard Deviation-based cut
        ////////////////////////////////////////////////////////////////////////////////////////////

        Log.e(Thread.currentThread().getName(), "Applying standard deviation-based cut..");
        Histogram histogram = new Histogram(HISTOGRAM_BOUNDS);

        int kept = 0;
        for (int i = 0; i < npixels; i++) {
            float val = (float) Math.sqrt(hs[i]*hs[i]+ hf[i]*hf[i] + cs[i]*cs[i] + cf[i]*cf[i]) / 4.f;
            histogram.add(mMaxPixelValue * val);
            if (val > 0.03f) {
                mMask[i] = 0;
            }
            else {
                kept++;
            }
        }

        String filename = "stddev_" + Datestamp.getDate() + GlobalSettings.HISTOGRAM_FILE;
        StorageMedia.writeCalibration(new OutputWrapper(filename, histogram,
                                        new Range<Float>(0.f, 0.03f * mMaxPixelValue)));

        HeapMemory.logAvailableMiB();
        String efficiency = NumToString.number(100. * kept / (float) npixels);
        String cut = "cut " + NumToString.number(npixels - kept) + " out of " + NumToString.number(npixels);
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\tStandard deviation cut efficiency: " + cut + " = " + efficiency + "%\n ");

        // Summary
        ////////////////////////////////////////////////////////////////////////////////////////////

        kept = 0;
        for (int i = 0; i < npixels; i++) {
            if (mMask[i] == 1) {
                kept++;
            }
        }

        // Store in allocation
        mMaskAlloc = AnalysisController.newUCharAllocation();
        mMaskAlloc.copyFrom(mMask);

        efficiency = NumToString.number(100. * kept / (float) npixels);
        cut = "cut " + NumToString.number(npixels - kept) + " out of " + NumToString.number(npixels);
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\tCombined cut efficiency: " + cut + " = " + efficiency + "%\n ");

        // Estimate the standard deviation for FPS fps at TEMPERATURE deg Celsius
        // coordinate system:
        //      x-axis:  temperature (cold to hot)
        //      y-axis:  exposure (short to long)
        ////////////////////////////////////////////////////////////////////////////////////////////

        Log.e(Thread.currentThread().getName(), "Estimating mean value for " + NumToString.number(FPS)
                + " fps at " + NumToString.number(TEMPERATURE) + " Celsius ..");

        Float coldFastTemp = coldFast.getTemperature();
        Float coldSlowTemp = coldSlow.getTemperature();
        Float hotFastTemp  = hotFast.getTemperature();
        Float hotSlowTemp  = hotSlow.getTemperature();

        if (coldFastTemp == null || coldSlowTemp == null || hotFastTemp == null || hotSlowTemp == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "At least one temperature is null, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return false;
        }

        float coldTemp  = (coldFastTemp + coldSlowTemp) / 2.f;
        float hotTemp   = (hotFastTemp  + hotSlowTemp ) / 2.f;
        float tempRange = hotTemp - coldTemp;
        float temp      = TEMPERATURE;
        float x         = (temp - coldTemp) / tempRange;

        Long coldFastExp = CaptureConfiguration.EXPOSURE_BOUNDS.getLower();
        Long coldSlowExp = CaptureConfiguration.EXPOSURE_BOUNDS.getUpper();
        Long hotFastExp  = CaptureConfiguration.EXPOSURE_BOUNDS.getLower();
        Long hotSlowExp  = CaptureConfiguration.EXPOSURE_BOUNDS.getUpper();

        float shortExp = (coldFastExp + hotFastExp) / 2.f;
        float longExp  = (coldSlowExp + hotSlowExp) / 2.f;
        float expRange = longExp - shortExp;
        float exp      = (float) 1e9 / FPS;
        float y        = (exp - shortExp) / expRange;

        for (int i = 0; i < npixels; i++) {
            float f00 = cf[i];
            float f10 = hf[i];
            float f01 = cs[i];
            float f11 = hs[i];

            mCutStatistic[i] = f00*(1.f - x)*(1.f - y) + f10*x*(1.f - y) + f01*(1.f - x)*y + f11*x*y;
        }

        HeapMemory.logAvailableMiB();

        // Store in allocation
        mStdDevAlloc = AnalysisController.newFloatAllocation();
        mStdDevAlloc.copyFrom(mCutStatistic);

        // Compute average standard error
        ////////////////////////////////////////////////////////////////////////////////////////////

        Long coldFastFrames = coldFast.getNframes();
        Long coldSlowFrames = coldSlow.getNframes();
        Long hotFastFrames  = hotFast.getNframes();
        Long hotSlowFrames  = hotSlow.getNframes();

        if (coldFastFrames == null || coldSlowFrames == null || hotFastFrames == null || hotSlowFrames == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing number of frames, cannot continue");
            return false;
        }

        mTotalStdDevFrames = coldFastFrames + coldSlowFrames + hotFastFrames + hotSlowFrames;

        for (int i = 0; i < npixels; i++) {
            float cferr = cf[i] / (float) Math.sqrt(coldFastFrames);
            float cserr = cs[i] / (float) Math.sqrt(coldSlowFrames);
            float hferr = hf[i] / (float) Math.sqrt(hotFastFrames);
            float hserr = hs[i] / (float) Math.sqrt(hotSlowFrames);

            mCutStatistic[i] = (float) Math.sqrt(cferr*cferr + cserr*cserr + hferr*hferr + hserr*hserr);
        }

        // Store in allocation
        mStdErrAlloc = AnalysisController.newFloatAllocation();
        mStdErrAlloc.copyFrom(mCutStatistic);

        return true;
    }

}