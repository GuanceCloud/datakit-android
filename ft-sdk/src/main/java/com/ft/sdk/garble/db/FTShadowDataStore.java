package com.ft.sdk.garble.db;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.file.FTFileDataStore;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads from the primary store and mirrors writes to a shadow store.
 */
class FTShadowDataStore implements FTDataStore {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTShadowDataStore";

    private final FTDataStore primary;
    private final FTDataStore shadow;

    FTShadowDataStore(FTDataStore primary, FTDataStore shadow) {
        this.primary = primary;
        this.shadow = shadow;
    }

    @Override
    public boolean insertFtOperation(SyncData data, boolean reInsert) {
        boolean result = primary.insertFtOperation(data, reInsert);
        resyncShadowSyncQueue();
        return result;
    }

    @Override
    public void initSumView(ViewBean data) {
        primary.initSumView(data);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.initSumView(data);
            }
        });
    }

    @Override
    public void initSumAction(ActionBean data) {
        primary.initSumAction(data);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.initSumAction(data);
            }
        });
    }

    @Override
    public void closeAction(String actionId, long duration, boolean force) {
        primary.closeAction(actionId, duration, force);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.closeAction(actionId, duration, force);
            }
        });
    }

    @Override
    public void closeView(String viewId, long loadTime, long timeSpent, String attr) {
        primary.closeView(viewId, loadTime, timeSpent, attr);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.closeView(viewId, loadTime, timeSpent, attr);
            }
        });
    }

    @Override
    public void increaseActionError(String actionId) {
        primary.increaseActionError(actionId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseActionError(actionId);
            }
        });
    }

    @Override
    public void increaseActionResource(String actionId) {
        primary.increaseActionResource(actionId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseActionResource(actionId);
            }
        });
    }

    @Override
    public void increaseActionLongTask(String actionId) {
        primary.increaseActionLongTask(actionId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseActionLongTask(actionId);
            }
        });
    }

    @Override
    public void increaseViewAction(String viewId) {
        primary.increaseViewAction(viewId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseViewAction(viewId);
            }
        });
    }

    @Override
    public void increaseViewResource(String viewId) {
        primary.increaseViewResource(viewId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseViewResource(viewId);
            }
        });
    }

    @Override
    public void increaseViewPendingResource(String viewId) {
        primary.increaseViewPendingResource(viewId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseViewPendingResource(viewId);
            }
        });
    }

    @Override
    public void increaseActionPendingResource(String actionId) {
        primary.increaseActionPendingResource(actionId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseActionPendingResource(actionId);
            }
        });
    }

    @Override
    public void reduceViewPendingResource(String viewId) {
        primary.reduceViewPendingResource(viewId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.reduceViewPendingResource(viewId);
            }
        });
    }

    @Override
    public void reduceActionPendingResource(String actionId) {
        primary.reduceActionPendingResource(actionId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.reduceActionPendingResource(actionId);
            }
        });
    }

    @Override
    public void increaseViewError(String viewId) {
        primary.increaseViewError(viewId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseViewError(viewId);
            }
        });
    }

    @Override
    public void increaseViewLongTask(String viewId) {
        primary.increaseViewLongTask(viewId);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.increaseViewLongTask(viewId);
            }
        });
    }

    @Override
    public void updateViewUploadTime(String viewId, long dateTime) {
        primary.updateViewUploadTime(viewId, dateTime);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.updateViewUploadTime(viewId, dateTime);
            }
        });
    }

    @Override
    public void updateViewUpdateTime(String viewId, long dateTime) {
        primary.updateViewUpdateTime(viewId, dateTime);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.updateViewUpdateTime(viewId, dateTime);
            }
        });
    }

    @Override
    public void updateViewExtraAttr(String viewId, String attr) {
        primary.updateViewExtraAttr(viewId, attr);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.updateViewExtraAttr(viewId, attr);
            }
        });
    }

    @Override
    public ArrayList<ActionBean> querySumAction(int limit) {
        return primary.querySumAction(limit);
    }

    @Override
    public ArrayList<ViewBean> querySumView(int limit) {
        return primary.querySumView(limit);
    }

    @Override
    public ArrayList<ViewBean> querySumView(int limit, boolean allData) {
        return primary.querySumView(limit, allData);
    }

    @Override
    public void cleanCloseActionData(String[] ids) {
        primary.cleanCloseActionData(ids);
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.cleanCloseActionData(ids);
            }
        });
    }

    @Override
    public void cleanCloseViewData() {
        primary.cleanCloseViewData();
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.cleanCloseViewData();
            }
        });
    }

    @Override
    public void closeAllActionAndView() {
        primary.closeAllActionAndView();
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.closeAllActionAndView();
            }
        });
    }

    @Override
    public boolean updateOrInsertSyncData(@NonNull SyncData data) {
        boolean result = primary.updateOrInsertSyncData(data);
        if (result) {
            resyncShadowSyncQueue();
        }
        return result;
    }

    @Override
    public boolean insertFtOptList(@NonNull List<SyncData> dataList, boolean reInsert) {
        boolean result = primary.insertFtOptList(dataList, reInsert);
        resyncShadowSyncQueue();
        return result;
    }

    @Override
    public List<SyncData> queryDataByDataByTypeLimit(int limit, DataType dataType) {
        return primary.queryDataByDataByTypeLimit(limit, dataType);
    }

    @Override
    public List<SyncData> queryDataByDataByTypeLimitDesc(int limit, DataType dataType) {
        return primary.queryDataByDataByTypeLimitDesc(limit, dataType);
    }

    @Override
    public List<SyncData> queryDataByDescLimit(int limit) {
        return primary.queryDataByDescLimit(limit);
    }

    @Override
    public List<SyncData> queryDataByDescLimit(int limit, boolean oldCache) {
        return primary.queryDataByDescLimit(limit, oldCache);
    }

    @Override
    public int updateDataType(DataType dataType, long errorDateline) {
        int result = primary.updateDataType(dataType, errorDateline);
        resyncShadowSyncQueue();
        return result;
    }

    @Override
    public int deleteExpireCache(DataType dataType, long now, long expireDuration) {
        int result = primary.deleteExpireCache(dataType, now, expireDuration);
        resyncShadowSyncQueue();
        return result;
    }

    @Override
    public int queryTotalCount(DataType dataType) {
        return primary.queryTotalCount(dataType);
    }

    @Override
    public int queryTotalCount(DataType[] list) {
        return primary.queryTotalCount(list);
    }

    @Override
    public void deleteOldestData(DataType type, int limit) {
        primary.deleteOldestData(type, limit);
        resyncShadowSyncQueue();
    }

    @Override
    public void deleteOldestData(DataType[] list, int limit) {
        primary.deleteOldestData(list, limit);
        resyncShadowSyncQueue();
    }

    @Override
    public boolean isOldCacheExist() {
        return primary.isOldCacheExist();
    }

    @Override
    public void delete(List<Long> ids, boolean oldCache) {
        primary.delete(ids, oldCache);
        if (oldCache) {
            mirror(new Runnable() {
                @Override
                public void run() {
                    shadow.delete(ids, true);
                }
            });
        } else {
            resyncShadowSyncQueue();
        }
    }

    @Override
    public void delete() {
        primary.delete();
        mirror(new Runnable() {
            @Override
            public void run() {
                shadow.delete();
            }
        });
    }

    private void mirror(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            LogUtils.e(TAG, "Shadow store operation failed: " + e.getMessage());
        }
    }

    private void resyncShadowSyncQueue() {
        if (!(shadow instanceof FTFileDataStore)) {
            return;
        }
        mirror(new Runnable() {
            @Override
            public void run() {
                ((FTFileDataStore) shadow).replaceSyncDataFrom(primary.queryDataByDescLimit(0));
            }
        });
    }
}
