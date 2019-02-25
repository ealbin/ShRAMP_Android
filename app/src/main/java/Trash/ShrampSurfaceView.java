package Trash;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.TextureView;

public class ShrampSurfaceView implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;

    ShrampSurfaceView(@NonNull Activity activity, int id) {
        mTextureView = (TextureView) activity.findViewById(id);
        assert mTextureView != null;

        mTextureView.getSurfaceTexture();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int hight) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
