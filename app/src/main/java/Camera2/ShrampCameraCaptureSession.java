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
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21) // Lollipop
public class ShrampCameraCaptureSession {

    private CameraDevice            mCameraDevice;
    private CameraCharacteristics   mCameraCharacteristics;
    private StreamConfigurationMap  mStreamConfigurationMap;

    private List<Integer>           mCameraAbilities;
    private CaptureRequest.Builder  mCaptureRequestBuilder;

    private StreamFormat            mStreamFormat;
    private CameraConfiguration     mCameraConfiguration;

    private class StreamFormat {
        private int  mOutputFormat;
        private int  mBitsPerPixel;
        private Size mOutputSize;


        StreamFormat(int outputFormat, int bitsPerPixel, Size outputSize) {
            mOutputFormat = outputFormat;
            mBitsPerPixel = bitsPerPixel;
            mOutputSize   = outputSize;
        }

        public int getOutputFormat() {
            return mOutputFormat;
        }

        public int getBitsPerPixel() {
            return mBitsPerPixel;
        }

        public Size getOutputSize() {
            return mOutputSize;
        }

        // may return 0 if this function is not implemented, otherwise in nanoseconds
        public long getOutputMinFrameDuration() {
            return mStreamConfigurationMap.getOutputMinFrameDuration(mOutputFormat, mOutputSize);
        }

        // additional time between frames in nanoseconds, 0 always for YUV_420_888
        public long getOutputStallDuration() {
            return mStreamConfigurationMap.getOutputStallDuration(mOutputFormat, mOutputSize);
        }
    }

    private class CameraConfiguration {
        private boolean mFlashOn;
        private boolean mControlModeAuto;

        private boolean mControlAeModeOn;
        private boolean mControlAeLockOn;
        private boolean mControlAeAntibandingOn;
        private boolean mControlAeCompensationSet;
        private boolean mAeTargetFpsRangeSet;

        private boolean mCaptureIntentPreview;

        private boolean mBlackLevelLocked;
        private boolean mColorCorrectionAberrationOn;


        CameraConfiguration() {
            configureFlash();
            configureControlMode();
            // do AWB and FB before AE
            configureAE();
            configureCaptureIntent();
            configureCorrections();


            // temp - make warnings go away
            boolean moo;
            moo = isFlashOn();
            moo = moo && isControlModeAuto();
            moo = moo && isControlAeModeOn();
            moo = moo && isControlAeLockOn();
            moo = moo && isControlAeAntibandingOn();
            moo = moo && isControlAeCompensationSet();
            moo = moo && isAeTargetFpsRangeSet();

            moo = moo && isCaptureIntentPreview();
            moo = moo && isBlackLevelLocked();
            moo = moo && isColorCorrectionAberrationModeOn();
            if (moo) {
                moo = false;
            }
        }

        private void configureFlash() {
            Boolean flashInfoAvailable = mCameraCharacteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE);

            if (flashInfoAvailable == null) {
                mFlashOn = false;
                return;
            }

            if (flashInfoAvailable) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
            mFlashOn = false;
        }
        public boolean isFlashOn() {
            return mFlashOn;
        }

        private void configureControlMode() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AVAILABLE_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                if (controlModes.contains(CameraMetadata.CONTROL_MODE_OFF)) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_MODE,
                            CameraMetadata.CONTROL_MODE_OFF);
                    mControlModeAuto = false;
                    return;
                }
            }

            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_MODE,
                    CameraMetadata.CONTROL_MODE_AUTO);
            mControlModeAuto = true;
        }
        public boolean isControlModeAuto() {
            return mControlModeAuto;
        }

        private void configureAE() {

            if (isControlModeAuto()) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                if (controlModes.contains(CameraMetadata.CONTROL_AE_MODE_OFF)) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_MODE,
                            CameraMetadata.CONTROL_AE_MODE_OFF);
                    mControlAeModeOn = false;
                }
                else {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_MODE,
                            CameraMetadata.CONTROL_AE_MODE_ON);
                    mControlAeModeOn = true;
                }
            }
            else {
                mControlAeModeOn = false;
            }

            //--------------------------------------------------------------------------------------

            if (isControlModeAuto() && isControlAeModeOn()) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_LOCK,
                        true);
                mControlAeLockOn = true;
            }
            else {
                mControlAeLockOn = false;
            }

            //--------------------------------------------------------------------------------------

            if (isControlModeAuto() && isControlAeModeOn()) {
                int[] modes = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
                assert modes != null;

                List<Integer> controlModes = new ArrayList<>();
                for (int mode : modes) {
                    controlModes.add(mode);
                }

                if (controlModes.contains(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF)) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                            CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF);
                    mControlAeAntibandingOn = false;
                }
                else if (controlModes.contains(CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ)) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                            CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ);
                    mControlAeAntibandingOn = true;
                }
                else {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                            CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO);
                    mControlAeAntibandingOn = true;
                }
            }

            //--------------------------------------------------------------------------------------

            mControlAeCompensationSet = false;
            if (isControlAeModeOn()) {
                Range<Integer> compensationRange = mCameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                assert compensationRange != null;

                if (!compensationRange.equals(new Range<> (0,0))) {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                            compensationRange.getLower());
                    mControlAeCompensationSet = true;
                }
            }

            //--------------------------------------------------------------------------------------

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
                    long   range_product =        range.getLower() *        range.getLower();
                    long current_product = fastestRange.getLower() * fastestRange.getUpper();
                    if (range_product > current_product) {
                        fastestRange = range;
                    }
                }

                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                        fastestRange);
                mAeTargetFpsRangeSet = true;
            }
            else {
                mAeTargetFpsRangeSet = false;
            }

            //--------------------------------------------------------------------------------------


        }
        public boolean isControlAeModeOn() {
            return mControlAeModeOn;
        }
        public boolean isControlAeLockOn() {
            return mControlAeLockOn;
        }
        public boolean isControlAeAntibandingOn() {
            return mControlAeAntibandingOn;
        }
        public boolean isControlAeCompensationSet() {
            return mControlAeCompensationSet;
        }
        public boolean isAeTargetFpsRangeSet() {
            return mAeTargetFpsRangeSet;
        }

        private void configureCaptureIntent() {
            if (mCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_CAPTURE_INTENT,
                        CameraMetadata.CONTROL_CAPTURE_INTENT_MANUAL);
                mCaptureIntentPreview = false;
                return;
            }

            mCaptureRequestBuilder.set(
                    CaptureRequest.CONTROL_CAPTURE_INTENT,
                    CameraMetadata.CONTROL_CAPTURE_INTENT_PREVIEW);
            mCaptureIntentPreview = true;
        }
        public boolean isCaptureIntentPreview() {
            return mCaptureIntentPreview;
        }

        private void configureCorrections() {
            mCaptureRequestBuilder.set(
                    CaptureRequest.BLACK_LEVEL_LOCK,
                    true);
            mBlackLevelLocked = true;

            //--------------------------------------------------------------------------------------

            int[] modes = mCameraCharacteristics.get(
                    CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES);
            assert modes != null;

            List<Integer> aberrationModes = new ArrayList<>();
            for (int mode : modes) {
                aberrationModes.add(mode);
            }

            if (aberrationModes.contains(CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF)) {
                mCaptureRequestBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                        CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF);
                mColorCorrectionAberrationOn = false;
            }
            else {
                mCaptureRequestBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                        CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_FAST);
                mColorCorrectionAberrationOn = true;
            }

            //--------------------------------------------------------------------------------------

            //color_correction_mode



        }
        public boolean isBlackLevelLocked() {
            return mBlackLevelLocked;
        }
        public boolean isColorCorrectionAberrationModeOn() {
            return mColorCorrectionAberrationOn;
        }
    }

    ShrampCameraCaptureSession(@NonNull CameraDevice device,
                               @NonNull CameraCharacteristics characteristics) {
        mCameraDevice           = device;
        mCameraCharacteristics  = characteristics;
        mStreamConfigurationMap = mCameraCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        getCameraAbilities();
        configureStreamFormat();
        configureCaptureRequestBuilder();

        mCameraConfiguration = new CameraConfiguration();
    }

    private void getCameraAbilities() {
        mCameraAbilities = new ArrayList<Integer>();

        assert mCameraCharacteristics != null;
        int[] abilities = mCameraCharacteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);

        assert abilities != null;
        for (int ability : abilities) {
            mCameraAbilities.add(ability);
        }
    }

    private void configureStreamFormat() {
        assert mCameraAbilities        != null;
        assert mStreamConfigurationMap != null;

        int outputFormat;
        if (mCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
            outputFormat = ImageFormat.RAW_SENSOR;
        }
        else {
            outputFormat = ImageFormat.YUV_420_888;
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

        mStreamFormat = new StreamFormat(outputFormat, bitsPerPixel, outputSize);
    }

    private void configureCaptureRequestBuilder() {

        int captureTemplate;
        if (mCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
            captureTemplate = CameraDevice.TEMPLATE_MANUAL;
        } else {
            // preview is guarented on all camera devices
            captureTemplate = CameraDevice.TEMPLATE_PREVIEW;
        }
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(captureTemplate);
        } catch (CameraAccessException e) {
            // TODO EXCEPTION
        }
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

    /*
    private enum CameraAbility {
                // specified in CameraMetdata
                BACKWARD_COMPATIBLE,            // API 21
                BURST_CAPTURE,                  // API 22
                CONSTRAINED_HIGH_SPEED_VIEDO,   // API 23
                DEPTH_OUTPUT,                   // API 23
                LOGICAL_MULTICAMERA,            // API 28
                MANUAL_POST_PROCESSING,         // API 21
                MANUAL_SENSOR,                  // API 21
                MONOCHROME,                     // API 28
                MOTION_TRACKING,                // API 28
                PRIVATE_REPROCESSING,           // API 23
                RAW,                            // API 21
                READ_SENSOR_SETTINGS,           // API 22
                YUV_REPROCESSING,               // API 23
                }
    */
    //private TreeMap<CameraAbility, Boolean> mCameraHasAbility;
    /*
    private void getCameraAbilities() {
        mCameraHasAbility = new TreeMap<CameraAbility, Boolean>();

        assert mCameraCharacteristics != null;
        int[] abilities = mCameraCharacteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        assert abilities != null;

        for (int ability : abilities) {
            switch (ability) {
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) : {
                    mCameraHasAbility.put(CameraAbility.BACKWARD_COMPATIBLE, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE) : {
                    mCameraHasAbility.put(CameraAbility.BURST_CAPTURE, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) : {
                    mCameraHasAbility.put(CameraAbility.CONSTRAINED_HIGH_SPEED_VIEDO, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) : {
                    mCameraHasAbility.put(CameraAbility.DEPTH_OUTPUT, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA) : {
                    mCameraHasAbility.put(CameraAbility.LOGICAL_MULTICAMERA, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING) : {
                    mCameraHasAbility.put(CameraAbility.MANUAL_POST_PROCESSING, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) : {
                    mCameraHasAbility.put(CameraAbility.MANUAL_SENSOR, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME) : {
                    mCameraHasAbility.put(CameraAbility.MONOCHROME, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING) : {
                    mCameraHasAbility.put(CameraAbility.MOTION_TRACKING, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING) : {
                    mCameraHasAbility.put(CameraAbility.PRIVATE_REPROCESSING, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW) : {
                    mCameraHasAbility.put(CameraAbility.RAW, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS) : {
                    mCameraHasAbility.put(CameraAbility.READ_SENSOR_SETTINGS, true);
                    break;
                }
                case (CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING) : {
                    mCameraHasAbility.put(CameraAbility.YUV_REPROCESSING, true);
                    break;
                }
                default : {
                    // TODO ERROR
                }
            }
        }

        for (CameraAbility ability : CameraAbility.values()) {
            if (!mCameraHasAbility.containsKey(ability)) {
                mCameraHasAbility.put(ability, false);
            }
        }
    }
    */