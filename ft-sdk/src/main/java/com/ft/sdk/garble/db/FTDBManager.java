package com.ft.sdk.garble.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.db.base.DBManager;
import com.ft.sdk.garble.db.base.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-02 15:55
 * Description:
 */
public class FTDBManager extends DBManager {
    private static FTDBManager ftdbManager;

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
    public void insertFTOperation(final RecordData data) {
        getDB(true, db -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
            contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getJsonString());
            contentValues.put(FTSQL.RECORD_COLUMN_SESSION_ID, data.getSessionid());
            db.insert(FTSQL.FT_TABLE_NAME, null, contentValues);
        });
    }

    /**
     * 埋点数据批量插入数据库
     *
     * @param dataList
     */
    public void insertFtOptList(final List<RecordData> dataList) {
        getDB(true, db -> {
            db.beginTransaction();
            for (RecordData data : dataList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
                contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getJsonString());
                contentValues.put(FTSQL.RECORD_COLUMN_SESSION_ID, data.getSessionid());
                db.insert(FTSQL.FT_TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        });
    }

    /**
     * 查询埋点数据
     *
     * @param limit limit == 0 表示获取全部数据
     * @return
     */
    public List<RecordData> queryDataByDescLimit(final int limit) {
        final List<RecordData> recordList = new ArrayList<>();
        getDB(false, db -> {
            Cursor cursor;
            if (limit == 0) {
                cursor = db.query(FTSQL.FT_TABLE_NAME, null, null, null, null, null, FTSQL.RECORD_COLUMN_ID + " desc");
            } else {
                cursor = db.query(FTSQL.FT_TABLE_NAME, null, null, null, null, null, FTSQL.RECORD_COLUMN_ID + " desc", "" + limit);
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
    public void delete() {
        getDB(true, db -> db.delete(FTSQL.FT_TABLE_NAME, null, null));
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
     * 根据用户的sessionId删除用户数据
     *
     * @param sessionId
     */
    public void deleteUserData(String sessionId) {
        getDB(true, db -> db.delete(FTSQL.FT_TABLE_USER_DATA, FTSQL.USER_COLUMN_SESSION_ID + "=?", new String[]{sessionId}));
    }
}
