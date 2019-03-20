package sci.crayfis.shramp.surfaces;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
final class TextureViewListener implements TextureView.SurfaceTextureListener {

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mSurface.....................................................................................
    // TODO: description
    private Surface mSurface;

    // mSurfaceHeight......................................................................................
    // TODO: description
    private Integer mSurfaceHeight;

    // mSurfaceWidth.......................................................................................
    // TODO: description
    private Integer mSurfaceWidth;

    // mTextureView.................................................................................
    // TODO: description
    private TextureView mTextureView;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Constructors
    //-------------

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TextureViewListener..........................................................................
    /**
     * TODO: description, comments and logging
     */
    TextureViewListener() {
        super();
        Log.e(Thread.currentThread().getName(), "TextureViewListener TextureViewListener");
    }

    //**********************************************************************************************
    // Class Methods
    //---------------------

    // Package-private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // openSurface..................................................................................
    /**
     * Create surface
     * @param activity bla
     */
    void openSurface(@NonNull Activity activity) {
        Log.e(Thread.currentThread().getName(), "TextureViewListener openSurface");
        mTextureView = new TextureView(activity);
        mTextureView.setSurfaceTextureListener(this);

        // program continues with onSurfaceTextureAvailable listener below
        activity.setContentView(mTextureView);
    }

    //**********************************************************************************************
    // Overriding Class Methods
    //-------------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onSurfaceTextureAvailable....................................................................
    /**
     * TODO: description, comments and logging
     * @param texture bla
     * @param width bla
     * @param height bla
     */
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture texture, int width, int height) {
        Log.e(Thread.currentThread().getName(), "TextureViewListener onSurfaceTextureAvailable");
        mSurfaceWidth  = width;
        mSurfaceHeight = height;
        mSurface = new Surface(texture);
        SurfaceManager.surfaceHasOpened(mSurface, TextureViewListener.class);
    }

    // onSurfaceTextureDestroyed....................................................................
    /**
     * TODO: description, comments and logging
     * @param texture bla
     * @return bla
     */
    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture texture) {
        Log.e(Thread.currentThread().getName(), "TextureViewListener onSurfaceTextureDestroyed");
        return false;
    }

    // onSurfaceTextureSizeChanged..................................................................
    /**
     * TODO: description, comments and logging
     * @param texture bla
     * @param width bla
     * @param height bla
     */
    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture texture, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    // onSurfaceTextureUpdated......................................................................
    /**
     * TODO: description, comments and logging
     * @param texture bla
     */
    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture texture) {
        //Log.e(Thread.currentThread().getName(), "TextureViewListener onSurfaceTextureUpdated");
    }

}
