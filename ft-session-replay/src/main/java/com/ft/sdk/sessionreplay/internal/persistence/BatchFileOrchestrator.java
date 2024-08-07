package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.storage.FilePersistenceConfig;
import com.ft.sdk.sessionreplay.internal.storage.MetricsDispatcher;
import com.ft.sdk.sessionreplay.internal.storage.RemovalReason;
import com.ft.sdk.sessionreplay.internal.utils.FileUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchFileOrchestrator implements FileOrchestrator {
    private static final String TAG = "BatchFileOrchestrator";

    private final File rootDir;
    private final FilePersistenceConfig config;
    private final InternalLogger internalLogger;
    private final MetricsDispatcher metricsDispatcher;

    private final FileFilter fileFilter = new BatchFileFilter();

    private final long recentReadDelayMs;
    private final long recentWriteDelayMs;

    private File previousFile = null;
    private long previousFileItemCount = 0;
    private long lastFileAccessTimestamp = 0L;
    private long lastCleanupTimestamp = 0L;

    private final Lock rootDirLock = new ReentrantLock();

    public BatchFileOrchestrator(File rootDir, FilePersistenceConfig config, InternalLogger internalLogger, MetricsDispatcher metricsDispatcher) {
        this.rootDir = rootDir;
        this.config = config;
        this.internalLogger = internalLogger;
        this.metricsDispatcher = metricsDispatcher;
        this.recentReadDelayMs = Math.round(config.getRecentDelayMs() * INCREASE_PERCENT);
        this.recentWriteDelayMs = Math.round(config.getRecentDelayMs() * DECREASE_PERCENT);
    }

    @WorkerThread
    @Override
    public File getWritableFile(boolean forceNewFile) {
        if (!isRootDirValid()) {
            return null;
        }

        if (canDoCleanup()) {
            List<File> files = listBatchFiles();
            files = deleteObsoleteFiles(files);
            freeSpaceIfNeeded(files);
            lastCleanupTimestamp = System.currentTimeMillis();
        }

        if (!forceNewFile) {
            File reusableFile = getReusableWritableFile();
            if (reusableFile != null) {
                return reusableFile;
            } else {
                return createNewFile(false);
            }
        }
        return createNewFile(true);
    }

    @WorkerThread
    @Override
    public File getReadableFile(Set<File> excludeFiles) {
        if (!isRootDirValid()) {
            return null;
        }

        List<File> files = listSortedBatchFiles();
        files = deleteObsoleteFiles(files);
        lastCleanupTimestamp = System.currentTimeMillis();

        for (File file : files) {
            if (!excludeFiles.contains(file) && !isFileRecent(file, recentReadDelayMs)) {
                return file;
            }
        }
        return null;
    }

    @WorkerThread
    @Override
    public List<File> getAllFiles() {
        if (!isRootDirValid()) {
            return Collections.emptyList();
        }
        return listSortedBatchFiles();
    }

    @WorkerThread
    @Override
    public List<File> getFlushableFiles() {
        return getAllFiles();
    }

    @WorkerThread
    @Override
    public File getRootDir() {
        if (!isRootDirValid()) {
            return null;
        }
        return rootDir;
    }

    @WorkerThread
    @Override
    public File getMetadataFile(File file) {
        if (!Objects.equals(file.getParent(), rootDir.getPath())) {
            internalLogger.d(TAG, String.format(Locale.US, DEBUG_DIFFERENT_ROOT, file.getPath(), rootDir.getPath()));
        }

        if (isBatchFile(file)) {
            return new File(file.getPath() + "_metadata");
        } else {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_NOT_BATCH_FILE, file.getPath()));
            return null;
        }
    }

    private boolean isRootDirValid() {
        if (FileUtils.existsSafe(rootDir, internalLogger)) {
            if (rootDir.isDirectory()) {
                if (FileUtils.canWriteSafe(rootDir, internalLogger)) {
                    return true;
                } else {
                    internalLogger.e(TAG, String.format(Locale.US, ERROR_ROOT_NOT_WRITABLE, rootDir.getPath()));
                    return false;
                }
            } else {
                internalLogger.e(TAG, String.format(Locale.US, ERROR_ROOT_NOT_DIR, rootDir.getPath()));
                return false;
            }
        } else {
            rootDirLock.lock();
            try {
                if (FileUtils.existsSafe(rootDir, internalLogger)) {
                    return true;
                }

                if (FileUtils.mkdirsSafe(rootDir, internalLogger)) {
                    return true;
                } else {
                    internalLogger.e(TAG, String.format(Locale.US, ERROR_CANT_CREATE_ROOT, rootDir.getPath()));
                    return false;
                }
            } finally {
                rootDirLock.unlock();
            }
        }
    }

    private File createNewFile(boolean wasForced) {
        String newFileName = String.valueOf(System.currentTimeMillis());
        File newFile = new File(rootDir, newFileName);
        File closedFile = previousFile;
        long closedFileLastAccessTimestamp = lastFileAccessTimestamp;
        if (closedFile != null) {
            metricsDispatcher.sendBatchClosedMetric(
                    closedFile,
                    new BatchClosedMetadata(
                            closedFileLastAccessTimestamp,
                            wasForced,
                            previousFileItemCount
                    )
            );
        }
        previousFile = newFile;
        previousFileItemCount = 1;
        lastFileAccessTimestamp = System.currentTimeMillis();
        return newFile;
    }

    private File getReusableWritableFile() {
        List<File> files = listBatchFiles();
        File lastFile = getLastBatchFile(files);
        if (lastFile == null) {
            return null;
        }

        File lastKnownFile = previousFile;
        long lastKnownFileItemCount = previousFileItemCount;
        if (!lastKnownFile.getPath().equals(lastFile.getPath())) {
            return null;
        }

        boolean isRecentEnough = isFileRecent(lastFile, recentWriteDelayMs);
        boolean hasRoomForMore = FileUtils.lengthSafe(lastFile, internalLogger) < config.getMaxBatchSize();
        boolean hasSlotForMore = lastKnownFileItemCount < config.getMaxItemsPerBatch();

        if (isRecentEnough && hasRoomForMore && hasSlotForMore) {
            previousFileItemCount = lastKnownFileItemCount + 1;
            lastFileAccessTimestamp = System.currentTimeMillis();
            return lastFile;
        } else {
            return null;
        }
    }

    private boolean isFileRecent(File file, long delayMs) {
        long now = System.currentTimeMillis();
        long fileTimestamp = Long.parseLong(file.getName());
        return fileTimestamp >= (now - delayMs);
    }

    private List<File> deleteObsoleteFiles(List<File> files) {
        long threshold = System.currentTimeMillis() - config.getOldFileThreshold();
        List<File> validFiles = new ArrayList<>();
        for (File file : files) {
            long fileTimestamp = Long.parseLong(file.getName());
            if (fileTimestamp < threshold) {
                if (FileUtils.deleteSafe(file, internalLogger)) {
                    metricsDispatcher.sendBatchDeletedMetric(file, new RemovalReason.Obsolete());
                }
                File metadataFile = getMetadataFile(file);
                if (FileUtils.existsSafe(metadataFile, internalLogger)) {
                    FileUtils.deleteSafe(metadataFile, internalLogger);
                }
            } else {
                validFiles.add(file);
            }
        }
        return validFiles;
    }

    private void freeSpaceIfNeeded(List<File> files) {
        // 计算磁盘上所有文件的总大小
        long sizeOnDisk = 0;
        for (File file : files) {
            sizeOnDisk += FileUtils.lengthSafe(file, internalLogger);
        }

        long maxDiskSpace = config.getMaxDiskSpace();
        long sizeToFree = sizeOnDisk - maxDiskSpace;

        if (sizeToFree > 0) {
            long finalSizeToFree = sizeToFree;
            internalLogger.e(TAG, String.format(Locale.US, ERROR_DISK_FULL, sizeOnDisk, maxDiskSpace, finalSizeToFree));

            // 按文件名排序
            Collections.sort(files);

            for (File file : files) {
                if (sizeToFree <= 0) {
                    break;
                }

                long deletedFileSize = deleteFile(file, true);
                long deletedMetaFileSize = deleteFile(getMetadataFile(file), false);

                sizeToFree -= deletedFileSize + deletedMetaFileSize;
            }
        }
    }

    private long deleteFile(File file, boolean sendMetric) {
        if (!FileUtils.existsSafe(file, internalLogger)) {
            return 0;
        }

        long size = FileUtils.lengthSafe(file, internalLogger);
        boolean wasDeleted = FileUtils.deleteSafe(file, internalLogger);
        if (wasDeleted && sendMetric) {
            metricsDispatcher.sendBatchDeletedMetric(file, new RemovalReason.Purged());
        }
        return size;
    }

    private List<File> listBatchFiles() {
        File[] files = FileUtils.listFilesSafe(rootDir, fileFilter, internalLogger);
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, files);
        return fileList;
    }

    private List<File> listSortedBatchFiles() {
        List<File> files = listBatchFiles();
        Collections.sort(files);
        return files;
    }

    private boolean canDoCleanup() {
        return System.currentTimeMillis() - lastCleanupTimestamp > config.getCleanupFrequencyThreshold();
    }

    private File getLastBatchFile(List<File> files) {
        if (files == null || files.isEmpty()) {
            return null;
        }

        File lastBatchFile = null;
        for (File file : files) {
            if (lastBatchFile == null || file.compareTo(lastBatchFile) > 0) {
                lastBatchFile = file;
            }
        }
        return lastBatchFile;
    }

    private boolean isBatchFile(File file) {
        try {
            Long.parseLong(file.getName());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private class BatchFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return isBatchFile(file);
        }
    }

    private static final double DECREASE_PERCENT = 0.95;
    private static final double INCREASE_PERCENT = 1.05;

    private static final String ERROR_ROOT_NOT_WRITABLE = "The provided root dir is not writable: %s";
    private static final String ERROR_ROOT_NOT_DIR = "The provided root file is not a directory: %s";
    private static final String ERROR_CANT_CREATE_ROOT = "The provided root dir can't be created: %s";
    private static final String ERROR_DISK_FULL = "Too much disk space used (%d/%d): cleaning up to free %d bytes…";
    private static final String ERROR_NOT_BATCH_FILE = "The file provided is not a batch file: %s";
    private static final String DEBUG_DIFFERENT_ROOT = "The file provided (%s) doesn't belong to the current folder (%s)";
}
