package Camera2;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.TreeMap;

@TargetApi(21) // Lollipop
public class ShrampCameraManager {

    private static ShrampCameraManager mSelfie;
    private enum Select { FRONT, BACK, EXTERNAL; }

    // set in getNewCameraManager
    private static CameraManager mCameraManager;

    private static TreeMap<Select, String>                  mCameraIds;
    private static TreeMap<Select, CameraCharacteristics>   mCameraCharacteristics;
    private static TreeMap<Select, ShrampCameraDevice>      mShrampCameraDevices;

    // access mCameraManager
    ShrampCameraManager(@NonNull Context context) {

        if (mSelfie != null) {
            return;
        }

        mCameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);

        if (mCameraManager == null) {
            // TODO report anomally
            return;
        }

        mCameraIds               = new TreeMap<Select, String>();
        mCameraCharacteristics   = new TreeMap<Select, CameraCharacteristics>();
        mShrampCameraDevices     = new TreeMap<Select, ShrampCameraDevice>();

        String[] cameraIds = null;
        try {
            cameraIds = mCameraManager.getCameraIdList();
        }
        catch (CameraAccessException e) {
            // TODO ERROR
            return;
        }

        for (String id : cameraIds) {
            CameraCharacteristics   cameraCharacteristics;
            Integer                 lensFacing;

            try {
                cameraCharacteristics  = mCameraManager.getCameraCharacteristics(id);
                lensFacing             = cameraCharacteristics.get(
                        CameraCharacteristics.LENS_FACING);

                if (lensFacing == null) {
                    // TODO report anomaly
                    continue;
                }

                Select cameraKey = null;
                switch (lensFacing) {
                    case (CameraCharacteristics.LENS_FACING_FRONT) : {
                        cameraKey = Select.FRONT;
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_BACK) : {
                        cameraKey = Select.BACK;
                        break;
                    }
                    case (CameraCharacteristics.LENS_FACING_EXTERNAL) : {
                        cameraKey = Select.EXTERNAL;
                        break;
                    }
                }
                if (cameraKey == null) {
                    // TODO report anomally
                    continue;
                }

                mCameraIds.put(cameraKey, id);
                mCameraCharacteristics.put(cameraKey, cameraCharacteristics);
            }
            catch (CameraAccessException e) {
                // TODO ERROR
                return;
            }
        }
    }

    public static void openFrontCamera() {
        if (!mShrampCameraDevices.containsKey(Select.FRONT)) {
            // TODO no front camera
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.FRONT);
        mShrampCameraDevices.put(Select.FRONT,
                new ShrampCameraDevice(characteristics, "shramp_front_cam"));
        openCamera(Select.FRONT);
    }

    public static void openBackCamera() {
        if (!mShrampCameraDevices.containsKey(Select.BACK)) {
            // TODO no back camera
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.BACK);
        mShrampCameraDevices.put(Select.BACK,
                new ShrampCameraDevice(characteristics, "shramp_back_cam"));
        openCamera(Select.BACK);
    }

    public static void openExternalCamera(Select cameraKey) {
        if (!mShrampCameraDevices.containsKey(Select.EXTERNAL)) {
            // TODO no external camera
            return;
        }
        CameraCharacteristics characteristics = mCameraCharacteristics.get(Select.EXTERNAL);
        mShrampCameraDevices.put(Select.EXTERNAL,
                new ShrampCameraDevice(characteristics, "shramp_external_cam"));
        openCamera(Select.EXTERNAL);
    }

    private static void openCamera(Select cameraKey) {
        String                     cameraId            = mCameraIds.get(cameraKey);
        ShrampCameraDevice         shrampCameraDevice  = mShrampCameraDevices.get(cameraKey);
        assert                     shrampCameraDevice != null;
        CameraDevice.StateCallback stateCallback       = shrampCameraDevice.getStateCallback();
        Handler                    handler             = shrampCameraDevice.getHandler();

        try {
            assert cameraId != null;
            mCameraManager.openCamera(cameraId, stateCallback, handler);
        }
        catch (SecurityException e ) {
            // TODO user hasn't granted permissions
        }
        catch (CameraAccessException e ) {
            // TODO ERROR
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested static abstract AvailabilityCallback class /////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private CameraManager.AvailabilityCallback mAvailabilityCallback
            = new CameraManager.AvailabilityCallback() {

        // set in onCameraAvailable, onCameraUnavailable
        private String mCameraId;

        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            super.onCameraAvailable(cameraId);

            mCameraId = cameraId;
        }

        @Override
        public void onCameraUnavailable(@NonNull String cameraId) {
            super.onCameraUnavailable(cameraId);

            mCameraId = cameraId;
        }
    };

    // access mAvailabilityCallback
    public CameraManager.AvailabilityCallback getmAvailabilityCallback() {
        return mAvailabilityCallback;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested static abstract TorchCallback class ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // (API 23)
    /*
    private CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {

        // set in onTorchModeUnavailable, onTorchModeChanged
        private String  mCameraId;

        // set in onTorchModeChanged
        private boolean mEnabled;

        @Override
        public void onTorchModeUnavailable(@NonNull String cameraId) {
            super.onTorchModeUnavailable(cameraId);

            mCameraId = cameraId;
        }

        @Override
        public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);

            mCameraId = cameraId;
            mEnabled  = enabled;
        }
    };

    // access mTorchCallback
    public CameraManager.TorchCallback getmTorchCallback() {
        return mTorchCallback;
    }
    */
}
