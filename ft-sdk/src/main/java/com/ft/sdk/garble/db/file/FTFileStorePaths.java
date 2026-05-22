package com.ft.sdk.garble.db.file;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * File-store directory layout for SDK persistence.
 */
public class FTFileStorePaths {
    static final String ROOT_DIR_NAME = "ft_data_store";
    static final String LOCK_FILE_NAME = "store.lock";
    static final String SYNC_DIR_NAME = "sync";
    static final String RUM_VIEW_DIR_NAME = "rum_view";
    static final String RUM_ACTION_DIR_NAME = "rum_action";

    private final File rootDir;
    private final File syncDir;
    private final File rumViewDir;
    private final File rumActionDir;
    private final File lockFile;

    public FTFileStorePaths(Context context) {
        this(context.getFilesDir());
    }

    public FTFileStorePaths(File filesDir) {
        rootDir = new File(filesDir, ROOT_DIR_NAME);
        syncDir = new File(rootDir, SYNC_DIR_NAME);
        rumViewDir = new File(rootDir, RUM_VIEW_DIR_NAME);
        rumActionDir = new File(rootDir, RUM_ACTION_DIR_NAME);
        lockFile = new File(rootDir, LOCK_FILE_NAME);
    }

    public void ensureReady() throws IOException {
        ensureDirectory(rootDir);
        ensureDirectory(syncDir);
        ensureDirectory(rumViewDir);
        ensureDirectory(rumActionDir);
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getSyncDir() {
        return syncDir;
    }

    public File getRumViewDir() {
        return rumViewDir;
    }

    public File getRumActionDir() {
        return rumActionDir;
    }

    public File getLockFile() {
        return lockFile;
    }

    private void ensureDirectory(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Path is not a directory: " + dir.getAbsolutePath());
            }
            return;
        }
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }
}
