package com.ft.sdk.internal.anr.historical;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.ViewBean;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class FileHistoricalRumContextStore implements HistoricalRumContextStore {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FileRumContext";
    private static final String FILE_SUFFIX = ".json";
    private static final int MAX_CONTEXT_FILES = 256;
    private static final long RETENTION_MS = 90L * 24 * 60 * 60 * 1000;

    private static final String KEY_PROCESS_NAME = "process_name";
    private static final String KEY_PROCESS_RUN_ID = "process_run_id";
    private static final String KEY_PROCESS_START_MS = "process_start_ms";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_VIEW_ID = "view_id";
    private static final String KEY_VIEW_NAME = "view_name";
    private static final String KEY_VIEW_START_NS = "view_start_ns";
    private static final String KEY_VIEW_TIME_SPENT_NS = "view_time_spent_ns";
    private static final String KEY_VIEW_CLOSED = "view_closed";
    private static final String KEY_COLLECT_TYPE = "collect_type";
    private static final String KEY_UPDATED_AT_MS = "updated_at_ms";

    private static volatile FileHistoricalRumContextStore instance;

    private final FTFileStorePaths paths;
    private final FTFileLock lock;

    public FileHistoricalRumContextStore(Context context) {
        this(new FTFileStorePaths(context));
    }

    public static FileHistoricalRumContextStore get(Context context) {
        FileHistoricalRumContextStore store = instance;
        if (store == null) {
            synchronized (FileHistoricalRumContextStore.class) {
                store = instance;
                if (store == null) {
                    Context applicationContext = context.getApplicationContext();
                    store = new FileHistoricalRumContextStore(
                            applicationContext == null ? context : applicationContext);
                    instance = store;
                }
            }
        }
        return store;
    }

    FileHistoricalRumContextStore(FTFileStorePaths paths) {
        this.paths = paths;
        this.lock = new FTFileLock(paths.getLockFile());
    }

    public void save(final ViewBean view) {
        if (view == null || isEmpty(view.getId()) || isEmpty(view.getProcessRunId())) {
            return;
        }
        try {
            saveOrThrow(view);
        } catch (Exception e) {
            LogUtils.e(TAG, "save failed: " + e.getMessage());
        }
    }

    void saveOrThrow(final ViewBean view) throws Exception {
        if (view == null || isEmpty(view.getId()) || isEmpty(view.getProcessRunId())) {
            return;
        }
        lock.withLock(new FTFileLock.LockedOperation<Void>() {
            @Override
            public Void run() throws Exception {
                paths.ensureReady();
                JSONObject json = new JSONObject();
                putNullable(json, KEY_PROCESS_NAME, view.getProcessName());
                putNullable(json, KEY_PROCESS_RUN_ID, view.getProcessRunId());
                json.put(KEY_PROCESS_START_MS, view.getProcessStartMs());
                putNullable(json, KEY_SESSION_ID, view.getSessionId());
                putNullable(json, KEY_VIEW_ID, view.getId());
                putNullable(json, KEY_VIEW_NAME, view.getViewName());
                json.put(KEY_VIEW_START_NS, view.getStartTime());
                json.put(KEY_VIEW_TIME_SPENT_NS, view.getTimeSpent());
                json.put(KEY_VIEW_CLOSED, view.isClose());
                json.put(KEY_COLLECT_TYPE, view.getCollectType().getValue());
                json.put(KEY_UPDATED_AT_MS, System.currentTimeMillis());
                FTAtomicFileHelper.writeUtf8(contextFile(view.getId()), json.toString());
                trimOldestIfNeeded();
                return null;
            }
        });
    }

    @Override
    public List<HistoricalRumContext> load(final String processName) throws Exception {
        return lock.withLock(
                new FTFileLock.LockedOperation<List<HistoricalRumContext>>() {
                    @Override
                    public List<HistoricalRumContext> run() throws Exception {
                        paths.ensureReady();
                        ArrayList<HistoricalRumContext> result = new ArrayList<>();
                        File[] files = contextFiles();
                        if (files == null) {
                            return result;
                        }
                        long expiresBefore = System.currentTimeMillis() - RETENTION_MS;
                        Exception firstFailure = null;
                        for (File file : files) {
                            if (file.lastModified() < expiresBefore) {
                                file.delete();
                                continue;
                            }
                            try {
                                JSONObject json = new JSONObject(
                                        FTAtomicFileHelper.readUtf8(file));
                                String storedProcessName = nullableString(
                                        json, KEY_PROCESS_NAME);
                                if (processName != null
                                        && !processName.equals(storedProcessName)) {
                                    continue;
                                }
                                result.add(new HistoricalRumContext(
                                        storedProcessName,
                                        nullableString(json, KEY_PROCESS_RUN_ID),
                                        json.optLong(KEY_PROCESS_START_MS),
                                        nullableString(json, KEY_SESSION_ID),
                                        nullableString(json, KEY_VIEW_ID),
                                        nullableString(json, KEY_VIEW_NAME),
                                        json.optLong(KEY_VIEW_START_NS),
                                        json.optLong(KEY_VIEW_TIME_SPENT_NS),
                                        json.optBoolean(KEY_VIEW_CLOSED),
                                        CollectType.fromValue(
                                                nullableString(json, KEY_COLLECT_TYPE))));
                            } catch (Exception e) {
                                if (firstFailure == null) {
                                    firstFailure = e;
                                }
                            }
                        }
                        if (firstFailure != null) {
                            throw new HistoricalRumContextLoadException(
                                    result, firstFailure);
                        }
                        return result;
                    }
                });
    }

    private void trimOldestIfNeeded() {
        File[] files = contextFiles();
        if (files == null || files.length <= MAX_CONTEXT_FILES) {
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File left, File right) {
                return Long.compare(left.lastModified(), right.lastModified());
            }
        });
        for (int i = 0; i < files.length - MAX_CONTEXT_FILES; i++) {
            files[i].delete();
        }
    }

    private File[] contextFiles() {
        return paths.getHistoricalViewDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(FILE_SUFFIX);
            }
        });
    }

    private File contextFile(String viewId) {
        String encoded = Base64.encodeToString(
                viewId.getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        return new File(paths.getHistoricalViewDir(), encoded + FILE_SUFFIX);
    }

    private static void putNullable(JSONObject json, String key, String value) throws Exception {
        if (value == null) {
            json.put(key, JSONObject.NULL);
        } else {
            json.put(key, value);
        }
    }

    private static String nullableString(JSONObject json, String key) {
        return json.isNull(key) ? null : json.optString(key, null);
    }

    private static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}
