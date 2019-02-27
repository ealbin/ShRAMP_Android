package sci.crayfis.shramp.surfaces;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.CaptureOverseer;
import sci.crayfis.shramp.logging.ShrampLogger;

@TargetApi(21)
public class SurfaceManager {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Single instance of SurfaceManager
    private static final SurfaceManager mInstance = new SurfaceManager();

    // List of surfaces being used as output
    private static final List<Surface> mSurfaces = new ArrayList<>();

    // Flags to signify a surface is ready
    private static Boolean mTextureViewReady = false;
    // other surface
    // other surface

    // TextureView Listener (inner class)
    private final TextureViewListener mTextureViewListener = new TextureViewListener();
    // other surface inner class
    // other surface inner class

    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

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
    public static SurfaceManager getInstance() {return SurfaceManager.mInstance;}

    //----------------------------------------------------------------------------------------------

    /**
     * Set up surfaces for output operation
     * @param activity source context
     */
    public void openSurfaces(Activity activity) {
        SurfaceManager.mLogger.log("Opening TextureView");
        this.mTextureViewListener.openSurface(activity);
        SurfaceManager.mLogger.log("Would be opening other surfaces now");
        // other surface
        // other surface
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Called whenever a surface is initialized, once all surfaces check in, notify CaptureOverseer
     */
    private void surfaceReady() {
        SurfaceManager.mLogger.log("A surface is ready");
        boolean isReady = mTextureViewReady; // && mBlankViewReady && ..
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

    //----------------------------------------------------------------------------------------------

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
            this.mTextureView = new TextureView(activity);
            this.mTextureView.setSurfaceTextureListener(this);
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
            SurfaceManager.mInstance.surfaceReady();
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

    // private final class SomethingListener ...
}
