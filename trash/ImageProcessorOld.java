package recycle_bin;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_Analysis;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.TimeManager;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public class ImageProcessorOld {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public static final Long LOW_MEMORY = GlobalSettings.LOW_MEMORY_MiB;
    public static final Long SUFFICIENT_MEMORY = GlobalSettings.SUFFICIENT_MEMORY_MiB;
    private static final Integer MAX_BACKLOG = GlobalSettings.MAX_BACKLOG;
    private static final Integer ACCEPTABLE_BACKLOG = GlobalSettings.ACCEPTABLE_BACKLOG;

    private static final Object DO_NOT_DISTURB = new Object();


    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = GlobalSettings.IMAGE_PROCESSOR_THREAD_PRIORITY;

    // QUEUE_ACCESS_LOCK...........................................................................
    // Lock to synchronize access to processImage() methods
    private static final Object QUEUE_ACCESS_LOCK = new Object();

    // RENDER_SCRIPT_FLAGS..........................................................................
    // TODO: description, and consider trying CREATE_FLAG_{LOW_LATENCY, LOW_POWER}
    private static final Integer RENDER_SCRIPT_FLAGS = GlobalSettings.RENDER_SCRIPT_FLAGS;

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "ImageProcessorThread";

    // Private object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mCaptureResultQueue..........................................................................
    // TODO: description
    private static final List<TotalCaptureResult> mCaptureResultQueue = new ArrayList<>();

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME, PRIORITY);

    // mImageDataQueue..............................................................................
    // TODO: description
    private static final List<DataWrapper> mImageDataQueue = new ArrayList<>();

    // mInstance....................................................................................
    // TODO: description
    private static final ImageProcessorOld mInstance = new ImageProcessorOld();

    // mProcessImage................................................................................
    // TODO: description
    private static final Runnable mProcessImage = new Runnable() {
        @Override
        public void run() {
            processImage();
        }
    };

    // mTimeManager.................................................................................
    // TODO: description
    private static final TimeManager mTimeManager = TimeManager.getInstance();

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mAnalysisHelper..............................................................................
    // TODO: description
    private static AnalysisHelper mAnalysisHelper;

    // mAS..........................................................................................
    // TODO: description
    private static ScriptC_Analysis mAS;

    // mIsFirstImage................................................................................
    // Set to false after first image is processed -- used in processImage(ByteBuffer)
    private static Boolean mIsFirstImage = true;

    // mRS..........................................................................................
    // TODO: description
    private static RenderScript mRS;

    // mDoubleType..................................................................................
    // TODO: description
    private static Type mDoubleType;

    // mLongType....................................................................................
    // TODO: description
    private static Type mLongType;

    // mShortType...................................................................................
    // TODO: description
    private static Type mShortType;

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBitsPerPixel................................................................................
    // TODO: description
    private Integer mBitsPerPixel;

    // mImageHeight.................................................................................
    // TODO: description
    private Integer mImageHeight;

    // mImageType...................................................................................
    // TODO: description
    private Type mImageType;

    // mImageWidth..................................................................................
    // TODO: description
    private Integer mImageWidth;

    private static double[] mMeanArray;
    private static double[] mErrArray;
    private static double[] mStdErrArray;

    private static byte[] mByteArray;
    private static short[] mShortArray;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Inner Static Classes
    //---------------------

    private static class DataWrapper {
        byte[] nData;
        long nTimestamp;

        DataWrapper(byte[] data, long timestamp) {
            nData = data;
            nTimestamp = timestamp;
        }
    }

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // AnalysisHelper...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private static class AnalysisHelper {

        //******************************************************************************************
        // Class Fields
        //-------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // nExposureSum.............................................................................
        // Total pixel exposure time in seconds
        private Long nExposureSum;

        // nExposureValueSumAlloc...................................................................
        // Pixel-wise sum of exposure time times pixel value
        private Allocation nExposureValueSumAlloc;

        // nExposureValue2SumAlloc..................................................................
        // Pixel-wise sum of exposure time times pixel value^2
        private Allocation nExposureValue2SumAlloc;

        // nImageAlloc..............................................................................
        // Current image pixel data
        private Allocation nImageAlloc;

        private Allocation nImageAlloc16bit;

        // nMeanAlloc...............................................................................
        // Pixel-wise mean value / seconds exposure
        private Allocation nMeanAlloc;

        // nNframes.................................................................................
        // Total number of images that have been captured
        private Long nNframes;

        // nStdDevAlloc.............................................................................
        // Pixel-wise standard deviation / seconds exposure
        private Allocation nStdDevAlloc;

        // nStdErrAlloc.............................................................................
        // Pixel-wise standard error / seconds exposure
        private Allocation nStdErrAlloc;

        ////////////////////////////////////////////////////////////////////////////////////////////
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
        ////////////////////////////////////////////////////////////////////////////////////////////

        //******************************************************************************************
        // Constructors
        //-------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // AnalysisHelper...........................................................................
        /**
         * TODO: description, comments and logging
         */
        private AnalysisHelper() {
            //Log.e(Thread.currentThread().getName(), "ImageProcessorOld.AnalysisHelper AnalysisHelper");

            // Image Allocation
            nImageAlloc = Allocation.createTyped(mRS, mInstance.mImageType, Allocation.USAGE_SCRIPT);

            nImageAlloc16bit = Allocation.createTyped(mRS, mShortType,Allocation.USAGE_SCRIPT);

            // Summation Allocations
            nExposureValueSumAlloc  = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nExposureValue2SumAlloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);

            //mAS.set_gExposureValueSum(nExposureValueSumAlloc);
            //mAS.set_gExposureValue2Sum(nExposureValue2SumAlloc);

            // Statistics Allocations
            nMeanAlloc   = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nStdDevAlloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nStdErrAlloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);

            //mAS.set_gMean(nMeanAlloc);
            //mAS.set_gStdDev(nStdDevAlloc);

            nNframes      = 0L;
            nExposureSum  = 0L;
        }

        //******************************************************************************************
        // Class Methods
        //--------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // analyze..................................................................................
        /**
         * TODO: description, comments and logging
         */
        private void analyze() {

            //synchronized (DO_NOT_DISTURB)
            {

                Log.e(Thread.currentThread().getName(), "ImageProcessorOld.AnalysisHelper analyze");

                mAS.set_gNframes(nNframes);
                mAS.set_gExposureSum(nExposureSum);

                Log.e(Thread.currentThread().getName(), "Nframes = " + Long.toString(nNframes)
                + ",  Exposure Sum: " + Long.toString(nExposureSum));


                mAS.set_gExposureValueSum(nExposureValueSumAlloc);
                mAS.forEach_getMean(nMeanAlloc);
                mRS.finish();
                nMeanAlloc.syncAll(Allocation.USAGE_SCRIPT);

                mAS.set_gNframes(nNframes);
                mAS.set_gExposureSum(nExposureSum);
                mAS.set_gExposureValueSum(nExposureValueSumAlloc);
                mAS.set_gExposureValue2Sum(nExposureValue2SumAlloc);
                mAS.set_gMean(nMeanAlloc);
                mAS.forEach_getStdDev(nStdDevAlloc);
                mRS.finish();
                nStdDevAlloc.syncAll(Allocation.USAGE_SCRIPT);

                mAS.set_gNframes(nNframes);
                mAS.set_gExposureSum(nExposureSum);
                mAS.set_gExposureValueSum(nExposureValueSumAlloc);
                mAS.set_gExposureValue2Sum(nExposureValue2SumAlloc);
                mAS.set_gMean(nMeanAlloc);
                mAS.set_gStdDev(nStdDevAlloc);
                mAS.forEach_getStdErr(nStdErrAlloc);
                mRS.finish();
                nStdErrAlloc.syncAll(Allocation.USAGE_SCRIPT);

                nMeanAlloc.copyTo(mMeanArray);
                //nStdDevAlloc.copyTo(mErrArray);
                nStdErrAlloc.copyTo(mErrArray);


                DecimalFormat df = new DecimalFormat("0.00E00");
                String report = Thread.currentThread().getName() + " \n";
                for (int i = 1; i < 1001; i++) {
                    report += df.format(mMeanArray[i]);
                    report += " +/- ";
                    report += df.format(mErrArray[i]);
                    report += "\t";
                    if (i % 10 == 0) {
                        report += "\n";
                    }
                }
                Log.e("Dump: ", report);


                //if (GlobalSettings.DEBUG_STATISTICS_CHECK) {
                 //   final int Npixels = mInstance.mImageWidth * mInstance.mImageHeight;

                    /*
                    nMeanAlloc.copyTo(mMeanArray);
                    nStdDevAlloc.copyTo(mErrArray);
                    nStdErrAlloc.copyTo(mStdErrArray);

                    double meanAve = 0.;
                    for (double val : mMeanArray) {
                        meanAve += val;
                    }
                    meanAve = meanAve / (double) Npixels;

                    String crap = "";
                    for (int i = 1; i < 1001; i++) {
                        crap += Double.toString(mMeanArray[i]) + "  ";
                        if (i % 10 == 0) {
                            crap += "\n";
                        }
                    }

                    Log.e(Thread.currentThread().getName(), "Mean Ave: " + Double.toString(meanAve));
                    Log.e(Thread.currentThread().getName(), "Here are some: " + crap);
                    */

                //}


            }
        }

        // clear....................................................................................
        /**
         * TODO: description, comments and logging
         */
        private void clear() {
            //synchronized (DO_NOT_DISTURB)
            {
                Log.e(Thread.currentThread().getName(), "ImageProcessorOld.AnalysisHelper clear");
                //nImageAlloc.destroy();

                //nExposureValueSumAlloc.destroy();
                //nExposureValue2SumAlloc.destroy();

                //nMeanAlloc.destroy();
                //nStdDevAlloc.destroy();
                //nStdErrAlloc.destroy();

                // TODO: Explicit zeroing might not be necessary
                //mAS.set_gNframes(0L);
                //mAS.set_gExposureSum(0L);
                //mAS.set_gExposureTime(0L);
                //mAS.set_gExposureValueSum(null);
                //mAS.set_gExposureValue2Sum(null);
                //mAS.set_gMean(null);
                //mAS.set_gStdDev(null);
            }
        }


        // update...................................................................................
        /**
         * TODO: description, comments and logging
         * @param resultHelper bla
         */
        private void update(@NonNull ResultHelper resultHelper) {
            synchronized (DO_NOT_DISTURB)
            {
                Log.e(Thread.currentThread().getName(), "ImageProcessorOld.AnalysisHelper update");

                nNframes += 1;
                nExposureSum += resultHelper.nExposureNanos;

                if (mInstance.mImageType.getElement().getBytesSize() == 1) {
                    nImageAlloc.copyTo(mByteArray);
                    int length = mShortArray.length;
                    for (int i = 0; i < length; i++) {
                        mShortArray[i] = (short) mByteArray[i];
                    }
                    nImageAlloc16bit.copyFrom(mShortArray);
                }
                else {
                    nImageAlloc16bit = nImageAlloc;
                }

                mAS.set_gExposureTime(resultHelper.nExposureNanos);
                mAS.set_gOldExposureValueSum(nExposureValueSumAlloc);
                mAS.forEach_updateExposureValueSum(nImageAlloc16bit, nExposureValueSumAlloc);
                mRS.finish();

                mAS.set_gExposureTime(resultHelper.nExposureNanos);
                mAS.set_gOldExposureValue2Sum(nExposureValue2SumAlloc);
                mAS.forEach_updateExposureValue2Sum(nImageAlloc16bit, nExposureValue2SumAlloc);
                mRS.finish();

                nExposureValueSumAlloc.syncAll(Allocation.USAGE_SCRIPT);
                nExposureValue2SumAlloc.syncAll(Allocation.USAGE_SCRIPT);

                /*
                if (mInstance.mImageType.getElement().getBytesSize() == 1) {
                    mAS.forEach_update_8bit(nImageAlloc);
                } else {
                    mAS.forEach_update_16bit(nImageAlloc);
                }

                mRS.finish();
                nImageAlloc.syncAll(Allocation.USAGE_SCRIPT);
                nExposureValue2SumAlloc.syncAll(Allocation.USAGE_SCRIPT);
                nExposureValueSumAlloc.syncAll(Allocation.USAGE_SCRIPT);
                */

                //if (GlobalSettings.DEBUG_RENDERSCRIPT_UPDATE) {

                    final int Npixels = mInstance.mImageWidth * mInstance.mImageHeight;

                    nExposureValueSumAlloc.copyTo(mMeanArray);
                    nExposureValue2SumAlloc.copyTo(mErrArray);

                    double evsSum = 0.;
                    double evs2Sum = 0.;

                    for (int i = 0; i < Npixels; i++) {
                        evsSum += mMeanArray[i];
                        evs2Sum += mErrArray[i];
                    }

                    Log.e(Thread.currentThread().getName(), "DEBUG UPDATE: evsSum = " +
                            Double.toString(evsSum) + ",  evs2Sum = " + Double.toString(evs2Sum)
                    + ", exposure time = " + Long.toString(mAS.get_gExposureTime()));

                    Log.e(Thread.currentThread().getName(), "DEBUG UPDATE PT.2: Nframes = " +
                            Long.toString(nNframes) + ", exposure sum = " + Long.toString(nExposureSum));

                    Log.e(Thread.currentThread().getName(), "DEBUG UPDATE PT.3: ratio = " +
                            Double.toString(evs2Sum / evsSum));

                //}

                synchronized (QUEUE_ACCESS_LOCK) {
                    int resultQueueSize = mCaptureResultQueue.size();
                    int imageQueueSize = mImageDataQueue.size();

                    // Check if done
                    if (resultQueueSize == 0 && imageQueueSize == 0) {
                        Log.e(Thread.currentThread().getName(), "ImageProcessorOld queue is empty");
                        mPendingJobs += 1;
                        mHandler.post(mProcessImage);
                    }
                }
            }
        }

        // updateImage..............................................................................
        /**
         * TODO: description, comments and logging
         * @param data bla
         */
        private void updateImage(@NonNull byte[] data) {
            synchronized (DO_NOT_DISTURB) {
                //Log.e(Thread.currentThread().getName(), "ImageProcessorOld.AnalysisHelper updateImage");
                if (nImageAlloc != null) {
                    nImageAlloc.copyFrom(data);
                }
            }
        }

        // updateImage..............................................................................
        /**
         * TODO: description, comments and logging
         * @param data bla
         */
        private void updateImage(@NonNull short[] data) {
            synchronized (DO_NOT_DISTURB) {
                //Log.e(Thread.currentThread().getName(), "ImageProcessorOld.AnalysisHelper updateImage");
                if (nImageAlloc != null) {
                    nImageAlloc.copyFrom(data);
                }
            }
        }

    }

    // ResultHelper.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static class ResultHelper {

        //******************************************************************************************
        // Class Fields
        //-------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // nFrameNumber.............................................................................
        // TODO: description
        private Long nFrameNumber;

        // nTimestampNanos..........................................................................
        // TODO: description
        private Long nTimestampNanos;

        // nExposureNanos...........................................................................
        // TODO: description
        private Long nExposureNanos;

        // nSkewNanos...............................................................................
        // TODO: description
        private Long nSkewNanos;

        ////////////////////////////////////////////////////////////////////////////////////////////
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
        ////////////////////////////////////////////////////////////////////////////////////////////

        //******************************************************************************************
        // Constructors
        //-------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // ResultHelper.............................................................................
        /**
         * DO NOT USE!!!
         */
        private ResultHelper() { assert false; }

        // ResultHelper.............................................................................
        /**
         * TODO: description, comments and logging
         * @param result bla
         */
        private ResultHelper(@NonNull TotalCaptureResult result) {
            //synchronized (DO_NOT_DISTURB)
            {
                //Log.e(Thread.currentThread().getName(), "ImageProcessorOld.ResultHelper ResultHelper");
                nFrameNumber = result.getFrameNumber();

                nTimestampNanos = result.get(CaptureResult.SENSOR_TIMESTAMP);
                assert nTimestampNanos != null;
                nTimestampNanos = mTimeManager.getElapsedNanos(nTimestampNanos);

                nExposureNanos = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                nSkewNanos = result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW);
            }
        }
    }

    //==============================================================================================
    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    //==============================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageProcessorOld...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private ImageProcessorOld() {
        //Log.e(Thread.currentThread().getName(), "ImageProcessorOld ImageProcessorOld");
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // init.........................................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     */
    public static void init(@NonNull Activity activity) {

        //synchronized (DO_NOT_DISTURB)
        {
            long startTime = SystemClock.elapsedRealtimeNanos();
            //Log.e(Thread.currentThread().getName(), "ImageProcessorOld init");

            // I have found bits per pixel to be unreliable..
            // ImageFormat.getBitsPerPixel(YUV_420_888) returns 12-bits per pixel,
            // but android documentation, and more importantly, actual data is 8-bits.. (?) wtf dude
            mInstance.mBitsPerPixel = CameraController.getBitsPerPixel();
            assert mInstance.mBitsPerPixel != null;

            Size outputSize = CameraController.getOutputSize();
            assert outputSize != null;
            mInstance.mImageHeight = outputSize.getHeight();
            mInstance.mImageWidth = outputSize.getWidth();

            mRS = RenderScript.create(activity, RenderScript.ContextType.NORMAL, RENDER_SCRIPT_FLAGS);
            mRS.setPriority(RenderScript.Priority.LOW);
            mAS = new ScriptC_Analysis(mRS);

            Integer outputFormat = CameraController.getOutputFormat();
            assert outputFormat != null;
            boolean isYUV = (outputFormat == ImageFormat.YUV_420_888);
            boolean isRAW = (outputFormat == ImageFormat.RAW_SENSOR);

            // Define allocation elements
            Element imageElement = null;
            if (isYUV) {
                // 8-bit YUV
                imageElement = Element.U8(mRS);
            } else if (isRAW) {
                // 12-bit YUV
                // 16-bit RAW
                imageElement = Element.U16(mRS);
            } else {
                // TODO: image format not supported
            }
            Element doubleElement = Element.F64(mRS);
            Element longElement   = Element.I64(mRS);
            Element shortElement  = Element.U16(mRS);

            // Define allocation types
            int imageWidth = mInstance.mImageWidth;
            int imageHeight = mInstance.mImageHeight;
            Type imageType;
            imageType   = new Type.Builder(mRS, imageElement).setX(imageWidth).setY(imageHeight).create();
            mShortType  = new Type.Builder(mRS, shortElement).setX(imageWidth).setY(imageHeight).create();
            mDoubleType = new Type.Builder(mRS, doubleElement).setX(imageWidth).setY(imageHeight).create();
            mLongType   = new Type.Builder(mRS, longElement).setX(imageWidth).setY(imageHeight).create();
            mInstance.mImageType = imageType;

            // Set up Allocations
            mAnalysisHelper = new AnalysisHelper();

            final int Npixels = mInstance.mImageWidth * mInstance.mImageHeight;
            mMeanArray   = new double[Npixels];
            mErrArray = new double[Npixels];
            //mStdErrArray = new double[Npixels];

            mByteArray = new byte[Npixels];
            mShortArray = new short[Npixels];

            String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
            //mLogger.log("return; elapsed = " + elapsed + " [ns]");
        }
    }

    private static Integer mPendingJobs = 0;
    // post.........................................................................................
    /**
     * TODO: description, commments and logging
     * @param runnable bla
     */
    public synchronized static void post(Runnable runnable) {
        mPendingJobs += 1;
        Log.e(Thread.currentThread().getName(), "ImageProcessorOld post, pending jobs: " + Integer.toString(mPendingJobs));
        mHandler.post(runnable);
    }

    // processImage.................................................................................
    /**
     * TODO: description, comments and logging
     * @param result bla
     */
    public static void processImage(TotalCaptureResult result) {
        synchronized (QUEUE_ACCESS_LOCK) {
            Log.e(Thread.currentThread().getName(), "ImageProcessorOld Queuing TotalCaptureResult, time code: " + TimeCode.toString(result.get(CaptureResult.SENSOR_TIMESTAMP)));
            mCaptureResultQueue.add(result);
            //mHandler.post(mProcessImage);
            mPendingJobs -= 1;
            Log.e(Thread.currentThread().getName(), "pending jobs: " + Integer.toString(mPendingJobs));
        }
    }

    // processImage.................................................................................
    /**
     * TODO: description, comments and logging
     * @param imageBytes bla
     */
    public static void processImage(byte[] imageBytes, long timestamp) {
        synchronized (QUEUE_ACCESS_LOCK) {
            Log.e(Thread.currentThread().getName(), "ImageProcessorOld Queuing byte[], time code: " + TimeCode.toString(timestamp));
            mImageDataQueue.add(new DataWrapper(imageBytes, timestamp));
            mPendingJobs -= 1;
            Log.e(Thread.currentThread().getName(), "pending jobs: " + Integer.toString(mPendingJobs));
            mPendingJobs += 1;
            mHandler.post(mProcessImage);
        }
    }

    // processStatistics............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void processStatistics() {
        if (mPendingJobs != 0) {
            Log.e(Thread.currentThread().getName(), "Was going to do stats, but not all jobs are in, job count: " + Integer.toString(mPendingJobs));
            Runnable stats = new Runnable() {
                @Override
                public void run() {
                    processStatistics();
                }
            };
            mPendingJobs += 1;
            mHandler.post(stats);
            return;
        }

        //if (GlobalSettings.DEBUG_DISABLE_STATISTICS) {
            Log.e(Thread.currentThread().getName(), "ImageProcessorOld would do statistics at this point");
        //}
        //else {
            Log.e(Thread.currentThread().getName(), "ImageProcessorOld processStatistics");
            mAnalysisHelper.analyze();
            reset();
        //}
    }

    // reset........................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void reset() {
        Log.e(Thread.currentThread().getName(), "ImageProcessorOld reset");
        synchronized (QUEUE_ACCESS_LOCK) {
            mCaptureResultQueue.clear();
            mImageDataQueue.clear();
        }
        if (mAnalysisHelper != null) {
            mAnalysisHelper.clear();
            mAnalysisHelper = new AnalysisHelper();
        }
    }

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static final Object MOOP = new Object();

    // processImage.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void processImage() {

        //synchronized (DO_NOT_DISTURB)
        {

            mPendingJobs -= 1;
            long startTime = SystemClock.elapsedRealtimeNanos();

            int resultQueueSize;
            int imageQueueSize;
            TotalCaptureResult result;
            DataWrapper imageData;
            int backlog;
            long freeMiB = HeapMemory.getAvailableMiB();
            synchronized (QUEUE_ACCESS_LOCK) {
                resultQueueSize = mCaptureResultQueue.size();
                imageQueueSize = mImageDataQueue.size();

                backlog = Math.max(resultQueueSize, imageQueueSize) - 1;
                Log.e(Thread.currentThread().getName(), "ImageProcessorOld Pause Capture = " + Boolean.toString(mPauseCapture));

                // sync with capture thread
                //mPauseCapture = CaptureManager.requestIsPaused();

                if (!mPauseCapture) {

                    if (resultQueueSize == 0 && imageQueueSize == 0) {
                        if (mPendingJobs <= 0) { // TODO: fix negative pending jobs
                            Log.e(Thread.currentThread().getName(), "ImageProcessorOld queue is empty, signaling CaptureOverseer");
                            Runnable ready = new Runnable() {
                                @Override
                                public void run() {
                                    CaptureOverseer.imageProcessorReady();
                                }
                            };
                            CaptureOverseer.post(ready);
                        }
                        else {
                            Log.e(Thread.currentThread().getName(), "Would signal CaptureOverseer, but jobs still left: " + Integer.toString(mPendingJobs));
                        }
                        return;
                    }

                    if (resultQueueSize == 0 || imageQueueSize == 0) {
                        Log.e(Thread.currentThread().getName(), "ImageProcessorOld processImage (waiting) " +
                                "( " + Integer.toString(resultQueueSize) + ", " + Integer.toString(imageQueueSize)
                                + " ) -> (result queue, image queue)");
                        try {
                            QUEUE_ACCESS_LOCK.wait(10);
                        }
                        catch (InterruptedException e) {
                        }
                        mPendingJobs += 1;
                        mHandler.post(mProcessImage);
                        return;
                    }

                    // resultQueueSize and imageQueueSize are greater than 0 by above if statements

                    if (freeMiB < LOW_MEMORY || backlog > MAX_BACKLOG) {
                        //mPauseCapture = CaptureManager.pauseRepeatingRequest();
                    }

                }
                else {

                    if (resultQueueSize == 0 || imageQueueSize == 0 || backlog == 1) {
                        Log.e(Thread.currentThread().getName(), "ImageProcessorOld restart capture>>>>>>>>>>>>");
                        mPauseCapture = false;
                        //CaptureManager.restartRepeatingRequest();
                        mPendingJobs += 1;
                        mHandler.post(mProcessImage);
                        return;
                    }

                    if ( freeMiB > SUFFICIENT_MEMORY && backlog < ACCEPTABLE_BACKLOG) {
                        Log.e(Thread.currentThread().getName(), "ImageProcessorOld restart capture>>>>>>>>>>>>");
                        mPauseCapture = false;
                        //CaptureManager.restartRepeatingRequest();
                    }
                }

                result = mCaptureResultQueue.remove(0);
                imageData = mImageDataQueue.remove(0);
            }

            Long resultTime = result.get(CaptureResult.SENSOR_TIMESTAMP);
            assert resultTime != null;
            Long imageTime = imageData.nTimestamp;

            Log.e(Thread.currentThread().getName(), "ImageProcessorOld processImage, time code: (result) " +
                    TimeCode.toString(resultTime) + " == (image) " + TimeCode.toString(imageTime));

            if (!resultTime.equals(imageTime)) {
                Log.e(Thread.currentThread().getName(), "TIMESTAMP MISMATCH!!!!!!!!!!!!!!!!!!!!!!!!!");
                // TODO: error
            }


            Log.e(Thread.currentThread().getName(), "ImageProcessorOld Free Heap Memory: " + Long.toString(freeMiB) + " [MiB]");

            byte[] imageBytes = imageData.nData;

            int nPixels = mInstance.mImageWidth * mInstance.mImageHeight;

            // 8-bit YUV
            if (mInstance.mBitsPerPixel <= 15) {
                // Do a quick sanity check
                if (mIsFirstImage) {
                    if (nPixels != imageBytes.length) {
                        // TODO: ERROR
                    }
                    mIsFirstImage = false;
                }
                mAnalysisHelper.updateImage(imageBytes);
            }
            // 12-bit YUV or 16-bit RAW
            else if (mInstance.mBitsPerPixel <= 16) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(imageBytes);
                ShortBuffer shortBuffer = byteBuffer.asShortBuffer();

                // Do a quick sanity check
                if (mIsFirstImage) {
                    if (nPixels != shortBuffer.capacity()) {
                        // TODO: ERROR
                    }
                    mIsFirstImage = false;
                }
                short[] data = new short[shortBuffer.capacity()];
                shortBuffer.get(data);
                mAnalysisHelper.updateImage(data);
            } else {
                // TODO: image format not supported
            }

            if (GlobalSettings.DEBUG_DISABLE_PROCESSING) {
                synchronized (QUEUE_ACCESS_LOCK) {
                    resultQueueSize = mCaptureResultQueue.size();
                    imageQueueSize = mImageDataQueue.size();

                    // Check if done
                    if (resultQueueSize == 0 && imageQueueSize == 0) {
                        Log.e(Thread.currentThread().getName(), "ImageProcessorOld queue is empty");
                        try {
                            QUEUE_ACCESS_LOCK.wait(10);
                        }
                        catch (InterruptedException e) {
                        }
                        mPendingJobs += 1;
                        mHandler.post(mProcessImage);
                    }
                }
            }
            else {
                // Perform image analysis
                mAnalysisHelper.update(new ResultHelper(result));

                Log.e(Thread.currentThread().getName(), "Result Queue Size: " + Integer.toString(resultQueueSize - 1)
                        + "    Image Queue Size: " + Integer.toString(imageQueueSize - 1));
            }

            System.gc();

            String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
            Log.e(Thread.currentThread().getName(), "finished processImage() on time code: " + TimeCode.toString(imageData.nTimestamp)
                    + "; elapsed = "
                    + elapsed + " [ns], backlog = " + Integer.toString(backlog));
        }
    }

    private static boolean mPauseCapture;
}

//String filename = CaptureOverseer.mDataPath
//        + "/" + String.format("%03d", frameNumber)
//        + "-" + Long.toString(msSinceEpoch)
//        + "-" + Long.toString(msExposure)
//        + "-" + Long.toString(msFrame) + ".data";
