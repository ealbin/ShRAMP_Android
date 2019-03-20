package sci.crayfis.shramp.util;

public class HeapMemory {

    private static final Runtime mRuntime = Runtime.getRuntime();

    private static final HeapMemory mInstance = new HeapMemory();

    private static final Long MEBIBYTE = 1048576L; // 2^20

    private HeapMemory() {}

    public static Long getAvailableMiB() {
        long maxHeapMiB = mRuntime.maxMemory() / MEBIBYTE;
        long usedMiB    = ( mRuntime.totalMemory() - mRuntime.freeMemory() ) / MEBIBYTE;
        return maxHeapMiB - usedMiB;
    }
}
