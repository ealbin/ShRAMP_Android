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

package sci.crayfis.shramp.camera2.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import sci.crayfis.shramp.GlobalSettings;

/**
 * For human readability, convert a timestamp in nanoseconds into a short string of characters
 * e.g. 123,456,789 [ns] -> (1 and 2 are dropped) "D EFG HIJ"
 */
@TargetApi(21)
abstract public class TimeCode {

    /**
     * Convert timestamp in nanoseconds into a 7-character time code
     * @param timestamp timestamp to convert (nanoseconds)
     * @return 7-character time code
     */
    @NonNull
    @Contract(pure = true)
    public static String toString(@NonNull Long timestamp) {
        double time = (double) timestamp;
        String out = "";

        if (!GlobalSettings.ENABLE_VULGARITY) {
            for (int i = 0; i < 7; i++) {
                time /= 10.;
                long iPart = (long) time;
                char code = 'A';
                code += (char) (10 * (time - iPart));
                time = iPart;
                out += code;
            }
        }
        else {
            char[][] code = { {'K', 'U', 'E', 'S', 'D', 'N', 'S', 'T', 'F', 'S'},
                              {'C', 'O', 'L', 'S', 'R', 'M', 'A', 'S', 'I', 'F'},
                              {'I', 'Y', 'O', 'A', 'U', 'E', 'W', 'H', 'A', 'Y'},
                              {'L', 'K', 'H', 'T', 'F', 'N', 'D', 'H', 'S', 'B'},
                              {'S', 'C', 'I', 'U', 'M', 'D', 'T', 'P', 'R', 'C'},
                              {'S', 'U', 'H', 'O', 'A', 'I', 'E', 'U', 'S', 'O'},
                              {'A', 'F', 'S', 'Y', 'B', 'D', 'G', 'C', 'J', 'D'}
                            };
            for (int i = 0; i < 7; i++) {
                time /= 10.;
                long iPart = (long) time;
                int j = (int) (10 * (time - iPart));
                time = iPart;
                out += code[i][j];
            }
        }

        String temp = out;
        out = "";
        for (int i = 6; i >= 0; i--) {
            out += temp.charAt(i);
            if (i == 3) {
                out += " ";
            }
        }

        return out;
    }

}