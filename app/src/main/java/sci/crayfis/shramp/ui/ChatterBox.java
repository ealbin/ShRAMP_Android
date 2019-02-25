package sci.crayfis.shramp.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import sci.crayfis.shramp.MaineShRAMP;

@TargetApi(Build.VERSION_CODES.LOLLIPOP) // 21
public class ChatterBox {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // Logcat strings
    private final static String     TAG = "ChatterBox";
    private final static String DIVIDER = "---------------------------------------------";

    // Reference to MaineShRAMP activity context, set by ChatterBox constructor
    private static MaineShRAMP mMaine_shramp;

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Initialize dialogs class
     * @param maine_shramp connects this context back to the main activity
     */
    public ChatterBox(@NonNull MaineShRAMP maine_shramp) {
        final String LOCAL_TAG = TAG.concat("ChatterBox(MaineShRAMP)");
        Log.e(LOCAL_TAG, DIVIDER);

        mMaine_shramp = maine_shramp;
        Log.e(LOCAL_TAG, "RETURN");
    }

    /**
     * Basic alert, aka title, message, and a neutral button.
     * Clicking the button doesn't do anything, just acknowledges alert has been read.
     * @param title the title of the alert
     * @param message the message of the alert
     * @param button the button text
     * @param action instance method to run when button is pressed
     */
    public void displayBasicAlert(@NonNull String title, @NonNull String message,
                                  @NonNull String button, @NonNull final Runnable action) {

        final String LOCAL_TAG = TAG.concat("displayBasicAlert(String,String,String)");
        Log.e(LOCAL_TAG, DIVIDER);

        AlertDialog.Builder alert_builder = new AlertDialog.Builder(mMaine_shramp);
        alert_builder.setTitle(title);
        alert_builder.setMessage(message);

        alert_builder.setNeutralButton(
                button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(LOCAL_TAG, "Neutral button has been pressed");
                        Log.e(LOCAL_TAG, "RETURN");
                        action.run();
                    }
                }
                );

        Log.e(LOCAL_TAG, "Showing alert");
        alert_builder.show();
        Log.e(LOCAL_TAG, "RETURN");
    }

}
