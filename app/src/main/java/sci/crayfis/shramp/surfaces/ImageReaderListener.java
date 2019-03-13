package sci.crayfis.shramp.surfaces;

import android.app.Activity;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.util.HandlerManager;

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
    private static final Integer MAX_IMAGES = 1;

    // PRIORITY.....................................................................................
    // TODO: description
    private static final Integer PRIORITY = Process.THREAD_PRIORITY_VIDEO;

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
    ImageReaderListener() { super(); }

    //**********************************************************************************************
    // Class Methods
    //---------------------

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // openSurface..................................................................................
    /**
     * TODO: description, comments and logging
     *
     * @param imageFormat
     * @param imageSize
     */
    void openSurface(@NonNull Activity activity,
                     @NonNull Integer imageFormat, @NonNull Size imageSize) {

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

    // onImageAvailable.............................................................................
    /**
     * TODO: description, comments and logging
     * @param reader
     */
    @Override
    public void onImageAvailable(@NonNull ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireNextImage();

            // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
            ByteBuffer imageBytes = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[imageBytes.capacity()];
            imageBytes.get(data);
            image.close();

            ImageProcessor.processImage(data);
        } catch (IllegalStateException e) {
            // TODO: error
            if (image != null) {
                image.close();
            }
        }
    }

}