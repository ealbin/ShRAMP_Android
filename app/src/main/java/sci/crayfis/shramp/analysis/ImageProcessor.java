package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.os.Process;
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

import sci.crayfis.shramp.ScriptC_Analysis;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.TimeManager;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public class ImageProcessor {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = Process.THREAD_PRIORITY_MORE_FAVORABLE;

    // QUEUE_ACCESS_LOCK...........................................................................
    // Lock to synchronize access to processImage() methods
    private static final Object QUEUE_ACCESS_LOCK = new Object();

    // RENDER_SCRIPT_FLAGS..........................................................................
    // TODO: description, and consider trying CREATE_FLAG_{LOW_LATENCY, LOW_POWER}
    private static final Integer RENDER_SCRIPT_FLAGS = RenderScript.CREATE_FLAG_NONE;

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
    private static final List<byte[]> mImageDataQueue = new ArrayList<>();

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

    // mInstance....................................................................................
    // TODO: description
    private static ImageProcessor mInstance;

    // mIsFirstImage................................................................................
    // Set to false after first image is processed -- used in processImage(ByteBuffer)
    private static Boolean mIsFirstImage = true;

    // mRS..........................................................................................
    // TODO: description
    private static RenderScript mRS;

    // mDoubleType..................................................................................
    // TODO: description
    private static Type mDoubleType;

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBitsPerPixel................................................................................
    // TODO: description
    private Integer mBitsPerPixel;

    // mImageFormat.................................................................................
    // TODO: description
    private Integer mImageFormat;

    // mImageHeight.................................................................................
    // TODO: description
    private Integer mImageHeight;

    // mImageType...................................................................................
    // TODO: description
    private Type mImageType;

    // mImageWidth..................................................................................
    // TODO: description
    private Integer mImageWidth;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Inner Static Classes
    //---------------------

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
        private Double nExposureSum;

        // nExposure3Sum............................................................................
        // Total pixel exposure time^3 in seconds^3
        private Double nExposure3Sum;

        // nExposureValueAlloc......................................................................
        // Pixel-wise sum of exposure time times pixel value
        private Allocation nExposureValueAlloc;

        // nExposure2ValueAlloc.....................................................................
        // Pixel-wise sum of exposure time^2 times pixel value
        private Allocation nExposure2ValueAlloc;

        // nExposureValue2Alloc.....................................................................
        // Pixel-wise sum of exposure time times pixel value^2
        private Allocation nExposureValue2Alloc;

        // nImageAlloc..............................................................................
        // Current image pixel data
        private Allocation nImageAlloc;

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

            // Image Allocation
            nImageAlloc = Allocation.createTyped(mRS, mInstance.mImageType, Allocation.USAGE_SCRIPT);

            // Summation Allocations
            nExposureValueAlloc  = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nExposure2ValueAlloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nExposureValue2Alloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);

            mAS.set_gExposureValue(nExposureValueAlloc);
            mAS.set_gExposure2Value(nExposure2ValueAlloc);
            mAS.set_gExposureValue2(nExposureValue2Alloc);

            // Statistics Allocations
            nMeanAlloc   = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nStdDevAlloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
            nStdErrAlloc = Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);

            nNframes = 0L;
            nExposureSum = 0.;
            nExposure3Sum = 0.;
        }

        //******************************************************************************************
        // Class Methods
        //--------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // analyse..................................................................................
        /**
         * TODO: description, comments and logging
         * @param resultHelper
         */
        private void analyse(@NonNull ResultHelper resultHelper) {
            nNframes      += 1;
            nExposureSum  += resultHelper.nExposureNanos;
            nExposure3Sum += resultHelper.getExposure3();

            mAS.set_gExposureTime(resultHelper.nExposureNanos);
            mAS.forEach_update(nImageAlloc);
        }

        // updateImage..............................................................................
        /**
         * TODO: description, comments and logging
         * @param data
         */
        private void updateImage(@NonNull byte[] data) {
            nImageAlloc.copyFrom(data);
        }

        // updateImage..............................................................................
        /**
         * TODO: description, comments and logging
         * @param data
         */
        private void updateImage(@NonNull short[] data) {
            nImageAlloc.copyFrom(data);
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
         * @param result
         */
        private ResultHelper(@NonNull TotalCaptureResult result) {
            nFrameNumber = result.getFrameNumber();

            nTimestampNanos = result.get(CaptureResult.SENSOR_TIMESTAMP);
            assert nTimestampNanos != null;
            nTimestampNanos = mTimeManager.getElapsedNanos(nTimestampNanos);

            nExposureNanos  = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            nSkewNanos      = result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW);
        }

        //******************************************************************************************
        // Class Methods
        //--------------

        // Private
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // getExposure3.............................................................................
        /**
         * TODO: description, comments and logging
         * @return
         */
        private long getExposure3() {
            return nExposureNanos * nExposureNanos * nExposureNanos;
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

    // ImageProcessor...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private ImageProcessor() {}

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageProcessor...............................................................................
    /**
     * TODO: description, comments and logging
     * @param activity
     */
    public ImageProcessor(@NonNull Activity activity) {
        this();

        long startTime = SystemClock.elapsedRealtimeNanos();
        mLogger.log("Building image processor");

        mInstance = this;

        mImageFormat  = CameraController.getOutputFormat();
        mBitsPerPixel = CameraController.getBitsPerPixel();
        assert mImageFormat  != null;
        assert mBitsPerPixel != null;

        Size outputSize = CameraController.getOutputSize();
        assert outputSize != null;
        mImageHeight = outputSize.getHeight();
        mImageWidth  = outputSize.getWidth();

        mRS = RenderScript.create(activity, RenderScript.ContextType.NORMAL, RENDER_SCRIPT_FLAGS);
        mAS = new ScriptC_Analysis(mRS);

        // Define allocation elements
        Element imageElement = null;
        if (mBitsPerPixel <= 8) {
            // 8-bit YUV
            imageElement = Element.U8(mRS);
        }
        else if (mBitsPerPixel <= 16) {
            // 12-bit YUV
            // 16-bit RAW
            imageElement = Element.U16(mRS);
        }
        else {
            // TODO: image format not supported
        }
        Element doubleElement = Element.F64(mRS);

        // Define allocation types
        mImageType  = new Type.Builder(mRS,  imageElement).setX(mImageWidth).setY(mImageHeight).create();
        mDoubleType = new Type.Builder(mRS, doubleElement).setX(mImageWidth).setY(mImageHeight).create();

        // Set up Allocations
        mAnalysisHelper = new AnalysisHelper();

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // post.........................................................................................
    /**
     * TODO: description, commments and logging
     * @param runnable
     */
    public static void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    // processImage.................................................................................
    /**
     * TODO: description, comments and logging
     * @param result
     */
    public static void processImage(TotalCaptureResult result) {
        synchronized (QUEUE_ACCESS_LOCK) {
            Log.e(Thread.currentThread().getName(), "Queuing TotalCaptureResult");
            mCaptureResultQueue.add(result);
            mHandler.post(mProcessImage);
        }
    }

    // processImage.................................................................................
    /**
     * TODO: description, comments and logging
     * @param imageBytes
     */
    public static void processImage(byte[] imageBytes) {
        synchronized (QUEUE_ACCESS_LOCK) {
            Log.e(Thread.currentThread().getName(), "Queuing byte[]");
            mImageDataQueue.add(imageBytes);
            mHandler.post(mProcessImage);
        }
    }

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // processImage.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void processImage() {

        long startTime = SystemClock.elapsedRealtimeNanos();

        int resultQueueSize;
        int imageQueueSize;
        TotalCaptureResult result;
        byte[] imageBytes;
        synchronized (QUEUE_ACCESS_LOCK) {
            resultQueueSize = mCaptureResultQueue.size();
            imageQueueSize  = mImageDataQueue.size();

            if (resultQueueSize == 0 || imageQueueSize == 0) {
                // wait for objects to queue
                return;
            }

            result     = mCaptureResultQueue.remove(0);
            imageBytes =     mImageDataQueue.remove(0);
        }

        int backlog = Math.max(resultQueueSize, imageQueueSize) - 1;
        int nPixels = mInstance.mImageWidth * mInstance.mImageHeight;

        // 8-bit YUV
        if (mInstance.mBitsPerPixel <= 8) {
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
            ByteBuffer  byteBuffer  = ByteBuffer.wrap(imageBytes);
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
        }
        else {
            // TODO: image format not supported
        }

        // Perform image analysis
        mAnalysisHelper.analyse(new ResultHelper(result));

        String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        Log.e(Thread.currentThread().getName(), "finished processImage(); elapsed = "
                + elapsed + " [ns], backlog = " + Integer.toString(backlog));
    }

    //==============================================================================================
    protected void doStatistics() {
        /*
        mAS.set_gNframes((double) mNframes);
        mAS.set_gExposureSum(mExposureSum);
        mAS.set_gExposureSum3(mExposure3Sum);

        mAS.forEach_getMean(mMeanAlloc);
        double[] meanArray = new double[mImageWidth * mImageHeight];
        mMeanAlloc.copyTo(meanArray);

        mAS.set_gMean(mMeanAlloc);
        mAS.forEach_getStdDev(mStdDevAlloc);
        double[] stdDevArray = new double[mImageWidth * mImageHeight];
        mStdDevAlloc.copyTo(stdDevArray);

        mAS.set_gStdDev(mStdDevAlloc);
        mAS.forEach_getStdErr(mStdErrAlloc);
        double[] stdErrArray = new double[mImageWidth * mImageHeight];
        mStdErrAlloc.copyTo(stdErrArray);

        DecimalFormat df = new DecimalFormat("#.##");
        String report = " \n";
        for (int i = 1; i < 1001; i++) {
            report += df.format(meanArray[i]);
            report += " +/- ";
            report += df.format(stdErrArray[i]);
            report += "\t";
            if (i % 10 == 0) {
                report += "\n";
            }
        }
        Log.e("Dump: ", report);
        */
    }
}

//String filename = CaptureOverseer.mDataPath
//        + "/" + String.format("%03d", frameNumber)
//        + "-" + Long.toString(msSinceEpoch)
//        + "-" + Long.toString(msExposure)
//        + "-" + Long.toString(msFrame) + ".data";
