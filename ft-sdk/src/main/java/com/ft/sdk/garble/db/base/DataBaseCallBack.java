package com.ft.sdk.garble.db.base;

import android.database.sqlite.SQLiteDatabase;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:02
 * Description: Used in conjunction with database read and write operations
 */
public interface DataBaseCallBack {
    void run(SQLiteDatabase db);
}
