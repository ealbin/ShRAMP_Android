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
 * @updated: 24 April 2019
 */

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Translates Build.VERSION.SDK_INT into a string describing the Android APK version
 */
@TargetApi(21)
abstract public class BuildString {

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // get..........................................................................................
    /**
     * Get a nice build string of the form: vX.X API XX Name (Date)
     * @return string
     */
    @NonNull
    public static String get() {
        int buildCode = Build.VERSION.SDK_INT;
        String api = Integer.toString(buildCode);
        String buildString;

        switch (buildCode) {
            case Build.VERSION_CODES.BASE: {
                buildString = "v1.0 API " + api + " \"Base\" (October 2008)";
                break;
            }

            case Build.VERSION_CODES.BASE_1_1: {
                buildString = "v1.1 API " + api + " \"Base 1.1\" (February 2009)";
                break;
            }

            case Build.VERSION_CODES.CUPCAKE: {
                buildString = "v1.5 API " + api + " \"Cupcake\" (May 2009)";
                break;
            }

            case Build.VERSION_CODES.DONUT: {
                buildString = "v1.6 API " + api + " \"Donut\" (September 2009)";
                break;
            }

            case Build.VERSION_CODES.ECLAIR: {
                buildString = "v2.0 API " + api + " \"Eclair\" (November 2009)";
                break;
            }

            case Build.VERSION_CODES.ECLAIR_0_1: {
                buildString = "v2.0.1 API " + api + " \"Eclair 0.1\" (December 2009)";
                break;
            }

            case Build.VERSION_CODES.ECLAIR_MR1: {
                buildString = "v2.1 API " + api + " \"Eclair MR1\" (January 2010)";
                break;
            }

            case Build.VERSION_CODES.FROYO: {
                buildString = "v2.2 API " + api + " \"Froyo\" (June 2010)";
                break;
            }

            case Build.VERSION_CODES.GINGERBREAD: {
                buildString = "v2.3 API " + api + " \"Gingerbread\" (November 2010)";
                break;
            }

            case Build.VERSION_CODES.GINGERBREAD_MR1: {
                buildString = "v2.3.3 API " + api + " \"Gingerbread MR1\" (February 2011)";
                break;
            }

            case Build.VERSION_CODES.HONEYCOMB: {
                buildString = "v3.0 API " + api + " \"Honeycomb\" (February 2011)";
                break;
            }

            case Build.VERSION_CODES.HONEYCOMB_MR1: {
                buildString = "v3.1 API " + api + " \"Honeycomb MR1\" (May 2011)";
                break;
            }

            case Build.VERSION_CODES.HONEYCOMB_MR2: {
                buildString = "v3.2 API " + api + " \"Honeycomb MR2\" (June 2011)";
                break;
            }

            case Build.VERSION_CODES.ICE_CREAM_SANDWICH: {
                buildString = "v4.0 API " + api + " \"Ice Cream Sandwich\" (October 2011)";
                break;
            }

            case Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1: {
                buildString = "v4.0.3 API " + api + " \"Ice Cream Sandwich MR1\" (December 2011)";
                break;
            }

            case Build.VERSION_CODES.JELLY_BEAN: {
                buildString = "v4.1 API " + api + " \"Jelly Bean\" (June 2012)";
                break;
            }

            case Build.VERSION_CODES.JELLY_BEAN_MR1: {
                buildString = "v4.2 API " + api + " \"Jelly Bean MR1\" (November 2012)";
                break;
            }

            case Build.VERSION_CODES.JELLY_BEAN_MR2: {
                buildString = "v4.3 API " + api + " \"Jelly Bean MR2\" (July 2013)";
                break;
            }

            case Build.VERSION_CODES.KITKAT: {
                buildString = "v4.4 API " + api + " \"KitKat\" (October 2013)";
                break;
            }

            case Build.VERSION_CODES.KITKAT_WATCH: {
                buildString = "v4.4W API " + api + " \"KitKat\" (June 2014)";
                break;
            }

            case Build.VERSION_CODES.LOLLIPOP: {
                buildString = "v5.0 API " + api + " \"Lollipop\" (November 2014)";
                break;
            }

            case Build.VERSION_CODES.LOLLIPOP_MR1: {
                buildString = "v5.1 API " + api + " \"Lollipop MR1\" (March 2015)";
                break;
            }

            case Build.VERSION_CODES.M: {
                buildString = "v6.0 API " + api + " \"Marshmellow\" (October 2015)";
                break;
            }

            case Build.VERSION_CODES.N: {
                buildString = "v7.0 API " + api + " \"Nougat\" (August 2016)";
                break;
            }

            case Build.VERSION_CODES.N_MR1: {
                buildString = "v7.1 API " + api + " \"Nougat MR1\" (October 2016)";
                break;
            }

            case Build.VERSION_CODES.O: {
                buildString = "v8.0 API " + api + " \"Oreo\" (August 2017)";
                break;
            }

            case Build.VERSION_CODES.O_MR1: {
                buildString = "v8.1 API " + api + " \"Oreo MR1\" (December 2017)";
                break;
            }

            case Build.VERSION_CODES.P: {
                buildString = "v9.0 API " + api + " \"Pie\" (August 2018)";
                break;
            }

            default: {
                if (buildCode > Build.VERSION_CODES.P) {
                    buildString = "version is post v9.0: API " + api;
                }
                else {
                    buildString = "unknown version code: API " + api;
                }
                break;
            }
        }

        return buildString;
    }

}