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

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.camera2.CameraController;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final public class SurfaceManager {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final SurfaceManager mInstance = new SurfaceManager();

    // mImageReaderListener.........................................................................
    // TODO: description
    private static final ImageReaderListener mImageReaderListener = new ImageReaderListener();

    // mSurfaces....................................................................................
    // TODO: description
    private static final List<Surface> mSurfaces = new ArrayList<>();

    // Private Instance Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mTextureViewListener.........................................................................
    // TODO: description
    private final TextureViewListener mTextureViewListener = new TextureViewListener();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mImageReaderIsReady..........................................................................
    // TODO: description
    private Boolean mImageReaderIsReady = false;

    // mTextureViewIsReady..........................................................................
    // TODO: description
    private Boolean mTextureViewIsReady = false;

    // mOutputFormat................................................................................
    // TODO: description
    private Integer mOutputFormat;

    // mOutputSize..................................................................................
    // TODO: description
    private Size mOutputSize;

    // mNextRunnable................................................................................
    // TODO: description
    private Runnable mNextRunnable;

    // mNextHandler.................................................................................
    // TODO: description
    private Handler mNextHandler;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // SurfaceManager...............................................................................
    /**
     * TODO: description, comments and logging
     */
    private SurfaceManager() {}

    // Public Class Methods
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

        if (GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED) {
            // TextureView itself isn't known to StreamConfigurationMap
            // but TextureView uses SurfaceTexture, which is known
            classList.add(SurfaceTexture.class);
        }

        if (GlobalSettings.IMAGE_READER_SURFACE_ENABLED) {
            classList.add(ImageReader.class);
        }
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

        Log.e(Thread.currentThread().getName(), "SurfaceManager openSurfaces");

        mInstance.mOutputFormat = CameraController.getOutputFormat();
        mInstance.mOutputSize   = CameraController.getOutputSize();

        assert mInstance.mOutputFormat != null;
        assert mInstance.mOutputSize   != null;

        if (handler == null) {
            mInstance.mNextHandler = new Handler(activity.getMainLooper());
        }
        mInstance.mNextHandler  = handler;
        mInstance.mNextRunnable = runnable;

        if (GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED) {
            mInstance.mTextureViewListener.openSurface(activity);
        }

        if (GlobalSettings.IMAGE_READER_SURFACE_ENABLED) {
            mImageReaderListener.openSurface(activity, mInstance.mOutputFormat, mInstance.mOutputSize);
        }
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // surfaceHasOpened.............................................................................
    /**
     * TODO: description, comments and logging
     * @param surface bla
     */
    static void surfaceHasOpened(@NonNull Surface surface, @NonNull Class klass) {
        Log.e(Thread.currentThread().getName(), "SurfaceManager surfaceHasOpened: " + klass.getSimpleName());
        mSurfaces.add(surface);

        if (klass == TextureViewListener.class) {
            mInstance.mTextureViewIsReady = true;
        }

        if (klass == ImageReaderListener.class) {
            mInstance.mImageReaderIsReady = true;
        }

        surfaceReady();
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // surfaceReady.................................................................................
    /**
     * TODO: description, comments and logging
     */
    private static void surfaceReady() {
        Log.e(Thread.currentThread().getName(), "SurfaceManager SurfaceReady");
        boolean allReady = true;
        if (GlobalSettings.TEXTURE_VIEW_SURFACE_ENABLED) {
            allReady = allReady && mInstance.mTextureViewIsReady;
        }
        if (GlobalSettings.IMAGE_READER_SURFACE_ENABLED) {
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