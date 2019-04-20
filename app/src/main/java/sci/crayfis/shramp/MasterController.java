/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 20 April 2019
 */

package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import sci.crayfis.shramp.analysis.AnalysisController;
import sci.crayfis.shramp.battery.BatteryController;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.capture.CaptureController;
import sci.crayfis.shramp.sensor.SensorController;
import sci.crayfis.shramp.surfaces.SurfaceController;
import sci.crayfis.shramp.util.StorageMedia;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;

/**
 * Oversees the setup of surfaces, cameras and capture session
 */
@TargetApi(21)
public final class MasterController extends Activity {

    // Private Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandler.....................................................................................
    // Reference to this Activity's thread Handler
    private static Handler mHandler;

    // mInstance....................................................................................
    // Static reference to single instance of this class.
    private static MasterController mInstance;

    // Execution-Routing Runnables
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // Devices and surfaces are prepared asynchronously, so these runnables enable execution to
    // pause until everything is ready

    // GoTo_prepareSurfaces.........................................................................
    // Called after the camera is initialized
    private final static Runnable GoTo_prepareSurfaces = new Runnable() {
        @Override
        public void run() {
            prepareSurfaces();
        }
    };

    // GoTo_prepareAnalysis.........................................................................
    // Called after the output surfaces are initialized
    private final static Runnable GoTo_prepareAnalysis = new Runnable() {
        @Override
        public void run() {
            prepareAnalysis();
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onCreate.....................................................................................
    /**
     * Entry point for this activity after MaineShRAMP hands control over to it.
     * Starts the chain of events that leads to data capture (configuring camera, surfaces, etc)
     * @param savedInstanceState passed in by Android OS for returning from a suspended state
     *                           (not used)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For access to this instance from static methods
        mInstance = this;

        // Activity thread Handler
        mHandler = new Handler(getMainLooper());

        // In the future, this will be removed.  For now, just start clean for simplicity.
        if (GlobalSettings.START_FROM_SCRATCH) {
            Log.e(Thread.currentThread().getName(), "Clearing ShRAMP data directory, starting from scratch");
            StorageMedia.cleanAll();
        }

        // Set up ShRAMP data directory
        StorageMedia.setUpShrampDirectory();

        // In the future, sensors will be initialized here
        //Log.e(Thread.currentThread().getName(), "Loading sensor package");
        //SensorController.initializeTemperature(mInstance, false);

        // Initialized battery information
        Log.e(Thread.currentThread().getName(), "Battery Info:");
        BatteryController.initialize(mInstance);
        GlobalSettings.TEMPERATURE_START = BatteryController.getCurrentTemperature();
        Log.e(Thread.currentThread().getName(), " \n" + BatteryController.getString() + " \n");

        // Get system camera manager
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Camera manager cannot be null");
            MasterController.quitSafely();
            return;
        }

        // Discover abilities of detectable cameras
        CameraController.discoverCameras(cameraManager);
        CameraController.writeCameraCharacteristics();

        // Open the preferred camera and ready it for capture.
        // The camera opens asynchronously, so whenever it finishes, it will run GoTo_prepareSurfaces
        // to continue execution in prepareSurfaces() below.
        if (!CameraController.openCamera(GlobalSettings.PREFERRED_CAMERA, GoTo_prepareSurfaces, mHandler)) {
             CameraController.openCamera(GlobalSettings.SECONDARY_CAMERA, GoTo_prepareSurfaces, mHandler);
        }
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // prepareSurfaces..............................................................................
    /**
     * Initialize all output surfaces.  This happens asynchronously, so whenever it finishes, it
     * will run GoTo_prepareAnalysis to continue execution in prepareAnalysis() below.
     */
    public static void prepareSurfaces() {
        SurfaceController.openSurfaces(mInstance, GoTo_prepareAnalysis, mHandler);
    }

    // prepareAnalysis..............................................................................
    /**
     * Initialize analysis Allocations and RenderScripts.  This happens synchronously as there is
     * no hardware setup directly involved unlike surfaces and cameras.  When finished continue with
     * startCaptureSequence() below.
     */
    public static void prepareAnalysis() {
        AnalysisController.initialize(mInstance);
        startCaptureSession();
    }

    // startCaptureSequence..........................................................................
    /**
     * This is essentially the end of the line for MasterController.
     * If there is enough memory left over after setup to support capture, pass execution control
     * over to the CaptureController and associates.
     */
    public static void startCaptureSession() {
        if (HeapMemory.getAvailableMiB() < GlobalSettings.AMPLE_MEMORY_MiB) {
            // TODO: error
            Log.e("LOW MEMORY " + Long.toString(HeapMemory.getAvailableMiB()) + " MiB", "CANNOT START");
            quitSafely();
            return;
        }

        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        HeapMemory.logAvailableMiB();

        CaptureController.startCaptureSequence();
    }

    // quitSafely...................................................................................
    /**
     * This method can be called by any class at any time to shut everything down, close all
     * cameras, surfaces etc, end all running threads and exit the app completely.
     */
    public static void quitSafely() {
        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        Log.e(Thread.currentThread().getName(), "MasterController quitSafely");
        CameraController.closeCamera();
        BatteryController.shutdown();
        HandlerManager.finish();
        mInstance.finish();
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // finish.......................................................................................
    /**
     * Final action to completely close the app.
     */
    @Override
    public void finish() {
        finishAffinity();
        Log.e(Thread.currentThread().getName(), "MasterController finished");
    }

    // onPause......................................................................................
    /**
     * Release resources on pause (app is not in foreground)
     */
    @Override
    public void onPause() {
        super.onPause();
        SensorController.onPause();
    }

    // onResume.....................................................................................
    /**
     * Regain resources on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        SensorController.onResume();
    }

}