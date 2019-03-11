package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public class TimeManager {

    //**********************************************************************************************
    // Static Class Fields
    //---------------------

    // Private Object Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mInstance....................................................................................
    // TODO: description
    private static final TimeManager mInstance = new TimeManager();

    //**********************************************************************************************
    // Class Fields
    //-------------

    // Private
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

    //**********************************************************************************************
    // Constructors
    //-------------

    // Private
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
    }

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getInstance..................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    public static TimeManager getInstance() { return mInstance; }

    //**********************************************************************************************
    // Class Methods
    //--------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // getElapsedNanos..............................................................................
    /**
     * TODO: description, comments and logging
     * @param timestamp
     * @return
     */
    public long getElapsedNanos(long timestamp) {
        if (mFirstTimestamp.equals(0L)) {
            mFirstTimestamp = timestamp;
            return 0L;
        }
        return timestamp - mFirstTimestamp;
    }

    // getElapsedSystemNanos.......................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    public long getElapsedSystemNanos() {
        return SystemClock.elapsedRealtimeNanos() - mSystemStartNanos;
    }

    // getStartDate.................................................................................
    /**
     * TODO: description, comments and logging
     * @return
     */
    @NonNull
    public String getStartDate() {
        return mStartDate;
    }

}