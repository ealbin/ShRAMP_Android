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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class HandlerManager {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandlerHelpers..............................................................................
    // TODO: description
    private final static List<HandlerHelper> mHandlerHelpers = new ArrayList<>();

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mUntitledThreadsCount........................................................................
    // TODO: description
    private static Integer mUntitledThreadsCount = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Private Inner Classes
    //----------------------

    /**
     * TODO: description, comments and logging
     */
    private static class HandlerHelper {

        // Private Instance Fields
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // nHandler.................................................................................
        // TODO: description
        private Handler nHandler;

        // nHandlerThread...........................................................................
        // TODO: description
        private HandlerThread nHandlerThread;

        ////////////////////////////////////////////////////////////////////////////////////////////
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
        ////////////////////////////////////////////////////////////////////////////////////////////

        // Constructors
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // HandlerHelper............................................................................
        /**
         * TODO: description, comments and logging
         * @param name bla
         * @param priority bla
         */
        private HandlerHelper(@Nullable String name,@Nullable Integer priority) {
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
         * TODO: description, comments and logging
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
     * TODO: description, comments and logging
     * @param name bla
     * @param priority bla
     * @return bla
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
     * TODO: description, comments and logging
     */
    public static void finish() {
        Log.e(Thread.currentThread().getName(), "Handler finish");
        for (HandlerHelper helper : mHandlerHelpers) {
            helper.finish();
        }
        mHandlerHelpers.clear();
    }

}