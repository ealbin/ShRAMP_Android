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
 * @updated: 29 April 2019
 */

package sci.crayfis.shramp.battery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

import sci.crayfis.shramp.MasterController;

/**
 * Base class for all battery status receivers
 */
@TargetApi(21)
abstract public class BatteryReceiver extends BroadcastReceiver {

    // Protected Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mActivity....................................................................................
    // A reference to the main activity running the app
    protected Activity mActivity;

    // mIntent......................................................................................
    // A reference to the last broadcasted battery data intent
    protected Intent mIntent;

    // mIntentString................................................................................
    // Needed to tell the system what kind of broadcast listener this is, e.g. Intent.ACTION_BATTERY_CHANGED
    protected String mIntentString;;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // BatteryReceiver..............................................................................
    /**
     * !! DO NOT CALL THIS !!
     * The default constructor has to be here to satisfy Android manifest requirements to receive
     * battery broadcast.
     */
    public BatteryReceiver() {}

    // BatteryReceiver..............................................................................
    /**
     * Register this broadcast listener with the system
     * @param activity Reference to the main activity running the app
     * @param intentString What kind of listener, e.g. Intent.ACTION_BATTERY_CHANGED
     */
    BatteryReceiver(@NonNull Activity activity, @NonNull String intentString) {
        mActivity       = activity;
        mIntentString   = intentString;
        mIntent         = activity.registerReceiver(this, new IntentFilter(mIntentString));
        if (mIntent == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Activity failed to register battery receiver");
            MasterController.quitSafely();
            return;
        }
        refresh();
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // refresh......................................................................................
    /**
     * Process last broadcasted battery information Intent
     */
    void refresh() {
        onReceive(mActivity, mIntent);
    }

    // getString....................................................................................
    /**
     * @return A string describing what is known by this object
     */
    @NonNull
    abstract String getString();

    // shutdown.....................................................................................
    /**
     * Unregister this listener from the system
     */
    void shutdown() {
        mActivity.unregisterReceiver(this);
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // isOkToProceed................................................................................
    /**
     * Android recommended practice is to double-check the broadcasted Intent matches the Intent
     * that was intended to be received
     * @param context The context this receiver is running in
     * @param intent The intent received containing the broadcast data
     * @return True if this was the correct Intent, false if not
     */
    protected boolean isOkToProceed(@NonNull Context context, @NonNull Intent intent) {
        return intent.getAction().equals(mIntentString);
    }

    // onReceive....................................................................................
    /**
     * Called by the system every time the battery broadcasts
     * @param context The context this receiver is running in
     * @param intent The intent received containing the broadcast data
     */
    @Override
    abstract public void onReceive(@NonNull Context context, @NonNull Intent intent);

}