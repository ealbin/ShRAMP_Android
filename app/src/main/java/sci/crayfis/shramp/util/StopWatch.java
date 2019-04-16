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

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Convenient stop watch class for benchmarking performance
 */
@TargetApi(21)
public final class StopWatch {

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // StopWatch....................................................................................
    /**
     * Create a new stop watch, mark current system nanosecond time as start epoch
     */
    public StopWatch() { reset(); }

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
     *          "Mean = xxxxx [ns], Longest = yyyyy [ns], shortest = zzzzzz [ns]"
     */
    public String getPerformance() {
        String out = "";
        out += "Mean = " + NumToString.number(getMean()) + " [ns]"
                + ", Longest = " + NumToString.number(getLongest()) + " [ns]"
                + ", Shortest = " + NumToString.number(getShortest()) + " [ns]";
        return out;
    }

}