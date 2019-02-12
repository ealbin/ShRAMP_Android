package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.RggbChannelVector;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level10_Color extends Level09_MiscControls {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer             mColorCorrectionAberrationMode;
    private   String              mColorCorrectionAberrationModeName;

    protected RggbChannelVector   mColorCorrectionGains;
    private   String              mColorCorrectionGainsName;

    protected Integer             mColorCorrectionMode;
    private   String              mColorCorrectionModeName;

    protected ColorSpaceTransform mColorCorrectionTransform;
    private   String              mColorCorrectionTransformName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level10_Color(@NonNull CameraCharacteristics characteristics,
                            @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setColorCorrectionAberrationMode();
        setColorCorrectionGains();
        setColorCorrectionMode();
        setColorCorrectionTransform();
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
    private void setColorCorrectionAberrationMode() {
        CaptureRequest.Key key = CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE;
        int modeOff  = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF;
        int modeFast = CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_FAST;
        /*
         * Added in API 21
         *
         * Mode of operation for the chromatic aberration correction algorithm.
         *
         * Chromatic (color) aberration is caused by the fact that different wavelengths of light
         * can not focus on the same point after exiting from the lens. This metadata defines the
         * high level control of chromatic aberration correction algorithm, which aims to minimize
         * the chromatic artifacts that may occur along the object boundaries in an image.
         *
         * FAST/HIGH_QUALITY both mean that camera device determined aberration correction will be
         * applied. HIGH_QUALITY mode indicates that the camera device will use the highest-quality
         * aberration correction algorithms, even if it slows down capture rate. FAST means the
         * camera device will not slow down capture rate when applying aberration correction.
         *
         * LEGACY devices will always be in FAST mode.
         *
         * This key is available on all devices.
         */
        if (!super.mRequestKeys.contains(key)) {
            mColorCorrectionAberrationMode     = null;
            mColorCorrectionAberrationModeName = "Not supported";
            return;
        }

        // Default
        mColorCorrectionAberrationMode     = modeFast;
        mColorCorrectionAberrationModeName = "Fast";
        /*
         * Added in API 21
         *
         * Aberration correction will not slow down capture rate relative to sensor raw output.
         */
        List<Integer> modes = super.getAvailable(
                              CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES);
        if (modes.contains(modeOff)) {
            mColorCorrectionAberrationMode = modeOff;
            mColorCorrectionAberrationModeName = "Off";
            /*
             * Added in API 21
             *
             * No aberration correction is applied.
             */
        }

        super.mCaptureRequestBuilder.set(key, mColorCorrectionAberrationMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setColorCorrectionGains() {
        CaptureRequest.Key key = CaptureRequest.COLOR_CORRECTION_GAINS;
        /*
         * Added in API 21
         *
         * Gains applying to Bayer raw color channels for white-balance.
         *
         * These per-channel gains are either set by the camera device when the request
         * android.colorCorrection.mode is not TRANSFORM_MATRIX, or directly by the application
         * in the request when the android.colorCorrection.mode is TRANSFORM_MATRIX.
         *
         * The gains in the result metadata are the gains actually applied by the camera device to
         * the current frame.
         *
         * The valid range of gains varies on different devices, but gains between [1.0, 3.0]
         * are guaranteed not to be clipped. Even if a given device allows gains below 1.0,
         * this is usually not recommended because this can create color artifacts.
         *
         * Units: Unitless gain factors
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mColorCorrectionGains     = null;
            mColorCorrectionGainsName = "Not supported";
            return;
        }

        if (super.mControlMode != CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX) {
            mColorCorrectionGains     = null;
            mColorCorrectionGainsName = "Disabled";
            return;
        }

        mColorCorrectionGains     = new RggbChannelVector(1, 1, 1, 1);
        mColorCorrectionGainsName = "(1 1 1 1)";

        super.mCaptureRequestBuilder.set(key, mColorCorrectionGains);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setColorCorrectionMode() {
        CaptureRequest.Key key = CaptureRequest.COLOR_CORRECTION_MODE;
        int modeMatrix  = CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX;
        int modeFast    = CameraMetadata.COLOR_CORRECTION_MODE_FAST;
        /*
         * Added in API 21
         *
         * The mode control selects how the image data is converted from the sensor's native color
         * into linear sRGB color.
         *
         * When auto-white balance (AWB) is enabled with android.control.awbMode, this control is
         * overridden by the AWB routine. When AWB is disabled, the application controls how the
         * color mapping is performed.
         *
         * We define the expected processing pipeline below. For consistency across devices, this
         * is always the case with TRANSFORM_MATRIX.
         *
         * When either FULL or HIGH_QUALITY is used, the camera device may do additional processing
         * but android.colorCorrection.gains and android.colorCorrection.transform will still be
         * provided by the camera device (in the results) and be roughly correct.
         *
         * Switching to TRANSFORM_MATRIX and using the data provided from FAST or HIGH_QUALITY will
         * yield a picture with the same white point as what was produced by the camera device in
         * the earlier frame.
         *
         * The white balance is encoded by two values, a 4-channel white-balance gain vector
         * (applied in the Bayer domain), and a 3x3 color transform matrix (applied after demosaic).
         *
         * The 4-channel white-balance gains are defined as:
         *      android.colorCorrection.gains = [ R G_even G_odd B ]
         *
         * where G_even is the gain for green pixels on even rows of the output, and G_odd is the
         * gain for green pixels on the odd rows. These may be identical for a given camera device
         * implementation; if the camera device does not support a separate gain for even/odd green
         * channels, it will use the G_even value, and write G_odd equal to G_even in the output
         * result metadata.
         *
         * The matrices for color transforms are defined as a 9-entry vector:
         *      android.colorCorrection.transform = [ I0 I1 I2 I3 I4 I5 I6 I7 I8 ]
         *
         * which define a transform from input sensor colors, P_in = [ r g b ], to output linear
         * sRGB, P_out = [ r' g' b' ],
         *
         * with colors as follows:
         *      r' = I0r + I1g + I2b
         *      g' = I3r + I4g + I5b
         *      b' = I6r + I7g + I8b
         *
         * Both the input and output value ranges must match. Overflow/underflow values are clipped
         * to fit within the range
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mColorCorrectionMode     = null;
            mColorCorrectionModeName = "Not supported";
            return;
        }

        // Default
        mColorCorrectionMode     = modeFast;
        mColorCorrectionModeName = "Fast";
        /*
         * Added in API 21
         *
         * Color correction processing must not slow down capture rate relative to sensor raw output.
         *
         * Advanced white balance adjustments above and beyond the specified white balance pipeline
         * may be applied.
         *
         * If AWB is enabled with android.control.awbMode != OFF, then the camera device uses the
         * last frame's AWB values (or defaults if AWB has never been run)
         */

        if (super.mControlAwbMode == null
                || super.mControlAwbMode == CameraMetadata.CONTROL_AWB_MODE_OFF) {
            mColorCorrectionMode     = modeMatrix;
            mColorCorrectionModeName = "Transform Matrix";
            /*
             * Added in API 21
             *
             * Use the android.colorCorrection.transform matrix and android.colorCorrection.gains to
             * do color conversion.
             *
             * All advanced white balance adjustments (not specified by our white balance pipeline)
             * must be disabled.
             *
             * If AWB is enabled with android.control.awbMode != OFF, then TRANSFORM_MATRIX is ignored.
             * The camera device will override this value to either FAST or HIGH_QUALITY.
             */
        }

        super.mCaptureRequestBuilder.set(key, mColorCorrectionMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setColorCorrectionTransform() {
        CaptureRequest.Key key = CaptureRequest.COLOR_CORRECTION_TRANSFORM;
        /*
         * Added in API 21
         *
         * A color transform matrix to use to transform from sensor RGB color space to output linear
         * sRGB color space.
         *
         * This matrix is either set by the camera device when the request
         * android.colorCorrection.mode is not TRANSFORM_MATRIX, or directly by the application in
         * the request when the android.colorCorrection.mode is TRANSFORM_MATRIX.
         *
         * In the latter case, the camera device may round the matrix to account for precision
         * issues; the final rounded matrix should be reported back in this matrix result metadata.
         * The transform should keep the magnitude of the output color values within [0, 1.0]
         * (assuming input color values is within the normalized range [0, 1.0]),
         * or clipping may occur.
         *
         * The valid range of each matrix element varies on different devices, but values
         * within [-1.5, 3.0] are guaranteed not to be clipped.
         *
         * Units: Unitless scale factors
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mColorCorrectionTransform     = null;
            mColorCorrectionTransformName = "Not supported";
            return;
        }

        if (mColorCorrectionMode == CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX) {
            mColorCorrectionTransform = new ColorSpaceTransform(new int[]{
                                                1, 1, 0, 1, 0, 1,    // 1/1 , 0/1 , 0/1 = 1 0 0
                                                0, 1, 1, 1, 0, 1,    // 0/1 , 1/1 , 0/1 = 0 1 0
                                                0, 1, 0, 1, 1, 1     // 0/1 , 0/1 , 1/1 = 0 0 1
                                        });
            mColorCorrectionTransformName = "(1 0 0),(0 1 0),(0 0 1)";

            super.mCaptureRequestBuilder.set(key, mColorCorrectionTransform);
        }
        else {
            mColorCorrectionTransform     = null;
            mColorCorrectionTransformName = "Disabled";
        }

    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 10 (Color)\n";
        string += "CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE: " + mColorCorrectionAberrationModeName + "\n";
        string += "CaptureRequest.COLOR_CORRECTION_GAINS:           " + mColorCorrectionGainsName          + "\n";
        string += "CaptureRequest.COLOR_CORRECTION_MODE:            " + mColorCorrectionModeName           + "\n";
        string += "CaptureRequest.COLOR_CORRECTION_TRANSFORM:       " + mColorCorrectionTransformName      + "\n";

        stringList.add(string);
        return stringList;
    }


}
