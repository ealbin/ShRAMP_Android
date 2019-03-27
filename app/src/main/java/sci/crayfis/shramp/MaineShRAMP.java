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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.system.Os;
import android.system.StructUtsname;
import android.widget.TextView;

import java.io.File;

import sci.crayfis.shramp.util.BuildString;
import sci.crayfis.shramp.logging.ShrampLogger;
import sci.crayfis.shramp.ssh.AsyncResponse;
import sci.crayfis.shramp.error.FailManager;



////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      UNDER CONSTRUCTION
////////////////////////////////////////////////////////////////////////////////////////////////////



/**
 * Entry point for the ShRAMP app
 * Checks permissions then runs MasterController
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

        //Log.e(Thread.currentThread().getName(), "MaineShRAMP onCreate");

       mNextActivity = new Intent(this, MasterController.class);
       mFailActivity = new Intent(this, FailManager.class);

       mNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       mFailActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        MaineShRAMP.mLogger.log("Welcome to the Shower Reconstruction Application for Mobile Phones");
        MaineShRAMP.mLogger.log("or \"ShRAMP\" for short");

        // Get build info
        String buildString = BuildString.get();
        MaineShRAMP.mLogger.log(buildString);

        StructUtsname uname = Os.uname();
        String unameString = " \n"
                + "Machine:   " + uname.machine  + "\n"
                + "Node name: " + uname.nodename + "\n"
                + "Release:   " + uname.release  + "\n"
                + "Sysname:   " + uname.sysname  + "\n"
                + "Version:   " + uname.version  + "\n";
        MaineShRAMP.mLogger.log(unameString);

        String buildDetails = " \n"
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
                + "User:                    " + Build.USER                + "\n";
        MaineShRAMP.mLogger.log(buildDetails);


        // if API 22 or below, user would have granted permissions on start
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            MaineShRAMP.mLogger.log("API 22 or below, permissions granted on start");
            MaineShRAMP.mLogger.log("Starting MasterController");
            //Log.e(Thread.currentThread().getName(), "MaineShRAMP -> NextActivity");
            //Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            super.startActivity(this.mNextActivity);
            //finish();
        }
        else {
            // if API > 22
            if (permissionsGranted()) {
                //MaineShRAMP.mLogger.log("API 23 or above, and permissions have previously been granted");
                //MaineShRAMP.mLogger.log("Starting MasterController");
                //Log.e(Thread.currentThread().getName(), "MaineShRAMP -> NextActivity");
                //Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
                super.startActivity(this.mNextActivity);
                //finish();
            }
            else {
                //MaineShRAMP.mLogger.log("API 23 or above, but permissions not granted, asking permissions");
                // response to request is handled in onRequestPermissionsResult()
                super.requestPermissions(PERMISSIONS, PERMISSION_CODE);
            }
        }
    }

    public void finish() {
        //Log.e(Thread.currentThread().getName(), "MaineShRAMP finish");
    }

    //----------------------------------------------------------------------------------------------

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
                MaineShRAMP.mLogger.log(permission + ": " + "DENIED");
                allGranted = false;
            }
            else {
                MaineShRAMP.mLogger.log(permission + ": " + "GRANTED");
            }
        }

        if (allGranted) {
            MaineShRAMP.mLogger.log("All permissions granted");
        }
        else {
            MaineShRAMP.mLogger.log("Some or all permissions denied");
        }

        MaineShRAMP.mLogger.log("permissionsGranted? return: " + Boolean.toString(allGranted));
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
        if (this.permissionsGranted()) {
            MaineShRAMP.mLogger.log("Permissions asked and granted");
            //MaineShRAMP.mLogger.log("Starting MasterController");
            //Log.e(Thread.currentThread().getName(), "MaineShRAMP -> NextActivity");
            //Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            super.startActivity(mNextActivity);
            //finish();
        }
        else {
            MaineShRAMP.mLogger.log("Permissions were not granted");
            //MaineShRAMP.mLogger.log("Starting FailManager");
            //Log.e(Thread.currentThread().getName(), "MaineShRAMP -> FailActivity");
            //Log.e(Thread.currentThread().getName(), ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            super.startActivity(mFailActivity);
            //finish();
        }
        //MaineShRAMP.mLogger.log("return;");
    }



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