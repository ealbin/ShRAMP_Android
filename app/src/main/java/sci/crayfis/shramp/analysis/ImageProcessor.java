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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.icu.util.Output;
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
        static long       ExposureNanos;
        static Allocation ExposureValueSum;
        static Allocation ExposureValue2Sum;
    }

    // Statistics...................................................................................
    // TODO: description
    private abstract static class Statistics {
        static Allocation MeanRate;
        static Allocation StdDevRate;
        static Allocation StdErrRate;
        static float SignificanceThreshold;
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

    // getMeanRate..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getMeanRate() {
        return Statistics.MeanRate;
    }

    // getStdErrRate................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getStdErrRate() {
        return Statistics.StdErrRate;
    }

    // getStdDevRate................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getStdDevRate() {
        return Statistics.StdDevRate;
    }

    // getExposureValueSum..........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getExposureValueSum() {
        return RunningTotal.ExposureValueSum;
    }

    // getExposureValue2Sum.........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    static Allocation getExposureValue2Sum() {
        return RunningTotal.ExposureValue2Sum;
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
     * @param meanRate bla
     * @param stdDevRate bla
     * @param stdErrRate bla
     */
    static void setStatistics(@NonNull Allocation meanRate,
                              @NonNull Allocation stdDevRate,
                              @NonNull Allocation stdErrRate) {
        Statistics.MeanRate   = meanRate;
        Statistics.StdDevRate = stdDevRate;
        Statistics.StdErrRate = stdErrRate;
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
        RunningTotal.ExposureNanos = 0L;

        if (RunningTotal.ExposureValueSum == null || RunningTotal.ExposureValue2Sum == null) {
            RunningTotal.ExposureValueSum  = AnalysisController.newDoubleAllocation();
            RunningTotal.ExposureValue2Sum = AnalysisController.newDoubleAllocation();
        }
        mLiveScript.forEach_zeroDoubleAllocation(RunningTotal.ExposureValueSum);
        mLiveScript.forEach_zeroDoubleAllocation(RunningTotal.ExposureValue2Sum);

        mLiveScript.set_gExposureValueSum(RunningTotal.ExposureValueSum);
        mLiveScript.set_gExposureValue2Sum(RunningTotal.ExposureValue2Sum);

        //mLiveScript.set_gMeanRate(Statistics.MeanRate);
        //mLiveScript.set_gStdDevRate(Statistics.StdDevRate);

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

        StopWatch stopWatch = new StopWatch();

        // TODO: description
        class Processor implements Runnable {

            private TotalCaptureResult Result;
            private ImageWrapper Wrapper;

            private Processor(@NonNull TotalCaptureResult result, @NonNull ImageWrapper wrapper) {
                Result  = result;
                Wrapper = wrapper;
            }

            public void run() {
                StopWatch stopWatch = new StopWatch();
                Long exposureTime = Result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                assert exposureTime != null;

                // assert isn't reliable :-(
                if (exposureTime == null) {
                    // turn exposureTime into frame count
                    exposureTime = 1L;
                }
                RunningTotal.ExposureNanos += exposureTime;
                RunningTotal.Nframes += 1;

                StopWatch setupWatch = new StopWatch();
                mLiveScript.set_gExposureTime(exposureTime);
                Log.e(Thread.currentThread().getName(), "<renderscript set_> time: " + NumToString.number(setupWatch.stop()) + " [ns]");

                mCountAboveThresholdArray[0] = 0L;
                mCountAboveThreshold.copyFrom(mCountAboveThresholdArray);
                mLiveScript.set_gCountAboveThreshold(mCountAboveThreshold);

                if (ImageWrapper.is8bitData()) {
                    mImage.copyFrom(Wrapper.get8bitData());
                    mLiveScript.forEach_process8bitData(mImage);
                }
                else if (ImageWrapper.is16bitData()) {
                    mImage.copyFrom(Wrapper.get16bitData());
                    mLiveScript.forEach_process16bitData(mImage);
                }
                else {
                    // TODO: error
                }

                //String filename = String.format(Locale.US,"%05d", mFileCount.getAndIncrement());
                //filename += "_" + Long.toString(TimeManager.getElapsedNanos(imageWrapper.getTimestamp())) + ".data";

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
                Log.e(Thread.currentThread().getName(), "<renderscript run()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
                mBacklog.decrementAndGet();
                Log.e(Thread.currentThread().getName(), "Processor Backlog: " + NumToString.number(mBacklog.get()));
            }
        }
        Log.e(Thread.currentThread().getName(), "<process(result, wrapper)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");

        mBacklog.incrementAndGet();
        mHandler.post(new Processor(result, wrapper));
    }

    // runStatistics................................................................................
    /**
     * TODO: description, comments and logging
     */
    static void runStatistics() {
        mBacklog.incrementAndGet();

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                mLiveScript.forEach_getExposureValueSum(RunningTotal.ExposureValueSum);
                mPostScript.set_gExposureValueSum(RunningTotal.ExposureValueSum);

                mLiveScript.forEach_getExposureValue2Sum(RunningTotal.ExposureValue2Sum);
                mPostScript.set_gExposureValue2Sum(RunningTotal.ExposureValue2Sum);

                mAnomalousStdDevArray[0] = 0L;
                mAnomalousStdDev.copyFrom(mAnomalousStdDevArray);
                mPostScript.set_gAnomalousStdDev(mAnomalousStdDev);

                mPostScript.set_gNframes(RunningTotal.Nframes);
                mPostScript.set_gExposureSum(RunningTotal.ExposureNanos);
                mPostScript.set_gMeanRate(Statistics.MeanRate);
                mPostScript.set_gStdDevRate(Statistics.StdDevRate);
                mPostScript.set_gStdErrRate(Statistics.StdErrRate);

                mPostScript.forEach_getMeanRate(Statistics.MeanRate);
                mPostScript.forEach_getStdDevRate(Statistics.StdDevRate);
                mPostScript.forEach_getStdErrRate(Statistics.StdErrRate);

                mLiveScript.set_gMeanRate(Statistics.MeanRate);
                mLiveScript.set_gStdDevRate(Statistics.StdDevRate);

                mPostScript.forEach_getAnomalousStdDev(mAnomalousStdDev);
                mAnomalousStdDev.copyTo(mAnomalousStdDevArray);
                Log.e(Thread.currentThread().getName(), "________Anomalous Std Dev: "
                + NumToString.number(mAnomalousStdDevArray[0]));

                if (GlobalSettings.DEBUG_SAVE_MEAN) {
                    String filename = String.format(Locale.US, "%015d", TimeManager.getElapsedSystemNanos())
                            + ".mean";
                    DataQueue.add(new OutputWrapper(filename, Statistics.MeanRate, RunningTotal.Nframes));
                }
                if (GlobalSettings.DEBUG_SAVE_STDDEV) {
                    String filename = String.format(Locale.US, "%015d", TimeManager.getElapsedSystemNanos())
                            + ".stddev";
                    DataQueue.add(new OutputWrapper(filename, Statistics.StdDevRate, RunningTotal.Nframes));
                }

                mBacklog.decrementAndGet();
            }
        });
    }

}