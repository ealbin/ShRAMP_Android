/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

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

    // Private Static Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandler.....................................................................................
    // TODO: description
    private static Handler mHandler;

    // mInstance....................................................................................
    // Static reference to single instance of this class.
    private static MasterController mInstance;

    // Execution Routing
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // GoTo_prepareSurfaces.........................................................................
    // TODO: description
    private final static Runnable GoTo_prepareSurfaces = new Runnable() {
        @Override
        public void run() {
            prepareSurfaces();
        }
    };

    // GoTo_prepareAnalysis.........................................................................
    // TODO: description
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
     * Entry point for this activity.
     * TODO: description, comments and logging
     * @param savedInstanceState bla
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For access in static methods
        mInstance = this;

        // Main thread
        mHandler = new Handler(getMainLooper());

        if (GlobalSettings.START_FROM_SCRATCH) {
            Log.e(Thread.currentThread().getName(), "Clearing ShRAMP data directory, starting from scratch");
            StorageMedia.cleanAll();
        }

        // Set up ShRAMP data directory
        StorageMedia.setUpShrampDirectory();


        //Log.e(Thread.currentThread().getName(), "Loading sensor package");
        //SensorController.initializeTemperature(mInstance, false);

        Log.e(Thread.currentThread().getName(), "Battery Info:");
        BatteryController.initialize(mInstance);
        GlobalSettings.TEMPERATURE_START = BatteryController.getCurrentTemperature();
        Log.e(Thread.currentThread().getName(), " \n" + BatteryController.getString() + " \n");

        // Get system camera manager
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        assert cameraManager != null;

        // Discover abilities of detectable cameras
        CameraController.discoverCameras(cameraManager);
        CameraController.writeCameraCharacteristics();

        if (!CameraController.openCamera(GlobalSettings.PREFERRED_CAMERA, GoTo_prepareSurfaces, mHandler)) {
             CameraController.openCamera(GlobalSettings.SECONDARY_CAMERA, GoTo_prepareSurfaces, mHandler);
        }
    }

    // Public Static Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // prepareSurfaces..............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void prepareSurfaces() {
        SurfaceController.openSurfaces(mInstance, GoTo_prepareAnalysis, mHandler);
    }

    // prepareAnalysis.......................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void prepareAnalysis() {
        Log.e(Thread.currentThread().getName(), "MasterController prepareAnalysis");
        AnalysisController.initialize(mInstance);
        startCaptureSession();
    }

    // startCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
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

        CaptureController.startCaptureSession();
    }




    // quitSafely...................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void quitSafely() {
        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        Log.e(Thread.currentThread().getName(), "MasterController quitSafely");
        CameraController.closeCamera();
        BatteryController.shutdown();
        HandlerManager.finish();
        mInstance.finish();
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // finish.......................................................................................
    /**
     * TODO: description
     */
    public void finish() {
        finishAffinity();
        Log.e(Thread.currentThread().getName(), "MasterController finished");
    }


    @Override
    public void onResume() {
        super.onResume();
        SensorController.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SensorController.onPause();
    }

}