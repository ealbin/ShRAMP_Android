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

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;
import sci.crayfis.shramp.util.StorageMedia;

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

    // mOutputQueue.................................................................................
    // TODO: description
    private static final List<OutputWrapper> mOutputQueue = new ArrayList<>();

    // ProcessQueue.................................................................................
    // TODO: description
    private static class ProcessQueue implements Runnable {
        static boolean mPurge = false;

        ProcessQueue setPurge() {
            mPurge = true;
            return this;
        }

        ProcessQueue noPurge() {
            mPurge = false;
            return this;
        }

        @Override
        public void run() {
            while (processQueue(mPurge)) {
                synchronized (ACCESS_LOCK) {
                    Log.e(Thread.currentThread().getName(), "Capture Result Queue Size: " + NumToString.number(mCaptureResultQueue.size())
                            + ", Image Queue Size: " + NumToString.number(mImageQueue.size())
                            + ", Processor Backlog: " + NumToString.number(ImageProcessor.getBacklog()));
                }
            }
            ;
        }
    }
    private static final ProcessQueue ProcessQueue = new ProcessQueue();

    private static final Runnable WriteOutput = new Runnable() {
        @Override
        public void run() {
            while (writeOutput()) {
                //synchronized (ACCESS_LOCK) {
                    Log.e(Thread.currentThread().getName(), "Output Queue Size: " + NumToString.number(mOutputQueue.size())
                            + ", I/O Backlog: " + NumToString.number(StorageMedia.getBacklog()));
                //}
            }
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
     *
     * @param result bla
     */
    public static void add(@NonNull TotalCaptureResult result) {
        StopWatch stopWatch = new StopWatch();
        Long time = result.get(CaptureResult.SENSOR_TIMESTAMP);
        assert time != null;
        Log.e("DATA QUEUE", "added total capture result " + TimeCode.toString(time));
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
        //Log.e(Thread.currentThread().getName(), "<add(TotalCaptureResult)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
    }

    // add..........................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param wrapper bla
     */
    public static void add(@NonNull ImageWrapper wrapper) {
        StopWatch stopWatch = new StopWatch();
        Log.e("DATA QUEUE", "added image data " + wrapper.getTimeCode());
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
            mHandler.post(ProcessQueue.noPurge());
        }
        //Log.e(Thread.currentThread().getName(), "<add(ImageWrapper)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
    }

    public static void add(@NonNull OutputWrapper wrapper) {
        StopWatch stopWatch = new StopWatch();
        Log.e("DATA QUEUE", "added output");
        class Add implements Runnable {
            private OutputWrapper mWrapper;

            private Add(OutputWrapper wrapper) {
                mWrapper = wrapper;
            }

            public void run() {
                //synchronized (ACCESS_LOCK) {
                    mOutputQueue.add(mWrapper);
                //}
            }
        }
        if (!GlobalSettings.DEBUG_DISABLE_QUEUE) {
            mHandler.post(new Add(wrapper));
            mHandler.post(WriteOutput);
        }
        //Log.e(Thread.currentThread().getName(), "<add(OutputWrapper)> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
    }

    // isEmpty......................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @return bla
     */
    public static boolean isEmpty() {
        synchronized (ACCESS_LOCK) {
            int resultSize = mCaptureResultQueue.size();
            int imageSize = mImageQueue.size();
            int outputSize = mOutputQueue.size();

            //if (imageSize == 0 && resultSize > 0) {
            //    ImageReaderListener.purge();
            //}
            //Log.e(Thread.currentThread().getName(), "R, I, O = " + Integer.toString(resultSize) +", "
            //+ Integer.toString(imageSize) + ", " + Integer.toString(outputSize));
            return (resultSize == 0) && (imageSize == 0) && (outputSize == 0);
        }
    }

    // purge........................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void purge() {
        if (!GlobalSettings.DEBUG_DISABLE_QUEUE) {
            if (!isEmpty()) {
                mHandler.post(ProcessQueue.setPurge());
                mHandler.post(WriteOutput);
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
            mOutputQueue.clear();
        }
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // processQueue.................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @return bla
     */
    private static boolean processQueue(boolean purging) {
        StopWatch stopWatch = new StopWatch();
        Log.e("DATA QUEUE", "processing queue");
        synchronized (ACCESS_LOCK) {
            int resultSize = mCaptureResultQueue.size();
            int imageSize = mImageQueue.size();

            if (resultSize > 0 && imageSize > 0) {
                TotalCaptureResult result = mCaptureResultQueue.remove(0);
                ImageWrapper wrapper = mImageQueue.remove(0);

                Long result_timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
                assert result_timestamp != null;
                String result_timecode = TimeCode.toString(result_timestamp);
                String wrapper_timecode = wrapper.getTimeCode();
                if (!result_timecode.equals(wrapper_timecode)) {
                    Log.e(Thread.currentThread().getName(), "!!!!!!!!!!!!!!!! Time Code Miss-Match: "
                    + result_timecode + " != " + wrapper_timecode);

                    resultSize = mCaptureResultQueue.size();
                    imageSize  = mImageQueue.size();
                    if (resultSize == 0 || imageSize == 0) {

                        if (purging) {
                            Log.e(Thread.currentThread().getName(), "Purging queues");
                            mCaptureResultQueue.clear();
                            mImageQueue.clear();
                            return false;
                        }

                        Log.e(Thread.currentThread().getName(), "Requeing both image and result");
                        if (resultSize == 0) {
                            mCaptureResultQueue.add(result);
                        }
                        else {
                            mCaptureResultQueue.add(0, result);
                        }

                        if (imageSize == 0) {
                            mImageQueue.add(wrapper);
                        }
                        else {
                            mImageQueue.add(0, wrapper);
                        }
                        return false;
                    }
                    
                    TotalCaptureResult nextResult = mCaptureResultQueue.get(0);
                    ImageWrapper nextWrapper = mImageQueue.get(0);

                    String nextResultCode = TimeCode.toString(nextResult.get(CaptureResult.SENSOR_TIMESTAMP));
                    String nextWrapperCode = nextWrapper.getTimeCode();

                    if (wrapper_timecode.equals(nextResultCode)) {
                        Log.e(Thread.currentThread().getName(), "Dropping result and requeuing wrapper");
                        mImageQueue.add(0, wrapper);
                    }
                    else if (nextWrapperCode.equals(result_timecode)) {
                        Log.e(Thread.currentThread().getName(), "Dropping image and requeuing result");
                        mCaptureResultQueue.add(0, result);
                    }
                    else {
                        Log.e(Thread.currentThread().getName(), "Dropping results to catch up");
                        while (mCaptureResultQueue.size() > 0) {
                            nextResult = mCaptureResultQueue.remove(0);
                            nextResultCode = TimeCode.toString(nextResult.get(CaptureResult.SENSOR_TIMESTAMP));
                            if (!wrapper_timecode.equals(nextResultCode)) {
                                Log.e(Thread.currentThread().getName(), "Dropping result: " + nextResultCode);
                            }
                            else {
                                mImageQueue.add(0, wrapper);
                                mCaptureResultQueue.add(0, nextResult);
                                return false;
                            }
                        }
                        Log.e(Thread.currentThread().getName(), "Dropping everything");
                        mCaptureResultQueue.clear();
                        mImageQueue.clear();
                    }

                    return false;
                }
                else {
                    Log.e(Thread.currentThread().getName(), "Time code match: "
                    + result_timecode + " == " + wrapper_timecode);
                }


                if (!GlobalSettings.DEBUG_DISABLE_PROCESSING) {
                    ImageProcessor.process(result, wrapper);
                }
                //Log.e(Thread.currentThread().getName(), "<processQueue()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
                return (resultSize != 1 && imageSize != 1);
            }

            if (purging) {
                Log.e(Thread.currentThread().getName(), "Purging queues");
                mCaptureResultQueue.clear();
                mImageQueue.clear();
            }
            //Log.e(Thread.currentThread().getName(), "Result size: " + Integer.toString(resultSize) + ", Image size: "
             //+ Integer.toString(imageSize));
            //Log.e(Thread.currentThread().getName(), "<processQueue()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
            return false;
        }
    }

    // WriteOutput..................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @return bla
     */
    private static boolean writeOutput() {
        StopWatch stopWatch = new StopWatch();
        //synchronized (ACCESS_LOCK) {
            int queueSize = mOutputQueue.size();

            if (queueSize > 0) {
                Log.e("DATA QUEUE", "writing output");
                OutputWrapper wrapper = mOutputQueue.remove(0);
                if (!GlobalSettings.DEBUG_DISABLE_SAVING) {
                    // add to wrapper, option for ascii text
                    // also, destionation: calibration or subpath
                    //StorageMedia.writeCalibration(wrapper);
                    StorageMedia.writeWorkingDirectory(wrapper, null);
                }
                Log.e(Thread.currentThread().getName(), "<WriteOutput()> time: " + NumToString.number(stopWatch.stop()) + " [ns]");
                Log.e(Thread.currentThread().getName(), "Output size: " + Integer.toString(queueSize));
                return (queueSize != 1);
            }
            return false;
        //}
    }

}