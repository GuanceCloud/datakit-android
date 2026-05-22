package com.ft.sdk.garble.db.file;

import android.content.Context;
import android.util.Base64;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTSQL;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * File-backed RUM view/action aggregate storage.
 */
public class FTRumFileAggregateStore {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTRumFileAggregateStore";
    private static final String FILE_SUFFIX = ".json";

    private final FTFileStorePaths paths;
    private final FTFileLock lock;

    public FTRumFileAggregateStore(Context context) {
        this(new FTFileStorePaths(context));
    }

    public FTRumFileAggregateStore(FTFileStorePaths paths) {
        this.paths = paths;
        this.lock = new FTFileLock(paths.getLockFile());
    }

    public void initSumView(final ViewBean data) {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    paths.ensureReady();
                    writeJson(getViewFile(data.getId()), viewToJson(data));
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void initSumAction(final ActionBean data) {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    paths.ensureReady();
                    writeJson(getActionFile(data.getId()), actionToJson(data));
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void closeAction(final String actionId, final long duration, final boolean force) {
        if (actionId == null) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    JSONObject json = readJson(getActionFile(actionId));
                    if (json == null) return null;
                    if (force || json.optInt(FTSQL.RUM_COLUMN_PENDING_RESOURCE) <= 0) {
                        json.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                        json.put(FTSQL.RUM_COLUMN_ACTION_DURATION, duration);
                        writeJson(getActionFile(actionId), json);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void closeView(final String viewId, final long loadTime, final long timeSpent, final String attr) {
        if (viewId == null) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    JSONObject json = readJson(getViewFile(viewId));
                    if (json == null) return null;
                    json.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                    json.put(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT, timeSpent);
                    json.put(FTSQL.RUM_COLUMN_VIEW_LOAD_TIME, loadTime);
                    json.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, attr);
                    writeJson(getViewFile(viewId), json);
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void increaseActionError(String actionId) {
        updateActionCount(actionId, FTSQL.RUM_COLUMN_ERROR_COUNT, 1);
    }

    public void increaseActionResource(String actionId) {
        updateActionCount(actionId, FTSQL.RUM_COLUMN_RESOURCE_COUNT, 1);
    }

    public void increaseActionLongTask(String actionId) {
        updateActionCount(actionId, FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 1);
    }

    public void increaseViewAction(String viewId) {
        updateViewCount(viewId, FTSQL.RUM_COLUMN_ACTION_COUNT, 1);
    }

    public void increaseViewResource(String viewId) {
        updateViewCount(viewId, FTSQL.RUM_COLUMN_RESOURCE_COUNT, 1);
    }

    public void increaseViewPendingResource(String viewId) {
        updateViewCount(viewId, FTSQL.RUM_COLUMN_PENDING_RESOURCE, 1);
    }

    public void increaseActionPendingResource(String actionId) {
        updateActionCount(actionId, FTSQL.RUM_COLUMN_PENDING_RESOURCE, 1);
    }

    public void reduceViewPendingResource(String viewId) {
        updateViewCount(viewId, FTSQL.RUM_COLUMN_PENDING_RESOURCE, -1);
    }

    public void reduceActionPendingResource(String actionId) {
        updateActionCount(actionId, FTSQL.RUM_COLUMN_PENDING_RESOURCE, -1);
    }

    public void increaseViewError(String viewId) {
        updateViewCount(viewId, FTSQL.RUM_COLUMN_ERROR_COUNT, 1);
    }

    public void increaseViewLongTask(String viewId) {
        updateViewCount(viewId, FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 1);
    }

    public void updateViewUploadTime(String viewId, long dateTime) {
        updateViewDateTimeByColumn(viewId, FTSQL.RUM_DATA_UPLOAD_TIME, dateTime);
    }

    public void updateViewUpdateTime(String viewId, long dateTime) {
        updateViewDateTimeByColumn(viewId, FTSQL.RUM_DATA_UPDATE_TIME, dateTime);
    }

    public void updateViewExtraAttr(final String viewId, final String attr) {
        if (viewId == null) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    JSONObject json = readJson(getViewFile(viewId));
                    if (json == null) return null;
                    json.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, attr);
                    writeJson(getViewFile(viewId), json);
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public ArrayList<ActionBean> querySumAction(final int limit) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<ArrayList<ActionBean>>() {
                @Override
                public ArrayList<ActionBean> run() throws IOException {
                    paths.ensureReady();
                    ArrayList<ActionRecord> records = readActionRecords();
                    Collections.sort(records, new Comparator<ActionRecord>() {
                        @Override
                        public int compare(ActionRecord o1, ActionRecord o2) {
                            return Long.compare(o1.startTime, o2.startTime);
                        }
                    });
                    ArrayList<ActionBean> result = new ArrayList<>();
                    for (ActionRecord record : records) {
                        if (record.isClose) {
                            result.add(record.bean);
                            if (limit > 0 && result.size() >= limit) {
                                break;
                            }
                        }
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            return new ArrayList<>();
        }
    }

    public ArrayList<ViewBean> querySumView(int limit) {
        return querySumView(limit, false);
    }

    public ArrayList<ViewBean> querySumView(final int limit, final boolean allData) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<ArrayList<ViewBean>>() {
                @Override
                public ArrayList<ViewBean> run() throws IOException {
                    paths.ensureReady();
                    ArrayList<ViewRecord> records = readViewRecords();
                    Collections.sort(records, new Comparator<ViewRecord>() {
                        @Override
                        public int compare(ViewRecord o1, ViewRecord o2) {
                            return Long.compare(o1.startTime, o2.startTime);
                        }
                    });
                    ArrayList<ViewBean> result = new ArrayList<>();
                    for (ViewRecord record : records) {
                        if (allData || shouldReturnView(record)) {
                            result.add(record.bean);
                            if (limit > 0 && result.size() >= limit) {
                                break;
                            }
                        }
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            return new ArrayList<>();
        }
    }

    public void cleanCloseActionData(final String[] ids) {
        if (ids == null || ids.length == 0) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() {
                    for (String id : ids) {
                        if (id != null) {
                            deleteFile(getActionFile(id));
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void cleanCloseViewData() {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    ArrayList<ViewRecord> records = readViewRecords();
                    for (ViewRecord record : records) {
                        if (record.isClose && record.pendingResourceCount <= 0) {
                            deleteFile(record.file);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void closeAllActionAndView() {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    long now = System.currentTimeMillis();
                    ArrayList<ViewRecord> viewRecords = readViewRecords();
                    for (ViewRecord record : viewRecords) {
                        record.json.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                        record.json.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
                        record.json.put(FTSQL.RUM_DATA_UPDATE_TIME, now);
                        writeJson(record.file, record.json);
                    }
                    ArrayList<ActionRecord> actionRecords = readActionRecords();
                    for (ActionRecord record : actionRecords) {
                        record.json.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                        record.json.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
                        writeJson(record.file, record.json);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void deleteAll() {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() {
                    deleteDirectoryFiles(paths.getRumViewDir());
                    deleteDirectoryFiles(paths.getRumActionDir());
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public long size() {
        return directorySize(paths.getRumViewDir()) + directorySize(paths.getRumActionDir());
    }

    private void updateActionCount(final String actionId, final String columnName, final int delta) {
        if (actionId == null) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    JSONObject json = readJson(getActionFile(actionId));
                    if (json == null) return null;
                    json.put(columnName, json.optInt(columnName) + delta);
                    writeJson(getActionFile(actionId), json);
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    private void updateViewCount(final String viewId, final String columnName, final int delta) {
        if (viewId == null) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    JSONObject json = readJson(getViewFile(viewId));
                    if (json == null) return null;
                    json.put(columnName, json.optInt(columnName) + delta);
                    writeJson(getViewFile(viewId), json);
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    private void updateViewDateTimeByColumn(final String viewId, final String columnName, final long dateTime) {
        if (viewId == null) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    JSONObject json = readJson(getViewFile(viewId));
                    if (json == null) return null;
                    json.put(columnName, dateTime);
                    json.put(FTSQL.RUM_VIEW_UPDATE_TIME, json.optLong(FTSQL.RUM_VIEW_UPDATE_TIME) + 1);
                    writeJson(getViewFile(viewId), json);
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    private boolean shouldReturnView(ViewRecord record) {
        return !record.isClose || record.uploadTime < record.updateTime || record.uploadTime == 0;
    }

    private JSONObject actionToJson(ActionBean data) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(FTSQL.RUM_COLUMN_START_TIME, data.getStartTime());
        json.put(FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_ID, data.getId());
        json.put(FTSQL.RUM_COLUMN_IS_CLOSE, data.isClose() ? 1 : 0);
        json.put(FTSQL.RUM_COLUMN_SESSION_ID, data.getSessionId());
        json.put(FTSQL.RUM_COLUMN_VIEW_ID, data.getViewId());
        json.put(FTSQL.RUM_COLUMN_VIEW_NAME, data.getViewName());
        json.put(FTSQL.RUM_COLUMN_VIEW_REFERRER, data.getViewReferrer());
        json.put(FTSQL.RUM_COLUMN_RESOURCE_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
        json.put(FTSQL.RUM_COLUMN_ACTION_DURATION, data.getDuration());
        json.put(FTSQL.RUM_COLUMN_ACTION_NAME, data.getActionName());
        json.put(FTSQL.RUM_COLUMN_ACTION_TYPE, data.getActionType());
        json.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, data.getAttrJsonString());
        return json;
    }

    private JSONObject viewToJson(ViewBean data) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(FTSQL.RUM_COLUMN_START_TIME, data.getStartTime());
        json.put(FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_ID, data.getId());
        json.put(FTSQL.RUM_COLUMN_IS_CLOSE, 0);
        json.put(FTSQL.RUM_COLUMN_ACTION_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_RESOURCE_COUNT, 0);
        json.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
        json.put(FTSQL.RUM_COLUMN_VIEW_NAME, data.getViewName());
        json.put(FTSQL.RUM_COLUMN_VIEW_REFERRER, data.getViewReferrer());
        json.put(FTSQL.RUM_COLUMN_VIEW_LOAD_TIME, data.getLoadTime());
        json.put(FTSQL.RUM_COLUMN_SESSION_ID, data.getSessionId());
        json.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, data.getAttrJsonString());
        json.put(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT, data.getTimeSpent());
        json.put(FTSQL.RUM_DATA_UPDATE_TIME, 0);
        json.put(FTSQL.RUM_DATA_UPLOAD_TIME, 0);
        json.put(FTSQL.RUM_VIEW_UPDATE_TIME, 1);
        return json;
    }

    private ActionRecord toActionRecord(File file, JSONObject json) {
        ActionBean bean = new ActionBean();
        bean.setClose(json.optInt(FTSQL.RUM_COLUMN_IS_CLOSE) == 1);
        bean.setSessionId(json.optString(FTSQL.RUM_COLUMN_SESSION_ID, null));
        bean.setId(json.optString(FTSQL.RUM_COLUMN_ID, null));
        bean.setActionName(json.optString(FTSQL.RUM_COLUMN_ACTION_NAME, null));
        bean.setViewId(json.optString(FTSQL.RUM_COLUMN_VIEW_ID, null));
        bean.setViewName(json.optString(FTSQL.RUM_COLUMN_VIEW_NAME, null));
        bean.setViewReferrer(json.optString(FTSQL.RUM_COLUMN_VIEW_REFERRER, null));
        bean.setActionType(json.optString(FTSQL.RUM_COLUMN_ACTION_TYPE, null));
        bean.setLongTaskCount(json.optInt(FTSQL.RUM_COLUMN_LONG_TASK_COUNT));
        bean.setErrorCount(json.optInt(FTSQL.RUM_COLUMN_ERROR_COUNT));
        bean.setResourceCount(json.optInt(FTSQL.RUM_COLUMN_RESOURCE_COUNT));
        bean.setDuration(json.optLong(FTSQL.RUM_COLUMN_ACTION_DURATION));
        bean.setStartTime(json.optLong(FTSQL.RUM_COLUMN_START_TIME));
        bean.setFromAttrJsonString(json.optString(FTSQL.RUM_COLUMN_EXTRA_ATTR, null));
        return new ActionRecord(file, json, bean);
    }

    private ViewRecord toViewRecord(File file, JSONObject json) {
        ViewBean bean = new ViewBean();
        bean.setClose(json.optInt(FTSQL.RUM_COLUMN_IS_CLOSE) == 1);
        bean.setId(json.optString(FTSQL.RUM_COLUMN_ID, null));
        bean.setActionCount(json.optInt(FTSQL.RUM_COLUMN_ACTION_COUNT));
        bean.setTimeSpent(json.optLong(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT));
        bean.setLoadTime(json.optLong(FTSQL.RUM_COLUMN_VIEW_LOAD_TIME));
        bean.setStartTime(json.optLong(FTSQL.RUM_COLUMN_START_TIME));
        bean.setResourceCount(json.optInt(FTSQL.RUM_COLUMN_RESOURCE_COUNT));
        bean.setErrorCount(json.optInt(FTSQL.RUM_COLUMN_ERROR_COUNT));
        bean.setLongTaskCount(json.optInt(FTSQL.RUM_COLUMN_LONG_TASK_COUNT));
        bean.setSessionId(json.optString(FTSQL.RUM_COLUMN_SESSION_ID, null));
        bean.setViewName(json.optString(FTSQL.RUM_COLUMN_VIEW_NAME, null));
        bean.setViewReferrer(json.optString(FTSQL.RUM_COLUMN_VIEW_REFERRER, null));
        bean.setFromAttrJsonString(json.optString(FTSQL.RUM_COLUMN_EXTRA_ATTR, null));
        bean.setViewUpdateTime(json.optLong(FTSQL.RUM_VIEW_UPDATE_TIME));
        return new ViewRecord(file, json, bean);
    }

    private ArrayList<ActionRecord> readActionRecords() throws IOException {
        return readRecords(paths.getRumActionDir(), true);
    }

    private ArrayList<ViewRecord> readViewRecords() throws IOException {
        return readRecords(paths.getRumViewDir(), false);
    }

    @SuppressWarnings("unchecked")
    private <T> ArrayList<T> readRecords(File dir, boolean action) throws IOException {
        paths.ensureReady();
        ArrayList<T> records = new ArrayList<>();
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(FILE_SUFFIX);
            }
        });
        if (files == null) {
            return records;
        }
        for (File file : files) {
            JSONObject json = readJson(file);
            if (json == null) continue;
            if (action) {
                records.add((T) toActionRecord(file, json));
            } else {
                records.add((T) toViewRecord(file, json));
            }
        }
        return records;
    }

    private JSONObject readJson(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try {
            return new JSONObject(FTAtomicFileHelper.readUtf8(file));
        } catch (JSONException e) {
            LogUtils.e(TAG, "Failed to parse aggregate file: " + file.getAbsolutePath());
            return null;
        }
    }

    private void writeJson(File file, JSONObject json) throws IOException {
        FTAtomicFileHelper.writeUtf8(file, json.toString());
    }

    private File getViewFile(String id) {
        return new File(paths.getRumViewDir(), getFileName(id));
    }

    private File getActionFile(String id) {
        return new File(paths.getRumActionDir(), getFileName(id));
    }

    private String getFileName(String id) {
        String value = id == null ? "" : id;
        String encoded = Base64.encodeToString(value.getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        return encoded + FILE_SUFFIX;
    }

    private void deleteFile(File file) {
        if (file.exists() && !file.delete()) {
            LogUtils.e(TAG, "Failed to delete aggregate file: " + file.getAbsolutePath());
        }
    }

    private void deleteDirectoryFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            deleteFile(file);
        }
    }

    private long directorySize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        if (dir.isFile()) {
            return dir.length();
        }
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += directorySize(file);
            }
        }
        return size;
    }

    private static class ActionRecord {
        final File file;
        final JSONObject json;
        final ActionBean bean;
        final boolean isClose;
        final long startTime;

        ActionRecord(File file, JSONObject json, ActionBean bean) {
            this.file = file;
            this.json = json;
            this.bean = bean;
            this.isClose = bean.isClose();
            this.startTime = bean.getStartTime();
        }
    }

    private static class ViewRecord {
        final File file;
        final JSONObject json;
        final ViewBean bean;
        final boolean isClose;
        final long startTime;
        final long uploadTime;
        final long updateTime;
        final int pendingResourceCount;

        ViewRecord(File file, JSONObject json, ViewBean bean) {
            this.file = file;
            this.json = json;
            this.bean = bean;
            this.isClose = bean.isClose();
            this.startTime = bean.getStartTime();
            this.uploadTime = json.optLong(FTSQL.RUM_DATA_UPLOAD_TIME);
            this.updateTime = json.optLong(FTSQL.RUM_DATA_UPDATE_TIME);
            this.pendingResourceCount = json.optInt(FTSQL.RUM_COLUMN_PENDING_RESOURCE);
        }
    }
}
