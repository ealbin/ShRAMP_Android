package recycle_bin.camera2.settings;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.List;

@TargetApi(21)
abstract class Level17_Misc extends Level16_Statistics {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Boolean mBlackLevelLock;
    private   String  mBlackLevelLockName;

    protected Float   mReprocessEffectiveExposureFactor;
    private   String  mReprocessEffectiveExposureFactorName;

    protected Rect    mScalarCropRegion;
    private   String  mScalarCropRegionName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level17_Misc(@NonNull CameraCharacteristics characteristics,
                           @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setBlackLevelLock();
        setReprocessEffectiveExposureFactor();
        setScalarCropRegion();
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
    private void setBlackLevelLock() {
        CaptureRequest.Key key = CaptureRequest.BLACK_LEVEL_LOCK;
        /*
         * Added in API 21
         *
         * Whether black-level compensation is locked to its current values, or is free to vary.
         *
         * When set to true (ON), the values used for black-level compensation will not change until
         * the lock is set to false (OFF).
         *
         * Since changes to certain capture parameters (such as exposure time) may require resetting
         * of black level compensation, the camera device must report whether setting the black
         * level lock was successful in the output result metadata.
         *
         * The camera device will maintain the lock to the extent possible, only overriding the lock
         * to OFF when changes to other request parameters require a black level recalculation or
         * reset.
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mBlackLevelLock     = null;
            mBlackLevelLockName = "Not supported";
            return;
        }

        // Default
        mBlackLevelLock     = true;
        mBlackLevelLockName = "On";

        super.mCaptureRequestBuilder.set(key, mBlackLevelLock);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setReprocessEffectiveExposureFactor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mReprocessEffectiveExposureFactor     = null;
            mReprocessEffectiveExposureFactorName = "Not supported";
            return;
        }
        CaptureRequest.Key key = CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR;
        /*
         * Added in API 23
         *
         * The amount of exposure time increase factor applied to the original output frame by the
         * application processing before sending for reprocessing.
         *
         * This is optional, and will be supported if the camera device supports YUV_REPROCESSING
         * capability (android.request.availableCapabilities contains YUV_REPROCESSING).
         *
         * For some YUV reprocessing use cases, the application may choose to filter the original
         * output frames to effectively reduce the noise to the same level as a frame that was
         * captured with longer exposure time. To be more specific, assuming the original captured
         * images were captured with a sensitivity of S and an exposure time of T, the model in the
         * camera device is that the amount of noise in the image would be approximately what would
         * be expected if the original capture parameters had been a sensitivity of
         * S/effectiveExposureFactor and an exposure time of T*effectiveExposureFactor, rather than
         * S and T respectively. If the captured images were processed by the application before
         * being sent for reprocessing, then the application may have used image processing
         * algorithms and/or multi-frame image fusion to reduce the noise in the
         * application-processed images (input images). By using the effectiveExposureFactor
         * control, the application can communicate to the camera device the actual noise level
         * improvement in the application-processed image. With this information, the camera device
         * can select appropriate noise reduction and edge enhancement parameters to avoid excessive
         * noise reduction (android.noiseReduction.mode) and insufficient edge enhancement
         * (android.edge.mode) being applied to the reprocessed frames.
         *
         * For example, for multi-frame image fusion use case, the application may fuse multiple
         * output frames together to a final frame for reprocessing. When N image are fused into 1
         * image for reprocessing, the exposure time increase factor could be up to square root of
         * N (based on a simple photon shot noise model). The camera device will adjust the
         * reprocessing noise reduction and edge enhancement parameters accordingly to produce the
         * best quality images.
         *
         * This is relative factor, 1.0 indicates the application hasn't processed the input buffer
         * in a way that affects its effective exposure time.
         *
         * This control is only effective for YUV reprocessing capture request. For noise reduction
         * reprocessing, it is only effective when android.noiseReduction.mode != OFF. Similarly,
         * for edge enhancement reprocessing, it is only effective when android.edge.mode != OFF.
         *
         * Units: Relative exposure time increase factor.
         *
         * Range of valid values:
         * >= 1.0
         *
         * Optional - This value may be null on some devices.
         *
         * Limited capability - Present on all camera devices that report being at least
         * HARDWARE_LEVEL_LIMITED devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mReprocessEffectiveExposureFactor     = null;
            mReprocessEffectiveExposureFactorName = "Not supported";
            return;
        }

        // Default
        mReprocessEffectiveExposureFactor = 1.f;
        DecimalFormat df = new DecimalFormat("#.##");
        mReprocessEffectiveExposureFactorName = df.format(this.mReprocessEffectiveExposureFactor);

        super.mCaptureRequestBuilder.set(key, mReprocessEffectiveExposureFactor);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setScalarCropRegion() {
        CaptureRequest.Key key = CaptureRequest.SCALER_CROP_REGION;
        mScalarCropRegion      = null;
        mScalarCropRegionName  = "Not applicable";
        /*
         * Added in API 21
         *
         * The desired region of the sensor to read out for this capture.
         *
         * This control can be used to implement digital zoom.
         *
         * For devices not supporting android.distortionCorrection.mode control, the coordinate
         * system always follows that of android.sensor.info.activeArraySize, with (0, 0) being the
         * top-left pixel of the active array.
         *
         * For devices supporting android.distortionCorrection.mode control, the coordinate system
         * depends on the mode being set. When the distortion correction mode is OFF, the coordinate
         * system follows android.sensor.info.preCorrectionActiveArraySize, with (0, 0) being the
         * top-left pixel of the pre-correction active array. When the distortion correction mode is
         * not OFF, the coordinate system follows android.sensor.info.activeArraySize, with (0, 0)
         * being the top-left pixel of the active array.
         *
         * Output streams use this rectangle to produce their output, cropping to a smaller region
         * if necessary to maintain the stream's aspect ratio, then scaling the sensor input to
         * match the output's configured resolution.
         *
         * The crop region is applied after the RAW to other color space (e.g. YUV) conversion.
         * Since raw streams (e.g. RAW16) don't have the conversion stage, they are not croppable.
         * The crop region will be ignored by raw streams.
         *
         * For non-raw streams, any additional per-stream cropping will be done to maximize the
         * final pixel area of the stream.
         *
         * For example, if the crop region is set to a 4:3 aspect ratio, then 4:3 streams will use
         * the exact crop region. 16:9 streams will further crop vertically (letterbox).
         *
         * Conversely, if the crop region is set to a 16:9, then 4:3 outputs will crop horizontally
         * (pillarbox), and 16:9 streams will match exactly. These additional crops will be centered
         * within the crop region.
         *
         * If the coordinate system is android.sensor.info.activeArraySize, the width and height of
         * the crop region cannot be set to be smaller than
         * floor( activeArraySize.width / android.scaler.availableMaxDigitalZoom ) and
         * floor( activeArraySize.height / android.scaler.availableMaxDigitalZoom ), respectively.
         *
         * If the coordinate system is android.sensor.info.preCorrectionActiveArraySize, the width
         * and height of the crop region cannot be set to be smaller than
         * floor( preCorrectionActiveArraySize.width / android.scaler.availableMaxDigitalZoom ) and
         * floor( preCorrectionActiveArraySize.height / android.scaler.availableMaxDigitalZoom ),
         * respectively.
         *
         * The camera device may adjust the crop region to account for rounding and other hardware
         * requirements; the final crop region used will be included in the output capture result.
         *
         * Units: Pixel coordinates relative to android.sensor.info.activeArraySize or
         * android.sensor.info.preCorrectionActiveArraySize depending on distortion correction
         * capability and mode
         *
         * This key is available on all devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 17 (Misc)\n";
        string += "CaptureRequest.BLACK_LEVEL_LOCK:                       " + mBlackLevelLockName                   + "\n";
        string += "CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR:    " + mReprocessEffectiveExposureFactorName + "\n";
        string += "CaptureRequest.SCALAR_CROP_REGION:                     " + mScalarCropRegionName                 + "\n";

        stringList.add(string);
        return stringList;
    }

}
