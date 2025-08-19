package com.ft.sdk.garble.db;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.ft.sdk.DBCacheDiscard;
import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.base.DBManager;
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

    private final ContentResolver contentProvider;

    private FTDBManager() {
        Context context = FTApplication.getApplication();
        contentProvider = context.getContentResolver();
    }

    public static FTDBManager get() {
        if (ftdbManager == null) {
            ftdbManager = new FTDBManager();
        }
        return ftdbManager;
    }

    @Override
    public SQLiteOpenHelper initDataBaseHelper() {
        return DatabaseHelper.getInstance(FTApplication.getApplication(), FTDBConfig.DATABASE_NAME,
                FTDBConfig.DATABASE_VERSION);
    }

    @Override
    protected boolean enableDBSizeLimit() {
        return FTDBCachePolicy.get().isEnableLimitWithDbSize();
    }

    @Override
    protected void onDBSizeCacheChange(SQLiteDatabase db, long fileSize) {
//        LogUtils.d(TAG, "onDBSizeCacheChange:" + (fileSize / 1024) + "KB");
        //only do it in main process, db todo
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

        Uri resultUri = contentProvider.insert(FTContentProvider.URI_VIEW_DATA, contentValues);
        if (resultUri == null) {
            LogUtils.e(TAG, "initSumView executed failed via ContentProvider:" + data.getId());
        }
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

        // Use ContentProvider to insert data
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

        Uri resultUri = contentProvider.insert(FTContentProvider.URI_ACTION_DATA, contentValues);
        if (resultUri == null) {
            LogUtils.e(TAG, "initSumAction executed failed via ContentProvider: " + data.getId());
        }
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
        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        Uri uri = FTContentProvider.URI_ACTION_DATA;
        String selection = FTSQL.RUM_COLUMN_ID + "=?";
        String[] selectionArgs = {actionId};

        Cursor cursor = contentProvider.query(uri, null, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
            contentValues.put(FTSQL.RUM_COLUMN_ACTION_DURATION, duration);

            int updatedRows = contentProvider.update(uri, contentValues, selection, selectionArgs);
            if (updatedRows > 0) {
            } else {
                LogUtils.e(TAG, "closeAction executed failed via ContentProvider");
            }
        }
        if (cursor != null) {
            cursor.close();
        }


    }

    /**
     * Update view close status
     *
     * @param viewId
     * @param timeSpent
     */
    public void closeView(final String viewId, final long loadTime, final long timeSpent, final String attr) {
        LogUtils.d(TAG, "closeVIew:" + viewId + ",timeSpent:" + timeSpent);
        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        Uri uri = FTContentProvider.URI_VIEW_DATA;
        String selection = FTSQL.RUM_COLUMN_ID + "=?";
        String[] selectionArgs = {viewId};

        Cursor cursor = contentProvider.query(uri, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
            contentValues.put(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT, timeSpent);
            contentValues.put(FTSQL.RUM_COLUMN_VIEW_LOAD_TIME, loadTime);
            contentValues.put(FTSQL.RUM_COLUMN_EXTRA_ATTR, attr);

            int updatedRows = contentProvider.update(uri, contentValues, selection, selectionArgs);
            if (updatedRows > 0) {
            } else {
                LogUtils.e(TAG, "closeView executed failed via ContentProvider");
            }

        }
        if (cursor != null) {
            cursor.close();
        }

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

        Uri uri = tableName.equals(FTSQL.FT_TABLE_VIEW) ?
                FTContentProvider.URI_VIEW_DATA : FTContentProvider.URI_ACTION_DATA;
        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        String selection = FTSQL.RUM_COLUMN_ID + "=?";
        String[] selectionArgs = {id};

        Cursor cursor = contentProvider.query(uri, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);

            if (count > 0) {
                // Use ContentProvider's call method to execute execSQL
                String updateSql = "UPDATE " + tableName + " SET " + columnName + "=" + columnName + "+1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + id + "'";

                Bundle updateExtras = new Bundle();
                updateExtras.putString("sql", updateSql);

                Bundle updateResult = contentProvider.call(uri, FTContentProvider.METHOD_EXEC_SQL, null, updateExtras);
                if (!updateResult.getBoolean("success")) {
                    LogUtils.e(TAG, "increase executed failed via ContentProvider: " + updateSql);

                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
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

        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        Uri uri = tableName.equals(FTSQL.FT_TABLE_VIEW) ?
                FTContentProvider.URI_VIEW_DATA : FTContentProvider.URI_ACTION_DATA;
        String selection = FTSQL.RUM_COLUMN_ID + "=?";
        String[] selectionArgs = {id};

        Cursor cursor = contentProvider.query(uri, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);

            if (count > 0) {
                // Use ContentProvider's call method to execute execSQL
                String updateSql = "UPDATE " + tableName + " SET " + columnName + "=" + columnName + "-1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + id + "'";

                Bundle updateExtras = new Bundle();
                updateExtras.putString("sql", updateSql);

                Bundle updateResult = contentProvider.call(uri, FTContentProvider.METHOD_EXEC_SQL, null, updateExtras);
                if (!updateResult.getBoolean("success")) {
                    LogUtils.d(TAG, "reduce executed failed via ContentProvider: " + updateSql);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
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
        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        Uri uri = FTContentProvider.URI_VIEW_DATA;
        String selection = FTSQL.RUM_COLUMN_ID + "=?";
        String[] selectionArgs = {viewId};

        Cursor cursor = contentProvider.query(uri, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);

            if (count > 0) {
                // Use ContentProvider's call method to execute execSQL
                String sql1 = "UPDATE " + FTSQL.FT_TABLE_VIEW + " SET " + columName + "=" + dateTime + " WHERE " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'";
                String sql2 = "UPDATE " + FTSQL.FT_TABLE_VIEW + " SET " + FTSQL.RUM_VIEW_UPDATE_TIME + "=" + FTSQL.RUM_VIEW_UPDATE_TIME + "+ 1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'";

                // Execute first UPDATE statement
                Bundle updateExtras1 = new Bundle();
                updateExtras1.putString("sql", sql1);
                Bundle updateResult1 = contentProvider.call(uri, FTContentProvider.METHOD_EXEC_SQL, null, updateExtras1);

                // Execute second UPDATE statement
                Bundle updateExtras2 = new Bundle();
                updateExtras2.putString("sql", sql2);
                Bundle updateResult2 = contentProvider.call(uri, FTContentProvider.METHOD_EXEC_SQL, null, updateExtras2);

                if (updateResult1.getBoolean("success")
                        && updateResult2.getBoolean("success")) {
                } else {
                    LogUtils.d(TAG, "updateViewDateTimeByColumn executed failed via ContentProvider");
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Get statistics for each Action, {@link ActionBean}
     *
     * @param limit
     * @return
     */
    public ArrayList<ActionBean> querySumAction(final int limit) {
        final ArrayList<ActionBean> list = new ArrayList<>();

        try {
            // Use ContentProvider to query data
            String selection = FTSQL.RUM_COLUMN_IS_CLOSE + "=?";
            String[] selectionArgs = {"1"};
            String sortOrder = FTSQL.RUM_COLUMN_START_TIME + " ASC";

            Cursor cursor = contentProvider.query(FTContentProvider.URI_ACTION_DATA, null, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext() && (limit <= 0 || count < limit)) {
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
                    count++;
                }
                cursor.close();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
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

        try {
            // Use ContentProvider to query data
            // Uri uri = Uri.parse("content://com.ft.sdk.provider/view_data");
            String selection;
            if (allData) {
                selection = null;
            } else {
                selection = "(" + FTSQL.RUM_COLUMN_IS_CLOSE + "=1 AND (" +
                        FTSQL.RUM_DATA_UPLOAD_TIME + "<" + FTSQL.RUM_DATA_UPDATE_TIME
                        + " OR " + FTSQL.RUM_DATA_UPLOAD_TIME + "=0 )) OR "
                        + FTSQL.RUM_COLUMN_IS_CLOSE + "=0 ";
            }
            String sortOrder = FTSQL.RUM_COLUMN_START_TIME + " ASC";

            Cursor cursor = contentProvider.query(FTContentProvider.URI_VIEW_DATA, null, selection, null, sortOrder);
            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext() && (limit <= 0 || count < limit)) {
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
                    count++;
                }
                cursor.close();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return list;
    }

    /**
     * Clean up all Action {@link FTSQL#RUM_COLUMN_IS_CLOSE} = 1 data
     */
    public void cleanCloseActionData(String[] ids) {
        if (isAndroidTest) return;
        if (ids.length == 0) return;

        // Use ContentProvider to delete data
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            placeholders.append("?");
            if (i < ids.length - 1) {
                placeholders.append(",");
            }
        }
        String selection = FTSQL.RUM_COLUMN_ID + " IN (" + placeholders + ")";
        int deletedRows = contentProvider.delete(FTContentProvider.URI_ACTION_DATA, selection, ids);
        if (deletedRows > 0) {
            LogUtils.d(TAG, "cleanCloseActionData executed successfully " +
                    "via ContentProvider, deleted: " + deletedRows);
        }
    }

    /**
     * Clean up all View {@link FTSQL#RUM_COLUMN_IS_CLOSE} = 1 data
     */
    public void cleanCloseViewData() {
        if (isAndroidTest) return;

        // Use ContentProvider's call method to execute execSQL
        String sql = "DELETE FROM " + FTSQL.FT_TABLE_VIEW + " WHERE " + FTSQL.RUM_COLUMN_IS_CLOSE
                + "=1 AND " + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "<=0";

        Bundle extras = new Bundle();
        extras.putString("sql", sql);

        Bundle result = contentProvider.call(FTContentProvider.URI_VIEW_DATA, FTContentProvider.METHOD_EXEC_SQL, null, extras);
        if (!result.getBoolean("success")) {
            LogUtils.e(TAG, "cleanCloseViewData executed failed via ContentProvider");
        }
    }

    public void closeAllActionAndView() {
        LogUtils.d(TAG, "closeAllActionAndView");

        // Use ContentProvider's call method to execute execSQL for View table
        String viewSql = "UPDATE " + FTSQL.FT_TABLE_VIEW + " SET "
                + FTSQL.RUM_COLUMN_IS_CLOSE + "=1," + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "=0," + FTSQL.RUM_DATA_UPDATE_TIME + "=" + System.currentTimeMillis();

        Bundle viewExtras = new Bundle();
        viewExtras.putString("sql", viewSql);

        Bundle viewResult = contentProvider.call(FTContentProvider.URI_VIEW_DATA, FTContentProvider.METHOD_EXEC_SQL, null, viewExtras);

        // Use ContentProvider's call method to execute execSQL for Action table
        String actionSql = "UPDATE " + FTSQL.FT_TABLE_ACTION + " SET "
                + FTSQL.RUM_COLUMN_IS_CLOSE + "=1 ," + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "=0";

        Bundle actionExtras = new Bundle();
        actionExtras.putString("sql", actionSql);

        Bundle actionResult = contentProvider.call(FTContentProvider.URI_ACTION_DATA, FTContentProvider.METHOD_EXEC_SQL, null, actionExtras);

        if (!viewResult.getBoolean("success") || !actionResult.getBoolean("success")) {
            //LogUtils.d(TAG, "closeAllActionAndView executed successfully via ContentProvider");
            LogUtils.e(TAG, "closeAllActionAndView executed failed via ContentProvider");
        }
    }

    /**
     * Batch insert buried point data into the database
     *
     * @param dataList
     */
    public boolean insertFtOptList(@NonNull final List<SyncData> dataList, boolean reInsert) {

        try {
            // Use ContentProvider to batch insert data
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

                Uri resultUri = contentProvider.insert(FTContentProvider.URI_SYNC_DATA_FLAT, contentValues);
                if (resultUri == null) {
                    return false;
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "insertFtOptList failed: " + e.getMessage());
            return false;
        }
        return true;
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
        // Use ContentProvider's call method to execute rawQuery
        String originType = dataType.getValue();
        String targetDataType = originType.replace(DataType.ERROR_SAMPLED_SUFFIX, "");

        Uri uri = FTContentProvider.URI_SYNC_DATA_FLAT;

        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        String selection = FTSQL.RECORD_COLUMN_DATA_TYPE + "=? AND " + FTSQL.RECORD_COLUMN_TM + "<=?";
        String[] selectionArgs = {originType, String.valueOf(errorDateline)};

        Cursor cursor = contentProvider.query(uri, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            count[0] = cursor.getInt(0);

            if (count[0] > 0) {
                ContentValues value = new ContentValues();
                value.put(FTSQL.RECORD_COLUMN_DATA_TYPE, targetDataType);

                int updatedRows = contentProvider.update(uri, value, selection, selectionArgs);
                if (updatedRows > 0) {

                } else {
                    LogUtils.e(TAG, "updateDataType executed failed via ContentProvider");
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return count[0];
    }

    /**
     * @param dataType
     * @param now
     * @param expireDuration
     */
    public int deleteExpireCache(DataType dataType, long now, long expireDuration) {
        int count = 0;
        // Use ContentProvider to delete data
        long expireTimeline = now - expireDuration;
        String selection = FTSQL.RECORD_COLUMN_DATA_TYPE + "='" + dataType.getValue() + "' AND "
                + FTSQL.RECORD_COLUMN_TM + "< " + expireTimeline;

        count = contentProvider.delete(FTContentProvider.URI_SYNC_DATA_FLAT, selection, null);
        if (count > 0) {
            LogUtils.d(TAG, "deleteExpireCache executed successfully via ContentProvider, deleted: " + count);
        }
        return count;
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
        int count = 0;
        // Use ContentProvider's call method to execute rawQuery
        String where = getDataTypeWhereString(list);
        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        Uri uri = FTContentProvider.URI_SYNC_DATA_FLAT;
        Cursor cursor = contentProvider.query(uri, null, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }
        return count;
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
        // Use ContentProvider's call method to execute execSQL
        String where = getDataTypeWhereString(list);
        String sql = "DELETE FROM " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " where _id in (SELECT _id from "
                + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " where " + where + " ORDER by tm ASC LIMIT " + limit + ")";

        Bundle extras = new Bundle();
        extras.putString("sql", sql);

        Bundle result = contentProvider.call(FTContentProvider.URI_SYNC_DATA_FLAT, FTContentProvider.METHOD_EXEC_SQL, null, extras);
        if (result.getBoolean("success")) {

            if (FTDBCachePolicy.get().isEnableLimitWithDbSize()
                    && FTDBCachePolicy.get().isReachDbLimit()) {
                LogUtils.w(TAG, "BD close to reduce size");
            }
        } else {
            LogUtils.e(TAG, "deleteOldestData executed failed via ContentProvider");
        }
    }


    /**
     * Determine if old cache exists, if table doesn't exist, it will be directly caught by
     * {@link #getDB} exception try catch
     *
     * @return
     */
    public boolean isOldCacheExist() {
        boolean result = false;
        // Since rawQuery returns a Bundle, we need to get data through other methods
        // Here we directly use ContentProvider's query method
        Uri uri = FTContentProvider.URI_SYNC_DATA;

        Cursor cursor = contentProvider.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getCount() > 0;
            }
            cursor.close();
        }
        return result;
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

        try {
            // Use ContentProvider to query data
            String tableName = oldCache ? FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME : FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME;
            Uri uri = tableName.equals(FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME) ?
                    FTContentProvider.URI_SYNC_DATA_FLAT : FTContentProvider.URI_SYNC_DATA;

            Cursor cursor;
            if (limit == 0) {
                cursor = contentProvider.query(uri, null, selection, selectionArgs,
                        FTSQL.RECORD_COLUMN_ID + " " + order);
            } else {
                cursor = contentProvider.query(uri, null, selection, selectionArgs,
                        FTSQL.RECORD_COLUMN_ID + " " + order);
            }

            if (cursor != null) {
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
        } catch (Exception e) {
            LogUtils.e(TAG, "queryDataByDescLimit failed: " + e.getMessage());
        }
        return recordList;
    }

    /**
     * Delete tracking data based on the queried Id collection
     *
     * @param ids
     */
    public void delete(final List<String> ids, boolean oldCache) {
        // Use ContentProvider to delete data
        String tableName = oldCache ? FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME : FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME;
        Uri uri = tableName.equals(FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME) ?
                FTContentProvider.URI_SYNC_DATA_FLAT : FTContentProvider.URI_SYNC_DATA;

        for (String id : ids) {
            String selection = FTSQL.RECORD_COLUMN_ID + "=?";
            String[] selectionArgs = {id};
            int deletedRows = contentProvider.delete(uri, selection, selectionArgs);
            if (deletedRows > 0) {

            } else {
                LogUtils.d(TAG, "delete executed failed via ContentProvider, id: " + id);
            }
        }

    }

    /**
     * For testing use, used to delete data in the database
     */
    public void delete() {
        // Use ContentProvider to delete data
        Uri syncUri = FTContentProvider.URI_SYNC_DATA_FLAT;
        Uri actionUri = FTContentProvider.URI_ACTION_DATA;
        Uri viewUri = FTContentProvider.URI_VIEW_DATA;

        int deletedSync = contentProvider.delete(syncUri, null, null);
        int deletedAction = contentProvider.delete(actionUri, null, null);
        int deletedView = contentProvider.delete(viewUri, null, null);

        LogUtils.e(TAG, "DB table delete via ContentProvider - sync: " + deletedSync +
                ", action: " + deletedAction + ", view: " + deletedView);

        if (FTDBCachePolicy.get().isEnableLimitWithDbSize()
                && FTDBCachePolicy.get().isReachDbLimit()) {
            // Close database connection
            LogUtils.d(TAG, "close BD to reduce size");
        }

    }

    /**
     * Release database
     */
    public static void release() {
        if (ftdbManager != null) {
            ftdbManager.shutDown();
        }
    }
}
