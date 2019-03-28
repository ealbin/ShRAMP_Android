package recycle_bin;

import android.support.annotation.NonNull;
import android.util.Log;

import sci.crayfis.shramp.util.NumToString;

final class StdDevTest {

    private static final int PEEK_SIZE = 100;

    private static double[] mExposureValueSum;
    private static double[] mExposureValue2Sum;

    private static float[] mMean;
    private static float[] mStdDev;
    private static long    nAnomaly;

    private static int mNpixels;
    private static long mNframes;

    private static class Stat {
        private long mCount;

        private class StatStat {
            private float mMax;
            private float mMin;
            private float mSum;

            private StatStat() { reset(); }

            private void reset() {
                mMax = 0.f;
                mMin = 0.f;
                mSum = 0.f;
            }

            private void add(float val) {
                mSum += val;
                if (val > mMax) {
                    mMax = val;
                }
                if (val < mMin) {
                    mMin = val;
                }
            }

            private float getMax() {
                return mMax;
            }

            private float getMin() {
                return mMin;
            }

            private float getAve() {
                return mSum / (float) mCount;
            }
        }

        private final StatStat mValue      = new StatStat();
        private final StatStat mExpValSum  = new StatStat();
        private final StatStat mExpVal2Sum = new StatStat();

        private Stat() { reset(); }

        private void reset() {
            mValue.reset();
            mExpValSum.reset();
            mExpVal2Sum.reset();
            mCount = 0L;
        }

        private void add(float value, float expValSum, float expValSum2) {
            mCount += 1;
            mValue.add(value);
            mExpValSum.add(expValSum);
            mExpVal2Sum.add(expValSum2);
        }

        private float[] getMax() {
            return new float[]{ mValue.getMax(), mExpValSum.getMax(), mExpVal2Sum.getMax() };
        }

        private float[] getMin() {
            return new float[]{ mValue.getMin(), mExpValSum.getMin(), mExpVal2Sum.getMin() };
        }

        private float[] getAve() {
            return new float[]{ mValue.getAve(), mExpValSum.getAve(), mExpVal2Sum.getAve() };
        }
    }
    private static final Stat mStat = new Stat();

    private static final StdDevTest mInstance = new StdDevTest();

    private StdDevTest() {}

    static void init(int npixels) {
        mNpixels = npixels;
        mExposureValueSum  = new double[npixels];
        mExposureValue2Sum = new double[npixels];
        mMean              = new float[npixels];
        mStdDev            = new float[npixels];
    }

    static void resetTotals() {
        for (int i = 0; i < mNpixels; i++) {
            mExposureValueSum[i] = 0.f;
            mExposureValue2Sum[i] = 0.f;
        }
        nAnomaly = 0L;
        mStat.reset();
    }

    static void process8bitData(byte[] values, long exposure) {
        for (int i = 0; i < mNpixels; i++) {
            int value = values[i] & 0xFF;
            mExposureValueSum[i]  += exposure * value;
            mExposureValue2Sum[i] += exposure * value * value;

            //mStat.add(value, exposure*value, exposure*value*value);
        }
    }

    static void process16bitData(short[] values, long exposure) {
        for (int i = 0; i < mNpixels; i++) {
            mExposureValueSum[i]  += exposure * values[i];
            mExposureValue2Sum[i] += exposure * values[i] * values[i];

            //mStat.add(values[i], exposure*values[i], exposure*values[i]*values[i]);
        }
    }

    static void getStats(long exposureSum, long nframes) {

        mNframes = nframes;
        double meanExposure = exposureSum / (double) nframes;

        for (int i = 0; i < mNpixels; i++) {

            double mean = mExposureValueSum[i] / (double) exposureSum;
            double meanRate = mean / meanExposure;

            double var = ( mExposureValue2Sum[i] / (double) exposureSum ) - (mean * mean);
            double stddev;
            if (var < 0.f) {
                nAnomaly += 1;
                stddev = 0.f;
            }
            else {
                stddev = (double) Math.sqrt(var) / meanExposure;
            }
            mMean[i]   = (float) meanRate;
            mStdDev[i] = (float) stddev;
        }

        //float[] statMeans = mStat.getAve();
        //float[] statMaxes = mStat.getMax();
        //float[] statMines = mStat.getMin();

        //String out = " \n\n";
        //out += "\t" + "Values" + "\t" + "ExposureValue" + "\t" + "ExposureValue2" + "\n";
        //out += "means:\n";
        //out += "\t" + NumToString.number(statMeans[0]) + "\t" + NumToString.number(statMeans[1]) + "\t" + NumToString.number(statMeans[2]) + "\n";
        //out += "maxes:\n";
        //out += "\t" + NumToString.number(statMaxes[0]) + "\t" + NumToString.number(statMaxes[1]) + "\t" + NumToString.number(statMaxes[2]) + "\n";
        //out += "mines:\n";
        //out += "\t" + NumToString.number(statMines[0]) + "\t" + NumToString.number(statMines[1]) + "\t" + NumToString.number(statMines[2]) + "\n";
        //out += "\n";
        //Log.e(Thread.currentThread().getName(), out);
    }

    static void peekMeanAndErr() {
        String out = " \n\n";
        out += "-----------------------------------------------------------------------------------\n";
        out += "//  (Java) Mean +/- Error Rates  //////////////////////////////////////////////////\n";
        out += "-----------------------------------------------------------------------------------\n";
        out += "\n";

        String[] mean = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            mean[i] = NumToString.sci(mMean[i]);
        }

        String[] err = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            err[i] = NumToString.sci(mStdDev[i] / Math.sqrt(mNframes));
        }

        int len = 0;
        for (int i = 0; i < PEEK_SIZE; i++) {
            if (mean[i].length() > len) {
                len = mean[i].length();
            }
            if (err[i].length() > len) {
                len = err[i].length();
            }
        }

        for (int i = 0; i < PEEK_SIZE; i++) {
            if (mean[i].length() < len) {
                for (int j = mean[i].length(); j < len; j++) {
                    mean[i] += ".";
                }
            }
            if (err[i].length() < len) {
                for (int j = err[i].length(); j < len; j++) {
                    err[i] += ".";
                }
            }
        }

        for (int i = 0; i < PEEK_SIZE; i++) {
            out += mean[i] + " +/- " + err[i] + "...";
            if ((i + 1) % 10 == 0) {
                out += "\n";
            }
        }
        out += " \n";
        Log.e(Thread.currentThread().getName(), out);
    }

    static void peekExposureValue() {

        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mExposureValueSum[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// (Java) Exposure Value Sum //////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);
    }

    static void peekExposureValue2() {

        String[] values = new String[PEEK_SIZE];
        for (int i = 0; i < PEEK_SIZE; i++) {
            values[i] = NumToString.sci(mExposureValue2Sum[i]);
        }

        String title = "";
        title += "-----------------------------------------------------------------------------------\n";
        title += "// (Java) Exposure Value^2 Sum ////////////////////////////////////////////////////\n";
        title += "-----------------------------------------------------------------------------------\n";

        logValues(title, values);
    }

    static void printAnomaly() {
        Log.e(Thread.currentThread().getName(), "________(Java) Anomalous Std Dev: "
                + NumToString.number(nAnomaly));
    }

    private static void logValues(@NonNull String title, @NonNull String[] values) {
        int len = 0;
        for (int i = 0; i < PEEK_SIZE; i++) {
            int tmp = NumToString.sci(mExposureValue2Sum[i]).length();
            if (tmp > len) {
                len = tmp;
            }
        }

        String out = " \n\n";
        out += title + "\n";
        for (int i = 0; i < PEEK_SIZE; i++) {
            if (values[i].length() < len) {
                for (int j = values[i].length(); j < len; j++) {
                    values[i] += ".";
                }
            }
            out += values[i] + "...";
            if ((i + 1) % 10 == 0) {
                out += "\n";
            }
        }
        out += "\n\n";
        Log.e(Thread.currentThread().getName(), out);
    }

}