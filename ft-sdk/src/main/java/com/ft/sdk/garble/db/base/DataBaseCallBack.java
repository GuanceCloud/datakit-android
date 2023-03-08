package com.ft.sdk.garble.db.base;

import android.database.sqlite.SQLiteDatabase;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:02
 * Description:配合数据库读取写入操作使用
 */
public interface DataBaseCallBack {
    void run(SQLiteDatabase db);
}
