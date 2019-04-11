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

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;

import org.jetbrains.annotations.Contract;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public final class StopWatch {

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mStartNanos..................................................................................
    // TODO: description
    private long mStartNanos;

    // mStopNanos...................................................................................
    // TODO: description
    private long mStopNanos;

    // mSum.........................................................................................
    // TODO: description
    private long mSum;

    // mCount.......................................................................................
    // TODO: description
    private long mCount;

    // mLongest.....................................................................................
    // TODO: description
    private long mLongest;

    // mShortest....................................................................................
    // TODO: description
    private long mShortest;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // StopWatch....................................................................................
    /**
     * TODO: description, comments and logging
     */
    public StopWatch() { reset(); }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // start........................................................................................
    /**
     * TODO: description, comments and logging
     */
    public void start() {
        mStartNanos = SystemClock.elapsedRealtimeNanos();
        mStopNanos  = mStartNanos;
    }

    // stop.........................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public long stop() {
        mStopNanos = SystemClock.elapsedRealtimeNanos();
        long elapsed = mStopNanos - mStartNanos;
        mStartNanos = mStopNanos;
        return elapsed;
    }

    // addTime......................................................................................
    /**
     * TODO: description, comments and logging
     * @param time bla
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
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public double getMean() {
        return mSum / (double) mCount;
    }

    // reset........................................................................................
    /**
     * TODO: description, comments and logging
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
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public long getLongest() {
        return mLongest;
    }

    // getShortest..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public long getShortest() {
        return mShortest;
    }

}