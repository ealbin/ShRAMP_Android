package sci.crayfis.shramp.surfaces;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.DataManager;

@TargetApi(21)
public class SurfaceManager {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Single instance of SurfaceManager
    private static final SurfaceManager mInstance = new SurfaceManager();

    // List of surfaces being used as output
    private static final List<Surface> mSurfaces = new ArrayList<>();

    // Flags to signify use of a surface (enable/disable output surface here)
    private static final boolean mEnableTextureViewOutput = true;
    private static final boolean mEnableImageReaderOutput = true;

    // Flags to signify a surface is ready
    private static Boolean mTextureViewReady = false;
    private static Boolean mImageReaderReady = false;
    // other surface
    // other surface

    // TextureView Listener (inner class)
    private TextureViewListener mTextureViewListener = new TextureViewListener();
    private ImageReaderListener mImageReaderListener = new ImageReaderListener();
    // other surface inner class
    // other surface inner class

    private static final Object SAVE_LOCK = new Object();

    // logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Disable constructor
     */
    private SurfaceManager() {}

    /**
     * In place of a constructor
     * @return single instance of SurfaceManager
     */
    public synchronized static SurfaceManager getInstance() {return SurfaceManager.mInstance;}

    /**
     * Activate/Deactivate output classes here
     * @return
     */
    public static List<Class> getOutputSurfaceClasses() {
        List<Class> classList = new ArrayList<>();

        // Enable TextureView
        if (SurfaceManager.mEnableTextureViewOutput) {
            // TextureView itself isn't known to StreamConfigurationMap
            // but TextureView uses SurfaceTexture, which is known
            classList.add(SurfaceTexture.class);
        }

        // Enable ImageReader
        if (SurfaceManager.mEnableImageReaderOutput) {
            classList.add(ImageReader.class);
        }

        return classList;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Set up surfaces for output operation (executed in main thread)
     * @param activity
     */
    public synchronized void openUiSurfaces(Activity activity) {
        SurfaceManager.mLogger.log("Opening TextureView");

        if (SurfaceManager.mEnableTextureViewOutput) {
           mTextureViewListener.openSurface(activity);
        }
        // other surface
        // other surface
    }

    /**
     * Set up surfaces for output operation (executed in camera thread)
     * @param imageFormat
     * @param bitsPerPixel
     * @param imageSize
     */
    public synchronized void openImageSurfaces(int imageFormat, int bitsPerPixel, Size imageSize) {
        SurfaceManager.mLogger.log("Opening ImageReader");

        if (SurfaceManager.mEnableImageReaderOutput) {
           mImageReaderListener.openSurface(imageFormat, bitsPerPixel, imageSize);
        }
        // other surface
        // other surface
    }


    //----------------------------------------------------------------------------------------------

    /**
     * Called whenever a surface is initialized, once all surfaces check in, notify CaptureOverseer
     */
    private synchronized void surfaceReady() {
        SurfaceManager.mLogger.log("A surface is ready");

        boolean isReady = true;
        if (SurfaceManager.mEnableTextureViewOutput) {
            isReady = isReady && SurfaceManager.mTextureViewReady;
        }
        if (SurfaceManager.mEnableImageReaderOutput) {
            isReady = isReady && SurfaceManager.mImageReaderReady;
        }

        if (isReady) {
            SurfaceManager.mLogger.log("All surfaces are ready");
            CaptureOverseer.surfacesReady(mSurfaces);
        }
        else {
            SurfaceManager.mLogger.log("Not all surfaces are ready, continuing to wait");
            // otherwise wait for the remaining surfaces to check in..
        }
        SurfaceManager.mLogger.log("return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TextureViewListener /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles everything to do with TextureView surface
     */
    private final class TextureViewListener implements TextureView.SurfaceTextureListener{
        private TextureView mTextureView;

        private long mUpdateCount = 0;
        private final long UPDATE_LIMIT = 5;

        /**
         * Create surface
         * @param activity source context
         */
        private void openSurface(Activity activity) {
            SurfaceManager.mLogger.log("TextureView is opening");
           mTextureView = new TextureView(activity);
           mTextureView.setSurfaceTextureListener(this);
            // program continues with onSurfaceTextureAvailable listener below
            activity.setContentView(mTextureView);
            SurfaceManager.mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
            SurfaceManager.mLogger.log("TextureView is open");
            // TODO what are arg1 and arg2?
            SurfaceManager.mSurfaces.add(new Surface(arg0));
            SurfaceManager.mTextureViewReady = true;
            SurfaceManager.mLogger.log("return;");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
            // TODO (maybe no action?)
            SurfaceManager.mLogger.log("TextureView has been destroyed");
            SurfaceManager.mLogger.log("return false;");
            return false;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,int arg2) {
            // TODO (maybe no action?)
            SurfaceManager.mLogger.log("TextureView has changed size");
            SurfaceManager.mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
            // TODO (maybe no action?)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ImageReaderListener /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles everything to do with ImageReader surface
     */
    private final class ImageReaderListener implements ImageReader.OnImageAvailableListener {

        private static final int MAX_IMAGES = 1;
        private ImageReader mImageReader;

        private Handler mHandler;
        private HandlerThread mHandlerThread;

        private int  mImageFormat;
        private int  mBitsPerPixel;
        private Size mImageSize;

        private long elapsedTime = SystemClock.elapsedRealtimeNanos();

        /**
         * Create surface
         * @param imageFormat ImageFormat const int (either YUV_420_888 or RAW_SENSOR)
         * @param bitsPerPixel bits per pixel
         * @param imageSize image size in pixels
         */
        private void openSurface(int imageFormat, int bitsPerPixel, Size imageSize) {
            SurfaceManager.mLogger.log("ImageReader is opening");
            mImageFormat  = imageFormat;
            mBitsPerPixel = bitsPerPixel;
            mImageSize    = imageSize;

           mImageReader = ImageReader.newInstance(
                    imageSize.getWidth(), imageSize.getHeight(), imageFormat, ImageReaderListener.MAX_IMAGES);

            startHandler();
            mImageReader.setOnImageAvailableListener(this, mHandler);
            SurfaceManager.mSurfaces.add(this.mImageReader.getSurface());
            SurfaceManager.mImageReaderReady = true;
            SurfaceManager.mInstance.surfaceReady();

            SurfaceManager.mLogger.log("return;");
        }

        private void startHandler() {
            String name = "ImageReaderListener";
            SurfaceManager.mLogger.log("Starting thread: " + name);
           mHandlerThread = new HandlerThread(name);
           mHandlerThread.start();  // must start before calling .getLooper()
           mHandler       = new Handler(this.mHandlerThread.getLooper());
            SurfaceManager.mLogger.log("Thread: " + name + " started; return;");
        }


        @Override
        public void onImageAvailable(ImageReader reader) {
            synchronized (SurfaceManager.SAVE_LOCK)
            {
                Image image = null;
                try {
                    image = reader.acquireNextImage();

                    long time = SystemClock.elapsedRealtimeNanos();
                    DecimalFormat df = new DecimalFormat("##.#");
                    String fps = df.format(1e9 / (double)(time - elapsedTime));
                    elapsedTime = time;
                    Log.e(Thread.currentThread().getName(),"reading image, realtime fps: " + fps);
                    // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] data = new byte[buffer.capacity()];
                    buffer.get(data);

                    DataManager.saveData(image.getTimestamp(), data);
                }
                catch (IllegalStateException e) {
                    // TODO: handle this -- with save lock shouldn't ever happen
                    SurfaceManager.mLogger.log("ERROR: Illegal State Exception");
                }
                finally {
                    if (image != null) {
                        // purge image from reader
                        image.close();
                    }
                }
            }
        }

    }

    // private final class SomethingListener ...
}
