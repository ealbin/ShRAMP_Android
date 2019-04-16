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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

/**
 * A TextureView is useful for displaying text or a live camera feed.
 * The purpose of this class is to handle the creation and change of a TextureView surface.
 * TextureView implicitly runs on the main thread.
 */
@TargetApi(21)
final class TextureViewListener implements TextureView.SurfaceTextureListener {

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mSurface.....................................................................................
    // Active TextureView surface
    private Surface mSurface;

    // mSurfaceHeight......................................................................................
    // Height dimension in pixels
    private Integer mSurfaceHeight;

    // mSurfaceWidth.......................................................................................
    // Width dimension in pixels
    private Integer mSurfaceWidth;

    // mTextureView.................................................................................
    // Active TextureView object (good for displaying text or live camera images)
    private TextureView mTextureView;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TextureViewListener..........................................................................
    /**
     * Nothing special, just make it
     */
    TextureViewListener() {
        super();
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // openSurface..................................................................................
    /**
     * Build/open a new TextureView surface
     * @param activity Activity in control of the app
     */
    void openSurface(@NonNull Activity activity) {
        mTextureView = new TextureView(activity);
        mTextureView.setSurfaceTextureListener(this);

        // execution continues with onSurfaceTextureAvailable() listener below
        activity.setContentView(mTextureView);
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onSurfaceTextureAvailable....................................................................
    /**
     * Called once the system asynchronously configures a new TextureView surface.
     * @param texture Reference to the new surface
     * @param width Width (in pixels) of the surface
     * @param height Height (in pixels) of the surface
     */
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture texture, int width, int height) {
        mSurfaceWidth  = width;
        mSurfaceHeight = height;
        mSurface = new Surface(texture);

        // return execution control to SurfaceController
        SurfaceController.surfaceHasOpened(mSurface, TextureViewListener.class);
    }

    // onSurfaceTextureUpdated......................................................................
    /**
     * Called by the system every time something is written to the surface, so it's best to
     * keep this minimal if anything needs to be done.
     * @param texture Reference to the TextureView surface
     */
    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture texture) {
        // do nothing
    }

    // onSurfaceTextureDestroyed....................................................................
    /**
     * Called by the system when the surface is destroyed
     * @param texture Reference to the TextureView surface
     * @return If returns true, no rendering should happen inside the surface texture after this
     * method is invoked. If returns false, the client needs to call SurfaceTexture.release().
     * Most applications should return true.
     */
    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture texture) {
        return true;
    }

    // onSurfaceTextureSizeChanged..................................................................
    /**
     * Called by the system when the surface dimensions are changed
     * @param texture Reference to the TextureView surface
     * @param width New surface width (in pixels)
     * @param height New surface height (in pixels)
     */
    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture texture, int width, int height) {
        Log.e(Thread.currentThread().getName(), "TextureViewListener size has changed to: "
        + Integer.toString(width) + " x " + Integer.toString(height) + " pixels");
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

}