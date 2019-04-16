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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.analysis.ImageWrapper;
import sci.crayfis.shramp.analysis.OutputWrapper;
import sci.crayfis.shramp.camera2.capture.CaptureController;


////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * This class controls all disk actions on the ShRAMP data directory
 */
@TargetApi(21)
abstract public class StorageMedia {

    // .../ShRAMP
    // .../ShRAMP/Transmittable
    // .../ShRAMP/Transmittable/YYYY-MM-DD-HH-MM-SS-mmm  (year-month-day-hour-minute-second-millisecond)
    // .../ShRAMP/WorkInProgress
    // .../ShRAMP/WorkInProgress/YYYY-MM-DD-HH-MM-SS-mmm
    // .../ShRAMP/WorkInProgress/YYYY-MM-DD-HH-MM-SS-mmm/data
    // .../ShRAMP/WorkInProgress/YYYY-MM-DD-HH-MM-SS-mmm/meta
    // .../ShRAMP/WorkInProgress/YYYY-MM-DD-HH-MM-SS-mmm/meta/log.txt
    // .../ShRAMP/WorkInProgress/YYYY-MM-DD-HH-MM-SS-mmm/meta/history.txt

    // Private Class Constants
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // THREAD_NAME..................................................................................
    // TODO: description
    private static final String THREAD_NAME = "StorageMediaThread";

    // mHandler.....................................................................................
    // TODO: description
    private static final Handler mHandler = HandlerManager.newHandler(THREAD_NAME,
            GlobalSettings.STORAGE_MEDIA_THREAD_PRIORITY);


    private static final Object READ_WRITE_LOCK = new Object();

    // Data path
    private static final String mDataPath = Environment.getExternalStorageDirectory() + "/ShRAMP";

    private static final AtomicInteger mBacklog = new AtomicInteger();

    abstract private static class Path {
        static String Transmittable;
        static String InProgress;
        static String Calibrations;
        static String WorkingDirectory;
    }

    /**
     * Check if ShRAMP data directory exists, if not initialize it
     * // TODO: errors when directories not created
     */
    public static void setUpShrampDirectory() {
        // TODO: consider using SD-card memory in addition to onboard memory
        createDirectory(null);
        Path.Transmittable  = createDirectory("Transmittable");
        Path.InProgress = createDirectory("WorkInProgress");
        Path.Calibrations = createDirectory("Calibrations");
    }

    /**
     * Creates a sub-directory for depositing data (could be a hierarchy, e.g. parent/parent/dir)
     * @param name Name of the sub-directory, usually meant to be a timestamp in string form
     * @return The full path of the new directory as a string, null if unsuccessful
     */
    @Nullable
    public static String createDirectory(@Nullable String name) {

        String path;
        if (name == null) {
            path = mDataPath;
        }
        else {
            path = mDataPath + "/" + name;
        }
        File newDirectory = new File(path);

        // Check if media is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // TODO: can't collect data, this is an error
            Log.e(Thread.currentThread().getName(),"ERROR: Media unavailable");
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
            // TODO: for some reason it didn't make the directory(ies)
            Log.e(Thread.currentThread().getName(),"ERROR: Failed to make directory: " + path);
            return null;
        }

        return path;
    }

    /**
     * Wipes out all files and directories under ShRAMP/
     */
    public static void cleanAll() {
        cleanDirectory(null);
    }

    /**
     * TODO: description, comments and logging
     * @param name bla
     */
    public static void cleanDirectory(@Nullable String name) {
        String path;
        if (name == null) {
            path = mDataPath;
        }
        else {
            path = mDataPath + "/" + name;
        }
        File directoryToClean = new File(path);

        if (!directoryToClean.exists()) {
            return;
        }

        //try {
            // TODO: doesn't exist in S6 or hwaiwai
        //    FileUtils.cleanDirectory(directoryToClean);
        //}
        //catch (IOException e) {
            // TODO: failed to delete
        //    Log.e(Thread.currentThread().getName(),"ERROR: IO Exception");
        //}
    }

    // TODO: error if cannot create
    public static void newWorkInProgress() {
        TimeManager.resetStartDate();
        Path.WorkingDirectory = createDirectory(Path.InProgress + "/" + TimeManager.getStartDate());
    }

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