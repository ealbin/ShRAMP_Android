package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.util.Range;

import java.text.DecimalFormat;
import java.util.List;

@TargetApi(21)
abstract class Level15_Sensor extends Level14_Lens {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Long    mSensorExposureTime;
    private   String  mSensorExposureTimeName;

    protected Long    mSensorFrameDuration;
    private   String  mSensorFrameDurationName;

    protected Integer mSensorAnalogSensitivity;
    private   String  mSensorAnalogSensitivityName;
    protected Integer mSensorSensitivity;
    private   String  mSensorSensitivityName;

    protected Integer mSensorTestPatternMode;
    private   String  mSensorTestPatternModeName;

    protected int[]   mSensorTestPatternData;
    private   String  mSensorTestPatternDataName;


    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level15_Sensor(@NonNull CameraCharacteristics characteristics,
                             @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setSensorExposureTime();
        setSensorFrameDuration();
        setSensorSensitivity();
        setSensorTestPatternMode();
        setSensorTestPatternData();
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
    private void setSensorExposureTime() {
        CaptureRequest.Key key = CaptureRequest.SENSOR_EXPOSURE_TIME;
        /*
         * Added in API 21
         *
         * Duration each pixel is exposed to light.
         *
         * If the sensor can't expose this exact duration, it will shorten the duration exposed to
         * the nearest possible value (rather than expose longer). The final exposure time used will
         * be available in the output capture result.
         *
         * This control is only effective if android.control.aeMode or android.control.mode is set
         * to OFF; otherwise the auto-exposure algorithm will override this value.
         *
         * Units: Nanoseconds
         *
         * Range of valid values:
         * android.sensor.info.exposureTimeRange
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mSensorExposureTime     = null;
            mSensorExposureTimeName = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.CONTROL_MODE_OFF
                || ( super.mControlAeMode != null
                     && super.mControlAeMode != CameraMetadata.CONTROL_AE_MODE_OFF)) {
            mSensorExposureTime     = null;
            mSensorExposureTimeName = "Disabled";
            return;
        }

        Range<Long> times = super.mCameraCharacteristics.get(
                            CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        if (times == null) {
            mSensorExposureTime     = null;
            mSensorExposureTimeName = "Not supported";
            return;
        }

        mSensorExposureTime     = times.getLower();
        DecimalFormat df        = new DecimalFormat("#.##");
        mSensorExposureTimeName = df.format(mSensorExposureTime / 1000.) + " [us]";

        super.mCaptureRequestBuilder.set(key, mSensorExposureTime);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setSensorFrameDuration() {
        CaptureRequest.Key key = CaptureRequest.SENSOR_FRAME_DURATION;
        /*
         * Added in API 21
         *
         * Duration from start of frame exposure to start of next frame exposure.
         *
         * The maximum frame rate that can be supported by a camera subsystem is a function of many
         * factors:
         *      Requested resolutions of output image streams
         *      Availability of binning / skipping modes on the imager
         *      The bandwidth of the imager interface
         *      The bandwidth of the various ISP processing blocks
         *
         * Since these factors can vary greatly between different ISPs and sensors, the camera
         * abstraction tries to represent the bandwidth restrictions with as simple a model as
         * possible.
         *
         * The model presented has the following characteristics:
         *      The image sensor is always configured to output the smallest resolution possible
         *      given the application's requested output stream sizes. The smallest resolution is
         *      defined as being at least as large as the largest requested output stream size;
         *      the camera pipeline must never digitally upsample sensor data when the crop region
         *      covers the whole sensor. In general, this means that if only small output stream
         *      resolutions are configured, the sensor can provide a higher frame rate.
         *
         *      Since any request may use any or all the currently configured output streams, the
         *      sensor and ISP must be configured to support scaling a single capture to all the
         *      streams at the same time. This means the camera pipeline must be ready to produce
         *      the largest requested output size without any delay. Therefore, the overall frame
         *      rate of a given configured stream set is governed only by the largest requested
         *      stream resolution.
         *
         *      Using more than one output stream in a request does not affect the frame duration.
         *
         *      Certain format-streams may need to do additional background processing before data
         *      is consumed/produced by that stream. These processors can run concurrently to the
         *      rest of the camera pipeline, but cannot process more than 1 capture at a time.
         *
         * The necessary information for the application, given the model above, is provided via
         * StreamConfigurationMap.getOutputMinFrameDuration(int, Size). These are used to determine
         * the maximum frame rate / minimum frame duration that is possible for a given stream
         * configuration.
         *
         * Specifically, the application can use the following rules to determine the minimum frame
         * duration it can request from the camera device:
         *      Let the set of currently configured input/output streams be called S.
         *
         *      Find the minimum frame durations for each stream in S, by looking it up in
         *      StreamConfigurationMap.getOutputMinFrameDuration(int, Size) (with its respective
         *      size/format). Let this set of frame durations be called F.
         *
         *      For any given request R, the minimum frame duration allowed for R is the maximum
         *      out of all values in F. Let the streams used in R be called S_r.
         *
         * If none of the streams in S_r have a stall time
         * (listed in StreamConfigurationMap.getOutputStallDuration(int, Size) using its respective
         * size/format), then the frame duration in F determines the steady state frame rate that
         * the application will get if it uses R as a repeating request. Let this special kind of
         * request be called Rsimple.
         *
         * A repeating request Rsimple can be occasionally interleaved by a single capture of a new
         * request Rstall (which has at least one in-use stream with a non-0 stall time) and if
         * Rstall has the same minimum frame duration this will not cause a frame rate loss if all
         * buffers from the previous Rstall have already been delivered.
         *
         * For more details about stalling, see
         * StreamConfigurationMap.getOutputStallDuration(int, Size).
         *
         * This control is only effective if android.control.aeMode or android.control.mode is
         * set to OFF; otherwise the auto-exposure algorithm will override this value.
         *
         * Units: Nanoseconds
         *
         * Range of valid values:
         * See android.sensor.info.maxFrameDuration, StreamConfigurationMap.
         * The duration is capped to max(duration, exposureTime + overhead).
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mSensorFrameDuration     = null;
            mSensorFrameDurationName = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.CONTROL_MODE_OFF
                || ( super.mControlAeMode != null
                     && super.mControlAeMode != CameraMetadata.CONTROL_AE_MODE_OFF)) {
            mSensorFrameDuration     = null;
            mSensorFrameDurationName = "Disabled";
            return;
        }

        long minDuration = super.mStreamConfigurationMap.getOutputMinFrameDuration(
                           super.mOutputFormat, super.mOutputSize);

        mSensorFrameDuration     = minDuration;
        DecimalFormat df         = new DecimalFormat("#.##");
        mSensorFrameDurationName = df.format(mSensorFrameDuration / 1000. / 1000.) + " [ms]";
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setSensorSensitivity() {
        CaptureRequest.Key key = CaptureRequest.SENSOR_SENSITIVITY;
        /*
         * Added in API 21
         *
         * The amount of gain applied to sensor data before processing.
         *
         * The sensitivity is the standard ISO sensitivity value, as defined in ISO 12232:2006.
         *
         * The sensitivity must be within android.sensor.info.sensitivityRange, and if if it less
         * than android.sensor.maxAnalogSensitivity, the camera device is guaranteed to use only
         * analog amplification for applying the gain.
         *
         * If the camera device cannot apply the exact sensitivity requested, it will reduce the
         * gain to the nearest supported value. The final sensitivity used will be available in the
         * output capture result.
         *
         * This control is only effective if android.control.aeMode or android.control.mode is set
         * to OFF; otherwise the auto-exposure algorithm will override this value.
         *
         * Units: ISO arithmetic units
         *
         * Range of valid values:
         * android.sensor.info.sensitivityRange
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mSensorAnalogSensitivity     = null;
            mSensorAnalogSensitivityName = "Not supported";
            mSensorSensitivity           = null;
            mSensorSensitivityName       = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.CONTROL_MODE_OFF
                || ( super.mControlAeMode != null
                     && super.mControlAeMode != CameraMetadata.CONTROL_AE_MODE_OFF)) {
            mSensorAnalogSensitivity     = null;
            mSensorAnalogSensitivityName = "Disabled";
            mSensorSensitivity           = null;
            mSensorSensitivityName       = "Disabled";
            return;
        }

        Integer maxAnalogSensitivity = super.mCameraCharacteristics.get(
                                       CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);
        if (maxAnalogSensitivity == null) {
            mSensorAnalogSensitivity     = null;
            mSensorAnalogSensitivityName = "Not supported";
        }
        else {
            mSensorAnalogSensitivity     = maxAnalogSensitivity;
            mSensorAnalogSensitivityName = "ISO " + Integer.toString(mSensorAnalogSensitivity);
        }

        Range<Integer> sensitivityRange = super.mCameraCharacteristics.get(
                                          CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (sensitivityRange == null) {
            mSensorSensitivity     = null;
            mSensorSensitivityName = "Not supported";
        }
        else {
            mSensorSensitivity     = sensitivityRange.getUpper();
            mSensorSensitivityName = "ISO " + Integer.toString(mSensorSensitivity);
        }

        if (mSensorSensitivity == null || mSensorAnalogSensitivity == null) {
            return;
        }

        super.mCaptureRequestBuilder.set(key, mSensorSensitivity);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setSensorTestPatternMode() {
        CaptureRequest.Key key = CaptureRequest.SENSOR_TEST_PATTERN_MODE;
        mSensorTestPatternMode     = null;
        mSensorTestPatternModeName = "Not applicable";
        /*
         * Added in API 21
         *
         * When enabled, the sensor sends a test pattern instead of doing a real exposure from the
         * camera.
         *
         * When a test pattern is enabled, all manual sensor controls specified by android.sensor.*
         * will be ignored. All other controls should work as normal.
         *
         * For example, if manual flash is enabled, flash firing should still occur
         * (and that the test pattern remain unmodified, since the flash would not actually affect
         * it).
         *
         * Defaults to OFF.
         *
         * Available values for this device:
         * android.sensor.availableTestPatternModes
         *
         * Optional - This value may be null on some devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setSensorTestPatternData() {
        CaptureRequest.Key key = CaptureRequest.SENSOR_TEST_PATTERN_DATA;
        mSensorTestPatternData     = null;
        mSensorTestPatternDataName = "Not applicable";
        /*
         * Added in API 21
         *
         * A pixel [R, G_even, G_odd, B] that supplies the test pattern when
         * android.sensor.testPatternMode is SOLID_COLOR.
         *
         * Each color channel is treated as an unsigned 32-bit integer. The camera device then uses
         * the most significant X bits that correspond to how many bits are in its Bayer raw sensor
         * output.
         *
         * For example, a sensor with RAW10 Bayer output would use the 10 most significant bits from
         * each color channel.
         *
         * Optional - This value may be null on some devices.
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

        String string = "Level 15 (Sensor)\n";
        string += "CaptureRequest.SENSOR_EXPOSURE_TIME:                 " + mSensorExposureTimeName      + "\n";
        string += "CaptureRequest.SENSOR_FRAME_DURATION:                " + mSensorFrameDurationName     + "\n";
        string += "CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY: " + mSensorAnalogSensitivityName + "\n";
        string += "CaptureRequest.SENSOR_SENSITIVITY:                   " + mSensorSensitivityName       + "\n";
        string += "CaptureRequest.SENSOR_TEST_PATTERN_MODE:             " + mSensorTestPatternModeName   + "\n";
        string += "CaptureRequest.SENSOR_TEST_PATTERN_DATA:             " + mSensorTestPatternDataName   + "\n";

        stringList.add(string);
        return stringList;
    }

}