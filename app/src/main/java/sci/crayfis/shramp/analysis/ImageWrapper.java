/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import sci.crayfis.shramp.camera2.util.TimeCode;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public final class ImageWrapper {

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mNpixels.....................................................................................
    // TODO: description
    private static long mNpixels = 0;

    // mTimestamp...................................................................................
    // TODO: description
    private long mTimestamp;

    //..............................................................................................
    // TODO: description
    private static boolean mIs8bitData  = false;
    private static boolean mIs16bitData = false;

    //..............................................................................................
    // TODO: description
    private byte[]  mData_8bit;
    private short[] mData_16bit;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageWrapper.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private ImageWrapper() {}

    // ImageWrapper.................................................................................
    /**
     * TODO: description, comments and logging
     * @param reader bla
     */
    public ImageWrapper(@NonNull ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireNextImage();
            mTimestamp = image.getTimestamp();

            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            long capacity         = byteBuffer.capacity();

            if (mIs8bitData && mNpixels == capacity) {
                mData_8bit = new byte[byteBuffer.capacity()];
                byteBuffer.get(mData_8bit);
            }
            else if (mIs16bitData && mNpixels == capacity / 2){
                ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                mData_16bit = new short[shortBuffer.capacity()];
                shortBuffer.get(mData_16bit);
            }
            else {
                // TODO: error
            }

            image.close();
        }
        catch (IllegalStateException e) {
            // TODO: error
            if (image != null) {
                image.close();
            }
            throw new IllegalStateException();
        }
    }

    // Package-private Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setAs8bitData................................................................................
    /**
     * TODO: description, comments and logging
     */
    static void setAs8bitData() {
        mIs8bitData  = true;
        mIs16bitData = false;
    }

    // setAs16bitData...............................................................................
    /**
     * TODO: description, comments and logging
     */
    static void setAs16bitData() {
        mIs8bitData  = false;
        mIs16bitData = true;
    }

    // setNpixels...................................................................................
    /**
     * TODO: description, comments and logging
     * @param nPixels
     */
    static void setNpixels(long nPixels) {
        mNpixels = nPixels;
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // get8bitData..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public byte[] get8bitData() {return mData_8bit;}

    // get16bitData.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public short[] get16bitData() {return mData_16bit;}

    // getTimestamp.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public long getTimestamp() { return mTimestamp; }

    // getTimeCode..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    @NonNull
    public String getTimeCode() { return TimeCode.toString(mTimestamp); }

    // getNpixels...................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public static long getNpixels() { return mNpixels; }

    // is8bitData...................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public static boolean is8bitData() { return mIs8bitData; }

    // is16bitData..................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Contract(pure = true)
    public static boolean is16bitData() { return mIs16bitData; }

}