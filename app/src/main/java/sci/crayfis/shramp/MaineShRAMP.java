package sci.crayfis.shramp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

import sci.crayfis.shramp.util.BuildString;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.ssh.AsyncResponse;
import sci.crayfis.shramp.util.FailManager;

/**
 * Entry point for the ShRAMP app
 * Checks permissions then runs CaptureOverseer
 */
@TargetApi(21)
public final class MaineShRAMP extends Activity implements AsyncResponse {

    //**********************************************************************************************
    // Class Variables
    //----------------

    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSION_CODE = 0; // could be anything >= 0

    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    private Intent mNextActivity;
    private Intent mFailActivity;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Entry point for the app at start.
     * @param savedInstanceState passed in by Android OS
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNextActivity = new Intent(this, CaptureOverseer.class);
        mFailActivity = new Intent(this, FailManager.class);

        mLogger.log("Welcome to the Shower Reconstruction Application for Mobile Phones");
        mLogger.log("or \"ShRAMP\" for short");

        // Get build info
        String buildString = BuildString.getIt();
        mLogger.log(buildString);

        // if API 22 or below, user would have granted permissions on start
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mLogger.log("API 22 or below, permissions granted on start");
            mLogger.log("Starting CaptureOverseer");
            startActivity(mNextActivity);
        }
        else {
            // if API > 22
            if (permissionsGranted()) {
                mLogger.log("API 23 or above, and permissions have previously been granted");
                mLogger.log("Starting CaptureOverseer");
                startActivity(mNextActivity);
            }
            else {
                mLogger.log("API 23 or above, but permissions not granted, asking permissions");
                // response to request is handled in onRequestPermissionsResult()
                requestPermissions(PERMISSIONS, PERMISSION_CODE);
            }
        }
        mLogger.log("return;");
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Check if permissions have been granted
     * @return true if all permissions have been granted, false if not
     */
    @TargetApi(23)
    private boolean permissionsGranted() {
        boolean allGranted = true;

        for (String permission : PERMISSIONS) {
            int permission_value = checkSelfPermission(permission);

            if (permission_value == PackageManager.PERMISSION_DENIED) {
                mLogger.log(permission + ": " + "DENIED");
                allGranted = false;
            }
            else {
                mLogger.log(permission + ": " + "GRANTED");
            }
        }

        if (allGranted) {
            mLogger.log("All permissions granted");
        }
        else {
            mLogger.log("Some or all permissions denied");
        }

        mLogger.log("permissionsGranted? return: " + Boolean.toString(allGranted));
        return allGranted;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * After user responds to permission request, this routine is called.
     * @param requestCode permission code, see PERMISSION_CODE
     * @param permissions permissions requested
     * @param grantResults user's response
     */
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsGranted()) {
            mLogger.log("Permissions asked and granted");
            mLogger.log("Starting CaptureOverseer");
            startActivity(mNextActivity);
        }
        else {
            mLogger.log("Permissions were not granted");
            mLogger.log("Starting FailManager");
            startActivity(mFailActivity);
        }
        mLogger.log("return;");
    }

    //----------------------------------------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////////////////////////


    //TextureView mTextureView;

    /**
     * After permissions are granted, begin app operations here.
     */
    /*
    public void startApp(){

        // set ssh listener to this class for information exchange
        SSHrampSession_reference.mainactivity = this;

        //Intent intent = new Intent(this, DAQActivity.class);
        //startActivity(intent);

        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append("Welcome to ShRAMP!\n");
        textOut.append("(Shower Reconstructing Application for Mobile Phones)\n");
        textOut.append("----------------------------------------------------------\n\n");
        textOut.append("Capturing a camera frame..  \n");



//        mCamera = new Camera(this, mQuit_action);

        //SurfaceView surface_view = findViewById(R.layout.activity_main.);

        //startActivity( new Intent(this, CameraSetup.class) );

        /*
        if (haveSSHKey()) {
            ssh.execute();
        }
        */
        //mLogger.log("return;");
    //}

    /*
    public void setView(View view) {
        setContentView(view);
    }
    */

    // ---------- CAMERA STUFF ------------------
    //public String filename = Environment.getExternalStorageDirectory()+"/ShRAMP_PIC.jpg";
    //public final File file = new File(filename);

/*
    public void createStillSession() {
        final String LOCAL_TAG = TAG.concat(".createStillSession()");
        Log.e(LOCAL_TAG, "Trying to capture still");

            image_reader = ImageReader.newInstance(image_dimensions[0].getWidth(), image_dimensions[0].getHeight(), image_format, 1);
            capture_request_builder.addTarget(image_reader.getSurface());
            List<Surface> output_surfaces = new ArrayList<Surface>(1);
            output_surfaces.add(image_reader.getSurface());
            ImageReader.OnImageAvailableListener reader_listener =
                    new ImageReader.OnImageAvailableListener() {
                final String LOCAL_TAG = TAG.concat(".createStillSession.ImageReader.OnImageAvailableListener()");

  */


    // SSHrampSession is an AsyncTask, holding this reference allows main to
    // see the result when it finishes.
    // It's linked to this main activity in onCreate below.
    //public static SSHrampSession SSHrampSession_reference = new SSHrampSession();

    /*
    public void upload() {

        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append("Uploading to craydata.ps.uci.edu..  \n");

        if (haveSSHKey()) {
            SSHrampSession_reference.execute(filename);
        }
        else {
            textOut.append("\t shit, ssh fail.");
        }
    }
    */

        /**
         * Tests if .ssh folder exists and can read it.
         * @return true (yes) or false (no)
         */
    public boolean haveSSHKey() {
        String ssh_path = Environment.getExternalStorageDirectory() + "/.ssh";
        File file_obj = new File(ssh_path);
        return file_obj.canRead();
    }

    /**
     * Implements the AsyncResponse interface.
     * Called after an SSHrampSession operation is completed as an AsyncTask.
     * @param status a string of information to give back to the Activity.
     */
    @Override
    public void processFinish(String status){
        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append(status);
    }


    /**
     * TODO
     */
    public void quit() {
        finish();
    }
}