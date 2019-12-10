package com.ft.sdk.garble.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.base.DBManager;
import com.ft.sdk.garble.db.base.DataBaseCallBack;
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

    public void insertFTOperation(final RecordData data) {
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
                contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getJsonString());
                db.insert(FTSQL.FT_TABLE_NAME, null, contentValues);
            }
        });
    }

    public void insertFtOptList(final List<RecordData> dataList) {
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                db.beginTransaction();
                for (RecordData data : dataList) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
                    contentValues.put(FTSQL.RECORD_COLUMN_DATA, data.getJsonString());
                    db.insert(FTSQL.FT_TABLE_NAME, null, contentValues);
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        });
    }

    public List<RecordData> queryDataByDescLimit(final String limit) {
        final List<RecordData> recordList = new ArrayList<>();
        getDB(false, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {

                Cursor cursor = db.query(FTSQL.FT_TABLE_NAME, null, null, null, null, null, FTSQL.RECORD_COLUMN_ID+" desc",limit);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_ID));
                    long time = cursor.getLong(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_TM));
                    String data = cursor.getString(cursor.getColumnIndex(FTSQL.RECORD_COLUMN_DATA));
                    RecordData recordData = new RecordData();
                    recordData.setId(id);
                    recordData.setTime(time);
                    recordData.parseJsonToObj(data);
                    recordList.add(recordData);
                }
                cursor.close();
            }
        });
        return recordList;
    }

    public void delete(final List<String> ids){
        getDB(true, new DataBaseCallBack() {
            @Override
            public void run(SQLiteDatabase db) {
                db.beginTransaction();
                for (String id:ids) {
                    db.delete(FTSQL.FT_TABLE_NAME, FTSQL.RECORD_COLUMN_ID+"=?",new String[]{id});
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        });
    }
}
