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
 * @updated: 3 May 2019
 */

package sci.crayfis.shramp.logging;

import android.support.annotation.NonNull;
import android.util.Log;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Low priority - ultimately a replacement for Log.e() etc
// It works, and I used to use it, but needs to be rebuilt as a queue so it doesn't drop calls

/**
 * Class for sci.crayfis.shramp.logging progress to various output streams
 */
public class ShrampLogger {

    // Defaults available for the general populous to use
    public static final OutStream DEFAULT_STREAM = OutStream.LOG_E;
    public static final DividerStyle DEFAULT_STYLE = DividerStyle.Normal;

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
     *
     * @param outStream stream for sci.crayfis.shramp.logging
     */
    public ShrampLogger(OutStream outStream) {
        mOutStream = outStream;
    }

    /**
     * Log a message to output stream.
     * Auto-tag is "Thread (priority) Filename : line number : method name()"
     *
     * @param message message to log
     */
    public void log(String message) {
        final int TRACE_INDEX = 4;

        if (mOutStream == OutStream.DISABLED) {
            return;
        }

        synchronized (PRINT_LOCK) {
            // Tag = Thread (priority) Filename : line number : method name()

            Thread currentThread = Thread.currentThread();
            String threadName = currentThread.getName();
            String priority = Integer.toString(currentThread.getPriority());
            StackTraceElement traceElement = currentThread.getStackTrace()[TRACE_INDEX];
            String fileName = traceElement.getFileName();
            String lineNumber = Integer.toString(traceElement.getLineNumber());
            String methodName = traceElement.getMethodName();

            String tag = threadName + " (" + priority + ") "
                    + fileName + " : " + lineNumber + " : " + methodName + "()";

            if (mOutStream == OutStream.LOG_E) {
                Log.e(tag, message);
                return;
            }
        }
    }

    /**
     * Log a separation line
     *
     * @param style thickness of line
     */
    public void divider(DividerStyle style) {
        synchronized (PRINT_LOCK) {
            if (mOutStream == OutStream.DISABLED) {
                return;
            }

            if (style == null) {
                style = DEFAULT_STYLE;
            }
            ;

            String divider = null;
            if (style == DividerStyle.Weak) {
                divider = WEAK_DIVIDER;
            }
            if (style == DividerStyle.Normal) {
                divider = NORM_DIVIDER;
            }
            if (style == DividerStyle.Strong) {
                divider = STRONG_DIVIDER;
            }

            if (mOutStream == OutStream.LOG_E) {
                Log.e(":", divider);
            }
        }
    }

}