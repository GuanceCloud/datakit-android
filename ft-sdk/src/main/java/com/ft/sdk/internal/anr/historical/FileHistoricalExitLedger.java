package com.ft.sdk.internal.anr.historical;

import android.util.Base64;

import com.ft.sdk.garble.db.file.FTAtomicFileHelper;
import com.ft.sdk.garble.db.file.FTFileLock;
import com.ft.sdk.garble.db.file.FTFileStorePaths;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;

final class FileHistoricalExitLedger implements HistoricalExitLedger {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FileExitLedger";
    private static final String FILE_SUFFIX = ".json";
    private static final int STATE_CLAIMED = 1;
    private static final int STATE_COMMITTED = 2;
    private static final int STATE_RETRY = 3;
    private static final int STATE_DROPPED = 4;

    private final FTFileStorePaths paths;
    private final FTFileLock lock;

    FileHistoricalExitLedger(FTFileStorePaths paths) {
        this.paths = paths;
        this.lock = new FTFileLock(paths.getLockFile());
    }

    @Override
    public Claim tryClaim(final String exitKey, final String ownerRunId,
                          final long nowMs, final long leaseUntilMs,
                          final ProcessExitRecord exit,
                          final HistoricalRumContext context,
                          final String eventDedupeKey) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Claim>() {
                @Override
                public Claim run() throws Exception {
                    paths.ensureReady();
                    File file = claimFile(exitKey);
                    JSONObject claim = read(file);
                    if (claim != null) {
                        int state = claim.optInt("state");
                        if (state == STATE_COMMITTED || state == STATE_DROPPED) {
                            return Claim.skipped();
                        }
                        if ((state == STATE_CLAIMED || state == STATE_RETRY)
                                && claim.optLong("lease_until_ms") > nowMs) {
                            return Claim.skipped();
                        }
                    } else {
                        claim = new JSONObject();
                        claim.put("exit_key", exitKey);
                    }
                    HistoricalRumContext snapshot = snapshot(claim, exit.getProcessName());
                    if (snapshot == null && context != null) {
                        claim.put("session_id", context.getSessionId());
                        claim.put("view_id", context.getViewId());
                        claim.put("view_name", context.getViewName());
                        claim.put("collect_type", context.getCollectType().getValue());
                        snapshot = context;
                    }
                    claim.put("state", STATE_CLAIMED);
                    claim.put("owner_run_id", ownerRunId);
                    claim.put("lease_until_ms", leaseUntilMs);
                    claim.put("attempt_count", claim.optInt("attempt_count") + 1);
                    claim.put("process_name", exit.getProcessName());
                    claim.put("pid", exit.getPid());
                    claim.put("exit_time_ms", exit.getTimestampMs());
                    claim.put("reason", exit.getReason());
                    claim.put("event_dedupe_key", eventDedupeKey);
                    claim.put("updated_at_ms", nowMs);
                    FTAtomicFileHelper.writeUtf8(file, claim.toString());
                    return Claim.acquired(snapshot);
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "tryClaim failed: " + e.getMessage());
            return Claim.skipped();
        }
    }

    @Override
    public void markCommitted(String exitKey, String ownerRunId, long nowMs) {
        updateState(exitKey, ownerRunId, STATE_COMMITTED, 0, null, nowMs);
    }

    @Override
    public void markRetry(String exitKey, String ownerRunId, long retryAtMs, long nowMs) {
        updateState(exitKey, ownerRunId, STATE_RETRY, retryAtMs, null, nowMs);
    }

    @Override
    public void markDropped(String exitKey, String ownerRunId, String reason, long nowMs) {
        updateState(exitKey, ownerRunId, STATE_DROPPED, 0, reason, nowMs);
    }

    @Override
    public void cleanup(final long terminalBeforeMs) {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    paths.ensureReady();
                    File[] files = paths.getHistoricalExitDir().listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.isFile()
                                    && pathname.getName().endsWith(FILE_SUFFIX);
                        }
                    });
                    if (files == null) {
                        return null;
                    }
                    for (File file : files) {
                        JSONObject claim = read(file);
                        if (claim == null) {
                            continue;
                        }
                        int state = claim.optInt("state");
                        if ((state == STATE_COMMITTED || state == STATE_DROPPED)
                                && claim.optLong("updated_at_ms") < terminalBeforeMs) {
                            file.delete();
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "cleanup failed: " + e.getMessage());
        }
    }

    private void updateState(final String exitKey, final String ownerRunId,
                             final int state, final long leaseUntilMs,
                             final String dropReason, final long nowMs) {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    File file = claimFile(exitKey);
                    JSONObject claim = read(file);
                    if (claim == null
                            || claim.optInt("state") != STATE_CLAIMED
                            || !ownerRunId.equals(claim.optString("owner_run_id"))) {
                        return null;
                    }
                    claim.put("state", state);
                    claim.put("lease_until_ms", leaseUntilMs);
                    claim.put("updated_at_ms", nowMs);
                    if (dropReason != null) {
                        claim.put("drop_reason", dropReason);
                    }
                    FTAtomicFileHelper.writeUtf8(file, claim.toString());
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "updateState failed: " + e.getMessage());
        }
    }

    private JSONObject read(File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        return new JSONObject(FTAtomicFileHelper.readUtf8(file));
    }

    private HistoricalRumContext snapshot(JSONObject claim, String processName) {
        if (!claim.has("session_id") || !claim.has("view_id")) {
            return null;
        }
        return HistoricalRumContext.fromSnapshot(
                processName,
                claim.optString("session_id", null),
                claim.optString("view_id", null),
                claim.optString("view_name", null),
                claim.optString("collect_type", null));
    }

    private File claimFile(String exitKey) {
        String encoded = Base64.encodeToString(
                exitKey.getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        return new File(paths.getHistoricalExitDir(), encoded + FILE_SUFFIX);
    }
}
