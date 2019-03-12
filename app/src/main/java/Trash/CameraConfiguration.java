package Trash;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Build;
import android.util.Range;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static Trash.ShrampCamSetup.mLogger;

////////////////////////////////////////////////////////////////////////////////////////////////
// implementation of nested class CameraConfiguration
////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Helper class to encapsulate the process of configuring the capture session.
 * It's meant to be a nested class of ShrampCamSetup, but it's so long it deserves it's own file.
 */
final class CameraConfiguration {

    //******************************************************************************************
    // Class Variables
    //----------------

    // reference back to owning ShrampCamSetup object
    final ShrampCamSetup mSetup;

    // and class needed variables
    private List<Integer>                          mCameraAbilities;
    private CameraCharacteristics                  mCameraCharacteristics;
    private ShrampCamSetup.CaptureConfiguration mCaptureConfiguration;
    private CaptureRequest.Builder                 mCaptureRequestBuilder;

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

    //******************************************************************************************
    // Class Methods
    //--------------

    /**
     * This is a biggie..
     * reference:  https://developer.android.com/reference/android/hardware/camera2/CaptureRequest
     */
    CameraConfiguration(ShrampCamSetup setup) {

        mSetup                 = setup;
        mCameraAbilities       = setup.getCameraAbilities();
        mCameraCharacteristics = setup.getCameraCharacteristics();
        mCaptureConfiguration  = setup.getCaptureConfiguration();
        mCaptureRequestBuilder = setup.getCaptureRequestBuilder();

        assert mCameraAbilities       != null;
        assert mCameraCharacteristics != null;
        assert mCaptureConfiguration  != null;
        assert mCaptureRequestBuilder != null;

        mLogger.log("Configuring flash");
        configureFlash();

        mLogger.log("Configuring control mode");
        configureControlMode();

        mLogger.log("Configuring capture intent");
        configureCaptureIntent();

        // Note: auto-white balance and auto-focus should be set before auto-exposure
        // reference:
        //  https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#CONTROL_AWB_MODE
        //  https://developer.android.com/reference/android/hardware/camera2/CameraMetadata#CONTROL_AE_MODE_OFF
        mLogger.log("Configuring auto-white balance");
        configureAWB();

        mLogger.log("Configuring auto-focus");
        configureAF();

        mLogger.log("Configuring auto-exposure");
        configureAE();

        mLogger.log("Configuring corrections");
        configureCorrections();

        mLogger.log("Configuring optics");
        configureOptics();

        mLogger.log("Configuring statistics");
        configureStatistics();

        mLogger.log("Configuring sensor");
        configureSensor();

        mLogger.log("Irrelevant setting: CONTROL_AWB_REGIONS");
        mLogger.log("Irrelevant setting: CONTROL_AF_REGIONS");
        mLogger.log("Irrelevant setting: CONTROL_AF_TRIGGER");
        mLogger.log("Irrelevant setting: CONTROL_AE_REGIONS");
        mLogger.log("Irrelevant setting: CONTROL_AE_PRECAPTURE_TRIGGER");
        mLogger.log("Irrelevant setting: CONTROL_ENABLE_ZSL");
        mLogger.log("Irrelevant setting: CONTROL_SCENE_MODE");
        mLogger.log("Irrelevant setting: JPEG_GPS_LOCATION");
        mLogger.log("Irrelevant setting: JPEG_ORIENTATION");
        mLogger.log("Irrelevant setting: JPEG_QUALITY");
        mLogger.log("Irrelevant setting: JPEG_THUMBNAIL_QUALITY");
        mLogger.log("Irrelevant setting: SCALAR_CROP_REGION");
        mLogger.log("Irrelevant setting: SENSOR_TEST_PATTERN_MODE");
        mLogger.log("Irrelevant setting: SENSOR_TEST_PATTERN_DATA");

        mLogger.log("return;");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure flash for capture session (turn off)
     */
    private void configureFlash() {

        mLogger.log("FLASH_MODE");
        Boolean flashInfoAvailable = mCameraCharacteristics.get(
                CameraCharacteristics.FLASH_INFO_AVAILABLE);

        if (flashInfoAvailable == null) {
            mmFlashOn = false;
            mLogger.log("No flash info available; return;");
            return;
        }

        if (flashInfoAvailable) {
            mCaptureRequestBuilder.set(
                    CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        }
        mmFlashOn = false;

        mLogger.log("return;");
    }

    /**
     * Is the flash on?
     * @return true if yes, false if no
     */
    public boolean isFlashOn() { return mmFlashOn; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure control mode for capture session (mode off if possible, otherwise auto)
     */
    private void configureControlMode() {

        mLogger.log("CONTROL_MODE");
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

        mLogger.log("return;");
    }

    /**
     * Is the control mode set to auto?
     * @return true if yes, false if manual
     */
    public boolean isControlModeAuto() { return mmControlModeAuto; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure capture intent for capture session (manual if possible, otherwise preview)
     */
    private void configureCaptureIntent() {

        mLogger.log("CONTROL_CAPTURE_INTENT");
        if (mCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_CAPTURE_INTENT,
                    //CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL); //TODO UNCOMMENT
                    CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW);
            mmCaptureIntentPreview = false;

            mLogger.log("return;");
            return;
        }

        mCaptureRequestBuilder.set(
                CaptureRequest.CONTROL_CAPTURE_INTENT,
                CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW);
        mmCaptureIntentPreview = true;

        mLogger.log("return;");
    }

    /**
     * Is the capture intent set to preview?
     * @return true if yes, false if manual
     */
    public boolean isCaptureIntentPreview() { return mmCaptureIntentPreview; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure auto-white balance for capture session.
     * Turn auto-white balance mode off if possible, auto otherwise.
     * Lock auto-white levels if possible.
     */
    private void configureAWB() {

        // CONTROL_AWB_MODE
        mLogger.log("CONTROL_AWB_MODE");

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

        //------------------------------------------------------------------------------------------

        // CONTROL_AWB_LOCK
        mLogger.log("CONTROL_AWB_LOCK");

        if (isControlModeAuto() && isControlAwbModeOn()) {
            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_AWB_LOCK,
                    true);
            mmControlAwbLockOn = true;
        } else {
            mmControlAwbLockOn = false;
        }

        mLogger.log("return;");
    }

    /**
     * Is the auto-white balance mode set to auto?
     * @return true if yes, false if off
     */
    public boolean isControlAwbModeOn() { return mmControlAwbModeOn; }

    /**
     * Is the auto-white balance lock engaged?
     * @return true if yes, false if no
     */
    public boolean isControlAwbLockOn() { return mmControlAwbLockOn; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure auto-focus for capture session.
     * Turn auto-focus mode off if possible, auto otherwise.
     */
    private void configureAF() {

        // CONTROL_AF_MODE
        mLogger.log("CONTROL_AF_MODE");

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

        mLogger.log("return;");
    }

    /**
     * Is the auto-focus mode set to auto?
     * @return true if yes, false if off
     */
    public boolean isControlAfModeOn() { return mmControlAfModeOn; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure auto-exposure for capture session.
     * Turn auto-exposure mode off if possible, auto otherwise.
     * Lock exposure setting if possible.
     * Disable anti-banding abilities if possible, otherwise set to 60 Hz, lastly auto.
     * Disable exposure compensation or minimize it if possible.
     * Set exposure to fastest fps
     */
    private void configureAE() {

        // CONTROL_AE_MODE
        mLogger.log("CONTROL_AE_MODE");

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

        //------------------------------------------------------------------------------------------

        // CONTROL_AE_LOCK
        mLogger.log("CONTROL_AE_LOCK");

        if (isControlModeAuto() && isControlAeModeOn()) {
            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_LOCK,
                    true);
            mmControlAeLockOn = true;
        } else {
            mmControlAeLockOn = false;
        }

        //------------------------------------------------------------------------------------------

        // CONTROL_AE_ANTIBANDING_MODE
        mLogger.log("CONTROL_AE_ANTIBANDING_MODE");

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

        //------------------------------------------------------------------------------------------

        // CONTROL_AE_EXPOSURE_COMPENSATION
        mLogger.log("CONTROL_AE_EXPOSURE_COMPENSATION");

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

        //------------------------------------------------------------------------------------------

        // CONTROL_AE_TARGET_FPS
        mLogger.log("CONTROL_AE_TARGET_FPS");

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

        mLogger.log("return;");
    }

    /**
     * Is the auto-exposure mode set to auto?
     * @return true if yes, false if off
     */
    public boolean isControlAeModeOn() { return mmControlAeModeOn; }

    /**
     * Is the auto-exposure lock engaged?
     * @return true if yes, false if off
     */
    public boolean isControlAeLockOn() { return mmControlAeLockOn; }

    /**
     * Is auto-exposure antibanding on?
     * @return true if auto or 60 Hz, false if off
     */
    public boolean isControlAeAntibandingOn() { return mmControlAeAntibandingOn; }

    /**
     * Is auto-exposure compensation set?
     * @return true if yes, false if no
     */
    public boolean isControlAeCompensationSet() { return mmControlAeCompensationSet; }

    /**
     * Is auto-exposure target frames-per-second range set?
     * @return true if yes, false if no
     */
    public boolean isAeTargetFpsRangeSet() { return mmAeTargetFpsRangeSet; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

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

        // BLACK_LEVEL_LOCK
        mLogger.log("BLACK_LEVEL_LOCK");

        mCaptureRequestBuilder.set(
                CaptureRequest.BLACK_LEVEL_LOCK,
                true);
        mmBlackLevelLocked = true;

        //------------------------------------------------------------------------------------------

        // COLOR_CORRECTION_ABERRATION_MODE
        mLogger.log("COLOR_CORRECTION_ABERRATION_MODE");

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

        //------------------------------------------------------------------------------------------

        // COLOR_CORRECTION_MODE
        mLogger.log("COLOR_CORRECTION_MODE");

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

        //------------------------------------------------------------------------------------------

        // COLOR_CORRECTION_TRANSFORM
        mLogger.log("COLOR_CORRECTION_TRANSFORM");

        if (isColorCorrectionModeTransformOn()) {
            mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM,
                    new ColorSpaceTransform(new int[]{
                            1, 1, 0, 1, 0, 1,    // 1/1 , 0/1 , 0/1 = 1 0 0
                            0, 1, 1, 1, 0, 1,    // 0/1 , 1/1 , 0/1 = 0 1 0
                            0, 1, 0, 1, 1, 1     // 0/1 , 0/1 , 1/1 = 0 0 1
                    }));
        }

        //------------------------------------------------------------------------------------------

        // COLOR_CORRECTION_GAINS
        mLogger.log("COLOR_CORRECTION_GAINS");

        if (isColorCorrectionModeTransformOn()) {
            mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS,
                    new RggbChannelVector(1, 1, 1, 1));
        }

        //------------------------------------------------------------------------------------------

        // COLOR_EFFECT_MODE
        mLogger.log("COLOR_EFFECT_MODE");

        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                CameraMetadata.CONTROL_EFFECT_MODE_OFF);

        //------------------------------------------------------------------------------------------

        // CONTROL_POST_RAW_SENSITIVITY_BOOST
        mLogger.log("CONTROL_POST_RAW_SENSITIVITY_BOOST");

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

        //------------------------------------------------------------------------------------------

        // CONTROL_VIDEO_STABILIZATION_MODE
        mLogger.log("CONTROL_VIDEO_SABILIZATION_MODE");

        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);

        //------------------------------------------------------------------------------------------

        // DISTORTION_CORRECTION_MODE
        mLogger.log("DISTORTION_CORRECTION_MODE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  // API 28
            mCaptureRequestBuilder.set(
                    CaptureRequest.DISTORTION_CORRECTION_MODE,
                    CameraMetadata.DISTORTION_CORRECTION_MODE_OFF);
        }

        //------------------------------------------------------------------------------------------

        // EDGE_MODE
        mLogger.log("EDGE_MODE");

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

        //------------------------------------------------------------------------------------------

        // HOT_PIXEL_MODE
        mLogger.log("HOT_PIXEL_MODE");

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

        //------------------------------------------------------------------------------------------

        // NOISE_REDUCTION_MODE
        mLogger.log("NOISE_REDUCTION_MODE");

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

        //------------------------------------------------------------------------------------------

        // REPROCESS_EFFECTIVE_EXPOSURE_FACTOR
        mLogger.log("REPROCESS_EFFECTIVE_EXPOSURE_FACTOR");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23
            if (mCameraAbilities.contains(
                    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING)) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR,
                        1.f);
            }
        }

        //------------------------------------------------------------------------------------------

        // SHADING_MODE
        mLogger.log("SHADING_MODE");

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

        //------------------------------------------------------------------------------------------

        // TONEMAP_MODE
        mLogger.log("TONEMAP_MODE");

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

        //------------------------------------------------------------------------------------------

        // TONEMAP_CURVE
        mLogger.log("TONEMAP_CURVE");

        if (usingConstrastCurve()) {
            float[] linear_response = {0, 0, 1, 1};
            mCaptureRequestBuilder.set(CaptureRequest.TONEMAP_CURVE,
                    new TonemapCurve(linear_response, linear_response, linear_response));
        }

        mLogger.log("return;");
    }

    /**
     * Is black level locked?
     * @return true if yes, false if no
     */
    public boolean isBlackLevelLocked() { return mmBlackLevelLocked; }

    /**
     * Is color correction for aberration on?
     * @return true if yes, false if no
     */
    public boolean isColorCorrectionAberrationModeOn() { return mmColorCorrectionAberrationOn; }

    /**
     * Is a color correction transform being used?
     * @return true if unity transform, false if fast default transform
     */
    public boolean isColorCorrectionModeTransformOn() { return mmColorCorrectionModeTransformOn; }

    /**
     * Is there a post-raw boost being applied?
     * @return true if unity, false if doesn't apply to this device
     */
    public boolean isUsingPostRawBoost() { return mmUsingPostRawBoost; }

    /**
     * Is edge correction being used?
     * @return true if fast default correction, false if off
     */
    public boolean isEdgeModeOn() { return mmEdgeModeOn; }

    /**
     * Is hotpixel removal being used?
     * @return true if fast default correction, false if off
     */
    public boolean isHotPixelModeOn() { return mmHotPixelModeOn; }

    /**
     * Is noise reduction on?
     * @return true if yes, false if no
     */
    public boolean isNoiseReductionOn() { return mmNoiseReductionOn; }

    /**
     * Is using a custom contrast curve?
     * @return true if yes, false if fast default
     */
    public boolean usingConstrastCurve() { return mmUsingContrastCurve; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure optics for capture session.
     * Try to set for minimum aperture (darkest).
     * Try to set for maximum filter density (darkest).
     * Try to set for maximum focal length (blurriest).
     * Set focal distance to infinity (defocus from near surfaces).
     * Try to disable optical stabilization.
     */
    private void configureOptics() {

        // LENS_APERTURE
        mLogger.log("LENS_APERTURE");

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

        //------------------------------------------------------------------------------------------

        // LENS_FILTER_DENSITY
        mLogger.log("LENS_FILTER_DENSITY");

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

        //------------------------------------------------------------------------------------------

        // LENS_FOCAL_LENGTH
        mLogger.log("LENS_FOCAL_LENGTH");

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

        //------------------------------------------------------------------------------------------

        // LENS_FOCUS_DISTANCE
        mLogger.log("LENS_FOCUS_DISTANCE");

        mCaptureRequestBuilder.set(
                CaptureRequest.LENS_FOCUS_DISTANCE,
                0.f);

        //------------------------------------------------------------------------------------------

        // LENS_OPTICAL_STABILIZATION_MODE
        mLogger.log("LENS_OPTICAL_STABILIZATION_MODE");

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

        mLogger.log("return;");
    }

    /**
     * Is optical stabilization being used?
     * @return true if yes, false if no
     */
    public boolean isOpticalStabilizationModeOne() { return mmOpticalStabilizationModeOn; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure statistics for capture session.
     * Disable face detection.
     * Disable hot pixel maps.
     * Disable lens shading maps.
     * Disable OIS data.
     */
    private void configureStatistics() {

        // STATISTICS_FACE_DETECT_MODE
        mLogger.log("STATISTICS_FACE_DETECT_MODE");

        mmFaceDetectModeOn = false;
        mCaptureRequestBuilder.set(
                CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF);

        //------------------------------------------------------------------------------------------

        // STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES
        mLogger.log("STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES");

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

        //------------------------------------------------------------------------------------------

        // STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES
        mLogger.log("STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES");

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

        //------------------------------------------------------------------------------------------

        // STATISTICS_OIS_DATA_MODE
        mLogger.log("STATISTICS_OIS_DATA_MODE");

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

        mLogger.log("return;");
    }

    /**
     * Is face detection being used?
     * @return true if yes, false if no
     */
    public boolean isFaceDetectModeOn() { return mmFaceDetectModeOn; }

    /**
     * Is a hot pixel map being made?
     * @return true if yes, false if no
     */
    public boolean isHotPixelMapModeOn() { return mmHotPixelMapModeOn; }

    /**
     * Is lens shading being mapped?
     * @return true if yes, false if no
     */
    public boolean isLensShadingMapModeOn() { return mmLensShadingMapModeOn; }

    /**
     * Is optical stabilization position information being recorded?
     * @return true if yes, false if no
     */
    public boolean isOisDataModeOn() { return mmOisDataModeOn; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configure sensor for capture session.
     * Set maximum analog sensitivity (no digital gain).
     * Set exposure time to minimum.
     * Set frame duration time to minimum.
     */
    private void configureSensor() {

        // SENSOR_SENSITIVITY
        mLogger.log("SENSOR_SENSITIVITY");

        Integer maxAnalogSensitivty;

        mmMaxAnalogSensitivity = false;
        if (!isControlModeAuto() || !isControlAeModeOn()) {
            Integer maxAnalogSensitivity = mCameraCharacteristics.get(
                    CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);

            Range<Integer> sensitivityRange = mCameraCharacteristics.get(
                    CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);

            if (sensitivityRange != null) {
                Integer maxSensitivty = sensitivityRange.getUpper();
                mmMaxAnalogSensitivity = true;
                mCaptureRequestBuilder.set(
                        CaptureRequest.SENSOR_SENSITIVITY,
                        maxSensitivty);
            }
        }

        //------------------------------------------------------------------------------------------

        // SENSOR_EXPOSURE_TIME
        mLogger.log("SENSOR_EXPOSURE_TIME");

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

        //------------------------------------------------------------------------------------------

        // SENSOR_FRAME_DURATION
        mLogger.log("SENSOR_FRAME_DURATION");

        mmMinFrameDurationOn = false;
        if (!isControlModeAuto() || !isControlAeModeOn()) {
            mmMinFrameDurationOn = true;
            mCaptureRequestBuilder.set(
                    CaptureRequest.SENSOR_FRAME_DURATION,
                    mCaptureConfiguration.getOutputMinFrameDuration());
        }

        mLogger.log("return;");
    }

    /**
     * Has analog sensitivity been set to maximum?
     * @return true if yes, false if no
     */
    public boolean usingMaxAnalogSensitivity() { return mmMaxAnalogSensitivity; }

    /**
     * Is the exposure time set to minimum?
     * @return true if yes, false if no
     */
    public boolean usingMinExposureTime() { return mmMinExposureTimeOn; }

    /**
     * Is the frame duration set to minimum?
     * @return true if yes, false if no
     */
    public boolean usingMinFrameDuration() { return mmMinFrameDurationOn; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Report a summary of settings
     * @return a string summary
     */
    String reportSettings() {

        int     mode;
        boolean flag;
        String  report = " \n";

        report = report.concat("Flash_ is: ");
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

        //------------------------------------------------------------------------------------------

        report = report.concat("step01_Control_ mode is: ");
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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

        report = report.concat("Color_ aberration correction is: ");
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

        //------------------------------------------------------------------------------------------

        report = report.concat("Color_ correction mode is: ");
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

        //------------------------------------------------------------------------------------------

        report = report.concat("Transform matrix is: " );
        try {
            ColorSpaceTransform transform =
                    mCaptureRequestBuilder.get(CaptureRequest.COLOR_CORRECTION_TRANSFORM);

            double rr = transform.getElement(0,0).doubleValue();
            double rg = transform.getElement(1,0).doubleValue();
            double rb = transform.getElement(2,0).doubleValue();

            double gr = transform.getElement(0,1).doubleValue();
            double gg = transform.getElement(1,1).doubleValue();
            double gb = transform.getElement(2,1).doubleValue();

            double br = transform.getElement(0,2).doubleValue();
            double bg = transform.getElement(1,2).doubleValue();
            double bb = transform.getElement(2,2).doubleValue();

            DecimalFormat df = new DecimalFormat("#.##");

            String r = "( " + df.format(rr) + " " + df.format(rg) + " " + df.format(rb) + " ),";
            String g = "( " + df.format(gr) + " " + df.format(gg) + " " + df.format(gb) + " ),";
            String b = "( " + df.format(br) + " " + df.format(bg) + " " + df.format(bb) + " )";

            report = report.concat(r + g + b);
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Color_ correction gains are: ");
        try {
            RggbChannelVector gains = mCaptureRequestBuilder.get(CaptureRequest.COLOR_CORRECTION_GAINS);

            DecimalFormat df = new DecimalFormat("#.##");

            String b  = df.format(gains.getBlue());
            String ge = df.format(gains.getGreenEven());
            String go = df.format(gains.getGreenOdd());
            String r  = df.format(gains.getRed());

            report = report.concat("( " + b + " " + ge + " " + go + " " + r + " )");
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Color_ effect mode is: ");
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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

        report = report.concat("Distortion_ correction mode is: ");
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

        //------------------------------------------------------------------------------------------

        report = report.concat("Edge_ mode is: ");
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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

        report = report.concat("Tone-map contrast curve is: ");
        try {
            TonemapCurve curve = mCaptureRequestBuilder.get(CaptureRequest.TONEMAP_CURVE);
            String test = "TonemapCurve{R:[(0.0, 0.0), (1.0, 1.0)], G:[(0.0, 0.0), (1.0, 1.0)], "
                        + "B:[(0.0, 0.0), (1.0, 1.0)]}";
            if (test.equals(curve.toString())) {
                report = report.concat("Linear");
            }
            else {
                report = report.concat("Non-linear");
            }
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Lens aperture (f-number, f/N) is: ");
        try {
            float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_APERTURE);
            report = report.concat(Float.toString(value));
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Lens filter density is: ");
        try {
            float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_FILTER_DENSITY);
            report = report.concat(Float.toString(value) + " [EV]");
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Lens focal length: ");
        try {
            float value = mCaptureRequestBuilder.get(CaptureRequest.LENS_FOCAL_LENGTH);
            report = report.concat(Float.toString(value) + " [mm]");
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------

        report = report.concat("Sensor sensitivity is: ");
        try {
            int sensitivity = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY);
            report = report.concat(Integer.toString(sensitivity) + " [ISO 12232:2006]");
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Sensor exposure time is: ");
        try {
            long   time_ns = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
            double time_us = time_ns / 1000.;

            DecimalFormat df = new DecimalFormat("#.###");
            report = report.concat(df.format(time_us) + " [us]");
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        report = report.concat("Sensor frame duration is: ");
        try {
            long time_ns = mCaptureRequestBuilder.get(CaptureRequest.SENSOR_FRAME_DURATION);
            if (time_ns == 0L) {
                report = report.concat("Not settable");
            }
            else {
                double time_ms = time_ns / 1000. / 1000.;

                DecimalFormat df = new DecimalFormat("#.###");
                report = report.concat(df.format(time_ms) + " [ms]");
            }
        }
        catch (Exception e) {
            report = report.concat("Not settable");
        }
        report = report.concat("\n");

        //------------------------------------------------------------------------------------------

        return report;
    }
}
