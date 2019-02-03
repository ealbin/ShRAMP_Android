package Camera2;

import android.annotation.SuppressLint;
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

import edu.crayfis.shramp.Camera;

@TargetApi(21) // Lollipop
public class ShrampCameraCaptureSession {

    private CameraDevice            mCameraDevice;
    private CameraCharacteristics   mCameraCharacteristics;
    private StreamConfigurationMap  mStreamConfigurationMap;

    private CaptureConfiguration    mCaptureConfiguration;
    private CameraConfiguration     mCameraConfiguration;

    private List<Integer>           mCameraAbilities;
    private CaptureRequest.Builder  mCaptureRequestBuilder;


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class CaptureConfiguration {
        private List<Integer> mmCameraAbilities;
        private int           mmOutputFormat;
        private int           mmBitsPerPixel;
        private Size          mmOutputSize;
        private boolean       mmUsingManualTemplate;
        private boolean       mmUsingRawImageFormat;

        CaptureConfiguration() {
            loadCameraAbilities();
            loadStreamFormat();
        }

        private void loadCameraAbilities() {
            mmCameraAbilities = new ArrayList<>();

            assert mCameraCharacteristics != null;
            int[] abilities = mCameraCharacteristics.get(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);

            assert abilities != null;
            for (int ability : abilities) {
                mmCameraAbilities.add(ability);
            }
        }
        public List<Integer> getCameraAbilities() {
            return mmCameraAbilities;
        }

        private void loadStreamFormat() {
            assert mStreamConfigurationMap != null;
            assert mmCameraAbilities       != null;

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
        }
        public boolean isOutputFormatRaw() {
            return mmUsingRawImageFormat;
        }
        public int getOutputFormat() {
            return mmOutputFormat;
        }
        public int getBitsPerPixel() {
            return mmBitsPerPixel;
        }
        public Size getOutputSize() {
            return mmOutputSize;
        }

        public CaptureRequest.Builder getCaptureRequestBuilder() {

            int captureTemplate;
            if (mmCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                captureTemplate = CameraDevice.TEMPLATE_MANUAL;
                mmUsingManualTemplate = true;
            } else {
                // preview is guarented on all camera devices
                captureTemplate = CameraDevice.TEMPLATE_PREVIEW;
                mmUsingManualTemplate = false;
            }
            try {
                return mCameraDevice.createCaptureRequest(captureTemplate);
            } catch (CameraAccessException e) {
                // TODO EXCEPTION
            }
            return null;
        }
        public boolean usingManualCaptureRequestTemplate() {
            return mmUsingManualTemplate;
        }

        // may return 0 if this function is not implemented, otherwise in nanoseconds
        public long getOutputMinFrameDuration() {
            return mStreamConfigurationMap.getOutputMinFrameDuration(mmOutputFormat, mmOutputSize);
        }

        // additional time between frames in nanoseconds, 0 always for YUV_420_888
        public long getOutputStallDuration() {
            return mStreamConfigurationMap.getOutputStallDuration(mmOutputFormat, mmOutputSize);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class CameraConfiguration {
        private boolean mmFlashOn;
        private boolean mmControlModeAuto;
        private boolean mmCaptureIntentPreview;

        private boolean mmControlAwbModeOn;
        private boolean mmControlAwbLockOn;

        private boolean mmControlAfModeOn;

        private boolean mmControlAeModeOn;
        private boolean mmControlAeLockOn;
        private boolean mmControlAeAntibandingOn;
        private boolean mmControlAeCompensationSet;
        private boolean mmAeTargetFpsRangeSet;

        private boolean mmBlackLevelLocked;
        private boolean mmColorCorrectionAberrationOn;
        private boolean mmColorCorrectionModeTransformOn;
        private boolean mmUsingPostRawBoost;
        private boolean mmEdgeModeOn;
        private boolean mmHotPixelModeOn;
        private boolean mmNoiseReductionOn;
        private boolean mmShadingModeOn;
        private boolean mmUsingContrastCurve;

        private boolean mmOpticalStabilizationModeOn;

        CameraConfiguration() {
            assert mCameraAbilities       != null;
            assert mCaptureRequestBuilder != null;
            // reference:  https://developer.android.com/reference/android/hardware/camera2/CaptureRequest

            configureFlash();
            configureControlMode();
            configureCaptureIntent();

            // AWB and AF should be set before AE:
            // reference:  https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#CONTROL_AWB_MODE
            // reference:  https://developer.android.com/reference/android/hardware/camera2/CameraMetadata#CONTROL_AE_MODE_OFF
            configureAWB();
            configureAF();
            configureAE();

            configureCorrections();
            configureOptics();
            configureStatistics();

            // irrelevant settings:
            // CONTROL_AWB_REGIONS
            //
            // CONTROL_AF_REGIONS
            // CONTROL_AF_TRIGGER
            //
            // CONTROL_AE_REGIONS
            // CONTROL_AE_PRECAPTURE_TRIGGER
            //
            // CONTROL_ENABLE_ZSL
            // CONTROL_SCENE_MODE
            //
            // JPEG_GPS_LOCATION
            // JPEG_ORIENTATION
            // JPEG_QUALITY
            // JPEG_THUMBNAIL_QUALITY
            //
            // SCALAR_CROP_REGION
        }

        //==========================================================================================

        private void configureFlash() {
            Boolean flashInfoAvailable = mCameraCharacteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE);

            if (flashInfoAvailable == null) {
                mmFlashOn = false;
                return;
            }

            if (flashInfoAvailable) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
            mmFlashOn = false;
        }

        public boolean isFlashOn() {
            return mmFlashOn;
        }

        //==========================================================================================

        private void configureControlMode() {

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
        }

        public boolean isControlModeAuto() {
            return mmControlModeAuto;
        }

        //==========================================================================================

        private void configureCaptureIntent() {

            if (mCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_CAPTURE_INTENT,
                        CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL);
                mmCaptureIntentPreview = false;
                return;
            }

            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_CAPTURE_INTENT,
                    CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW);
            mmCaptureIntentPreview = true;
        }

        public boolean isCaptureIntentPreview() {
            return mmCaptureIntentPreview;
        }

        //==========================================================================================

        private void configureAWB() {

            // CONTROL_AWB_MODE

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

            if (isControlModeAuto() && isControlAwbModeOn()) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AWB_LOCK,
                        true);
                mmControlAwbLockOn = true;
            } else {
                mmControlAwbLockOn = false;
            }
        }

        public boolean isControlAwbModeOn() {
            return mmControlAwbModeOn;
        }

        public boolean imControlAwbLockOn() {
            return mmControlAwbLockOn;
        }

        //==========================================================================================

        private void configureAF() {

            // CONTROL_AF_MODE

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
        }

        public boolean isControlAfModeOn() {
            return mmControlAfModeOn;
        }

        //==========================================================================================

        private void configureAE() {

            // CONTROL_AE_MODE

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
        }

        public boolean isControlAeModeOn() {
            return mmControlAeModeOn;
        }

        public boolean isControlAeLockOn() {
            return mmControlAeLockOn;
        }

        public boolean isControlAeAntibandingOn() {
            return mmControlAeAntibandingOn;
        }

        public boolean isControlAeCompensationSet() {
            return mmControlAeCompensationSet;
        }

        public boolean isAeTargetFpsRangeSet() {
            return mmAeTargetFpsRangeSet;
        }

        //==========================================================================================

        private void configureCorrections() {

            // BLACK_LEVEL_LOCK

            mCaptureRequestBuilder.set(
                    CaptureRequest.BLACK_LEVEL_LOCK,
                    true);
            mmBlackLevelLocked = true;

            //--------------------------------------------------------------------------------------

            // COLOR_CORRECTION_ABERRATION_MODE

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

            if (isColorCorrectionModeTransformOn()) {
                mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS,
                        new RggbChannelVector(1, 1, 1, 1));
            }

            //--------------------------------------------------------------------------------------

            // COLOR_EFFECT_MODE

            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CameraMetadata.CONTROL_EFFECT_MODE_OFF);

            //--------------------------------------------------------------------------------------

            // CONTROL_POST_RAW_SENSITIVITY_BOOST

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

            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);

            //--------------------------------------------------------------------------------------

            // DISTORTION_CORRECTION_MODE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  // API 28
                mCaptureRequestBuilder.set(
                        CaptureRequest.DISTORTION_CORRECTION_MODE,
                        CameraMetadata.DISTORTION_CORRECTION_MODE_OFF);
            }

            //--------------------------------------------------------------------------------------

            // EDGE_MODE

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

            if (usingConstrastCurve()) {
                float[] linear_response = {0, 0, 1, 1};
                mCaptureRequestBuilder.set(CaptureRequest.TONEMAP_CURVE,
                        new TonemapCurve(linear_response, linear_response, linear_response));
            }
        }

        public boolean isBlackLevelLocked() {
            return mmBlackLevelLocked;
        }

        public boolean isColorCorrectionAberrationModeOn() {
            return mmColorCorrectionAberrationOn;
        }

        public boolean isColorCorrectionModeTransformOn() {
            return mmColorCorrectionModeTransformOn;
        }

        public boolean isUsingPostRawBoost() {
            return mmUsingPostRawBoost;
        }

        public boolean isEdgeModeOn() {
            return mmEdgeModeOn;
        }

        public boolean isHotPixelModeOn() {
            return mmHotPixelModeOn;
        }

        public boolean isNoiseReductionOn() {
            return mmNoiseReductionOn;
        }

        public boolean usingConstrastCurve() {
            return mmUsingContrastCurve;
        }

        //==========================================================================================

        private void configureOptics() {

            // LENS_APERTURE

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

            mCaptureRequestBuilder.set(
                    CaptureRequest.LENS_FOCUS_DISTANCE,
                    0.f);

            //--------------------------------------------------------------------------------------

            // LENS_OPTICAL_STABILIZATION_MODE

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
        }
        public boolean isOpticalStabilizationModeOne() {
            return mmOpticalStabilizationModeOn;
        }

        //==========================================================================================

        private void configureStatistics() {

        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ShrampCameraCaptureSession(@NonNull CameraDevice device,
                               @NonNull CameraCharacteristics characteristics) {
        mCameraDevice           = device;
        mCameraCharacteristics  = characteristics;

        mStreamConfigurationMap = mCameraCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        mCaptureConfiguration   = new CaptureConfiguration();
        mCameraAbilities        = mCaptureConfiguration.getCameraAbilities();

        mCaptureRequestBuilder  = mCaptureConfiguration.getCaptureRequestBuilder();
        mCameraConfiguration    = new CameraConfiguration();
    }




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