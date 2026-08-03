package com.ft.sdk.internal.anr.historical;

import android.util.Base64;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.db.file.FTAtomicFileHelper;
import com.ft.sdk.garble.db.file.FTFileLock;
import com.ft.sdk.garble.db.file.FTFileStorePaths;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    void importV4Claim(final JSONObject imported) throws Exception {
        lock.withLock(new FTFileLock.LockedOperation<Void>() {
            @Override
            public Void run() throws Exception {
                paths.ensureReady();
                String exitKey = imported.optString("exit_key", null);
                if (exitKey == null || exitKey.length() == 0) {
                    return null;
                }
                File file = claimFile(exitKey);
                JSONObject existing = read(file);
                if (existing == null
                        || (isTerminal(imported.optInt("state"))
                        && !isTerminal(existing.optInt("state")))) {
                    FTAtomicFileHelper.writeUtf8(file, imported.toString());
                }
                return null;
            }
        });
    }

    private static boolean isTerminal(int state) {
        return state == STATE_COMMITTED || state == STATE_DROPPED;
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
                        HistoricalRumContext snapshot = snapshot(
                                claim, exit.getProcessName());
                        if (state == STATE_COMMITTED) {
                            if (claim.optBoolean("event_visible")) {
                                return Claim.skipped();
                            }
                            return Claim.committed(snapshot);
                        }
                        if (state == STATE_DROPPED) {
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
    public boolean markCommitted(String exitKey, String ownerRunId, long nowMs) {
        return updateState(exitKey, ownerRunId, STATE_COMMITTED, 0, null, nowMs);
    }

    @Override
    public List<PendingCommit> loadPendingCommits() {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<List<PendingCommit>>() {
                @Override
                public List<PendingCommit> run() throws Exception {
                    paths.ensureReady();
                    File[] files = claimFiles();
                    if (files == null) {
                        return Collections.emptyList();
                    }
                    ArrayList<PendingCommit> pending = new ArrayList<>();
                    for (File file : files) {
                        try {
                            JSONObject claim = read(file);
                            if (claim == null
                                    || claim.optInt("state") != STATE_COMMITTED
                                    || claim.optBoolean("event_visible")) {
                                continue;
                            }
                            String exitKey = claim.optString("exit_key", null);
                            String eventDedupeKey = claim.optString(
                                    "event_dedupe_key", null);
                            if (exitKey == null || exitKey.length() == 0
                                    || eventDedupeKey == null
                                    || eventDedupeKey.length() == 0) {
                                continue;
                            }
                            pending.add(new PendingCommit(
                                    exitKey,
                                    eventDedupeKey,
                                    CollectType.fromValue(
                                            claim.optString("collect_type", null))));
                        } catch (Exception e) {
                            LogUtils.e(TAG, "loadPendingCommits skipped corrupt claim: "
                                    + e.getMessage());
                        }
                    }
                    return pending;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "loadPendingCommits failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean markEventVisible(final String exitKey,
                                    final String eventDedupeKey,
                                    final long nowMs) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    File file = claimFile(exitKey);
                    JSONObject claim = read(file);
                    if (claim == null
                            || claim.optInt("state") != STATE_COMMITTED
                            || !eventDedupeKey.equals(claim.optString(
                                    "event_dedupe_key", null))) {
                        return false;
                    }
                    claim.put("event_visible", true);
                    claim.put("updated_at_ms", nowMs);
                    FTAtomicFileHelper.writeUtf8(file, claim.toString());
                    return true;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "markEventVisible failed: " + e.getMessage());
            return false;
        }
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
                    File[] files = claimFiles();
                    if (files == null) {
                        return null;
                    }
                    for (File file : files) {
                        JSONObject claim = read(file);
                        if (claim == null) {
                            continue;
                        }
                        int state = claim.optInt("state");
                        boolean removable = state == STATE_DROPPED
                                || (state == STATE_COMMITTED
                                && claim.optBoolean("event_visible"));
                        if (removable
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

    private boolean updateState(final String exitKey, final String ownerRunId,
                                final int state, final long leaseUntilMs,
                                final String dropReason, final long nowMs) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    File file = claimFile(exitKey);
                    JSONObject claim = read(file);
                    if (claim == null
                            || claim.optInt("state") != STATE_CLAIMED
                            || !ownerRunId.equals(claim.optString("owner_run_id"))) {
                        return false;
                    }
                    claim.put("state", state);
                    claim.put("lease_until_ms", leaseUntilMs);
                    claim.put("updated_at_ms", nowMs);
                    if (dropReason != null) {
                        claim.put("drop_reason", dropReason);
                    }
                    FTAtomicFileHelper.writeUtf8(file, claim.toString());
                    return true;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "updateState failed: " + e.getMessage());
            return false;
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

    private File[] claimFiles() {
        return paths.getHistoricalExitDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile()
                        && pathname.getName().endsWith(FILE_SUFFIX);
            }
        });
    }
}
