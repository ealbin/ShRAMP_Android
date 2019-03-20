package sci.crayfis.shramp.camera2.util;

import android.support.annotation.NonNull;

abstract public class TimeCode {

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
