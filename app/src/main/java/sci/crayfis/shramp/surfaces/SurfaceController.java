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

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.camera2.CameraController;

/**
 * This class is intended to be the public face of all surface operations, controlling
 * creation, updating, etc internally.
 */
@TargetApi(21)
final public class SurfaceController {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // Reference to single instance
    private static final SurfaceController mInstance = new SurfaceController();

    // mImageReaderListener.........................................................................
    // Reference to single ImageReader surface for receiving camera frames
    private static final ImageReaderListener mImageReaderListener = new ImageReaderListener();

    // mSurfaces....................................................................................
    // Master list of any and all open surfaces ready for use
    private static final List<Surface> mSurfaces = new ArrayList<>();

    // Private Instance Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mTextureViewListener.........................................................................
    // Reference to single TextureView surface for displaying text or video, cannot be static
    // due to its link with the governing Activity
    private final TextureViewListener mTextureViewListener = new TextureViewListener();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageReaderIsReady..........................................................................
    // Status of ImageReader, true if ready for use, false if not
    private Boolean mImageReaderIsReady = false;

    // mTextureViewIsReady..........................................................................
    // Status of TextureView, true if ready for use, false if not
    private Boolean mTextureViewIsReady = false;

    // mOutputFormat................................................................................
    // Output format, either ImageFormat.RAW or ImageFormat.YUV_420_888
    private Integer mOutputFormat;

    // mOutputSize..................................................................................
    // Output image dimensions (width, height) in pixels
    private Size mOutputSize;

    // mNextRunnable................................................................................
    // After a surface is opened (asynchronously), execute this runnable on mNextHandler's thread
    private Runnable mNextRunnable;

    // mNextHandler.................................................................................
    // Handler of the thread to run mNextRunnable on after opening a surface asynchronously
    private Handler mNextHandler;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // SurfaceController...............................................................................
    /**
     * Nothing special, just create single instance
     */
    private SurfaceController() {}

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getOpenSurfaces..............................................................................
    /**
     * @return Master list of open surfaces ready to use
     */
    @NonNull
    @Contract(pure = true)
    public static List<Surface> getOpenSurfaces() {
        return mSurfaces;
    }

    // getOutputSurfaceClasses......................................................................
    /**
     * @return List of surface classes to be used, useful for determining output format / resolution
     */
    @NonNull
    public static List<Class> getOutputSurfaceClasses() {
        List<Class> classList = new ArrayList<>();

        // Video feed on screen
        if (GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED) {
            // The TextureView class itself isn't known to StreamConfigurationMap for determining
            // output format / resolution abilities, but TextureView turns out to use
            // SurfaceTexture, which is known to StreamConfigurationMap
            classList.add(SurfaceTexture.class);
        }

        // Image processing
        if (GlobalSettings.IMAGE_READER_SURFACE_ENABLED) {
            classList.add(ImageReader.class);
        }

        return classList;
    }

    // openSurfaces.................................................................................
    /**
     * Open all surfaces specified in GlobalSettings
     * @param activity The app-controlling activity
     * @param runnable Optional Runnable to run on handler's thread after asynchronous opening
     *                 all surfaces.  This method itself returns before the surfaces are open.
     * @param handler Handler to thread to run on after opening surfaces, defaults to main thread
     */
    public static void openSurfaces(@NonNull Activity activity,
                                    @Nullable Runnable runnable, @Nullable Handler handler) {

        mInstance.mOutputFormat = CameraController.getOutputFormat();
        mInstance.mOutputSize   = CameraController.getOutputSize();

        if (mInstance.mOutputFormat == null || mInstance.mOutputSize == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Output format/size cannot be null");
            MasterController.quitSafely();
            return;
        }

        if (handler == null) {
            mInstance.mNextHandler = new Handler(activity.getMainLooper());
        }
        mInstance.mNextHandler  = handler;
        mInstance.mNextRunnable = runnable;

        // Video feed on screen
        if (GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED) {
            mInstance.mTextureViewListener.openSurface(activity);
        }

        // Image processing
        if (GlobalSettings.IMAGE_READER_SURFACE_ENABLED) {
            mImageReaderListener.openSurface(mInstance.mOutputFormat, mInstance.mOutputSize);
        }
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // surfaceHasOpened.............................................................................
    /**
     * Called by other classes in this immediate package as their surfaces come online
     * @param surface Surface that has opened
     * @param klass Class of surface that has opened
     */
    static void surfaceHasOpened(@NonNull Surface surface, @NonNull Class klass) {
        Log.e(Thread.currentThread().getName(), klass.getSimpleName() + " surface has opened");
        mSurfaces.add(surface);

        if (klass == TextureViewListener.class) {
            mInstance.mTextureViewIsReady = true;
        }

        if (klass == ImageReaderListener.class) {
            mInstance.mImageReaderIsReady = true;
        }

        boolean allReady = true;
        if (GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED) {
            allReady = allReady && mInstance.mTextureViewIsReady;
        }
        if (GlobalSettings.IMAGE_READER_SURFACE_ENABLED) {
            allReady = allReady && mInstance.mImageReaderIsReady;
        }

        if (allReady) {
            if (mInstance.mNextRunnable != null) {
                mInstance.mNextHandler.post(mInstance.mNextRunnable);
            }
            mInstance.mNextHandler  = null;
            mInstance.mNextRunnable = null;
        }
    }

}