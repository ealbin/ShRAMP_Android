package sci.crayfis.shramp.analysis;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.renderscript.Allocation;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_PostProcess;
import sci.crayfis.shramp.ScriptC_StreamProcess;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

abstract public class ImageProcessor {

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "StreamProcessorThread";

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = GlobalSettings.STREAM_PROCESSOR_THREAD_PRIORITY;

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME, PRIORITY);

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static Allocation mImage;
    private static Allocation mSignificance;

    private static ScriptC_StreamProcess mStreamScript;
    private static ScriptC_PostProcess mPostScript;

    private static AtomicInteger mBacklog = new AtomicInteger();

    private abstract static class RunningTotal {
        static long Nframes;
        static long ExposureNanos;
        static Allocation ExposureValueSum;
        static Allocation ExposureValue2Sum;
    }

    private abstract static class PreviousResult {
        static int Exists = 0;
        static Allocation MeanRate;
        static Allocation StdDevRate;
        static Allocation StdErrRate;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static void setRenderScript(ScriptC_StreamProcess script) {
        mStreamScript = script;
    }
    static void setRenderScriptPost(ScriptC_PostProcess script) { mPostScript = script; }

    static void setImageAllocation(Allocation image) {
        mImage = image;
    }
    static void setSignificanceAllocation(Allocation significance) { mSignificance = significance; }

    static void resetTotals() {
        DataQueue.clear();
        mBacklog.set(0);

        RunningTotal.Nframes = 0L;
        RunningTotal.ExposureNanos = 0L;

        AnalysisManager.resetAllocation(RunningTotal.ExposureValueSum);
        AnalysisManager.resetAllocation(RunningTotal.ExposureValue2Sum);

        RunningTotal.ExposureValueSum  = AnalysisManager.newDoubleAllocation();
        RunningTotal.ExposureValue2Sum = AnalysisManager.newDoubleAllocation();

        mStreamScript.set_gExposureValueSum(RunningTotal.ExposureValueSum);
        mStreamScript.set_gExposureValue2Sum(RunningTotal.ExposureValue2Sum);

        mStreamScript.set_gSignificance(mSignificance);
        mStreamScript.set_gMeanRate(PreviousResult.MeanRate);
        mStreamScript.set_gStdDevRate(PreviousResult.StdDevRate);
    }

    static void updateResults(Allocation meanRate, Allocation stdDevRate, Allocation stdErrRate) {
        PreviousResult.MeanRate   = meanRate;
        PreviousResult.StdDevRate = stdDevRate;
        PreviousResult.StdErrRate = stdErrRate;
    }

    static void process(TotalCaptureResult result, ImageWrapper wrapper) {
        StopWatch stopWatch = new StopWatch();
        class Processor implements Runnable {

            private TotalCaptureResult Result;
            private ImageWrapper Wrapper;

            private Processor(TotalCaptureResult result, ImageWrapper wrapper) {
                Result  = result;
                Wrapper = wrapper;
            }

            public void run() {
                StopWatch stopWatch = new StopWatch();
                Long exposureTime = Result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                assert exposureTime != null;

                RunningTotal.ExposureNanos += exposureTime;
                RunningTotal.Nframes += 1;

                mStreamScript.set_gDoSignificance(0); // TODO: do significance

                StopWatch setupWatch = new StopWatch();
                mStreamScript.set_gExposureTime(exposureTime);
                Log.e(Thread.currentThread().getName(), "<renderscript set_> time: " + NumToString.number(setupWatch.stop()) + " [ns]");

                setupWatch.start();
                if (ImageWrapper.is8bitData()) {
                    mImage.copyFrom(Wrapper.get8bitData());
                    mStreamScript.forEach_process8bitData(mImage);
                }
                else if (ImageWrapper.is16bitData()) {
                    mImage.copyFrom(Wrapper.get16bitData());
                    mStreamScript.forEach_process16bitData(mImage);
                }
                else {
                    // TODO: error
                }
                Log.e(Thread.currentThread().getName(), "<renderscript foreach_process> time: " + NumToString.number(setupWatch.stop()) + " [ns]");

                setupWatch.start();
                //mStreamScript.forEach_getExposureValueSum(RunningTotal.ExposureValueSum);
                //mStreamScript.forEach_getExposureValue2Sum(RunningTotal.ExposureValue2Sum);
                mStreamScript.forEach_getSignificance(mSignificance); // TODO: do significance
                Log.e(Thread.currentThread().getName(), "<renderscript foreach_get> time: " + NumToString.number(setupWatch.stop()) + " [ns]");
                Log.e(Thread.currentThread().getName(), "<renderscript run()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
                mBacklog.decrementAndGet();
                Log.e(Thread.currentThread().getName(), "Processor Backlog: " + NumToString.number(mBacklog.get()));
            }
        }
        Log.e(Thread.currentThread().getName(), "<process(result, wrapper)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
        mBacklog.incrementAndGet();
        mHandler.post(new Processor(result, wrapper));
    }

    public static boolean isBusy() {
        return mBacklog.get() != 0;
    }

    static int getBacklog() {
        return mBacklog.get();
    }

    static void postProcess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPostScript.set_gNframes(RunningTotal.Nframes);
                mPostScript.set_gExposureSum(RunningTotal.ExposureNanos);
                mPostScript.set_gExposureValueSum(RunningTotal.ExposureValueSum);
                mPostScript.set_gExposureValue2Sum(RunningTotal.ExposureValue2Sum);

                mPostScript.forEach_getMeanRate(PreviousResult.MeanRate);
                mPostScript.forEach_getStdDevRate(PreviousResult.StdDevRate);
                mPostScript.forEach_getStdErrRate(PreviousResult.StdErrRate);
            }
        });
    }

}
