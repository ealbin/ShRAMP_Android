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
    public static SurfaceManager getInstance() {return mInstance;}

    //----------------------------------------------------------------------------------------------

    /**
     * Set up surfaces for output operation
     * @param activity source context
     */
    public void openSurfaces(Activity activity) {
        mLogger.log("Opening TextureView");
        mTextureViewListener.openSurface(activity);
        mLogger.log("Would be opening other surfaces now");
        // other surface
        // other surface
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Called whenever a surface is initialized, once all surfaces check in, notify CaptureOverseer
     */
    private void surfaceReady() {
        mLogger.log("A surface is ready");
        boolean isReady = mTextureViewReady; // && mBlankViewReady && ..
        if (isReady) {
            mLogger.log("All surfaces are ready");
            CaptureOverseer.surfacesReady(mSurfaces);
        }
        else {
            mLogger.log("Not all surfaces are ready, continuing to wait");
            // otherwise wait for the remaining surfaces to check in..
        }
        mLogger.log("return;");
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Handles everything to do with TextureView surface
     */
    private final class TextureViewListener implements TextureView.SurfaceTextureListener{
        private TextureView mmTextureView;

        private long mmUpdateCount = 0;
        private final long UPDATE_LIMIT = 5;

        /**
         * Create surface
         * @param activity source context
         */
        private void openSurface(Activity activity) {
            mLogger.log("TextureView is opening");
            mmTextureView = new TextureView(activity);
            mmTextureView.setSurfaceTextureListener(this);
            // program continues with onSurfaceTextureAvailable listener below
            activity.setContentView(mmTextureView);
            mLogger.log("return;");
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
            mLogger.log("TextureView is open");
            // TODO what are arg1 and arg2?
            mSurfaces.add(new Surface(arg0));
            mTextureViewReady = true;
            surfaceReady();
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
            // TODO (maybe no action?)
            mmUpdateCount += 1;
            if (mmUpdateCount <= UPDATE_LIMIT) {
                mLogger.log("TextureView has been updated, count = " + Long.toString(mmUpdateCount));
                if (mmUpdateCount == UPDATE_LIMIT) {
                    mLogger.log("Silencing further updates");
                }
                mLogger.log("return;");
            }
        }
    }

    // private final class SomethingListener ...
}
