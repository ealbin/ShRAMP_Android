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
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.analysis.OutputWrapper;


////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// Mostly complete, I think I'll have this operate the SSH interface in the future ..


/**
 * This class controls all disk actions on the ShRAMP data directory
 */
@TargetApi(21)
abstract public class StorageMedia {

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // Thread for handling output writing and storage management
    private static final String THREAD_NAME = "StorageMediaThread";

    // mHandler.....................................................................................
    // Reference to storage media thread Handler
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
                                                    GlobalSettings.STORAGE_MEDIA_THREAD_PRIORITY);

    // mBacklog.....................................................................................
    // Thread-safe count of files to be written
    private static final AtomicInteger mBacklog = new AtomicInteger();

    /**
     * Runnable for saving files on the Storage Media Thread
     */
    private static class DataSaver implements Runnable {

        // Payload
        private String mPath;
        private OutputWrapper mOutputWrapper;

        // Constructor
        private DataSaver(String path, OutputWrapper wrapper) {
            mPath = path;
            mOutputWrapper = wrapper;
        }

        // Action
        public void run() {

            if (GlobalSettings.DEBUG_DISABLE_ALL_SAVING) {
                Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> WRITING DISABLED FOR: " + mPath
                        + File.separator + mOutputWrapper.getFilename() + " <<\n ");
                mBacklog.decrementAndGet();
                return;
            }

            Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> WRITING: " + mPath
                    + File.separator + mOutputWrapper.getFilename() + " <<\n ");

            // Check for enough disk space
            File file = new File(mPath);
            long freeSpace  = file.getFreeSpace();
            long totalSpace = file.getTotalSpace();
            float usage = 1.f - (freeSpace / (float) totalSpace);

            if (usage > 0.9) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> ERROR: OUT OF MEMORY, CANNOT SAVE DATA <<\n ");
                MasterController.quitSafely();
                return;
            }

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mPath + File.separator + mOutputWrapper.getFilename());
                outputStream.getChannel().write(mOutputWrapper.getByteBuffer());
            }
            catch (FileNotFoundException e) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> ERROR: INVALID PATH, CANNOT SAVE DATA <<\n ");
                MasterController.quitSafely();
                return;
            }
            catch (IOException e) {
                // TODO: error
                Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> ERROR: IO EXCEPTION, CANNOT SAVE DATA <<\n ");
                MasterController.quitSafely();
                return;
            }
            finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    }
                    catch (IOException e) {
                        // TODO: error
                        Log.e(Thread.currentThread().getName(), " \n\n\t\t\t>> ERROR: IO EXCEPTION, CANNOT CLOSE OUTPUT STREAM <<\n ");
                        MasterController.quitSafely();
                    }
                }
            }
            mBacklog.decrementAndGet();
        }
    }

    // Path.........................................................................................
    // Handy absolute path links
    abstract private static class Path {
        static final String Home = Environment.getExternalStorageDirectory() + File.separator + "ShRAMP";
        static String Transmittable;
        static String InProgress;
        static String Calibrations;
        static String WorkingDirectory;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // setUpShrampDirectory.........................................................................
    /**
     * Check if ShRAMP data directory exists, if not initialize it
     */
    public static void setUpShrampDirectory() {
        // TODO: consider using SD-card memory in addition to onboard memory
        String Home = createDirectory(null);
        if (Home == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Unable to create home directory");
            MasterController.quitSafely();
            return;
        }

        Path.Transmittable  = createDirectory("Transmittable");
        Path.InProgress     = createDirectory("WorkInProgress");
        Path.Calibrations   = createDirectory("Calibrations");
        if (Path.Transmittable == null || Path.InProgress == null || Path.Calibrations == null) {
            // TODO: error
            Log.e(Thread.currentThread().getName(), "Unable to create directory hierarchy");
            MasterController.quitSafely();
        }
    }

    // cleanSlate...................................................................................
    /**
     * Wipes out all files and directories under ShRAMP/, but does not delete ShRAMP/
     */
    public static void cleanSlate() {
        cleanDir(null);
    }

    // createDirectory..............................................................................
    /**
     * Creates a sub-directory for depositing data (could be a hierarchy, e.g. parent/parent/dir)
     * @param name Name of the sub-directory, usually meant to be a timestamp in string form,
     *             the ShRAMP home directory is implied if not part of the name, i.e. this name is
     *             then understood as home/name.  If name is null, creates home directory.
     * @return The full path of the new directory as a string, null if unsuccessful
     */
    @Nullable
    public static String createDirectory(@Nullable String name) {
        String path;
        if (name == null) {
            path = Path.Home;
        }
        else if (!name.contains(Path.Home)) {
            path = Path.Home + File.separator + name;
        }
        else {
            path = name;
        }
        File newDirectory = new File(path);

        // Check if media is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // TODO: error
            Log.e(Thread.currentThread().getName(),"ERROR: Media unavailable");
            MasterController.quitSafely();
            return null;
        }

        // Check if data directory already exists
        if (newDirectory.exists()) {
            if (newDirectory.isDirectory()) {
                Log.e(Thread.currentThread().getName(),"WARNING: " + path + " already exists, no action taken");
                return path;
            }
            else {
                // someone saved a file with the name of this directory request
                Log.e(Thread.currentThread().getName(),"ERROR: Existing file \"" + name + "\" where this directory should be: " + path);
                return null;
            }
        }

        // By this point, we're clear to make the directory
        if (!newDirectory.mkdirs()) {
            // TODO: error
            Log.e(Thread.currentThread().getName(),"ERROR: Failed to make directory: " + path);
            MasterController.quitSafely();
            return null;
        }

        return path;
    }

    // cleanDir.....................................................................................
    /**
     * Clean a directory of all it's files and subfolders, but does not delete the directory itself.
     * @param name If null, clears everything under ShRAMP/, if not an absolute path assumes its
     *             relative to ShRAMP/.
     */
    public static void cleanDir(@Nullable String name) {
        String path;
        if (name == null) {
            path = Path.Home;
        }
        else if (!name.contains(Path.Home)) {
            path = Path.Home + File.separator + name;
        }
        else {
            path = name;
        }
        File directoryToClean = new File(path);

        if (!directoryToClean.exists()) {
            Log.e(Thread.currentThread().getName(), "Directory " + path + " does not exist, cannot clean");
            return;
        }

        for (File file : directoryToClean.listFiles()) {
            if (file.isDirectory()) {
                cleanDir(file.getAbsolutePath());
            }
            if (!file.delete()) {
                Log.e(Thread.currentThread().getName(), "Unable to delete " + file.getAbsolutePath());
            }
        }
    }

    // removeDir....................................................................................
    /**
     * Remove a directory and all of it's files and subfolders.
     * @param name If null, removes everything under ShRAMP/ including ShRAMP/ itself.  If not an
     *             absolute path, assumes its relative to ShRAMP/
     */
    public static void removeDir(@Nullable String name) {
        String path;
        if (name == null) {
            path = Path.Home;
        }
        else if (!name.contains(Path.Home)) {
            path = Path.Home + File.separator + name;
        }
        else {
            path = name;
        }
        File directoryToRemove = new File(path);

        if (!directoryToRemove.exists()) {
            Log.e(Thread.currentThread().getName(), "Directory " + path + " does not exist, cannot clean");
            return;
        }

        cleanDir(path);

        if (!directoryToRemove.delete()) {
            Log.e(Thread.currentThread().getName(), "Unable to delete " + directoryToRemove.getAbsolutePath());
        }
    }

    // removeEmptyDirs...............................................................................
    /**
     * Wipes out any empty directories under startDirectory
     * @param startDirectory empty directories under this, if null, startDirectory = ShRAMP/
     */
    public static void removeEmptyDirs(@Nullable String startDirectory) {
        String path;
        if (startDirectory == null) {
            path = Path.Home;
        }
        else if (!startDirectory.contains(Path.Home)) {
            path = Path.Home + File.separator + startDirectory;
        }
        else {
            path = startDirectory;
        }
        File directoryToClean = new File(path);

        if (!directoryToClean.exists()) {
            Log.e(Thread.currentThread().getName(), "Directory " + path + " does not exist, cannot clean");
            return;
        }

        if (directoryToClean.listFiles().length == 0) {
            removeDir(path);
        }
        else {
            for (File file : directoryToClean.listFiles()) {
                if (file.isDirectory()) {
                    removeEmptyDirs(file.getAbsolutePath());
                }
            }
        }
    }

    // newInProgress................................................................................
    /**
     * Create a new directory under ShRAMP/InProgress/ and sets WorkingDirectory to this.
     * @param name If null, makes a new directory with the current date Datestamp.
     *             If not an absolute path, assumes its relative to ShRAMP/InProgress/.
     *             If directory already exists, takes no action besides setting WorkingDirectory to this.
     */
    public static void newInProgress(@Nullable String name) {
        String path;
        if (name == null) {
            path = Path.InProgress + File.separator + Datestamp.getDate();
        }
        else if (!name.contains(Path.InProgress)) {
            path = Path.InProgress + File.separator + name;
        }
        else {
            path = name;
        }
        File newDirectory = new File(path);

        if (newDirectory.exists()) {
            Log.e(Thread.currentThread().getName(), "Directory " + name + " already exists, making it the working directory");
            Path.WorkingDirectory = path;
            return;
        }

        Path.WorkingDirectory = createDirectory(path);
    }

    // TODO: method for moving/tarballing directory or files to Transmittable
    //public static void makeTransmittable(...)

    // isBusy.......................................................................................
    /**
     * @return True if files are currently being written, false if in idle
     */
    public static boolean isBusy() {
        return mBacklog.get() > 0;
    }

    // getBacklog...................................................................................
    /**
     * @return The number of files in backlog to be / are being written
     */
    public static int getBacklog() {
        return mBacklog.get();
    }

    // writeCalibration.............................................................................
    /**
     * Writes a new calibration file to the Calibrations directory
     * @param wrapper Calibration data (e.g. mean, stddev, etc)
     */
    public static void writeCalibration(@NonNull OutputWrapper wrapper) {
        mBacklog.incrementAndGet();
        mHandler.post(new DataSaver(Path.Calibrations, wrapper));
    }

    /**
     * Writes OutputWrapper in the current working directory (if path is null), or to the specified path.
     * Path can be relative to /ShRAMP (i.e. "mydir" translates to /ShRAMP/mydir).
     * Caution: existing files with the same name will be overwritten.
     * Note: writing occurs on the storage media thread, so the calling thread will not be burdened.
     * @param wrapper OutputWrapper to be written
     * @param path (Optional) If null, writes to working directory, if specified, writes to that
     */
    public static void writeInternalStorage(@NonNull OutputWrapper wrapper, @Nullable String path) {
        mBacklog.incrementAndGet();

        String outpath;
        if (path == null) {
            outpath = Path.WorkingDirectory;
        }
        else if (!path.contains(Path.Home)) {
            outpath = Path.Home + File.separator + path;
        }
        else {
            outpath = path;
        }

        File outfile = new File(outpath + File.separator + wrapper.getFilename());
        if (outfile.exists()) {
            Log.e(Thread.currentThread().getName(), "WARNING: " + outfile.getAbsolutePath() + " already exists and will be OVERWRITTEN");
        }

        mHandler.post(new DataSaver(path, wrapper));
    }

    /**
     * @param head options include "cold_fast", "cold_slow", "hot_fast", "hot_slow",
     *             "mean", "stddev", "stderr", and "mask"
     * @param extension options include "mean", "stddev", "stderr", and "mask"
     * @return Returns the absolute path of the most recent calibration file matching the parameters,
     *         or null if one cannot be found
     */
    // TODO: (PRIORITY) double check it's sorting correctly
    @Nullable
    @Contract(pure = true)
    public static String findRecentCalibration(@NonNull String head, @NonNull String extension) {
        if (!head.equals("cold_fast") && !head.equals("cold_slow") && !head.equals("hot_fast")
                && !head.equals("hot_slow") && !head.equals("mean") && !head.equals("stddev")
                && !head.equals("stderr") && !head.equals("mask")) {
            Log.e(Thread.currentThread().getName(), "Unable to find calibration by this heading: " + head);
            return null;
        }

        if (!extension.equals(GlobalSettings.MEAN_FILE) && !extension.equals(GlobalSettings.STDDEV_FILE)
                && !extension.equals(GlobalSettings.STDERR_FILE) && !extension.equals(GlobalSettings.MASK_FILE)) {
            Log.e(Thread.currentThread().getName(), "Unable to find calibration by this extension: " + extension);
            return null;
        }

        File calibrations = new File(Path.Calibrations);

        // Filename filter
        class CalibrationFilter implements FilenameFilter {
            private String Head;
            private String Extension;

            private CalibrationFilter(@NonNull String head, @NonNull String extension) {
                Head = head;
                Extension = extension;
            }

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(Head) && name.endsWith(Extension);
            }
        }

        // Order files by datestamp
        class LatestDateFirst implements Comparator<String> {
            private int HeadLen;
            private int ExtLen;
            private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US);

            private LatestDateFirst(@NonNull String head, @NonNull String extension) {
                HeadLen = head.length() + 1;
                ExtLen  = extension.length();
            }

            @Override
            public int compare(String o1, String o2) {
                try {
                    Date date1 = format.parse(o1.substring(HeadLen, o1.length() - ExtLen));
                    Date date2 = format.parse(o2.substring(HeadLen, o2.length() - ExtLen));
                    return date1.compareTo(date2);
                }
                catch (ParseException e) {
                    // TODO: error
                    Log.e(Thread.currentThread().getName(), "Parse exception, cannot sort files");
                    return 0;
                }
            }
        }

        // Sort found files
        List<String> sortedFiles = ArrayToList.convert(calibrations.list(new CalibrationFilter(head, extension)));
        Collections.sort( sortedFiles, new LatestDateFirst(head, extension) );

        if (sortedFiles.size() == 0) {
            return null;
        }

        File foundFile = new File(Path.Calibrations + File.separator + sortedFiles.get(0));
        return foundFile.getAbsolutePath();
    }

}