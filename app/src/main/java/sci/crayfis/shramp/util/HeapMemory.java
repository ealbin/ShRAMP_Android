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
import android.util.Log;

import sci.crayfis.shramp.GlobalSettings;

/**
 * Convenient monitor of available heap memory
 */
@TargetApi(21)
abstract public class HeapMemory {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // MEBIBYTE.....................................................................................
    // 1 Mebibyte is 2^20 bytes, memory returned from mRuntime is in bytes
    private static final long MEBIBYTE = 1048576L; // 2^20

    // mRuntime.....................................................................................
    // Reference to Java Runtime object (the interface with the environment currently running)
    private static final Runtime mRuntime = Runtime.getRuntime();

    // mStopWatch...................................................................................
    // For now, monitoring performance -- (TODO) to be removed later
    private static final StopWatch mStopWatch = new StopWatch();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getAvailableMiB..............................................................................
    /**
     * @return the amount of heap memory available to the application
     */
    public static long getAvailableMiB() {
        long maxHeapMiB = mRuntime.maxMemory() / MEBIBYTE;
        long usedMiB    = ( mRuntime.totalMemory() - mRuntime.freeMemory() ) / MEBIBYTE;
        long available = maxHeapMiB - usedMiB;
        mStopWatch.addTime();
        return available;
    }

    // logAvailableMiB..............................................................................
    /**
     * Log the amount of heap memory available to the application
     */
    public static void logAvailableMiB() {
        Log.e(Thread.currentThread().getName(), "Available Heap Memory: "
                + NumToString.number(getAvailableMiB()) + " [MiB]");
    }

    // isMemoryAmple.................................................................................
    /**
     * @return true if memory available is greater than GlobalSettings.AMPLE_MEMORY_MiB
     */
    public static boolean isMemoryAmple() {
        return getAvailableMiB() > GlobalSettings.AMPLE_MEMORY_MiB;
    }

    // isMemoryLow..................................................................................
    /**
     * @return true if memory available is less than GlobalSettings.LOW_MEMORY_MiB
     */
    public static boolean isMemoryLow() {
        return getAvailableMiB() < GlobalSettings.LOW_MEMORY_MiB;
    }

    /**
     * TODO: remove
     */
    public static void logPerformance() {
        Log.e(Thread.currentThread().getName(), mStopWatch.getPerformance());
    }

}