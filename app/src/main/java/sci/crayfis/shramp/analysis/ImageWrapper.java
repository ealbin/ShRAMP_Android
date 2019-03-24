package sci.crayfis.shramp.analysis;

import android.media.Image;
import android.media.ImageReader;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import sci.crayfis.shramp.camera2.util.TimeCode;

public final class ImageWrapper {

    private static int mNpixels = 0;
    private static boolean mIs8bitData = false;
    private static boolean mIs16bitData = false;

    private long mTimestamp;
    private byte[] mData_8bit;
    private short[] mData_16bit;

    static void setIs8bitData() {
        mIs8bitData  = true;
        mIs16bitData = false;
    }
    static void setIs16bitData() {
        mIs8bitData  = false;
        mIs16bitData = true;
    }
    static void setNpixels(int nPixels) {
        mNpixels = nPixels;
    }

    public ImageWrapper(ImageReader reader) {

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

    public long getTimestamp() { return mTimestamp; }
    public String getTimeCode() { return TimeCode.toString(mTimestamp); }
    public static long getNpixels() { return mNpixels; }
    public static boolean is8bitData() { return mIs8bitData; }
    public static boolean is16bitData() { return mIs16bitData; }
    public byte[] get8bitData() {return mData_8bit;}
    public short[] get16bitData() {return mData_16bit;}

}
