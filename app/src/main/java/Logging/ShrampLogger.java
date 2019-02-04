package Logging;

import android.support.annotation.NonNull;
import android.util.Log;

public class ShrampLogger {

    private static final String DIVIDER =
            "-------------------------------------------------------------------------------------";

    private OutStream mOutStream;

    public ShrampLogger(OutStream outStream) {
        mOutStream = outStream;
    }

    public void log(String tag, String message) {
        if (mOutStream == OutStream.LOG_E) {
            Log.e(tag, message);
        }
    }

    public void divider() {
        if (mOutStream == OutStream.LOG_E) {
            Log.e("", DIVIDER);
        }
    }

    public void logTrace() {
        if (mOutStream == OutStream.LOG_E) {
            Log.e("stack trace: ", getString());
        }
    }

    @NonNull
    private String getString() {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        // element 0:  Thread.currentThread
        // element 1:  ShrampLogger.toString
        // element 2:  calling method
        // element 3:  previous call
        if (traceElements.length < 4) {
            return "error: stack trace has less than 4 elements.";
        }
        String currently  = makeString(traceElements[2]);
        String previously = makeString(traceElements[3]);

        return "Currently: \n" + currently + "\n"
                + "Previously: \n" + previously + "\n";
    }
    private String makeString(StackTraceElement element) {
        String fileName   = element.getFileName();
        String lineNumber = Integer.toString(element.getLineNumber());
        String className  = element.getClassName();
        String methodName = element.getMethodName();

        return    "\t" + "File name:Line number = "  + fileName  + ":" + lineNumber + "\n"
                + "\t" + "Class name.Method name = " + className + "." + methodName;
    }
}
