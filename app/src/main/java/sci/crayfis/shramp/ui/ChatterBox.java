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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;


////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Ignore this -- it's a relic of an earlier effort.. probably will be re-worked or removed
// in the near future...

/**
 * Displays a pop-up message to the user
 */
@TargetApi(21)
public class ChatterBox {

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Reference to running Activity, set by ChatterBox constructor
    private Activity mActivity;

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Initialize dialogs class
     * @param activity connects this context back to the main activity
     */
    public ChatterBox(@NonNull Activity activity) {
        mActivity = activity;
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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

        AlertDialog.Builder alert_builder = new AlertDialog.Builder(mActivity);
        alert_builder.setTitle(title);
        alert_builder.setMessage(message);

        alert_builder.setNeutralButton(
                button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action.run();
                    }
                }
                );
        alert_builder.show();
    }

}