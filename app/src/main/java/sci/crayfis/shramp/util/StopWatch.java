package sci.crayfis.shramp.util;

public final class StopWatch {
    private long mStartNanos;
    private long mStopNanos;

    public StopWatch() {
        start();
    }

    public void start() {
        mStartNanos = System.nanoTime();
        mStopNanos  = mStartNanos;
    }

    public long stop() {
        mStopNanos = System.nanoTime();
        long elapsed = mStopNanos - mStartNanos;
        mStartNanos = mStopNanos;
        return elapsed;
    }
}
