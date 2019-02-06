package Camera2;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

import Logging.DividerStyle;
import Logging.OutStream;
import Logging.ShrampLogger;

/**
 * Configures camera for capture session.
 * ShRAMP optimizes settings for dark, raw and fast capture.
 */
@TargetApi(21) // Lollipop
public class ShrampCameraCaptureSession {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Activity lock
    private final Object LOCK = new Object();

    // passed into constructor
    private CameraDevice            mCameraDevice;
    private CameraCharacteristics   mCameraCharacteristics;

    // created in constructor
    private StreamConfigurationMap  mStreamConfigurationMap;

    // nested classes defined at bottom
    private CaptureConfiguration    mCaptureConfiguration;
    private CameraConfiguration     mCameraConfiguration;

    // created in constructor
    private List<Integer>           mCameraAbilities;
    private CaptureRequest.Builder  mCaptureRequestBuilder;

    // Logging object
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final String TAG = "ShrampCameraCaptureSession";

    /**
     * Create ShrampCameraCaptureSession and accompanying objects.
     * Configures camera device through creation of CaptureConfiguration and CameraConfiguration
     * objects.
     * @param device CameraDevice to configure
     * @param characteristics CameraCharacteristics of device to configure
     */
    ShrampCameraCaptureSession(@NonNull CameraDevice device,
                               @NonNull CameraCharacteristics characteristics) {

        synchronized (LOCK) {
            mLogger.divider(DividerStyle.Strong);
            mLogger.logTrace();

            mCameraDevice          = device;
            mCameraCharacteristics = characteristics;

            mStreamConfigurationMap = mCameraCharacteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mLogger.log(TAG, "Discovering camera");

            // define stream and get properties of camera
            mCaptureConfiguration = new CaptureConfiguration();
            mCameraAbilities      = mCaptureConfiguration.getCameraAbilities();

            mLogger.log(TAG, "Configuring camera");

            // configure camera for capture
            mCaptureRequestBuilder = mCaptureConfiguration.getCaptureRequestBuilder();
            mCameraConfiguration   = new CameraConfiguration();

            // dump settings to log
            String report = mCaptureConfiguration.reportSettings();
            report = report.concat(mCameraConfiguration.reportSettings());
            mLogger.logTrace();
            mLogger.log(TAG, "Settings dump:");
            mLogger.log("", report);
            mLogger.log(TAG, "return;");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested class CaptureConfiguration
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper class to encapsulate the process of learning what this camera can do
     */
    private class CaptureConfiguration {

        final String LTAG = TAG.concat(".CaptureConfiguration");

        private List<Integer> mmCameraAbilities;
        private int           mmOutputFormat;
        private int           mmBitsPerPixel;
        private Size          mmOutputSize;
        private boolean       mmUsingManualTemplate;
        private boolean       mmUsingRawImageFormat;

        /**
         * Create CaptureConfiguration to learn of camera's abilities and stream format
         */
        CaptureConfiguration() {
            mLogger.divider(DividerStyle.Normal);
            mLogger.logTrace();

            assert mCameraCharacteristics  != null;
            assert mStreamConfigurationMap != null;

            mLogger.log(LTAG, "Loading camera abilities");
            loadCameraAbilities();

            mLogger.log(LTAG, "Loading stream format");
            loadStreamFormat();

            mLogger.log(LTAG, "return;");
        }

        /**
         * Discover camera's abilities
         */
        private void loadCameraAbilities() {
            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            mmCameraAbilities = new ArrayList<>();
            int[] abilities = mCameraCharacteristics.get(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);

            assert abilities != null;
            for (int ability : abilities) {
                mmCameraAbilities.add(ability);
            }

            mLogger.log(LTAG, "loadCameraAbilities: return;");
        }

        /**
         * Access camera abilities
         * @return a list of abilities (see android.hardware.camera2.CameraMetadata)
         */
        List<Integer> getCameraAbilities() { return mmCameraAbilities; }

        //==========================================================================================

        /**
         * Determine stream format
         */
        private void loadStreamFormat() {
            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            assert mmCameraAbilities != null;
            int outputFormat;
            if (mmCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
                outputFormat = ImageFormat.RAW_SENSOR;
                mmUsingRawImageFormat = true;
            }
            else {
                outputFormat = ImageFormat.YUV_420_888;
                mmUsingRawImageFormat = false;
            }
            int bitsPerPixel = ImageFormat.getBitsPerPixel(outputFormat);

            Size[] outputSizes = mStreamConfigurationMap.getOutputSizes(outputFormat);
            Size   outputSize = null;
            for (Size size : outputSizes) {
                if (outputSize == null) {
                    outputSize = size;
                    continue;
                }
                long outputArea = outputSize.getWidth() * outputSize.getHeight();
                long sizeArea   =       size.getWidth() *       size.getHeight();
                if (sizeArea > outputArea) {
                    outputSize = size;
                }
            }
            mmOutputFormat = outputFormat;
            mmBitsPerPixel = bitsPerPixel;
            mmOutputSize   = outputSize;

            mLogger.log(LTAG, "loadStreamFormat: return;");
        }

        /**
         * Can this camera use the raw pixel format?
         * @return true if yes, false if no (aka YUV_420_888)
         */
        boolean isOutputFormatRaw() { return mmUsingRawImageFormat; }

        /**
         * Access the output format
         * @return int format code (android.hardware.camera2.CameraMetadata)
         */
        int getOutputFormat() { return mmOutputFormat; }

        /**
         * Number of bits per pixel
         * @return int
         */
        int getBitsPerPixel() { return mmBitsPerPixel; }

        /**
         * Access output format size in pixels
         * @return Size object, use methods getWidth() and getHeight()
         */
        Size getOutputSize() { return mmOutputSize; }

        //==========================================================================================

        /**
         * Create CaptureRequest.Builder and configure with manual template if possible.
         * Use preview template if manual is unavailable for maximum frame rate.
         * @return a CaptureRequest.Builder object used to configure the capture
         */
        CaptureRequest.Builder getCaptureRequestBuilder() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            int captureTemplate;
            if (mmCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                captureTemplate = CameraDevice.TEMPLATE_MANUAL;
                mmUsingManualTemplate = true;
            } else {
                // preview is guarenteed on all camera devices
                captureTemplate = CameraDevice.TEMPLATE_PREVIEW;
                mmUsingManualTemplate = false;
            }
            try {
                mLogger.log(LTAG, "getCaptureRequestBuilder: return CaptureRequest.Builder");
                return mCameraDevice.createCaptureRequest(captureTemplate);
            } catch (CameraAccessException e) {
                // TODO EXCEPTION
                mLogger.log(LTAG, "getCaptureRequestBuilder: Camera Access Exeption");
                mLogger.log(LTAG, "getCaptureRequestBuilder: return null;");
                return null;
            }
        }

        /**
         * Is this camera using the manual template?
         * @return true if yes, false if no (aka preview template)
         */
        boolean usingManualCaptureRequestTemplate() { return mmUsingManualTemplate; }

        //==========================================================================================

        /**
         * Discover minimum frame duration for this stream configuration
         * @return time in nanoseconds (possibly 0 if camera does not support this function)
         */
        long getOutputMinFrameDuration() {
            return mStreamConfigurationMap.getOutputMinFrameDuration(mmOutputFormat, mmOutputSize);
        }

        /**
         * Discover time between frame captures (if any)
         * @return time in nanoseconds (hopefully 0, always 0 for YUV_420_888)
         */
        long getOutputStallDuration() {
            return mStreamConfigurationMap.getOutputStallDuration(mmOutputFormat, mmOutputSize);
        }

        /**
         * Report a summary of settings
         * @return a string summary
         */
        String reportSettings() {

            String report = " \n";
            if (isOutputFormatRaw()) {
                report = report.concat("Output Format: Raw");
            }
            else {
                report = report.concat("Output Format: YUV_420_888");
            }
            report = report.concat("\n");

            report = report.concat("Bits per pixel: " + Integer.toString(getBitsPerPixel()));
            report = report.concat("\n");

            report = report.concat("Output size: " + getOutputSize().toString());
            report = report.concat("\n");

            if (usingManualCaptureRequestTemplate()) {
                report = report.concat("Capture Template: Manual");
            }
            else {
                report = report.concat("Capture Template: Preview");
            }
            report = report.concat("\n");

            return report;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested class CameraConfiguration
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper class to encapsulate the process of configuring the capture session
     */
    private class CameraConfiguration {

        final String LTAG = TAG.concat(".CameraConfiguration");

        // configure flash
        private boolean mmFlashOn;

        // configure control mode
        private boolean mmControlModeAuto;

        // configure intent
        private boolean mmCaptureIntentPreview;

        // configure auto-white balance
        private boolean mmControlAwbModeOn;
        private boolean mmControlAwbLockOn;

        // configure auto-focus
        private boolean mmControlAfModeOn;

        // configure auto-exposure
        private boolean mmControlAeModeOn;
        private boolean mmControlAeLockOn;
        private boolean mmControlAeAntibandingOn;
        private boolean mmControlAeCompensationSet;
        private boolean mmAeTargetFpsRangeSet;

        // configure corrections
        private boolean mmBlackLevelLocked;
        private boolean mmColorCorrectionAberrationOn;
        private boolean mmColorCorrectionModeTransformOn;
        private boolean mmUsingPostRawBoost;
        private boolean mmEdgeModeOn;
        private boolean mmHotPixelModeOn;
        private boolean mmNoiseReductionOn;
        private boolean mmShadingModeOn;
        private boolean mmUsingContrastCurve;

        // configure optics
        private boolean mmOpticalStabilizationModeOn;

        // configure statistics
        private boolean mmFaceDetectModeOn;
        private boolean mmHotPixelMapModeOn;
        private boolean mmLensShadingMapModeOn;
        private boolean mmOisDataModeOn;

        // configure sensor
        private boolean mmMaxAnalogSensitivity;
        private boolean mmMinExposureTimeOn;
        private boolean mmMinFrameDurationOn;

        /**
         * This is a biggie..
         * reference:  https://developer.android.com/reference/android/hardware/camera2/CaptureRequest
         */
        CameraConfiguration() {

            mLogger.divider(DividerStyle.Normal);
            mLogger.logTrace();

            assert mCameraAbilities       != null;
            assert mCaptureConfiguration  != null;
            assert mCaptureRequestBuilder != null;

            mLogger.log(LTAG, "configuring flash");
            configureFlash();

            mLogger.log(LTAG, "configuring control mode");
            configureControlMode();

            mLogger.log(LTAG, "configuring capture intent");
            configureCaptureIntent();

            // Note: auto-white balance and auto-focus should be set before auto-exposure
            // reference:
            //  https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#CONTROL_AWB_MODE
            //  https://developer.android.com/reference/android/hardware/camera2/CameraMetadata#CONTROL_AE_MODE_OFF
            mLogger.log(LTAG, "configuring auto-white balance");
            configureAWB();

            mLogger.log(LTAG, "configuring auto-focus");
            configureAF();

            mLogger.log(LTAG, "configuring auto-exposure");
            configureAE();

            mLogger.log(LTAG, "configuring corrections");
            configureCorrections();

            mLogger.log(LTAG, "configuring optics");
            configureOptics();

            mLogger.log(LTAG, "configuring statistics");
            configureStatistics();

            mLogger.log(LTAG, "configuring sensor");
            configureSensor();

            mLogger.log(LTAG, "irrelevant setting: CONTROL_AWB_REGIONS");
            mLogger.log(LTAG, "irrelevant setting: CONTROL_AF_REGIONS");
            mLogger.log(LTAG, "irrelevant setting: CONTROL_AF_TRIGGER");
            mLogger.log(LTAG, "irrelevant setting: CONTROL_AE_REGIONS");
            mLogger.log(LTAG, "irrelevant setting: CONTROL_AE_PRECAPTURE_TRIGGER");
            mLogger.log(LTAG, "irrelevant setting: CONTROL_ENABLE_ZSL");
            mLogger.log(LTAG, "irrelevant setting: CONTROL_SCENE_MODE");
            mLogger.log(LTAG, "irrelevant setting: JPEG_GPS_LOCATION");
            mLogger.log(LTAG, "irrelevant setting: JPEG_ORIENTATION");
            mLogger.log(LTAG, "irrelevant setting: JPEG_QUALITY");
            mLogger.log(LTAG, "irrelevant setting: JPEG_THUMBNAIL_QUALITY");
            mLogger.log(LTAG, "irrelevant setting: SCALAR_CROP_REGION");
            mLogger.log(LTAG, "irrelevant setting: SENSOR_TEST_PATTERN_MODE");
            mLogger.log(LTAG, "irrelevant setting: SENSOR_TEST_PATTERN_DATA");

            mLogger.log(LTAG, "return;");
        }

        //==========================================================================================

        /**
         * Configure flash for capture session (turn off)
         */
        private void configureFlash() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            mLogger.log(LTAG, "configureFlash: FLASH_MODE");
            Boolean flashInfoAvailable = mCameraCharacteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE);

            if (flashInfoAvailable == null) {
                mmFlashOn = false;
                mLogger.log(LTAG, "configureFlash: no flash info available");
                mLogger.log(LTAG, "configureFlash: return;");
                return;
            }

            if (flashInfoAvailable) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
            mmFlashOn = false;

            mLogger.log(LTAG, "configureFlash: return;");
        }

        /**
         * Is the flash on?
         * @return true if yes, false if no
         */
        public boolean isFlashOn() { return mmFlashOn; }

        //==========================================================================================

        /**
         * Configure control mode for capture session (mode off if possible, otherwise auto)
         */
        private void configureControlMode() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            mLogger.log(LTAG, "configureControlMode: CONTROL_MODE");
            mmControlModeAuto = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int[] conModes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AVAILABLE_MODES);

                if (conModes != null && conModes.length > 0) {

                    List<Integer> controlModes = new ArrayList<>();
                    for (int mode : conModes) {
                        controlModes.add(mode);
                    }

                    int controlMode;
                    if (controlModes.contains(CameraMetadata.CONTROL_MODE_OFF)) {
                        controlMode = CameraMetadata.CONTROL_MODE_OFF;
                        mmControlModeAuto = false;
                    } else {
                        controlMode = CameraMetadata.CONTROL_MODE_AUTO;
                    }

                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_MODE,
                            controlMode);
                }
            } else {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_MODE,
                        CameraMetadata.CONTROL_MODE_OFF);
                mmControlModeAuto = false;
            }

            mLogger.log(LTAG, "configureControlMode: return;");
        }
        public boolean isControlModeAuto() { return mmControlModeAuto; }

        //==========================================================================================

        /**
         * Configure capture intent for capture session (manual if possible, otherwise preview)
         */
        private void configureCaptureIntent() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            mLogger.log(LTAG, "configureCaptureIntent: CONTROL_CAPTURE_INTENT");
            if (mCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_CAPTURE_INTENT,
                        CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL);
                mmCaptureIntentPreview = false;

                mLogger.log(LTAG, "configureCaptureIntent: return;");
                return;
            }

            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_CAPTURE_INTENT,
                    CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW);
            mmCaptureIntentPreview = true;

            mLogger.log(LTAG, "configureCaptureIntent: return;");
        }
        public boolean isCaptureIntentPreview() { return mmCaptureIntentPreview; }

        //==========================================================================================

        /**
         * Configure auto-white balance for capture session.
         * Turn auto-white balance mode off if possible, auto otherwise.
         * Lock auto-white levels if possible.
         */
        private void configureAWB() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // CONTROL_AWB_MODE
            mLogger.log(LTAG, "configureAWB: CONTROL_AWB_MODE");

            mmControlAwbModeOn = false;
            if (isControlModeAuto()) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                int awbMode;
                if (controlModes.contains(CameraMetadata.CONTROL_AWB_MODE_OFF)) {
                    awbMode = CameraMetadata.CONTROL_AWB_MODE_OFF;
                } else {
                    awbMode = CameraMetadata.CONTROL_AWB_MODE_AUTO;
                    mmControlAwbModeOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AWB_MODE,
                        awbMode);
            }

            //--------------------------------------------------------------------------------------

            // CONTROL_AWB_LOCK
            mLogger.log(LTAG, "configureAWB: CONTROL_AWB_LOCK");

            if (isControlModeAuto() && isControlAwbModeOn()) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AWB_LOCK,
                        true);
                mmControlAwbLockOn = true;
            } else {
                mmControlAwbLockOn = false;
            }

            mLogger.log(LTAG, "configureAWB: return;");
        }
        public boolean isControlAwbModeOn() { return mmControlAwbModeOn; }
        public boolean isControlAwbLockOn() { return mmControlAwbLockOn; }

        //==========================================================================================

        /**
         * Configure auto-focus for capture session.
         * Turn auto-focus mode off if possible, auto otherwise.
         */
        private void configureAF() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // CONTROL_AF_MODE
            mLogger.log(LTAG, "configureAF: CONTROL_AF_MODE");

            mmControlAfModeOn = false;
            if (isControlModeAuto()) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                int afMode;
                if (controlModes.contains(CameraMetadata.CONTROL_AF_MODE_OFF)) {
                    afMode = CameraMetadata.CONTROL_AF_MODE_OFF;
                } else {
                    afMode = CameraMetadata.CONTROL_AF_MODE_AUTO;
                    mmControlAfModeOn = true;
                }
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        afMode);
            }

            mLogger.log(LTAG, "configureAF: return;");
        }
        public boolean isControlAfModeOn() { return mmControlAfModeOn; }

        //==========================================================================================

        /**
         * Configure auto-exposure for capture session.
         * Turn auto-exposure mode off if possible, auto otherwise.
         * Lock exposure setting if possible.
         * Disable anti-banding abilities if possible, otherwise set to 60 Hz, lastly auto.
         * Disable exposure compensation or minimize it if possible.
         * Set exposure to fastest fps
         */
        private void configureAE() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // CONTROL_AE_MODE
            mLogger.log(LTAG, "configureAE: CONTROL_AE_MODE");

            mmControlAeModeOn = false;
            if (isControlModeAuto()) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                int aeMode;
                if (controlModes.contains(CameraMetadata.CONTROL_AE_MODE_OFF)) {
                    aeMode = CameraMetadata.CONTROL_AE_MODE_OFF;
                } else {
                    aeMode = CameraMetadata.CONTROL_AE_MODE_ON;
                    mmControlAeModeOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        aeMode);
            }

            //--------------------------------------------------------------------------------------

            // CONTROL_AE_LOCK
            mLogger.log(LTAG, "configureAE: CONTROL_AE_LOCK");

            if (isControlModeAuto() && isControlAeModeOn()) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_LOCK,
                        true);
                mmControlAeLockOn = true;
            } else {
                mmControlAeLockOn = false;
            }

            //--------------------------------------------------------------------------------------

            // CONTROL_AE_ANTIBANDING_MODE
            mLogger.log(LTAG, "configureAE: CONTROL_AE_ANTIBANDING_MODE");

            mmControlAeAntibandingOn = false;
            if (isControlModeAuto() && isControlAeModeOn()) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                int antiMode;
                if (controlModes.contains(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF)) {
                    antiMode = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF;
                } else if (controlModes.contains(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ)) {
                    antiMode = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ;
                    mmControlAeAntibandingOn = true;
                } else {
                    antiMode = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO;
                    mmControlAeAntibandingOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                        antiMode);
            }

            //--------------------------------------------------------------------------------------

            // CONTROL_AE_EXPOSURE_COMPENSATION
            mLogger.log(LTAG, "configureAE: CONTROL_AE_EXPOSURE_COMPENSATION");

            mmControlAeCompensationSet = false;
            if (isControlAeModeOn()) {
                Range<Integer> compensationRange = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                assert compensationRange != null;

                if (!compensationRange.equals(new Range<>(0, 0))) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                            compensationRange.getLower());
                    mmControlAeCompensationSet = true;
                }
            }

            //--------------------------------------------------------------------------------------

            // CONTROL_AE_TARGET_FPS
            mLogger.log(LTAG, "configureAE: CONTROL_AE_TARGET_FPS");

            mmAeTargetFpsRangeSet = false;
            if (isControlModeAuto() || isControlAeModeOn()) {
                Range<Integer>[] fpsRanges = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                assert fpsRanges != null;

                Range<Integer> fastestRange = null;
                for (Range<Integer> range : fpsRanges) {
                    if (fastestRange == null) {
                        fastestRange = range;
                        continue;
                    }
                    long range_product = range.getLower() * range.getLower();
                    long current_product = fastestRange.getLower() * fastestRange.getUpper();
                    if (range_product > current_product && range.getUpper() >= fastestRange.getUpper()) {
                        fastestRange = range;
                    }
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                        fastestRange);
                mmAeTargetFpsRangeSet = true;
            }

            mLogger.log(LTAG, "configureAE: return;");
        }
        public boolean isControlAeModeOn() { return mmControlAeModeOn; }
        public boolean isControlAeLockOn() { return mmControlAeLockOn; }
        public boolean isControlAeAntibandingOn() { return mmControlAeAntibandingOn; }
        public boolean isControlAeCompensationSet() { return mmControlAeCompensationSet; }
        public boolean isAeTargetFpsRangeSet() { return mmAeTargetFpsRangeSet; }

        //==========================================================================================

        /**
         * Configure corrections for capture session.
         * Lock black level.
         * Disable color-correction for aberration if possible, set to fast otherwise.
         * Set color-correction mode to transform matrix if possible, otherwise fast.
         * Set color transform matrix to unity (RGB -> RGB).
         * Set color correction gains to unity.
         * Disable color effect mode.
         * Set post-raw sensitivity boost to unity (disabled).
         * Disable video stabilization mode.
         * Disable distortion correction mode.
         * Disable edge mode if possible, fast otherwise.
         * Disable hot pixel mode if possible, fast otherwise.
         * Disable noise reduction mode if possible, fast otherwise.
         * Set reprocess exposure factor to unity.
         * Disable shading mode if possible, fast otherwise.
         * Set tone-map mode to specified curve if possible, fast otherwise.
         * Set tone-map curve to linear response.
         */
        private void configureCorrections() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // BLACK_LEVEL_LOCK
            mLogger.log(LTAG, "configureCorrections: BLACK_LEVEL_LOCK");

            mCaptureRequestBuilder.set(
                    CaptureRequest.BLACK_LEVEL_LOCK,
                    true);
            mmBlackLevelLocked = true;

            //--------------------------------------------------------------------------------------

            // COLOR_CORRECTION_ABERRATION_MODE
            mLogger.log(LTAG, "configureCorrections: COLOR_CORRECTION_ABERRATION_MODE");

            mmColorCorrectionAberrationOn = false;
            int[] abModes = mCameraCharacteristics.get(
                    CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES);

            if (abModes != null && abModes.length > 0) {
                List<Integer> aberrationModes = new ArrayList<>();
                for (int mode : abModes) {
                    aberrationModes.add(mode);
                }

                int abMode;
                if (aberrationModes.contains(CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF)) {
                    abMode = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF;
                } else {
                    abMode = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_FAST;
                    mmColorCorrectionAberrationOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                        abMode);
            }

            //--------------------------------------------------------------------------------------

            // COLOR_CORRECTION_MODE
            mLogger.log(LTAG, "configureCorrections: COLOR_CORRECTION_MODE");

            if (isControlAwbModeOn()) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_MODE,
                        CameraMetadata.COLOR_CORRECTION_MODE_FAST);
                mmColorCorrectionModeTransformOn = false;
            } else {
                mCaptureRequestBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_MODE,
                        CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
                mmColorCorrectionModeTransformOn = true;
            }

            //--------------------------------------------------------------------------------------

            // COLOR_CORRECTION_TRANSFORM
            mLogger.log(LTAG, "configureCorrections: COLOR_CORRECTION_TRANSFORM");

            if (isColorCorrectionModeTransformOn()) {
                mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM,
                        new ColorSpaceTransform(new int[]{
                                1, 1, 0, 1, 0, 1,    // 1/1 , 0/1 , 0/1 = 1 0 0
                                0, 1, 1, 1, 0, 1,    // 0/1 , 1/1 , 0/1 = 0 1 0
                                0, 1, 0, 1, 1, 1     // 0/1 , 0/1 , 1/1 = 0 0 1
                        }));
            }

            //--------------------------------------------------------------------------------------

            // COLOR_CORRECTION_GAINS
            mLogger.log(LTAG, "configureCorrections: COLOR_CORRECTION_GAINS");

            if (isColorCorrectionModeTransformOn()) {
                mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS,
                        new RggbChannelVector(1, 1, 1, 1));
            }

            //--------------------------------------------------------------------------------------

            // COLOR_EFFECT_MODE
            mLogger.log(LTAG, "configureCorrections: COLOR_EFFECT_MODE");

            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CameraMetadata.CONTROL_EFFECT_MODE_OFF);

            //--------------------------------------------------------------------------------------

            // CONTROL_POST_RAW_SENSITIVITY_BOOST
            mLogger.log(LTAG, "configureCorrections: CONTROL_POST_RAW_SENSITIVITY_BOOST");

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) { // API 24
                mmUsingPostRawBoost = false;
            } else if (isControlModeAuto() && isControlAeModeOn()) {
                mmUsingPostRawBoost = true;
            } else {
                if (mCaptureConfiguration.isOutputFormatRaw()) {
                    mmUsingPostRawBoost = false;
                } else {
                    Range<Integer> boostRange = mCameraCharacteristics.get(
                            CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE);
                    if (boostRange != null) {
                        mCaptureRequestBuilder.set(
                                CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST,
                                100);
                    }
                    mmUsingPostRawBoost = false;
                }
            }

            //--------------------------------------------------------------------------------------

            // CONTROL_VIDEO_STABILIZATION_MODE
            mLogger.log(LTAG, "configureCorrections: CONTROL_VIDEO_SABILIZATION_MODE");

            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);

            //--------------------------------------------------------------------------------------

            // DISTORTION_CORRECTION_MODE
            mLogger.log(LTAG, "configureCorrections: DISTORTION_CORRECTION_MODE");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  // API 28
                mCaptureRequestBuilder.set(
                        CaptureRequest.DISTORTION_CORRECTION_MODE,
                        CameraMetadata.DISTORTION_CORRECTION_MODE_OFF);
            }

            //--------------------------------------------------------------------------------------

            // EDGE_MODE
            mLogger.log(LTAG, "configureCorrections: EDGE_MODE");

            mmEdgeModeOn = false;
            int[] edModes = mCameraCharacteristics.get(
                    CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES);

            if (edModes != null && edModes.length > 0) {
                List<Integer> edgeModes = new ArrayList<>();
                for (int mode : edModes) {
                    edgeModes.add(mode);
                }

                int edgeMode;
                if (edgeModes.contains(CameraMetadata.EDGE_MODE_OFF)) {
                    edgeMode = CameraMetadata.EDGE_MODE_OFF;
                } else {
                    edgeMode = CameraMetadata.EDGE_MODE_FAST;
                    mmEdgeModeOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.EDGE_MODE,
                        edgeMode);
            }

            //--------------------------------------------------------------------------------------

            // HOT_PIXEL_MODE
            mLogger.log(LTAG, "configureCorrections: HOT_PIXEL_MODE");

            mmHotPixelModeOn = false;
            int[] hModes = mCameraCharacteristics.get(
                    CameraCharacteristics.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES);

            if (hModes != null && hModes.length > 0) {
                List<Integer> hotModes = new ArrayList<>();
                for (int mode : hModes) {
                    hotModes.add(mode);
                }

                int hotMode;
                if (hotModes.contains(CameraMetadata.HOT_PIXEL_MODE_OFF)) {
                    hotMode = CameraMetadata.HOT_PIXEL_MODE_OFF;
                } else {
                    hotMode = CameraMetadata.HOT_PIXEL_MODE_FAST;
                    mmHotPixelModeOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.HOT_PIXEL_MODE,
                        hotMode);
            }

            //--------------------------------------------------------------------------------------

            // NOISE_REDUCTION_MODE
            mLogger.log(LTAG, "configureCorrections: NOISE_REDUCTION_MODE");

            mmNoiseReductionOn = false;
            int[] nModes = mCameraCharacteristics.get(
                    CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);

            if (nModes != null && nModes.length > 0) {
                List<Integer> noiseModes = new ArrayList<>();
                for (int mode : nModes) {
                    noiseModes.add(mode);
                }

                int noiseMode;
                if (noiseModes.contains(CameraMetadata.NOISE_REDUCTION_MODE_OFF)) {
                    noiseMode = CameraMetadata.NOISE_REDUCTION_MODE_OFF;
                } else {
                    noiseMode = CameraMetadata.NOISE_REDUCTION_MODE_FAST;
                    mmNoiseReductionOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.NOISE_REDUCTION_MODE,
                        noiseMode);
            }

            //--------------------------------------------------------------------------------------

            // REPROCESS_EFFECTIVE_EXPOSURE_FACTOR
            mLogger.log(LTAG, "configureCorrections: REPROCESS_EFFECTIVE_EXPOSURE_FACTOR");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23
                if (mCameraAbilities.contains(
                        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING)) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR,
                            1.f);
                }
            }

            //--------------------------------------------------------------------------------------

            // SHADING_MODE
            mLogger.log(LTAG, "configureCorrections: SHADING_MODE");

            mmShadingModeOn = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23
                int[] sModes = mCameraCharacteristics.get(
                        CameraCharacteristics.SHADING_AVAILABLE_MODES);

                if (sModes != null && sModes.length > 0) {
                    List<Integer> shadingModes = new ArrayList<>();
                    for (int mode : sModes) {
                        shadingModes.add(mode);
                    }

                    int shadingMode;
                    if (shadingModes.contains(CameraMetadata.SHADING_MODE_OFF)) {
                        shadingMode = CameraMetadata.SHADING_MODE_OFF;
                    } else {
                        shadingMode = CameraMetadata.SHADING_MODE_FAST;
                        mmShadingModeOn = true;
                    }

                    mCaptureRequestBuilder.set(
                            CaptureRequest.SHADING_MODE,
                            shadingMode);
                }
            }

            //--------------------------------------------------------------------------------------

            // TONEMAP_MODE
            mLogger.log(LTAG, "configureCorrections: TONEMAP_MODE");

            mmUsingContrastCurve = false;
            int[] tModes = mCameraCharacteristics.get(
                    CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES);

            if (tModes != null && tModes.length > 0) {
                List<Integer> toneModes = new ArrayList<>();
                for (int mode : tModes) {
                    toneModes.add(mode);
                }

                int toneMode;
                if (toneModes.contains(CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE)) {
                    toneMode = CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE;
                    mmUsingContrastCurve = true;
                } else {
                    toneMode = CameraMetadata.TONEMAP_MODE_FAST;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.TONEMAP_MODE,
                        toneMode);
            }

            //--------------------------------------------------------------------------------------

            // TONEMAP_CURVE
            mLogger.log(LTAG, "configureCorrections: TONEMAP_CURVE");

            if (usingConstrastCurve()) {
                float[] linear_response = {0, 0, 1, 1};
                mCaptureRequestBuilder.set(CaptureRequest.TONEMAP_CURVE,
                        new TonemapCurve(linear_response, linear_response, linear_response));
            }

            mLogger.log(LTAG, "configureCorrections: return;");
        }
        public boolean isBlackLevelLocked() { return mmBlackLevelLocked; }
        public boolean isColorCorrectionAberrationModeOn() { return mmColorCorrectionAberrationOn; }
        public boolean isColorCorrectionModeTransformOn() { return mmColorCorrectionModeTransformOn; }
        public boolean isUsingPostRawBoost() { return mmUsingPostRawBoost; }
        public boolean isEdgeModeOn() { return mmEdgeModeOn; }
        public boolean isHotPixelModeOn() { return mmHotPixelModeOn; }
        public boolean isNoiseReductionOn() { return mmNoiseReductionOn; }
        public boolean usingConstrastCurve() { return mmUsingContrastCurve; }

        //==========================================================================================

        /**
         * Configure optics for capture session.
         * Try to set for minimum aperture (darkest).
         * Try to set for maximum filter density (darkest).
         * Try to set for maximum focal length (blurriest).
         * Set focal distance to infinity (defocus from near surface).
         * Try to disable optical stabilization.
         */
        private void configureOptics() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // LENS_APERTURE
            mLogger.log(LTAG, "configureOptics: LENS_APERTURE");

            float[] lApertures = mCameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);

            if (lApertures != null && lApertures.length > 0 && !isControlAeModeOn()) {
                Float minAperture = null;
                for (float aperture : lApertures) {
                    if (minAperture == null) {
                        minAperture = aperture;
                        continue;
                    }

                    if (aperture < minAperture) {
                        minAperture = aperture;
                    }
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.LENS_APERTURE,
                        minAperture);
            }

            //--------------------------------------------------------------------------------------

            // LENS_FILTER_DENSITY
            mLogger.log(LTAG, "configureOptics: LENS_FILTER_DENSITY");

            float[] lDensities = mCameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES);

            if (lDensities != null && lDensities.length > 0) {
                Float maxDensity = null;
                for (float density : lDensities) {
                    if (maxDensity == null) {
                        maxDensity = density;
                        continue;
                    }

                    if (density > maxDensity) {
                        maxDensity = density;
                    }
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.LENS_FILTER_DENSITY,
                        maxDensity);
            }

            //--------------------------------------------------------------------------------------

            // LENS_FOCAL_LENGTH
            mLogger.log(LTAG, "configureOptics: LENS_FOCAL_LENGTH");

            float[] fLengths = mCameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

            if (fLengths != null && fLengths.length > 0) {
                Float maxFocalLength = null;
                for (float length : fLengths) {
                    if (maxFocalLength == null) {
                        maxFocalLength = length;
                        continue;
                    }

                    if (length > maxFocalLength) {
                        maxFocalLength = length;
                    }
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.LENS_FOCAL_LENGTH,
                        maxFocalLength);
            }

            //--------------------------------------------------------------------------------------

            // LENS_FOCUS_DISTANCE
            mLogger.log(LTAG, "configureOptics: LENS_FOCUS_DISTANCE");

            mCaptureRequestBuilder.set(
                    CaptureRequest.LENS_FOCUS_DISTANCE,
                    0.f);

            //--------------------------------------------------------------------------------------

            // LENS_OPTICAL_STABILIZATION_MODE
            mLogger.log(LTAG, "configureOptics: LENS_OPTICAL_STABILIZATION_MODE");

            mmOpticalStabilizationModeOn = false;
            int[] sModes = mCameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);

            if (sModes != null && sModes.length > 0) {
                List<Integer> stabilizationModes = new ArrayList<>();
                for (int mode : sModes) {
                    stabilizationModes.add(mode);
                }

                int stabilizationMode;
                if (stabilizationModes.contains(CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF)) {
                    stabilizationMode = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF;
                }
                else {
                    stabilizationMode = CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON;
                    mmOpticalStabilizationModeOn = true;
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        stabilizationMode);
            }

            mLogger.log(LTAG, "configureOptics: return;");
        }
        public boolean isOpticalStabilizationModeOne() { return mmOpticalStabilizationModeOn; }

        //==========================================================================================

        /**
         * Configure statistics for capture session.
         * Disable face detection.
         * Disable hot pixel maps.
         * Disable lens shading maps.
         * Disable OIS data.
         */
        private void configureStatistics() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // STATISTICS_FACE_DETECT_MODE
            mLogger.log(LTAG, "configureStatistics: STATISTICS_FACE_DETECT_MODE");

            mmFaceDetectModeOn = false;
            mCaptureRequestBuilder.set(
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF);

            //--------------------------------------------------------------------------------------

            // STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES
            mLogger.log(LTAG, "configureStatistics: STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES");

            boolean[] pModes = mCameraCharacteristics.get(
                    CameraCharacteristics.STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES);

            mmHotPixelMapModeOn = false;
            if (pModes != null && pModes.length > 0) {
                mmHotPixelMapModeOn = true;
                for (boolean modeOn : pModes) {
                    if (!modeOn) {
                        mmHotPixelMapModeOn = false;
                        break;
                    }
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.STATISTICS_HOT_PIXEL_MAP_MODE,
                        mmHotPixelMapModeOn);
            }

            //--------------------------------------------------------------------------------------

            // STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES
            mLogger.log(LTAG, "configureStatistics: STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES");

            mmLensShadingMapModeOn = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23
                int[] sModes = mCameraCharacteristics.get(
                        CameraCharacteristics.STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES);

                if (sModes != null && sModes.length > 0) {
                    List<Integer> shadingModes = new ArrayList<>();
                    for (int mode : sModes) {
                        shadingModes.add(mode);
                    }

                    int modeCode;
                    if (shadingModes.contains(CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_OFF)) {
                        modeCode = CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_OFF;
                        mmLensShadingMapModeOn = false;
                    }
                    else {
                        modeCode = CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_ON;
                        mmLensShadingMapModeOn = true;
                    }

                    mCaptureRequestBuilder.set(
                            CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE,
                            modeCode);
                }
            }

            //--------------------------------------------------------------------------------------

            // STATISTICS_OIS_DATA_MODE
            mLogger.log(LTAG, "configureStatistics: STATISTICS_OIS_DATA_MODE");

            mmOisDataModeOn = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28

                int[] oModes = mCameraCharacteristics.get(
                        CameraCharacteristics.STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES);

                if (oModes != null && oModes.length > 0) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.STATISTICS_OIS_DATA_MODE,
                            CameraMetadata.STATISTICS_OIS_DATA_MODE_OFF);
                }
            }

            mLogger.log(LTAG, "configureStatistics: return;");
        }
        public boolean isFaceDetectModeOn() { return mmFaceDetectModeOn; }
        public boolean isHotPixelMapModeOn() { return mmHotPixelMapModeOn; }
        public boolean isLensShadingMapModeOn() { return mmLensShadingMapModeOn; }
        public boolean isOisDataModeOn() { return mmOisDataModeOn; }

        //==========================================================================================

        /**
         * Configure sensor for capture session.
         * Set maximum analog sensitivity (no digital gain).
         * Set exposure time to minimum.
         * Set frame duration time to minimum.
         */
        private void configureSensor() {

            mLogger.divider(DividerStyle.Weak);
            mLogger.logTrace();

            // SENSOR_SENSITIVITY
            mLogger.log(LTAG, "configureSensor: SENSOR_SENSITIVITY");

            mmMaxAnalogSensitivity = false;
            if (!isControlModeAuto() || !isControlAeModeOn()) {
                Integer maxSensitivty = mCameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);

                if (maxSensitivty != null) {
                    mmMaxAnalogSensitivity = true;
                    mCaptureRequestBuilder.set(
                            CaptureRequest.SENSOR_SENSITIVITY,
                            maxSensitivty);
                }
            }

            //--------------------------------------------------------------------------------------

            // SENSOR_EXPOSURE_TIME
            mLogger.log(LTAG, "configureSensor: SENSOR_EXPOSURE_TIME");

            mmMinExposureTimeOn = false;
            if (!isControlModeAuto() || !isControlAeModeOn()) {
                Range<Long> expTimes = mCameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);

                if (expTimes != null) {
                    mmMinExposureTimeOn = true;
                    mCaptureRequestBuilder.set(
                            CaptureRequest.SENSOR_EXPOSURE_TIME,
                            expTimes.getLower());
                }
            }

            //--------------------------------------------------------------------------------------

            // SENSOR_FRAME_DURATION
            mLogger.log(LTAG, "configureSensor: SENSOR_FRAME_DURATION");

            mmMinFrameDurationOn = false;
            if (!isControlModeAuto() || !isControlAeModeOn()) {
                mmMinFrameDurationOn = true;
                mCaptureRequestBuilder.set(
                        CaptureRequest.SENSOR_FRAME_DURATION,
                        mCaptureConfiguration.getOutputMinFrameDuration());
            }

            mLogger.log(LTAG, "configureSensor: return;");
        }
        public boolean usingMaxAnalogSensitivity() { return mmMaxAnalogSensitivity; }
        public boolean usingMinExposureTime() { return mmMinExposureTimeOn; }
        public boolean usingMinFrameDuration() { return mmMinFrameDurationOn; }

        /**
         * Report a summary of settings
         * @return a string summary
         */
        String reportSettings() {

            int     mode;
            boolean flag;
            String  report = " \n";

            report = report.concat("Flash is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.FLASH_MODE);
                switch (mode) {
                    case (CameraMetadata.FLASH_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.FLASH_MODE_SINGLE) : {
                        report = report.concat("Single");
                        break;
                    }
                    case (CameraMetadata.FLASH_MODE_TORCH) : {
                        report = report.concat("Torch");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e ) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Control mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_MODE);
                switch (mode) {
                    case (CameraMetadata.CONTROL_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.CONTROL_MODE_AUTO) : {
                        report = report.concat("Auto");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Capture intent is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_CAPTURE_INTENT);
                switch (mode) {
                    case (CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL) : {
                        report = report.concat("Manual");
                        break;
                    }
                    case (CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW) : {
                        report = report.concat("Preview");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-white balance mode is: ");
            if (isControlAwbModeOn()) {
                try {
                    mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AWB_MODE);
                    switch (mode) {
                        case (CameraMetadata.CONTROL_AWB_MODE_OFF) : {
                            report = report.concat("Off");
                            break;
                        }
                        case (CameraMetadata.CONTROL_AWB_MODE_AUTO) : {
                            report = report.concat("Auto");
                            break;
                        }
                        default : {
                            report = report.concat("Unknown");
                        }
                    }
                }
                catch (Exception e ) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-white balance lock is: ");
            if (isControlAwbLockOn()) {
                try {
                    flag = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AWB_LOCK);
                    if (flag) {
                        report = report.concat("Engaged");
                    }
                    else {
                        report = report.concat("Open");
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-focus mode is: ");
            if (isControlAfModeOn()) {
                try {
                    mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE);
                    switch (mode) {
                        case (CameraMetadata.CONTROL_AF_MODE_OFF) : {
                            report = report.concat("Off");
                            break;
                        }
                        case (CameraMetadata.CONTROL_AF_MODE_AUTO) : {
                            report = report.concat("Auto");
                            break;
                        }
                        default :
                            report = report.concat("Unknown");
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-exposure mode is: ");
            if (isControlAeModeOn()) {
                try {
                    mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_MODE);
                    switch (mode) {
                        case (CameraMetadata.CONTROL_AE_MODE_OFF) : {
                            report = report.concat("Off");
                            break;
                        }
                        case (CameraMetadata.CONTROL_AE_MODE_ON) : {
                            report = report.concat("On");
                            break;
                        }
                        default : {
                            report = report.concat("Unknown");
                        }
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-exposure lock is: ");
            if (isControlAeLockOn()) {
                try {
                    flag = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_LOCK);
                    if (flag) {
                        report = report.concat("Engaged");
                    }
                    else {
                        report = report.concat("Open");
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-exposure antibanding mode is: ");
            if (isControlAeAntibandingOn()) {
                try {
                    mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE);
                    switch (mode) {
                        case (CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF) : {
                            report = report.concat("Off");
                            break;
                        }
                        case (CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ) : {
                            report = report.concat("60 Hz");
                            break;
                        }
                        case (CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO) : {
                            report = report.concat("Auto");
                            break;
                        }
                        default : {
                            report = report.concat("Unknown");
                        }
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-exposure compensation is: ");
            if (isControlAeCompensationSet()) {
                try {
                    int value = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
                    report = report.concat(Integer.toString(value) + " [EV]");
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Auto-exposure target fps range: ");
            if (isAeTargetFpsRangeSet()) {
                try {
                    Range<Integer> range = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                    report = report.concat(range.toString());
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Disabled");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Black level lock is: ");
            try {
                flag = mCaptureRequestBuilder.get(CaptureRequest.BLACK_LEVEL_LOCK);
                if (flag) {
                    report = report.concat("Engaged");
                }
                else {
                    report = report.concat("Open");
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Color aberration correction is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE);
                switch (mode) {
                    case (CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Color correction mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.COLOR_CORRECTION_MODE);
                switch (mode) {
                    case (CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX) : {
                        report = report.concat("Transform matrix");
                        break;
                    }
                    case (CameraMetadata.COLOR_CORRECTION_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Transform matrix is: " );
            try {
                ColorSpaceTransform transform =
                        mCaptureRequestBuilder.get(CaptureRequest.COLOR_CORRECTION_TRANSFORM);
                report = report.concat(transform.toString());
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Color correction gains are: ");
            try {
                RggbChannelVector gains = mCaptureRequestBuilder.get(CaptureRequest.COLOR_CORRECTION_GAINS);
                report = report.concat(gains.toString());
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Color effect mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE);
                switch (mode) {
                    case (CameraMetadata.CONTROL_EFFECT_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Post-raw sensitivity boost is: ");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                report = report.concat("Not supported");
            }
            else {
                try {
                    int value = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST);
                    report = report.concat(Integer.toString(value));
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Video stabilization mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE);
                switch (mode) {
                    case (CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON) : {
                        report = report.concat("On");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Distortion correction mode is: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    mode = mCaptureRequestBuilder.get(CaptureRequest.DISTORTION_CORRECTION_MODE);
                    switch (mode) {
                        case (CameraMetadata.DISTORTION_CORRECTION_MODE_OFF) : {
                            report = report.concat("Off");
                            break;
                        }
                        case (CameraMetadata.DISTORTION_CORRECTION_MODE_FAST) : {
                            report = report.concat("Fast");
                            break;
                        }
                        default : {
                            report = report.concat("Unknown");
                        }
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Not supported");
            }

            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Edge mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.EDGE_MODE);
                switch (mode) {
                    case (CameraMetadata.EDGE_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.EDGE_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Hot pixel mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.HOT_PIXEL_MODE);
                switch (mode) {
                    case (CameraMetadata.HOT_PIXEL_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.HOT_PIXEL_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Noise reduction mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.NOISE_REDUCTION_MODE);
                switch (mode) {
                    case (CameraMetadata.NOISE_REDUCTION_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.NOISE_REDUCTION_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Reprocess factor is: " );
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                report = report.concat("Not supported");
            }
            else {
                try {
                    float value = mCaptureRequestBuilder.get(CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR);
                    report = report.concat(Float.toString(value));
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Shading mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.SHADING_MODE);
                switch (mode) {
                    case (CameraMetadata.SHADING_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.SHADING_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Tone-map mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.TONEMAP_MODE);
                switch (mode) {
                    case (CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE) : {
                        report = report.concat("Contrast curve");
                        break;
                    }
                    case (CameraMetadata.TONEMAP_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Tone-map contrast curve is: ");
            try {
                TonemapCurve curve = mCaptureRequestBuilder.get(CaptureRequest.TONEMAP_CURVE);
                report = report.concat(curve.toString());
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Lens aperture (f-number, f/N) is: ");
            try {
                float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_APERTURE);
                report = report.concat(Float.toString(value));
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Lens filter density is: ");
            try {
                float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_FILTER_DENSITY);
                report = report.concat(Float.toString(value) + " [EV]");
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Lens focal length: ");
            try {
                float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_FOCAL_LENGTH);
                report = report.concat(Float.toString(value) + " [mm]");
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Lens focus distance: ");
            try {
                float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_FOCUS_DISTANCE);
                report = report.concat(Float.toString(value));

                try {
                    mode = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
                    if (mode != CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED) {
                        report = report.concat(" [1/m]");
                    }
                    else {
                        report = report.concat(" [uncalibrated 1/distance]");
                    }
                }
                catch (Exception e) {
                    // do nothing
                }

            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Lens optical stabilization is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE);
                switch (mode) {
                    case (CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON) : {
                        report = report.concat("On");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Face detection mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE);
                switch (mode) {
                    case (CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Hot pixel map mode is: ");
            try {
                flag = mCaptureRequestBuilder.get(CaptureRequest.STATISTICS_HOT_PIXEL_MAP_MODE);
                if (flag) {
                    report = report.concat("On");
                }
                else {
                    report = report.concat("Off");
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Lens shading map mode is: ");
            try {
                mode = mCaptureRequestBuilder.get(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE);
                switch (mode) {
                    case (CameraMetadata.SHADING_MODE_OFF) : {
                        report = report.concat("Off");
                        break;
                    }
                    case (CameraMetadata.SHADING_MODE_FAST) : {
                        report = report.concat("Fast");
                        break;
                    }
                    default : {
                        report = report.concat("Unknown");
                    }
                }
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("OIS data mode is: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    mode = mCaptureRequestBuilder.get(CaptureRequest.STATISTICS_OIS_DATA_MODE);
                    switch(mode) {
                        case (CameraMetadata.STATISTICS_OIS_DATA_MODE_OFF) : {
                            report = report.concat("Off");
                            break;
                        }
                        case (CameraMetadata.STATISTICS_OIS_DATA_MODE_ON) : {
                            report = report.concat("On");
                            break;
                        }
                        default : {
                            report = report.concat("Unknown");
                        }
                    }
                }
                catch (Exception e) {
                    report = report.concat("Not settable");
                }
            }
            else {
                report = report.concat("Not supported");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Sensor sensitivity is: ");
            try {
                int sensitivity = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY);
                report = report.concat(Integer.toString(sensitivity) + " [ISO 12232:2006]");
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Sensor exposure time is: ");
            try {
                long   time_ns = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
                double time_us = time_ns / 1000.;
                report = report.concat(Double.toString(time_us) + " [us]");
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Sensor frame duration is: ");
            try {
                long   time_ns = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_FRAME_DURATION);
                double time_ms = time_ns / 1000. / 1000.;
                report = report.concat(Double.toString(time_ms) + " [ms]");
            }
            catch (Exception e) {
                report = report.concat("Not settable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            return report;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of abstract CameraCaptureSession class /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private CameraCaptureSession mCameraCaptureSession = new CameraCaptureSession() {

        // set in getDevice()
        private CameraDevice                            mCameraDevice;

        // set in prepare(Surface)
        private Surface                                 mSurface;

        // set in finalizeOutputConfigurations(List<OutputConfiguration>)
        private List<OutputConfiguration>               mOutputConfigs;

        // set in capture(CaptureRequest, CaptureCallback, Handler)
        // set in capture(List<CaptureRequest>, CaptureCallback, Handler)
        // set in setRepeatingRequest(CaptureRequest, CaptureCallback, Handler)
        // set in setRepeatingRequest(List<CaptureRequest>, CaptureCallback, Handler)
        private CaptureRequest                          mCaptureRequest;
        private List<CaptureRequest>                    mCaptureRequests;
        private CameraCaptureSession.CaptureCallback    mCaptureCallback;
        private Handler                                 mHandler;

        @NonNull
        @Override
        public CameraDevice getDevice() {
            return mCameraDevice;
        }

        @Override
        public void prepare(@NonNull Surface surface) throws CameraAccessException {
            mSurface = surface;
        }

        @Override
        public void finalizeOutputConfigurations(List<OutputConfiguration> outputConfigs)
                throws CameraAccessException {
            mOutputConfigs = outputConfigs;
        }

        @Override
        public int capture(@NonNull CaptureRequest request,
                           @Nullable CameraCaptureSession.CaptureCallback listener,
                           @Nullable Handler handler) throws CameraAccessException {

            mCaptureRequest  = request;
            mCaptureCallback = listener;
            mHandler         = handler;

            return 0;
        }

        @Override
        public int captureBurst(@NonNull List<CaptureRequest> requests,
                                @Nullable CameraCaptureSession.CaptureCallback listener,
                                @Nullable Handler handler) throws CameraAccessException {

            mCaptureRequests = requests;
            mCaptureCallback = listener;
            mHandler         = handler;

            return 0;
        }

        @Override
        public int setRepeatingRequest(@NonNull CaptureRequest request,
                                       @Nullable CameraCaptureSession.CaptureCallback listener,
                                       @Nullable Handler handler) throws CameraAccessException {

            mCaptureRequest  = request;
            mCaptureCallback = listener;
            mHandler         = handler;

            return 0;
        }

        @Override
        public int setRepeatingBurst(@NonNull List<CaptureRequest> requests,
                                     @Nullable CameraCaptureSession.CaptureCallback listener,
                                     @Nullable Handler handler) throws CameraAccessException {

            mCaptureRequests = requests;
            mCaptureCallback = listener;
            mHandler         = handler;

            return 0;
        }

        @Override
        public void stopRepeating() throws CameraAccessException {

        }

        @Override
        public void abortCaptures() throws CameraAccessException {

        }

        @Override
        public boolean isReprocessable() {
            return false;
        }

        @Nullable
        @Override
        public Surface getInputSurface() {
            return mSurface;
        }

        @Override
        public void close() {

        }
    };

    // access mCameraCaptureSession
    public CameraCaptureSession getCameraCaptureSession() {
        return mCameraCaptureSession;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested abstract static CaptureCallback class ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

        // set in onCaptureStarted, onCaptureProgressed, onCaptureCompleted, onCaptureFailed,
        // onCaptureSequenceCompleted, onCaptureSequenceAborted, onCaptureBufferLost
        private CameraCaptureSession    mCameraCaptureSession;

        // set in onCaptureStarted, onCaptureProgressed, onCaptureCompleted, onCaptureFailed,
        // onCaptureBufferLost
        private CaptureRequest          mCaptureRequest;

        // set in onCaptureStarted
        private long                    mTimestamp;

        // set in onCaptureStarted, onCaptureSequenceCompleted, onCaptureBufferLost
        private long                    mFrameNumber;

        // set in onCaptureProgressed
        private CaptureResult           mCaptureResult;

        // set in onCaptureCompleted
        private TotalCaptureResult      mTotalCaptureResult;

        // set in onCaptureFailed
        private CaptureFailure          mCaptureFailure;

        // set in onCaptureSequenceCompleted, onCaptureSequenceAborted
        private int                     mSequenceId;

        // set in onCaptureBufferLost
        private Surface                 mSurface;

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                     @NonNull CaptureRequest request,
                                     long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mTimestamp            = timestamp;
            mFrameNumber          = frameNumber;
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mCaptureResult        = partialResult;
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mTotalCaptureResult   = result;
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mCaptureFailure       = failure;
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                               int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);

            mCameraCaptureSession = session;
            mSequenceId           = sequenceId;
            mFrameNumber          = frameNumber;
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session,
                                             int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);

            mCameraCaptureSession = session;
            mSequenceId           = sequenceId;
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);

            mCameraCaptureSession = session;
            mCaptureRequest       = request;
            mSurface              = target;
            mFrameNumber          = frameNumber;
        }
    };

    // access mCaptureCallback
    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return mCaptureCallback;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested abstract static StateCallback class //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private CameraCaptureSession.StateCallback mStateCallback = new CameraCaptureSession.StateCallback() {

        // set in onConfigured, onConfigureFailed
        private CameraCaptureSession    mCameraCaptureSession;

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCameraCaptureSession = session;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            mCameraCaptureSession = session;
        }
    };

    // access mStateCallback
    public CameraCaptureSession.StateCallback getStateCallback() {
        return mStateCallback;
    }
}