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
public class HandlerManager {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandlerHelpers..............................................................................
    // TODO: description
    private final static List<HandlerHelper> mHandlerHelpers = new ArrayList<>();

    // mInstance....................................................................................
    // TODO: description
    private final static HandlerManager mInstance = new HandlerManager();

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mUntitledThreadsCount........................................................................
    // TODO: description
    private static Integer mUntitledThreadsCount = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Inner Classes
    //---------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * TODO: description, comments and logging
     */
    private class HandlerHelper {

        //******************************************************************************************
        // Class Fields
        //-------------

        // Private
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

        //******************************************************************************************
        // Constructors
        //-------------

        // Private
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

        //******************************************************************************************
        // Class Methods
        //--------------

        // Private
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

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // HandlerManager...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private HandlerManager() {}

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
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
        HandlerHelper helper = mInstance.new HandlerHelper(name, priority);
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