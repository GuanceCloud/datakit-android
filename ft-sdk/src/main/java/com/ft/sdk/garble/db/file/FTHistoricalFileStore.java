package com.ft.sdk.garble.db.file;

import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.File;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class FTHistoricalFileStore {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTHistoricalFileStore";

    private final FTFileStorePaths paths;
    private final FTFileLock lock;

    public FTHistoricalFileStore(FTFileStorePaths paths) {
        this.paths = paths;
        this.lock = new FTFileLock(paths.getLockFile());
    }

    public void deleteAll() {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    paths.ensureReady();
                    deleteFiles(paths.getHistoricalExitDir());
                    deleteFiles(paths.getHistoricalViewDir());
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "deleteAll failed: " + e.getMessage());
        }
    }

    private static void deleteFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }
}
