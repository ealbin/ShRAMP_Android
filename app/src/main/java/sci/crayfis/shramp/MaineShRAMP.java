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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.system.Os;
import android.system.StructUtsname;
import android.util.Log;

import sci.crayfis.shramp.util.BuildString;
import sci.crayfis.shramp.error.FailManager;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Right now, this doesn't do much..
// The app starts with onCreate(), and this class logs basic device metadata and asks permissions
// before handing full control over to MasterController.
// For the future, I haven't decided exactly what else I want this to do, or if it should just
// be part of MasterController..

/**
 * Entry point for the ShRAMP app
 * Checks permissions then hands control over to MasterController
 * AsyncResponse is for SSH data transfer, currently disabled and probably going to be moved
 * out of this class.
 */
@TargetApi(21)
public final class MaineShRAMP extends Activity { //implements AsyncResponse {

    // Public Class Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // PERMISSIONS and PERMISSION_CODE..............................................................
    // The list of device permissions needed for this app to operate.
    // Consider moving this over to GlobalSettings..
    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSION_CODE = 0; // could be anything >= 0

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mNextActivity and mFailActivity..............................................................
    // Where to pass control of the app over to.  Set in onCreate()
    private Intent mNextActivity;
    private Intent mFailActivity;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // onCreate.....................................................................................
    /**
     * Entry point for the app at start.
     * @param savedInstanceState passed in by Android OS for returning from a suspended state
     *                           (not used)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNextActivity = new Intent(this, MasterController.class);
        mFailActivity = new Intent(this, FailManager.class);

        // Setting this flag destroys MaineShRAMP after passing control over to one of these new
        // intents
        mNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mFailActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Log.e(Thread.currentThread().getName(), "Welcome to the Shower Reconstruction Application for Mobile Phones");
        Log.e(Thread.currentThread().getName(), "or \"ShRAMP\" for short");

        // Log build info
        String buildString = BuildString.get();
        Log.e(Thread.currentThread().getName(), buildString);

        // Log device info
        StructUtsname uname = Os.uname();
        String unameString = " \n\n"
                + "Machine:   " + uname.machine  + "\n"
                + "Node name: " + uname.nodename + "\n"
                + "Release:   " + uname.release  + "\n"
                + "Sysname:   " + uname.sysname  + "\n"
                + "Version:   " + uname.version  + "\n\n";
        Log.e(Thread.currentThread().getName(), unameString);

        // Log hardware info
        String buildDetails = " \n\n"
                + "Underlying board:        " + Build.BOARD               + "\n"
                + "Bootloader version:      " + Build.BOOTLOADER          + "\n"
                + "Brand:                   " + Build.BRAND               + "\n"
                + "Industrial device:       " + Build.DEVICE              + "\n"
                + "Build fingerprint:       " + Build.FINGERPRINT         + "\n"
                + "Hardware:                " + Build.HARDWARE            + "\n"
                + "Host:                    " + Build.HOST                + "\n"
                + "Changelist label/number: " + Build.ID                  + "\n"
                + "Hardware manufacturer:   " + Build.MANUFACTURER        + "\n"
                + "Model:                   " + Build.MODEL               + "\n"
                + "Product name:            " + Build.PRODUCT             + "\n"
                + "Radio firmware version:  " + Build.getRadioVersion()   + "\n"
                + "Build tags:              " + Build.TAGS                + "\n"
                + "Build time:              " + Long.toString(Build.TIME) + "\n"
                + "Build type:              " + Build.TYPE                + "\n"
                + "User:                    " + Build.USER                + "\n\n";
        Log.e(Thread.currentThread().getName(), buildDetails);

        // if the API was 22 or below, the user would have granted permissions on start
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(Thread.currentThread().getName(), "API 22 or below, permissions granted on start");
            Log.e(Thread.currentThread().getName(), "Starting MasterController");
            super.startActivity(this.mNextActivity);
        }
        else {
            // if API > 22
            if (permissionsGranted()) {
                super.startActivity(this.mNextActivity);
            }
            else {
                // Execution resumes with onRequestPermissionResult() below
                super.requestPermissions(PERMISSIONS, PERMISSION_CODE);
            }
        }
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // permissionsGranted...........................................................................
    /**
     * Check if permissions have been granted
     * @return true if all permissions have been granted, false if not
     */
    @TargetApi(23)
    private boolean permissionsGranted() {
        boolean allGranted = true;

        for (String permission : MaineShRAMP.PERMISSIONS) {
            int permission_value = checkSelfPermission(permission);

            if (permission_value == PackageManager.PERMISSION_DENIED) {
                Log.e(Thread.currentThread().getName(), permission + ": " + "DENIED");
                allGranted = false;
            }
            else {
                Log.e(Thread.currentThread().getName(), permission + ": " + "GRANTED");
            }
        }

        if (allGranted) {
            Log.e(Thread.currentThread().getName(), "All permissions granted");
        }
        else {
            Log.e(Thread.currentThread().getName(), "Some or all permissions denied");
        }

        return allGranted;
    }

    // onRequestPermissions.........................................................................
    /**
     * After user responds to permission request, this routine is called.
     * @param requestCode permission code, ref. PERMISSION_CODE field
     * @param permissions permissions requested
     * @param grantResults user's response
     */
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.permissionsGranted()) {
            Log.e(Thread.currentThread().getName(), "Permissions asked and granted");
            super.startActivity(mNextActivity);
        }
        else {
            Log.e(Thread.currentThread().getName(), "Permissions were not granted");
            super.startActivity(mFailActivity);
        }
    }


    // TODO: SSH stuff works, but isn't used at this moment as I work on the StorageMeda details
    // Also, probably going to to move this out of MaineShRAMP..
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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
    //public boolean haveSSHKey() {
    //    String ssh_path = Environment.getExternalStorageDirectory() + "/.ssh";
    //    File file_obj = new File(ssh_path);
    //    return file_obj.canRead();
    //}

    /**
     * Implements the AsyncResponse interface.
     * Called after an SSHrampSession operation is completed as an AsyncTask.
     * @param status a string of information to give back to the Activity.
     */
    //@Override
    //public void processFinish(String status){
    //    TextView textOut = (TextView) findViewById(R.id.textOut);
    //    textOut.append(status);
    //}

}