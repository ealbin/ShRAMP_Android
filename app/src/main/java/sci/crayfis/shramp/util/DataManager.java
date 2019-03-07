package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.Environment;
import android.os.SystemClock;
import android.renderscript.RenderScript;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.TreeMap;

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

    // Data storage
    private static TreeMap<Long, DataManager.DataSaver> mHasData     = new TreeMap<>();
    private static TreeMap<Long, DataManager.DataSaver> mHasFilename = new TreeMap<>();

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
    public static void clean() {
        try {
            FileUtils.cleanDirectory(new File(mDataPath));
        }
        catch (IOException e) {
            // TODO: failed to delete
            DataManager.mLogger.log("ERROR: IO Exception");
        }
    }

    public static synchronized void saveData(long timestamp, String filename) {
        if (DataManager.mHasData.containsKey(timestamp)) {
            DataManager.mHasData.remove(timestamp).update(filename).start();
        }
        else {
            DataManager.mHasFilename.put(timestamp, new DataManager.DataSaver(timestamp, filename));
        }
    }

    public static synchronized void saveData(long timestamp, byte[] data) {
        if (DataManager.mHasFilename.containsKey(timestamp)) {
            DataManager.mHasFilename.remove(timestamp).update(data).start();
        }
        else {
            DataManager.mHasData.put(timestamp, new DataManager.DataSaver(timestamp, data));
        }
    }

    public static void flush() {
        for (long key : DataManager.mHasData.keySet()) {
            if (DataManager.mHasFilename.containsKey(key)) {
                String filename = DataManager.mHasFilename.remove(key).getFilename();
                DataManager.mHasData.remove(key).update(filename).start();
            }
            else {
                DataManager.mLogger.log("Data found without a filename!");
                // purge it
                DataManager.mHasData.remove(key);
            }
        }

        // mHasData has been purged
        for (long key : DataManager.mHasFilename.keySet()) {
            DataManager.mLogger.log("Filename found without data!");
            // purge it
            DataManager.mHasFilename.remove(key);
        }

        // We should be starting over, but just to be sure...
        DataManager.mHasData     = new TreeMap<>();
        DataManager.mHasFilename = new TreeMap<>();
    }

    static class DataSaver extends Thread {
        private static final Object WRITE_LOCK = new Object();

        private long   mTimestamp;
        private String mFilename = null;
        private byte[] mData     = null;

        private DataSaver() {}

        DataSaver(long timestamp, String filename) {
            super("Data timestamp: " + Long.toString(timestamp));
            mTimestamp = timestamp;
            mFilename  = filename;
            super.setPriority(Thread.MAX_PRIORITY);
        }

        DataSaver(long timestamp, byte[] data) {
            super("Data timestamp: " + Long.toString(timestamp));
            mTimestamp = timestamp;
            mData      = data;
            super.setPriority(Thread.MAX_PRIORITY);
        }

        DataSaver update(String filename) {
            assert mFilename == null;
            mFilename = filename;
            return this;
        }

        DataSaver update(byte[] data) {
            assert mData == null;
            mData = data;
            return this;
        }

        String getFilename() {return mFilename;}
        byte[] getData() {return mData;}

        // save data
        public void run() {
            synchronized (WRITE_LOCK) {
                long elapsedTime = SystemClock.elapsedRealtimeNanos();
                assert mFilename != null && mData != null;
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(mFilename);
                    outputStream.write(mData);
                    long time = SystemClock.elapsedRealtimeNanos();
                    DecimalFormat df = new DecimalFormat("#.##");
                    String fps = df.format(1e9 / (double) (time - elapsedTime));
                    Log.e(Thread.currentThread().getName(), "wrote file: " + mFilename + ", priority: " + Integer.toString(this.getPriority()) + ", fps: " + fps);
                } catch (FileNotFoundException e) {
                    // TODO: big uh oh
                    DataManager.mLogger.log("ERROR: File Not Found Exception");
                } catch (IOException e) {
                    // TODO: big uh oh
                    DataManager.mLogger.log("ERROR: IO Exception");
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            // TODO: regular uh oh
                            DataManager.mLogger.log("ERROR: IO Exception");
                        }
                    }
                }
            }
        }
    }
}
