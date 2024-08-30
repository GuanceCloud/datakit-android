package com.ft.sdk.garble.manager;

import android.content.Context;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * SDK 日志管理类
 */
public class LogFileHelper {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "LogFileHelper";
    /**
     * 日志分割大小限制
     */
    private final static int SPLIT_FILE_SIZE = 33554432; //32MB

    /**
     * 日志总大小限制
     */
    private final static int CACHE_MAX_TOTAL_SIZE = 1073741824; //1G

    /**
     * Android Test 日志分割大小限制
     */
    public final static int TEST_CACHE_MAX_TOTAL_SIZE = 100;

    /**
     * Android Test 日志总大小限制
     */
    private final static int TEST_SPLIT_FILE_SIZE = 50;

    public final static String LOG_BACKUP_CACHE_PATH = "LogBackup";

    private final File cacheFile;

    private final File backupLogDir;

    private final int cacheMaxTotalSize;
    private final int splitFileSize;

    public LogFileHelper(Context context, File cache, boolean isAndroidTest) {
        this.backupLogDir = new File(context.getFilesDir(), LOG_BACKUP_CACHE_PATH);
        if (!backupLogDir.exists()) {
            backupLogDir.mkdirs();
        }
        this.cacheFile = cache;
        this.splitFileSize = isAndroidTest ? TEST_SPLIT_FILE_SIZE : SPLIT_FILE_SIZE;
        this.cacheMaxTotalSize = isAndroidTest ? TEST_CACHE_MAX_TOTAL_SIZE : CACHE_MAX_TOTAL_SIZE;
    }

    /**
     * 追加日志
     * <p>
     * 注意：避免使用{@link LogUtils} 打印日志，可能会引起代码堆栈越界
     *
     * @param logMessage 日志数据
     */
    public void appendLog(String logMessage) {
        if (cacheFile.length() >= splitFileSize) {
            String filePath = cacheFile.getAbsolutePath();
            splitLog(filePath);
        }
        try {
            Utils.writeToFile(cacheFile, logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分日志至 {@link #LOG_BACKUP_CACHE_PATH}
     *
     * @param filePath 日志路径
     */
    private void splitLog(String filePath) {
        File oldLogFile = new File(filePath);
        File newLogFile = new File(backupLogDir,
                Utils.getNameWithoutExtension(oldLogFile.getName())
                        + "_" + System.currentTimeMillis() + ".log");
        oldLogFile.renameTo(newLogFile);
        deleteOldLogFilesIfNecessary(backupLogDir);
    }

    /**
     * 超过 {@link #CACHE_MAX_TOTAL_SIZE} 就会删除最旧的文件
     *
     * @param logDir 日志路径
     */
    private void deleteOldLogFilesIfNecessary(File logDir) {
        File[] logFiles = logDir.listFiles();
        if (logFiles == null || logFiles.length == 0) {
            return;
        }

        Arrays.sort(logFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });

        long totalSize = 0;
        for (File file : logFiles) {
            totalSize += file.length();
        }

        if (totalSize > cacheMaxTotalSize) {
            long deletedSize = 0;
            for (File file : logFiles) {
                long fileSize = file.length();
                if (file.delete()) {
                    deletedSize += fileSize;
                    if (totalSize - deletedSize <= cacheMaxTotalSize * 0.8) {
                        break;
                    }
                } else {
                    LogUtils.e(TAG, "Failed to delete log file: " + file.getAbsolutePath());
                }
            }
        }
    }


}
