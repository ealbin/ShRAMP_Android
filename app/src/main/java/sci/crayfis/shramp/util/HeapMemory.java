package sci.crayfis.shramp.util;

import android.util.Log;

import sci.crayfis.shramp.GlobalSettings;

abstract public class HeapMemory {

    private static final Runtime mRuntime = Runtime.getRuntime();

    //private static final HeapMemory mInstance = new HeapMemory();

    private static final long MEBIBYTE = 1048576L; // 2^20

    //private HeapMemory() {}

    public static long getAvailableMiB() {
        long maxHeapMiB = mRuntime.maxMemory() / MEBIBYTE;
        long usedMiB    = ( mRuntime.totalMemory() - mRuntime.freeMemory() ) / MEBIBYTE;
        return maxHeapMiB - usedMiB;
    }

    public static boolean isMemoryLow() {
        return getAvailableMiB() < GlobalSettings.LOW_MEMORY_MiB;
    }

    public static boolean isMemoryGood() {
        return getAvailableMiB() > GlobalSettings.SUFFICIENT_MEMORY_MiB;
    }

    public static void logAvailableMiB() {
        Log.e(Thread.currentThread().getName(), "Available Heap Memory: "
                + NumToString.number(getAvailableMiB()) + " [MiB]");
    }
}
