package Trash;

import android.graphics.SurfaceTexture;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import org.w3c.dom.Text;

import sci.crayfis.shramp.MaineShRAMP;

public class CaptureProcessing {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "CaptureProcessing";
    private final static String DIVIDER = "---------------------------------------------";


    MaineShRAMP mMaine_shramp;

    public SurfaceTexture mSurface_texture;
    public TextureView mTexture_view;
    public TextureView.SurfaceTextureListener mSurface_listener;
    public Surface mSurface;

    public RenderScript mRender_script;
    public Allocation mAllocation;

    CaptureProcessing(MaineShRAMP main_shramp, Size image_size) {
        // debug Logcat string
        final String LOCAL_TAG = TAG.concat(".CaptureProcessing(MaineShRAMP, Size)");
        Log.e(LOCAL_TAG,  DIVIDER);


        mMaine_shramp = main_shramp;

        //RenderScript mRender_script = RenderScript.create(mMaine_shramp);
        //Allocation mAllocation = Allocation.

        Log.e(LOCAL_TAG, "Surface stuff");
        //mTexture_view = (TextureView) mMaine_shramp.findViewById(R.id.preview_view);
        assert mTexture_view != null;
        mTexture_view.setSurfaceTextureListener(mSurface_listener);
        Log.e(LOCAL_TAG, mTexture_view.toString());

        //mSurface_texture = mTexture_view.getSurfaceTexture();
        //assert mSurface_texture != null;
        Log.e(LOCAL_TAG, mTexture_view.getSurfaceTexture().toString());
        //mSurface_texture.setDefaultBufferSize(image_size.getWidth(), image_size.getWidth());
        //mSurface = new Surface(mSurface_texture);

        //mSurface_texture = mMaine_shramp.findViewById(R.id.preview_view);

        mSurface_listener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        };
    }


}
