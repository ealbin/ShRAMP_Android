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
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;

import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.util.StopWatch;

/**
 * Encapsulate image data received by an ImageReader.onImageAvailable() method,
 * e.g. in ImageReaderListener
 */
@TargetApi(21)
public final class ImageWrapper {

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageMetadata................................................................................
    // Image format properties common to all images being produced
    private static abstract class ImageMetadata {
        static int nPixels = 0;
        static int nRows   = 0;
        static int nCols   = 0;

        static boolean is8bitData  = false;
        static boolean is16bitData = false;

        static void is8bitFormat() {
            is8bitData  = true;
            is16bitData = false;
        }

        static void is16bitFormat() {
            is8bitData  = false;
            is16bitData = true;
        }

        static void setRowsCols(int rows, int cols) {
            nRows = rows;
            nCols = cols;
            nPixels = rows * cols;
        }
    }

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageData....................................................................................
    // Sensor timestamp of the image and its data
    private class ImageData {
        long    Timestamp;
        byte[]  Data_8bit;
        short[] Data_16bit;

        // Set the data and timestamp from an Image
        void setData(Image image) {
            Timestamp = image.getTimestamp();

            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            int capacity          = byteBuffer.capacity();

            if (ImageMetadata.is8bitData && ImageMetadata.nPixels == capacity) {
                Data_8bit = new byte[capacity];
                byteBuffer.get(Data_8bit);
                Data_16bit = null;
            }
            else if (ImageMetadata.is16bitData && ImageMetadata.nPixels == capacity / 2){
                Data_16bit = new short[capacity / 2];
                byteBuffer.asShortBuffer().get(Data_16bit);
                Data_8bit = null;
            }
            else {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "Image data cannot be unknown format");
                MasterController.quitSafely();
            }
        }
    }
    private final ImageData mImageData = new ImageData();

    // For now, monitor performance (TODO: remove in the future)
    private final static StopWatch mStopWatch = new StopWatch("new ImageWrapper()");

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageWrapper.................................................................................
    /**
     * Disabled
     */
    private ImageWrapper() {}

    // ImageWrapper.................................................................................
    /**
     * Wrap Image data to this object, and purge it from the ImageReader buffer
     * @param reader ImageReader buffer of images
     */
    public ImageWrapper(@NonNull ImageReader reader) {
        mStopWatch.start();

        Image image = null;
        try {
            image = reader.acquireNextImage();
            if (image == null) {
                return;
            }
            mImageData.setData(image);
            image.close();
        }
        catch (IllegalStateException e) {
            if (image != null) {
                image.close();
            }
            // TODO: error
            Log.e(Thread.currentThread().getName(), "ImageReader Illegal State Exception");
            MasterController.quitSafely();
        }

        mStopWatch.addTime();
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setAs8bitData................................................................................
    /**
     * Notify ImageWrapper that the images received will have 8-bit pixel depth (e.g. YUV_420_888)
     */
    static void setAs8bitData() { ImageMetadata.is8bitFormat();}

    // setAs16bitData...............................................................................
    /**
     * Notify ImageWrapper that the images received will have 16-bit pixel depth (
     */
    static void setAs16bitData() { ImageMetadata.is16bitFormat(); }

    // setRowsCols..................................................................................
    /**
     * Notify ImageWrapper that the images received will have "rows", "cols" and n_pixels = rows * cols
     * @param rows Number of pixel rows in an image
     * @param cols Number of pixel columns in an image
     */
    static void setRowsCols(int rows, int cols) { ImageMetadata.setRowsCols(rows, cols); }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // get8bitData..................................................................................
    /**
     * @return 8 bit data (if that's what the image is, null if it's 16 bit)
     */
    @Nullable
    @Contract(pure = true)
    byte[] get8bitData() {return mImageData.Data_8bit;}

    // get16bitData.................................................................................
    /**
     * @return 16 bit data (if that's what the image is, null if it's 8 bit)
     */
    @Nullable
    @Contract(pure = true)
    short[] get16bitData() {return mImageData.Data_16bit;}

    // getTimestamp.................................................................................
    /**
     * @return Sensor timestamp for the image
     */
    @Contract(pure = true)
    long getTimestamp() { return mImageData.Timestamp; }

    // getTimeCode..................................................................................
    /**
     * @return A short human-friendly character representation of the timestamp
     */
    @Contract(pure = true)
    @NonNull
    String getTimeCode() { return TimeCode.toString(mImageData.Timestamp); }

    // getNpixels...................................................................................
    /**
     * @return The number of pixels in an image
     */
    @Contract(pure = true)
    static int getNpixels() { return ImageMetadata.nPixels; }

    // getNrows.....................................................................................
    /**
     * @return The number of rows in an image
     */
    @Contract(pure = true)
    static int getNrows() { return ImageMetadata.nRows; }

    // getNcols.....................................................................................
    /**
     * @return The number of columns in an image
     */
    @Contract(pure = true)
    static int getNcols() { return ImageMetadata.nCols; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // is8bitData...................................................................................
    /**
     * @return True if image data is 8-bit depth, false if not
     */
    @Contract(pure = true)
    static boolean is8bitData() { return ImageMetadata.is8bitData; }

    // is16bitData..................................................................................
    /**
     * @return True if image data is 16-bit depth, false if not
     */
    @Contract(pure = true)
    static boolean is16bitData() { return ImageMetadata.is16bitData; }

}