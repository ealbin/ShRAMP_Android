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
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.sql.DataTruncation;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;

/**
 * Encapsulates metadata and statistical, image or mask data that is read in from disk.
 * TODO: option for ascii text?  ..or should that just go to logger?
 * TODO: read in data overwrites OutputWrapper static members, possibly a bug if settings are changed
 */
@TargetApi(21)
public final class InputWrapper extends OutputWrapper {

    // Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mExposure....................................................................................
    // Sensor exposure for image data
    private Long mExposure;

    // mNframes.....................................................................................
    // The number of frames that were involved to produce this statistical data
    private Long mNframes;

    // mTemperature.................................................................................
    // The temperature the data (image or statistical) was taken at [Celsius]
    private Float mTemperature;

    // mStatisticsData..............................................................................
    // The statistics data (if that's what it is)
    private float[] mStatisticsData;

    // mImage8bit...................................................................................
    // The image data (if that's what it is)
    private byte[] mImage8bit;

    // mImage16bit..................................................................................
    // The image data (if that's what it is)
    private short[] mImage16bit;

    // mMaskData....................................................................................
    // The mask data (if that's what it is)
    private byte[] mMaskData;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // InputWrapper................................................................................
    /**
     * Create an input wrapper for image, statistical, or mask data
     * @param filepath Absolute file path for data, data type is inferred from the extension
     */
    InputWrapper(@NonNull String filepath) {
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> Reading " + filepath + " <<\n ");

        File infile = new File(filepath);

        if (!infile.exists() || infile.isDirectory() || !infile.canRead()) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Cannot read file: " + filepath);
            return;
        }

        super.mFilename = infile.getName();
        int length      = (int) infile.length();

        // Check file size is correct
        if (super.mFilename.endsWith(GlobalSettings.IMAGE_FILE)) {
            super.mDatatype = Datatype.IMAGE;
            if (length != OutputWrapper.mSensorBytes) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "File has wrong size, cannot read");
                return;
            }
        }
        else if (super.mFilename.endsWith(GlobalSettings.MASK_FILE)) {
            super.mDatatype = Datatype.MASK;
            if (length != OutputWrapper.mMaskBytes) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "File has wrong size, cannot read");
                return;
            }
        }
        else { // .mean, .stddev, or .signif
            super.mDatatype = Datatype.STATISTICS;
            if (length != OutputWrapper.mStatisticsBytes) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "File has wrong size, cannot read");
                return;
            }
        }

        // Read into ByteBuffer
        int bytesRead;
        InputStream inputStream = null;
        try {
            super.mByteBuffer = ByteBuffer.allocateDirect(length);
            inputStream = new FileInputStream(filepath);
            bytesRead = inputStream.read(super.mByteBuffer.array());
        }
        catch (FileNotFoundException e) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Cannot read file: " + filepath);
            return;
        }
        catch (IOException e) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "IO Exception on file: " + filepath);
            return;
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException e) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), "IO Exception on close, read aborted");
                return;
            }
        }
        if (bytesRead != length) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Reading unsuccessful, cannot continue");
            return;
        }

        // Set buffer to beginning and read data
        //------------------------------------------------------------------------------------------

        super.mByteBuffer.flip();

        OutputWrapper.mBitsPerPixel = super.mByteBuffer.get();

        OutputWrapper.mRows    = super.mByteBuffer.getInt();
        OutputWrapper.mColumns = super.mByteBuffer.getInt();

        if (super.mDatatype == Datatype.IMAGE) {
            mExposure = super.mByteBuffer.getLong();
        }
        else if (super.mDatatype == Datatype.STATISTICS) {
            mNframes = super.mByteBuffer.getLong();
        }

        if (super.mDatatype != Datatype.MASK) {
            mTemperature = super.mByteBuffer.getFloat();
        }
        else {
            mMaskData = new byte[super.mByteBuffer.remaining()];
            super.mByteBuffer.get(mMaskData, super.mByteBuffer.position(), super.mByteBuffer.remaining());
        }

        if (super.mDatatype == Datatype.IMAGE) {
            if (OutputWrapper.mBitsPerPixel == 8) {
                mImage8bit = new byte[super.mByteBuffer.remaining()];
                super.mByteBuffer.get(mImage8bit, super.mByteBuffer.position(), super.mByteBuffer.remaining());
            }
            else { // OutputWrapper.mBitsPerPixel == 16
                ShortBuffer shortBuffer = super.mByteBuffer.asShortBuffer();
                mImage16bit = new short[shortBuffer.remaining()];
                shortBuffer.get(mImage16bit, shortBuffer.position(), shortBuffer.remaining());
            }
        }
        else if (super.mDatatype == Datatype.STATISTICS) {
            FloatBuffer floatBuffer = super.mByteBuffer.asFloatBuffer();
            mStatisticsData = new float[floatBuffer.remaining()];
            floatBuffer.get(mStatisticsData, floatBuffer.position(), floatBuffer.remaining());
        }

        // Free memory
        super.mByteBuffer = null;
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // isStatisticsData.............................................................................
    /**
     * @return True if this is statistical data, false if it isn't
     */
    @Contract(pure = true)
    public boolean isStatisticsData() { return mStatisticsData != null; }

    // is8bitData...................................................................................
    /**
     * @return True if this is 8-bit image data, false if it isn't
     */
    @Contract(pure = true)
    public boolean is8bitData() { return mImage8bit != null; }

    // is16bitData..................................................................................
    /**
     * @return True if this is 16-bit image data, false if it isn't
     */
    @Contract(pure = true)
    public boolean is16bitData() { return mImage16bit != null; }

    // isMaskData...................................................................................
    /**
     * @return True if this is mask data, false if it isn't
     */
    @Contract(pure = true)
    public boolean isMaskData() { return mMaskData != null; }

    // getStatisticsData............................................................................
    /**
     * @return Statistics data (null if this wasn't statistical data)
     */
    @Nullable
    @Contract(pure = true)
    public float[] getStatisticsData() { return mStatisticsData; }

    // get8bitData..................................................................................
    /**
     * @return 8-bit image data (null if this wasn't that)
     */
    @Nullable
    @Contract(pure = true)
    public byte[] get8bitData() { return mImage8bit; }

    // get16bitData.................................................................................
    /**
     * @return 16-bit image data (null if this wasn't that)
     */
    @Nullable
    @Contract(pure = true)
    public short[] get16bitData() { return mImage16bit; }

    // getMaskData..................................................................................
    /**
     * @return Mask data (null if this wasn't that)
     */
    @Nullable
    @Contract(pure = true)
    public byte[] getMaskData() { return mMaskData; }

    // getTemperature...............................................................................
    /**
     * @return Temperature in Celsius (null if not available)
     */
    @Nullable
    @Contract(pure = true)
    public Float getTemperature() { return mTemperature; }

    // getExposure..................................................................................
    /**
     * @return Exposure in nanoseconds (null if not available)
     */
    @Nullable
    @Contract(pure = true)
    public Long getExposure() { return mExposure; }

    // getNframes...................................................................................
    /**
     * @return The number of frames used to make this data (null if not available)
     */
    @Nullable
    @Contract(pure = true)
    public Long getNframes() { return mNframes; }

}