package sci.crayfis.shramp.battery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class BatteryReceiver extends BroadcastReceiver {

    // Protected Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mActivity....................................................................................
    // TODO: description
    protected Activity mActivity;

    // mIntent......................................................................................
    // TODO: description
    protected Intent mIntent;

    // mIntentString................................................................................
    // TODO: description
    protected String mIntentString;;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // BatteryChanged...............................................................................
    /**
     * TODO: description, comments and logging
     */
    public BatteryReceiver() {
    }

    // BatteryReceiver..............................................................................
    /**
     * TODO: description, comments and logging
     * @param activity bla
     */
    BatteryReceiver(@NonNull Activity activity, @NonNull String intentString) {
        mActivity       = activity;
        mIntentString   = intentString;
        mIntent         = activity.registerReceiver(this, new IntentFilter(mIntentString));
        assert mIntent != null;
        refresh();
    }

    // Package-private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getString....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    abstract String getString();

    // refresh......................................................................................
    /**
     * TODO: description, comments and logging
     */
    void refresh() {
        onReceive(mActivity, mIntent);
    }

    // shutdown.....................................................................................
    /**
     * TODO: description, comments and logging
     */
    void shutdown() {
        mActivity.unregisterReceiver(this);
    }

    // Public Overriding Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // isOkToProceed................................................................................
    /**
     * TODO: description, comments and logging
     * @param context bla
     * @param intent bla
     * @return bla
     */
    protected boolean isOkToProceed(@NonNull Context context, @NonNull Intent intent) {
        return intent.getAction().equals(mIntentString);
    }

    // onReceive....................................................................................
    /**
     * TODO: description, comments and logging
     * @param context bla
     * @param intent bla
     */
    @Override
    abstract public void onReceive(@NonNull Context context, @NonNull Intent intent);
}