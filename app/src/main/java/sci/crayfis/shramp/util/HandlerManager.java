package sci.crayfis.shramp.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HandlerManager {

    //**********************************************************************************************
    // Class Variables
    //----------------

    private final static HandlerManager mInstance            = new HandlerManager();
    private final static List<HandlerHelper> mHandlerHelpers = new ArrayList<>();


    //**********************************************************************************************
    // Inner Classes
    //--------------

    private class HandlerHelper {

        private int nUntitledCount = 0;

        //..........................................................................................

        private HandlerThread nHandlerThread;
        private Handler       nHandler;

        ////////////////////////////////////////////////////////////////////////////////////////////

        private HandlerHelper(@Nullable String name,@Nullable Integer priority) {
            if (name == null) {
                name = "Untitled thread: " + Integer.toString(nUntitledCount);
                nUntitledCount += 1;
            }

            if (priority == null) {
                priority = Process.THREAD_PRIORITY_DEFAULT;
            }

            nHandlerThread = new HandlerThread(name, priority);
            nHandlerThread.start();  // must start before calling .getLooper()
            nHandler = new Handler(this.nHandlerThread.getLooper());
        }

        private Handler getHandler() { return nHandler; }

        private void finish() {
            nHandlerThread.quitSafely();
        }
    }


    //**********************************************************************************************
    // Class Methods
    //--------------

    private HandlerManager() {}

    public static Handler newHandler(@Nullable String name, @Nullable Integer priority) {
        HandlerHelper helper = mInstance.new HandlerHelper(name, priority);
        mHandlerHelpers.add(helper);
        return helper.getHandler();
    }

    public static void finish() {
        for (HandlerHelper helper : mHandlerHelpers) {
            helper.finish();
        }
        mHandlerHelpers.clear();
    }

}
