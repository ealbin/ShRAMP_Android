package Trash.camera2.settings;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Size;

import java.util.Collections;
import java.util.List;

import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.SizeSortedSet;

@TargetApi(21)
abstract class Level02_OutputFormat extends Level01_Abilities {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected StreamConfigurationMap mStreamConfigurationMap;

    protected Integer                mOutputFormat;
    private   String                 mOutputFormatName;
    protected Integer                mBitsPerPixel;

    protected Size                   mOutputSize;


    // RAW_SENSOR is offered on some smartphones, but fps performance doesn't push past ~10-15 fps
    // using ImageReader, and RAW_SENSOR doesn't work directly with Renderscript Allocation..
    // So for those reasons, (and YUV is supported on all cameras), RAW_SENSOR can be disabled
    private static final boolean ALLOW_RAW_SENSOR = true;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     *
     * @param characteristics
     */
    protected Level02_OutputFormat(@NonNull CameraCharacteristics characteristics) {
        super(characteristics);
        setStreamConfigurationMap();
        setOutputFormat();
        setOutputSize();
    }

    //----------------------------------------------------------------------------------------------

    // Documentation provided by:
    // https://developer.android.com/reference/android/hardware/camera2/CaptureRequest.html
    // https://developer.android.com/reference/android/hardware/camera2/CameraMetadata.html
    // https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html

    /**
     *
     */
    private void setStreamConfigurationMap() {
        CameraCharacteristics.Key key = CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
        /*
         * Added in API 21
         *
         * The available stream configurations that this camera device supports; also includes the
         * minimum frame durations and the stall durations for each format/size combination.
         *
         * All camera devices will support sensor maximum resolution (defined by
         * android.sensor.info.activeArraySize) for the JPEG format.
         *
         * For a given use case, the actual maximum supported resolution may be lower than what is
         * listed here, depending on the destination Surface for the image data. For example,
         * for recording video, the video encoder chosen may have a maximum size limit (e.g. 1080p)
         * smaller than what the camera (e.g. maximum resolution is 3264x2448) can provide.
         *
         * Please reference the documentation for the image data destination to check if it limits
         * the maximum size for image data.
         *
         * Refer to android.request.availableCapabilities and CameraDevice.createCaptureSession
         * (SessionConfiguration) for additional mandatory stream configurations on a per-capability
         * basis.
         *
         * *1: For JPEG format, the sizes may be restricted by below conditions:
         *      The HAL may choose the aspect ratio of each Jpeg size to be one of well known ones
         *      (e.g. 4:3, 16:9, 3:2 etc.). If the sensor maximum resolution
         *      (defined by android.sensor.info.activeArraySize) has an aspect ratio other than
         *      these, it does not have to be included in the supported JPEG sizes.
         *
         *      Some hardware JPEG encoders may have pixel boundary alignment requirements, such as
         *      the dimensions being a multiple of 16. Therefore, the maximum JPEG size may be
         *      smaller than sensor maximum resolution. However, the largest JPEG size will be as
         *      close as possible to the sensor maximum resolution given above constraints. It is
         *      required that after aspect ratio adjustments, additional size reduction due to other
         *      issues must be less than 3% in area. For example, if the sensor maximum resolution
         *      is 3280x2464, if the maximum JPEG size has aspect ratio 4:3, and the JPEG encoder
         *      alignment requirement is 16, the maximum JPEG size will be 3264x2448.
         *
         * This key is available on all devices.
         */
        mStreamConfigurationMap = (StreamConfigurationMap) mCameraCharacteristics.get(key);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setOutputFormat() {
       // Default
        mOutputFormat     = ImageFormat.YUV_420_888;
        mOutputFormatName = "YUV_420_888";

        /*
         * Added in API 19
         *
         * Multi-plane Android YUV 420 format
         *
         * This format is a generic YCbCr format, capable of describing any 4:2:0 chroma-subsampled
         * planar or semiplanar buffer (but not fully interleaved), with 8 bits per color sample.
         *
         * Images in this format are always represented by three separate buffers of data, one for
         * each color plane. Additional information always accompanies the buffers, describing the
         * row stride and the pixel stride for each plane.
         *
         * The order of planes in the array returned by Image#getPlanes() is guaranteed such that
         * plane #0 is always Y, plane #1 is always U (Cb), and plane #2 is always V (Cr).
         *
         * The Y-plane is guaranteed not to be interleaved with the U/V planes (in particular, pixel
         * stride is always 1 in yPlane.getPixelStride()).
         *
         * The U/V planes are guaranteed to have the same row stride and pixel stride
         * (in particular, uPlane.getRowStride() == vPlane.getRowStride() and
         * uPlane.getPixelStride() == vPlane.getPixelStride(); ).
         *
         * For example, the Image object can provide data in this format from a CameraDevice through
         * a ImageReader object.
         */

        if (ALLOW_RAW_SENSOR && super.mIsRawAble) {
            mOutputFormat     = ImageFormat.RAW_SENSOR;
            mOutputFormatName = "RAW_SENSOR";
            /*
             * Added in API 21
             *
             * General raw camera sensor image format, usually representing a single-channel
             * Bayer-mosaic image. Each pixel color sample is stored with 16 bits of precision.
             *
             * The layout of the color mosaic, the maximum and minimum encoding values of the raw
             * pixel data, the color space of the image, and all other needed information to
             * interpret a raw sensor image must be queried from the CameraDevice which produced
             * the image.
             */
        }
        if (!ALLOW_RAW_SENSOR && super.mIsRawAble) {
            mOutputFormatName += " (RAW_SENSOR DISABLED)";
        }

        mBitsPerPixel =  ImageFormat.getBitsPerPixel(mOutputFormat);
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setOutputSize() {

        // Find the largest output size supported by all output surfaces
        SizeSortedSet outputSizes = new SizeSortedSet();

        Size[] streamOutputSizes = (Size[]) mStreamConfigurationMap.getOutputSizes(mOutputFormat);
        Collections.addAll(outputSizes, streamOutputSizes);

        List<Class> outputClasses = SurfaceManager.getOutputSurfaceClasses();
        for (Class klass : outputClasses) {
            Size[] classOutputSizes = (Size[]) mStreamConfigurationMap.getOutputSizes(klass);
            assert classOutputSizes != null;
            for (Size s : classOutputSizes) {
                if (!outputSizes.contains(s)) {
                    outputSizes.remove(s);
                }
            }
        }

        mOutputSize = outputSizes.last();
    }

    //----------------------------------------------------------------------------------------------

    public int  getOutputFormat() { return mOutputFormat; }
    public int  getBitsPerPixel() { return mBitsPerPixel; }
    public Size getOutputSize()   { return mOutputSize;   }

    //----------------------------------------------------------------------------------------------

    /**
     *
     * @return
     */
    @NonNull
    public List<String> getString() {
        List<String> stringList = super.getString();

        String string = "Level 02 (Output format)\n";
        string += "ImageFormat:  " + mOutputFormatName                    + "\n";
        string += "BitsPerPixel: " + Integer.toString(mBitsPerPixel)      + "\n";
        string += "OutputSize:   " + mOutputSize.toString() + " [pixels]" + "\n";

        stringList.add(string);
        return stringList;
    }
}
