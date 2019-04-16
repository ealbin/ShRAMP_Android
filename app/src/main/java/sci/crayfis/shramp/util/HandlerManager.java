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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the creation and finish of all running threads.
 * Call newHandler() to start a new thread, and finish() to shut all threads down.
 */
@TargetApi(21)
abstract public class HandlerManager {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandlerHelpers..............................................................................
    // A list of all running threads
    private final static List<HandlerHelper> mHandlerHelpers = new ArrayList<>();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mUntitledThreadsCount........................................................................
    // A count of threads without explicitly specified names
    private static Integer mUntitledThreadsCount = 0;

    // Private Inner Class
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * The HandlerHelper encapsulates a thread's Handler into a convenient bundle
     */
    private static class HandlerHelper {

        // nHandler.................................................................................
        // The thread's Handler contained by this helper instance
        private Handler nHandler;

        // nHandlerThread...........................................................................
        // The thread's HandlerThread contained by this helper instance
        private HandlerThread nHandlerThread;

        // Constructors
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // HandlerHelper............................................................................
        /**
         * Start up a new thread with name 'name'
         * @param name Optional name for the thread
         * @param priority Optional priority for the thread
         */
        private HandlerHelper(@Nullable String name, @Nullable Integer priority) {
            if (name == null) {
                name = "Untitled thread: " + Integer.toString(mUntitledThreadsCount);
                mUntitledThreadsCount += 1;
            }
            Log.e(Thread.currentThread().getName(), "HandlerHelper HandlerHelper: " + name);
            if (priority == null) {
                priority = Process.THREAD_PRIORITY_DEFAULT;
            }

            nHandlerThread = new HandlerThread(name, priority);
            nHandlerThread.start();  // must start before calling .getLooper()
            nHandler = new Handler(this.nHandlerThread.getLooper());
        }

        // Instance Methods
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // finish...................................................................................
        /**
         * Shut down the thread
         */
        private void finish() {
            Log.e(Thread.currentThread().getName(), "HandlerHelper quit safely: " + nHandlerThread.getName());
            nHandlerThread.quitSafely();
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // newHandler...................................................................................
    /**
     * Start up a new thread named 'name' with priority 'priority'
     * @param name Name of new thread
     * @param priority Priority of new thread
     * @return Handler to new thread
     */
    @NonNull
    public static Handler newHandler(@Nullable String name, @Nullable Integer priority) {
        Log.e(Thread.currentThread().getName(), "Handler newHandler: " + name);
        HandlerHelper helper = new HandlerHelper(name, priority);
        mHandlerHelpers.add(helper);
        return helper.nHandler;
    }

    // finish.......................................................................................
    /**
     * Shut down **all** running threads started by this class
     */
    public static void finish() {
        Log.e(Thread.currentThread().getName(), "Handler finish");
        for (HandlerHelper helper : mHandlerHelpers) {
            helper.finish();
        }
        mHandlerHelpers.clear();
    }

}