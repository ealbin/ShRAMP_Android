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
import android.renderscript.Allocation;
import android.util.Log;

import sci.crayfis.shramp.GlobalSettings;

import sci.crayfis.shramp.util.Datestamp;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * Given calibration files, applies cuts to determine trustworthy pixels
 */
@TargetApi(21)
abstract class ApplyCuts {

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // makePixelMask................................................................................
    /**
     * Loads most recent calibration files from ShRAMP/Calibrations, and generates a pixel mask
     * of what pixels should be used in significance computation.
     * Note: assumes "hot" is hotter than "cold" and "fast" is faster than "slow"
     */
    static void makePixelMask() {

        // Free up any memory possible
        System.gc();

        Allocation meanAllocation   = AnalysisController.newFloatAllocation();
        Allocation stddevAllocation = AnalysisController.newFloatAllocation();
        Allocation stderrAllocation = AnalysisController.newFloatAllocation();
        Allocation maskAllocation   = AnalysisController.newUCharAllocation();

        String coldFastMeanPath = StorageMedia.findRecentCalibration("cold_fast", GlobalSettings.MEAN_FILE);
        String coldSlowMeanPath = StorageMedia.findRecentCalibration("cold_slow", GlobalSettings.MEAN_FILE);
        String hotFastMeanPath  = StorageMedia.findRecentCalibration("hot_fast",  GlobalSettings.MEAN_FILE);
        String hotSlowMeanPath  = StorageMedia.findRecentCalibration("hot_slow",  GlobalSettings.MEAN_FILE);

        String coldFastStdDevPath = StorageMedia.findRecentCalibration("cold_fast", GlobalSettings.STDDEV_FILE);
        String coldSlowStdDevPath = StorageMedia.findRecentCalibration("cold_slow", GlobalSettings.STDDEV_FILE);
        String hotFastStdDevPath  = StorageMedia.findRecentCalibration("hot_fast",  GlobalSettings.STDDEV_FILE);
        String hotSlowStdDevPath  = StorageMedia.findRecentCalibration("hot_slow",  GlobalSettings.STDDEV_FILE);

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
            return;
        }

        int npixels = ImageWrapper.getNpixels();
        byte[] mask = new byte[npixels];
        for (int i = 0; i < npixels; i++) {
            mask[i] = 1;
        }

        if (HeapMemory.getAvailableMiB() < 250) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Not enough memory to apply cuts");
            HeapMemory.logAvailableMiB();
            return;
        }
        InputWrapper coldFast = new InputWrapper(coldFastMeanPath);
        InputWrapper coldSlow = new InputWrapper(coldSlowMeanPath);
        InputWrapper hotFast  = new InputWrapper(hotFastMeanPath);
        InputWrapper hotSlow  = new InputWrapper(hotSlowMeanPath);

        if (HeapMemory.isMemoryLow()) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Not enough memory to apply cuts");
            HeapMemory.logAvailableMiB();
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return;
        }

        // TODO: possibly a bug if settings change between writes / runs
        int maxPixelValue = 0;
        if (OutputWrapper.mBitsPerPixel == 8) {
            maxPixelValue = 255;
        }
        else { // OutputWrapper.mBitsPerPixel == 16
            maxPixelValue = 1023;
        }

        float[] cf = coldFast.getStatisticsData();
        float[] cs = coldSlow.getStatisticsData();
        float[] hf = hotFast.getStatisticsData();
        float[] hs = hotSlow.getStatisticsData();

        if (cf == null || cs == null || hf == null || hs == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing statistical data, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return;
        }

        Long coldFastFrames = coldFast.getNframes();
        Long coldSlowFrames = coldSlow.getNframes();
        Long hotFastFrames  = hotFast.getNframes();
        Long hotSlowFrames  = hotSlow.getNframes();

        if (coldFastFrames == null || coldSlowFrames == null || hotFastFrames == null || hotSlowFrames == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing number of frames, cannot continue");
            return;
        }

        long totalMeanFrames = coldFastFrames + coldSlowFrames + hotFastFrames + hotSlowFrames;

        float[] variation = new float[npixels];
        Histogram histogram = new Histogram(-100, 100);

        // Temperature-based cut
        //----------------------
        for (int i = 0; i < npixels; i++) {
            variation[i] = maxPixelValue * ( (hf[i] + hs[i]) - (cf[i] + cs[i]) ) / 2.f;
            histogram.add(variation[i]);
        }

        double maxValue = histogram.getBinCenter(histogram.getMaxBin());
        double stddev   = histogram.getMaxStdDev();
        double upperLimit = maxValue + Math.max(1., 3. * stddev);
        double lowerLimit = maxValue - Math.max(1., 3. * stddev);

        int kept = 0;
        for (int i = 0; i < npixels; i++) {
            float val = variation[i];
            if (val < lowerLimit || val > upperLimit) {
                mask[i] = 0;
            } else {
                kept++;
            }
        }

        String efficiency = NumToString.decimal(100. * kept / (float) npixels);
        Log.e(Thread.currentThread().getName(), "Temperature cut efficiency: " + efficiency + "%");

        // Exposure-based cut
        //-------------------

        histogram.reset();
        for (int i = 0; i < npixels; i++) {
            variation[i] = maxPixelValue * ( (hs[i] + cs[i]) - (hf[i] + cf[i]) ) / 2.f;
            histogram.add(variation[i]);
        }

        maxValue = histogram.getBinCenter(histogram.getMaxBin());
        stddev   = histogram.getMaxStdDev();
        upperLimit = maxValue + Math.max(1., 3. * stddev);
        lowerLimit = maxValue - Math.max(1., 3. * stddev);

        kept = 0;
        for (int i = 0; i < npixels; i++) {
            float val = variation[i];
            if (val < lowerLimit || val > upperLimit) {
                mask[i] = 0;
            } else {
                kept++;
            }
        }

        efficiency = NumToString.decimal(100. * kept / (float) npixels);
        Log.e(Thread.currentThread().getName(), "Exposure cut efficiency: " + efficiency + "%");

        // Estimate the mean for 10 fps at 35 deg Celsius
        // coordinate system:
        //      x-axis:  temperature (cold to hot)
        //      y-axis:  exposure (short to long)
        //------------------------------------------------

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
            return;
        }

        float coldTemp  = (coldFastTemp + coldSlowTemp) / 2.f;
        float hotTemp   = (hotFastTemp  + hotSlowTemp ) / 2.f;
        float tempRange = hotTemp - coldTemp;
        float temp      = 35.f;
        float x         = (temp - coldTemp) / tempRange;

        Long coldFastExp = coldFast.getExposure();
        Long coldSlowExp = coldSlow.getExposure();
        Long hotFastExp  = hotFast.getExposure();
        Long hotSlowExp  = hotSlow.getExposure();

        if (coldFastExp == null || coldSlowExp == null || hotFastExp == null || hotSlowExp == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "At least one exposure is null, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return;
        }

        float shortExp = (coldFastExp + hotFastExp) / 2.f;
        float longExp  = (coldSlowExp + hotSlowExp) / 2.f;
        float expRange = longExp - shortExp;
        float exp      = (float) 1e9 / 10.f;
        float y        = (exp - shortExp) / expRange;

        for (int i = 0; i < npixels; i++) {
            float f00 = cf[i];
            float f10 = hf[i];
            float f01 = cs[i];
            float f11 = hs[i];

            variation[i] = f00*(1.f - x)*(1.f - y) + f10*x*(1.f - y) + f01*(1.f - x)*y + f11*x*y;
        }

        // Store in allocation
        meanAllocation.copyFrom(variation);

        // Free memory
        coldFast = null;
        coldSlow = null;
        hotFast  = null;
        hotSlow  = null;
        System.gc();

        coldFast = new InputWrapper(coldFastStdDevPath);
        coldSlow = new InputWrapper(coldSlowStdDevPath);
        hotFast  = new InputWrapper(hotFastStdDevPath);
        hotSlow  = new InputWrapper(hotSlowStdDevPath);

        cf = coldFast.getStatisticsData();
        cs = coldSlow.getStatisticsData();
        hf = hotFast.getStatisticsData();
        hs = hotSlow.getStatisticsData();

        if (cf == null || cs == null || hf == null || hs == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing statistical data, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return;
        }

        // Standard Deviation-based cut
        //-----------------------------

        kept = 0;
        for (int i = 0; i < npixels; i++) {
            float val = (float) Math.sqrt(hs[i]*hs[i]+ hf[i]*hf[i] + cs[i]*cs[i] + cf[i]*cf[i]) / 4.f;
            if (val > 0.03f) {
                mask[i] = 0;
            }
            else {
                kept++;
            }
        }

        efficiency = NumToString.decimal(100. * kept / (float) npixels);
        Log.e(Thread.currentThread().getName(), "Standard deviation cut efficiency: " + efficiency + "%");

        // Summary
        //--------

        kept = 0;
        for (int i = 0; i < npixels; i++) {
            if (mask[i] == 1) {
                kept++;
            }
        }

        // Store in allocation
        maskAllocation.copyFrom(mask);

        efficiency = NumToString.decimal(100. * kept / (float) npixels);
        Log.e(Thread.currentThread().getName(), "Combined cut efficiency: " + efficiency + "%");

        // Estimate the standard deviation for 10 fps at 35 deg Celsius
        // coordinate system:
        //      x-axis:  temperature (cold to hot)
        //      y-axis:  exposure (short to long)
        //--------------------------------------------------------------

        coldFastTemp = coldFast.getTemperature();
        coldSlowTemp = coldSlow.getTemperature();
        hotFastTemp  = hotFast.getTemperature();
        hotSlowTemp  = hotSlow.getTemperature();

        if (coldFastTemp == null || coldSlowTemp == null || hotFastTemp == null || hotSlowTemp == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "At least one temperature is null, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return;
        }

        coldTemp  = (coldFastTemp + coldSlowTemp) / 2.f;
        hotTemp   = (hotFastTemp  + hotSlowTemp ) / 2.f;
        tempRange = hotTemp - coldTemp;
        x         = (temp - coldTemp) / tempRange;

        coldFastExp = coldFast.getExposure();
        coldSlowExp = coldSlow.getExposure();
        hotFastExp  = hotFast.getExposure();
        hotSlowExp  = hotSlow.getExposure();

        if (coldFastExp == null || coldSlowExp == null || hotFastExp == null || hotSlowExp == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "At least one exposure is null, cannot continue");
            coldFast = null;
            coldSlow = null;
            hotFast  = null;
            hotSlow  = null;
            System.gc();
            return;
        }

        shortExp = (coldFastExp + hotFastExp) / 2.f;
        longExp  = (coldSlowExp + hotSlowExp) / 2.f;
        expRange = longExp - shortExp;
        y        = (exp - shortExp) / expRange;

        for (int i = 0; i < npixels; i++) {
            float f00 = cf[i];
            float f10 = hf[i];
            float f01 = cs[i];
            float f11 = hs[i];

            variation[i] = f00*(1.f - x)*(1.f - y) + f10*x*(1.f - y) + f01*(1.f - x)*y + f11*x*y;
        }

        // Store in allocation
        stddevAllocation.copyFrom(variation);

        // Compute average standard error
        //-------------------------------

        coldFastFrames = coldFast.getNframes();
        coldSlowFrames = coldSlow.getNframes();
        hotFastFrames  = hotFast.getNframes();
        hotSlowFrames  = hotSlow.getNframes();

        if (coldFastFrames == null || coldSlowFrames == null || hotFastFrames == null || hotSlowFrames == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Missing number of frames, cannot continue");
            return;
        }

        long totalStdDevFrames = coldFastFrames + coldSlowFrames + hotFastFrames + hotSlowFrames;

        for (int i = 0; i < npixels; i++) {
            float cferr = cf[i] / (float) Math.sqrt(coldFastFrames);
            float cserr = cs[i] / (float) Math.sqrt(coldSlowFrames);
            float hferr = hf[i] / (float) Math.sqrt(hotFastFrames);
            float hserr = hs[i] / (float) Math.sqrt(hotSlowFrames);

            variation[i] = (float) Math.sqrt(cferr*cferr + cserr*cserr + hferr*hferr + hserr*hserr);
        }

        // Store in allocation
        stderrAllocation.copyFrom(variation);

        // Free memory
        variation = null;
        coldFast  = null;
        coldSlow  = null;
        hotFast   = null;
        hotSlow   = null;
        System.gc();

        // Update statistics
        ImageProcessor.setStatistics(meanAllocation, stddevAllocation, stderrAllocation, maskAllocation);

        // Save statistics
        String date = Datestamp.getDate();
        StorageMedia.writeCalibration(new OutputWrapper("mean_"   + date + GlobalSettings.MEAN_FILE,   meanAllocation,   totalMeanFrames,  35.f));
        StorageMedia.writeCalibration(new OutputWrapper("stddev_" + date + GlobalSettings.STDDEV_FILE, stddevAllocation, totalStdDevFrames,35.f));
        StorageMedia.writeCalibration(new OutputWrapper("stderr_" + date + GlobalSettings.STDERR_FILE, stderrAllocation, totalStdDevFrames,35.f));
        StorageMedia.writeCalibration(new OutputWrapper("mask_"   + date + GlobalSettings.MASK_FILE,   mask));
    }

}