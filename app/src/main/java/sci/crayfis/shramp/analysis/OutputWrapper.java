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
 * @updated: 29 April 2019
 */

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;

import sci.crayfis.shramp.MasterController;

/**
 * Encapsulates statistical, image data or mask data and packages it, along with metadata, into a
 * ByteBuffer ready to write to disk.
 * TODO: option for ascii text?  ..or should that just go to logger?
 */
@TargetApi(21)
public class OutputWrapper {

    // Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // What this OutputWrapper can contain
    public enum Datatype { IMAGE, STATISTICS, MASK }

    // String shortcuts
    private static final String ByteSize   = Integer.toString(Byte.SIZE    / 8);
    private static final String ShortSize  = Integer.toString(Short.SIZE   / 8);
    private static final String IntSize    = Integer.toString(Integer.SIZE / 8);
    private static final String LongSize   = Integer.toString(Integer.SIZE / 8);
    private static final String FloatSize  = Integer.toString(Float.SIZE   / 8);
    private static final String DoubleSize = Integer.toString(Double.SIZE  / 8);

    // CACHE_LOCK...................................................................................
    // Prevent two OutputWrappers from simultaneously using the cache (float[] array)
    private static final Object CACHE_LOCK = new Object();

    // Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBitsPerPixel................................................................................
    // Bits per pixel for image data
    protected static byte mBitsPerPixel;

    // mRows........................................................................................
    // Number of rows of pixel sensor
    protected static int mRows;

    // mColumns.....................................................................................
    // Number of columns of pixel sensor
    protected static int mColumns;

    // mSensorBytes.................................................................................
    // Total number of bytes for image data
    protected static int mSensorBytes;

    // mStatisticsBytes.............................................................................
    // Total number of bytes for statistical data
    protected static int mStatisticsBytes;

    // mMaskBytes...................................................................................
    // Total number of bytes for mask data
    protected static int mMaskBytes;

    // mSensorHeader................................................................................
    // Description of byte-ordering for image data
    private static String mSensorHeader;

    // mStatisticsHeader............................................................................
    // Description of byte-ordering for statistical data
    private static String mStatisticsHeader;

    // mMaskHeader..................................................................................
    // Description of byte-ordering for mask data
    private static String mMaskHeader;

    // mFloatCache..................................................................................
    // Used in an intermediate step in converting a statistical RenderScript Allocation into bytes,
    // rather than create/destroy a new array every time since it's around 30-50 MB
    private static float[] mFloatCache;

    // Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mFilename....................................................................................
    // Intended filename for writing data
    protected String mFilename;

    // mDatatype....................................................................................
    // Denotes if this OutputWrapper is for image data or statistics
    protected Datatype mDatatype;

    // mByteBuffer..................................................................................
    // Packaged bytes ready to write
    protected ByteBuffer mByteBuffer;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // OutputWrapper................................................................................
    /**
     * Default constructor for object inheritance, does nothing
     */
    protected OutputWrapper() {}

    // OutputWrapper................................................................................
    /**
     * Create an output wrapper for float-type RenderScript Allocation data, e.g. statistics
     * @param filename Filename for data (no path, just filename)
     * @param statistics Float-type RenderScript Allocation data, e.g. mean, stddev, significance, etc
     * @param Nframes The number of frames that went into making this data,
     *                e.g. significance would be 1, mean and stddev would be 1000 for example
     * @param temperature The approximate temperature when the data was taken in Celsius
     */
    OutputWrapper(@NonNull String filename, @NonNull Allocation statistics, long Nframes, float temperature) {
        mFilename = filename;
        mByteBuffer = ByteBuffer.allocate(mStatisticsBytes);
        mByteBuffer.put(mBitsPerPixel);
        mByteBuffer.putInt(mRows);
        mByteBuffer.putInt(mColumns);
        mByteBuffer.putLong(Nframes);
        mByteBuffer.putFloat(temperature);
        synchronized (CACHE_LOCK) {
            statistics.copyTo(mFloatCache);
            mByteBuffer.asFloatBuffer().put(mFloatCache);
        }
        mDatatype = Datatype.STATISTICS;
    }

    // OutputWrapper................................................................................
    /**
     * Create an output wrapper for 8 or 16-bit image data
     * @param filename Filename for data (no path, just filename)
     * @param wrapper ImageWrapper containing image data
     * @param exposure (Optional) Sensor exposure in nanoseconds if available, if null defaults to 0
     * @param temperature Temperature data was taken at in Celsius
     */
    OutputWrapper(@NonNull String filename, @NonNull ImageWrapper wrapper, @Nullable Long exposure, float temperature) {
        mFilename = filename;
        mByteBuffer = ByteBuffer.allocate(mSensorBytes);
        mByteBuffer.put(mBitsPerPixel);
        mByteBuffer.putInt(mRows);
        mByteBuffer.putInt(mColumns);
        if (exposure == null) {
            exposure = 0L;
        }
        mByteBuffer.putLong(exposure);
        mByteBuffer.putFloat(temperature);
        if (ImageWrapper.is8bitData()) {
            mByteBuffer.put(wrapper.get8bitData());
        }
        else {
            mByteBuffer.asShortBuffer().put(wrapper.get16bitData());
        }
        mDatatype = Datatype.IMAGE;
    }

    // OutputWrapper................................................................................
    /**
     * Create an output wrapper for cut mask data
     * @param filename Filename for data (no path, just filename)
     * @param mask Pixel mask data
     */
    OutputWrapper(@NonNull String filename, byte[] mask) {
        mFilename = filename;
        mByteBuffer = ByteBuffer.allocate(mMaskBytes);
        mByteBuffer.put(mBitsPerPixel);
        mByteBuffer.putInt(mRows);
        mByteBuffer.putInt(mColumns);
        mByteBuffer.put(mask);
        mDatatype = Datatype.MASK;
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // configure....................................................................................
    /**
     * Sets up cache and initializes all important fields
     * TODO: gets information from ImageWrapper, consider subclassing this? .. or making it its own?
     */
    static void configure() {
        mSensorBytes     = 0;
        mStatisticsBytes = 0;
        mMaskBytes       = 0;

        int Npixels = ImageWrapper.getNpixels();
        mFloatCache = new float[Npixels];

        // Image data bytes
        if (ImageWrapper.is8bitData()) {
            mBitsPerPixel = 8;
            mSensorBytes += Npixels * Byte.SIZE / 8;
        }
        else if (ImageWrapper.is16bitData()) {
            mBitsPerPixel = 16;
            mSensorBytes += Npixels * Short.SIZE / 8;
        }
        else {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Unknown image format");
            MasterController.quitSafely();
            return;
        }
        mStatisticsBytes += Npixels * Float.SIZE / 8;
        mMaskBytes       += Npixels * Byte.SIZE / 8;

        // Bits per pixel
        mSensorBytes     += 1;
        mStatisticsBytes += 1;
        mMaskBytes       += 1;

        mRows = ImageWrapper.getNrows();
        mSensorBytes     += Integer.SIZE / 8;
        mStatisticsBytes += Integer.SIZE / 8;
        mMaskBytes       += Integer.SIZE / 8;

        mColumns = ImageWrapper.getNcols();
        mSensorBytes     += Integer.SIZE / 8;
        mStatisticsBytes += Integer.SIZE / 8;
        mMaskBytes       += Integer.SIZE / 8;

        // Sensor exposure
        mSensorBytes += Long.SIZE / 8;

        // Frames count
        mStatisticsBytes += Long.SIZE / 8;

        // Temperature
        mSensorBytes     += Float.SIZE / 8;
        mStatisticsBytes += Float.SIZE / 8;

        mSensorHeader  = "Byte order (big endian): \t Bits-per-pixel \t Number of Rows \t Number of Columns \t Sensor Exposure [ns] \t Temperature [C] \t Pixel data\n";
        mSensorHeader += "Number of bytes: \t " + ByteSize + " \t " + IntSize + " \t " + IntSize + " \t " + LongSize + " \t " + FloatSize + "\t"
                + Byte.toString(mBitsPerPixel) + "x" + Integer.toString(Npixels) + "\n";

        mStatisticsHeader  = "Byte order (big endian): \t Bits-per-pixel \t Number of Rows \t Number of Columns \t Number of Stacked Images \t Temperature [C] \t PostProcessing\n";
        mStatisticsHeader += "Number of bytes: \t " + ByteSize + "\t" + IntSize + " \t " + IntSize + " \t " + LongSize + " \t " + FloatSize + "\t"
                + FloatSize + "x" + Integer.toString(Npixels) + "\n";

        mMaskHeader  = "Byte order (big endian): \t Bits-per-pixel \t Number of Rows \t Number of Columns \t Mask data\n";
        mMaskHeader += "Number of bytes: \t " + ByteSize + "\t" + IntSize + " \t " + IntSize + " \t " + ByteSize + "x" + Integer.toString(Npixels) + "\n";
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * @return Get what kind of data is being held, image data or statistical
     */
    @NonNull
    @Contract(pure = true)
    public Datatype getType() { return mDatatype; }

    /**
     * @return A String describing the byte-order of image data
     */
    @NonNull
    @Contract(pure = true)
    public String getSensorHeader() { return mSensorHeader; }

    /**
     * @return A String describing the byte-order of statistical data
     */
    @NonNull
    @Contract(pure = true)
    public String getStatisticsHeader() { return mStatisticsHeader; }

    /**
     * @return The filename for writing this data
     */
    @NonNull
    @Contract(pure = true)
    public String getFilename() { return mFilename; }

    /**
     * @return The ByteBuffer containing this data and metadata as described in the header
     */
    @Nullable
    @Contract(pure = true)
    public ByteBuffer getByteBuffer() { return mByteBuffer; }

}