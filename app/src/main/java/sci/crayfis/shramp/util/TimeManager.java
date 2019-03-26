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

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public final class TimeManager {

    // Private Static Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final TimeManager mInstance = new TimeManager();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mFirstTimestamp..............................................................................
    // TODO: description
    private Long mFirstTimestamp;

    // mStartDate...................................................................................
    // TODO: description
    private String mStartDate;

    // mSystemStartNanos............................................................................
    // TODO: description
    private Long mSystemStartNanos;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // TimeManager..................................................................................
    /**
     * TODO: description, comments and logging
     */
    private TimeManager() {

        // Make sure time zone is Pacific Standard Time (no daylight savings)
        TimeZone pst = TimeZone.getTimeZone("Etc/GMT+8");
        if (pst.useDaylightTime()) {
            // TODO: error
        }
        TimeZone.setDefault(pst);

        // Get time at this moment
        Calendar calendar = Calendar.getInstance(pst, Locale.US);
        int year          = calendar.get(Calendar.YEAR);
        int month         = calendar.get(Calendar.MONDAY);
        int day           = calendar.get(Calendar.DAY_OF_MONTH);
        int hour          = calendar.get(Calendar.HOUR_OF_DAY);
        int minute        = calendar.get(Calendar.MINUTE);
        int second        = calendar.get(Calendar.SECOND);
        int millisecond   = calendar.get(Calendar.MILLISECOND);
        mSystemStartNanos = SystemClock.elapsedRealtimeNanos();
        mFirstTimestamp   = 0L;

        mStartDate = Integer.toString(year) + "-"
                + Integer.toString(month)   + "-"
                + Integer.toString(day)     + "-"
                + Integer.toString(hour)    + "-"
                + Integer.toString(minute)  + "-"
                + Integer.toString(second)  + "-"
                + Integer.toString(millisecond);
        Log.e(Thread.currentThread().getName(), "TimeManager mStartDate: " + mStartDate);
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getElapsedNanos..............................................................................
    /**
     * TODO: description, comments and logging
     * @param timestamp bla
     * @return bla
     */
    public static long getElapsedNanos(long timestamp) {
        if (mInstance.mFirstTimestamp.equals(0L)) {
            mInstance.mFirstTimestamp = timestamp;
            return 0L;
        }
        return timestamp - mInstance.mFirstTimestamp;
    }

    // getElapsedSystemNanos.......................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    public static long getElapsedSystemNanos() {
        return SystemClock.elapsedRealtimeNanos() - mInstance.mSystemStartNanos;
    }

    // getStartDate.................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    public static String getStartDate() {
        return mInstance.mStartDate;
    }

}