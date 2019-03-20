package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import sci.crayfis.shramp.analysis.ImageProcessor;
import sci.crayfis.shramp.camera2.CameraController;
import sci.crayfis.shramp.camera2.capture.CaptureManager;
import sci.crayfis.shramp.logging.DividerStyle;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.surfaces.SurfaceManager;
import sci.crayfis.shramp.util.DataManager;
import sci.crayfis.shramp.util.HandlerManager;
import sci.crayfis.shramp.util.HeapMemory;

/**
 * Oversees the setup of surfaces, cameras and capture session
 */
@TargetApi(21)
public final class CaptureOverseer extends Activity {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Public Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PREFERRED_CAMERA.............................................................................
    // TODO: description
    private static final CameraController.Select PREFERRED_CAMERA = GlobalSettings.PREFERRED_CAMERA;

    // SECONDARY_CAMERA.............................................................................
    // TODO: description
    private static final CameraController.Select SECONDARY_CAMERA = GlobalSettings.SECONDARY_CAMERA;

    // Private
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mHandler.....................................................................................
    // TODO: description
    private static Handler mHandler;

    // mInstance....................................................................................
    // Static reference to single instance of this class.
    private static CaptureOverseer mInstance;

    //==============================================================================================
    // Logging
    private static final ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);
    private static final DecimalFormat mNanosFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    //==============================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //**********************************************************************************************
    // Overriding Methods
    //--------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onCreate.....................................................................................
    /**
     * Entry point for this activity.
     * TODO: description, comments and logging
     * @param savedInstanceState bla
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.e(Thread.currentThread().getName(), "CaptureOverseer onCreate");

        // For access in static methods
        mInstance = this;

        mHandler = new Handler(getMainLooper());

        //mLogger.divider(DividerStyle.Strong);
        //mLogger.log("Capture Overseer has begun");
        //long startTime = SystemClock.elapsedRealtimeNanos();

        // Set up ShRAMP data directory
        DataManager.setUpShrampDirectory();

        // TODO: REMOVE IN THE FUTURE
        // start fresh
        //mLogger.log("Clearing ShRAMP data directory, starting from scratch");
        DataManager.clean();

        //==========================================================================================

        // Turning control over to the ShrampCamManager to set up the camera
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        assert cameraManager != null;

        CameraController.discoverCameras(cameraManager);
        //CameraController.writeCameraCharacteristics();

        Runnable next = new Runnable() {
            @Override
            public void run() {
                prepareSurfaces();
            }
        };
        if (!CameraController.openCamera(PREFERRED_CAMERA, next, mHandler)) {
             CameraController.openCamera(SECONDARY_CAMERA, next, mHandler);
        }

        //String elapsed = mNanosFormatter.format(SystemClock.elapsedRealtimeNanos() - startTime);
        //mLogger.log("return; elapsed = " + elapsed + " [ns]");
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // post.........................................................................................
    /**
     * TODO: description, comments and logging
     * @param runnable bla
     */
    public synchronized static void post(Runnable runnable) {
        Log.e(Thread.currentThread().getName(), "CaptureOverseer post");
        mHandler.post(runnable);
    }

    // prepareImageProcessing.......................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void prepareImageProcessing() {
        //Log.e(Thread.currentThread().getName(), "CaptureOverseer prepareImageProcessing");
        ImageProcessor.init(mInstance);
        startCaptureSession();
    }

    // prepareSurfaces..............................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void prepareSurfaces() {
        //Log.e(Thread.currentThread().getName(), "CaptureOverseer prepareSurfaces");
        Runnable next = new Runnable() {
            @Override
            public void run() {
                prepareImageProcessing();
            }
        };
        SurfaceManager.openSurfaces(mInstance, next, mHandler);
    }

    // quitSafely...................................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void quitSafely() {
        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        Log.e(Thread.currentThread().getName(), "CaptureOverseer quitSafely");
        CameraController.closeCamera();
        HandlerManager.finish();
        mInstance.finish();
    }

    // startCaptureSession..........................................................................
    /**
     * TODO: description, comments and logging
     */
    public static void startCaptureSession() {
        if (HeapMemory.getAvailableMiB() < GlobalSettings.SUFFICIENT_MEMORY_MiB) {
            // TODO: error
            Log.e("LOW MEMORY " + Long.toString(HeapMemory.getAvailableMiB()) + " MiB", "CANNOT START");
            quitSafely();
            return;
        }

        Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        Log.e(Thread.currentThread().getName(), "CaptureOverseer startCaptureSession");
        CaptureManager.startCaptureSession();
    }

    public void finish() {
        Log.e(Thread.currentThread().getName(), "CaptureOverseer finish");
        finishAffinity();
    }

    private static Runnable mNextAction;

    public synchronized static void setNextAction(Runnable action) {
        Log.e(Thread.currentThread().getName(), "CaptureOverseer Next Action has been Set----------------------");
        mNextAction = action;
    }

    public synchronized static void imageProcessorReady() {
        Log.e(Thread.currentThread().getName(), "CaptureOverseer imageProcessorReady");
        if (mNextAction != null) {
            Runnable doThis = mNextAction;
            mNextAction = null;
            doThis.run();
        }
        else {
            Log.e(Thread.currentThread().getName(), "CaptureOverseer NextAction is null!");
        }
    }
}
