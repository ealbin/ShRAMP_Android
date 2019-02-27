package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.Environment;
import android.provider.ContactsContract;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import sci.crayfis.shramp.logging.ShrampLogger;

/**
 * This class controls actions on the ShRAMP data directory
 */
@TargetApi(21)
public class DataManager {

    //**********************************************************************************************
    // Class Variables
    //----------------

    // Data path
    private static final String mDataPath = Environment.getExternalStorageDirectory() + "/ShRAMP";

    // logging
    private static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    //**********************************************************************************************
    // Class Methods
    //--------------

    /**
     * Check if ShRAMP data directory exists, if not create it
     */
    public static void setUpShrampDirectory() {
        // TODO: for now uses onboard memory, consider using SD-card memory

        // Check if media is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // TODO: can't collect data, this is an error
            DataManager.mLogger.log("ERROR: Media unavailable");
        }

        // Check if data directory already exists, create it if it doesn't
        File dataDirectory = new File(mDataPath);
        if (dataDirectory.exists()) {
            if (!dataDirectory.isDirectory()) {
                // TODO: uh oh, someone saved a file with the name of ShRAMP
                DataManager.mLogger.log("ERROR: Existing file named ShRAMP where a directory should be");
            }
            // Otherwise it's set up already, return
        }
        else {
            // Create it
            if (!dataDirectory.mkdir()) {
                // TODO: for some reason it didn't make the directory
                DataManager.mLogger.log("ERROR: Failed to make the data directory");
            }
        }
    }

    /**
     * Creates a sub-directory for depositing data (could be a hierarchy, e.g. parent/parent/dir)
     * @param name Name of the sub-directory, usually meant to be a timestamp in string form
     * @return The full path of the new directory as a string, null if unsuccessful
     */
    public static String createDataDirectory(String name) {
        String path = mDataPath + "/" + name;
        File newDirectory = new File(path);

        if (newDirectory.exists()) {
            // TODO: uh oh directory already exists
            DataManager.mLogger.log("ERROR: " + path + " already exists");
            return null;
        }

        // Create it
        if (!newDirectory.mkdirs()) {
            // TODO: for some reason it didn't make the directory(ies)
            DataManager.mLogger.log("ERROR: Failed to make " + path);
            return null;
        }

        return path;
    }


    /**
     * Wipes out all files and directories under ShRAMP/
     */
    public static void purgeData() {
        try {
            FileUtils.cleanDirectory(new File(mDataPath));
        }
        catch (IOException e) {
            // TODO: failed to delete
            DataManager.mLogger.log("ERROR: IO Exception");
        }
    }

}
