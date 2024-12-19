package com.ft.sdk.garble.db.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * BY huangDianHua
 * DATE:2019-12-02 11:23
 * Description:数据库管理，获取数据库对象以及数据关闭
 */
public abstract class DBManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "DBManager";
    private SQLiteOpenHelper databaseHelper;

    protected abstract SQLiteOpenHelper initDataBaseHelper();

    SQLiteOpenHelper getDataBaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = initDataBaseHelper();
        }
        return databaseHelper;
    }

    /**
     * 同步锁，使数据库操作线程安全
     *
     * @param write
     * @param callBack
     */
    protected void getDB(boolean write, DataBaseCallBack callBack) {
        synchronized (this) {
            try {
                SQLiteOpenHelper helper = getDataBaseHelper();
                SQLiteDatabase db;
                if (write)
                    db = helper.getWritableDatabase();
                else
                    db = helper.getReadableDatabase();
                if (db.isOpen()) {
                    callBack.run(db);
                    if (write) {
                        checkDatabaseSize(db);
                    }
                    //db.close();
                }
            } catch (Exception e) {
                LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            }
        }
    }

    private long pageSize = 0;


    private void checkDatabaseSize(SQLiteDatabase db) {
        if (db != null) {
            // 获取数据库文件的大小
//            File dbFile = new File(db.getPath());
            if (enableDBSizeLimit()) {

                if (pageSize <= 0) {
                    Cursor cursor = db.rawQuery("PRAGMA page_size;", null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            pageSize = cursor.getInt(0);
                        }
                        cursor.close();
                    }

                }
                long pageCount = 0;
                Cursor cursor = db.rawQuery("PRAGMA page_count;", null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        pageCount = cursor.getInt(0);
                    }
                    cursor.close();
                }
                long fileSize = pageCount * pageSize; // 文件大小（字节）
                // 如果超过限制，执行清理操作
                onDBSizeCacheChange(db, fileSize);
            }
        }
    }

    protected abstract boolean enableDBSizeLimit();

    protected abstract void onDBSizeCacheChange(SQLiteDatabase db, long reachLimit);

    public void closeDB() {
        synchronized (this) {
            if (databaseHelper != null) {
                databaseHelper.close();
                LogUtils.d(TAG, "DB close");
            }
        }
    }

    /**
     * 关闭并释放数据库文件对象
     */
    protected void shutDown() {
        synchronized (this) {
            if (databaseHelper != null) {
                databaseHelper.close();
                databaseHelper = null;
            }
        }
    }
}
