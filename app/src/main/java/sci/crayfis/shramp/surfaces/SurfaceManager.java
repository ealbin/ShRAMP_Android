package sci.crayfis.shramp.surfaces;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import android.os.Process;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.util.HandlerManager;

@TargetApi(21)
public class SurfaceManager {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Flags to signify use of a surface (enable/disable output surface here)
    private static final boolean mEnableTextureViewOutput = true;
    private static final boolean mEnableImageReaderOutput = false;

    //..............................................................................................

    // Single instance of SurfaceManager
    private static final SurfaceManager mInstance = new SurfaceManager();

    // List of surfaces being used as output
    private static final List<Surface> mSurfaces = new ArrayList<>();

    //..............................................................................................

    // TextureView Listener (inner class)
    private TextureViewListener mTextureViewListener = new TextureViewListener();
    private ImageReaderListener mImageReaderListener = new ImageReaderListener();

    //..............................................................................................

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
    public static SurfaceManager getInstance() {return mInstance;}

    /**
     *
     * @return
     */
    public static List<Class> getOutputSurfaceClasses() {
        List<Class> classList = new ArrayList<>();

        if (mEnableTextureViewOutput) {
            // TextureView itself isn't known to StreamConfigurationMap
            // but TextureView uses SurfaceTexture, which is known
            classList.add(SurfaceTexture.class);
        }

        if (mEnableImageReaderOutput) {
            classList.add(ImageReader.class);
        }

        return classList;
    }

    //----------------------------------------------------------------------------------------------

    public void openSurfaces(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {

        mLogger.log("Opening TextureView");
        if (mEnableTextureViewOutput) {
            mTextureViewListener.openSurface(activity);
        }

        mLogger.log("Opening ImageReader");
        if (mEnableImageReaderOutput) {
            mImageReaderListener.openSurface(activity, imageFormat, bitsPerPixel, imageSize);
        }

        CaptureOverseer.surfacesReady(mSurfaces);
    }

    //----------------------------------------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TextureViewListener /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles everything to do with TextureView surface
     */
    private final class TextureViewListener implements TextureView.SurfaceTextureListener{

        private Handler     nHandler;
        private TextureView nTextureView;

        /**
         * Create surface
         * @param activity source context
         */
        private void openSurface(Activity activity) {
            mLogger.log("TextureView is opening");
            nHandler     = new Handler(activity.getMainLooper());
            nTextureView = new TextureView(activity);
            nTextureView.setSurfaceTextureListener(this);

            // program continues with onSurfaceTextureAvailable listener below
            activity.setContentView(nTextureView);
            mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
            mLogger.log("TextureView is open");
            // TODO what are arg1 and arg2?
            mSurfaces.add(new Surface(arg0));
            mLogger.log("return;");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
            // TODO (maybe no action?)
            mLogger.log("TextureView has been destroyed");
            mLogger.log("return false;");
            return false;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,int arg2) {
            // TODO (maybe no action?)
            mLogger.log("TextureView has changed size");
            mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
            // TODO: (maybe no action?)
            // TODO: could measure fps
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ImageReaderListener /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles everything to do with ImageReader surface
     */
    private final class ImageReaderListener implements ImageReader.OnImageAvailableListener {
        // ImageReader max simultaneous image access
        private static final int MAX_IMAGES = 1;

        private Handler     nHandler;
        private ImageReader nImageReader;

        /**
         * Do nothing constructor
         */
        private ImageReaderListener() {
            super();
        }

        /**
         * Create surface
         *
         * @param imageFormat  ImageFormat const int (either YUV_420_888 or RAW_SENSOR)
         * @param bitsPerPixel bits per pixel
         * @param imageSize    image size in pixels
         */
        private void openSurface(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {
            mLogger.log("ImageReader is opening");

            nHandler = HandlerManager.newHandler("ImageReader", Process.THREAD_PRIORITY_VIDEO);

            nImageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(),
                                                   imageFormat, MAX_IMAGES);
            nImageReader.setOnImageAvailableListener(this, nHandler);
            mSurfaces.add(nImageReader.getSurface());

            mLogger.log("return;");
        }

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireNextImage();

                // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
                ByteBuffer imageBytes = image.getPlanes()[0].getBuffer();
                image.close();

                CaptureOverseer.processImage(imageBytes);
            }
            catch (IllegalStateException e) {
                // TODO: handle this -- with synchronized shouldn't ever happen
                SurfaceManager.mLogger.log("ERROR: Illegal State Exception");
                if (image != null) {
                    image.close();
                }
            }
        }


















        public void done() {
            //super.doStatistics();
            /*
            Element doubleElement = Element.F64(mRenderScript);
            Type sumType = new Type.Builder(mRenderScript, doubleElement).setX(mImageSize.getWidth()).setY(mImageSize.getHeight()).create();
            Allocation output = Allocation.createTyped(mRenderScript, sumType, Allocation.USAGE_SCRIPT);

            double[] outarray = new double[mImageSize.getWidth() * mImageSize.getHeight()];
            mTimeValSumScript.forEach_getOutput(output);
            output.copyTo(outarray);

            String dump = "";
            for (int i = 1; i < 1001; i++) {
                dump += " " + df.format(outarray[i-1]);
                if (i % 10 == 0) { dump += "\n";}
            }
            mLogger.log("Value dump: \n" + dump);

            double mean = 0.;
            for (double val : outarray) {
                mean += val;
            }
            mean /= mImageSize.getWidth() * mImageSize.getHeight();

            mLogger.log("Average value: " + df.format(mean));
        }

        */
        }
    }
}


//private long elapsedTime = SystemClock.elapsedRealtimeNanos();

//private DecimalFormat df = new DecimalFormat("##.#");

