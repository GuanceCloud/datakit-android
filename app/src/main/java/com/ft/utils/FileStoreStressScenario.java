package com.ft.utils;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates FileStore load from multiple threads/processes and reports the final consistency state.
 */
public final class FileStoreStressScenario {
    public static final String TAG = "FileStoreStress";
    public static final String ROLE_MAIN = "main";
    public static final String ROLE_REMOTE = "remote";

    public static final String EXTRA_WORKERS = "workers";
    public static final String EXTRA_EVENTS_PER_WORKER = "events_per_worker";
    public static final String EXTRA_PAYLOAD_BYTES = "payload_bytes";

    public static final int DEFAULT_WORKERS = 4;
    public static final int DEFAULT_EVENTS_PER_WORKER = 800;
    public static final int DEFAULT_PAYLOAD_BYTES = 2048;

    private static final long SDK_SETTLE_INTERVAL_MS = 2000;
    private static final long SDK_SETTLE_MAX_WAIT_MS = 60000;
    private static final int SDK_SETTLE_STABLE_ROUNDS = 3;

    private static final String ROOT_DIR_NAME = "ft_data_store";
    private static final String SYNC_DIR_NAME = "sync";
    private static final String RUM_VIEW_DIR_NAME = "rum_view";
    private static final String RUM_ACTION_DIR_NAME = "rum_action";
    private static final String SIZE_FILE_NAME = "store.size";

    private FileStoreStressScenario() {
    }

    public static void startAsync(Context context, String role) {
        startAsync(context, role, DEFAULT_WORKERS, DEFAULT_EVENTS_PER_WORKER, DEFAULT_PAYLOAD_BYTES);
    }

    public static void startAsync(final Context context, final String role, final int workers,
                                  final int eventsPerWorker, final int payloadBytes) {
        final Context appContext = context.getApplicationContext();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runBlocking(appContext, role, workers, eventsPerWorker, payloadBytes);
            }
        }, "FTFileStoreStress-" + role);
        thread.start();
    }

    public static void runBlocking(Context context, String role, int workers, int eventsPerWorker,
                                   int payloadBytes) {
        long startMs = SystemClock.elapsedRealtime();
        String runId = role + "-" + Process.myPid() + "-" + System.currentTimeMillis();
        Log.i(TAG, "start role=" + role
                + ",pid=" + Process.myPid()
                + ",workers=" + workers
                + ",eventsPerWorker=" + eventsPerWorker
                + ",payloadBytes=" + payloadBytes
                + ",runId=" + runId);

        final AtomicInteger logCount = new AtomicInteger();
        final AtomicInteger actionCount = new AtomicInteger();
        final AtomicInteger errorCount = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(workers);
        ExecutorService executor = Executors.newFixedThreadPool(workers);

        for (int worker = 0; worker < workers; worker++) {
            final int workerIndex = worker;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int index = 0; index < eventsPerWorker; index++) {
                            writeEvent(runId, role, workerIndex, index, payloadBytes,
                                    logCount, actionCount);
                        }
                    } catch (Throwable throwable) {
                        errorCount.incrementAndGet();
                        Log.e(TAG, "worker failed role=" + role
                                + ",worker=" + workerIndex
                                + ",error=" + throwable.getMessage(), throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!latch.await(5, TimeUnit.MINUTES)) {
                Log.w(TAG, "timeout role=" + role + ",unfinishedWorkers=" + latch.getCount());
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "interrupted role=" + role, e);
            Thread.currentThread().interrupt();
        }

        WaitResult waitResult = waitForSdkWrites(context, role);
        CacheSnapshot after = waitResult.snapshot;
        Log.i(TAG, "finish role=" + role
                + ",pid=" + Process.myPid()
                + ",runId=" + runId
                + ",stable=" + waitResult.stable
                + ",settleElapsedMs=" + waitResult.elapsedMs
                + ",logs=" + logCount.get()
                + ",actions=" + actionCount.get()
                + ",errors=" + errorCount.get()
                + ",elapsedMs=" + (SystemClock.elapsedRealtime() - startMs)
                + ",actualBytes=" + after.actualBytes
                + ",trackedBytes=" + after.trackedBytes
                + ",diffBytes=" + after.diffBytes()
                + ",syncFiles=" + after.syncFiles
                + ",rumViewFiles=" + after.rumViewFiles
                + ",rumActionFiles=" + after.rumActionFiles);
    }

    private static WaitResult waitForSdkWrites(Context context, String role) {
        CacheSnapshot previous = null;
        CacheSnapshot latest = null;
        int stableRounds = 0;
        long start = SystemClock.elapsedRealtime();

        while (SystemClock.elapsedRealtime() - start < SDK_SETTLE_MAX_WAIT_MS) {
            try {
                Thread.sleep(SDK_SETTLE_INTERVAL_MS);
            } catch (InterruptedException e) {
                Log.w(TAG, "waitForSdkWrites interrupted role=" + role, e);
                Thread.currentThread().interrupt();
                break;
            }

            latest = readCacheSnapshot(context);
            if (previous != null && latest.hasSameContent(previous)) {
                stableRounds++;
                if (stableRounds >= SDK_SETTLE_STABLE_ROUNDS) {
                    return new WaitResult(latest, true,
                            SystemClock.elapsedRealtime() - start);
                }
            } else {
                stableRounds = 0;
            }
            previous = latest;
        }

        if (latest == null) {
            latest = readCacheSnapshot(context);
        }
        long elapsedMs = SystemClock.elapsedRealtime() - start;
        Log.w(TAG, "waitForSdkWrites timeout role=" + role
                + ",elapsedMs=" + elapsedMs
                + ",actualBytes=" + latest.actualBytes
                + ",trackedBytes=" + latest.trackedBytes
                + ",diffBytes=" + latest.diffBytes());
        return new WaitResult(latest, false, elapsedMs);
    }

    private static CacheSnapshot readCacheSnapshot(Context context) {
        File rootDir = new File(context.getFilesDir(), ROOT_DIR_NAME);
        File syncDir = new File(rootDir, SYNC_DIR_NAME);
        File rumViewDir = new File(rootDir, RUM_VIEW_DIR_NAME);
        File rumActionDir = new File(rootDir, RUM_ACTION_DIR_NAME);

        return new CacheSnapshot(
                directorySize(syncDir),
                directorySize(rumViewDir),
                directorySize(rumActionDir),
                countFiles(syncDir),
                countFiles(rumViewDir),
                countFiles(rumActionDir),
                readTrackedSize(new File(rootDir, SIZE_FILE_NAME))
        );
    }

    private static void writeEvent(String runId, String role, int workerIndex, int index,
                                   int payloadBytes, AtomicInteger logCount,
                                   AtomicInteger actionCount) {
        String payload = buildPayload(role, workerIndex, index, payloadBytes);
        HashMap<String, Object> property = new HashMap<>();
        property.put("stress_run_id", runId);
        property.put("stress_role", role);
        property.put("stress_worker", workerIndex);
        property.put("stress_index", index);
        property.put("stress_payload", payload);

        FTLogger.getInstance().logBackground("FileStoreStress log role=" + role
                + ",worker=" + workerIndex
                + ",index=" + index, Status.ERROR, property);
        logCount.incrementAndGet();

        FTRUMGlobalManager.get().addAction(String.format(Locale.US,
                "FileStoreStress action %s-%d-%d", role, workerIndex, index),
                "file_store_stress", property);
        actionCount.incrementAndGet();
    }

    private static String buildPayload(String role, int workerIndex, int index, int payloadBytes) {
        String prefix = role + "-" + workerIndex + "-" + index + "-";
        StringBuilder builder = new StringBuilder(payloadBytes + prefix.length());
        builder.append(prefix);
        while (builder.length() < payloadBytes) {
            builder.append("0123456789abcdef");
        }
        if (builder.length() > payloadBytes) {
            builder.setLength(payloadBytes);
        }
        return builder.toString();
    }

    private static long directorySize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }
        long total = 0;
        File[] children = file.listFiles();
        if (children == null) {
            return 0;
        }
        for (File child : children) {
            total += directorySize(child);
        }
        return total;
    }

    private static int countFiles(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return 1;
        }
        int total = 0;
        File[] children = file.listFiles();
        if (children == null) {
            return 0;
        }
        for (File child : children) {
            total += countFiles(child);
        }
        return total;
    }

    private static long readTrackedSize(File sizeFile) {
        if (!sizeFile.isFile()) {
            return -1;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sizeFile);
            byte[] buffer = new byte[(int) Math.min(Math.max(sizeFile.length(), 1), 64)];
            int length = inputStream.read(buffer);
            if (length <= 0) {
                return -1;
            }
            String value = new String(buffer, 0, length, StandardCharsets.UTF_8).trim();
            return Long.parseLong(value);
        } catch (IOException | NumberFormatException e) {
            Log.w(TAG, "readTrackedSize failed path=" + sizeFile.getAbsolutePath(), e);
            return -2;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static final class CacheSnapshot {
        public final long syncBytes;
        public final long rumViewBytes;
        public final long rumActionBytes;
        public final int syncFiles;
        public final int rumViewFiles;
        public final int rumActionFiles;
        public final long actualBytes;
        public final long trackedBytes;

        CacheSnapshot(long syncBytes, long rumViewBytes, long rumActionBytes, int syncFiles,
                      int rumViewFiles, int rumActionFiles, long trackedBytes) {
            this.syncBytes = syncBytes;
            this.rumViewBytes = rumViewBytes;
            this.rumActionBytes = rumActionBytes;
            this.syncFiles = syncFiles;
            this.rumViewFiles = rumViewFiles;
            this.rumActionFiles = rumActionFiles;
            this.actualBytes = syncBytes + rumViewBytes + rumActionBytes;
            this.trackedBytes = trackedBytes;
        }

        public long diffBytes() {
            if (trackedBytes < 0) {
                return trackedBytes;
            }
            return trackedBytes - actualBytes;
        }

        boolean hasSameContent(CacheSnapshot other) {
            return other != null
                    && syncBytes == other.syncBytes
                    && rumViewBytes == other.rumViewBytes
                    && rumActionBytes == other.rumActionBytes
                    && syncFiles == other.syncFiles
                    && rumViewFiles == other.rumViewFiles
                    && rumActionFiles == other.rumActionFiles
                    && trackedBytes == other.trackedBytes;
        }
    }

    private static final class WaitResult {
        final CacheSnapshot snapshot;
        final boolean stable;
        final long elapsedMs;

        WaitResult(CacheSnapshot snapshot, boolean stable, long elapsedMs) {
            this.snapshot = snapshot;
            this.stable = stable;
            this.elapsedMs = elapsedMs;
        }
    }
}
