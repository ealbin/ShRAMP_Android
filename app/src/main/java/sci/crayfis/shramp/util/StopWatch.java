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

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Convenient stop watch class for benchmarking performance
 */
@TargetApi(21)
public final class StopWatch {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mLabeledStopWatches..........................................................................
    // An array of every stop watch ever created (with a label)
    private static final List<StopWatch> mLabeledStopWatches = new ArrayList<>();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mStartNanos..................................................................................
    // System nanosecond time epoch when stopwatch is started
    private long mStartNanos;

    // mStopNanos...................................................................................
    // System nanosecond time epoch when stopwatch is stopped/sampled
    private long mStopNanos;

    // mSum.........................................................................................
    // Ever-growing sum of total elapsed time when addTime() is called
    private long mSum;

    // mCount.......................................................................................
    // The number of entries contained in mSum (number of times stop watch is stopped/sampled)
    private long mCount;

    // mLongest.....................................................................................
    // The longest elapsed time measured so far
    private long mLongest;

    // mShortest....................................................................................
    // The shortest elapsed time measured so far
    private long mShortest;

    // mLabel.......................................................................................
    // A short label describing this StopWatch
    private String mLabel;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // StopWatch....................................................................................
    /**
     * Create a new stop watch, mark current system nanosecond time as start epoch
     * (Not kept in master list of stopwatches)
     */
    public StopWatch() {
        mLabel = null;
        reset();
    }

    // StopWatch....................................................................................
    /**
     * Create a new stop watch, mark current system nanosecond time as start epoch
     * (A reference is kept in the master list of stopwatches)
     * @param label A short string labeling this StopWatch
     */
    public StopWatch(@NonNull String label) {
        mLabel = label;
        mLabeledStopWatches.add(this);
        reset();
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getLabeledPerformances...........................................................................
    /**
     * @return A String summarizing performance of all stop watches with labels of the format:
     *          "Label:
     *             Count = www, Shortest = zzzzzz [ns], Mean = xxxxx [ns], Longest = yyyyy [ns]"
     */
    @NonNull
    public static String getLabeledPerformances() {
        // Sort longest mean to shortest mean
        Comparator<StopWatch> comparator = new Comparator<StopWatch>() {
            @Override
            public int compare(StopWatch o1, StopWatch o2) {
                if (o1.mCount == 0 || o2.mCount == 0) {
                    return Double.compare(o2.mCount, o1.mCount);
                }
                return Double.compare(o2.getMean(), o1.getMean());
            }
        };
        Collections.sort(mLabeledStopWatches, comparator);
        String out = " \n Stop watch results: \n\n ";
        for (StopWatch stopwatch : mLabeledStopWatches) {
            out += stopwatch.mLabel + ":\n" + stopwatch.getPerformance() + "\n\n";
        }
        return out + " ";
    }

    // resetLabeled.................................................................................
    /**
     * Resets all stopwatches with labels
     */
    public static void resetLabeled() {
        for (StopWatch stopWatch : mLabeledStopWatches) {
            stopWatch.reset();
        }
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // start........................................................................................
    /**
     * Start a new measurement interval
     */
    public void start() {
        mStartNanos = SystemClock.elapsedRealtimeNanos();
        mStopNanos  = mStartNanos;
    }

    // stop.........................................................................................
    /**
     * Stop current measurement interval
     * @return elapsed nanoseconds
     */
    public long stop() {
        mStopNanos = SystemClock.elapsedRealtimeNanos();
        long elapsed = mStopNanos - mStartNanos;
        mStartNanos = mStopNanos;
        return elapsed;
    }

    // addTime......................................................................................
    /**
     * Stop current measurement interval and add the elapsed time to the running total
     */
    public void addTime() {
        addTime(stop());
    }

    // addTime......................................................................................
    /**
     * Add an elapsed time to the running total
     * @param time Time to add to the running total
     */
    public void addTime(long time) {
        mSum   += time;
        mCount += 1;
        if (time > mLongest) {
            mLongest = time;
        }

        if (mShortest == 0L) {
            mShortest = time;
        }
        else if (time < mShortest) {
            mShortest = time;
        }
    }

    // getMean......................................................................................
    /**
     * @return Average elapsed time from addTime() calls
     */
    @Contract(pure = true)
    public double getMean() {
        return mSum / (double) mCount;
    }

    // reset........................................................................................
    /**
     * Reset/clear this stop watch
     */
    public void reset() {
        mSum   = 0L;
        mCount = 0L;
        mLongest  = 0L;
        mShortest = 0L;
        start();
    }

    // getLongest...................................................................................
    /**
     * @return The longest recorded elapsed time from addTime()
     */
    @Contract(pure = true)
    public long getLongest() {
        return mLongest;
    }

    // getShortest..................................................................................
    /**
     * @return The shortest recorded elapsed time from addTime()
     */
    @Contract(pure = true)
    public long getShortest() {
        return mShortest;
    }

    // getPerformance...............................................................................
    /**
     * @return A String summarizing performance from addTime() of the format:
     *          "Count = www, Shortest = zzzzzz [ns], Mean = xxxxx [ns], Longest = yyyyy [ns]"
     */
    @NonNull
    public String getPerformance() {
        String out = "\t";
        out += "Count = " + NumToString.number(mCount)
                + ", Shortest = " + NumToString.number(mShortest) + " [ns]"
                + ", Mean = " + NumToString.number(Math.round(getMean())) + " [ns]"
                + ", Longest = " + NumToString.number(mLongest) + " [ns]";
        return out;
    }

}