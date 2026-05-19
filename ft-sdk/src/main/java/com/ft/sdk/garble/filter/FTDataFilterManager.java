package com.ft.sdk.garble.filter;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.threadpool.RemoteConfigThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FTDataFilterManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "DataFilterManager";
    private static final int DEFAULT_PULL_INTERVAL_SECONDS = 30 * 60;
    private static volatile FTDataFilterManager instance;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile FTDataFilter localFilter = FTDataFilter.empty();
    private volatile FTDataFilter remoteFilter = FTDataFilter.empty();
    private volatile boolean enabled;
    private volatile int pullIntervalSeconds = DEFAULT_PULL_INTERVAL_SECONDS;
    private volatile long lastPullTimeMs;
    private volatile String lastMd5;
    private volatile boolean remotePoolTouched;
    private volatile boolean remoteFilterSynced;

    private FTDataFilterManager() {
    }

    public static FTDataFilterManager get() {
        if (instance == null) {
            synchronized (FTDataFilterManager.class) {
                if (instance == null) {
                    instance = new FTDataFilterManager();
                }
            }
        }
        return instance;
    }

    public void init(FTSDKConfig config) {
        if (config == null) {
            return;
        }
        enabled = config.isEnableDataFilter();
        pullIntervalSeconds = Math.max(1, config.getDataFilterUpdateInterval());
        lastPullTimeMs = 0;
        lastMd5 = null;
        remotePoolTouched = false;
        remoteFilterSynced = false;
        localFilter = FTDataFilter.empty();
        remoteFilter = FTDataFilter.empty();

        if (!enabled) {
            return;
        }

        HashMap<String, String[]> localFilters = config.getDataFilters();
        if (localFilters != null && !localFilters.isEmpty()) {
            localFilter = FTDataFilter.compile(localFilters);
            LogUtils.d(TAG, "local data filters configured, categories:" + localFilters.keySet()
                    + ", rules:" + countRules(localFilters));
        }
        syncRemoteIfNeeded(true);
    }

    public boolean isFiltered(DataType dataType, String measurement, String uuid,
                              Map<String, Object> tags, Map<String, Object> fields) {
        if (!enabled) {
            return false;
        }
        if (isFilteredBy(localFilter, "local", dataType, measurement, uuid, tags, fields)) {
            return true;
        }
        return isFilteredBy(remoteFilter, "remote", dataType, measurement, uuid, tags, fields);
    }

    private boolean isFilteredBy(FTDataFilter filterSource, String source, DataType dataType,
                                 String measurement, String uuid, Map<String, Object> tags,
                                 Map<String, Object> fields) {
        if (filterSource == null || filterSource.isEmpty()) {
            return false;
        }
        boolean filtered = filterSource.isFiltered(dataType, measurement, tags, fields);
        if (!filtered) {
            return false;
        }
        String category = FTDataFilter.categoryOf(dataType);
        LogUtils.w(TAG, "drop data by " + source + " filter, category:" + category
                + ", measurement:" + measurement
                + ", uuid:" + uuid
                + ", filters:" + filterSource.getRawConditions(category));
        return true;
    }

    public void syncRemoteIfNeeded() {
        syncRemoteIfNeeded(false);
    }

    public void syncRemoteIfNeeded(boolean force) {
        if (!enabled) {
            return;
        }
        if (running.get()) {
            if (force) {
                LogUtils.d(TAG, "skip remote filter pull, previous request is running");
            }
            return;
        }
        boolean hasDatakitUrl = !Utils.isNullOrEmpty(FTHttpConfigManager.get().getDatakitUrl());
        boolean hasDatawayUrl = !Utils.isNullOrEmpty(FTHttpConfigManager.get().getDatawayUrl());
        if (!hasDatakitUrl && !hasDatawayUrl) {
            if (force) {
                LogUtils.d(TAG, "skip remote filter pull, datakit and dataway url are empty");
            }
            return;
        }
        if (!hasDatakitUrl && Utils.isNullOrEmpty(FTHttpConfigManager.get().getClientToken())) {
            if (force) {
                LogUtils.d(TAG, "skip remote filter pull, client token is empty");
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (!force && now - lastPullTimeMs < pullIntervalSeconds * 1000L) {
            return;
        }

        if (!running.compareAndSet(false, true)) {
            return;
        }

        LogUtils.d(TAG, "schedule remote filter pull, force:" + force
                + ", interval:" + pullIntervalSeconds + "s");
        remotePoolTouched = true;
        RemoteConfigThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    pullRemoteFilter();
                } finally {
                    lastPullTimeMs = System.currentTimeMillis();
                    running.set(false);
                }
            }
        });
    }

    private void pullRemoteFilter() {
        try {
            String target = Utils.isNullOrEmpty(FTHttpConfigManager.get().getDatakitUrl())
                    ? "dataway" : "datakit";
            LogUtils.d(TAG, "remote filter pull start, target:" + target);
            HttpBuilder builder = HttpBuilder.Builder()
                    .addHeadParam(Constants.SYNC_DATA_CONTENT_TYPE_HEADER,
                            Constants.SYNC_DATA_CONTENT_TYPE_VALUE)
                    .setModel(Constants.URL_DATAKIT_PULL)
                    .addParam("filters", "true")
                    .setMethod(RequestMethod.GET);
            if ("dataway".equals(target)) {
                builder.useDatawayUrl();
            }
            FTResponseData data = builder.executeSync();

            if (data.getCode() != HttpURLConnection.HTTP_OK) {
                LogUtils.w(TAG, "remote filter pull failed, code:" + data.getCode());
                return;
            }

            String body = data.getMessage();
            if (Utils.isNullOrEmpty(body)) {
                LogUtils.d(TAG, "remote filter pull success, response body is empty");
                return;
            }
            String md5 = Utils.toMD5(body);
            if (md5.equals(lastMd5)) {
                LogUtils.d(TAG, "remote filter pull success, config unchanged");
                return;
            }

            RemoteFilterConfig remote = parseRemoteFilter(body);
            remoteFilter = FTDataFilter.compile(remote.filters);
            remoteFilterSynced = true;
            lastMd5 = md5;
            if (remote.pullIntervalSeconds > 0) {
                pullIntervalSeconds = remote.pullIntervalSeconds;
            }
            LogUtils.d(TAG, "remote data filters updated, categories:" + remote.filters.keySet()
                    + ", rules:" + countRules(remote.filters)
                    + ", interval:" + pullIntervalSeconds + "s");
        } catch (Exception e) {
            LogUtils.e(TAG, "remote filter load error:" + LogUtils.getStackTraceString(e));
        }
    }

    private RemoteFilterConfig parseRemoteFilter(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);
        JSONObject filtersJson = json.optJSONObject("filters");
        HashMap<String, String[]> filters = new HashMap<>();
        if (filtersJson != null) {
            Iterator<String> keys = filtersJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = filtersJson.opt(key);
                if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    String[] rules = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        rules[i] = array.optString(i);
                    }
                    filters.put(key, rules);
                } else if (value instanceof String) {
                    filters.put(key, new String[]{(String) value});
                } else {
                    String type = value == null ? "null" : value.getClass().getSimpleName();
                    LogUtils.d(TAG, "ignore remote filter category:" + key
                            + ", unsupported value type:" + type);
                }
            }
        }

        RemoteFilterConfig config = new RemoteFilterConfig();
        config.filters = filters;
        config.pullIntervalSeconds = parsePullIntervalSeconds(json.opt("pull_interval"));
        return config;
    }

    private int parsePullIntervalSeconds(Object value) {
        if (value instanceof Number) {
            long raw = ((Number) value).longValue();
            if (raw <= 0) {
                return 0;
            }
            if (raw < 1_000_000L) {
                return (int) Math.min(Integer.MAX_VALUE, raw);
            }
            long ms = raw / 1_000_000L;
            return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, ms / 1000L));
        }
        if (value instanceof String) {
            return parseDurationString((String) value);
        }
        return 0;
    }

    private int parseDurationString(String duration) {
        if (duration == null) {
            return 0;
        }
        String value = duration.trim().toLowerCase();
        try {
            if (value.endsWith("ms")) {
                long ms = Long.parseLong(value.substring(0, value.length() - 2).trim());
                return (int) Math.max(1L, ms / 1000L);
            } else if (value.endsWith("s")) {
                return Integer.parseInt(value.substring(0, value.length() - 1).trim());
            } else if (value.endsWith("m")) {
                return Integer.parseInt(value.substring(0, value.length() - 1).trim()) * 60;
            } else if (value.endsWith("h")) {
                return Integer.parseInt(value.substring(0, value.length() - 1).trim()) * 3600;
            }
            return Integer.parseInt(value);
        } catch (Exception e) {
            LogUtils.w(TAG, "invalid pull_interval:" + duration);
            return 0;
        }
    }

    private int countRules(Map<String, String[]> filters) {
        int count = 0;
        if (filters == null) {
            return count;
        }
        for (String[] rules : filters.values()) {
            if (rules != null) {
                count += rules.length;
            }
        }
        return count;
    }

    public boolean shouldDisableServerFilter() {
        return enabled && remoteFilterSynced;
    }

    public static void release() {
        FTDataFilterManager manager = instance;
        if (manager != null && manager.remotePoolTouched) {
            RemoteConfigThreadPool.get().shutDown();
        }
        instance = null;
    }

    private static class RemoteFilterConfig {
        HashMap<String, String[]> filters;
        int pullIntervalSeconds;
    }
}
