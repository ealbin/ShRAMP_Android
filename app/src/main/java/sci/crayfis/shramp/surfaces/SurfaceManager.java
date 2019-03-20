package sci.crayfis.shramp.surfaces;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class SurfaceManager {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TEXTURE_VIEW_SURFACE_ENABLED.................................................................
    // TODO: description
    private static final Boolean TEXTURE_VIEW_SURFACE_ENABLED = GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED;

    // IMAGE_READER_SURFACE_ENABLED.................................................................
    // TODO: description
    private static final Boolean IMAGE_READER_SURFACE_ENABLED = GlobalSettings.IMAGE_READER_SURFACE_ENABLED;

    // Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageReaderListener.........................................................................
    // TODO: description
    private static final ImageReaderListener mImageReaderListener = new ImageReaderListener();

    // mInstance....................................................................................
    // TODO: description
    private static final SurfaceManager mInstance = new SurfaceManager();

    // mSurfaces....................................................................................
    // TODO: description
    private static final List<Surface> mSurfaces = new ArrayList<>();

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mTextureViewListener.........................................................................
    // TODO: description
    private final TextureViewListener mTextureViewListener = new TextureViewListener();

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageReaderIsReady..........................................................................
    // TODO: description
    private Boolean mImageReaderIsReady = false;

    // mNextHandler.................................................................................
    // TODO: description
    private Handler mNextHandler;

    // mNextRunnable................................................................................
    // TODO: description
    private Runnable mNextRunnable;

    // mOutputFormat................................................................................
    // TODO: description
    private Integer mOutputFormat;

    // mOutputSize..................................................................................
    // TODO: description
    private Size mOutputSize;

    // mTextureViewIsReady..........................................................................
    // TODO: description
    private Boolean mTextureViewIsReady = false;


    //==============================================================================================
    // logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    //==============================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // SurfaceManager...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private SurfaceManager() {
        //Log.e(Thread.currentThread().getName(), "SurfaceManager SurfaceManager");
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getOpenSurfaces..............................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    public static List<Surface> getOpenSurfaces() {
        return mSurfaces;
    }

    // getOutputSurfaceClasses......................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    public static List<Class> getOutputSurfaceClasses() {
        List<Class> classList = new ArrayList<>();

        if (TEXTURE_VIEW_SURFACE_ENABLED) {
            // TextureView itself isn't known to StreamConfigurationMap
            // but TextureView uses SurfaceTexture, which is known
            classList.add(SurfaceTexture.class);
        }

        if (IMAGE_READER_SURFACE_ENABLED) {
            classList.add(ImageReader.class);
        }
        //Log.e(Thread.currentThread().getName(), "SurfaceManager getOutputSurfaceClasses");
        return classList;
    }

    // openSurfaces.................................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     * @param runnable bla
     * @param handler bla
     */
    public static void openSurfaces(@NonNull Activity activity,
                                    @Nullable Runnable runnable, @Nullable Handler handler) {

        //Log.e(Thread.currentThread().getName(), "SurfaceManager openSurfaces");

        mInstance.mOutputFormat = CameraController.getOutputFormat();
        mInstance.mOutputSize   = CameraController.getOutputSize();

        assert mInstance.mOutputFormat != null;
        assert mInstance.mOutputSize   != null;

        if (handler == null) {
            mInstance.mNextHandler = new Handler(activity.getMainLooper());
        }
        mInstance.mNextHandler  = handler;
        mInstance.mNextRunnable = runnable;

        //long startTime = SystemClock.elapsedRealtimeNanos();
        //mLogger.log("Opening TextureView");
        if (TEXTURE_VIEW_SURFACE_ENABLED) {
            mInstance.mTextureViewListener.openSurface(activity);
        }
        //String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        //mLogger.log("TextureView prepared in: " + elapsed + " [ns]");

        //sartTime = SystemClock.elapsedRealtimeNanos();
        //mLogger.log("Opening ImageReader");
        if (IMAGE_READER_SURFACE_ENABLED) {
            mImageReaderListener.openSurface(activity, mInstance.mOutputFormat, mInstance.mOutputSize);
        }
        //elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        //mLogger.log("ImageReader prepared in: " + elapsed + " [ns]");
    }

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // surfaceHasOpened.............................................................................
    /**
     * TODO: description, comments and logging
     * @param surface bla
     */
    static void surfaceHasOpened(@NonNull Surface surface, @NonNull Class klass) {
        //Log.e(Thread.currentThread().getName(), "SurfaceManager surfaceHasOpened");
        mSurfaces.add(surface);

        if (klass == TextureViewListener.class) {
            mInstance.mTextureViewIsReady = true;
        }

        if (klass == ImageReaderListener.class) {
            mInstance.mImageReaderIsReady = true;
        }

        surfaceReady();
    }

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // surfaceReady.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void surfaceReady() {
        //Log.e(Thread.currentThread().getName(), "SurfaceManager SurfaceReady");
        boolean allReady = true;
        if (TEXTURE_VIEW_SURFACE_ENABLED) {
            allReady = allReady && mInstance.mTextureViewIsReady;
        }
        if (IMAGE_READER_SURFACE_ENABLED) {
            allReady = allReady && mInstance.mImageReaderIsReady;
        }

        if (allReady) {
            if (mInstance.mNextRunnable != null && mInstance.mNextHandler != null) {
                mInstance.mNextHandler.post(mInstance.mNextRunnable);
                mInstance.mNextHandler  = null;
                mInstance.mNextRunnable = null;
            }
        }
    }

}

