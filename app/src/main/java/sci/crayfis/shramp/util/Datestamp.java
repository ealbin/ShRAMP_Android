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

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Produces the current date and time as a String, all times are in Pacific Standard.
 * Also gives nanoseconds elapsed from start for sensor timestamps.
 */
@TargetApi(21)
public final class Datestamp {

    // Private Static Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final Datestamp mInstance = new Datestamp();

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mFirstTimestamp..............................................................................
    // First sensor timestamp, all future timestamps are based off of this
    private Long mFirstTimestamp;

    // mStartDate...................................................................................
    // A String representation of the current date
    private String mStartDate;

    // mSystemStartNanos............................................................................
    // Nanoseconds since the last boot at the time of this object's creation
    private Long mSystemStartNanos;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Datestamp....................................................................................
    /**
     * Disabled
     */
    private Datestamp() {
        setStartDate();
    }

    // Private Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setStartDate.................................................................................
    /**
     * Sets the start date to the current time,
     * YYYY-MM-DD-HH-MM-SS-mmm (year-month-day-hour-minute-second-millisecond)
     */
    private void setStartDate() {
        mSystemStartNanos = SystemClock.elapsedRealtimeNanos();
        mFirstTimestamp   = 0L;
        mStartDate = getDate();
    }

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getDate......................................................................................
    /**
     * Gets the current date and time without resetting the start date.
     * @return YYYY-MM-DD-HH-MM-SS-mmm (year-month-day-hour-minute-second-millisecond)
     */
    @NonNull
    public static String getDate() {

        // Make sure time zone is Pacific Standard Time (no daylight savings)
        TimeZone pst = TimeZone.getTimeZone("Etc/GMT+8");

        // Redundant check
        if (pst.useDaylightTime()) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> USING DAYLIGHT SAVINGS TIME <<\n ");
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

        return    Integer.toString(year)    + "-"
                + Integer.toString(month)   + "-"
                + Integer.toString(day)     + "-"
                + Integer.toString(hour)    + "-"
                + Integer.toString(minute)  + "-"
                + Integer.toString(second)  + "-"
                + Integer.toString(millisecond);
    }

    // resetStartDate...............................................................................
    /**
     * Resets the start date to now
     */
    public static void resetStartDate() {
        mInstance.setStartDate();
    }

    // getStartDate.................................................................................
    /**
     * @return A String representation of the start date (when object was created) YYYY-MM-DD-HH-MM-SS-mmm
     */
    @NonNull
    @Contract(pure = true)
    public static String getStartDate() {
        return mInstance.mStartDate;
    }

    // logStartDate.................................................................................
    /**
     * Displays the current date
     */
    public static void logStartDate() {
        Log.e(Thread.currentThread().getName(), " \n\n\t\t\t" + mInstance.mStartDate + "\n ");
    }

    // resetElapsedNanos............................................................................
    /**
     * Sets sensor timestamp reference point and updates the current date
     * @param timestamp Sensor timestamp to base further timestamps off of
     */
    public static void resetElapsedNanos(long timestamp) {
        mInstance.setStartDate();
        mInstance.mFirstTimestamp = timestamp;
        logStartDate();
    }

    // getElapsedTimestampNanos.....................................................................
    /**
     * @param timestamp Sensor timestamp in nanoseconds
     * @return Nanoseconds from start date
     */
    public static long getElapsedTimestampNanos(long timestamp) {
        if (mInstance.mFirstTimestamp.equals(0L)) {
            resetElapsedNanos(timestamp);
            return 0L;
        }
        return timestamp - mInstance.mFirstTimestamp;
    }

    // getElapsedSystemNanos.......................................................................
    /**
     * @return System nanoseconds from start date
     */
    public static long getElapsedSystemNanos() {
        return SystemClock.elapsedRealtimeNanos() - mInstance.mSystemStartNanos;
    }

}