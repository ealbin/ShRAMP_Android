package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.util.concurrent.atomic.AtomicInteger;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_PostProcessing;
import sci.crayfis.shramp.ScriptC_LiveProcessing;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

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
    private static int mEnableSignificance = 0;

    // mSignificance................................................................................
    // TODO: description
    private static Allocation mSignificance;

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
        static int Exists = 0;
        static Allocation MeanRate;
        static Allocation StdDevRate;
        static Allocation StdErrRate;
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
        mEnableSignificance = 1;
        mLiveScript.set_gEnableSignificance(mEnableSignificance);
    }

    // disableSignificance..........................................................................
    /**
     * TODO: description, comments and logging
     */
    static void disableSignificance() {
        mEnableSignificance = 0;
        mLiveScript.set_gEnableSignificance(mEnableSignificance);
    }

    // isSignificanceEnabled........................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    static boolean isSignificanceEnabled() {
        return mEnableSignificance == 1;
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
     * TODO: description, commments and logging
     * @param significance bla
     */
    static void setSignificanceAllocation(@NonNull Allocation significance) { mSignificance = significance; }

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

        RunningTotal.Nframes = 0L;
        RunningTotal.ExposureNanos = 0L;

        AnalysisManager.resetAllocation(RunningTotal.ExposureValueSum);
        AnalysisManager.resetAllocation(RunningTotal.ExposureValue2Sum);

        RunningTotal.ExposureValueSum  = AnalysisManager.newFloatAllocation();
        RunningTotal.ExposureValue2Sum = AnalysisManager.newFloatAllocation();

        mLiveScript.set_gExposureValueSum(RunningTotal.ExposureValueSum);
        mLiveScript.set_gExposureValue2Sum(RunningTotal.ExposureValue2Sum);

        mLiveScript.set_gSignificance(mSignificance);
        mLiveScript.set_gMeanRate(Statistics.MeanRate);
        mLiveScript.set_gStdDevRate(Statistics.StdDevRate);
    }

    // process......................................................................................
    /**
     * TODO: description, comments and logging
     * @param result bla
     * @param wrapper bla
     */
    static void process(@NonNull TotalCaptureResult result, @NonNull ImageWrapper wrapper) {
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

                RunningTotal.ExposureNanos += exposureTime;
                RunningTotal.Nframes += 1;

                StopWatch setupWatch = new StopWatch();
                mLiveScript.set_gExposureTime(exposureTime);
                Log.e(Thread.currentThread().getName(), "<renderscript set_> time: " + NumToString.number(setupWatch.stop()) + " [ns]");

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
                if (mEnableSignificance == 1) {
                    mLiveScript.forEach_getSignificance(mSignificance);
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

                mPostScript.set_gNframes(RunningTotal.Nframes);
                mPostScript.set_gExposureSum(RunningTotal.ExposureNanos);
                mPostScript.set_gMeanRate(Statistics.MeanRate);
                mPostScript.set_gStdDevRate(Statistics.StdDevRate);
                mPostScript.set_gStdErrRate(Statistics.StdErrRate);

                mPostScript.forEach_getMeanRate(Statistics.MeanRate);
                mPostScript.forEach_getStdDevRate(Statistics.StdDevRate);
                mPostScript.forEach_getStdErrRate(Statistics.StdErrRate);

                mBacklog.decrementAndGet();
            }
        });
    }

}