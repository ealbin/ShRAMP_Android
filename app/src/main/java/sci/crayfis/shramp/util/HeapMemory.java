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
import android.util.Log;

import sci.crayfis.shramp.GlobalSettings;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class HeapMemory {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mRuntime.....................................................................................
    // TODO: description
    private static final Runtime mRuntime = Runtime.getRuntime();

    // MEBIBYTE.....................................................................................
    // TODO: description
    private static final long MEBIBYTE = 1048576L; // 2^20

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getAvailableMiB..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static long getAvailableMiB() {
        long maxHeapMiB = mRuntime.maxMemory() / MEBIBYTE;
        long usedMiB    = ( mRuntime.totalMemory() - mRuntime.freeMemory() ) / MEBIBYTE;
        return maxHeapMiB - usedMiB;
    }

    // logAvailableMiB..............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void logAvailableMiB() {
        Log.e(Thread.currentThread().getName(), "Available Heap Memory: "
                + NumToString.number(getAvailableMiB()) + " [MiB]");
    }

    // isMemoryAmple.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static boolean isMemoryAmple() {
        return getAvailableMiB() > GlobalSettings.AMPLE_MEMORY_MiB;
    }

    // isMemoryLow..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static boolean isMemoryLow() {
        return getAvailableMiB() < GlobalSettings.LOW_MEMORY_MiB;
    }

}