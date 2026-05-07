package com.ft.sdk.garble.db;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;

import java.util.ArrayList;
import java.util.List;

/**
 * SDK persistence contract for sync data and RUM aggregate state.
 */
public interface FTDataStore {

    boolean insertFtOperation(SyncData data, boolean reInsert);

    void initSumView(ViewBean data);

    void initSumAction(ActionBean data);

    void closeAction(String actionId, long duration, boolean force);

    void closeView(String viewId, long loadTime, long timeSpent, String attr);

    void increaseActionError(String actionId);

    void increaseActionResource(String actionId);

    void increaseActionLongTask(String actionId);

    void increaseViewAction(String viewId);

    void increaseViewResource(String viewId);

    void increaseViewPendingResource(String viewId);

    void increaseActionPendingResource(String actionId);

    void reduceViewPendingResource(String viewId);

    void reduceActionPendingResource(String actionId);

    void increaseViewError(String viewId);

    void increaseViewLongTask(String viewId);

    void updateViewUploadTime(String viewId, long dateTime);

    void updateViewUpdateTime(String viewId, long dateTime);

    void updateViewExtraAttr(String viewId, String attr);

    ArrayList<ActionBean> querySumAction(int limit);

    ArrayList<ViewBean> querySumView(int limit);

    ArrayList<ViewBean> querySumView(int limit, boolean allData);

    void cleanCloseActionData(String[] ids);

    void cleanCloseViewData();

    void closeAllActionAndView();

    boolean updateOrInsertSyncData(@NonNull SyncData data);

    boolean insertFtOptList(@NonNull List<SyncData> dataList, boolean reInsert);

    List<SyncData> queryDataByDataByTypeLimit(int limit, DataType dataType);

    List<SyncData> queryDataByDataByTypeLimitDesc(int limit, DataType dataType);

    List<SyncData> queryDataByDescLimit(int limit);

    List<SyncData> queryDataByDescLimit(int limit, boolean oldCache);

    int updateDataType(DataType dataType, long errorDateline);

    int deleteExpireCache(DataType dataType, long now, long expireDuration);

    int queryTotalCount(DataType dataType);

    int queryTotalCount(DataType[] list);

    void deleteOldestData(DataType type, int limit);

    void deleteOldestData(DataType[] list, int limit);

    boolean isOldCacheExist();

    void delete(List<Long> ids, boolean oldCache);

    void delete();
}
