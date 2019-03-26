package recycle_bin;

import android.app.Activity;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;
import android.util.Size;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;

import sci.crayfis.shramp.ScriptC_Analysis;

abstract class ImageProcessor {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Set to false after first image is processed -- used in processImage()
    private boolean mIsFirstImage = true;

    // Exposure for current image frame in seconds
    protected double mExposureTime;

    protected int mFormat;
    protected int mBitsPerPixel;
    protected int mWidth;
    protected int mHeight;

    private RenderScript mRS;
    private ScriptC_Analysis mSumsScript;

    private Type mImageType;
    private Type mSumType;

    // Current image pixel data
    private Allocation mImageAlloc;

    //..............................................................................................

    // Total number of images that have been captured
    private long mNframes;

    // Total pixel exposure time in seconds
    private double mExposureSum;

    // Total pixel exposure time^3 in seconds^3
    private double mExposure3Sum;

    // Pixel-wise sum of exposure time times pixel value
    private Allocation mExposureValueAlloc;

    // Pixel-wise sum of exposure time^2 times pixel value
    private Allocation mExposure2ValueAlloc;

    // Pixel-wise sum of exposure time times pixel value^2
    private Allocation mExposureValue2Alloc;

    //..............................................................................................

    // Pixel-wise mean value / seconds exposure
    private Allocation mMeanAlloc;

    // Pixel-wise standard deviation / seconds exposure
    private Allocation mStdDevAlloc;

    // Pixel-wise standard error / seconds exposure
    private Allocation mStdErrAlloc;


    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Do nothing constructor
     */
    ImageProcessor() {}

    protected void openSurface(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {
        mFormat       = imageFormat;
        mBitsPerPixel = bitsPerPixel;
        mWidth        = imageSize.getWidth();
        mHeight       = imageSize.getHeight();

        // when this works, consider trying CREATE_FLAG_LOW_LATENCY and LOW_POWER
        mRS = RenderScript.create(activity,
                RenderScript.ContextType.NORMAL,
                RenderScript.CREATE_FLAG_NONE);

        mSumsScript  = new ScriptC_Analysis(mRS);

        setUpAllocations();
    }

    private void setUpAllocations() {

        // Image Allocation
        //..........................................................................................
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

        mImageType = new Type.Builder(mRS, imageElement).setX(mWidth).setY(mHeight).create();

        mImageAlloc = Allocation.createTyped(mRS, mImageType, Allocation.USAGE_SCRIPT);

        // Exposure-Value-Sum Allocations
        //..........................................................................................
        Element sumElement = Element.F64(mRS);
        mSumType = new Type.Builder(mRS, sumElement).setX(mWidth).setY(mHeight).create();

        mExposureValueAlloc  = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
        mExposure2ValueAlloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
        mExposureValue2Alloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);

        //mSumsScript.set_gExposureValue(mExposureValueAlloc);
        //mSumsScript.set_gExposure2Value(mExposure2ValueAlloc);
        //mSumsScript.set_gExposureValue2(mExposureValue2Alloc);

        // Statistics Allocations
        //..........................................................................................
        mMeanAlloc   = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
        mStdDevAlloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);
        mStdErrAlloc = Allocation.createTyped(mRS, mSumType, Allocation.USAGE_SCRIPT);

        //mSumsScript.set_gMean(mMeanAlloc);
        //mSumsScript.set_gStdDev(mStdDevAlloc);
        //mSumsScript.set_gStdErr(mStdErrAlloc);
    }

    protected void doStatistics() {

        //mSumsScript.set_gNframes((double) mNframes);
        //mSumsScript.set_gExposureSum(mExposureSum);
        //mSumsScript.set_gExposureSum3(mExposure3Sum);

        mSumsScript.forEach_getMean(mMeanAlloc);
        double[] meanArray = new double[mWidth * mHeight];
        mMeanAlloc.copyTo(meanArray);

        mSumsScript.set_gMean(mMeanAlloc);
        mSumsScript.forEach_getStdDev(mStdDevAlloc);
        double[] stdDevArray = new double[mWidth * mHeight];
        mStdDevAlloc.copyTo(stdDevArray);

        mSumsScript.set_gStdDev(mStdDevAlloc);
        mSumsScript.forEach_getStdErr(mStdErrAlloc);
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
    }

    protected void processImage(Image image) {

        // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();

        if (mBitsPerPixel <= 8) {
            // 8-bit YUV

            // Do a quick sanity check
            if (mIsFirstImage) {
                int nPixels = mWidth * mHeight;
                if (nPixels != buffer.capacity()) {
                    // TODO: ERROR
                }
                mIsFirstImage = false;
            }

            byte[] data = new byte[buffer.capacity()];
            buffer.get(data);

            mImageAlloc.copyFrom(data);
        }
        else if (mBitsPerPixel <= 16) {
            // 12-bit YUV
            // 16-bit RAW
            ShortBuffer shortBuffer = buffer.asShortBuffer();

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

            mImageAlloc.copyFrom(data);
        }
        else {
            // TODO: image format not supported
        }

        mNframes      += 1;
        mExposureSum  += mExposureTime;
        mExposure3Sum += mExposureTime * mExposureTime * mExposureTime;
        //mSumsScript.set_gExposureTime(mExposureTime);
        //mSumsScript.forEach_update(mImageAlloc);
    }
}



//long time = SystemClock.elapsedRealtimeNanos();
//String fps = df.format(1e9 / (double)(time - elapsedTime));
//elapsedTime = time;
//Log.e(Thread.currentThread().getName(),"reading image, realtime fps: " + fps);
