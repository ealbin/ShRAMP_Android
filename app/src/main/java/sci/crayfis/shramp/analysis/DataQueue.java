package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class DataQueue {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "DataQueueThread";

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
            GlobalSettings.DATA_QUEUE_THREAD_PRIORITY);

    // ACCESS_LOCK..................................................................................
    // TODO: description
    private static final Object ACCESS_LOCK = new Object();

    // mCaptureResultQueue..........................................................................
    // TODO: description
    private static final List<TotalCaptureResult> mCaptureResultQueue = new ArrayList<>();

    // mImageDataQueue..............................................................................
    // TODO: description
    private static final List<ImageWrapper> mImageQueue = new ArrayList<>();

    // ProcessQueue.................................................................................
    // TODO: description
    private static final Runnable ProcessQueue = new Runnable() {
        @Override
        public void run() {
            while(processQueue()) {
                synchronized (ACCESS_LOCK) {
                    Log.e(Thread.currentThread().getName(), "Capture Result Queue Size: " + NumToString.number(mCaptureResultQueue.size())
                    + ", Image Queue Size: " + NumToString.number(mImageQueue.size())
                    + ", Processor Backlog: " + NumToString.number(ImageProcessor.getBacklog()));
                }
            };
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // add..........................................................................................
    /**
     * TODO: description, comments and logging
     * @param result bla
     */
    public static void add(@NonNull TotalCaptureResult result) {
        StopWatch stopWatch = new StopWatch();
        Log.e("DATA QUEUE", "added total capture result");
        class Add implements Runnable {
            private TotalCaptureResult mResult;

            private Add(TotalCaptureResult result) {
                mResult = result;
            }
            public void run() {
                synchronized (ACCESS_LOCK) {
                    mCaptureResultQueue.add(mResult);
                }
            }
        }

        if (!GlobalSettings.DEBUG_DISABLE_QUEUE) {
            mHandler.post(new Add(result));
        }
        Log.e(Thread.currentThread().getName(), "<add(TotalCaptureResult)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
    }

    // add..........................................................................................
    /**
     * TODO: description, comments and logging
     * @param wrapper bla
     */
    public static void add(@NonNull ImageWrapper wrapper) {
        StopWatch stopWatch = new StopWatch();
        Log.e("DATA QUEUE", "added image data");
        class Add implements Runnable {
            private ImageWrapper mWrapper;
            private Add(ImageWrapper wrapper) {
                mWrapper = wrapper;
            }
            public void run() {
                synchronized (ACCESS_LOCK) {
                    mImageQueue.add(mWrapper);
                }
            }
        }
        if (!GlobalSettings.DEBUG_DISABLE_QUEUE) {
            mHandler.post(new Add(wrapper));
            mHandler.post(ProcessQueue);
        }
        Log.e(Thread.currentThread().getName(), "<add(ImageWrapper)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
    }

    // isEmpty......................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static boolean isEmpty() {
        synchronized (ACCESS_LOCK) {
            return (mCaptureResultQueue.size() == 0) && (mImageQueue.size() == 0);
        }
    }

    // purge........................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void purge() {
        if (!GlobalSettings.DEBUG_DISABLE_QUEUE) {
            if (!isEmpty()) {
                mHandler.post(ProcessQueue);
            }
        }
    }

    // clear........................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void clear() {
        synchronized (ACCESS_LOCK) {
            mCaptureResultQueue.clear();
            mImageQueue.clear();
        }
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // processQueue.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    private static boolean processQueue() {
        StopWatch stopWatch = new StopWatch();
        Log.e("DATA QUEUE", "processing queue");
        synchronized (ACCESS_LOCK) {
            int resultSize = mCaptureResultQueue.size();
            int imageSize = mImageQueue.size();

            if (resultSize > 0 && imageSize > 0) {
                TotalCaptureResult result = mCaptureResultQueue.remove(0);
                ImageWrapper wrapper = mImageQueue.remove(0);
                if (!GlobalSettings.DEBUG_DISABLE_PROCESSING) {
                    ImageProcessor.process(result, wrapper);
                }
                Log.e(Thread.currentThread().getName(), "<processQueue()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
                return (resultSize != 1 && imageSize != 1);
            }
            Log.e(Thread.currentThread().getName(), "<processQueue()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
            return false;
        }
    }

}