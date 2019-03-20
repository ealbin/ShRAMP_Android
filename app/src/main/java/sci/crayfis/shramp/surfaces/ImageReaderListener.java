package sci.crayfis.shramp.surfaces;

import android.app.Activity;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.util.TimeCode;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;

/**
 * TODO: description, comments and logging
 */
final class ImageReaderListener implements ImageReader.OnImageAvailableListener {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // MAX_IMAGES...................................................................................
    // TODO: description
    private static final Integer MAX_IMAGES = GlobalSettings.MAX_IMAGES;

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = GlobalSettings.IMAGE_READER_THREAD_PRIORITY;

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "ImageReaderThread";

    // Private object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME, PRIORITY);

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageFormat.................................................................................
    // TODO: description
    private Integer mImageFormat;

    // mImageHeight......................................................................................
    // TODO: description
    private Integer mImageHeight;

    // mImageReader.................................................................................
    // TODO: description
    private ImageReader mImageReader;

    // mImageWidth.......................................................................................
    // TODO: description
    private Integer mImageWidth;

    // mSurface.....................................................................................
    // TODO: description
    private Surface mSurface;

    /**
     * TODO: description, comments and logging
     */
    class QueueData implements Runnable {
        private byte[] nData;
        private long nTimestamp;

        private QueueData() { assert false; }

        QueueData(byte[] data, long timestamp) {
            nData = data;
            nTimestamp = timestamp;
        }

        @Override
        public void run() {
            ImageProcessor.processImage(nData, nTimestamp);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageReaderListener..........................................................................
    /**
     * TODO: description, comments and logging
     */
    ImageReaderListener() {
        super();
        //Log.e(Thread.currentThread().getName(), "ImageReaderListener ImageReaderListener");
    }

    //**********************************************************************************************
    // Class Methods
    //---------------------

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // openSurface..................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param imageFormat bla
     * @param imageSize bla
     */
    void openSurface(@NonNull Activity activity,
                     @NonNull Integer imageFormat, @NonNull Size imageSize) {
        //Log.e(Thread.currentThread().getName(), "ImageReaderListener openSurface");

        mImageFormat = imageFormat;
        mImageWidth  = imageSize.getWidth();
        mImageHeight = imageSize.getHeight();

        mImageReader = ImageReader.newInstance(mImageWidth, mImageHeight, mImageFormat, MAX_IMAGES);
        mImageReader.setOnImageAvailableListener(this, mHandler);

        SurfaceManager.surfaceHasOpened(mImageReader.getSurface(), ImageReaderListener.class);
    }

    //**********************************************************************************************
    // Overriding Methods
    //-------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    Object lock = new Object();

    // onImageAvailable.............................................................................
    /**
     * TODO: description, comments and logging
     * @param reader bla
     */
    @Override
    public void onImageAvailable(@NonNull ImageReader reader) {

        synchronized (lock) {
            while (HeapMemory.getAvailableMiB() < ImageProcessor.LOW_MEMORY) {
                Log.e("LOW MEMORY", "ImageReaderListener is waiting for memory to clear.........");
                try {
                    lock.wait(10);
                } catch (InterruptedException e) {
                }
                System.gc();
            }


            //Log.e(Thread.currentThread().getName(), "ImageReaderListener onImageAvailable");
            Image image = null;
            try {
                image = reader.acquireNextImage();

                long timestamp = image.getTimestamp();
                Log.e(Thread.currentThread().getName(), "ImageReaderListener has recieved " + TimeCode.toString(timestamp));

                // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
                ByteBuffer imageBytes = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[imageBytes.capacity()];
                imageBytes.get(data);
                image.close();

                if (!GlobalSettings.DEBUG_NO_DATA_POSTING) {
                    ImageProcessor.post(new QueueData(data, timestamp));
                }
            } catch (IllegalStateException e) {
                // TODO: error
                if (image != null) {
                    image.close();
                }
            }
        }
    }

}