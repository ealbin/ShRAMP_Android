package Trash;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.CaptureOverseer;
//import sci.crayfis.shramp.ScriptC_TimeValSum;
import sci.crayfis.shramp.logging.ShrampLogger;

@TargetApi(21)
public class SurfaceManagerOLD {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Single instance of SurfaceManager
    private static final SurfaceManagerOLD mInstance = new SurfaceManagerOLD();

    // List of surfaces being used as output
    private static final List<Surface> mSurfaces = new ArrayList<>();

    // Flags to signify use of a surface (enable/disable output surface here)
    private static final boolean mEnableTextureViewOutput = false;
    private static final boolean mEnableImageReaderOutput = true;
    private static final boolean mEnableAllocationOutput  = false;

    // Flags to signify a surface is ready
    private static Boolean mTextureViewReady = false;
    private static Boolean mImageReaderReady = false;
    private static Boolean mAllocationReady  = false;
    // other surface
    // other surface

    // TextureView Listener (inner class)
    private TextureViewListener mTextureViewListener = new TextureViewListener();
    private ImageReaderListener mImageReaderListener = new ImageReaderListener();
    private AllocationListener  mAllocationListener  = new AllocationListener();
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
    private SurfaceManagerOLD() {}

    /**
     * In place of a constructor
     * @return single instance of SurfaceManager
     */
    public synchronized static SurfaceManagerOLD getInstance() {return SurfaceManagerOLD.mInstance;}

    /**
     * Activate/Deactivate output classes here
     * @return
     */
    public static List<Class> getOutputSurfaceClasses() {
        List<Class> classList = new ArrayList<>();

        // Enable TextureView
        if (SurfaceManagerOLD.mEnableTextureViewOutput) {
            // TextureView itself isn't known to StreamConfigurationMap
            // but TextureView uses SurfaceTexture, which is known
            classList.add(SurfaceTexture.class);
        }

        // Enable ImageReader
        if (SurfaceManagerOLD.mEnableImageReaderOutput) {
            classList.add(ImageReader.class);
        }

        // Enable Allocation
        if (SurfaceManagerOLD.mEnableAllocationOutput) {
            classList.add(Allocation.class);
        }

        return classList;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Set up surfaces for output operation (executed in main thread)
     * @param activity
     */
    public synchronized void openUiSurfaces(Activity activity) {
        SurfaceManagerOLD.mLogger.log("Opening TextureView");

        if (SurfaceManagerOLD.mEnableTextureViewOutput) {
           mTextureViewListener.openSurface(activity);
        }
        // other surface
        // other surface

        if (!SurfaceManagerOLD.mEnableTextureViewOutput) { // && other !surfaces
            surfaceReady();
        }
    }

    /**
     * Set up surfaces for output operation (executed in camera thread)
     * @param imageFormat
     * @param bitsPerPixel
     * @param imageSize
     */
    public synchronized void openImageSurfaces(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {
        SurfaceManagerOLD.mLogger.log("Opening ImageReader");

        if (SurfaceManagerOLD.mEnableImageReaderOutput) {
           mImageReaderListener.openSurface(activity, imageFormat, bitsPerPixel, imageSize);
        }

        if (SurfaceManagerOLD.mEnableAllocationOutput) {
            mAllocationListener.openSurface(activity, imageFormat, bitsPerPixel, imageSize);
        }
        // other surface
        // other surface

        if (!SurfaceManagerOLD.mEnableImageReaderOutput
                && !SurfaceManagerOLD.mEnableAllocationOutput) { // && other !surfaces
            surfaceReady();
        }
    }


    //----------------------------------------------------------------------------------------------

    /**
     * Called whenever a surface is initialized, once all surfaces check in, notify CaptureOverseer
     */
    private synchronized void surfaceReady() {
        SurfaceManagerOLD.mLogger.log("A surface is ready");

        boolean isReady = true;
        if (SurfaceManagerOLD.mEnableTextureViewOutput) {
            isReady = isReady && SurfaceManagerOLD.mTextureViewReady;
        }
        if (SurfaceManagerOLD.mEnableImageReaderOutput) {
            isReady = isReady && SurfaceManagerOLD.mImageReaderReady;
        }
        if (SurfaceManagerOLD.mEnableAllocationOutput) {
            isReady = isReady && SurfaceManagerOLD.mAllocationReady;
        }

        if (isReady) {
            SurfaceManagerOLD.mLogger.log("All surfaces are ready");
            //CaptureOverseer.surfacesReady(mSurfaces);
        }
        else {
            SurfaceManagerOLD.mLogger.log("Not all surfaces are ready, continuing to wait");
            // otherwise wait for the remaining surfaces to check in..
        }
        SurfaceManagerOLD.mLogger.log("return;");
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
            SurfaceManagerOLD.mLogger.log("TextureView is opening");
           mTextureView = new TextureView(activity);
           mTextureView.setSurfaceTextureListener(this);
            // program continues with onSurfaceTextureAvailable listener below
            activity.setContentView(mTextureView);
            SurfaceManagerOLD.mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
            SurfaceManagerOLD.mLogger.log("TextureView is open");
            // TODO what are arg1 and arg2?
            SurfaceManagerOLD.mSurfaces.add(new Surface(arg0));
            SurfaceManagerOLD.mTextureViewReady = true;
            SurfaceManagerOLD.mLogger.log("return;");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
            // TODO (maybe no action?)
            SurfaceManagerOLD.mLogger.log("TextureView has been destroyed");
            SurfaceManagerOLD.mLogger.log("return false;");
            return false;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,int arg2) {
            // TODO (maybe no action?)
            SurfaceManagerOLD.mLogger.log("TextureView has changed size");
            SurfaceManagerOLD.mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
            // TODO (maybe no action?)
        }
    }

    public void done() {
        mImageReaderListener.done();
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
        private void openSurface(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {
            SurfaceManagerOLD.mLogger.log("ImageReader is opening");
            mImageFormat  = imageFormat;
            mBitsPerPixel = bitsPerPixel;
            mImageSize    = imageSize;

           mImageReader = ImageReader.newInstance(
                    imageSize.getWidth(), imageSize.getHeight(), imageFormat, ImageReaderListener.MAX_IMAGES);

            startHandler();
            mImageReader.setOnImageAvailableListener(this, mHandler);
            SurfaceManagerOLD.mSurfaces.add(this.mImageReader.getSurface());
            SurfaceManagerOLD.mImageReaderReady = true;
            SurfaceManagerOLD.mInstance.surfaceReady();

            //--------------------------------------------------------------------------------------
            // Renderscript
            //--------------------------------------------------------------------------------------
            // when this works, consider trying CREATE_FLAG_LOW_LATENCY and LOW_POWER
            mRenderScript = RenderScript.create(activity,
                    RenderScript.ContextType.NORMAL,
                    RenderScript.CREATE_FLAG_NONE);

            int width  = imageSize.getWidth();
            int height = imageSize.getHeight();

            // Create image data allocation
            Element element = Element.U8(mRenderScript);
            if (imageFormat == ImageFormat.RAW_SENSOR) {
                element = Element.U16(mRenderScript);
            }
            Type imageDataType = new Type.Builder(mRenderScript, element).setX(width).setY(height).create();
            //Type imageDataType = Type.createXY(mRenderScript, element, width, height);
            int usage = Allocation.USAGE_SCRIPT;
            mImageData = Allocation.createTyped(mRenderScript, imageDataType, usage);

            // Create sum allocation
            Element doubleElement = Element.F64(mRenderScript);
            Type sumType = new Type.Builder(mRenderScript, doubleElement).setX(width).setY(height).create();
            //Type sumType = Type.createXY(mRenderScript, doubleElement, width, height);
            mTimeValSum = Allocation.createTyped(mRenderScript, sumType, Allocation.USAGE_SCRIPT);//Allocation.USAGE_IO_OUTPUT);

            //mTimeValSumScript = new ScriptC_TimeValSum(mRenderScript);
            //mTimeValSumScript.set_gRunningSum(mTimeValSum);
            //======================================================================================

            SurfaceManagerOLD.mLogger.log("return;");
        }

        private void startHandler() {
            String name = "ImageReaderListener";
            SurfaceManagerOLD.mLogger.log("Starting thread: " + name);
           mHandlerThread = new HandlerThread(name, Process.THREAD_PRIORITY_VIDEO);
           mHandlerThread.start();  // must start before calling .getLooper()
           mHandler       = new Handler(this.mHandlerThread.getLooper());
            SurfaceManagerOLD.mLogger.log("Thread: " + name + " started; return;");
        }

        private DecimalFormat df = new DecimalFormat("##.#");

        private RenderScript       mRenderScript;
        private Allocation         mImageData;
        private Allocation         mTimeValSum;
        //private ScriptC_TimeValSum mTimeValSumScript;

        public void done() {

            Element doubleElement = Element.F64(mRenderScript);
            Type sumType = new Type.Builder(mRenderScript, doubleElement).setX(mImageSize.getWidth()).setY(mImageSize.getHeight()).create();
            Allocation output = Allocation.createTyped(mRenderScript, sumType, Allocation.USAGE_SCRIPT);

            double[] outarray = new double[mImageSize.getWidth() * mImageSize.getHeight()];
            //mTimeValSumScript.forEach_getOutput(output);
            output.copyTo(outarray);

            String dump = "";
            for (int i = 0; i < 100; i++) {
                dump += " " + Double.toString(outarray[i]);
                if (i % 10 == 0) { dump += "\n";}
            }
            mLogger.log("Value dump: \n" + dump);



            //mTimeValSumScript.invoke_dump();
            /*
            mTimeValSum = mTimeValSumScript.get_gRunningSum();
            ImageReader sum = ImageReader.newInstance(
                    mImageSize.getWidth(), mImageSize.getHeight(), mImageFormat, 1);
            mTimeValSum.setSurface(sum.getSurface());
            Image image = sum.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            switch (image.getFormat()) {
                case (ImageFormat.YUV_420_888): {
                    byte[] data = new byte[buffer.capacity()];
                    break;
                }
                case (ImageFormat.RAW_SENSOR): {
                    ShortBuffer shortBuffer = buffer.asShortBuffer();
                    short[] data = new short[buffer.capacity()];
                    String ploop = "";
                    for (int i = 0; i < 20; i++ ) {
                        ploop += Short.toString(data[i]) + "  ";
                    }
                    mLogger.log("Data:  "  + ploop);
                    break;
                }
            }
            */
        }


        @Override
        public void onImageAvailable(ImageReader reader) {
            synchronized (SurfaceManagerOLD.SAVE_LOCK)
            {
                Image image = null;
                try {
                    image = reader.acquireNextImage();
                    //image = reader.acquireLatestImage();
                    //long time = SystemClock.elapsedRealtimeNanos();
                    //String fps = df.format(1e9 / (double)(time - elapsedTime));
                    //elapsedTime = time;
                    //Log.e(Thread.currentThread().getName(),"reading image, realtime fps: " + fps);

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    switch (image.getFormat()) {
                        case (ImageFormat.YUV_420_888): {
                            //byte[] data = new byte[buffer.capacity()];
                            //buffer.get(data);
                            //String vals = "";
                            //for (int i = 10000; i < 10020; i++) {
                            //    vals += "  " + Short.toString(data[i]);
                            //}
                            //Log.e("Some values: ", vals);
                            //mLogger.log("BUFFER: " + Integer.toString(buffer.capacity()) + " should be: " + Integer.toString(image.getHeight()*image.getWidth()));
                            //mImageData.copyFrom(data);
                            //mImageData.copy2DRangeFrom(0, 0, mImageSize.getWidth(), mImageSize.getHeight(), data);
                            //mTimeValSumScript.forEach_addYuv(mImageData);
                            break;
                        }
                        case (ImageFormat.RAW_SENSOR): {
                            ShortBuffer shortBuffer = buffer.asShortBuffer();

                            if( mImageSize.getWidth() * mImageSize.getHeight() != shortBuffer.capacity()) {
                                // TODO: ERROR
                            }

                            short[] data = new short[shortBuffer.capacity()];
                            shortBuffer.get(data);

                            mImageData.copyFrom(data);
                            //mTimeValSumScript.forEach_addRaw(mImageData);

                            //mLogger.log("BUFFER: " + Integer.toString(buffer.capacity()) + " should be: " + Integer.toString(2*image.getHeight()*image.getWidth()));
                            //String vals = "";
                            //for (int i = 10000; i < 10020; i++) {
                            //    vals += "  " + Integer.toString(data[i]);
                            //}
                            //Log.e("Some values: ", vals);
                            //long sum = 0L;
                            //for (short val : data) {
                            //    sum += val;
                            //}
                            //mImageData.copyFrom(data);
                            //mImageData.copy2DRangeFrom(0, 0, mImageSize.getWidth(), mImageSize.getHeight(), data);
                            //mTimeValSumScript.set_gInput(mImageData);
                            //mTimeValSumScript.forEach_addRaw(mImageData);
                            //Log.e("Sum should be: " + Long.toString(sum) + ", its: ", Long.toString(mTimeValSumScript.get_gSum()));
                            break;
                        }
                    }

                    //mTimeValSumScript.forEach_add(mImageData);

                    // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
                    //ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    //byte[] data = new byte[buffer.capacity()];
                    //buffer.get(data);

                    //Log.e(Thread.currentThread().getName(), "sending " + Long.toString(image.getTimestamp()) + " to data manager");
                    //DataManager.saveData(image.getTimestamp(), data);
                }
                catch (IllegalStateException e) {
                    // TODO: handle this -- with save lock shouldn't ever happen
                    SurfaceManagerOLD.mLogger.log("ERROR: Illegal State Exception");
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



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // AllocationListener  /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles everything to do with Allocation surface
     */
    private final class AllocationListener implements Allocation.OnBufferAvailableListener {

        private RenderScript       mRenderScript;
        private Allocation         mImageAllocation;
        private Allocation         mTimeValSum;
        //private ScriptC_TimeValSum mTimeValSumScript;

        private final DecimalFormat df = new DecimalFormat("##.#");

        /**
         * Create surface
         * @param imageFormat ImageFormat const int (must be YUV_420_888)
         * @param bitsPerPixel bits per pixel
         * @param imageSize image size in pixels
         */
        private void openSurface(Activity activity, int imageFormat, int bitsPerPixel, Size imageSize) {
            SurfaceManagerOLD.mLogger.log("Allocation is opening");



            //--------------------------------------------------------------------------------------
            // Renderscript
            //--------------------------------------------------------------------------------------
            // when this works, consider trying CREATE_FLAG_LOW_LATENCY and LOW_POWER
            mRenderScript = RenderScript.create(activity,
                    RenderScript.ContextType.NORMAL,
                    RenderScript.CREATE_FLAG_NONE);

            int width  = imageSize.getWidth();
            int height = imageSize.getHeight();

            // Create image data allocation
            Element element = Element.U8(mRenderScript);
            if (imageFormat == ImageFormat.RAW_SENSOR) {
                element = Element.U16(mRenderScript);
            }
            Type imageDataType = new Type.Builder(mRenderScript, element).setX(width).setY(height).create();
            //Type imageDataType = Type.createXY(mRenderScript, element, width, height);
            int usage = Allocation.USAGE_SCRIPT | Allocation.USAGE_IO_INPUT;
            mImageAllocation = Allocation.createTyped(mRenderScript, imageDataType, usage);
            mImageAllocation.setOnBufferAvailableListener(this);
            SurfaceManagerOLD.mSurfaces.add(mImageAllocation.getSurface());

            // Create sum allocation
            Element doubleElement = Element.F64(mRenderScript);
            Type sumType = new Type.Builder(mRenderScript, doubleElement).setX(width).setY(height).create();
            //Type sumType = Type.createXY(mRenderScript, doubleElement, width, height);
            mTimeValSum = Allocation.createTyped(mRenderScript, sumType, Allocation.USAGE_SCRIPT);//Allocation.USAGE_IO_OUTPUT);

            //mTimeValSumScript = new ScriptC_TimeValSum(mRenderScript);
            //mTimeValSumScript.set_gRunningSum(mTimeValSum);
            //======================================================================================


            // TODO: if imageFormat isn't YUV, it's an error

            // when this works, consider trying CREATE_FLAG_LOW_LATENCY and LOW_POWER
            //mRenderScript = RenderScript.create(activity,
            //                                    RenderScript.ContextType.NORMAL,
            //                                    RenderScript.CREATE_FLAG_NONE);

            //int width  = imageSize.getWidth();
            //int height = imageSize.getHeight();

            // Create image data allocation
            // TODO: imageformat might be 8 bits YUV or 12..
            //Type yuvType = new Type.Builder(mRenderScript, Element.YUV(mRenderScript))
            //                        .setX(width).setY(height).setYuvFormat(ImageFormat.YUV_420_888).create();
            //int usage = Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT;
            //mImageAllocation = Allocation.createTyped(mRenderScript, yuvType, usage);
            //mImageAllocation.setOnBufferAvailableListener(this);
            //SurfaceManager.mSurfaces.add(mImageAllocation.getSurface());

            // Create sum allocation
            //Element doubleElement = Element.F64(mRenderScript);
            //Type sumType = Type.createXY(mRenderScript, doubleElement, width, height);
            //mTimeValSum = Allocation.createTyped(mRenderScript, sumType, Allocation.USAGE_IO_OUTPUT);

            //mTimeValSumScript = new ScriptC_TimeValSum(mRenderScript);
            //mTimeValSumScript.set_gRunningSum(mTimeValSum);


            SurfaceManagerOLD.mAllocationReady = true;
            SurfaceManagerOLD.mInstance.surfaceReady();
            SurfaceManagerOLD.mLogger.log("return;");
        }

        @Override
        public void onBufferAvailable(Allocation allocation) {
            Log.e("Allocation", "do stuff");
            allocation.ioReceive();
            //mLogger.log("FUUUUUUUUUUUUUK:  " + allocation.getType().getElement().getDataType().name());

            //mTimeValSumScript.forEach_addRaw(allocation);

            //mTimeValSumScript.forEach_YUVadd(allocation);



            /*
            Type type = allocation.getType();
            type.
            byte[] data = new byte[size];
            allocation.copyTo(data);
            String ohboy = "";
            for (int i = 0; i < 20; i++ ) {
                ohboy += Byte.toString(data[i]);
            }
            Log.e("oh boy: ", ohboy);
            */
            //ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            //byte[] data = new byte[buffer.capacity()];
            //buffer.get(data);

            //synchronized (SurfaceManager.SAVE_LOCK)
            /*
            {
                Image image = null;
                try {
                    //image = reader.acquireNextImage();
                    image = reader.acquireLatestImage();
                    long time = SystemClock.elapsedRealtimeNanos();
                    String fps = df.format(1e9 / (double)(time - elapsedTime));
                    elapsedTime = time;
                    Log.e(Thread.currentThread().getName(),"reading image, realtime fps: " + fps);
                    // RAW_SENSOR has 1 plane, YUV has 3 but the luminosity (Y) is plane 1
                    //ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    //byte[] data = new byte[buffer.capacity()];
                    //buffer.get(data);

                    //Log.e(Thread.currentThread().getName(), "sending " + Long.toString(image.getTimestamp()) + " to data manager");
                    //DataManager.saveData(image.getTimestamp(), data);
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
            */
        }

    }
    // private final class SomethingListener ...
}
