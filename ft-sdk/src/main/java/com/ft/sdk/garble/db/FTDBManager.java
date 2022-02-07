package com.ft.sdk.garble.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.base.DBManager;
import com.ft.sdk.garble.db.base.DataBaseCallBack;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-02 15:55
 * Description:
 */
public class FTDBManager extends DBManager {
    private static FTDBManager ftdbManager;
    public final static String TAG = "FTDBManager";

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

    /**
     * 埋点数据插入数据库
     *
     * @param data
     */
    public boolean insertFtOperation(final SyncJsonData data) {
        ArrayList<SyncJsonData> list = new ArrayList<>();
        list.add(data);
        return insertFtOptList(list);
    }


    /**
     * 更新 view 统计
     *
     * @param data
     * @return
     */

    public void initSumView(ViewBean data) {
        LogUtils.d(TAG, "initSumView:id:" + data.getId() + ",name:" + data.getViewName());
        getDB(true, db -> {
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
            db.insert(FTSQL.FT_TABLE_VIEW, null, contentValues);

        });
    }


    /**
     * 更新 action 统计
     *
     * @param data
     * @return
     */
    public void initSumAction(ActionBean data) {
        LogUtils.d(TAG, "initSumAction,id:" + data.getId() + ",Viewname:" + data.getViewName()
                + ",actionName:" + data.getActionName());

        getDB(true, db -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FTSQL.RUM_COLUMN_START_TIME, data.getStartTime());
            contentValues.put(FTSQL.RUM_COLUMN_LONG_TASK_COUNT, 0);
            contentValues.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
            contentValues.put(FTSQL.RUM_COLUMN_ID, data.getId());
            contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, data.isClose());
            contentValues.put(FTSQL.RUM_COLUMN_SESSION_ID, data.getSessionId());
            contentValues.put(FTSQL.RUM_COLUMN_VIEW_ID, data.getViewId());
            contentValues.put(FTSQL.RUM_COLUMN_VIEW_NAME, data.getViewName());
            contentValues.put(FTSQL.RUM_COLUMN_VIEW_REFERRER, data.getViewReferrer());
            contentValues.put(FTSQL.RUM_COLUMN_RESOURCE_COUNT, 0);
            contentValues.put(FTSQL.RUM_COLUMN_PENDING_RESOURCE, 0);
            contentValues.put(FTSQL.RUM_COLUMN_ACTION_DURATION, data.getDuration());
            contentValues.put(FTSQL.RUM_COLUMN_ACTION_NAME, data.getActionName());
            contentValues.put(FTSQL.RUM_COLUMN_ACTION_TYPE, data.getActionType());
            db.insert(FTSQL.FT_TABLE_ACTION, null, contentValues);
        });
    }

    /**
     * 更新 action 关闭状态
     *
     * @param actionId
     * @param duration
     */
    public void closeAction(String actionId, long duration, boolean force) {
        String where = FTSQL.RUM_COLUMN_ID + "='" + actionId + "'"
                + (force ? "" : ("AND " + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "<=0"));


        getDB(true, db -> {
            String sql = "select count(*) from " + FTSQL.FT_TABLE_ACTION
                    + " where " + where;
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            ContentValues contentValues = new ContentValues();

            if (count > 0) {

                contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                contentValues.put(FTSQL.RUM_COLUMN_ACTION_DURATION, duration);
                db.update(FTSQL.FT_TABLE_ACTION, contentValues,
                        FTSQL.RUM_COLUMN_ID + "='" + actionId + "'", null);

            }
        });


    }

    /**
     * 更新 view 关闭状态
     *
     * @param viewId
     * @param timeSpent
     */
    public void closeView(String viewId, long timeSpent) {
        LogUtils.d(TAG, "closeVIew:" + viewId);
        getDB(true, db -> {
            Cursor cursor = db.rawQuery("select count(*) from " + FTSQL.FT_TABLE_VIEW
                    + " where " + FTSQL.RUM_COLUMN_ID + "='" + viewId + "'", null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            ContentValues contentValues = new ContentValues();

            if (count > 0) {

                contentValues.put(FTSQL.RUM_COLUMN_IS_CLOSE, 1);
                contentValues.put(FTSQL.RUM_COLUMN_VIEW_TIME_SPENT, timeSpent);
                db.update(FTSQL.FT_TABLE_VIEW, contentValues,
                        FTSQL.RUM_COLUMN_ID + "='" + viewId + "'", null);
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


    private void increase(String tableName, String id, String columnName) {
        getDB(true, db -> {
            Cursor cursor = db.rawQuery("select count(*) from " + tableName
                    + " where " + FTSQL.RUM_COLUMN_ID + "='" + id + "'", null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            if (count > 0) {

                db.execSQL("UPDATE " + tableName + " SET "
                        + columnName + "=" + columnName + "+1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + id + "'");
            }
        });
    }

    private void reduce(String tableName, String id, String columnName) {
        getDB(true, db -> {
            Cursor cursor = db.rawQuery("select count(*) from " + tableName
                    + " where " + FTSQL.RUM_COLUMN_ID + "='" + id + "'", null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            if (count > 0) {
                db.execSQL("UPDATE " + tableName + " SET "
                        + columnName + "=" + columnName + "-1 WHERE " + FTSQL.RUM_COLUMN_ID + "='" + id + "'");
            }
        });
    }

    public ArrayList<ActionBean> querySumAction(int limit) {
        ArrayList<ActionBean> list = new ArrayList<>();
        getDB(false, db -> {
            try {
                Cursor cursor = db.query(FTSQL.FT_TABLE_ACTION, null, FTSQL.RUM_COLUMN_IS_CLOSE + "=1", null,
                        null, null, FTSQL.RUM_COLUMN_START_TIME + " asc", limit == 0 ? null : limit + "");
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
                    list.add(bean);
                    LogUtils.d(TAG, bean.toString());


                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return list;
    }

    public ArrayList<ViewBean> querySumView(int limit) {
        ArrayList<ViewBean> list = new ArrayList<>();

        getDB(false, db -> {

            try {
                Cursor cursor = db.query(FTSQL.FT_TABLE_VIEW, null, null, null,
                        null, null, FTSQL.RUM_COLUMN_START_TIME + " asc", limit == 0 ? null : limit + "");
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
                    list.add(viewBean);
                    LogUtils.d(TAG, viewBean.toString());
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return list;
    }

    public void cleanCloseActionData() {

        getDB(true, db -> {
            db.execSQL("DELETE FROM " + FTSQL.FT_TABLE_ACTION + " WHERE " + FTSQL.RUM_COLUMN_IS_CLOSE + "=1");
        });
    }

    public void cleanCloseViewData() {
        getDB(true, db -> {
            db.execSQL("DELETE FROM " + FTSQL.FT_TABLE_VIEW + " WHERE " + FTSQL.RUM_COLUMN_IS_CLOSE
                    + "=1 AND " + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "<=0");
        });
    }

    public void closeAllActionAndView() {
        getDB(true, db -> {
            db.execSQL("UPDATE " + FTSQL.FT_TABLE_VIEW + " SET "
                    + FTSQL.RUM_COLUMN_IS_CLOSE + "=1," + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "=0");
            db.execSQL("UPDATE " + FTSQL.FT_TABLE_ACTION + " SET "
                    + FTSQL.RUM_COLUMN_IS_CLOSE + "=1 ," + FTSQL.RUM_COLUMN_PENDING_RESOURCE + "=0");
        });

    }

    /**
     * 埋点数据批量插入数据库
     *
     * @param dataList
     */
    public boolean insertFtOptList(@NonNull final List<SyncJsonData> dataList) {
        final boolean[] result = new boolean[1];
        getDB(true, db -> {
            db.beginTransaction();
            int count = 0;
            for (SyncJsonData data : dataList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
                contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getDataString());
                contentValues.put(FTSQL.RECORD_COLUMN_DATA_TYPE, data.getDataType().getValue());
                long rowId = db.insert(FTSQL.FT_SYNC_TABLE_NAME, null, contentValues);
                if (rowId >= 0) {
                    count++;
                }
            }
            result[0] = count == dataList.size();
            db.setTransactionSuccessful();
            db.endTransaction();
        });
        return result[0];
    }


    public List<SyncJsonData> queryDataByDataByTypeLimit(int limit, DataType dataType) {
        return queryDataByDescLimit(limit, FTSQL.RECORD_COLUMN_DATA_TYPE + "=? ", new String[]{dataType.getValue()}, "asc");
    }

    public List<SyncJsonData> queryDataByDataByTypeLimitDesc(int limit, DataType dataType) {
        return queryDataByDescLimit(limit, FTSQL.RECORD_COLUMN_DATA_TYPE + "=? ", new String[]{dataType.getValue()}, "desc");
    }

    /**
     * 查询所有数据
     *
     * @param limit limit == 0 表示获取全部数据
     * @return
     */
    public List<SyncJsonData> queryDataByDescLimit(final int limit) {
        return queryDataByDescLimit(limit, null, null, "asc");
    }

    /**
     * 查询数据库中数据的总数
     *
     * @return
     */
    public int queryTotalCount(DataType dataType) {
        final int[] count = new int[1];
        getDB(false, db -> {
            try {
                Cursor cursor = db.rawQuery("select count(*) from "
                        + FTSQL.FT_SYNC_TABLE_NAME + " where " + FTSQL.RECORD_COLUMN_DATA_TYPE
                        + "='" + dataType.getValue() + "'", null);
                cursor.moveToFirst();
                count[0] = cursor.getInt(0);
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return count[0];
    }

    /**
     * 删除数据表中的前 limit 行数的数据
     *
     * @param type
     * @param limit
     * @return
     */
    public void deleteOldestData(DataType type, int limit) {
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                try {
                    db.execSQL("DELETE FROM ft_operation_record where _id in (SELECT _id from ft_operation_record where " + FTSQL.RECORD_COLUMN_DATA_TYPE + "='" + type.getValue() + "' ORDER by tm ASC LIMIT " + limit + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 根据条件查询数据
     *
     * @param limit
     * @param selection
     * @param selectionArgs
     * @return
     */
    private List<SyncJsonData> queryDataByDescLimit(final int limit, String selection, String[] selectionArgs, String order) {
        final List<SyncJsonData> recordList = new ArrayList<>();
        getDB(false, db -> {
            Cursor cursor;
            if (limit == 0) {
                cursor = db.query(FTSQL.FT_SYNC_TABLE_NAME, null, selection, selectionArgs,
                        null, null, FTSQL.RECORD_COLUMN_ID + " " + order);
            } else {
                cursor = db.query(FTSQL.FT_SYNC_TABLE_NAME, null, selection, selectionArgs,
                        null, null, FTSQL.RECORD_COLUMN_ID + " " + order, "" + limit);
            }
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_ID));
                long time = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_TM));
                String data = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA));
                String type = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA_TYPE));


                SyncJsonData recordData = null;

                for (DataType dataType : DataType.values()) {
                    if (dataType.getValue().equals(type)) {
                        recordData = new SyncJsonData(dataType);
                        break;
                    }
                }
                if (recordData != null) {
                    recordData.setId(id);
                    recordData.setTime(time);
                    recordData.setDataString(data);
                    recordList.add(recordData);
                }


            }
            cursor.close();
        });
        return recordList;
    }

    /**
     * 根据查询的Id集合删除埋点数据
     *
     * @param ids
     */
    public void delete(final List<String> ids) {
        getDB(true, db -> {
            db.beginTransaction();
            for (String id : ids) {
                db.delete(FTSQL.FT_SYNC_TABLE_NAME, FTSQL.RECORD_COLUMN_ID + "=?", new String[]{id});
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        });
    }

    /**
     * 测试使用，用于删除数据库中的数据
     */
    public void delete() {
        getDB(true, db -> {
            db.delete(FTSQL.FT_SYNC_TABLE_NAME, null, null);
            db.delete(FTSQL.FT_TABLE_ACTION, null, null);
            db.delete(FTSQL.FT_TABLE_VIEW, null, null);

        });
    }

//    /**
//     * 插入用户数据进数据库
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
//     * 查询数据库中的用户信息
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
//     * 查询数据库中的所有用户信息
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
//     * 根据用户的sessionId删除用户数据
//     *
//     * @param sessionId
//     */
//    public void deleteUserData(String sessionId) {
//        getDB(true, db -> db.delete(FTSQL.FT_TABLE_USER_DATA, FTSQL.USER_COLUMN_SESSION_ID + "=?", new String[]{sessionId}));
//    }
}
