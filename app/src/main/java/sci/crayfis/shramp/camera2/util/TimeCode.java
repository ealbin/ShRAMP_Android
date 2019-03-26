package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class TimeCode {

    /**
     * TODO: description, comments and logging
     * @param timestamp bla
     * @return bla
     */
    @NonNull
    public static String toString(@NonNull Long timestamp) {
        double time = (double) timestamp;
        String out = "";
        for (int i = 0; i < 6; i++ ) {
            time /= 10.;
            long iPart = (long) time;
            char code = 'A';
            code += (char) (10 * (time - iPart));
            time = iPart;
            if (i == 3) {
                out += " ";
            }
            out += code;
        }
        return out;
    }

}