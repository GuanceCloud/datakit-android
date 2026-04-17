package com.ft.sdk.sessionreplay.internal.storage;

import android.util.Log;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class FileMover {
    private static final String TAG = "FileMover";
    private final InternalLogger internalLogger;

    public FileMover(InternalLogger internalLogger) {
        this.internalLogger = internalLogger;
    }

    @WorkerThread
    public boolean delete(File target) {
        try {
            return deleteRecursively(target);
        } catch (FileNotFoundException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_DELETE, target.getPath()));
            return false;
        } catch (SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_DELETE,
                    target.getPath()) + "," + Log.getStackTraceString(e));
            return false;
        }
    }

    @WorkerThread
    @SuppressWarnings("ReturnCount")
    public boolean moveFiles(final File srcDir, final File destDir) {
        if (!existsSafe(srcDir, internalLogger)) {
            internalLogger.i(TAG, String.format(Locale.US, INFO_MOVE_NO_SRC, srcDir.getPath()));
            return true;
        }
        if (!isDirectorySafe(srcDir, internalLogger)) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_MOVE_NOT_DIR, srcDir.getPath()));
            return false;
        }
        if (!existsSafe(destDir, internalLogger)) {
            if (!mkdirsSafe(destDir, internalLogger)) {
                internalLogger.e(TAG, String.format(Locale.US, ERROR_MOVE_NO_DST, srcDir.getPath()));
                return false;
            }
        } else if (!isDirectorySafe(destDir, internalLogger)) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_MOVE_NOT_DIR, destDir.getPath()));
            return false;
        }

        File[] srcFiles = listFilesSafe(srcDir);
        if (srcFiles == null) {
            return false;
        }
        for (File file : srcFiles) {
            if (!moveFile(file, destDir)) {
                return false;
            }
        }
        return true;
    }

    private boolean moveFile(File file, File destDir) {
        File destFile = new File(destDir, file.getName());
        return renameToSafe(file, destFile);
    }

    private boolean deleteRecursively(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        boolean result = true;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                result &= deleteRecursively(child);
            }
        }
        return result && file.delete();
    }

    public static boolean existsSafe(File file, InternalLogger internalLogger) {
        try {
            return file.exists();
        } catch (SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, "Unable to check existence of file: %s",
                            file.getPath()),
                    e);
            return false;
        }
    }

    private boolean isDirectorySafe(File file, InternalLogger internalLogger) {
        try {
            return file.isDirectory();
        } catch (SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, "Unable to check if file is a directory: %s", file.getPath()), e);
            return false;
        }
    }

    private boolean mkdirsSafe(File file, InternalLogger internalLogger) {
        try {
            return file.mkdirs();
        } catch (SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, String.format(Locale.US, "Unable to create directory: %s", file.getPath()), e));
            return false;
        }
    }

    private File[] listFilesSafe(File file) {
        try {
            return file.listFiles();
        } catch (SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, "Unable to check if file is a directory: %s", file.getPath())
                    , e);

            return null;
        }
    }

    private boolean renameToSafe(File src, File dest) {
        try {
            return src.renameTo(dest);
        } catch (SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, "Unable to rename file from %s to %s",
                            src.getPath(), dest.getPath())
                    , e);
            return false;
        }
    }

    private static final String ERROR_DELETE = "Unable to delete file: %s";
    private static final String INFO_MOVE_NO_SRC = "Unable to move files; source directory does not exist: %s";
    private static final String ERROR_MOVE_NOT_DIR = "Unable to move files; file is not a directory: %s";
    private static final String ERROR_MOVE_NO_DST = "Unable to move files; could not create directory: %s";
}
