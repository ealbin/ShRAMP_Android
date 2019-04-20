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
 * @updated: 20 April 2019
 */

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
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.NumToString;
import sci.crayfis.shramp.util.StopWatch;
import sci.crayfis.shramp.util.StorageMedia;

/**
 * Intermediate queue between receiving image data and its processing, or writing to disk
 */
@TargetApi(21)
abstract public class DataQueue {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // The queue acts on itself using its own thread to take the load off data receivers
    private static final String THREAD_NAME = "QueueThread";

    // mHandler.....................................................................................
    // Reference to QueueThread Handler
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                        GlobalSettings.DATA_QUEUE_THREAD_PRIORITY);

    // ACCESS_LOCK..................................................................................
    // Force actions on the two image queues to happen sequentially.
    // Needed because isBusy() can be called from any thread.
    private static final Object ACCESS_LOCK = new Object();

    // mCaptureResultQueue..........................................................................
    // Queue for TotalCaptureResults (metadata about the capture)
    private static final List<TotalCaptureResult> mCaptureResultQueue = new ArrayList<>();

    // mImageDataQueue..............................................................................
    // Queue for the actual pixel image data
    private static final List<ImageWrapper> mImageQueue = new ArrayList<>();

    // mOutputQueue.................................................................................
    // Queue for images to be written to disk
    private static final List<OutputWrapper> mOutputQueue = new ArrayList<>();

    // ProcessNextImage.............................................................................
    // Runnable for queue to process itself on its own thread when called from another thread
    private static class ProcessNextImage implements Runnable {

        // When true, continue processing image queues until queues are emptied, or clears them
        // if needed.
        // When false, processes as many elements of the queue as currently possible without
        // explicitly clearing the queues.
        static boolean nPurge = false;

        DataQueue.ProcessNextImage setPurge() {
            nPurge = true;
            return this;
        }

        DataQueue.ProcessNextImage unsetPurge() {
            nPurge = false;
            return this;
        }

        @Override
        public void run() {
            // Runs processImageQueues() until all possible processing has happened (nPurge = false)
            // or forces a clear of the queues after that point (nPurge = true) to purge any
            // unprocessable stragglers.
            while (processImageQueues(nPurge)) {
                synchronized (ACCESS_LOCK) {
                    Log.e(Thread.currentThread().getName(),
                            "Metadata Queue Size: " + NumToString.number(mCaptureResultQueue.size())
                            + ", Image Queue Size: "     + NumToString.number(mImageQueue.size())
                            + ", Processor Backlog: "    + NumToString.number(ImageProcessor.getBacklog()));
                }
            }
        }
    }
    private static final DataQueue.ProcessNextImage ProcessNextImage = new ProcessNextImage();

    // PurgeOutputQueue.............................................................................
    // Runnable for queue to process itself on its own thread when called from another thread
    private static final Runnable PurgeOutputQueue = new Runnable() {
        @Override
        public void run() {
            // Runs writeOutput() until the output queue is empty
            while (writeOutput()) {
                Log.e(Thread.currentThread().getName(),
                        "Output Queue Size: " + NumToString.number(mOutputQueue.size())
                        + ", I/O Backlog: "        + NumToString.number(StorageMedia.getBacklog()));
            }
        }
    };

    // For now, monitor performance (TODO: remove in the future)
    private abstract static class StopWatches {
        final static StopWatch AddTotalCaptureResult = new StopWatch("DataQueue.addTotalCaptureResult()");
        final static StopWatch AddImageWrapper       = new StopWatch("DataQueue.addImageWrapper()");
        final static StopWatch AddOutputWrapper      = new StopWatch("DataQueue.addOutputWrapper()");
        final static StopWatch IsEmpty               = new StopWatch("DataQueue.isEmpty()");
        final static StopWatch ProcessImageQueues    = new StopWatch("DataQueue.processImageQueues() (no problems)");
        final static StopWatch ProcessImageQueues2   = new StopWatch("DataQueue.processImageQueues() (problems)");
        final static StopWatch WriteOutput           = new StopWatch("DataQueue.writeOuput()");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // add..........................................................................................
    /**
     * Add capture metadata to the end of the TotalCaptureResult queue
     * (Called from a CameraCaptureSession.CaptureCallback->onCaptureCompleted() method)
     * Doesn't directly add to queue, but rather queues (posts) the add operation itself onto the
     * QueueThread Handler to return from this method ASAP
     * @param result TotalCaptureResult generated from an image capture
     */
    public static void add(@NonNull TotalCaptureResult result) {
        StopWatches.AddTotalCaptureResult.start();

        Long time = result.get(CaptureResult.SENSOR_TIMESTAMP);
        if (time == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Sensor timestamp cannot be null");
            MasterController.quitSafely();
            return;
        }

        if (GlobalSettings.DEBUG_DISABLE_QUEUE) {
            Log.e(Thread.currentThread().getName(), "[DISABLED] Time code of metadata to queue: " + TimeCode.toString(time));
            return;
        }
        Log.e(Thread.currentThread().getName(), "Time code of metadata to queue: " + TimeCode.toString(time));

        // Runnable action to add metadata to TotalCaptureResult queue using the QueueThread
        class Add implements Runnable {
            // Payload
            private TotalCaptureResult nResult;

            // Constructor
            private Add(TotalCaptureResult result) {
                nResult = result;
            }

            // Action
            @Override
            public void run() {
                synchronized (ACCESS_LOCK) {
                    mCaptureResultQueue.add(nResult);
                }
            }
        }

        // Execute Add action on QueueThread when the opportunity arises
        mHandler.post(new Add(result));

        StopWatches.AddTotalCaptureResult.addTime();
    }

    // add..........................................................................................
    /**
     * Add captured image data to the end of the ImageWrapper queue
     * (Called from an ImageReader.OnImageAvailableListener->onImageAvailable() method)
     * Doesn't directly add to queue, but rather queues (posts) the add operation itself onto the
     * QueueThread Handler to return from this method ASAP
     * @param wrapper ImageWrapper created from an image capture
     */
    public static void add(@NonNull ImageWrapper wrapper) {
        StopWatches.AddImageWrapper.start();

        if (GlobalSettings.DEBUG_DISABLE_QUEUE) {
            Log.e(Thread.currentThread().getName(), "[DISABLED] Time code of image to queue: " + wrapper.getTimeCode());
            return;
        }
        Log.e(Thread.currentThread().getName(), "Time code of image to queue: " + wrapper.getTimeCode());

        // Runnable action to add image data to ImageWrapper queue using the QueueThread
        class Add implements Runnable {
            // Payload
            private ImageWrapper mWrapper;

            // Constructor
            private Add(ImageWrapper wrapper) {
                mWrapper = wrapper;
            }

            // Action
            @Override
            public void run() {
                synchronized (ACCESS_LOCK) {
                    mImageQueue.add(mWrapper);
                }
            }
        }

        // Execute Add action on QueueThread when the opportunity arises
        mHandler.post(new Add(wrapper));

        // 99 times out of 100 the image data comes in after the metadata, therefore the image queues
        // are only now asked to process itself assuming the metadata is already queued.
        // A single process request is made; purging the queues is not needed at this time.
        // Every now and then, a frame of image data can get dropped as the system tries to keep up
        // with everything, therefore in a typical run often there are more metadatas queued up
        // than actual image data, so usually processImage() is not over-called this way.
        mHandler.post(ProcessNextImage.unsetPurge());

        StopWatches.AddImageWrapper.addTime();
    }

    // add..........................................................................................
    /**
     * Add data to the end of the OutputWrapper queue.
     * (Usually called from a method in ImageProcessor, but potentially could come from anywhere)
     * Doesn't directly add to queue, but rather queues (posts) the add operation itself onto the
     * QueueThread Handler to return from this method ASAP
     * @param wrapper OutputWrapper containing data to write to disk
     */
    public static void add(@NonNull OutputWrapper wrapper) {
        StopWatches.AddOutputWrapper.start();

        if (GlobalSettings.DEBUG_DISABLE_QUEUE) {
            Log.e(Thread.currentThread().getName(), "[DISABLED] Filename of data to queue for writing: " + wrapper.getFilename());
            return;
        }
        Log.e(Thread.currentThread().getName(), "Filename of data to queue for writing: " + wrapper.getFilename());

        // Runnable action to add data to OutputWrapper queue using the QueueThread
        class Add implements Runnable {
            // Payload
            private OutputWrapper mWrapper;

            // Constructor
            private Add(OutputWrapper wrapper) {
                mWrapper = wrapper;
            }

            // Action
            @Override
            public void run() {
                mOutputQueue.add(mWrapper);
            }
        }

        // Execute Add action on QueueThread when the opportunity arises
        mHandler.post(new Add(wrapper));

        // Purge output queue on QueueThread when the opportunity arises
        mHandler.post(PurgeOutputQueue);

        StopWatches.AddOutputWrapper.addTime();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // clear........................................................................................
    /**
     * Wipe/reset all queues clean and start fresh -- use only when all hope is lost.
     * Action is performed on calling method's thread.
     */
    public static void clear() {
        synchronized (ACCESS_LOCK) {
            mCaptureResultQueue.clear();
            mImageQueue.clear();
        }
    }

    // isEmpty......................................................................................
    /**
     * Note: called on caller's thread, there could be a delay if queue is in use already
     * @return True if all queues are empty, false if at least one queue is not empty
     */
    public static boolean isEmpty() {
        StopWatches.IsEmpty.start();

        int resultSize;
        int imageSize;
        synchronized (ACCESS_LOCK) {
            resultSize = mCaptureResultQueue.size();
            imageSize = mImageQueue.size();
        }
        int outputSize = mOutputQueue.size();

        StopWatches.IsEmpty.addTime();
        return (resultSize == 0) && (imageSize == 0) && (outputSize == 0);
    }

    // purge........................................................................................
    /**
     * Purges (processes) all queues for any unfinished jobs until their empty using the queue thread
     */
    public static void purge() {
        if (isEmpty()) {
            return;
        }

        mHandler.post(ProcessNextImage.setPurge());
        mHandler.post(PurgeOutputQueue);
    }

    // Private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // processImageQueues...........................................................................
    /**
     * Sends the next image (and metadata) staged in the image queues off to ImageProcessor
     * @param purging True if no new data is expected and clears both queues when at least one queue
     *                has no more elements
     * @return True if after running this method, image queues still have more data staged for
     *         processing, false if queues are now empty
     */
    private static boolean processImageQueues(boolean purging) {
        StopWatches.ProcessImageQueues.start();
        StopWatches.ProcessImageQueues2.start();

        // All actions occur under ACCESS_LOCK
        synchronized (ACCESS_LOCK) {

            int resultSize = mCaptureResultQueue.size();
            int imageSize  = mImageQueue.size();

            // Image queues are not empty
            if (resultSize > 0 && imageSize > 0) {
                TotalCaptureResult result = mCaptureResultQueue.remove(0);
                ImageWrapper wrapper      = mImageQueue.remove(0);
                resultSize -= 1;
                imageSize  -= 1;

                Long result_timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
                if (result_timestamp == null) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Sensor timestamp cannot be null");
                    MasterController.quitSafely();
                    return false;
                }
                String result_timecode = TimeCode.toString(result_timestamp);

                // Everything checks out, process image
                if (result_timestamp == wrapper.getTimestamp()) {
                    Log.e(Thread.currentThread().getName(), "Timestamp match, time-codes: "
                            + result_timecode + " == " + wrapper.getTimeCode());

                    if (!GlobalSettings.DEBUG_DISABLE_PROCESSING) {
                        // ImageProcessor returns rapidly as it builds a processing Runnable that
                        // runs on the ImageProcessorThread instead of directly processing now
                        ImageProcessor.process(result, wrapper);
                    }

                    StopWatches.ProcessImageQueues.addTime();
                    return (resultSize != 0 && imageSize != 0);
                }
                //----------------------------------------------------------------------------------
                // Head-ache .. figure out what's wrong
                else {
                    Log.e(Thread.currentThread().getName(), "Timestamps do not match, time-codes: "
                            + result_timecode + " != " + wrapper.getTimeCode());

                    // Timestamps don't match and at least one queue is now empty
                    //-----------------------------------------------------------
                    if (resultSize == 0 || imageSize == 0) {

                        // No new data coming in, go ahead and clear the queues
                        if (purging) {
                            Log.e(Thread.currentThread().getName(), "Purging image queues");
                            mCaptureResultQueue.clear();
                            mImageQueue.clear();
                        }
                        // New data will be coming in, wait for it
                        else {
                            Log.e(Thread.currentThread().getName(), "Requeing both image and result");
                            mCaptureResultQueue.add(0, result);
                            mImageQueue.add(0, wrapper);
                        }

                        StopWatches.ProcessImageQueues2.addTime();
                        return false;
                    }
                    // Timestamps don't match and neither queue is empty
                    //--------------------------------------------------
                    else {
                        // Look at what's next in the queues
                        TotalCaptureResult nextResult = mCaptureResultQueue.get(0);
                        ImageWrapper nextWrapper      = mImageQueue.get(0);

                        Long nextResult_timestamp = nextResult.get(CaptureResult.SENSOR_TIMESTAMP);
                        if (nextResult_timestamp == null) {
                            // TODO: error
                            Log.e(Thread.currentThread().getName(), "Sensor timestamp cannot be null");
                            MasterController.quitSafely();
                            return false;
                        }
                        String nextResult_timecode  = TimeCode.toString(nextResult_timestamp);

                        // If current ImageWrapper matches next TotalCaptureResult
                        // i.e. an image was dropped by the system
                        // Requeue for next processImageQueues() call
                        if (wrapper.getTimestamp() == nextResult_timestamp) {
                            Log.e(Thread.currentThread().getName(), "An image was dropped that would have had time-code: "
                                    + result_timecode + ", dropping that metadata from queue");
                            mImageQueue.add(0, wrapper);
                            StopWatches.ProcessImageQueues2.addTime();
                            return true;
                        }
                        // If current TotalCaptureResult matches next ImageWrapper
                        // i.e. metadata was dropped (extremely rare)
                        // Requeue for next processImageQueues() call
                        else if (result_timestamp == nextWrapper.getTimestamp()) {
                            Log.e(Thread.currentThread().getName(), "A metadata was dropped that would have had time-code: "
                                    + wrapper.getTimeCode() + ", dropping that image from queue");
                            mCaptureResultQueue.add(0, result);
                            StopWatches.ProcessImageQueues2.addTime();
                            return true;
                        }
                        // ImageWrappers and TotalCaptureResults have fallen out of sync by more than
                        // one capture (e.g. the system dropped two or more consecutive image frames)
                        else {
                            Log.e(Thread.currentThread().getName(), "Multiple consecutive images were dropped, dropping metadata from queue to catch up");
                            mCaptureResultQueue.remove(0);
                            while (mCaptureResultQueue.size() > 0) {
                                nextResult = mCaptureResultQueue.remove(0);

                                nextResult_timestamp = nextResult.get(CaptureResult.SENSOR_TIMESTAMP);
                                if (nextResult_timestamp == null) {
                                    // TODO: error
                                    Log.e(Thread.currentThread().getName(), "Sensor timestamp cannot be null");
                                    MasterController.quitSafely();
                                    return false;
                                }
                                nextResult_timecode = TimeCode.toString(nextResult_timestamp);

                                // Everything checks out at last, requeue for next processImageQueues() call
                                if (wrapper.getTimestamp() == nextResult_timestamp) {
                                    Log.e(Thread.currentThread().getName(), "Timestamp match, time-codes: "
                                            + nextResult_timecode + " == " + wrapper.getTimeCode());
                                    mImageQueue.add(0, wrapper);
                                    mCaptureResultQueue.add(0, nextResult);
                                    StopWatches.ProcessImageQueues2.addTime();
                                    return true;
                                }
                                // Still not caught up
                                else {
                                    Log.e(Thread.currentThread().getName(), "Dropping metadata with time-code: " + nextResult_timecode);
                                }
                            }

                            // This is exceptionally rare, could happen if the system dropped two
                            // consecutive TotalCaptureResults, but pretty much unheard of.
                            // Most likely this is an edge condition, either at the start or end of
                            // a run.
                            Log.e(Thread.currentThread().getName(), "Ran out of metadata to drop, dropping everything from both queues");
                            mCaptureResultQueue.clear();
                            mImageQueue.clear();
                            StopWatches.ProcessImageQueues2.addTime();
                            return false;
                        }
                    }
                }
            }
            // At least one image queue is empty
            else {
                // No new data coming in, go ahead and clear the queues
                if (purging) {
                    Log.e(Thread.currentThread().getName(), "Purging queues");
                    mCaptureResultQueue.clear();
                    mImageQueue.clear();
                }
                StopWatches.ProcessImageQueues2.addTime();
                return false;
            }

        }
    }

    // writeOutput..................................................................................
    /**
     * Writes the next data staged in the OutputWrapper queue
     * @return True if OutputWrapper queue has more data staged for writing, false if queue is empty
     */
    private static boolean writeOutput() {
        StopWatches.WriteOutput.start();

        int queueSize = mOutputQueue.size();

        if (queueSize > 0) {
            OutputWrapper wrapper = mOutputQueue.remove(0);

            if (GlobalSettings.DEBUG_DISABLE_ALL_SAVING) {
                Log.e(Thread.currentThread().getName(), "[DISABLED] Writing output filename: " + wrapper.getFilename());
                return (queueSize != 1);
            }
            Log.e(Thread.currentThread().getName(), "Writing output filename: " + wrapper.getFilename());

            // TODO: specify destination, i.e. calibration or subpath
            StorageMedia.writeWorkingDirectory(wrapper, null);

            Log.e(Thread.currentThread().getName(), "Output queue remaining: " + Integer.toString(queueSize - 1));
            StopWatches.WriteOutput.addTime();
            return (queueSize != 1);
        }

        return false;
    }

}

// TODO: delete this:
                        /*
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
                        */