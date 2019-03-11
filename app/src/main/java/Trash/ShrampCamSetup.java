package Trash;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SizeF;

import java.util.ArrayList;
import java.util.List;

import Trash.CameraConfiguration;
import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * Configures camera for capture session.
 * ShRAMP optimizes settings for dark, raw and fast capture.
 */
@TargetApi(21) // Lollipop
public class ShrampCamSetup {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Activity lock
    private final Object LOCK = new Object();

    // passed into constructor
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;

    // created in constructor
    private StreamConfigurationMap mStreamConfigurationMap;

    // nested classes defined at bottom
    private CaptureConfiguration mCaptureConfiguration;
    private CameraConfiguration mCameraConfiguration;

    // created in constructor
    private List<Integer> mCameraAbilities;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    // logging
    static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Create ShrampCamSetup and accompanying objects.
     * Configures camera device through creation of CaptureConfiguration and CameraConfiguration
     * objects.
     *
     * @param device          CameraDevice to configure
     * @param characteristics CameraCharacteristics of device to configure
     */
    ShrampCamSetup(@NonNull CameraDevice device,
                   @NonNull CameraCharacteristics characteristics) {

        // only allow one camera to be setup at a time
        synchronized (LOCK) {

            mCameraDevice = device;
            mCameraCharacteristics = characteristics;
            mStreamConfigurationMap = mCameraCharacteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // define stream and get properties of camera
            mLogger.log("Loading camera abilities");
            mCaptureConfiguration = new CaptureConfiguration();
            mCameraAbilities = mCaptureConfiguration.getCameraAbilities();
            mCaptureRequestBuilder = mCaptureConfiguration.getCaptureRequestBuilder();

            // configure camera for capture
            mLogger.log("Configuring camera");
            mCameraConfiguration = new CameraConfiguration(this);

            // dump settings to log
            mLogger.log("Reporting camera configuration");
            String report = mCaptureConfiguration.reportSettings();
            report = report.concat(mCameraConfiguration.reportSettings());
            mLogger.log(report);
            mLogger.log("return;");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Access to CameraDevice
     *
     * @return mCameraDevice
     */
    CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    /**
     * Access to CameraCharacteristics
     *
     * @return mCameraCharacteristics
     */
    CameraCharacteristics getCameraCharacteristics() {
        return mCameraCharacteristics;
    }

    /**
     * Access to StreamConfigurationMap
     *
     * @return mStreamConfigurationMap
     */
    StreamConfigurationMap getStreamConfigurationMap() {
        return mStreamConfigurationMap;
    }

    /**
     * Access to CaptureConfiguration
     *
     * @return mCaptureConfiguration
     */
    CaptureConfiguration getCaptureConfiguration() {
        return mCaptureConfiguration;
    }

    /**
     * Access to CameraConfiguration
     *
     * @return mCameraConfiguration
     */
    CameraConfiguration getCameraConfiguration() {
        return mCameraConfiguration;
    }

    /**
     * Access to camera abilities
     *
     * @return mCameraAbilities
     */
    List<Integer> getCameraAbilities() {
        return mCameraAbilities;
    }

    /**
     * Access to CaptureRequest.Builder
     *
     * @return mCaptureRequestBuilder
     */
    CaptureRequest.Builder getCaptureRequestBuilder() {
        return mCaptureRequestBuilder;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of nested class CaptureConfiguration
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper class to encapsulate the process of learning what this camera can do
     */
    class CaptureConfiguration {

        //******************************************************************************************
        // Class Variables
        //----------------

        private List<Integer> mmCameraAbilities;
        private int mmOutputFormat;
        private int mmBitsPerPixel;
        private Size mmOutputSize;
        private boolean mmUsingManualTemplate;
        private boolean mmUsingRawImageFormat;

        //******************************************************************************************
        // Class Methods
        //--------------

        /**
         * Create CaptureConfiguration to learn of camera's abilities and stream format
         */
        CaptureConfiguration() {

            assert mCameraCharacteristics != null;
            assert mStreamConfigurationMap != null;

            mLogger.log("Loading camera abilities");
            loadCameraAbilities();

            mLogger.log("Loading stream format");
            loadStreamFormat();

            mLogger.log("return;");
        }

        /**
         * Discover camera's abilities
         */
        private void loadCameraAbilities() {

            mmCameraAbilities = new ArrayList<>();
            int[] abilities = mCameraCharacteristics.get(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);

            assert abilities != null;
            for (int ability : abilities) {
                mmCameraAbilities.add(ability);
            }

            mLogger.log("return;");
        }

        /**
         * Access camera abilities
         *
         * @return a list of abilities (see android.hardware.camera2.CameraMetadata)
         */
        List<Integer> getCameraAbilities() {
            return mmCameraAbilities;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Determine stream format
         */
        private void loadStreamFormat() {

            assert mmCameraAbilities != null;
            int outputFormat;
            if (mmCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
                outputFormat = ImageFormat.RAW_SENSOR;
                mmUsingRawImageFormat = true;
            } else {
                outputFormat = ImageFormat.YUV_420_888;
                mmUsingRawImageFormat = false;
            }
            int bitsPerPixel = ImageFormat.getBitsPerPixel(outputFormat);

            Size[] outputSizes = mStreamConfigurationMap.getOutputSizes(outputFormat);
            Size outputSize = null;
            for (Size size : outputSizes) {
                if (outputSize == null) {
                    outputSize = size;
                    continue;
                }
                long outputArea = outputSize.getWidth() * outputSize.getHeight();
                long sizeArea = size.getWidth() * size.getHeight();
                if (sizeArea > outputArea) {
                    outputSize = size;
                }
            }
            mmOutputFormat = outputFormat;
            mmBitsPerPixel = bitsPerPixel;
            mmOutputSize = outputSize;

            mLogger.log("return;");
        }

        /**
         * Can this camera use the raw pixel format?
         *
         * @return true if yes, false if no (aka YUV_420_888)
         */
        boolean isOutputFormatRaw() {
            return mmUsingRawImageFormat;
        }

        /**
         * Access the output format
         *
         * @return int format code (android.hardware.camera2.CameraMetadata)
         */
        int getOutputFormat() {
            return mmOutputFormat;
        }

        /**
         * Number of bits per pixel
         *
         * @return int
         */
        int getBitsPerPixel() {
            return mmBitsPerPixel;
        }

        /**
         * Access output format size in pixels
         *
         * @return Size object, use methods getWidth() and getHeight()
         */
        Size getOutputSize() {
            return mmOutputSize;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Create CaptureRequest.Builder and configure with manual template if possible.
         * Use preview template if manual is unavailable for maximum frame rate.
         *
         * @return a CaptureRequest.Builder object used to configure the capture
         */
        CaptureRequest.Builder getCaptureRequestBuilder() {

            int captureTemplate;
            if (mmCameraAbilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
                captureTemplate = CameraDevice.TEMPLATE_MANUAL;
                mmUsingManualTemplate = true;
            } else {
                // preview is guaranteed on all camera devices
                captureTemplate = CameraDevice.TEMPLATE_PREVIEW;
                mmUsingManualTemplate = false;
            }


            // delete
            //captureTemplate = CameraDevice.TEMPLATE_PREVIEW;

            try {
                mLogger.log("return CaptureRequest.Builder;");
                return mCameraDevice.createCaptureRequest(captureTemplate);
            } catch (CameraAccessException e) {
                // TODO EXCEPTION
                mLogger.log("Error: Camera Access Exception; return null;");
                return null;
            }
        }

        /**
         * Is this camera using the manual template?
         *
         * @return true if yes, false if no (aka preview template)
         */
        boolean usingManualCaptureRequestTemplate() {
            return mmUsingManualTemplate;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Discover minimum frame duration for this stream configuration
         *
         * @return time in nanoseconds (possibly 0 if camera does not support this function)
         */
        long getOutputMinFrameDuration() {
            return mStreamConfigurationMap.getOutputMinFrameDuration(mmOutputFormat, mmOutputSize);
        }

        /**
         * Discover time between frame captures (if any)
         *
         * @return time in nanoseconds (hopefully 0, always 0 for YUV_420_888)
         */
        long getOutputStallDuration() {
            return mStreamConfigurationMap.getOutputStallDuration(mmOutputFormat, mmOutputSize);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Report a summary of settings
         *
         * @return a string summary
         */
        String reportSettings() {

            String report = " \n";

            report = report.concat("Hardware level: ");
            try {
                int hardwareLevel = mCameraCharacteristics.get(
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                switch (hardwareLevel) {
                    case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED): {
                        report = report.concat("Limited");
                        break;
                    }
                    case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL): {
                        report = report.concat("Full");
                        break;
                    }
                    case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY): {
                        report = report.concat("Legacy");
                        break;
                    }
                    case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3): {
                        report = report.concat("Level 3");
                        break;
                    }
                    case (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL): {
                        report = report.concat("External");
                        break;
                    }
                    default: {
                        report = report.concat("Unknown");
                    }
                }
            } catch (Exception e) {
                report = report.concat("Unavailable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Camera version: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28
                try {
                    String version = mCameraCharacteristics.get(
                            CameraCharacteristics.INFO_VERSION);

                    version = version.replaceAll(":", "=");
                    report = report.concat(version);
                } catch (Exception e) {
                    report = report.concat("Unavailable");
                }
            } else {
                report = report.concat("Not supported");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Sensor physical size: ");
            try {
                SizeF size = mCameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);

                report = report.concat(size.toString() + " [mm]");
            } catch (Exception e) {
                report = report.concat("Unavailable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Total pixel array size: ");
            try {
                Size array = mCameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);

                report = report.concat(array.toString());
            } catch (Exception e) {
                report = report.concat("Unavailable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Pre-correction pixel array size: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23
                try {
                    Rect array = mCameraCharacteristics.get(
                            CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE);

                    String width = Integer.toString(array.width());
                    String height = Integer.toString(array.height());
                    report = report.concat(width + "x" + height);
                } catch (Exception e) {
                    report = report.concat("Unavailable");
                }
            } else {
                report = report.concat("Not supported");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Active pixel array size: ");
            try {
                Rect array = mCameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

                String width = Integer.toString(array.width());
                String height = Integer.toString(array.height());
                report = report.concat(width + "x" + height);
            } catch (Exception e) {
                report = report.concat("Unavailable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Sensor has optically black regions: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // API 24
                try {
                    Rect[] regions = mCameraCharacteristics.get(
                            CameraCharacteristics.SENSOR_OPTICAL_BLACK_REGIONS);

                    if (regions.length > 0) {
                        report = report.concat("Yes");
                    } else {
                        report = report.concat("No");
                    }
                } catch (Exception e) {
                    report = report.concat("Unavailable");
                }
            } else {
                report = report.concat("Not supported");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Color_ filter arrangement: ");
            try {
                int arrangement = mCameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT);

                switch (arrangement) {
                    case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB): {
                        report = report.concat("RGGB");
                        break;
                    }
                    case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG): {
                        report = report.concat("GRBG");
                        break;
                    }
                    case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG): {
                        report = report.concat("GBRG");
                        break;
                    }
                    case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR): {
                        report = report.concat("BGGR");
                        break;
                    }
                    case (CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB): {
                        report = report.concat("RGB");
                        break;
                    }
                    default: {
                        report = report.concat("Unknown");
                    }
                }
            } catch (Exception e) {
                report = report.concat("Unavailable");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            if (isOutputFormatRaw()) {
                report = report.concat("Output Format: Raw");
            } else {
                report = report.concat("Output Format: YUV_420_888");
            }
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Bits per pixel: " + Integer.toString(getBitsPerPixel()));
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            report = report.concat("Output size: " + getOutputSize().toString());
            report = report.concat("\n");

            //--------------------------------------------------------------------------------------

            if (usingManualCaptureRequestTemplate()) {
                report = report.concat("Capture Template: Manual");
            } else {
                report = report.concat("Capture Template: Preview");
            }
            report = report.concat("\n");

            return report;
        }
    }

}