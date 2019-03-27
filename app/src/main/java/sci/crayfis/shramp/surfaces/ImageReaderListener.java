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
import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.analysis.DataQueue;
import sci.crayfis.shramp.analysis.ImageWrapper;
import sci.crayfis.shramp.camera2.capture.CaptureController;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;

/**
 * TODO: description, comments and logging
 */
final class ImageReaderListener implements ImageReader.OnImageAvailableListener {

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "ImageReaderThread";

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                       GlobalSettings.IMAGE_READER_THREAD_PRIORITY);

    // LOCK.........................................................................................
    // TODO: description
    private static final Object LOCK = new Object();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageFormat.................................................................................
    // TODO: description
    private Integer mImageFormat;

    // mImageHeight......................................................................................
    // TODO: description
    private Integer mImageHeight;

    // mImageWidth.......................................................................................
    // TODO: description
    private Integer mImageWidth;

    // mImageReader.................................................................................
    // TODO: description
    private ImageReader mImageReader;

    // mSurface.....................................................................................
    // TODO: description
    private Surface mSurface;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ImageReaderListener..........................................................................
    /**
     * TODO: description, comments and logging
     */
    ImageReaderListener() {
        super();
    }

    // Package-private Instance Methods
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
        mImageFormat = imageFormat;
        mImageWidth  = imageSize.getWidth();
        mImageHeight = imageSize.getHeight();

        mImageReader = ImageReader.newInstance(mImageWidth, mImageHeight, mImageFormat, GlobalSettings.MAX_SIMULTANEOUS_IMAGES);
        mImageReader.setOnImageAvailableListener(this, mHandler);

        SurfaceController.surfaceHasOpened(mImageReader.getSurface(), ImageReaderListener.class);
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onImageAvailable.............................................................................
    /**
     * TODO: description, comments and logging
     * @param reader bla
     */
    @Override
    public void onImageAvailable(@NonNull ImageReader reader) {
        synchronized (LOCK) {

            // TODO: better wait solution
            while (HeapMemory.isMemoryLow()) {

                Log.e("LOW MEMORY", "ImageReaderListener is waiting for memory to clear...");
                HeapMemory.logAvailableMiB();

                try {
                    LOCK.wait(3 * CaptureController.getTargetFrameNanos() / 1000 / 1000);
                } catch (InterruptedException e) {
                }
                System.gc();
                if (Build.VERSION.SDK_INT > 27) {
                    reader.discardFreeBuffers();
                }

                if (!AnalysisController.isBusy()) {
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