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
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_PostProcessing;
import sci.crayfis.shramp.ScriptC_LiveProcessing;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;
import sci.crayfis.shramp.util.TimeManager;

/**
 * Oversees both live and post image processing with RenderScript
 */
@TargetApi(21)
abstract class ImageProcessor {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // To maximize performance and avoid loading down calling threads, run image processing on its own thread
    private static final String THREAD_NAME = "ImageProcessorThread";

    // mHandler.....................................................................................
    // Reference to this thread's Handler
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                    GlobalSettings.IMAGE_PROCESSOR_THREAD_PRIORITY);

    // mIsFirstFrame................................................................................
    // Thread-safe flag denoting the first frame to be processed
    private static final AtomicBoolean mIsFirstFrame = new AtomicBoolean();

    // mBacklog.....................................................................................
    // Thread-safe count of jobs waiting for processing on this thread
    private static final AtomicInteger mBacklog = new AtomicInteger();

    // mFramesAboveThreshold........................................................................
    // Thread-safe count of frames with at least one pixel found to be above threshold
    private static final AtomicInteger mFramesAboveThreshold = new AtomicInteger();

    // mCountAboveThresholdArray....................................................................
    // Number of pixels in a frame that were found to be above threshold.
    // Corresponds to mCountAboveThreshold (RenderScript Allocation) below
    private static final long[] mCountAboveThresholdArray = new long[1];

    // mAnomalousStdDevArray........................................................................
    // In the process of determining the mean and standard deviation, an unlikely overflow in
    // the summing variables might occur under extreme conditions, if this happens the number of
    // pixels with this problem are recorded in this variable.
    // Corresponds to mAnomalousStdDev (RenderScript Allocation) below
    private static final long[] mAnomalousStdDevArray = new long[1];

    // ENABLED / DISABLED...........................................................................
    // Constants denoting whether significance testing is enabled or disabled
    private static final int ENABLED  = 1;
    private static final int DISABLED = 0;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mLiveScript..................................................................................
    // Reference to the LiveProcessing.rs RenderScript
    private static ScriptC_LiveProcessing mLiveScript;

    // mPostScript..................................................................................
    // Reference to the PostProcessing.rs RenderScript
    private static ScriptC_PostProcessing mPostScript;

    // mImage.......................................................................................
    // Image data (received from an ImageWrapper) converted into a RenderScript Allocation
    private static Allocation mImage;

    // mEnableSignificance..........................................................................
    // Denotes whether significance testing is enabled or disabled
    private static int mEnableSignificance = DISABLED;

    // mSignificance................................................................................
    // Significance of each pixel in an image as a RenderScript Allocation
    private static Allocation mSignificance;

    // mCountAboveThreshold.........................................................................
    // Number of pixels in a frame that were found to above threshold.
    // Corresponds to mCountAboveThresholdArray above
    private static Allocation mCountAboveThreshold;

    // mAnomalousStdDev.............................................................................
    // In the process of determining the mean and standard deviation, an unlikely overflow in
    // the summing variables might occur under extreme conditions, if this happens the number of
    // pixels with this problem are recorded in this variable.
    // Corresponds to mAnomalousStdDevArray above
    private static Allocation mAnomalousStdDev;

    // Inner Classes
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // RunningTotal.................................................................................
    // Collection of quantities that increase with each image processed
    private abstract static class RunningTotal {
        static long       Nframes;
        static Allocation ValueSum;
        static Allocation Value2Sum;
    }

    // PostProcessing...................................................................................
    // Collection of quantities of a statistical nature
    private abstract static class Statistics {
        static Allocation Mean;
        static Allocation StdDev;
        static Allocation StdErr;
        static float      SignificanceThreshold;
    }

    // For now, monitor performance (TODO: remove in the future)
    private abstract static class StopWatches {
        final static StopWatch LiveProcessing = new StopWatch("ImageProcessor.process()");
        final static StopWatch PostProcessing = new StopWatch("ImageProcessor.runStatistics()");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // isBusy.......................................................................................
    /**
     * @return True if there are image processing jobs still in queue, false if idling
     */
    static boolean isBusy() {
        return mBacklog.get() != 0;
    }

    // getBacklog...................................................................................
    /**
     * @return The number of backlogged image processing jobs waiting to run
     */
    static int getBacklog() {
        return mBacklog.get();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // enableSignificance...........................................................................
    /**
     * Enable live statistical significance testing on each pixel of input images
     */
    static void enableSignificance() {
        mEnableSignificance = ENABLED;
        mLiveScript.set_gEnableSignificance(mEnableSignificance);
    }

    // disableSignificance..........................................................................
    /**
     * Disable live statistical significance testing on each pixel of input images
     */
    static void disableSignificance() {
        mEnableSignificance = DISABLED;
        mLiveScript.set_gEnableSignificance(mEnableSignificance);
    }

    // isSignificanceEnabled........................................................................
    /**
     * @return True if significance testing is being done, false if it is disabled
     */
    @Contract(pure = true)
    static boolean isSignificanceEnabled() {
        return mEnableSignificance == ENABLED;
    }

    // getSignificance..............................................................................
    /**
     * @return RenderScript Allocation of pixel statistical significance for last image processed
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getSignificance() {
        return mSignificance;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getMean......................................................................................
    /**
     * @return RenderScript Allocation of pixel mean values currently being used
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getMean() {
        return Statistics.Mean;
    }

    // getStdDev....................................................................................
    /**
     * @return RenderScript Allocation of pixel standard deviation values currently being used
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getStdDev() {
        return Statistics.StdDev;
    }

    // getStdErr....................................................................................
    /**
     * @return RenderScript Allocation of pixel standard error values currently being used
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getStdErr() {
        return Statistics.StdErr;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // getValueSum..................................................................................
    /**
     * @return RenderScript Allocation of the pixel-wise sum of processed pixel values
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getValueSum() {
        return RunningTotal.ValueSum;
    }

    // getValue2Sum.................................................................................
    /**
     * @return RenderScript Allocation of the pixel-wise sum of processed pixel values**2
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getValue2Sum() {
        return RunningTotal.Value2Sum;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // setLiveProcessor.............................................................................
    /**
     * @param script Reference to RenderScript LiveProcessing.rs
     */
    static void setLiveProcessor(@NonNull ScriptC_LiveProcessing script) {
        mLiveScript = script;
    }

    // setPostProcessor.............................................................................
    /**
     * @param script Reference to RenderScript PostProcessing.rs
     */
    static void setPostProcessor(@NonNull ScriptC_PostProcessing script) { mPostScript = script; }

    // setImageAllocation...........................................................................
    /**
     * @param image Initialized RenderScript Allocation to contain image data
     */
    static void setImageAllocation(@NonNull Allocation image) {
        mImage = image;
    }

    // setSignificanceAllocation....................................................................
    /**
     * @param significance Initialized RenderScript Allocation to contain pixel significance
     */
    static void setSignificanceAllocation(@NonNull Allocation significance) { mSignificance = significance; }

    // setCountAboveThresholdAllocation.............................................................
    /**
     * @param countAboveThreshold Initialized RenderScript Allocation to count pixels above threshold
     */
    static void setCountAboveThresholdAllocation(@NonNull Allocation countAboveThreshold) {
        mCountAboveThreshold = countAboveThreshold;
    }

    // setAnomalousStdDevAllocation.................................................................
    /**
     * @param anomalousStdDev Initialized RenderScript Allocation to count overflows in summing
     */
    static void setAnomalousStdDevAllocation(@NonNull Allocation anomalousStdDev) {
        mAnomalousStdDev = anomalousStdDev;
    }

    // setStatistics................................................................................
    /**
     * @param mean Initialized RenderScript Allocation to contain pixel means
     * @param stdDev Initialized RenderScript Allocation to contain pixel standard deviations
     * @param stdErr Initialized RenderScript Allocation to contain pixel standard errors
     */
    static void setStatistics(@NonNull Allocation mean,
                              @NonNull Allocation stdDev,
                              @NonNull Allocation stdErr) {
        Statistics.Mean   = mean;
        Statistics.StdDev = stdDev;
        Statistics.StdErr = stdErr;
    }

    // setSignificanceThreshold.....................................................................
    /**
     * @param threshold Threshold to determine if a pixel's value is statistically significant
     */
    static void setSignificanceThreshold(float threshold) {
        mLiveScript.set_gSignificanceThreshold(threshold);
        Statistics.SignificanceThreshold = threshold;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // resetTotals..................................................................................
    /**
     * Reset all running / summing variables for a fresh start, reset live-processing RenderScript globals
     */
    static void resetTotals() {
        mBacklog.set(0);
        mFramesAboveThreshold.set(0);
        mIsFirstFrame.set(true);

        RunningTotal.Nframes = 0L;
        if (RunningTotal.ValueSum == null || RunningTotal.Value2Sum == null) {
            RunningTotal.ValueSum  = AnalysisController.newUIntAllocation();
            RunningTotal.Value2Sum = AnalysisController.newUIntAllocation();
        }

        mLiveScript.forEach_zeroUIntAllocation(RunningTotal.ValueSum);
        mLiveScript.forEach_zeroUIntAllocation(RunningTotal.Value2Sum);

        mLiveScript.set_gValueSum(RunningTotal.ValueSum);
        mLiveScript.set_gValue2Sum(RunningTotal.Value2Sum);

        mLiveScript.set_gMean(Statistics.Mean);
        mLiveScript.set_gStdDev(Statistics.StdDev);

        // Values are set in RenderScript LiveProcessing.rs
        mLiveScript.set_gSignificance(mSignificance);

        // Zeroed in process()
        mLiveScript.set_gCountAboveThreshold(mCountAboveThreshold);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // process......................................................................................
    /**
     * This method doesn't directly process an image, rather it builds a Runnable that processes
     * the image and posts it to the ImageProcessorThread to avoid slowing down the calling thread
     * @param result Image metadata
     * @param wrapper Image data
     */
    static void process(@NonNull TotalCaptureResult result, @NonNull ImageWrapper wrapper) {

        // skip the first frame, for YUV_420_888 in particular pixel values tend to be anomalously
        // big, I don't know why exactly, but it seems to be
        if (mIsFirstFrame.get()) {
            mIsFirstFrame.set(false);
            return;
        }

        // This Runnable is the image processor that runs on the ImageProcessorThread
        class Processor implements Runnable {

            // Payloads
            private TotalCaptureResult Result;
            private ImageWrapper Wrapper;

            // Constructor
            private Processor(@NonNull TotalCaptureResult result, @NonNull ImageWrapper wrapper) {
                Result  = result;
                Wrapper = wrapper;
            }

            // Action
            @Override
            public void run() {
                StopWatches.LiveProcessing.start();

                RunningTotal.Nframes += 1;

                // Save every DEBUG_IMAGE_SAVING_INTERVAL image
                // WARNING: each image will be ~20-30 MB or so
                if (GlobalSettings.DEBUG_ENABLE_IMAGE_SAVING
                        && RunningTotal.Nframes % GlobalSettings.DEBUG_IMAGE_SAVING_INTERVAL == 0) {
                    // filename = [frame number]_[nanoseconds since start].frame
                    String filename = String.format(Locale.US,"%05d", RunningTotal.Nframes);
                    filename += "_" + String.format(Locale.US, "%015d", TimeManager.getElapsedNanos(Wrapper.getTimestamp()));
                    filename += ".frame";
                    // TODO: exposure
                    DataQueue.add(new OutputWrapper(filename, Wrapper, null));
                }

                // Zero count of number of pixels above threshold
                mCountAboveThresholdArray[0] = 0L;
                mCountAboveThreshold.copyFrom(mCountAboveThresholdArray);
                mLiveScript.set_gCountAboveThreshold(mCountAboveThreshold);

                // RenderScript image processing
                if (ImageWrapper.is8bitData()) {
                    mImage.copyFrom(Wrapper.get8bitData());
                    mLiveScript.forEach_process8bitData(mImage);
                }
                else { // ImageWrapper.is16bitData()
                    mImage.copyFrom(Wrapper.get16bitData());
                    mLiveScript.forEach_process16bitData(mImage);
                }

                if (mEnableSignificance == ENABLED) {

                    mLiveScript.forEach_getCountAboveThreshold(mCountAboveThreshold);
                    mCountAboveThreshold.copyTo(mCountAboveThresholdArray);
                    Log.e(Thread.currentThread().getName(), "Pixel count above threshold: "
                            + NumToString.number(mCountAboveThresholdArray[0]));

                    // TODO: in the future when i'm happy with the rates over threshold, save it
                    if (mCountAboveThresholdArray[0] > 0L) {
                        //mLiveScript.forEach_getSignificance(mSignificance);
                        // filename = [frame number]_[nanoseconds since start].signif
                        //String filename = String.format(Locale.US, "%05d", RunningTotal.Nframes);
                        //filename += "_" + String.format(Locale.US, "%015d", TimeManager.getElapsedNanos(Wrapper.getTimestamp()));
                        //filename += ".signif";
                        //DataQueue.add(new OutputWrapper(filename, mSignificance, 1));
                    }

                    // TODO: for now:
                    // Save every DEBUG_SIGNIFICANCE_SAVING_INTERVAL significance
                    // WARNING: each image will be ~40-50 MB or so and will slow down processing
                    if (GlobalSettings.DEBUG_SAVE_SIGNIFICANCE
                            && RunningTotal.Nframes % GlobalSettings.DEBUG_SIGNIFICANCE_SAVING_INTERVAL == 0) {
                        mLiveScript.forEach_getSignificance(mSignificance);
                        // filename = [frame number]_[nanoseconds since start].signif
                        String filename = String.format(Locale.US, "%05d", RunningTotal.Nframes);
                        filename += "_" + String.format(Locale.US, "%015d", TimeManager.getElapsedNanos(Wrapper.getTimestamp()));
                        filename += ".signif";
                        DataQueue.add(new OutputWrapper(filename, mSignificance, 1));
                    }

                    // TODO: hopefully remove in the future
                    if (GlobalSettings.DEBUG_ENABLE_THRESHOLD_INCREASE && mCountAboveThresholdArray[0] > 0L) {
                        int nFrames = mFramesAboveThreshold.incrementAndGet();
                        if (nFrames >= GlobalSettings.MAX_FRAMES_ABOVE_THRESHOLD) {
                            Log.e(Thread.currentThread().getName(), ":::::: REQUESTING THRESHOLD INCREASE :::::::");
                            AnalysisController.increaseSignificanceThreshold();
                            mFramesAboveThreshold.set(0);
                        }
                    }
                }

                Log.e(Thread.currentThread().getName(), "Image processor backlog: " + NumToString.number(mBacklog.decrementAndGet()));
                StopWatches.LiveProcessing.addTime( StopWatches.LiveProcessing.stop() );
            }
        }

        mBacklog.incrementAndGet();
        mHandler.post(new Processor(result, wrapper));
    }

    // runStatistics................................................................................
    /**
     * This method doesn't directly process statistics, rather it builds a Runnable that does
     * and posts it to the ImageProcessorThread to avoid slowing down the calling thread and avoid
     * running statistics in the middle of live image processing
     * @param filename Optional filename to save statistics (file extension is provided by this
     *                 method)
     */
    static void runStatistics(@Nullable String filename) {

        class RunStatistics implements Runnable {
            // Payload
            private String mFilename;

            // Constructor
            private RunStatistics(String filename) {
                mFilename = filename;
            }

            // Action
            @Override
            public void run() {
                StopWatches.PostProcessing.start();

                if (ImageWrapper.is8bitData()) {
                    mPostScript.set_gIs8bit(1); // true
                }
                else { // ImageWrapper.is16bitData()
                    mPostScript.set_gIs8bit(0); // false
                }

                // Move value sum from LiveProcessing.rs to PostProcessing.rs
                mLiveScript.forEach_getValueSum(RunningTotal.ValueSum);
                mPostScript.set_gValueSum(RunningTotal.ValueSum);

                // Move value**2 sum from LiveProcessing.rs to PostProcessing.rs
                mLiveScript.forEach_getValue2Sum(RunningTotal.Value2Sum);
                mPostScript.set_gValue2Sum(RunningTotal.Value2Sum);

                // Zero overflow detection
                mAnomalousStdDevArray[0] = 0L;
                mAnomalousStdDev.copyFrom(mAnomalousStdDevArray);
                mPostScript.set_gAnomalousStdDev(mAnomalousStdDev);

                // Finish setting remaining globals
                mPostScript.set_gNframes(RunningTotal.Nframes);
                mPostScript.set_gMean(Statistics.Mean);
                mPostScript.set_gStdDev(Statistics.StdDev);
                mPostScript.set_gStdErr(Statistics.StdErr);

                // Compute statistics and fetch from RenderScript
                mPostScript.forEach_getMean(Statistics.Mean);
                mPostScript.forEach_getStdDev(Statistics.StdDev);
                mPostScript.forEach_getStdErr(Statistics.StdErr);

                // Move new statistics over to LiveProcessing.rs
                mLiveScript.set_gMean(Statistics.Mean);
                mLiveScript.set_gStdDev(Statistics.StdDev);

                // Check for overflows
                mPostScript.forEach_getAnomalousStdDev(mAnomalousStdDev);
                mAnomalousStdDev.copyTo(mAnomalousStdDevArray);
                // TODO: make more of a big deal about this
                Log.e(Thread.currentThread().getName(), "Anomalous Std Dev Count: "
                                                    + NumToString.number(mAnomalousStdDevArray[0]));

                if (GlobalSettings.DEBUG_SAVE_MEAN) {
                    DataQueue.add(new OutputWrapper(mFilename + ".mean", Statistics.Mean, RunningTotal.Nframes));
                }
                if (GlobalSettings.DEBUG_SAVE_STDDEV) {
                    DataQueue.add(new OutputWrapper(mFilename + ".stddev", Statistics.StdDev, RunningTotal.Nframes));
                }

                mBacklog.decrementAndGet();
                StopWatches.PostProcessing.addTime();

                // TODO: remove in future
                PrintAllocations.printMaxMin();
            }
        }

        mBacklog.incrementAndGet();
        mHandler.post(new RunStatistics(filename));
    }

}