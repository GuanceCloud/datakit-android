package com.ft.sdk.garble.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.UserData;
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
    public boolean insertFTOperation(final RecordData data) {
        final boolean[] result = new boolean[1];
        getDB(true, db -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
            contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getJsonString());
            contentValues.put(FTSQL.RECORD_COLUMN_SESSION_ID, data.getSessionid());
            contentValues.put(FTSQL.RECORD_COLUMN_OPTION, data.getOp());
            try {
                long value = db.insert(FTSQL.FT_TABLE_NAME, null, contentValues);
                LogUtils.d(TAG,"insert value:"+value);
                if(value>=0){
                    result[0] = true;
                }else{
                    result[0] = false;
                }
            }catch (Exception e){
                e.printStackTrace();
                LogUtils.e(TAG,"insert error message:"+e.getLocalizedMessage());
                result[0] = false;
            }
        });
        return result[0];
    }

    /**
     * 埋点数据批量插入数据库
     *
     * @param dataList
     */
    public boolean insertFtOptList(final List<RecordData> dataList) {
        final boolean[] result = new boolean[1];
        getDB(true, db -> {
            db.beginTransaction();
            int count = 0;
            for (RecordData data : dataList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
                contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getJsonString());
                contentValues.put(FTSQL.RECORD_COLUMN_SESSION_ID, data.getSessionid());
                contentValues.put(FTSQL.RECORD_COLUMN_OPTION, data.getOp());
                long rowId = db.insert(FTSQL.FT_TABLE_NAME, null, contentValues);
                if(rowId>=0){
                    count++;
                }
            }
            if(count == dataList.size()){
                result[0] = true;
            }else{
                result[0] = false;
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        });
        return result[0];
    }

    /**
     * 查询log数据
     * @param limit
     * @return
     */
    public List<RecordData> queryDataByDescLimitLog(final int limit){
        return queryDataByDescLimit(limit,"option=?",new String[]{OP.LOG.value});
    }

    /**
     * 查询 KeyEvent 数据
     * @param limit
     * @return
     */
    public List<RecordData> queryDataByDescLimitKeyEvent(final int limit){
        return queryDataByDescLimit(limit,"option=?",new String[]{OP.KEYEVENT.value});
    }

    /**
     * 查询 Object 数据
     * @param limit
     * @return
     */
    public List<RecordData> queryDataByDescLimitObject(final int limit){
        return queryDataByDescLimit(limit,"option=?",new String[]{OP.OBJECT.value});
    }

    /**
     * 查询埋点事件数据
     * @param limit
     * @return
     */
    public List<RecordData> queryDataByDescLimitTrack(final int limit){
        return queryDataByDescLimit(limit,"option!=? AND option!=? AND option!=? ",new String[]{OP.LOG.value,OP.KEYEVENT.value,OP.OBJECT.value});
    }

    /**
     * 查询所有数据
     *
     * @param limit limit == 0 表示获取全部数据
     * @return
     */
    public List<RecordData> queryDataByDescLimit(final int limit) {
        return queryDataByDescLimit(limit,null,null);
    }

    /**
     * 查询数据库中数据的总数
     * @return
     */
    public int queryTotalCount(OP op){
        final int[] count = new int[1];
        getDB(false,db ->{
            try {
                Cursor cursor = db.rawQuery("select count(*) from " + FTSQL.FT_TABLE_NAME + " where option='" + op.value+"'", null);
                cursor.moveToFirst();
                count[0] = cursor.getInt(0);
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        return count[0];
    }

    /**
     * 删除数据表中的前 limit 行数的数据
     * @param op
     * @param limit
     * @return
     */
    public void deleteOldestData(OP op,int limit){
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                try {
                    db.execSQL("DELETE FROM ft_operation_record where _id in (SELECT _id from ft_operation_record where option='"+op.value+"' ORDER by tm ASC LIMIT "+limit+")");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 根据条件查询数据
     * @param limit
     * @param selection
     * @param selectionArgs
     * @return
     */
    public List<RecordData> queryDataByDescLimit(final int limit,String selection,String[] selectionArgs) {
        final List<RecordData> recordList = new ArrayList<>();
        getDB(false, db -> {
            Cursor cursor;
            if (limit == 0) {
                cursor = db.query(FTSQL.FT_TABLE_NAME, null, selection, selectionArgs, null, null, FTSQL.RECORD_COLUMN_ID + " desc");
            } else {
                cursor = db.query(FTSQL.FT_TABLE_NAME, null, selection, selectionArgs, null, null, FTSQL.RECORD_COLUMN_ID + " desc", "" + limit);
            }
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_ID));
                long time = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_TM));
                String sessionId = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_SESSION_ID));
                String data = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA));
                RecordData recordData = new RecordData();
                recordData.setId(id);
                recordData.setTime(time);
                recordData.setSessionid(sessionId);
                recordData.parseJsonToObj(data);
                recordList.add(recordData);
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
                db.delete(FTSQL.FT_TABLE_NAME, FTSQL.RECORD_COLUMN_ID + "=?", new String[]{id});
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        });
    }

    /**
     * 测试使用，用于删除数据库中的数据
     */
    public boolean delete() {
        final boolean[] result = new boolean[1];
        getDB(true, db -> {
            int value = db.delete(FTSQL.FT_TABLE_NAME, null, null);
            if(value > 0){
                result[0] = true;
            }else {
                result[0] = false;
            }
        });
        return result[0];
    }

    /**
     * 插入用户数据进数据库
     *
     * @param userData
     */
    public void insertFTUserData(UserData userData) {
        getDB(true, db -> {
            ContentValues cv = new ContentValues();
            cv.put(FTSQL.USER_COLUMN_SESSION_ID, userData.getSessionId());
            cv.put(FTSQL.USER_COLUMN_DATA, userData.createDBDataString());
            db.insert(FTSQL.FT_TABLE_USER_DATA, null, cv);
        });
    }

    /**
     * 查询数据库中的用户信息
     *
     * @param sessionId
     * @return
     */
    public UserData queryFTUserData(String sessionId) {
        try {
            UserData userData = new UserData();
            getDB(false, db -> {
                Cursor cursor = db.query(FTSQL.FT_TABLE_USER_DATA, null, FTSQL.USER_COLUMN_SESSION_ID + "=?", new String[]{sessionId}, null, null, null, null);
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_SESSION_ID));
                    String data = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_DATA));
                    userData.setSessionId(id);
                    userData.parseUserDataFromDBData(data);
                    break;
                }
                cursor.close();
            });
            return userData.getName() == null ? null : userData;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询数据库中的所有用户信息
     *
     * @return
     */
    public List<UserData> queryFTUserDataList() {
        try {
            List<UserData> userDataList = new ArrayList<>();
            getDB(false, db -> {
                Cursor cursor = db.query(FTSQL.FT_TABLE_USER_DATA, null, null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    UserData userData = new UserData();
                    String id = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_SESSION_ID));
                    String data = cursor.getString(cursor.getColumnIndex(FTSQL.USER_COLUMN_DATA));
                    userData.setSessionId(id);
                    userData.parseUserDataFromDBData(data);
                    userDataList.add(userData);
                }
                cursor.close();
            });
            return userDataList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户的sessionId删除用户数据
     *
     * @param sessionId
     */
    public void deleteUserData(String sessionId) {
        getDB(true, db -> db.delete(FTSQL.FT_TABLE_USER_DATA, FTSQL.USER_COLUMN_SESSION_ID + "=?", new String[]{sessionId}));
    }
}
