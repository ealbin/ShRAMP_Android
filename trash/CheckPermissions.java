package recycle_bin;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import sci.crayfis.shramp.MaineShRAMP;
import sci.crayfis.shramp.ui.ChatterBox;

public class CheckPermissions {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // debug Logcat strings
    private final static String     TAG = "CheckPermissions";
    private final static String DIVIDER = "---------------------------------------------";

    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSION_CODE = 0; // could be anything >= 0

    // Reference to MaineShRAMP activity context, set by CheckAPI constructor
    private static MaineShRAMP mMaine_shramp;
    private static Runnable    mPermission_granted_action;
    private static Runnable    mQuit_action;


    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Instantiate CheckPermissions object
     * @param main_shramp MaineShRAMP reference to MaineShRAMP activity
     * @param action Runnable reference to method to run after permissions granted
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP) // 21
    CheckPermissions(@NonNull MaineShRAMP main_shramp,
                     @NonNull Runnable action, @NonNull Runnable quit_action) {
        final String LOCAL_TAG = TAG.concat("CheckPermissions(MaineShRAMP, Runnable)");
        Log.e(LOCAL_TAG, DIVIDER);

        mMaine_shramp = main_shramp;
        mPermission_granted_action = action;
        mQuit_action = quit_action;

        // if API 22 or below, user would have granted permissions on start
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(LOCAL_TAG, "API 22 or below, permissions granted on start");
            mPermission_granted_action.run();
            Log.e(LOCAL_TAG, "RETURN");
            return;
        }

        // if API > 22, ask
        if ( hasPermissions() ) {
            Log.e(LOCAL_TAG, "Permissions granted");
            Log.e(LOCAL_TAG, "Passing control to mPermission_granted_action");
            mPermission_granted_action.run();
            Log.e(LOCAL_TAG, "RETURN");
        }
        else {
            Log.e(LOCAL_TAG, "Asking permissions");
            mMaine_shramp.requestPermissions(PERMISSIONS, PERMISSION_CODE);
            Log.e(LOCAL_TAG, "RETURN");
        }
    }

    /**
     * Check for app permissions being granted.
     * @return true if all permissions granted, false if not.
     */
    @TargetApi(Build.VERSION_CODES.M) // 23
    private boolean hasPermissions() {
        final String LOCAL_TAG = TAG.concat(".hasPermissions()");
        Log.e(LOCAL_TAG, DIVIDER);

        Log.e(LOCAL_TAG, "Checking permissions:");
        for (String permission : PERMISSIONS) {
            int permission_value = mMaine_shramp.checkSelfPermission(permission);

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
     * @param requestCode permission code, see PERMISSION_CODE
     * @param permissions permissions requested
     * @param grantResults user's response
     */
    @TargetApi(Build.VERSION_CODES.M) // 23
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        final String LOCAL_TAG = TAG.concat(".onRequestPermissionsResult()");
        Log.e(LOCAL_TAG, DIVIDER);

        if ( hasPermissions() ) {
            Log.e(LOCAL_TAG, "All permissions granted");
            Log.e(LOCAL_TAG, "calling MaineShRAMP.startApp()");
            mPermission_granted_action.run();
            Log.e(LOCAL_TAG, "RETURN");
        }
        else {
            Log.e(LOCAL_TAG, "Notifying user no permissions = no app");

            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Womp womp";
            String message = "Thank you for trying ShRAMP, but" + "\n"
                    + "No granted permissions," + "\n"
                    + "No app";
            String button = "Accept defeat";
            chatterbox.displayBasicAlert(title, message, button, mQuit_action);
            Log.e(LOCAL_TAG, "RETURN");
        }
    }
}
