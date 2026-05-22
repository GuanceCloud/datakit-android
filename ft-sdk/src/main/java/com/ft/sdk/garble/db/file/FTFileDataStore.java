package com.ft.sdk.garble.db.file;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ft.sdk.CacheDiscard;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.db.FTDataStore;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File-backed SDK data store.
 */
public class FTFileDataStore implements FTDataStore {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTFileDataStore";
    private static final String DB_FLAT_MIGRATION_MARKER = "db_flat_migrated";

    private final FTFileStorePaths paths;
    private final FTSyncFileDataStore syncStore;
    private final FTRumFileAggregateStore rumStore;
    private boolean trimmingSizeLimit;

    public FTFileDataStore(Context context) {
        this(context, false);
    }

    public FTFileDataStore(Context context, boolean migrateDbFlatCache) {
        this(new FTFileStorePaths(context), migrateDbFlatCache);
    }

    public FTFileDataStore(FTFileStorePaths paths) {
        this(paths, false);
    }

    public FTFileDataStore(FTFileStorePaths paths, boolean migrateDbFlatCache) {
        this.paths = paths;
        this.syncStore = new FTSyncFileDataStore(paths);
        this.rumStore = new FTRumFileAggregateStore(paths);
        if (migrateDbFlatCache) {
            migrateCurrentDbCacheIfNeeded();
        }
    }

    @Override
    public boolean insertFtOperation(SyncData data, boolean reInsert) {
        boolean result = syncStore.insertFtOperation(data, reInsert);
        updateFileSizeCache();
        return result;
    }

    @Override
    public void initSumView(ViewBean data) {
        rumStore.initSumView(data);
        updateFileSizeCache();
    }

    @Override
    public void initSumAction(ActionBean data) {
        rumStore.initSumAction(data);
        updateFileSizeCache();
    }

    @Override
    public void closeAction(String actionId, long duration, boolean force) {
        rumStore.closeAction(actionId, duration, force);
        updateFileSizeCache();
    }

    @Override
    public void closeView(String viewId, long loadTime, long timeSpent, String attr) {
        rumStore.closeView(viewId, loadTime, timeSpent, attr);
        updateFileSizeCache();
    }

    @Override
    public void increaseActionError(String actionId) {
        rumStore.increaseActionError(actionId);
        updateFileSizeCache();
    }

    @Override
    public void increaseActionResource(String actionId) {
        rumStore.increaseActionResource(actionId);
        updateFileSizeCache();
    }

    @Override
    public void increaseActionLongTask(String actionId) {
        rumStore.increaseActionLongTask(actionId);
        updateFileSizeCache();
    }

    @Override
    public void increaseViewAction(String viewId) {
        rumStore.increaseViewAction(viewId);
        updateFileSizeCache();
    }

    @Override
    public void increaseViewResource(String viewId) {
        rumStore.increaseViewResource(viewId);
        updateFileSizeCache();
    }

    @Override
    public void increaseViewPendingResource(String viewId) {
        rumStore.increaseViewPendingResource(viewId);
        updateFileSizeCache();
    }

    @Override
    public void increaseActionPendingResource(String actionId) {
        rumStore.increaseActionPendingResource(actionId);
        updateFileSizeCache();
    }

    @Override
    public void reduceViewPendingResource(String viewId) {
        rumStore.reduceViewPendingResource(viewId);
        updateFileSizeCache();
    }

    @Override
    public void reduceActionPendingResource(String actionId) {
        rumStore.reduceActionPendingResource(actionId);
        updateFileSizeCache();
    }

    @Override
    public void increaseViewError(String viewId) {
        rumStore.increaseViewError(viewId);
        updateFileSizeCache();
    }

    @Override
    public void increaseViewLongTask(String viewId) {
        rumStore.increaseViewLongTask(viewId);
        updateFileSizeCache();
    }

    @Override
    public void updateViewUploadTime(String viewId, long dateTime) {
        rumStore.updateViewUploadTime(viewId, dateTime);
        updateFileSizeCache();
    }

    @Override
    public void updateViewUpdateTime(String viewId, long dateTime) {
        rumStore.updateViewUpdateTime(viewId, dateTime);
        updateFileSizeCache();
    }

    @Override
    public void updateViewExtraAttr(String viewId, String attr) {
        rumStore.updateViewExtraAttr(viewId, attr);
        updateFileSizeCache();
    }

    @Override
    public ArrayList<ActionBean> querySumAction(int limit) {
        return rumStore.querySumAction(limit);
    }

    @Override
    public ArrayList<ViewBean> querySumView(int limit) {
        return rumStore.querySumView(limit);
    }

    @Override
    public ArrayList<ViewBean> querySumView(int limit, boolean allData) {
        return rumStore.querySumView(limit, allData);
    }

    @Override
    public void cleanCloseActionData(String[] ids) {
        rumStore.cleanCloseActionData(ids);
        updateFileSizeCache();
    }

    @Override
    public void cleanCloseViewData() {
        rumStore.cleanCloseViewData();
        updateFileSizeCache();
    }

    @Override
    public void closeAllActionAndView() {
        rumStore.closeAllActionAndView();
        updateFileSizeCache();
    }

    @Override
    public boolean updateOrInsertSyncData(@NonNull SyncData data) {
        boolean result = syncStore.updateOrInsertSyncData(data);
        updateFileSizeCache();
        return result;
    }

    @Override
    public boolean insertFtOptList(@NonNull List<SyncData> dataList, boolean reInsert) {
        boolean result = syncStore.insertFtOptList(dataList, reInsert);
        updateFileSizeCache();
        return result;
    }

    @Override
    public List<SyncData> queryDataByDataByTypeLimit(int limit, DataType dataType) {
        return syncStore.queryDataByDataByTypeLimit(limit, dataType);
    }

    @Override
    public List<SyncData> queryDataByDataByTypeLimitDesc(int limit, DataType dataType) {
        return syncStore.queryDataByDataByTypeLimitDesc(limit, dataType);
    }

    @Override
    public List<SyncData> queryDataByDescLimit(int limit) {
        return syncStore.queryDataByDescLimit(limit);
    }

    @Override
    public List<SyncData> queryDataByDescLimit(int limit, boolean oldCache) {
        if (oldCache) {
            return FTDBManager.get().queryDataByDescLimit(limit, true);
        }
        return syncStore.queryDataByDescLimit(limit);
    }

    @Override
    public int updateDataType(DataType dataType, long errorDateline) {
        int count = syncStore.updateDataType(dataType, errorDateline);
        updateFileSizeCache();
        return count;
    }

    @Override
    public int deleteExpireCache(DataType dataType, long now, long expireDuration) {
        int count = syncStore.deleteExpireCache(dataType, now, expireDuration);
        updateFileSizeCache();
        return count;
    }

    @Override
    public int queryTotalCount(DataType dataType) {
        return syncStore.queryTotalCount(dataType);
    }

    @Override
    public int queryTotalCount(DataType[] list) {
        return syncStore.queryTotalCount(list);
    }

    @Override
    public void deleteOldestData(DataType type, int limit) {
        syncStore.deleteOldestData(type, limit);
        updateFileSizeCache();
    }

    @Override
    public void deleteOldestData(DataType[] list, int limit) {
        syncStore.deleteOldestData(list, limit);
        updateFileSizeCache();
    }

    @Override
    public boolean isOldCacheExist() {
        return FTDBManager.get().isOldCacheExist();
    }

    @Override
    public void delete(List<Long> ids, boolean oldCache) {
        if (oldCache) {
            FTDBManager.get().delete(ids, true);
        } else {
            syncStore.delete(ids);
        }
        updateFileSizeCache();
    }

    @Override
    public void delete() {
        syncStore.deleteAll();
        rumStore.deleteAll();
        updateFileSizeCache();
    }

    public void replaceSyncDataFrom(List<SyncData> list) {
        syncStore.replaceAll(list);
        updateFileSizeCache();
    }

    public void refreshFileSizeCache() {
        updateFileSizeCache();
    }

    private void migrateCurrentDbCacheIfNeeded() {
        try {
            paths.ensureReady();
            File marker = getDbFlatMigrationMarker();
            List<SyncData> dbData = FTDBManager.get().queryDataByDescLimit(0, false);
            if (dbData.isEmpty()) {
                LogUtils.d(TAG, "migrateCurrentDbCacheIfNeeded skipped: DB flat cache is empty");
                return;
            }
            String migrationState = buildDbFlatMigrationState(dbData);
            if (marker.exists() && migrationState.equals(FTAtomicFileHelper.readUtf8(marker))) {
                return;
            }
            if (syncStore.isEmpty()) {
                syncStore.insertMigratedData(dbData);
            } else {
                syncStore.appendMissingMigratedData(dbData);
            }
            FTAtomicFileHelper.writeUtf8(marker, migrationState);
        } catch (Exception e) {
            LogUtils.e(TAG, "migrateCurrentDbCacheIfNeeded failed: " + e.getMessage());
        }
    }

    private String buildDbFlatMigrationState(List<SyncData> list) {
        long maxId = 0;
        long hash = 1125899906842597L;
        for (SyncData data : list) {
            maxId = Math.max(maxId, data.getId());
            hash = 31 * hash + data.getId();
            hash = 31 * hash + data.getTime();
            hash = 31 * hash + stringHash(data.getUuid());
            hash = 31 * hash + data.getDataType().getValue().hashCode();
            hash = 31 * hash + stringHash(data.getDataString());
        }
        return list.size() + ":" + maxId + ":" + hash;
    }

    private int stringHash(String value) {
        return value == null ? 0 : value.hashCode();
    }

    private File getDbFlatMigrationMarker() {
        return new File(paths.getRootDir(), DB_FLAT_MIGRATION_MARKER);
    }

    private void updateFileSizeCache() {
        FTDBCachePolicy policy = FTDBCachePolicy.get();
        policy.setCurrentCacheSize(currentStoreSize());
        trimOldestSyncDataIfNeeded(policy);
    }

    private void trimOldestSyncDataIfNeeded(FTDBCachePolicy policy) {
        if (trimmingSizeLimit
                || !policy.isLimitWithCacheSize()
                || policy.getCacheDiscard() != CacheDiscard.DISCARD_OLDEST
                || !policy.isReachCacheLimit()) {
            return;
        }

        trimmingSizeLimit = true;
        try {
            while (policy.isReachCacheLimit() && !syncStore.isEmpty()) {
                if (syncStore.queryTotalCount(DataType.LOG) > 0) {
                    syncStore.deleteOldestData(DataType.LOG, Constants.CACHE_OLD_DATA_REMOVE_COUNT);
                } else {
                    syncStore.deleteOldestData(Constants.CACHE_OLD_DATA_REMOVE_COUNT);
                }
                policy.setCurrentCacheSize(currentStoreSize());
            }
        } finally {
            trimmingSizeLimit = false;
        }
    }

    private long currentStoreSize() {
        return syncStore.size() + rumStore.size();
    }
}
