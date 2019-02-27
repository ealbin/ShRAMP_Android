package sci.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.TonemapCurve;
import android.support.annotation.NonNull;

import java.util.List;

@TargetApi(21)
abstract class Level11_Tonemap extends Level10_Color {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Integer      mTonemapMode;
    private   String       mTonemapModeName;

    protected TonemapCurve mTonemapCurve;
    private   String       mTonemapCurveName;

    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level11_Tonemap(@NonNull CameraCharacteristics characteristics,
                              @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setTonemapMode();
        setTonemapCurve();
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
    private void setTonemapMode() {
        CaptureRequest.Key key = CaptureRequest.TONEMAP_MODE;
        int modeCurve = CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE;
        int modeFast  = CameraMetadata.TONEMAP_MODE_FAST;
        /*
         * Added in API 21
         *
         * High-level global contrast/gamma/tonemapping control.
         *
         * When switching to an application-defined contrast curve by setting android.tonemap.mode
         * to CONTRAST_CURVE, the curve is defined per-channel with a set of (in, out) points that
         * specify the mapping from input high-bit-depth pixel value to the output low-bit-depth
         * value. Since the actual pixel ranges of both input and output may change depending on the
         * camera pipeline, the values are specified by normalized floating-point numbers.
         *
         * More-complex color mapping operations such as 3D color look-up tables, selective chroma
         * enhancement, or other non-linear color transforms will be disabled when
         * android.tonemap.mode is CONTRAST_CURVE.
         *
         * When using either FAST or HIGH_QUALITY, the camera device will emit its own tonemap curve
         * in android.tonemap.curve. These values are always available, and as close as possible to
         * the actually used nonlinear/nonglobal transforms.
         *
         * If a request is sent with CONTRAST_CURVE with the camera device's provided curve in FAST
         * or HIGH_QUALITY, the image's tonemap will be roughly the same.
         *
         * Available values for this device:
         * android.tonemap.availableToneMapModes
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (!super.mRequestKeys.contains(key)) {
            mTonemapMode     = null;
            mTonemapModeName = "Not supported";
            return;
        }

        // Default
        mTonemapMode     = modeFast;
        mTonemapModeName = "Fast";
        /*
         * Added in API 21
         *
         * Advanced gamma mapping and color enhancement may be applied, without reducing frame rate
         * compared to raw sensor output.
         */

        List<Integer> modes = super.getAvailable(CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES);
        if (modes.contains(modeCurve)) {
            mTonemapMode     = modeCurve;
            mTonemapModeName = "Contrast curve";
            /*
             * Added in API 21
             *
             * Use the tone mapping curve specified in the android.tonemap.curve* entries.
             *
             * All color enhancement and tonemapping must be disabled, except for applying the
             * tonemapping curve specified by android.tonemap.curve.
             *
             * Must not slow down frame rate relative to raw sensor output.
             */
        }

        mCaptureRequestBuilder.set(key, mTonemapMode);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setTonemapCurve() {
        CaptureRequest.Key key = CaptureRequest.TONEMAP_CURVE;
        /*
         * Added in API 21
         *
         * Tonemapping / contrast / gamma curve to use when android.tonemap.mode is CONTRAST_CURVE.
         *
         * The tonemapCurve consist of three curves for each of red, green, and blue channels
         * respectively. The following example uses the red channel as an example. The same logic
         * applies to green and blue channel. Each channel's curve is defined by an array of control
         * points:
         *
         *  curveRed =
         *        [ P0(in, out), P1(in, out), P2(in, out), P3(in, out), ..., PN(in, out) ]
         *  2 <= N <= android.tonemap.maxCurvePoints
         *
         * These are sorted in order of increasing Pin; it is always guaranteed that input values
         * 0.0 and 1.0 are included in the list to define a complete mapping. For input values
         * between control points, the camera device must linearly interpolate between the
         * control points.
         *
         * Each curve can have an independent number of points, and the number of points can be less
         * than max (that is, the request doesn't have to always provide a curve with number of
         * points equivalent to android.tonemap.maxCurvePoints).
         *
         * For devices with MONOCHROME capability, only red channel is used. Green and blue channels
         * are ignored.
         *
         * A few examples, and their corresponding graphical mappings; these only specify the red
         * channel and the precision is limited to 4 digits, for conciseness.
         *
         * Linear mapping:
         *      curveRed = [ (0, 0), (1.0, 1.0) ]
         *
         * Invert mapping:
         *      curveRed = [ (0, 1.0), (1.0, 0) ]
         *
         * Gamma 1/2.2 mapping, with 16 control points:
         *      curveRed = [
         *          (0.0000, 0.0000), (0.0667, 0.2920), (0.1333, 0.4002), (0.2000, 0.4812),
         *          (0.2667, 0.5484), (0.3333, 0.6069), (0.4000, 0.6594), (0.4667, 0.7072),
         *          (0.5333, 0.7515), (0.6000, 0.7928), (0.6667, 0.8317), (0.7333, 0.8685),
         *          (0.8000, 0.9035), (0.8667, 0.9370), (0.9333, 0.9691), (1.0000, 1.0000) ]
         *
         * Standard sRGB gamma mapping, per IEC 61966-2-1:1999, with 16 control points:
         *      curveRed = [
         *          (0.0000, 0.0000), (0.0667, 0.2864), (0.1333, 0.4007), (0.2000, 0.4845),
         *          (0.2667, 0.5532), (0.3333, 0.6125), (0.4000, 0.6652), (0.4667, 0.7130),
         *          (0.5333, 0.7569), (0.6000, 0.7977), (0.6667, 0.8360), (0.7333, 0.8721),
         *          (0.8000, 0.9063), (0.8667, 0.9389), (0.9333, 0.9701), (1.0000, 1.0000) ]
         *
         * Optional - This value may be null on some devices.
         *
         * Full capability - Present on all camera devices that report being HARDWARE_LEVEL_FULL
         * devices in the android.info.supportedHardwareLevel key
         */
        if (this.mTonemapMode == null || mTonemapMode != CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE) {
            mTonemapCurve     = null;
            mTonemapCurveName = "Not supported";
            return;
        }

        float[] linear_response = {0, 0, 1, 1};
        mTonemapCurve     = new TonemapCurve(linear_response, linear_response, linear_response);
        mTonemapCurveName = "Linear response";

        super.mCaptureRequestBuilder.set(key, mTonemapCurve);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 11 (Tonemap)\n";
        string += "CaptureRequest.TONEMAP_MODE:  " + mTonemapModeName  + "\n";
        string += "CaptureRequest.TONEMAP_CURVE: " + mTonemapCurveName + "\n";

        stringList.add(string);
        return stringList;
    }

}