package com.ft.sdk.garble.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.ft.sdk.DBCacheDiscard;
import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.base.DBManager;
import com.ft.sdk.garble.db.base.DataBaseCallBack;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-02 15:55
 * Description: Database management class
 */
@SuppressLint("Range")
public class FTDBManager extends DBManager {
    private static FTDBManager ftdbManager;
    public final static String TAG = Constants.LOG_TAG_PREFIX + "FTDBManager";

    /**
     * Note: AndroidTest will call this method {@link com.ft.test.base.FTBaseTest#avoidCleanData()}
     */
    private boolean isAndroidTest = false;


    private FTDBManager() {
    }

    public static FTDBManager get() {
        if (ftdbManager == null) {
            ftdbManager = new FTDBManager();
        }
        return ftdbManager;
    }

    @Override
    public SQLiteOpenHelper initDataBaseHelper() {
        return DatabaseHelper.getInstance(FTApplication.getApplication(), FTDBConfig.DATABASE_NAME, FTDBConfig.DATABASE_VERSION);
    }

    @Override
    protected boolean enableDBSizeLimit() {
        return FTDBCachePolicy.get().isEnableLimitWithDbSize();
    }

    @Override
    protected void onDBSizeCacheChange(SQLiteDatabase db, long fileSize) {
//        LogUtils.d(TAG, "onDBSizeCacheChange:" + (fileSize / 1024) + "KB");
        FTDBCachePolicy.get().setCurrentDBSize(fileSize);
        if (FTDBCachePolicy.get().isReachDbLimit()) {
            if (FTDBCachePolicy.get().getDbCacheDiscard() == DBCacheDiscard.DISCARD_OLDEST) {
                LogUtils.w(TAG, "Database size exceeds limit! Performing cleanup...");
                db.execSQL("DELETE FROM " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " where _id in (SELECT _id from "
                        + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " ORDER by tm ASC LIMIT " + Constants.DB_OLD_CACHE_REMOVE_COUNT + ")");
                LogUtils.d(TAG, "Cleanup completed. Size reduced.");
                db.close();
                LogUtils.w(TAG, "DB Close to reduce size");
            }
        }

    }

    /**
     * Insert tracking data into the database
     *
     * @param data
     */
    public boolean insertFtOperation(final SyncData data, boolean reInsert) {
        ArrayList<SyncData> list = new ArrayList<>();
        list.add(data);
        return insertFtOptList(list, reInsert);
    }


    /**
     * RUM view data statistics
     *
     * @param data
     * @return
     */

    public void initSumView(final ViewBean data) {
        LogUtils.d(TAG, "initSumView id:" + data.getId() + ",name:" + data.getViewName());
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FTSQL.RUM_COLUMN_START_TIME, data.getStartTime());
                contentValues.put(FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_ID, data.getId());
                contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 0);
                contentValues.put(FTSQL.RUM_COLUMN_ACTION_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_RESOURCE_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_NAME, data.getViewName());
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_REFERRER, data.getViewReferrer());
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_LOAD_TIME, data.getLoadTime());
                contentValues.put(FTSQL.RUM_COLUMN_SESSION_ID, data.getSessionId());
                contentValues.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, data.getAttrJsonString());

                try {
                    db.insertOrThrow(FTSQL.FT_TABLE_VIEW, null, contentValues);
                } catch (SQLException e) {
                    LogUtils.e(TAG, "initSumView id:" + data.getId() + "db insert ignore, " +
                            "reason:" + e.getMessage());

                }

            }
        });
    }


    /**
     * RUM action statistics
     *
     * @param data
     * @return
     */
    public void initSumAction(final ActionBean data) {
        LogUtils.d(TAG, "initSumAction id:" + data.getId() + ",ViewName:" + data.getViewName()
                + ",actionName:" + data.getActionName());

        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FTSQL.RUM_COLUMN_START_TIME, data.getStartTime());
                contentValues.put(FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_ID, data.getId());
                contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, data.isClose() ? 1 : 0);
                contentValues.put(FTSQL.RUM_COLUMN_SESSION_ID, data.getSessionId());
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_ID, data.getViewId());
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_NAME, data.getViewName());
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_REFERRER, data.getViewReferrer());
                contentValues.put(FTSQL.RUM_COLUMN_RESOURCE_COUNT, 0);
                contentValues.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
                contentValues.put(FTSQL.RUM_COLUMN_ACTION_DURATION, data.getDuration());
                contentValues.put(FTSQL.RUM_COLUMN_ACTION_NAME, data.getActionName());
                contentValues.put(FTSQL.RUM_COLUMN_ACTION_TYPE, data.getActionType());
                contentValues.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, data.getAttrJsonString());
                try {
                    db.insertOrThrow(FTSQL.FT_TABLE_ACTION, null, contentValues);
                } catch (SQLException e) {
                    LogUtils.d(TAG, "initSumAction id:" + data.getId() + "db insert ignore, " +
                            "reason:" + e.getMessage());
                }
            }
        });
    }

    /**
     * Update action close status
     *
     * @param actionId
     * @param duration
     */
    public void closeAction(final String actionId, final long duration, boolean force) {
        LogUtils.d(TAG, "closeAction:" + actionId + ",duration:" + duration);
        final String where = FTSQL.RUM_COLUMN_ID + "='" + actionId + "'"
                + (force ? "" : ("AND " + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "<=0"));


        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                String sql = "select count(*) from " + FTSQL.FT_TABLE_ACTION
                        + " where " + where;
                Cursor cursor = db.rawQuery(sql, null);
                cursor.moveToFirst();
                int count = cursor.getInt(0);
                cursor.close();

                if (count > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                    contentValues.put(FTSQL.RUM_COLUMN_ACTION_DURATION, duration);
                    db.update(FTSQL.FT_TABLE_ACTION, contentValues,
                            FTSQL.RUM_COLUMN_ID + "='" + actionId + "'", null);

                }
            }
        });


    }

    /**
     * Update view close status
     *
     * @param viewId
     * @param timeSpent
     */
    public void closeView(final String viewId, final long timeSpent, final String attr) {
        LogUtils.d(TAG, "closeVIew:" + viewId + ",timeSpent:" + timeSpent);
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                Cursor cursor = db.rawQuery("select count(*) from " + FTSQL.FT_TABLE_VIEW
                        + " where " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'", null);
                cursor.moveToFirst();
                int count = cursor.getInt(0);
                cursor.close();

                if (count > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                    contentValues.put(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT, timeSpent);
                    contentValues.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, attr);
                    db.update(FTSQL.FT_TABLE_VIEW, contentValues,
                            FTSQL.RUM_COLUMN_ID + "='" + viewId + "'", null);
                }
            }
        });
    }


    public void increaseActionError(String actionId) {
        increase(FTSQL.FT_TABLE_ACTION, actionId, FTSQL.RUM_COLUMN_ERROR_COUNT);
    }


    public void increaseActionResource(String actionId) {
        increase(FTSQL.FT_TABLE_ACTION, actionId, FTSQL.RUM_COLUMN_RESOURCE_COUNT);
    }

    public void increaseActionLongTask(String actionId) {
        increase(FTSQL.FT_TABLE_ACTION, actionId, FTSQL.RUM_COLUMN_LONG_TASK_COUNT);

    }

    public void increaseViewAction(String viewId) {
        increase(FTSQL.FT_TABLE_VIEW, viewId, FTSQL.RUM_COLUMN_ACTION_COUNT);
    }

    public void increaseViewResource(String viewId) {
        increase(FTSQL.FT_TABLE_VIEW, viewId, FTSQL.RUM_COLUMN_RESOURCE_COUNT);
    }

    public void increaseViewPendingResource(String viewId) {
        increase(FTSQL.FT_TABLE_VIEW, viewId, FTSQL.RUM_COLUMN_PENDING_RESOURCE);
    }

    public void increaseActionPendingResource(String actionId) {
        increase(FTSQL.FT_TABLE_ACTION, actionId, FTSQL.RUM_COLUMN_PENDING_RESOURCE);
    }

    public void reduceViewPendingResource(String viewId) {
        reduce(FTSQL.FT_TABLE_VIEW, viewId, FTSQL.RUM_COLUMN_PENDING_RESOURCE);
    }

    public void reduceActionPendingResource(String actionId) {
        reduce(FTSQL.FT_TABLE_ACTION, actionId, FTSQL.RUM_COLUMN_PENDING_RESOURCE);
    }

    public void increaseViewError(String viewId) {
        increase(FTSQL.FT_TABLE_VIEW, viewId, FTSQL.RUM_COLUMN_ERROR_COUNT);

    }

    public void increaseViewLongTask(String viewId) {
        increase(FTSQL.FT_TABLE_VIEW, viewId, FTSQL.RUM_COLUMN_LONG_TASK_COUNT);
    }


    /**
     * Decrement data count
     *
     * @param tableName
     * @param id
     * @param columnName
     */
    private void increase(final String tableName, final String id, final String columnName) {
        if (id == null) return;
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                Cursor cursor = db.rawQuery("select count(*) from " + tableName
                        + " where " + FTSQL.RUM_COLUMN_ID + "='" + id + "'", null);
                cursor.moveToFirst();
                int count = cursor.getInt(0);
                cursor.close();
                if (count > 0) {

                    db.execSQL("UPDATE " + tableName + " SET "
                            + columnName + "=" + columnName + "+1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + id + "'");
                }
            }
        });
    }

    /**
     * Decrement data count
     *
     * @param tableName
     * @param id
     * @param columnName
     */

    private void reduce(final String tableName, final String id, final String columnName) {
        if (id == null) return;
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                Cursor cursor = db.rawQuery("select count(*) from " + tableName
                        + " where " + FTSQL.RUM_COLUMN_ID + "='" + id + "'", null);
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    if (count > 0) {
                        db.execSQL("UPDATE " + tableName + " SET "
                                + columnName + "=" + columnName + "-1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + id + "'");
                    }
                }
                cursor.close();
            }
        });
    }

    /**
     * Update View upload time, used to mark delayed Resource data
     */
    public void updateViewUploadTime(String viewId, long dateTime) {
        updateViewDateTimeByColumn(viewId, FTSQL.RUM_DATA_UPLOAD_TIME, dateTime);


    }

    /**
     * Update View update time, used to mark delayed Resource data
     */
    public void updateViewUpdateTime(String viewId, long dateTime) {
        updateViewDateTimeByColumn(viewId, FTSQL.RUM_DATA_UPDATE_TIME, dateTime);

    }

    /**
     * Update viewid update and upload status
     *
     * @param viewId
     * @param columName {@link FTSQL#RUM_DATA_UPDATE_TIME},{@link FTSQL#RUM_DATA_UPLOAD_TIME}
     * @param dateTime  time, milliseconds
     */
    private void updateViewDateTimeByColumn(String viewId, String columName, long dateTime) {
        getDB(true, new DataBaseCallBack() {
                    @Override
                    public void run(SQLiteDatabase db) {
                        Cursor cursor = db.rawQuery("select count(*) from " + FTSQL.FT_TABLE_VIEW
                                + " where " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'", null);
                        if (cursor.moveToFirst()) {
                            int count = cursor.getInt(0);
                            if (count > 0) {
                                db.execSQL("UPDATE " + FTSQL.FT_TABLE_VIEW + " SET "
                                        + columName + "=" + dateTime
                                        + " WHERE " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'");
                                db.execSQL("UPDATE " + FTSQL.FT_TABLE_VIEW + " SET "
                                        + FTSQL.RUM_VIEW_UPDATE_TIME + "=" + FTSQL.RUM_VIEW_UPDATE_TIME + "+ 1"
                                        + " WHERE " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'");
                            }
                        }

                        cursor.close();
                    }
                }
        );
    }

    /**
     * Update {@link FTSQL#RUM_COLUMN_EXTRA_ATTR}
     *
     * @param viewId
     * @param attr
     */
    public void updateViewExtraAttr(String viewId, String attr) {
        getDB(true, new DataBaseCallBack() {
                    @Override
                    public void run(SQLiteDatabase db) {
                        Cursor cursor = db.rawQuery("select count(*) from " + FTSQL.FT_TABLE_VIEW
                                + " where " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'", null);
                        if (cursor.moveToFirst()) {
                            int count = cursor.getInt(0);
                            if (count > 0) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, attr);
                                db.update(FTSQL.FT_TABLE_VIEW, contentValues,
                                        FTSQL.RUM_COLUMN_ID + "= ?", new String[]{viewId});
                            }
                        }
                        cursor.close();
                    }
                }
        );
    }

    /**
     * Get statistics for each Action, {@link ActionBean}
     *
     * @param limit
     * @return
     */
    public ArrayList<ActionBean> querySumAction(final int limit) {
        final ArrayList<ActionBean> list = new ArrayList<>();
        getDB(false, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                try {
                    Cursor cursor = db.query(FTSQL.FT_TABLE_ACTION, null, FTSQL.RUM_COLUMN_IS_CLOSE + "=1", null,
                            null, null, FTSQL.RUM_COLUMN_START_TIME + " asc", limit == 0 ? null : String.valueOf(limit));
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ID));
                        int close = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_IS_CLOSE));
                        String sessionId = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_SESSION_ID));
                        String actionName = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ACTION_NAME));
                        String actionType = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ACTION_TYPE));
                        int longTaskCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_LONG_TASK_COUNT));
                        int errorCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ERROR_COUNT));
                        int resourceCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_RESOURCE_COUNT));
                        long duration = cursor.getLong(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ACTION_DURATION));
                        long startTime = cursor.getLong(cursor.getColumnIndex(FTSQL.RUM_COLUMN_START_TIME));
                        String viewId = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_ID));
                        String viewName = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_NAME));
                        String viewReferrer = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_REFERRER));
                        String attr = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_EXTRA_ATTR));

                        ActionBean bean = new ActionBean();
                        bean.setClose(close == 1);
                        bean.setSessionId(sessionId);
                        bean.setId(id);
                        bean.setActionName(actionName);
                        bean.setViewId(viewId);
                        bean.setViewName(viewName);
                        bean.setViewReferrer(viewReferrer);
                        bean.setActionType(actionType);
                        bean.setLongTaskCount(longTaskCount);
                        bean.setErrorCount(errorCount);
                        bean.setResourceCount(resourceCount);
                        bean.setDuration(duration);
                        bean.setStartTime(startTime);
                        bean.setFromAttrJsonString(attr);
                        list.add(bean);
                    }
                    cursor.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
        return list;
    }

    public ArrayList<ViewBean> querySumView(final int limit) {
        return querySumView(limit, false);
    }

    /**
     * Get statistics for each View {@link ViewBean}
     *
     * @param limit
     * @return
     */

    public ArrayList<ViewBean> querySumView(final int limit, boolean allData) {
        final ArrayList<ViewBean> list = new ArrayList<>();

        getDB(false, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {

                try {
                    String selection = allData ? null : "(" + FTSQL.RUM_COLUMN_IS_CLOSE + "=1 AND (" +
                            FTSQL.RUM_DATA_UPLOAD_TIME + "<" + FTSQL.RUM_DATA_UPDATE_TIME
                            + " OR " + FTSQL.RUM_DATA_UPLOAD_TIME + "=0 )) OR "
                            + FTSQL.RUM_COLUMN_IS_CLOSE + "=0";
                    Cursor cursor = db.query(FTSQL.FT_TABLE_VIEW, null, selection,
                            null, null, null,
                            FTSQL.RUM_COLUMN_START_TIME + " asc", limit == 0 ? null : String.valueOf(limit));
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ID));
                        int close = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_IS_CLOSE));
                        String sessionId = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_SESSION_ID));
                        int longTaskCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_LONG_TASK_COUNT));
                        int errorCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ERROR_COUNT));
                        int resourceCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_RESOURCE_COUNT));
                        int actionCount = cursor.getInt(cursor.getColumnIndex(FTSQL.RUM_COLUMN_ACTION_COUNT));
                        long timeSpent = cursor.getLong(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT));
                        long startTime = cursor.getLong(cursor.getColumnIndex(FTSQL.RUM_COLUMN_START_TIME));
                        long loadTime = cursor.getLong(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_LOAD_TIME));
                        String viewName = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_NAME));
                        String viewReferrer = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_VIEW_REFERRER));
                        String attr = cursor.getString(cursor.getColumnIndex(FTSQL.RUM_COLUMN_EXTRA_ATTR));
                        long viewUpdateTime = cursor.getLong(cursor.getColumnIndex(FTSQL.RUM_VIEW_UPDATE_TIME));

                        ViewBean viewBean = new ViewBean();
                        viewBean.setClose(close == 1);
                        viewBean.setId(id);
                        viewBean.setActionCount(actionCount);
                        viewBean.setTimeSpent(timeSpent);
                        viewBean.setLoadTime(loadTime);
                        viewBean.setStartTime(startTime);
                        viewBean.setResourceCount(resourceCount);
                        viewBean.setErrorCount(errorCount);
                        viewBean.setLongTaskCount(longTaskCount);
                        viewBean.setSessionId(sessionId);
                        viewBean.setViewName(viewName);
                        viewBean.setViewReferrer(viewReferrer);
                        viewBean.setFromAttrJsonString(attr);
                        viewBean.setViewUpdateTime(viewUpdateTime);

                        list.add(viewBean);
                    }
                    cursor.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));

                }
            }
        });
        return list;
    }

    /**
     * Clean up all Action {@link FTSQL#RUM_COLUMN_IS_CLOSE} = 1 data
     */
    public void cleanCloseActionData(String[] ids) {
        if (isAndroidTest) return;
        if (ids.length == 0) return;
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                String tableName = FTSQL.FT_TABLE_ACTION;
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < ids.length; i++) {
                    placeholders.append("?");
                    if (i < ids.length - 1) {
                        placeholders.append(",");
                    }
                }
                db.delete(tableName, FTSQL.RUM_COLUMN_ID + " IN (" + placeholders + ")", ids);
            }
        });
    }

    /**
     * Clean up all View {@link FTSQL#RUM_COLUMN_IS_CLOSE} = 1 data
     */
    public void cleanCloseViewData() {
        if (isAndroidTest) return;
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                db.execSQL("DELETE FROM " + FTSQL.FT_TABLE_VIEW + " WHERE " + FTSQL.RUM_COLUMN_IS_CLOSE
                        + "=1 AND " + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "<=0");
            }
        });
    }

    public void closeAllActionAndView() {
        LogUtils.d(TAG, "closeAllActionAndView");
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                db.execSQL("UPDATE " + FTSQL.FT_TABLE_VIEW + " SET "
                        + FTSQL.RUM_COLUMN_IS_CLOSE + "=1," + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "=0," + FTSQL.RUM_DATA_UPDATE_TIME + "=" + System.currentTimeMillis());
                db.execSQL("UPDATE " + FTSQL.FT_TABLE_ACTION + " SET "
                        + FTSQL.RUM_COLUMN_IS_CLOSE + "=1 ," + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "=0");
            }
        });

    }

    /**
     * Batch insert buried point data into the database
     *
     * @param dataList
     */
    public boolean insertFtOptList(@NonNull final List<SyncData> dataList, boolean reInsert) {
        final boolean[] result = new boolean[1];
        getDB(true, dataList.size(), new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                int count = 0;
                for (SyncData data : dataList) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
                    if (reInsert) {
                        String uuid = Utils.getGUID_16();
                        contentValues.put(FTSQL.RECORD_COLUMN_DATA_UUID, uuid);
                        contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getDataString(uuid));
                    } else {
                        contentValues.put(FTSQL.RECORD_COLUMN_DATA_UUID, data.getUuid());
                        contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getDataString());
                    }

                    contentValues.put(FTSQL.RECORD_COLUMN_DATA_TYPE, data.getDataType().getValue());
                    long rowId = db.insert(FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME, null, contentValues);
                    if (rowId >= 0) {
                        count++;
                    }
                }
                result[0] = count == dataList.size();
            }
        });
        return result[0];
    }


    public List<SyncData> queryDataByDataByTypeLimit(int limit, DataType dataType) {
        return queryDataByDescLimit(limit, FTSQL.RECORD_COLUMN_DATA_TYPE + "=? ", new String[]{dataType.getValue()}, "asc", false);
    }

    public List<SyncData> queryDataByDataByTypeLimitDesc(int limit, DataType dataType) {
        return queryDataByDescLimit(limit, FTSQL.RECORD_COLUMN_DATA_TYPE + "=? ", new String[]{dataType.getValue()}, "desc", false);
    }

    /**
     * Query all user information in the database
     *
     * @return
     */
    public List<SyncData> queryDataByDescLimit(final int limit) {
        return queryDataByDescLimit(limit, false);
    }

    /**
     * Query all user information in the database
     *
     * @param limit limit == 0 means get all data
     * @return
     */
    public List<SyncData> queryDataByDescLimit(final int limit, boolean oldCache) {
        return queryDataByDescLimit(limit, null, null, "asc", oldCache);
    }

    /**
     * Change the type of data that needs to be uploaded in the cache area
     *
     * @param dataType
     */
    public int updateDataType(DataType dataType, long errorDateline) {
        final int[] count = new int[1];
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                ContentValues value = new ContentValues();
                String originType = dataType.getValue();
                String targetDataType = originType.replace(DataType.ERROR_SAMPLED_SUFFIX, "");

                String whereSql = FTSQL.RECORD_COLUMN_DATA_TYPE + "='" + originType + "' AND "
                        + FTSQL.RECORD_COLUMN_TM + " <= " + errorDateline;
                Cursor cursor = db.rawQuery("select count(*) from " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                        + " where " + whereSql, null);
                if (cursor.moveToFirst()) {
                    count[0] = cursor.getInt(0);
                }
                cursor.close();

                if (count[0] > 0) {
                    value.put(FTSQL.RECORD_COLUMN_DATA_TYPE, targetDataType);
                    db.update(FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME, value, whereSql, null);
                }

            }
        });
        return count[0];
    }

    /**
     * @param dataType
     * @param now
     * @param expireDuration
     */
    public int deleteExpireCache(DataType dataType, long now, long expireDuration) {
        final int[] count = new int[1];
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                long expireTimeline = now - expireDuration;
                count[0] = db.delete(FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                        FTSQL.RECORD_COLUMN_DATA_TYPE + "='" + dataType.getValue() + "' AND "
                                + FTSQL.RECORD_COLUMN_TM + "< " + expireTimeline, null);
            }
        });
        return count[0];
    }

    /**
     * Query the total number of data in the database
     *
     * @return
     */
    public int queryTotalCount(final DataType dataType) {
        return queryTotalCount(new DataType[]{dataType});
    }

    public int queryTotalCount(DataType[] list) {
        final int[] count = new int[1];
        getDB(false, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                try {
                    String where = getDataTypeWhereString(list);
                    Cursor cursor = db.rawQuery("select count(*) from "
                            + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " where " + where, null);
                    cursor.moveToFirst();
                    count[0] = cursor.getInt(0);
                    cursor.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
        return count[0];
    }

    private String getDataTypeWhereString(DataType[] list) {
        StringBuilder where = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            where.append(FTSQL.RECORD_COLUMN_DATA_TYPE + "='").append(list[i].getValue()).append("'");
            if (i < list.length - 1) {
                where.append(" OR ");
            }
        }
        return where.toString();
    }

    /**
     * Delete the first limit rows of data in the data table
     *
     * @param type
     * @param limit
     * @return
     */
    public void deleteOldestData(final DataType type, final int limit) {
        deleteOldestData(new DataType[]{type}, limit);
    }

    public void deleteOldestData(DataType[] list, final int limit) {
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                try {
                    String where = getDataTypeWhereString(list);
                    db.execSQL("DELETE FROM " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " where _id in (SELECT _id from "
                            + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " where " + where + " ORDER by tm ASC LIMIT " + limit + ")");

                    if (FTDBCachePolicy.get().isEnableLimitWithDbSize()
                            && FTDBCachePolicy.get().isReachDbLimit()) {
                        db.close();
                        LogUtils.w(TAG, "BD close to reduce size");
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));

                }
            }
        });
    }


    /**
     * Determine if old cache exists, if table doesn't exist, it will be directly caught by
     * {@link #getDB} exception try catch
     *
     * @return
     */
    public boolean isOldCacheExist() {
        final boolean[] result = new boolean[1];
        getDB(false, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                try {
                    Cursor cursor = db.rawQuery("SELECT EXISTS (SELECT 1 FROM " + FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME + ");", null);

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            result[0] = cursor.getInt(0) == 1;
                        }
                        cursor.close();
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("no such table: sync_data")) {
                        LogUtils.d(TAG, "There is no old cache in 'sync_data', ignore this error");
                    } else {
                        LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                    }
                }
            }
        });
        return result[0];
    }

    /**
     * Query data based on conditions
     *
     * @param limit
     * @param selection
     * @param selectionArgs
     * @return
     */
    private List<SyncData> queryDataByDescLimit(final int limit, final String selection, final String[] selectionArgs, final String order, boolean oldCache) {
        final List<SyncData> recordList = new ArrayList<>();
        getDB(false, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                String tableName = oldCache ? FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME : FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME;

                Cursor cursor;
                if (limit == 0) {
                    cursor = db.query(tableName, null, selection, selectionArgs,
                            null, null, FTSQL.RECORD_COLUMN_ID + " " + order);
                } else {
                    cursor = db.query(tableName, null, selection, selectionArgs,
                            null, null, FTSQL.RECORD_COLUMN_ID + " " + order, String.valueOf(limit));
                }
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_ID));
                    long time = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_TM));
                    String data = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA));
                    String type = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA_TYPE));
                    String uuid = oldCache ? "" : cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA_UUID));

                    SyncData recordData = null;

                    for (DataType dataType : DataType.values()) {
                        if (dataType.getValue().equals(type)) {
                            recordData = new SyncData(dataType);
                            break;
                        }
                    }
                    if (recordData != null) {
                        recordData.setId(id);
                        recordData.setTime(time);
                        recordData.setUuid(uuid);
                        recordData.setDataString(data);
                        recordList.add(recordData);
                    }


                }
                cursor.close();
            }
        });
        return recordList;
    }

    /**
     * Delete tracking data based on the queried Id collection
     *
     * @param ids
     */
    public void delete(final List<String> ids, boolean oldCache) {
        getDB(true, ids.size(), new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                String tableName = oldCache ? FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME : FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME;
                for (String id : ids) {
                    db.delete(tableName, FTSQL.RECORD_COLUMN_ID + "=?", new String[]{id});
                }
            }
        });
    }

    /**
     * For testing use, used to delete data in the database
     */
    public void delete() {
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                db.delete(FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME, null, null);
                db.delete(FTSQL.FT_TABLE_ACTION, null, null);
                db.delete(FTSQL.FT_TABLE_VIEW, null, null);
                LogUtils.e(TAG, "DB table delete");

                if (FTDBCachePolicy.get().isEnableLimitWithDbSize()
                        && FTDBCachePolicy.get().isReachDbLimit()) {
                    db.close();
                    LogUtils.d(TAG, "close BD to reduce size");
                }

            }
        });
    }

    /**
     * Release database
     */
    public static void release() {
        if (ftdbManager != null) {
            ftdbManager.shutDown();
        }
    }


//    /**
//     * Insert user data into database
//     *
//     * @param userData
//     */
//    public void insertFTUserData(UserData userData) {
//        getDB(true, db -> {
//            ContentValues cv = new ContentValues();
//            cv.put(FTSQL.USER_COLUMN_SESSION_ID, userData.getSessionId());
//            cv.put(FTSQL.USER_COLUMN_DATA, userData.createDBDataString());
//            db.insert(FTSQL.FT_TABLE_USER_DATA, null, cv);
//        });
//    }
//
//    /**
//     * Query user information in database
//     *
//     * @param sessionId
//     * @return
//     */
//    public UserData queryFTUserData(String sessionId) {
//        try {
//            UserData userData = new UserData();
//            getDB(false, db -> {
//                Cursor cursor = db.query(FTSQL.FT_TABLE_USER_DATA, null, FTSQL.USER_COLUMN_SESSION_ID + "=?", new String[]{sessionId}, null, null, null, null);
//                while (cursor.moveToNext()) {
//                    String id = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_SESSION_ID));
//                    String data = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_DATA));
//                    userData.setSessionId(id);
//                    userData.setExtsWithJsonString(data);
//                    break;
//                }
//                cursor.close();
//            });
//            return userData.getName() == null ? null : userData;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    /**
//     * Query all user information in database
//     *
//     * @return
//     */
//    public List<UserData> queryFTUserDataList() {
//        try {
//            List<UserData> userDataList = new ArrayList<>();
//            getDB(false, db -> {
//                Cursor cursor = db.query(FTSQL.FT_TABLE_USER_DATA, null, null, null, null, null, null, null);
//                while (cursor.moveToNext()) {
//                    UserData userData = new UserData();
//                    String id = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_SESSION_ID));
//                    String data = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_DATA));
//                    userData.setSessionId(id);
//                    userData.setExtsWithJsonString(data);
//                    userDataList.add(userData);
//                }
//                cursor.close();
//            });
//            return userDataList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    /**
//     * Delete user data based on user's sessionId
//     *
//     * @param sessionId
//     */
//    public void deleteUserData(String sessionId) {
//        getDB(true, db -> db.delete(FTSQL.FT_TABLE_USER_DATA, FTSQL.USER_COLUMN_SESSION_ID + "=?", new String[]{sessionId}));
//    }
}
