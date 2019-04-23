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
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;

/**
 * TODO: description, comments and logging
 * TODO: add to wrapper, option for ascii text
 */
@TargetApi(21)
public final class OutputWrapper {

    // Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TODO: description
    private static final String ByteSize   = Integer.toString(Byte.SIZE    / 8);
    private static final String ShortSize  = Integer.toString(Short.SIZE   / 8);
    private static final String IntSize    = Integer.toString(Integer.SIZE / 8);
    private static final String LongSize   = Integer.toString(Integer.SIZE / 8);
    private static final String FloatSize  = Integer.toString(Float.SIZE   / 8);
    private static final String DoubleSize = Integer.toString(Double.SIZE  / 8);

    // CACHE_LOCK...................................................................................
    // TODO: description
    private static final Object CACHE_LOCK = new Object();

    // Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBitsPerPixel................................................................................
    // TODO: description
    private static byte mBitsPerPixel;

    // mRows........................................................................................
    // TODO: description
    private static int mRows;

    // mColumns.....................................................................................
    // TODO: description
    private static int mColumns;

    // mSensorBytes.................................................................................
    // TODO: description
    private static int mSensorBytes;

    // mStatisticsBytes.............................................................................
    // TODO: description
    private static int mStatisticsBytes;

    // mSensorHeader................................................................................
    // TODO: description
    private static String mSensorHeader;

    // mStatisticsHeader............................................................................
    // TODO: description
    private static String mStatisticsHeader;

    // mFloatCache..................................................................................
    // TODO: description
    private static float[] mFloatCache;

    // Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mFilename....................................................................................
    // TODO: description
    private String mFilename;

    // mByteBuffer..................................................................................
    // TODO: description
    private ByteBuffer mByteBuffer;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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
        mByteBuffer.putInt(mRows);
        mByteBuffer.putInt(mColumns);
        mByteBuffer.putLong(Nframes);
        mByteBuffer.putFloat(temperature);
        synchronized (CACHE_LOCK) {
            statistics.copyTo(mFloatCache);
            mByteBuffer.asFloatBuffer().put(mFloatCache);
        }
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
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // configure....................................................................................
    /**
     * TODO: description, comments and logging
     */
    static void configure() {
        mSensorBytes     = 0;
        mStatisticsBytes = 0;

        int Npixels = ImageWrapper.getNpixels();
        mFloatCache = new float[Npixels];

        if (ImageWrapper.is8bitData()) {
            mBitsPerPixel = 8;
            mSensorBytes += Byte.SIZE / 8;
            mSensorBytes += Npixels * Byte.SIZE / 8;
        }
        else if (ImageWrapper.is16bitData()) {
            mBitsPerPixel = 16;
            mSensorBytes += Short.SIZE / 8;
            mSensorBytes += Npixels * Short.SIZE / 8;
        }
        else {
            // TODO: error
            return;
        }
        mStatisticsBytes += Npixels * Float.SIZE / 8;

        mRows = ImageWrapper.getNrows();
        mSensorBytes     += Integer.SIZE / 8;
        mStatisticsBytes += Integer.SIZE / 8;

        mColumns = ImageWrapper.getNcols();
        mSensorBytes     += Integer.SIZE / 8;
        mStatisticsBytes += Integer.SIZE / 8;

        // Sensor exposure
        mSensorBytes += Long.SIZE / 8;

        // Frames count
        mStatisticsBytes += Long.SIZE / 8;

        // Temperature
        mSensorBytes     += Float.SIZE / 8;
        mStatisticsBytes += Float.SIZE / 8;

        mSensorHeader = "Byte order (big endian): \t Bits-per-pixel \t Number of Rows \t Number of Columns \t Sensor Exposure [ns] \t Temperature [C] \t Pixel data\n";
        mSensorHeader = "Number of bytes: \t " + ByteSize + " \t " + IntSize + " \t " + IntSize + " \t " + LongSize + " \t " + FloatSize + "\t"
                + Byte.toString(mBitsPerPixel) + "x" + Integer.toString(Npixels) + "\n";

        mStatisticsHeader = "Byte order (big endian): \t Number of Rows \t Number of Columns \t Number of Stacked Images \t Temperature [C] \t PostProcessing\n";
        mStatisticsHeader = "Number of bytes: \t " + IntSize + " \t " + IntSize + " \t " + LongSize + " \t " + FloatSize + "\t"
                + FloatSize + "x" + Integer.toString(Npixels) + "\n";
    }

    public String getFilename() {
        return mFilename;
    }

    public ByteBuffer getByteBuffer() {
        return mByteBuffer;
    }

}