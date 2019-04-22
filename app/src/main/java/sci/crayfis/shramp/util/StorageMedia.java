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
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.MasterController;
import sci.crayfis.shramp.analysis.OutputWrapper;


////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////


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


    private static final Object READ_WRITE_LOCK = new Object();

    private static final AtomicInteger mBacklog = new AtomicInteger();

    // Path.........................................................................................
    // Handy absolute path links
    abstract private static class Path {
        static final String Home = Environment.getExternalStorageDirectory() + File.pathSeparator + "ShRAMP";
        static String Transmittable;
        static String InProgress;
        static String Calibrations;
        static String WorkingDirectory;
    }

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
            path = Path.Home + File.pathSeparator + name;
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
            path = Path.Home + File.pathSeparator + name;
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
            path = Path.Home + File.pathSeparator + name;
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
            path = Path.Home + File.pathSeparator + startDirectory;
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
            path = Path.InProgress + File.pathSeparator + Datestamp.getDate();
        }
        else if (!name.contains(Path.InProgress)) {
            path = Path.InProgress + File.pathSeparator + name;
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

    // newCalibration...............................................................................
    /**
     * Create a new directory under ShRAMP/Calibrations/ and sets WorkingDirectory to this.
     * @param name If null, makes a new directory with the current date Datestamp.
     *             If not an absolute path, assumes its relative to ShRAMP/Calibrations/.
     *             If directory already exists, takes no action besides setting WorkingDirectory to this.
     */
    public static void newCalibration(@Nullable String name) {
        String path;
        if (name == null) {
            path = Path.Calibrations + File.pathSeparator + Datestamp.getDate();
        }
        else if (!name.contains(Path.Calibrations)) {
            path = Path.Calibrations + File.pathSeparator + name;
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








    public static boolean isBusy() {
        return mBacklog.get() > 0;
    }

    public static int getBacklog() {
        return mBacklog.get();
    }

    public static void writeWorkingDirectory(@NonNull OutputWrapper wrapper, @Nullable String subpath) {
        mBacklog.incrementAndGet();
        String path = Path.InProgress;//Path.WorkingDirectory;
        if (subpath != null) {
            path = createDirectory(Path.WorkingDirectory + "/" + subpath);
        }
        mHandler.post(new DataSaver(path, wrapper));
    }

    public static void writeCalibration(@NonNull OutputWrapper wrapper) {
        mBacklog.incrementAndGet();
        mHandler.post(new DataSaver(Path.Calibrations, wrapper));
    }

    /**
     * TODO: description, comments and logging
     */
    private static class DataSaver extends Thread {

        private String mPath;
        private OutputWrapper mOutputWrapper;

        private DataSaver(String path, OutputWrapper wrapper) {
            mPath = path;
            mOutputWrapper = wrapper;
        }

        public void run() {
            synchronized (READ_WRITE_LOCK) {

                Log.e(Thread.currentThread().getName(), "___WRITING: " + mPath + "/" + mOutputWrapper.getFilename());

                File file = new File(mPath);
                long freeSpace  = file.getFreeSpace();
                long totalSpace = file.getTotalSpace();
                float usage = 1.f - (freeSpace / (float) totalSpace);

                if (usage > 0.9) {
                    Log.e(Thread.currentThread().getName(), "ERROR: OUT OF MEMORY, CANNOT SAVE DATA");
                    // TODO: shutdown
                    mBacklog.decrementAndGet();
                    return;
                }

                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(mPath + "/" + mOutputWrapper.getFilename());
                    outputStream.write(mOutputWrapper.getByteBuffer().array());
                }
                catch (FileNotFoundException e) {
                    // TODO: error
                }
                catch (IOException e) {
                    // TODO: error
                }
                finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        }
                        catch (IOException e) {
                            // TODO: error
                        }
                    }
                }
                mBacklog.decrementAndGet();
            }
        }
    }

}