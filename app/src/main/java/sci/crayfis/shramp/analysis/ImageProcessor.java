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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
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
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class ImageProcessor {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "StreamProcessorThread";

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
            GlobalSettings.IMAGE_PROCESSOR_THREAD_PRIORITY);

    // mBacklog.....................................................................................
    // TODO: description
    private static final AtomicInteger mBacklog = new AtomicInteger();

    // mFramesAboveThreshold........................................................................
    // TODO: description
    private static final AtomicInteger mFramesAboveThreshold = new AtomicInteger();

    // mIsFirstFrame................................................................................
    // TODO: description
    private static final AtomicBoolean mIsFirstFrame = new AtomicBoolean();

    // mCountAboveThresholdArray....................................................................
    // TODO: description
    private static final long[] mCountAboveThresholdArray = new long[1];
    private static final long[] mAnomalousStdDevArray = new long[1];

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mLiveScript..................................................................................
    // TODO: description
    private static ScriptC_LiveProcessing mLiveScript;

    // mPostScript..................................................................................
    // TODO: description
    private static ScriptC_PostProcessing mPostScript;

    // mImage.......................................................................................
    // TODO: description
    private static Allocation mImage;

    // mEnableSignificance..........................................................................
    // TODO: description
    private static int mEnableSignificance = DISABLED;

    // mSignificance................................................................................
    // TODO: description
    private static Allocation mSignificance;

    // mCountAboveThreshold.........................................................................
    // TODO: description
    private static Allocation mCountAboveThreshold;
    private static Allocation mAnomalousStdDev;

    // RunningTotal.................................................................................
    // TODO: description
    private abstract static class RunningTotal {
        static long       Nframes;
        //static long       ExposureNanos;
        static Allocation ValueSum;
        static Allocation Value2Sum;
    }

    // Statistics...................................................................................
    // TODO: description
    private abstract static class Statistics {
        static Allocation Mean;
        static Allocation StdDev;
        static Allocation StdErr;
        static float SignificanceThreshold;
    }

    private abstract static class StopWatches {
        final static StopWatch ImageProcessing = new StopWatch();
        final static StopWatch Statistics = new StopWatch();

        static String getMeanString() {
            String out = " \n";
            out += "Mean elapsed time: \n";
            out += "\t" + "ImageProcessing: " + NumToString.number(ImageProcessing.getMean()) + " [ns]\n";
            out += "\t" + "Statistics:      " + NumToString.number(Statistics.getMean()) + " [ns]\n";
            return out;
        }

        static String getLongestString() {
            String out = " \n";
            out += "Longest elapsed time: \n";
            out += "\t" + "ImageProcessing: " + NumToString.number(ImageProcessing.getLongest()) + " [ns]\n";
            out += "\t" + "Statistics:      " + NumToString.number(Statistics.getLongest()) + " [ns]\n";
            return out;
        }

        static String getShortestString() {
            String out = " \n";
            out += "Shortest elapsed time: \n";
            out += "\t" + "ImageProcessing: " + NumToString.number(ImageProcessing.getShortest()) + " [ns]\n";
            out += "\t" + "Statistics:      " + NumToString.number(Statistics.getShortest()) + " [ns]\n";
            return out;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // enableSignificance...........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void enableSignificance() {
        mEnableSignificance = ENABLED;
        mLiveScript.set_gEnableSignificance(mEnableSignificance);
    }

    // disableSignificance..........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void disableSignificance() {
        mEnableSignificance = DISABLED;
        mLiveScript.set_gEnableSignificance(mEnableSignificance);
    }

    // isSignificanceEnabled........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    static boolean isSignificanceEnabled() {
        return mEnableSignificance == ENABLED;
    }

    // isBusy.......................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    static boolean isBusy() {
        return mBacklog.get() != 0;
    }

    // getBacklog...................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    static int getBacklog() {
        return mBacklog.get();
    }

    // getMean......................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getMean() {
        return Statistics.Mean;
    }

    // getStdErr....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getStdErr() {
        return Statistics.StdErr;
    }

    // getStdDev....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getStdDev() {
        return Statistics.StdDev;
    }

    // getValueSum..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getValueSum() {
        return RunningTotal.ValueSum;
    }

    // getValue2Sum.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getValue2Sum() {
        return RunningTotal.Value2Sum;
    }

    // getSignificance..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getSignificance() {
        return mSignificance;
    }

    // setLiveProcessor.............................................................................
    /**
     * TODO: description, comments and logging
     * @param script bla
     */
    static void setLiveProcessor(@NonNull ScriptC_LiveProcessing script) {
        mLiveScript = script;
    }

    // setPostProcessor.............................................................................
    /**
     * TODO: description, comments and logging
     * @param script bla
     */
    static void setPostProcessor(@NonNull ScriptC_PostProcessing script) { mPostScript = script; }

    // setImageAllocation...........................................................................
    /**
     * TODO: description, comments and logging
     * @param image bla
     */
    static void setImageAllocation(@NonNull Allocation image) {
        mImage = image;
    }

    // setSignificanceAllocation....................................................................
    /**
     * TODO: description, comments and logging
     * @param significance bla
     */
    static void setSignificanceAllocation(@NonNull Allocation significance) { mSignificance = significance; }

    // setSignificanceThreshold.....................................................................
    /**
     * TODO: description, comments and logging
     * @param threshold bla
     */
    static void setSignificanceThreshold(float threshold) {
        mLiveScript.set_gSignificanceThreshold(threshold);
        Statistics.SignificanceThreshold = threshold;
    }

    // setCountAboveThresholdAllocation.............................................................
    /**
     * TODO: description, comments and logging
     * @param countAboveThreshold bla
     */
    static void setCountAboveThresholdAllocation(@NonNull Allocation countAboveThreshold) {
        mCountAboveThreshold = countAboveThreshold;
    }
    static void setAnomalousStdDevAllocation(@NonNull Allocation anomalousStdDev) {
        mAnomalousStdDev = anomalousStdDev;
    }

    // setStatistics................................................................................
    /**
     * TODO: description, comments and logging
     * @param mean bla
     * @param stdDev bla
     * @param stdErr bla
     */
    static void setStatistics(@NonNull Allocation mean,
                              @NonNull Allocation stdDev,
                              @NonNull Allocation stdErr) {
        Statistics.Mean = mean;
        Statistics.StdDev = stdDev;
        Statistics.StdErr = stdErr;
    }

    // resetTotals..................................................................................
    /**
     * TODO: description, comments and logging
     */
    static void resetTotals() {
        DataQueue.clear();
        mBacklog.set(0);
        mFramesAboveThreshold.set(0);
        mIsFirstFrame.set(true);

        RunningTotal.Nframes = 0L;
        //RunningTotal.ExposureNanos = 0L;

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

        mLiveScript.set_gSignificance(mSignificance);
        mLiveScript.set_gCountAboveThreshold(mCountAboveThreshold);
    }

    // process......................................................................................
    /**
     * TODO: description, comments and logging
     * @param result bla
     * @param wrapper bla
     */
    static void process(@NonNull TotalCaptureResult result, @NonNull ImageWrapper wrapper) {

        // skip first frame, for YUV_420_888 tends to be anomalous
        if (mIsFirstFrame.get()) {
            mIsFirstFrame.set(false);
            return;
        }

        // TODO: description
        class Processor implements Runnable {

            private TotalCaptureResult Result;
            private ImageWrapper Wrapper;

            private Processor(@NonNull TotalCaptureResult result, @NonNull ImageWrapper wrapper) {
                Result  = result;
                Wrapper = wrapper;
            }

            public void run() {
                StopWatches.ImageProcessing.start();

                RunningTotal.Nframes += 1;
                mCountAboveThresholdArray[0] = 0L;
                mCountAboveThreshold.copyFrom(mCountAboveThresholdArray);
                mLiveScript.set_gCountAboveThreshold(mCountAboveThreshold);

                if (ImageWrapper.is8bitData()) {
                    mImage.copyFrom(Wrapper.get8bitData());
                    mLiveScript.forEach_process8bitData(mImage);
                }
                else { // ImageWrapper.is16bitData()
                    mImage.copyFrom(Wrapper.get16bitData());
                    mLiveScript.forEach_process16bitData(mImage);
                }

                String filename2 = String.format(Locale.US,"%05d", 0);//mFileCount.getAndIncrement());
                filename2 += "_" + Long.toString(TimeManager.getElapsedNanos(Wrapper.getTimestamp())) + ".data";
                DataQueue.add(new OutputWrapper(filename2, Wrapper, 1L));


                if (mEnableSignificance == ENABLED) {
                    if (GlobalSettings.DEBUG_SAVE_SIGNIFICANCE && RunningTotal.Nframes % 10 == 0) {
                        mLiveScript.forEach_getSignificance(mSignificance);
                        String filename = String.format(Locale.US, "%05d", RunningTotal.Nframes)
                                + "_" + String.format(Locale.US, "%015d", TimeManager.getElapsedNanos(Wrapper.getTimestamp()))
                                + ".signif";
                        DataQueue.add(new OutputWrapper(filename, mSignificance, 1));
                    }
                    mLiveScript.forEach_getCountAboveThreshold(mCountAboveThreshold);
                    mCountAboveThreshold.copyTo(mCountAboveThresholdArray);
                    Log.e(Thread.currentThread().getName(), "______________________________ N above threshold: "
                    + NumToString.number(mCountAboveThresholdArray[0]));

                    if (false && mCountAboveThresholdArray[0] > 0L) {
                        int nFrames = mFramesAboveThreshold.incrementAndGet();
                        if (nFrames >= GlobalSettings.MAX_FRAMES_ABOVE_THRESHOLD) {
                            Log.e(Thread.currentThread().getName(), ":::::: REQUESTING THRESHOLD INCREASE :::::::");
                            AnalysisController.increaseSignificanceThreshold();
                        }
                    }
                }
                StopWatches.ImageProcessing.addTime( StopWatches.ImageProcessing.stop() );
                mBacklog.decrementAndGet();
                Log.e(Thread.currentThread().getName(), "Processor Backlog: " + NumToString.number(mBacklog.get()));
            }
        }

        mBacklog.incrementAndGet();
        mHandler.post(new Processor(result, wrapper));
    }

    // runStatistics................................................................................
    /**
     * TODO: description, comments and logging
     */
    static void runStatistics(String filename) {
        mBacklog.incrementAndGet();

        class RunStatistics implements Runnable {
            private String mFilename;

            private RunStatistics(String filename) {
                mFilename = filename;
            }

            @Override
            public void run() {

                StopWatches.Statistics.start();

                if (ImageWrapper.is8bitData()) {
                    mPostScript.set_gIs8bit(1);
                }
                else {
                    mPostScript.set_gIs8bit(0);
                }

                mLiveScript.forEach_getValueSum(RunningTotal.ValueSum);
                mPostScript.set_gValueSum(RunningTotal.ValueSum);

                mLiveScript.forEach_getValue2Sum(RunningTotal.Value2Sum);
                mPostScript.set_gValue2Sum(RunningTotal.Value2Sum);

                mAnomalousStdDevArray[0] = 0L;
                mAnomalousStdDev.copyFrom(mAnomalousStdDevArray);
                mPostScript.set_gAnomalousStdDev(mAnomalousStdDev);

                mPostScript.set_gNframes(RunningTotal.Nframes);
                mPostScript.set_gMean(Statistics.Mean);
                mPostScript.set_gStdDev(Statistics.StdDev);
                mPostScript.set_gStdErr(Statistics.StdErr);

                mPostScript.forEach_getMean(Statistics.Mean);
                mPostScript.forEach_getStdDev(Statistics.StdDev);
                mPostScript.forEach_getStdErr(Statistics.StdErr);

                mLiveScript.set_gMean(Statistics.Mean);
                mLiveScript.set_gStdDev(Statistics.StdDev);

                mPostScript.forEach_getAnomalousStdDev(mAnomalousStdDev);
                mAnomalousStdDev.copyTo(mAnomalousStdDevArray);
                Log.e(Thread.currentThread().getName(), "________Anomalous Std Dev: "
                        + NumToString.number(mAnomalousStdDevArray[0]));

                StopWatches.Statistics.addTime( StopWatches.Statistics.stop() );

                if (GlobalSettings.DEBUG_SAVE_MEAN) {
                    DataQueue.add(new OutputWrapper(mFilename + ".mean", Statistics.Mean, RunningTotal.Nframes));
                }
                if (GlobalSettings.DEBUG_SAVE_STDDEV) {
                    DataQueue.add(new OutputWrapper(mFilename + ".stddev", Statistics.StdDev, RunningTotal.Nframes));
                }

                PrintAllocations.printMaxMin();

                Log.e(Thread.currentThread().getName(), " \n" + StopWatches.getMeanString()
                        + StopWatches.getLongestString() + StopWatches.getShortestString());

                mBacklog.decrementAndGet();
            }
        }

        mHandler.post(new RunStatistics(filename));
    }

}