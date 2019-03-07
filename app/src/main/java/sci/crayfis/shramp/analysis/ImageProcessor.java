package sci.crayfis.shramp.analysis;

import android.app.Activity;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.os.Process;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.ScriptC_Analysis;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.TimeManager;

public class ImageProcessor {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Lock to synchronize access to processImage() methods
    private final Object mAccessLock = new Object();

    // Set to false after first image is processed -- used in processImage(ByteBuffer)
    private boolean mIsFirstImage = true;

    //..............................................................................................

    // Image information
    private int mFormat;
    private int mBitsPerPixel;
    private int mWidth;
    private int mHeight;

    //..............................................................................................

    // Renderscript
    private RenderScript     mRS;
    private ScriptC_Analysis mAnalysisScript;

    // Allocation types
    private Type mImageType;
    private Type mSumType;

    //..............................................................................................

    // ImageProcessor thread handler
    private Handler mHandler;

    // Set in processImage() methods, nulled (reset) in processImageBytes()
    private CaptureResultRunnable mCaptureResultRunnable;
    private ImageRunnable         mImageRunnable;

    //..............................................................................................

    // Convenient storage wrappers
    private AnalysisHelper mAnalysisHelper;
    private ResultHelper   mResultHelper;


    //**********************************************************************************************
    // Inner Classes
    //--------------

    private class AnalysisHelper {

        // Current image pixel data
        private Allocation nImageAlloc;

        //..........................................................................................

        // Total number of images that have been captured
        private long nNframes;

        // Total pixel exposure time in seconds
        private double nExposureSum;

        // Total pixel exposure time^3 in seconds^3
        private double nExposure3Sum;

        //..........................................................................................

        // Pixel-wise sum of exposure time times pixel value
        private Allocation nExposureValueAlloc;

        // Pixel-wise sum of exposure time^2 times pixel value
        private Allocation nExposure2ValueAlloc;

        // Pixel-wise sum of exposure time times pixel value^2
        private Allocation nExposureValue2Alloc;

        //..........................................................................................

        // Pixel-wise mean value / seconds exposure
        private Allocation nMeanAlloc;

        // Pixel-wise standard deviation / seconds exposure
        private Allocation nStdDevAlloc;

        // Pixel-wise standard error / seconds exposure
        private Allocation nStdErrAlloc;

        ////////////////////////////////////////////////////////////////////////////////////////////

        AnalysisHelper() {
            // Image Allocation
            nImageAlloc = Allocation.createTyped(mRS, mImageType, Allocation.USAGE_SCRIPT);

            //......................................................................................

            // Summation Allocations
            nExposureValueAlloc  = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
            nExposure2ValueAlloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
            nExposureValue2Alloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);

            mAnalysisScript.set_gExposureValue(nExposureValueAlloc);
            mAnalysisScript.set_gExposure2Value(nExposure2ValueAlloc);
            mAnalysisScript.set_gExposureValue2(nExposureValue2Alloc);

            // Statistics Allocations
            //......................................................................................
            nMeanAlloc   = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
            nStdDevAlloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
            nStdErrAlloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
        }

        //------------------------------------------------------------------------------------------

        void updateImage(byte[] data) {
            nImageAlloc.copyFrom(data);
        }

        void updateImage(short[] data) {
            nImageAlloc.copyFrom(data);
        }

        //------------------------------------------------------------------------------------------

        void analyse() {
            nNframes      += 1;
            nExposureSum  += mResultHelper.nExposureNanos;
            nExposure3Sum += mResultHelper.getExposure3();

            mAnalysisScript.set_gExposureTime(mResultHelper.nExposureNanos);
            mAnalysisScript.forEach_update(nImageAlloc);
        }

    }

    private class ResultHelper {

        private TimeManager nTimeManager = TimeManager.getInstance();

        //..........................................................................................

        private Long nFrameNumber;
        private Long nTimestampNanos;
        private Long nExposureNanos;
        private Long nSkewNanos;

        ////////////////////////////////////////////////////////////////////////////////////////////

        private ResultHelper(TotalCaptureResult result) {
            nFrameNumber = result.getFrameNumber();

            nTimestampNanos = result.get(CaptureResult.SENSOR_TIMESTAMP);
            assert nTimestampNanos != null;
            nTimestampNanos = nTimeManager.getElapsedNanos(nTimestampNanos);

            nExposureNanos  = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            nSkewNanos      = result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW);
        }

        private long getExposure3() {
            return nExposureNanos * nExposureNanos * nExposureNanos;
        }
    }

    //..............................................................................................

    private class CaptureResultRunnable implements Runnable {
        private TotalCaptureResult nResult;

        CaptureResultRunnable(TotalCaptureResult result) {
            nResult = result;
        }

        @Override
        public void run() {
            processTotalCaptureResult(nResult);
        }
    }

    private class ImageRunnable implements Runnable {
        private ByteBuffer nImageBytes;

        ImageRunnable(ByteBuffer imageBytes) {
            nImageBytes = imageBytes;
        }

        @Override
        public void run() {
            processImageBytes(nImageBytes);
        }
    }


    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Do nothing constructor
     */
    private ImageProcessor() {}

    public ImageProcessor(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {
        mFormat       = imageFormat;
        mBitsPerPixel = bitsPerPixel;
        mWidth        = imageSize.getWidth();
        mHeight       = imageSize.getHeight();

        mHandler = HandlerManager.newHandler("ImageProcessor", Process.THREAD_PRIORITY_VIDEO);
        mCaptureResultRunnable = null;
        mImageRunnable         = null;

        // when this works, consider trying CREATE_FLAG_LOW_LATENCY and LOW_POWER
        mRS = RenderScript.create(activity,
                RenderScript.ContextType.NORMAL,
                RenderScript.CREATE_FLAG_NONE);
        mAnalysisScript = new ScriptC_Analysis(mRS);

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
        Element sumElement = Element.F64(mRS);

        // Define allocation types
        mImageType = new Type.Builder(mRS, imageElement).setX(mWidth).setY(mHeight).create();
        mSumType   = new Type.Builder(mRS, sumElement).setX(mWidth).setY(mHeight).create();

        // Set up Allocations
        mAnalysisHelper = new AnalysisHelper();
    }

    //----------------------------------------------------------------------------------------------

    // Called from camera thread
    public void processImage(TotalCaptureResult result) {
        synchronized (mAccessLock) {
            mCaptureResultRunnable = new CaptureResultRunnable(result);

            // Continue execution on image processor thread
            mHandler.post(mCaptureResultRunnable);
        }
    }

    // Called from image reader thread
    public void processImage(ByteBuffer imageBytes) {
        synchronized (mAccessLock) {
            mImageRunnable = new ImageRunnable(imageBytes);

            // If this was called before processImage(TotalCaptureResult), return
            if (mCaptureResultRunnable == null) {
                return;
            }
            // Otherwise process image
            mHandler.post(mImageRunnable);
        }
    }

    //----------------------------------------------------------------------------------------------

    private void processTotalCaptureResult(TotalCaptureResult result) {

        mResultHelper = new ResultHelper(result);
        // do stuff

        if (mImageRunnable != null) {
            mHandler.post(mImageRunnable);
        }
    }

    private void processImageBytes(ByteBuffer imageBytes) {

        // 8-bit YUV
        if (mBitsPerPixel <= 8) {
            // Do a quick sanity check
            if (mIsFirstImage) {
                int nPixels = mWidth * mHeight;
                if (nPixels != imageBytes.capacity()) {
                    // TODO: ERROR
                }
                mIsFirstImage = false;
            }

            byte[] data = new byte[imageBytes.capacity()];
            imageBytes.get(data);
            mAnalysisHelper.updateImage(data);
        }
        // 12-bit YUV or 16-bit RAW
        else if (mBitsPerPixel <= 16) {
            ShortBuffer shortBuffer = imageBytes.asShortBuffer();

            // Do a quick sanity check
            if (mIsFirstImage) {
                int nPixels = mWidth * mHeight;
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

        mAnalysisHelper.analyse();

        mCaptureResultRunnable = null;
        mResultHelper          = null;
        mImageRunnable         = null;
    }













    protected void doStatistics() {
        /*
        mAnalysisScript.set_gNframes((double) mNframes);
        mAnalysisScript.set_gExposureSum(mExposureSum);
        mAnalysisScript.set_gExposureSum3(mExposure3Sum);

        mAnalysisScript.forEach_getMean(mMeanAlloc);
        double[] meanArray = new double[mWidth * mHeight];
        mMeanAlloc.copyTo(meanArray);

        mAnalysisScript.set_gMean(mMeanAlloc);
        mAnalysisScript.forEach_getStdDev(mStdDevAlloc);
        double[] stdDevArray = new double[mWidth * mHeight];
        mStdDevAlloc.copyTo(stdDevArray);

        mAnalysisScript.set_gStdDev(mStdDevAlloc);
        mAnalysisScript.forEach_getStdErr(mStdErrAlloc);
        double[] stdErrArray = new double[mWidth * mHeight];
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
