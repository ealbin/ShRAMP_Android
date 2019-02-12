package sci.crayfis.shramp;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.annotation.Target;

@TargetApi(Build.VERSION_CODES.LOLLIPOP) // 21
public class CheckAPI {
    //**********************************************************************************************
    // Class Variables
    //----------------

    // Logcat strings
    private final static String     TAG = "CheckAPI";
    private final static String DIVIDER = "---------------------------------------------";

    // Reference to MaineShRAMP activity context, set by CheckAPI constructor
    private static MaineShRAMP mMaine_shramp;


    // Minimum version = 21, Android 5.0 "Lollipop" (2014)
    public static final int MINIMUM_SDK = Build.VERSION_CODES.LOLLIPOP;

    // Minimum version = 23, Android 6.0 "Marshmellow" (2015)
    //public static final int MIN_SDK = Build.VERSION_CODES.M;


    //**********************************************************************************************
    // Class Methods
    //--------------

    CheckAPI(@NonNull MaineShRAMP main_shramp, @NonNull Runnable quit_action) {
        final String LOCAL_TAG = TAG.concat("CheckAPI(MaineShRAMP)");
        Log.e(LOCAL_TAG, DIVIDER);

        mMaine_shramp = main_shramp;

        if (outdatedAndroidVersion()) {
            Log.e(LOCAL_TAG, "Notifying user Android version is too old");

            ChatterBox chatterbox = new ChatterBox(mMaine_shramp);
            String title = "Womp womp";
            String message = "Thank you for trying ShRAMP, but" + "\n"
                    + getBuildString() + "\n"
                    + "is too old for this app." + "\n\n"
                    + getBuildString(MINIMUM_SDK) + "\n" + "is the oldest supported.";
            String button = "Push for defeat";
            chatterbox.displayBasicAlert(title, message, button, quit_action);
            Log.e(LOCAL_TAG, "RETURN");
        }
        else {
            Log.e(LOCAL_TAG, "API is sufficient");
            Log.e(LOCAL_TAG, "RETURN");
        }
    }

    /**
     * Check for minimum Android version requirement.
     * @return true if too old, false if modern
     */
    private boolean outdatedAndroidVersion() {
        final String LOCAL_TAG = TAG.concat(".outdatedAndroidVersion()");
        Log.e(LOCAL_TAG, DIVIDER);

        String build_string = getBuildString();
        if (Build.VERSION.SDK_INT < MINIMUM_SDK) {
            Log.e(LOCAL_TAG,"Build version is too old: " + build_string);
            Log.e(LOCAL_TAG,"RETURN");
            return true;
        }
        Log.e(LOCAL_TAG, "Build version checks out: " + build_string);
        Log.e(LOCAL_TAG, "RETURN");
        return false;
    }

    /**
     * Returns Android build version number, codename and date initially released for this
     * device (android.os.Build.VERSION.SDK_INT).
     * @return a string of the form "vX.X API YY \"Codename\" (Month YEAR)"
     */
    public String getBuildString() {
        final String LOCAL_TAG = TAG.concat(".getBuildString()");
        Log.e(LOCAL_TAG, DIVIDER);

        String build_string = getBuildString(Build.VERSION.SDK_INT);
        Log.e(LOCAL_TAG, "Build: " + build_string);

        Log.e(LOCAL_TAG, "RETURN");
        return build_string;
    }

    /**
     * Returns Android build version number, codename and date initially released for requested
     * build code.
     * @param build_code e.g. Build.VERSION.SDK_INT
     * @return a string of the form "vX.X \"Codename\" (Month Year)"
     */
    public String getBuildString(int build_code) {
        final String LOCAL_TAG = TAG.concat(".getBuildString(int)");
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

}
