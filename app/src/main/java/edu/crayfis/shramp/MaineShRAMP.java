package edu.crayfis.shramp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import edu.crayfis.shramp.camera2.MaineShrampCamera;
import Trash.Camera;

@TargetApi(Build.VERSION_CODES.LOLLIPOP) // 21
public class MaineShRAMP extends Activity implements AsyncResponse, TextureView.SurfaceTextureListener {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "MaineShRAMP";
    private final static String DIVIDER = "---------------------------------------------";

    // SSH is an AsyncTask, holding this reference allows main to
    // see the result when it finishes.
    // It's linked to this main activity in onCreate below.
    public static SSH SSH_reference = new SSH();

    // initialized in onCreate()
    public static CheckAPI         mCheck_api;
    public static CheckPermissions mCheck_permissions;


    private  MaineShrampCamera mShrampCam;

    public Runnable mQuit_action = new Runnable() {
        @Override
        public void run() {
            quit();
        }
    };

    public static Camera mCamera;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Entry point for the app at start.
     * @param savedInstanceState passed in by Android OS
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // debug Logcat string
        final String LOCAL_TAG = TAG.concat(".onCreate()");

        // debug error stream
        Log.e(LOCAL_TAG,  DIVIDER);
        Log.e(LOCAL_TAG, "Welcome to Shower Reconstructing Array of Mobile Phones");
        Log.e(LOCAL_TAG, "or \"ShRAMP\" for short");

        // Main screen
        setContentView(R.layout.activity_main);

        // Check API compatibility
        mCheck_api = new CheckAPI(this, mQuit_action);

        // Check granted permissions, if granted, run startApp()
        Runnable next_action = new Runnable() {
            @Override
            public void run() {
                startApp();
            }
        };
        mCheck_permissions = new CheckPermissions(this, next_action, mQuit_action);

        Log.e(LOCAL_TAG, "RETURN");
    }


    /**
     * Passes permission result control back over to mCheck_permissions
     * @param requestCode permission code, see PERMISSION_CODE
     * @param permissions permissions requested
     * @param grantResults user's response
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        final String LOCAL_TAG = TAG.concat(".onRequestPermissionsResult()");
        Log.e(LOCAL_TAG, DIVIDER);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(LOCAL_TAG, "Passing control to mCheck_permissions");
        mCheck_permissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(LOCAL_TAG, "RETURN");
    }

    TextureView mTextureView;

    /**
     * After permissions are granted, begin app operations here.
     */
    public void startApp(){
        final String LOCAL_TAG = TAG.concat(".startApp()");
        Log.e(LOCAL_TAG, DIVIDER);

        // set ssh listener to this class for information exchange
        SSH_reference.mainactivity = this;

        //Intent intent = new Intent(this, DAQActivity.class);
        //startActivity(intent);

        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append("Welcome to ShRAMP!\n");
        textOut.append("(Shower Reconstructing Application for Mobile Phones)\n");
        textOut.append("----------------------------------------------------------\n\n");
        textOut.append("Capturing a camera frame..  \n");

        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(this);
        setView(mTextureView);


//        mCamera = new Camera(this, mQuit_action);

        //SurfaceView surface_view = findViewById(R.layout.activity_main.);

        //startActivity( new Intent(this, CameraSetup.class) );

        /*
        if (haveSSHKey()) {
            ssh.execute();
        }
        */
        Log.e(LOCAL_TAG, "RETURN");
    }

    public void setView(View view) {
        setContentView(view);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {

        Surface surface = new Surface(arg0);

        Log.e("================> ", "Creating camera via new MainShrampCamera()");
        mShrampCam = new MaineShrampCamera(this, surface);


    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,int arg2) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
    }

    // ---------- CAMERA STUFF ------------------
    public String filename = Environment.getExternalStorageDirectory()+"/ShRAMP_PIC.jpg";
    public final File file = new File(filename);

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

    public void upload() {
        final String LOCAL_TAG = TAG.concat(".upload()");
        Log.e(LOCAL_TAG, "yay!  uploading at last");

        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append("Uploading to craydata.ps.uci.edu..  \n");

        if (haveSSHKey()) {
            SSH_reference.execute(filename);
        }
        else {
            Log.e(LOCAL_TAG, "crap");
            textOut.append("\t shit, ssh fail.");
        }
    }


        /**
         * Tests if .ssh folder exists and can read it.
         * @return true (yes) or false (no)
         */
    public boolean haveSSHKey() {
        final String LOCAL_TAG = TAG.concat(".haveSSHKey");

        Log.e(LOCAL_TAG, "checking file access");
        String ssh_path = Environment.getExternalStorageDirectory() + "/.ssh";
        File file_obj = new File(ssh_path);
        return file_obj.canRead();
    }

    /**
     * Implements the AsyncResponse interface.
     * Called after an SSH operation is completed as an AsyncTask.
     * @param status a string of information to give back to the Activity.
     */
    @Override
    public void processFinish(String status){
        final String LOCAL_TAG = TAG.concat(".processFinish");
        Log.e(LOCAL_TAG, "We're done!");
        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append(status);
    }


    /**
     * TODO
     */
    public void quit() {
        finish();
        return;
    }
}