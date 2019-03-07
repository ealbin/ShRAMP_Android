package sci.crayfis.shramp.util;

import android.os.SystemClock;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeManager {

    //**********************************************************************************************
    // Class Variables
    //----------------

    private static final TimeManager mInstance = new TimeManager();

    //..............................................................................................

    private String mStartDate;
    private long   mSystemStartNanos;
    private long   mFirstTimestamp;


    //**********************************************************************************************
    // Class Methods
    //--------------

    public static TimeManager getInstance() { return mInstance; }

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
        mFirstTimestamp   = 0;

        mStartDate = Integer.toString(year) + "-"
                + Integer.toString(month)   + "-"
                + Integer.toString(day)     + "-"
                + Integer.toString(hour)    + "-"
                + Integer.toString(minute)  + "-"
                + Integer.toString(second)  + "-"
                + Integer.toString(millisecond);
    }

    //----------------------------------------------------------------------------------------------

    public String getStartDate() {
        return mStartDate;
    }

    public long getElapsedNanos(long timestamp) {
        if (mFirstTimestamp == 0) {
            mFirstTimestamp = timestamp;
            return 0;
        }

        return timestamp - mFirstTimestamp;
    }

    public long getElapsedSystemNanos() {
        return SystemClock.elapsedRealtimeNanos() - mSystemStartNanos;
    }
}
