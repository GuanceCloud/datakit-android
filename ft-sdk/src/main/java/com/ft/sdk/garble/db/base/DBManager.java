package com.ft.sdk.garble.db.base;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * BY huangDianHua
 * DATE:2019-12-02 11:23
 * Description:
 */
public abstract class DBManager {
    private SQLiteOpenHelper databaseHelper;
    protected abstract SQLiteOpenHelper initDataBaseHelper();
    SQLiteOpenHelper getDataBaseHelper(){
        if(databaseHelper == null){
            databaseHelper = initDataBaseHelper();
        }
        return databaseHelper;
    }

    protected void getDB(boolean write,DataBaseCallBack callBack){
        synchronized (this){
            SQLiteOpenHelper helper = getDataBaseHelper();
            SQLiteDatabase db;
            if(write)
                db = helper.getWritableDatabase();
            else
                db = helper.getReadableDatabase();
            if(db.isOpen()){
                callBack.run(db);
                db.close();
            }
        }
    }

    protected void shutDown(){
        synchronized (this){
            if(databaseHelper != null){
                databaseHelper.close();
                databaseHelper = null;
            }
        }
    }
}
