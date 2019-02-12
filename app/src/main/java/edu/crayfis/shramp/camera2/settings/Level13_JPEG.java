package edu.crayfis.shramp.camera2.settings;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Size;

import java.util.List;

@TargetApi(21)
abstract class Level13_JPEG extends Level12_MiscModes {

    //**********************************************************************************************
    // Class Variables
    //----------------

    protected Location  mJpegGpsLocation;
    private   String    mJpegGpsLocationName;

    protected Integer   mJpegOrientation;
    private   String    mJpegOrientationName;

    protected Byte      mJpegQuality;
    private   String    mJpegQualityName;

    protected Byte      mJpegThumbnailQuality;
    private   String    mJpegThumbnailQualityName;

    protected Size      mJpegThumbnailSize;
    private   String    mJpegThumbnailSizeName;


    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level13_JPEG(@NonNull CameraCharacteristics characteristics,
                           @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
        setJpegGpsLocation();
        setJpegOrientation();
        setJpegQuality();
        setJpegThumbnailQuality();
        setJpegThumbnailSize();
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
    private void setJpegGpsLocation() {
        CaptureRequest.Key key = CaptureRequest.JPEG_GPS_LOCATION;
        mJpegGpsLocation     = null;
        mJpegGpsLocationName = "Not applicable";
        /*
         * Added in API 21
         *
         * A location object to use when generating image GPS metadata.
         *
         * Setting a location object in a request will include the GPS coordinates of the location
         * into any JPEG images captured based on the request. These coordinates can then be viewed
         * by anyone who receives the JPEG image.
         *
         * This key is available on all devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setJpegOrientation() {
        CaptureRequest.Key key = CaptureRequest.JPEG_ORIENTATION;
        mJpegOrientation     = null;
        mJpegOrientationName = "Not applicable";
        /*
         * Added in API 21
         *
         * The orientation for a JPEG image.
         *
         * The clockwise rotation angle in degrees, relative to the orientation to the camera,
         * that the JPEG picture needs to be rotated by, to be viewed upright.
         *
         * Camera devices may either encode this value into the JPEG EXIF header, or rotate the
         * image data to match this orientation. When the image data is rotated, the thumbnail
         * data will also be rotated.
         *
         * Note that this orientation is relative to the orientation of the camera sensor, given
         * by android.sensor.orientation.
         *
         * For EXTERNAL cameras the sensor orientation will always be set to 0 and the facing will
         * also be set to EXTERNAL. The above code is not relevant in such case.
         *
         * Units: Degrees in multiples of 90
         *
         * Range of valid values:
         * 0, 90, 180, 270
         *
         * This key is available on all devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setJpegQuality() {
        CaptureRequest.Key key = CaptureRequest.JPEG_QUALITY;
        mJpegQuality     = null;
        mJpegQualityName = "Not applicable";
        /*
         * Added in API 21
         *
         * Compression quality of the final JPEG image.
         *
         * 85-95 is typical usage range.
         *
         * Range of valid values:
         * 1-100; larger is higher quality
         *
         * This key is available on all devices
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setJpegThumbnailQuality() {
        CaptureRequest.Key key = CaptureRequest.JPEG_THUMBNAIL_QUALITY;
        mJpegThumbnailQuality     = null;
        mJpegThumbnailQualityName = "Not applicable";
        /*
         * Added in API 21
         *
         * Compression quality of JPEG thumbnail.
         *
         * Range of valid values:
         * 1-100; larger is higher quality
         *
         * This key is available on all devices.
         */
    }

    //----------------------------------------------------------------------------------------------

    /**
     *
     */
    private void setJpegThumbnailSize() {
        CaptureRequest.Key key = CaptureRequest.JPEG_THUMBNAIL_SIZE;
        mJpegThumbnailSize     = null;
        mJpegThumbnailSizeName = "Not applicable";
        /*
         * Added in API 21
         *
         * Resolution of embedded JPEG thumbnail.
         *
         * When set to (0, 0) value, the JPEG EXIF will not contain thumbnail, but the captured JPEG
         * will still be a valid image.
         *
         * For best results, when issuing a request for a JPEG image, the thumbnail size selected
         * should have the same aspect ratio as the main JPEG output.
         *
         * If the thumbnail image aspect ratio differs from the JPEG primary image aspect ratio, the
         * camera device creates the thumbnail by cropping it from the primary image. For example,
         * if the primary image has 4:3 aspect ratio, the thumbnail image has 16:9 aspect ratio, the
         * primary image will be cropped vertically (letterbox) to generate the thumbnail image. The
         * thumbnail image will always have a smaller Field Of View (FOV) than the primary image
         * when aspect ratios differ.
         *
         * When an android.jpeg.orientation of non-zero degree is requested, the camera device will
         * handle thumbnail rotation in one of the following ways:
         *
         * Set the EXIF orientation flag and keep jpeg and thumbnail image data unrotated.
         * Rotate the jpeg and thumbnail image data and not set EXIF orientation flag. In this case,
         * LIMITED or FULL hardware level devices will report rotated thumnail size in capture
         * result, so the width and height will be interchanged if 90 or 270 degree orientation is
         * requested. LEGACY device will always report unrotated thumbnail size.
         *
         * Range of valid values:
         *      android.jpeg.availableThumbnailSizes
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

        String string = "Level 13 (JPEG)\n";
        string += "CaptureRequest.JPEG_GPS_LOCATION:      " + mJpegGpsLocationName      + "\n";
        string += "CaptureRequest.JPEG_ORIENTATION:       " + mJpegOrientationName      + "\n";
        string += "CaptureRequest.JPEG_QUALITY:           " + mJpegQualityName          + "\n";
        string += "CaptureRequest.JPEG_THUMBNAIL_QUALITY: " + mJpegThumbnailQualityName + "\n";
        string += "CaptureRequest.JPEG_THUMBNAIL_SIZE:    " + mJpegThumbnailSizeName    + "\n";

        stringList.add(string);
        return stringList;
    }

}