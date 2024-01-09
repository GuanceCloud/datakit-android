package com.ft.sdk.garble.db.base;

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
            SQLiteOpenHelper helper = getDataBaseHelper();
            SQLiteDatabase db;
            if (write)
                db = helper.getWritableDatabase();
            else
                db = helper.getReadableDatabase();
            if (db.isOpen()) {
                callBack.run(db);
                //db.close();
            }
        }
    }

    public void closeDB() {
        LogUtils.d(TAG, "DB try to close");
        synchronized (this) {
            if (databaseHelper != null) {
                databaseHelper.close();
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
