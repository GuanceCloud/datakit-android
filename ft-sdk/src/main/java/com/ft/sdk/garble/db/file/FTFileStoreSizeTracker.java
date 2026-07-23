package com.ft.sdk.garble.db.file;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Incremental byte-size tracker for file-backed cache storage.
 */
class FTFileStoreSizeTracker {
    private final File sizeFile;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicLong currentSize = new AtomicLong();

    FTFileStoreSizeTracker() {
        this(null);
    }

    FTFileStoreSizeTracker(FTFileStorePaths paths) {
        this.sizeFile = paths == null ? null : paths.getSizeFile();
    }

    boolean isInitialized() {
        return initialized.get();
    }

    long currentSize() {
        return currentSize.get();
    }

    void reset(long size) {
        setSize(Math.max(0, size));
    }

    boolean initializeFromMetadata() {
        Long persistedSize = readPersistedSize();
        if (persistedSize == null) {
            return false;
        }
        currentSize.set(Math.max(0, persistedSize));
        initialized.set(true);
        return true;
    }

    void writeUtf8(File file, String content) throws IOException {
        long before = fileSize(file);
        FTAtomicFileHelper.writeUtf8(file, content);
        add(fileSize(file) - before);
    }

    boolean deleteFile(File file) {
        long before = fileSize(file);
        boolean deleted = file == null || !file.exists() || file.delete();
        if (deleted) {
            add(-before);
        }
        return deleted;
    }

    private void add(long delta) {
        if (delta == 0) {
            return;
        }
        Long persistedSize = readPersistedSize();
        long current = persistedSize == null ? currentSize.get() : persistedSize;
        setSize(Math.max(0, current + delta));
    }

    private long fileSize(File file) {
        return file != null && file.exists() ? file.length() : 0;
    }

    private void setSize(long size) {
        long nextSize = Math.max(0, size);
        currentSize.set(nextSize);
        initialized.set(true);
        persistSize(nextSize);
    }

    private Long readPersistedSize() {
        if (sizeFile == null || !sizeFile.exists()) {
            return null;
        }
        try {
            String content = FTAtomicFileHelper.readUtf8(sizeFile).trim();
            if (content.length() == 0) {
                return null;
            }
            return Long.parseLong(content);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void persistSize(long size) {
        if (sizeFile == null) {
            return;
        }
        try {
            FTAtomicFileHelper.writeUtf8(sizeFile, String.valueOf(size));
        } catch (IOException ignored) {
            // Keep the in-memory value if the shared metadata cannot be written.
        }
    }
}
