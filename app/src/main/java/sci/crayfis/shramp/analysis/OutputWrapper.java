package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * TODO: description, comments and logging
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
     * TODO: description, comments and logging
     * @param filename bla
     * @param statistics bla
     * @param Nframes bla
     */
    OutputWrapper(@NonNull String filename, @NonNull Allocation statistics, long Nframes) {
        mFilename = filename;
        mByteBuffer = ByteBuffer.allocate(mStatisticsBytes);
        mByteBuffer.putInt(mRows);
        mByteBuffer.putInt(mColumns);
        mByteBuffer.putLong(Nframes);
        synchronized (CACHE_LOCK) {
            statistics.copyTo(mFloatCache);
            mByteBuffer.asFloatBuffer().put(mFloatCache);
        }
    }

    // OutputWrapper................................................................................
    /**
     * TODO: description, comments and logging
     * @param filename bla
     * @param wrapper bla
     * @param exposure bla
     */
    OutputWrapper(@NonNull String filename, @NonNull ImageWrapper wrapper, long exposure) {
        mFilename = filename;
        mByteBuffer = ByteBuffer.allocate(mSensorBytes);
        mByteBuffer.put(mBitsPerPixel);
        mByteBuffer.putInt(mRows);
        mByteBuffer.putInt(mColumns);
        mByteBuffer.putLong(exposure);
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

        mSensorHeader = "Byte order (big endian): \t Bits-per-pixel \t Number of Rows \t Number of Columns \t Sensor Exposure [ns] \t Pixel data\n";
        mSensorHeader = "Number of bytes: \t " + ByteSize + " \t " + IntSize + " \t " + IntSize + " \t " + LongSize + " \t "
                + Byte.toString(mBitsPerPixel) + "x" + Integer.toString(Npixels) + "\n";

        mStatisticsHeader = "Byte order (big endian): \t Number of Rows \t Number of Columns \t Number of Stacked Images \t Statistics\n";
        mStatisticsHeader = "Number of bytes: \t " + IntSize + " \t " + IntSize + " \t " + LongSize + " \t "
                + FloatSize + "x" + Integer.toString(Npixels) + "\n";
    }

    public String getFilename() {
        return mFilename;
    }

    public ByteBuffer getByteBuffer() {
        return mByteBuffer;
    }

}