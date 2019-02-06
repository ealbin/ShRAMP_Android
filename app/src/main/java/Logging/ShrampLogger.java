package Logging;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Class for logging progress to various output streams
 */
public class ShrampLogger {

    // Defaults available for the general populous to use
    public static final OutStream    DEFAULT_STREAM = OutStream.LOG_E;
    public static final DividerStyle DEFAULT_STYLE  = DividerStyle.Normal;

    // Internal style settings
    private static final String WEAK_DIVIDER =
            ".....................................................................................";
    private static final String NORM_DIVIDER =
            "-------------------------------------------------------------------------------------";
    private static final String STRONG_DIVIDER =
            "=====================================================================================";

    // Lock to prevent multiple threads from printing at once
    private static final Object PRINT_LOCK = new Object();

    // Output stream chosen
    private OutStream mOutStream;

    /**
     * Simple constructor, choose output stream
     * @param outStream stream for logging
     */
    public ShrampLogger(OutStream outStream) {
        mOutStream = outStream;
    }

    /**
     * Log a message to output stream
     * @param tag a tag before the message
     * @param message message to log
     */
    public void log(String tag, String message) {
        synchronized (PRINT_LOCK) {
            if (mOutStream == OutStream.DISABLED) { return; }
            if (mOutStream == OutStream.LOG_E) { Log.e(tag, message); return; }
        }
    }

    /**
     * Log a separation line
     * @param style thickness of line
     */
    public void divider(DividerStyle style) {
        synchronized (PRINT_LOCK) {
            if (mOutStream == OutStream.DISABLED) {return;}

            if (style == null) { style = DEFAULT_STYLE; };

            String divider = null;
            if (style == DividerStyle.Weak)   { divider = WEAK_DIVIDER; }
            if (style == DividerStyle.Normal) { divider = NORM_DIVIDER; }
            if (style == DividerStyle.Strong) { divider = STRONG_DIVIDER; }

            if (mOutStream == OutStream.LOG_E) { Log.e(":", divider); }
        }
    }

    /**
     * Dump info on this (and previous) calls to log
     */
    public void logTrace() {
        synchronized (PRINT_LOCK) {
            if (mOutStream == OutStream.DISABLED) {return;}

            if (mOutStream == OutStream.LOG_E) { Log.e("stack trace: ", getString()); }
        }
    }

    /**
     * Assemble From: / To: string
     * @return
     */
    @NonNull
    private String getString() {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        // element 0:  Thread.currentThread
        // element 1:  ShrampLogger.toString
        // element 2:  calling method
        // element 3:  previous call
        if (traceElements.length < 6) {
            return "error: stack trace has less than 4 elements.";
        }
        String threadName = Thread.currentThread().getName();
        String priority   = Integer.toString(Thread.currentThread().getPriority());

        String currently  = makeString(traceElements[4]);
        //String previously = makeString(traceElements[5]);

        return " \n"
                + "Right now: \n"
                + "\t" + "Thread = " + threadName + ", Priority = " + priority + "\n"
                + currently + "\n";
                //+ "Thread/Priority: " + threadName + "/" + priority + "\n"
                //+ "From: \n" + previously + "\n"
                //+ "To: \n"   + currently  + "\n";
    }

    /**
     * Helper for getString()
     * @param element StackTraceElement to stringify
     * @return the info
     */
    private String makeString(StackTraceElement element) {
        String fileName   = element.getFileName();
        String lineNumber = Integer.toString(element.getLineNumber());
        String className  = element.getClassName();
        String methodName = element.getMethodName();

        return    "\t" + "File name   = " + fileName   + "\n"
                + "\t" + "Line number = " + lineNumber + "\n"
                + "\t" + "Class name  = " + className  + "\n"
                + "\t" + "Method name = " + methodName + "\n";
                //":Line number = "  + fileName  + ":" + lineNumber + "\n"
                //+ "\t" + "Class name.Method name = " + className + "." + methodName;
    }
}
