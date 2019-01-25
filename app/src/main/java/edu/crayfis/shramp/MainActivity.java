package edu.crayfis.shramp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;
import android.view.Surface;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AsyncResponse {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "MainActivity";
    private final static String DIVIDER = "---------------------------------------------";

    // Permissions Required
    public static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };
    public static final int PERMISSION_CODE = 0; // could be anything >= 0

    // Minimum version = 23, Android 6.0 "Marshmellow" (2015)
    public static final int MIN_SDK = Build.VERSION_CODES.M;

    // SSH is an AsyncTask, holding this reference allows main to
    // see the result when it finishes.
    // It's linked to this main activity in onCreate below.
    SSH SSH_reference = new SSH();


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
        Log.e(LOCAL_TAG, "Welcome to the app!");

        // set ssh listener to this class for information exchange
        SSH_reference.mainactivity = this;

        // Main screen
        setContentView(R.layout.activity_main);

        // If permissions have been granted, run the app.
        // Execution continues in onRequestPermissionsResult
        if ( outdatedAndroidVersion() ) {
            // TODO: notify user the version won't work
            Log.e(LOCAL_TAG, "Outdated Android Version");
            Log.e(LOCAL_TAG, "QUITTING");
            finish();
            return;
        }

        if ( hasPermissions() ) {
            Log.e(LOCAL_TAG, "Permissions granted, starting app...");
            startApp();
        }
        else {
            Log.e(LOCAL_TAG, "Asking permissions");
            requestPermissions(PERMISSIONS, PERMISSION_CODE);
        }

        Log.e(LOCAL_TAG, "END");
    }

    /**
     * Returns Android build version number, codename and date initially released.
     * @param build_code e.g. Build.VERSION.SDK_INT
     * @return a string of the form "vX.X \"Codename\" (Month Year)"
     */
    protected String getBuildString(int build_code) {
        final String LOCAL_TAG = TAG.concat(".getBuildString()");
        Log.e(LOCAL_TAG, DIVIDER);

        String api = Integer.toString(build_code);
        String build_string;
        switch (build_code) {
            case Build.VERSION_CODES.BASE :
                build_string = "v1.0 API " + api + " \"Base\" (October 2008)";
                break;

            case Build.VERSION_CODES.BASE_1_1 :
                build_string = "v1.1 API " + api + " \"Base 1.1\" (February 2009)";
                break;

            case Build.VERSION_CODES.CUPCAKE :
                build_string = "v1.5 API " + api + " \"Cupcake\" (May 2009)";
                break;

            case Build.VERSION_CODES.DONUT :
                build_string = "v1.6 API " + api + " \"Donut\" (September 2009)";
                break;

            case Build.VERSION_CODES.ECLAIR :
                build_string = "v2.0 API " + api + " \"Eclair\" (November 2009)";
                break;

            case Build.VERSION_CODES.ECLAIR_0_1 :
                build_string = "v2.0.1 API " + api + " \"Eclair 0.1\" (December 2009)";
                break;

            case Build.VERSION_CODES.ECLAIR_MR1 :
                build_string = "v2.1 API " + api + " \"Eclair MR1\" (January 2010)";
                break;

            case Build.VERSION_CODES.FROYO :
                build_string = "v2.2 API " + api + " \"Froyo\" (June 2010)";
                break;

            case Build.VERSION_CODES.GINGERBREAD :
                build_string = "v2.3 API " + api + " \"Gingerbread\" (November 2010)";
                break;

            case Build.VERSION_CODES.GINGERBREAD_MR1 :
                build_string = "v2.3.3 API " + api + " \"Gingerbread MR1\" (February 2011)";
                break;

            case Build.VERSION_CODES.HONEYCOMB :
                build_string = "v3.0 API " + api + " \"Honeycomb\" (February 2011)";
                break;

            case Build.VERSION_CODES.HONEYCOMB_MR1 :
                build_string = "v3.1 API " + api + " \"Honeycomb MR1\" (May 2011)";
                break;

            case Build.VERSION_CODES.HONEYCOMB_MR2 :
                build_string = "v3.2 API " + api + " \"Honeycomb MR2\" (June 2011)";
                break;

            case Build.VERSION_CODES.ICE_CREAM_SANDWICH :
                build_string = "v4.0 API " + api + " \"Ice Cream Sandwich\" (October 2011)";
                break;

            case Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 :
                build_string = "v4.0.3 API " + api + " \"Ice Cream Sandwich MR1\" (December 2011)";
                break;

            case Build.VERSION_CODES.JELLY_BEAN :
                build_string = "v4.1 API " + api + " \"Jelly Bean\" (June 2012)";
                break;

            case Build.VERSION_CODES.JELLY_BEAN_MR1 :
                build_string = "v4.2 API " + api + " \"Jelly Bean MR1\" (November 2012)";
                break;

            case Build.VERSION_CODES.JELLY_BEAN_MR2 :
                build_string = "v4.3 API " + api + " \"Jelly Bean MR2\" (July 2013)";
                break;

            case Build.VERSION_CODES.KITKAT :
                build_string = "v4.4 API " + api + " \"KitKat\" (October 2013)";
                break;

            case Build.VERSION_CODES.KITKAT_WATCH :
                build_string = "v4.4W API " + api + " \"KitKat\" (June 2014)";
                break;

            case Build.VERSION_CODES.LOLLIPOP :
                build_string = "v5.0 API " + api + " \"Lollipop\" (November 2014)";
                break;

            case Build.VERSION_CODES.LOLLIPOP_MR1 :
                build_string = "v5.1 API " + api + " \"Lollipop MR1\" (March 2015)";
                break;

            case Build.VERSION_CODES.M :
                build_string = "v6.0 API " + api + " \"Marshmellow\" (October 2015)";
                break;

            case Build.VERSION_CODES.N :
                build_string = "v7.0 API " + api + " \"Nougat\" (August 2016)";
                break;

            case Build.VERSION_CODES.N_MR1 :
                build_string = "v7.1 API " + api + " \"Nougat MR1\" (October 2016)";
                break;

            case Build.VERSION_CODES.O :
                build_string = "v8.0 API " + api + " \"Oreo\" (August 2017)";
                break;

            case Build.VERSION_CODES.O_MR1 :
                build_string = "v8.1 API " + api + " \"Oreo MR1\" (December 2017)";
                break;

            case Build.VERSION_CODES.P :
                build_string = "v9.0 API " + api + " \"Pie\" (August 2018)";
                break;

            default :
                if (build_code > Build.VERSION_CODES.P) {
                    build_string = "version is post v9.0: API " + api;
                }
                else {
                    build_string = "unknown version code: API " + api;
                }
                break;
        }

        Log.e(LOCAL_TAG, "Build: " + build_string);
        Log.e(LOCAL_TAG, "RETURN");
        return build_string;
    }

    /**
     * Check for minimum Android version requirement.
     * @return true if too old, false if modern
     */
    protected boolean outdatedAndroidVersion() {
        final String LOCAL_TAG = TAG.concat(".outdatedAndroidVersion()");
        Log.e(LOCAL_TAG, DIVIDER);

        int build_code = Build.VERSION.SDK_INT;
        String build_string = getBuildString(build_code);
        if (build_code < MIN_SDK) {
            Log.e(LOCAL_TAG,"Build version is too old: " + build_string);
            Log.e(LOCAL_TAG,"RETURN");
            return true;
        }
        Log.e(LOCAL_TAG, "Build version checks out: " + build_string);
        Log.e(LOCAL_TAG, "RETURN");
        return false;
    }

    /**
     * Check for app permissions being granted.
     * @return true if all permissions granted, false if not.
     */
    protected boolean hasPermissions() {
        final String LOCAL_TAG = TAG.concat(".hasPermissions()");
        Log.e(LOCAL_TAG, DIVIDER);

        Log.e(LOCAL_TAG, "Checking permissions:");
        for (String permission : PERMISSIONS) {
            int permission_value = checkSelfPermission(permission);

            if (permission_value == PackageManager.PERMISSION_DENIED) {
                Log.e(LOCAL_TAG, permission + ": " + "DENIED");
                Log.e(LOCAL_TAG, "RETURN");
                return false;
            }
            Log.e(LOCAL_TAG, permission + ": " + "GRANTED");
        }

        Log.e(LOCAL_TAG, "All permissions granted");
        Log.e(LOCAL_TAG, "RETURN");
        return true;
    }

    /**
     * After user responds to permission request, this routine is called.
     * If permissions are granted, continue with the app, otherwise die.
     *
     * @param requestCode permission code, see askPermission()
     * @param permissions permissions requested
     * @param grantResults user's response
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String LOCAL_TAG = TAG.concat(".onRequestPermissionsResult()");
        Log.e(LOCAL_TAG, DIVIDER);

        if ( hasPermissions() ) {
            Log.e(LOCAL_TAG, "Permissions granted, starting app...");
            startApp();
        }
        else {
            // TODO: notify user the version won't work
            Log.e(LOCAL_TAG, "Outdated Android Version");
            Log.e(LOCAL_TAG, "QUITTING");
            finish();
            return;
        }
        Log.e(LOCAL_TAG, "END");
    }

    /**
     * After permissions are granted, begin app operations here.
     */
    public void startApp(){
        final String LOCAL_TAG = TAG.concat(".startApp()");
        Log.e(LOCAL_TAG, DIVIDER);

        //Intent intent = new Intent(this, DAQActivity.class);
        //startActivity(intent);

        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append("Welcome to ShRAMP!\n");
        textOut.append("(Shower Reconstructing Array of Mobile Phones)\n");
        textOut.append("----------------------------------------------------------\n\n");
        textOut.append("Capturing a camera frame..  \n");

        setUpCamera();

        //startActivity( new Intent(this, Camera.class) );

        /*
        if (haveSSHKey()) {
            TextView textOut = (TextView) findViewById(R.id.textOut);

            //ssh.execute();
            textOut.append("Doing good");
            //camera.openCamera();

            Context context = getApplicationContext();
            CharSequence text = "Cheers!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            //toast.setGravity(Gravity.TOP| Gravity.LEFT, 0, 0);
            toast.show();
        }
        */
        // TODO: dies if no access
    }


    // ---------- CAMERA STUFF ------------------
    public String filename = Environment.getExternalStorageDirectory()+"/ShRAMP_PIC.jpg";
    public final File file = new File(filename);


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

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.e(LOCAL_TAG, "reading image");
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        Log.e(LOCAL_TAG, "file not found");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e(LOCAL_TAG, "IOexception");
                        e.printStackTrace();
                    } finally {
                        Log.e(LOCAL_TAG, "finally");
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    Log.e(LOCAL_TAG, "SAVING IMAGE!");
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);

                        TextView textOut = (TextView) findViewById(R.id.textOut);
                        textOut.append("\t saved: " + file + "\n\n");
                        upload();

                    } finally {
                        Log.e(LOCAL_TAG, "finally");
                        if (null != output) {
                            output.close();
                        }
                        camera_device.close();
                        stopBackgroundThread();

                        Log.e(LOCAL_TAG, "Leaving save() for upload()");
                        Log.e(LOCAL_TAG, DIVIDER);
                    }
                }
            };

            image_reader.setOnImageAvailableListener(reader_listener, background_handler);

            camera_device.createCaptureSession(output_surfaces, new CameraCaptureSession.StateCallback() {
                final String LOCAL_TAG = TAG.concat(".createStillImage.CameraCaptureSession");

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.e(LOCAL_TAG, "onConfigured()");
                    try {
                        session.capture(capture_request_builder.build(), capture_listener, background_handler);
                    } catch (CameraAccessException e) {
                        Log.e(LOCAL_TAG,"camera access exception");
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(LOCAL_TAG, "onConfigureFailed");
                }
            }, background_handler);
    }


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
}