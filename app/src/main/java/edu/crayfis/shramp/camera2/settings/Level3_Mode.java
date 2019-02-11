package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TargetApi(21)
abstract class Level3_Mode extends Level2_Template {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected List<CaptureRequest.Key<?>> mRequestKeys;

    protected int    mControlMode;
    private   String mControlModeName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level3_Mode(@NonNull CameraCharacteristics characteristics,
                          @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        mRequestKeys = super.mCameraCharacteristics.getAvailableCaptureRequestKeys();
        setControlMode();
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @param key
     * @return
     */
    protected List<Integer> getAvailable(CameraCharacteristics.Key key) {
        int[] available = (int[])super.mCameraCharacteristics.get(key);
        assert available != null;

        List<Integer> list = new ArrayList<>();
        for (int item : available) {
            list.add(item);
        }

        return list;
    }

    //----------------------------------------------------------------------------------------------

    // Documentation provided by:
    // https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
    // https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
    // https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html

    /**
     *
     */
    private void setControlMode() {
        CaptureRequest.Key key = CaptureRequest.CONTROL_MODE;
        int modeOff  = CameraMetadata.CONTROL_MODE_OFF;
        int modeAuto = CameraMetadata.CONTROL_MODE_AUTO;
        /*
         * Added in API 21
         *
         * Overall mode of 3A (auto-exposure, auto-white-balance, auto-focus) control routines.
         *
         * This is a top-level 3A control switch. When set to OFF, all 3A control by the camera
         * device is disabled. The application must set the fields for capture parameters itself.
         *
         * When set to AUTO, the individual algorithm controls in android.control.* are in effect,
         * such as android.control.afMode.
         *
         * When set to USE_SCENE_MODE, the individual controls in android.control.* are mostly
         * disabled, and the camera device implements one of the scene mode settings
         * (such as ACTION, SUNSET, or PARTY) as it wishes. The camera device scene mode 3A
         * settings are provided by capture results.
         *
         * When set to OFF_KEEP_STATE, it is similar to OFF mode, the only difference is that this
         * frame will not be used by camera device background 3A statistics update, as if this
         * frame is never captured. This mode can be used in the scenario where the application
         * doesn't want a 3A manual control capture to affect the subsequent auto 3A capture results.
         *
         * This key is available on all devices.
         */

        // Default
        mControlMode     = modeAuto;
        mControlModeName = "Auto";
        /*
         * Added in API 21
         *
         * Use settings for each individual 3A routine.
         *
         * Manual control of capture parameters is disabled. All controls in
         * android.control.* besides sceneMode take effect.
         */

        if (super.mHardwareLevel == HardwareLevel.LIMITED
                || super.mHardwareLevel == HardwareLevel.FULL ) {
            mControlMode     = modeOff;
            mControlModeName = "Off";
            /*
             * Added in API 21
             *
             * Full application control of pipeline.
             *
             * All control by the device's metering and focusing (3A) routines is disabled, and no
             * other settings in android.control.* have any effect, except that
             * android.control.captureIntent may be used by the camera device to select
             * post-processing values for processing blocks that do not allow for manual control,
             * or are not exposed by the camera API.
             *
             * However, the camera device's 3A routines may continue to collect statistics and
             * update their internal state so that when control is switched to AUTO mode,
             * good control values can be immediately applied.
             *
             * LIMITED and FULL devices will always support OFF, AUTO modes.
             */
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /*
             * Added in API 23
             *
             * List of control modes for android.control.mode that are supported by this
             * camera device.
             *
             * This list contains control modes that can be set for the camera device.
             * LEGACY mode devices will always support AUTO mode.
             * LIMITED and FULL devices will always support OFF, AUTO modes.
             */
            List<Integer> modes = getAvailable(CameraCharacteristics.CONTROL_AVAILABLE_MODES);
            if (modes.contains(modeOff)) {
                mControlMode     = modeOff;
                mControlModeName = "Off";
            }
        }
        super.mCaptureRequestBuilder.set(key, mControlMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public String toString() {
        String string = super.toString() + "\n";

        string = string.concat("CaptureRequest.CONTROL_MODE: " + mControlModeName + "\n");

        return string;
    }

}
