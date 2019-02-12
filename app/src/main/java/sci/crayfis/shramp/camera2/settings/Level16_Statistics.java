package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level16_Statistics extends Level15_Sensor {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer mStatisticsFaceDetectMode;
    private   String  mStatisticsFaceDetectModeName;

    protected Boolean mStatisticsHotPixelMapMode;
    private   String  mStatisticsHotPixelMapModeName;

    protected Integer mStatisticsLensShadingMapMode;
    private   String  mStatisticsLensShadingMapModeName;

    protected Integer mStatisticsOisDataMode;
    private   String  mStatisticsOisDataModeName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level16_Statistics(@NonNull CameraCharacteristics characteristics,
                                 @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setStatisticsFaceDetectMode();
        setStatisticsHotPixelMapMode();
        setStatisticsLensShadingMapMode();
        setStatisticsOisDataMode();
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
    private void setStatisticsFaceDetectMode() {
        CaptureRequest.Key key = CaptureRequest.STATISTICS_FACE_DETECT_MODE;
        /*
         * Added in API 21
         *
         * Operating mode for the face detector unit.
         *
         * Whether face detection is enabled, and whether it should output just the basic fields or
         * the full set of fields.
         *
         * Available values for this device:
         * android.statistics.info.availableFaceDetectModes
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mStatisticsFaceDetectMode     = null;
            mStatisticsFaceDetectModeName = "Not supported";
            return;
        }

        // Default
        mStatisticsFaceDetectMode     = CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF;
        mStatisticsFaceDetectModeName = "Off";
        /*
         * Added in API 21
         *
         * Do not include face detection statistics in capture results.
         */

        super.mCaptureRequestBuilder.set(key, mStatisticsFaceDetectMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setStatisticsHotPixelMapMode() {
        CaptureRequest.Key key = CaptureRequest.STATISTICS_HOT_PIXEL_MAP_MODE;
        /*
         * Added in API 21
         *
         * Operating mode for hot pixel map generation.
         *
         * If set to true, a hot pixel map is returned in android.statistics.hotPixelMap. If set to
         * false, no hot pixel map will be returned.
         *
         * Range of valid values:
         * android.statistics.info.availableHotPixelMapModes
         *
         * Optional - This value may be null on some devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mStatisticsHotPixelMapMode     = null;
            mStatisticsHotPixelMapModeName = "Not supported";
            return;
        }

        // Default
        mStatisticsHotPixelMapMode     = false;
        mStatisticsHotPixelMapModeName = "Off";

        super.mCaptureRequestBuilder.set(key, mStatisticsHotPixelMapMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setStatisticsLensShadingMapMode() {
        CaptureRequest.Key key = CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE;
        /*
         * Added in API 21
         *
         * Whether the camera device will output the lens shading map in output result metadata.
         *
         * When set to ON, android.statistics.lensShadingMap will be provided in the output result
         * metadata.
         *
         * ON is always supported on devices with the RAW capability.
         *
         * Available values for this device:
         * android.statistics.info.availableLensShadingMapModes
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mStatisticsLensShadingMapMode     = null;
            mStatisticsLensShadingMapModeName = "Not supported";
            return;
        }

        // Default
        mStatisticsLensShadingMapMode     = CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_OFF;
        mStatisticsLensShadingMapModeName = "Off";
        /*
         * Added in API 21
         *
         * Do not include a lens shading map in the capture result.
         */

        super.mCaptureRequestBuilder.set(key, mStatisticsLensShadingMapMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setStatisticsOisDataMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            mStatisticsOisDataMode     = null;
            mStatisticsOisDataModeName = "Not supported";
            return;
        }
        CaptureRequest.Key key = CaptureRequest.STATISTICS_OIS_DATA_MODE;
        /*
         * Added in API 28
         *
         * A control for selecting whether optical stabilization (OIS) position information is
         * included in output result metadata.
         *
         * Since optical image stabilization generally involves motion much faster than the duration
         * of individualq image exposure, multiple OIS samples can be included for a single capture
         * result. For example, if the OIS reporting operates at 200 Hz, a typical camera operating
         * at 30fps may have 6-7 OIS samples per capture result. This information can be combined
         * with the rolling shutter skew to account for lens motion during image exposure in
         * post-processing algorithms.
         *
         * Available values for this device:
         * android.statistics.info.availableOisDataModes
         *
         * Optional - This value may be null on some devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mStatisticsOisDataMode     = null;
            mStatisticsOisDataModeName = "Not supported";
            return;
        }

        // Default
        mStatisticsOisDataMode     = CameraMetadata.STATISTICS_OIS_DATA_MODE_OFF;
        mStatisticsOisDataModeName = "Off";
        /*
         * Added in API 28
         *
         * Do not include OIS data in the capture result.
         */

        super.mCaptureRequestBuilder.set(key, mStatisticsOisDataMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 16 (Statistics)\n";
        string += "CaptureRequest.STATISTICS_FACE_DETECT_MODE:      " + mStatisticsFaceDetectModeName     + "\n";
        string += "CaptureRequest.STATISTICS_HOT_PIXEL_MAP_MODE:    " + mStatisticsHotPixelMapModeName    + "\n";
        string += "CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE: " + mStatisticsLensShadingMapModeName + "\n";
        string += "CaptureRequest.STATISTICS_OIS_DATA_MODE:         " + mStatisticsOisDataModeName        + "\n";

        stringList.add(string);
        return stringList;
    }

}