package sci.crayfis.shramp.surfaces;

import android.app.Activity;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.DataQueue;
import Trash.ImageProcessorOld;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.analysis.ImageWrapper;
import sci.crayfis.shramp.camera2.capture.CaptureManager;
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
            ImageProcessorOld.processImage(nData, nTimestamp);
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

    private final static Object lock = new Object();

    // onImageAvailable.............................................................................
    /**
     * TODO: description, comments and logging
     * @param reader bla
     */
    @Override
    public void onImageAvailable(@NonNull ImageReader reader) {

        synchronized (lock) {

            // TODO: better wait solution
            while (HeapMemory.isMemoryLow()) {
                Log.e("LOW MEMORY", "ImageReaderListener is waiting for memory to clear.........");
                HeapMemory.logAvailableMiB();
                try {
                    lock.wait(2 * CaptureManager.getTargetFrameNanos() / 1000 / 1000);
                } catch (InterruptedException e) {
                }
                System.gc();
                if (Build.VERSION.SDK_INT > 27) {
                    reader.discardFreeBuffers();
                }

                if (!ImageProcessor.isBusy()) {
                    break;
                }
            }

            try {
                DataQueue.add(new ImageWrapper(reader));
            }
            catch (IllegalStateException e) {
                // TODO: error
            }
        }
    }

}