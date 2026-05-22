package com.ft.sdk.garble.filter;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FTDataFilterManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "DataFilterManager";
    private static final int DEFAULT_PULL_INTERVAL_SECONDS = 10;
    private static volatile FTDataFilterManager instance;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile FTDataFilter localFilter = FTDataFilter.empty();
    private volatile RemoteFilterState remoteFilterState = RemoteFilterState.empty();
    private volatile boolean enabled;
    private volatile int pullIntervalSeconds = DEFAULT_PULL_INTERVAL_SECONDS;
    private volatile long lastPullTimeMs;
    private volatile boolean remotePoolTouched;

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
        lastPullTimeMs = 0;
        remotePoolTouched = false;
        localFilter = FTDataFilter.empty();
        remoteFilterState = RemoteFilterState.empty();

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
        return isFilteredBy(remoteFilterState.filter, "remote", dataType, measurement, uuid, tags, fields);
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
            if (md5.equals(remoteFilterState.id)) {
                LogUtils.d(TAG, "remote filter pull success, config unchanged");
                return;
            }

            RemoteFilterConfig remote = parseRemoteFilter(body);
            FTDataFilter compiledFilter = FTDataFilter.compile(remote.filters);
            boolean canDisableServerFilter = canDisableServerFilter(remote.filters);
            remoteFilterState = RemoteFilterState.synced(md5, compiledFilter, canDisableServerFilter);
            if (remote.pullIntervalSeconds > 0) {
                pullIntervalSeconds = remote.pullIntervalSeconds;
            }
            LogUtils.d(TAG, "remote data filters updated, categories:" + remote.filters.keySet()
                    + ", rules:" + countRules(remote.filters)
                    + ", disableServerFilter:" + canDisableServerFilter
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
        return enabled && remoteFilterState.canDisableServerFilter;
    }

    public UploadFilterResult prepareForUpload(DataType dataType, List<SyncData> dataList) {
        if (!shouldDisableServerFilter() || dataList == null || dataList.isEmpty()) {
            return UploadFilterResult.noBypass(dataList);
        }

        RemoteFilterState state = remoteFilterState;
        ArrayList<SyncData> uploadDataList = new ArrayList<>(dataList.size());
        ArrayList<SyncData> filteredDataList = new ArrayList<>();
        boolean allUploadDataChecked = true;

        for (SyncData data : dataList) {
            if (data == null) {
                allUploadDataChecked = false;
                continue;
            }

            ParsedLineProtocol parsed = parseLineProtocol(data.getDataString());
            if (parsed == null) {
                allUploadDataChecked = false;
                uploadDataList.add(data);
                continue;
            }

            if (state.filter.isFiltered(dataType, parsed.measurement, parsed.tags, parsed.fields)) {
                filteredDataList.add(data);
            } else {
                uploadDataList.add(data);
            }
        }

        return new UploadFilterResult(uploadDataList, filteredDataList,
                allUploadDataChecked && !uploadDataList.isEmpty());
    }

    private boolean canDisableServerFilter(Map<String, String[]> filters) {
        boolean hasSupportedRemoteRule = false;
        if (filters == null || filters.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String[]> entry : filters.entrySet()) {
            if (normalizeCategory(entry.getKey()) == null) {
                continue;
            }
            String[] rules = entry.getValue();
            if (rules == null) {
                continue;
            }
            for (String rule : rules) {
                if (rule == null || rule.trim().isEmpty()) {
                    continue;
                }
                if (FTFilterParser.parseConditions(rule).isEmpty()) {
                    return false;
                }
                hasSupportedRemoteRule = true;
            }
        }
        return hasSupportedRemoteRule;
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String normalized = category.trim().toLowerCase();
        if (FTDataFilter.CATEGORY_LOGGING.equals(normalized)
                || FTDataFilter.CATEGORY_RUM.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private ParsedLineProtocol parseLineProtocol(String dataString) {
        if (Utils.isNullOrEmpty(dataString)) {
            return null;
        }
        int lineEnd = dataString.indexOf('\n');
        String line = lineEnd >= 0 ? dataString.substring(0, lineEnd) : dataString;
        if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
        }

        int tagEnd = findUnescapedSpaceOutsideQuotes(line, 0);
        if (tagEnd <= 0 || tagEnd >= line.length() - 1) {
            return null;
        }
        int fieldEnd = findUnescapedSpaceOutsideQuotes(line, tagEnd + 1);
        if (fieldEnd <= tagEnd + 1) {
            return null;
        }

        String measurementAndTags = line.substring(0, tagEnd);
        String fieldPart = line.substring(tagEnd + 1, fieldEnd);
        int firstComma = findUnescapedChar(measurementAndTags, ',', 0);
        String measurement = firstComma < 0
                ? unescapeIdentifier(measurementAndTags)
                : unescapeIdentifier(measurementAndTags.substring(0, firstComma));
        if (Utils.isNullOrEmpty(measurement)) {
            return null;
        }

        HashMap<String, Object> tags = new HashMap<>();
        if (firstComma >= 0 && !parseKeyValues(measurementAndTags, firstComma + 1,
                false, tags)) {
            return null;
        }

        HashMap<String, Object> fields = new HashMap<>();
        if (!parseKeyValues(fieldPart, 0, true, fields) || fields.isEmpty()) {
            return null;
        }

        return new ParsedLineProtocol(measurement, tags, fields);
    }

    private boolean parseKeyValues(String input, int start, boolean fieldValues,
                                   HashMap<String, Object> values) {
        int partStart = start;
        while (partStart < input.length()) {
            int partEnd = findUnescapedCommaOutsideQuotes(input, partStart);
            if (partEnd < 0) {
                partEnd = input.length();
            }
            if (partEnd > partStart) {
                int equal = findUnescapedChar(input, '=', partStart);
                if (equal <= partStart || equal >= partEnd) {
                    return false;
                }
                String key = unescapeIdentifier(input.substring(partStart, equal));
                String rawValue = input.substring(equal + 1, partEnd);
                if (Utils.isNullOrEmpty(key)) {
                    return false;
                }
                Object value = fieldValues ? parseFieldValue(rawValue) : unescapeIdentifier(rawValue);
                values.put(key, value);
            }
            partStart = partEnd + 1;
        }
        return true;
    }

    private Object parseFieldValue(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        String value = rawValue.trim();
        if (value.length() >= 2 && value.charAt(0) == '"'
                && value.charAt(value.length() - 1) == '"') {
            return unescapeQuotedString(value.substring(1, value.length() - 1));
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        try {
            if (value.endsWith("i")) {
                return Long.parseLong(value.substring(0, value.length() - 1));
            }
            if (value.indexOf('.') >= 0 || value.indexOf('e') >= 0 || value.indexOf('E') >= 0) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return value;
        }
    }

    private int findUnescapedSpaceOutsideQuotes(String input, int start) {
        boolean escaped = false;
        boolean inString = false;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString && c == ' ') {
                return i;
            }
        }
        return -1;
    }

    private int findUnescapedCommaOutsideQuotes(String input, int start) {
        boolean escaped = false;
        boolean inString = false;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString && c == ',') {
                return i;
            }
        }
        return -1;
    }

    private int findUnescapedChar(String input, char target, int start) {
        boolean escaped = false;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == target) {
                return i;
            }
        }
        return -1;
    }

    private String unescapeIdentifier(String value) {
        if (value == null || value.indexOf('\\') < 0) {
            return value;
        }
        StringBuilder builder = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                builder.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                builder.append(c);
            }
        }
        if (escaped) {
            builder.append('\\');
        }
        return builder.toString();
    }

    private String unescapeQuotedString(String value) {
        if (value == null || value.indexOf('\\') < 0) {
            return value;
        }
        StringBuilder builder = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                builder.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                builder.append(c);
            }
        }
        if (escaped) {
            builder.append('\\');
        }
        return builder.toString();
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

    private static class RemoteFilterState {
        final String id;
        final FTDataFilter filter;
        final boolean canDisableServerFilter;

        RemoteFilterState(String id, FTDataFilter filter, boolean canDisableServerFilter) {
            this.id = id;
            this.filter = filter;
            this.canDisableServerFilter = canDisableServerFilter;
        }

        static RemoteFilterState empty() {
            return new RemoteFilterState(null, FTDataFilter.empty(), false);
        }

        static RemoteFilterState synced(String id, FTDataFilter filter, boolean canDisableServerFilter) {
            return new RemoteFilterState(id, filter == null ? FTDataFilter.empty() : filter,
                    canDisableServerFilter);
        }
    }

    private static class ParsedLineProtocol {
        final String measurement;
        final HashMap<String, Object> tags;
        final HashMap<String, Object> fields;

        ParsedLineProtocol(String measurement, HashMap<String, Object> tags,
                           HashMap<String, Object> fields) {
            this.measurement = measurement;
            this.tags = tags;
            this.fields = fields;
        }
    }

    public static class UploadFilterResult {
        private final List<SyncData> uploadDataList;
        private final List<SyncData> filteredDataList;
        private final boolean disableServerFilter;

        UploadFilterResult(List<SyncData> uploadDataList, List<SyncData> filteredDataList,
                           boolean disableServerFilter) {
            this.uploadDataList = uploadDataList;
            this.filteredDataList = filteredDataList;
            this.disableServerFilter = disableServerFilter;
        }

        static UploadFilterResult noBypass(List<SyncData> dataList) {
            List<SyncData> uploadDataList = dataList == null
                    ? new ArrayList<SyncData>() : dataList;
            return new UploadFilterResult(uploadDataList, new ArrayList<SyncData>(), false);
        }

        public List<SyncData> getUploadDataList() {
            return uploadDataList;
        }

        public List<SyncData> getFilteredDataList() {
            return filteredDataList;
        }

        public boolean isDisableServerFilter() {
            return disableServerFilter;
        }
    }
}
