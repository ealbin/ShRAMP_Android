package edu.crayfis.shramp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
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
    private final static String TAG = "MainActivity";
    private final static String DIVIDER = "---------------------------------------------";

    // SSH is an AsyncTask, holding this reference allows main to
    // see the result when it finishes.
    // It's linked to this main activity in onCreate below.
    SSH ssh = new SSH();

    // Access to camera
    //Camera camera = new Camera();

    /**
     * Entry point for the app at start.
     * @param savedInstanceState passed in by Android OS
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String LOCAL_TAG = TAG.concat(".onCreate()");
        Log.e(LOCAL_TAG, "Welcome to the app!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set ssh listener to this class
        ssh.mainactivity = this;


        // If permissions have been granted, run the app.
        // Execution continues in onRequestPermissionsResult
        Log.e(LOCAL_TAG, "Leaving onCreate() for askPermission()");
        Log.e(LOCAL_TAG, DIVIDER);
        askPermission();
    }

    /**
     * Asks user for necessary permissions.
     * Execution continues after user response in onRequestPermissionsResult().
     */
    public void askPermission() {
        final String LOCAL_TAG = TAG.concat(".askPermission()");
        Log.e(LOCAL_TAG, "Asking permissions");

        final String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };
        final int PERMISSION_CODE = 1; // could be anything >= 0

        // Ask user for permission
        Log.e(LOCAL_TAG, "Leaving askPermission() for onRequestPermissionsResult()");
        Log.e(LOCAL_TAG, DIVIDER);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
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
        final String LOCAL_TAG = TAG.concat(".onRequestPermissionsResult()");
        Log.e(LOCAL_TAG, "Testing if permissions have been granted");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check if permission is now granted
        if ( checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            Log.e(LOCAL_TAG, "Permissions have been granted");
            Log.e(LOCAL_TAG, "Leaving onRequestPermissionsResult() for startApp()");
            Log.e(LOCAL_TAG, DIVIDER);
            startApp();
        }
        else {
            Log.e(LOCAL_TAG, "Permissions ARE NOT granted, game over man");
        }
    }

    /**
     * After permissions are granted, begin app operations here.
     */
    public void startApp(){
        final String LOCAL_TAG = TAG.concat(".startApp()");
        Log.e(LOCAL_TAG, "Leaving startApp() for setUpCamera()");
        Log.e(LOCAL_TAG, DIVIDER);

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

    // int to denote front vs back camera
    final int FRONT_CAMERA = CameraCharacteristics.LENS_FACING_FRONT;
    final int BACK_CAMERA  = CameraCharacteristics.LENS_FACING_BACK;

    public String camera_id;
    public CameraManager camera_manager;
    public HandlerThread background_thread;
    public Handler background_handler;
    public CameraDevice.StateCallback state_callback;
    public CameraDevice camera_device;
    public CaptureRequest capture_request;
    public CaptureRequest.Builder capture_request_builder;
    public CameraCaptureSession camera_capture_session;
    public ImageReader image_reader;
    public Size[] image_dimensions;
    public int image_format;
    public String filename = Environment.getExternalStorageDirectory()+"/ShRAMP_PIC.jpg";
    public final File file = new File(filename);


    public void setUpCamera() {
        final String LOCAL_TAG = TAG.concat(".setUpCamera()");
        Log.e(LOCAL_TAG, "trying to set up camera...");

        camera_manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // find camera_id for the back camera
            for (String camera_id : camera_manager.getCameraIdList()) {

                CameraCharacteristics cameraCharacteristics =
                        camera_manager.getCameraCharacteristics(camera_id);

                // select the back camera
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        BACK_CAMERA) {
                    Log.e(LOCAL_TAG, "found back camera, ID = " + camera_id);
                    this.camera_id = camera_id;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(LOCAL_TAG, "EXCEPTION!  FAIL!");
        }

        Log.e(LOCAL_TAG, "Instantiating stateCallback");
        state_callback = new CameraDevice.StateCallback() {
            final String LOCAL_TAG = TAG.concat(".CameraDevice.StateCallback");

            @Override
            public void onOpened(CameraDevice cameraDevice) {
                Log.e(LOCAL_TAG, "onOpened()");
                MainActivity.this.camera_device = cameraDevice;
                Log.e(LOCAL_TAG, "Leaving onOpened() for createStillSession()");
                Log.e(LOCAL_TAG, DIVIDER);
                createStillSession();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                Log.e(LOCAL_TAG, "onDisconnected");
                cameraDevice.close();
                MainActivity.this.camera_device = null;
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                Log.e(LOCAL_TAG, "onError");
                cameraDevice.close();
                MainActivity.this.camera_device = null;
            }
        };

        Log.e(LOCAL_TAG, "Leaving setUpCamera() for openBackgroundThread()");
        Log.e(LOCAL_TAG, DIVIDER);
        openBackgroundThread();
    }

    public void openBackgroundThread() {
        final String LOCAL_TAG = TAG.concat(".openBackgroundThread()");
        Log.e(LOCAL_TAG, "Setting up handlers");
        background_thread = new HandlerThread("camera_background_thread");
        background_thread.start();
        background_handler = new Handler(background_thread.getLooper());
        Log.e(LOCAL_TAG, "Leaving openBackgroundThread() for openCamera()");
        Log.e(LOCAL_TAG, DIVIDER);
        openCamera();
    }

    public void stopBackgroundThread() {
        final String LOCAL_TAG = TAG.concat(".openBackgroundThread()");
        Log.e(LOCAL_TAG, "Stopping handlers");
        background_thread.quitSafely();
        try {
            background_thread.join();
            background_thread = null;
            background_handler = null;
        } catch (InterruptedException e) {
            Log.e(LOCAL_TAG, "ERROR");
        }
    }
    public void openCamera() {
        final String LOCAL_TAG = TAG.concat(".openCamera()");
        Log.e(LOCAL_TAG, "Trying to open camera");

        try {
            Log.e(LOCAL_TAG, "Leaving openCamera() for state_callback.onOpened() if all goes well");
            Log.e(LOCAL_TAG, DIVIDER);

            image_format = ImageFormat.JPEG;
            CameraCharacteristics characteristics = camera_manager.getCameraCharacteristics(camera_id);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            image_dimensions = map.getOutputSizes(image_format);
            //image_dimensions = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.RAW_SENSOR);
            Log.e(LOCAL_TAG, "width, height = " + Integer.toString(image_dimensions[0].getWidth()) + ", " + Integer.toString(image_dimensions[0].getHeight()));
            camera_manager.openCamera(camera_id, state_callback, background_handler);

        } catch (CameraAccessException e) {
            Log.e(LOCAL_TAG, "EXCEPTION!  FAIL!");
        }
        Log.e(LOCAL_TAG, "Leaving openCamera() after try-catch block");
    }

    public void createStillSession() {
        final String LOCAL_TAG = TAG.concat(".createStillSession()");
        Log.e(LOCAL_TAG, "Trying to capture still");

        try {
            image_reader = ImageReader.newInstance(image_dimensions[0].getWidth(), image_dimensions[0].getHeight(), image_format, 1);
            capture_request_builder = camera_device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            capture_request_builder.addTarget(image_reader.getSurface());
            capture_request_builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            //Range<Integer> fps_range = new Range(1,2);
            //capture_request_builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps_range);
            capture_request_builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 100);
            capture_request_builder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);

            List<Surface> output_surfaces = new ArrayList<Surface>(1);
            output_surfaces.add(image_reader.getSurface());

            Log.e(LOCAL_TAG, "implementing interfaces");
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
            Log.e(LOCAL_TAG, "reader_listener has been implemented");

            image_reader.setOnImageAvailableListener(reader_listener, background_handler);

            Log.e(LOCAL_TAG, "implementing capture_listener");
            final CameraCaptureSession.CaptureCallback capture_listener = new CameraCaptureSession.CaptureCallback() {
                final String LOCAL_TAG = TAG.concat(".createStillImage.CameraCaptureSession.CaptureCallback");

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    Log.e(LOCAL_TAG, "onCaptureCompleted()");
                    super.onCaptureCompleted(session, request, result);
                    //Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                }
            };
            Log.e(LOCAL_TAG, "capture_listener has been implemented");

            Log.e(LOCAL_TAG, "creating capture session");
            Log.e(LOCAL_TAG, DIVIDER);

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


            Log.e(LOCAL_TAG, "pengas");
            //camera_capture_session = new CameraCaptureSession();
            //CameraActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest,
            //        null, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(LOCAL_TAG, "errored out");
            e.printStackTrace();
        }
    }


    public void upload() {
        final String LOCAL_TAG = TAG.concat(".upload()");
        Log.e(LOCAL_TAG, "yay!  uploading at last");

        TextView textOut = (TextView) findViewById(R.id.textOut);
        textOut.append("Uploading to craydata.ps.uci.edu..  \n");

        if (haveSSHKey()) {
            ssh.execute(filename);
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