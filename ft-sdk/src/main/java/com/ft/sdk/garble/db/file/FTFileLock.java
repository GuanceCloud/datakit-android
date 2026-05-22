package com.ft.sdk.garble.db.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cross-process file lock with an additional in-process lock for same-JVM callers.
 */
public class FTFileLock {
    private static final ConcurrentHashMap<String, ReentrantLock> PROCESS_LOCKS =
            new ConcurrentHashMap<>();

    private final File lockFile;

    public FTFileLock(File lockFile) {
        this.lockFile = lockFile;
    }

    public <T> T withLock(LockedOperation<T> operation) throws Exception {
        ReentrantLock processLock = getProcessLock();
        processLock.lock();
        try {
            ensureLockFileParent();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile, "rw");
                 FileChannel channel = randomAccessFile.getChannel()) {
                FileLock fileLock = channel.lock();
                try {
                    return operation.run();
                } finally {
                    fileLock.release();
                }
            }
        } finally {
            processLock.unlock();
        }
    }

    private ReentrantLock getProcessLock() {
        String key;
        try {
            key = lockFile.getCanonicalPath();
        } catch (IOException e) {
            key = lockFile.getAbsolutePath();
        }

        ReentrantLock lock = PROCESS_LOCKS.get(key);
        if (lock != null) {
            return lock;
        }

        ReentrantLock newLock = new ReentrantLock();
        ReentrantLock existingLock = PROCESS_LOCKS.putIfAbsent(key, newLock);
        return existingLock == null ? newLock : existingLock;
    }

    private void ensureLockFileParent() throws IOException {
        File parent = lockFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Failed to create lock directory: " + parent.getAbsolutePath());
        }
    }

    public interface LockedOperation<T> {
        T run() throws Exception;
    }
}
