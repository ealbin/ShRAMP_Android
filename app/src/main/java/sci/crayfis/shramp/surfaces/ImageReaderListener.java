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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.surfaces;

import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.analysis.ImageWrapper;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;
import sci.crayfis.shramp.util.StopWatch;

/**
 * An ImageReader is useful for receiving camera image data.
 * The purpose of this class is to handle its creation and reception of image data.
 */
public final class ImageReaderListener implements ImageReader.OnImageAvailableListener {

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // To maximize performance, the camera image data is received on its own thread
    private static final String THREAD_NAME = "ImageReaderThread";

    // mHandler.....................................................................................
    // Handler to the ImageReaderThread
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                       GlobalSettings.IMAGE_READER_THREAD_PRIORITY);

    // LOCK.........................................................................................
    // Synchronous lock to prevent the camera system thread from calling onImageAvailable() twice
    // (or more) in a row while ImageReaderThread is still processing the first call and from
    // getting the order of images messed up.. TODO: this might not be strictly necessary.
    private static final Object LOCK = new Object();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageFormat.................................................................................
    // The output image format: ImageFormat.RAW or ImageFormat.YUV_420_888
    private Integer mImageFormat;

    // mImageHeight......................................................................................
    // Image height in pixels
    private Integer mImageHeight;

    // mImageWidth.......................................................................................
    // Image width in pixels
    private Integer mImageWidth;

    // mImageReader.................................................................................
    // Reference to the ImageReader object that controls the surface
    private ImageReader mImageReader;

    // mSurface.....................................................................................
    // The corresponding surface to the ImageReader object
    private Surface mSurface;

    // mStopWatch1..................................................................................
    // For now, monitoring performance -- (TODO) to be removed later
    private static final StopWatch mStopWatch1 = new StopWatch();

    // mStopWatch2..................................................................................
    // For now, monitoring performance -- (TODO) to be removed later
    private static final StopWatch mStopWatch2 = new StopWatch();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageReaderListener..........................................................................
    /**
     * Nothing special, just make it
     */
    ImageReaderListener() {
        super();
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // openSurface..................................................................................
    /**
     * Build/open a new ImageReader surface to receive camera image data
     *
     * @param imageFormat ImageFormat.RAW or ImageFormat.YUV_420_888
     * @param imageSize Image size width and height in pixels
     */
    void openSurface(@NonNull Integer imageFormat, @NonNull Size imageSize) {
        mImageFormat = imageFormat;
        mImageWidth  = imageSize.getWidth();
        mImageHeight = imageSize.getHeight();

        mImageReader = ImageReader.newInstance(mImageWidth, mImageHeight, mImageFormat,
                                                            GlobalSettings.MAX_SIMULTANEOUS_IMAGES);
        mImageReader.setOnImageAvailableListener(this, mHandler);

        SurfaceController.surfaceHasOpened(mImageReader.getSurface(), ImageReaderListener.class);
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onImageAvailable.............................................................................
    /**
     * Called by the system every time a new image is ready from the camera
     * @param reader ImageReader buffer that holds the backlog of images
     */
    @Override
    public void onImageAvailable(@NonNull ImageReader reader) {
        mStopWatch1.start();

        // TODO: Lock probably not necessary
        // onImageAvailable() runs on its own thread, so multiple calls from the system should
        // automatically queue..  Haven't tested yet
        synchronized (LOCK) {

            // Wait until there is enough memory to queue up an image for processing
            while (HeapMemory.isMemoryLow()) {

                Log.e(Thread.currentThread().getName(), ">> LOW MEMORY << ImageReaderListener is waiting for memory to clear >> LOW MEMORY <<");
                HeapMemory.logAvailableMiB();

                try {
                    LOCK.wait(GlobalSettings.DEFAULT_WAIT_MS);
                }
                catch (InterruptedException e) {
                    // TODO: error?
                }

                // Try to free memory
                System.gc();
                if (Build.VERSION.SDK_INT > 27) {
                    reader.discardFreeBuffers();
                }

                // If images are not being processed, go ahead and queue this image up.
                // Sometimes the garbage collector just needs a kick.
                if (!AnalysisController.isBusy()) {
                    break;
                }
            }

            mStopWatch2.start();
            DataQueue.add(new ImageWrapper(reader));
            mStopWatch2.addTime();
        }

        mStopWatch1.addTime();
    }

    /**
     * TODO: remove
     */
    public static void logPerformance() {
        Log.e(Thread.currentThread().getName(), "onImageAvailable method: " + mStopWatch1.getPerformance());
        Log.e(Thread.currentThread().getName(), "DataQueue.add(new ImageWrapper()): " + mStopWatch2.getPerformance());
    }

}