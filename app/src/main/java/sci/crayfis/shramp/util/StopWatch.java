package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // StopWatch....................................................................................
    /**
     * TODO: description, comments and logging
     */
    public StopWatch() {
        start();
    }

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
}