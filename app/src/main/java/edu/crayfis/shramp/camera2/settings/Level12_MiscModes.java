package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level12_MiscModes extends Level11_Tonemap {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer mDistortionCorrectionMode;
    private   String  mDistortionCorrectionModeName;

    protected Integer mEdgeMode;
    private   String  mEdgeModeName;

    protected Integer mFlashMode;
    private   String  mFlashModeName;

    protected Integer mHotPixelMode;
    private   String  mHotPixelModeName;

    protected Integer mNoiseReductionMode;
    private   String  mNoiseReductionModeName;

    protected Integer mShadingMode;
    private   String  mShadingModeName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level12_MiscModes(@NonNull CameraCharacteristics characteristics,
                                @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setDistortionCorrectionMode();
        setEdgeMode();
        setFlashMode();
        setHotPixelMode();
        setNoiseReductionMode();
        setShadingMode();
    }

    //----------------------------------------------------------------------------------------------

    /*
     * Documentation provided by:
     * https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
     * https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
     */

    /**
     *
     */
    private void setDistortionCorrectionMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            mDistortionCorrectionMode     = null;
            mDistortionCorrectionModeName = "Not supported";
            return;
        }
        CaptureRequest.Key key = CaptureRequest.DISTORTION_CORRECTION_MODE;
        /*
         * Added in API 28
         *
         * Mode of operation for the lens distortion correction block.
         *
         * The lens distortion correction block attempts to improve image quality by fixing radial,
         * tangential, or other geometric aberrations in the camera device's optics. If available,
         * the android.lens.distortion field documents the lens's distortion parameters.
         *
         * OFF means no distortion correction is done.
         *
         * FAST/HIGH_QUALITY both mean camera device determined distortion correction will be
         * applied. HIGH_QUALITY mode indicates that the camera device will use the highest-quality
         * correction algorithms, even if it slows down capture rate. FAST means the camera device
         * will not slow down capture rate when applying correction. FAST may be the same as OFF if
         * any correction at all would slow down capture rate. Every output stream will have a
         * similar amount of enhancement applied.
         *
         * The correction only applies to processed outputs such as YUV, JPEG, or DEPTH16; it is not
         * applied to any RAW output.
         *
         * This control will be on by default on devices that support this control. Applications
         * disabling distortion correction need to pay extra attention with the coordinate system of
         * metering regions, crop region, and face rectangles. When distortion correction is OFF,
         * metadata coordinates follow the coordinate system of
         * android.sensor.info.preCorrectionActiveArraySize. When distortion is not OFF, metadata
         * coordinates follow the coordinate system of android.sensor.info.activeArraySize. The
         * camera device will map these metadata fields to match the corrected image produced by the
         * camera device, for both capture requests and results. However, this mapping is not very
         * precise, since rectangles do not generally map to rectangles when corrected. Only linear
         * scaling between the active array and precorrection active array coordinates is performed.
         * Applications that require precise correction of metadata need to undo that linear
         * scaling, and apply a more complete correction that takes into the account the app's own
         * requirements.
         *
         * The full list of metadata that is affected in this way by distortion correction is:
         *
         * android.control.afRegions
         * android.control.aeRegions
         * android.control.awbRegions
         * android.scaler.cropRegion
         * android.statistics.faces
         *
         * Available values for this device:
         * android.distortionCorrection.availableModes
         *
         * Optional - This value may be null on some devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mDistortionCorrectionMode     = null;
            mDistortionCorrectionModeName = "Not supported";
            return;
        }

        mDistortionCorrectionMode     = CameraMetadata.DISTORTION_CORRECTION_MODE_OFF;
        mDistortionCorrectionModeName = "Off";
        /*
         * Added in API 28
         *
         * No distortion correction is applied.
         */

        super.mCaptureRequestBuilder.set(key, mDistortionCorrectionMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setEdgeMode() {
        CaptureRequest.Key key = CaptureRequest.EDGE_MODE;
        /*
         * Added in API 21
         *
         * Operation mode for edge enhancement.
         *
         * Edge enhancement improves sharpness and details in the captured image. OFF means no
         * enhancement will be applied by the camera device.
         *
         * FAST/HIGH_QUALITY both mean camera device determined enhancement will be applied.
         * HIGH_QUALITY mode indicates that the camera device will use the highest-quality
         * enhancement algorithms, even if it slows down capture rate. FAST means the camera device
         * will not slow down capture rate when applying edge enhancement. FAST may be the same as
         * OFF if edge enhancement will slow down capture rate. Every output stream will have a
         * similar amount of enhancement applied.
         *
         * ZERO_SHUTTER_LAG is meant to be used by applications that maintain a continuous circular
         * buffer of high-resolution images during preview and reprocess image(s) from that buffer
         * into a final capture when triggered by the user. In this mode, the camera device applies
         * edge enhancement to low-resolution streams (below maximum recording resolution) to
         * maximize preview quality, but does not apply edge enhancement to high-resolution streams,
         * since those will be reprocessed later if necessary.
         *
         * For YUV_REPROCESSING, these FAST/HIGH_QUALITY modes both mean that the camera device will
         * apply FAST/HIGH_QUALITY YUV-domain edge enhancement, respectively. The camera device may
         * adjust its internal edge enhancement parameters for best image quality based on the
         * android.reprocess.effectiveExposureFactor, if it is set.
         *
         * Available values for this device:
         * android.edge.availableEdgeModes
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mEdgeMode     = null;
            mEdgeModeName = "Not supported";
            return;
        }

        mEdgeMode     = CameraMetadata.EDGE_MODE_OFF;
        mEdgeModeName = "Off";
        /*
         * Added in API 21
         *
         * No edge enhancement is applied.
         */

        super.mCaptureRequestBuilder.set(key, mEdgeMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setFlashMode() {
        CaptureRequest.Key key = CaptureRequest.FLASH_MODE;
        /*
         * Added in API 21
         *
         * The desired mode for for the camera device's flash control.
         *
         * This control is only effective when flash unit is available
         * (android.flash.info.available == true).
         *
         * When this control is used, the android.control.aeMode must be set to ON or OFF.
         * Otherwise, the camera device auto-exposure related flash control (ON_AUTO_FLASH,
         * ON_ALWAYS_FLASH, or ON_AUTO_FLASH_REDEYE) will override this control.
         *
         * When set to OFF, the camera device will not fire flash for this capture.
         *
         * When set to SINGLE, the camera device will fire flash regardless of the camera device's
         * auto-exposure routine's result. When used in still capture case, this control should be
         * used along with auto-exposure (AE) precapture metering sequence
         * (android.control.aePrecaptureTrigger), otherwise, the image may be incorrectly exposed.
         *
         * When set to TORCH, the flash will be on continuously. This mode can be used for use cases
         * such as preview, auto-focus assist, still capture, or video recording.
         *
         * The flash status will be reported by android.flash.state in the capture result metadata.
         *
         * This key is available on all devices.
         */
        boolean hasFlashUnit = mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        if (!super.mRequestKeys.contains(key) || !hasFlashUnit) {
            mFlashMode     = null;
            mFlashModeName = "Not supported";
            return;
        }

        mFlashMode     = CameraMetadata.FLASH_MODE_OFF;
        mFlashModeName = "Off";
        /*
         * Added in API 21
         *
         * Do not fire the flash for this capture.
         */

        super.mCaptureRequestBuilder.set(key, mFlashMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setHotPixelMode() {
        CaptureRequest.Key key = CaptureRequest.HOT_PIXEL_MODE;
        int modeOff  = CameraMetadata.HOT_PIXEL_MODE_OFF;
        int modeFast = CameraMetadata.HOT_PIXEL_MODE_FAST;
        /*
         * Added in API 21
         *
         * Operational mode for hot pixel correction.
         *
         * Hotpixel correction interpolates out, or otherwise removes, pixels that do not accurately
         * measure the incoming light (i.e. pixels that are stuck at an arbitrary value or are
         * oversensitive).
         *
         * Available values for this device:
         * android.hotPixel.availableHotPixelModes
         *
         * Optional - This value may be null on some devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mHotPixelMode     = null;
            mHotPixelModeName = "Not supported";
            return;
        }

        // Default
        mHotPixelMode     = modeFast;
        mHotPixelModeName = "Fast";
        /*
         * Added in API 21
         *
         * Hot pixel correction is applied, without reducing frame rate relative to sensor raw
         * output.
         *
         * The hotpixel map may be returned in android.statistics.hotPixelMap.
         */

        List<Integer> modes = super.getAvailable(CameraCharacteristics.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES);
        if (modes.contains(modeOff)) {
            mHotPixelMode     = modeOff;
            mHotPixelModeName = "Off";
            /*
             * Added in API 21
             *
             * No hot pixel correction is applied.
             *
             * The frame rate must not be reduced relative to sensor raw output for this option.
             *
             * The hotpixel map may be returned in android.statistics.hotPixelMap.
             */
        }

        super.mCaptureRequestBuilder.set(key, mHotPixelMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setNoiseReductionMode() {
        CaptureRequest.Key key = CaptureRequest.NOISE_REDUCTION_MODE;
        int modeOff  = CameraMetadata.NOISE_REDUCTION_MODE_OFF;
        int modeFast = CameraMetadata.NOISE_REDUCTION_MODE_FAST;
        /*
         * Added in API 21
         *
         * Mode of operation for the noise reduction algorithm.
         *
         * The noise reduction algorithm attempts to improve image quality by removing excessive
         * noise added by the capture process, especially in dark conditions.
         *
         * OFF means no noise reduction will be applied by the camera device, for both raw and YUV
         * domain.
         *
         * MINIMAL means that only sensor raw domain basic noise reduction is enabled ,to remove
         * demosaicing or other processing artifacts. For YUV_REPROCESSING, MINIMAL is same as OFF.
         * This mode is optional, may not be support by all devices. The application should check
         * android.noiseReduction.availableNoiseReductionModes before using it.
         *
         * FAST/HIGH_QUALITY both mean camera device determined noise filtering will be applied.
         * HIGH_QUALITY mode indicates that the camera device will use the highest-quality noise
         * filtering algorithms, even if it slows down capture rate. FAST means the camera device
         * will not slow down capture rate when applying noise filtering. FAST may be the same as
         * MINIMAL if MINIMAL is listed, or the same as OFF if any noise filtering will slow down
         * capture rate. Every output stream will have a similar amount of enhancement applied.
         *
         * ZERO_SHUTTER_LAG is meant to be used by applications that maintain a continuous circular
         * buffer of high-resolution images during preview and reprocess image(s) from that buffer
         * into a final capture when triggered by the user. In this mode, the camera device applies
         * noise reduction to low-resolution streams (below maximum recording resolution) to
         * maximize preview quality, but does not apply noise reduction to high-resolution streams,
         * since those will be reprocessed later if necessary.
         *
         * For YUV_REPROCESSING, these FAST/HIGH_QUALITY modes both mean that the camera device will
         * apply FAST/HIGH_QUALITY YUV domain noise reduction, respectively. The camera device may
         * adjust the noise reduction parameters for best image quality based on the
         * android.reprocess.effectiveExposureFactor if it is set.
         *
         * Available values for this device:
         * android.noiseReduction.availableNoiseReductionModes
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mNoiseReductionMode     = null;
            mNoiseReductionModeName = "Not supported";
            return;
        }

        // Default
        mNoiseReductionMode     = modeFast;
        mNoiseReductionModeName = "Fast";
        /*
         * Added in API 21
         *
         * Noise reduction is applied without reducing frame rate relative to sensor output. It may
         * be the same as OFF if noise reduction will reduce frame rate relative to sensor.
         */

        List<Integer> modes = super.getAvailable(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
        if (modes.contains(modeOff)) {
            mNoiseReductionMode     = modeOff;
            mNoiseReductionModeName = "Off";
            /*
             * Added in API 21
             *
             * No noise reduction is applied.
             */
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int modeMin = CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL;
            if (modes.contains(modeMin)) {
                mNoiseReductionMode     = modeMin;
                mNoiseReductionModeName = "Minimal";
                /*
                 * Added in API 23
                 *
                 * MINIMAL noise reduction is applied without reducing frame rate relative to sensor
                 * output.
                 */
            }
        }

        super.mCaptureRequestBuilder.set(key, mNoiseReductionMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setShadingMode() {
        CaptureRequest.Key key = CaptureRequest.SHADING_MODE;
        /*
         * Added in API 21
         *
         * Quality of lens shading correction applied to the image data.
         *
         * When set to OFF mode, no lens shading correction will be applied by the camera device,
         * and an identity lens shading map data will be provided if
         * android.statistics.lensShadingMapMode == ON. For example, for lens shading map with size
         * of [ 4, 3 ], the output android.statistics.lensShadingCorrectionMap for this case will be
         * an identity map shown below:
         *
         *      [ 1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0,
         *        1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0,
         *        1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0,
         *        1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0,
         *        1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0,
         *        1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0 ]
         *
         * When set to other modes, lens shading correction will be applied by the camera device.
         * Applications can request lens shading map data by setting
         * android.statistics.lensShadingMapMode to ON, and then the camera device will provide lens
         * shading map data in android.statistics.lensShadingCorrectionMap; the returned shading map
         * data will be the one applied by the camera device for this capture request.
         *
         * The shading map data may depend on the auto-exposure (AE) and AWB statistics, therefore
         * the reliability of the map data may be affected by the AE and AWB algorithms.
         * When AE and AWB are in AUTO modes(android.control.aeMode != OFF and
         * android.control.awbMode != OFF), to get best results, it is recommended that the
         * applications wait for the AE and AWB to be converged before using the returned shading
         * map data.
         *
         * Available values for this device:
         * android.shading.availableModes
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mShadingMode     = null;
            mShadingModeName = "Not supported";
            return;
        }

        mShadingMode     = CameraMetadata.SHADING_MODE_OFF;
        mShadingModeName = "Off";
        /*
         * Added in API 21
         *
         * No lens shading correction is applied.
         */

        super.mCaptureRequestBuilder.set(key, mShadingMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 12 (Miscellaneous modes)\n";
        string += "CaptureRequest.DISTORTION_CORRECTION_MODE: " + mDistortionCorrectionModeName + "\n";
        string += "CaptureRequest.EDGE_MODE:                  " + mEdgeModeName                 + "\n";
        string += "CaptureRequest.FLASH_MODE:                 " + mFlashModeName                + "\n";
        string += "CaptureRequest.HOT_PIXEL_MODE:             " + mHotPixelModeName             + "\n";
        string += "CaptureRequest.NOISE_REDUCTION_MODE:       " + mNoiseReductionModeName       + "\n";
        string += "CaptureReuqest.SHADING_MODE:               " + mShadingModeName              + "\n";

        stringList.add(string);
        return stringList;
    }

}
