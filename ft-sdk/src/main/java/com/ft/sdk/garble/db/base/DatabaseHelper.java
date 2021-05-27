package com.ft.sdk.garble.db.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.db.FTSQL;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:19
 * Description:
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static class DatabaseSingleton {
        static DatabaseHelper single(Context context, String name, int version) {
            return new DatabaseHelper(context, name, version);
        }
    }

    public static DatabaseHelper getInstance(Context context, String name, int version) {
        return DatabaseSingleton.single(context, name, version);
    }

    public DatabaseHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTable(db);

    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL(FTSQL.FT_TABLE_SYNC_CREATE);
        db.execSQL(FTSQL.FT_TABLE_VIEW_CREATE);
        db.execSQL(FTSQL.FT_TABLE_ACTION_CREATE);
//        db.execSQL(FTSQL.FT_TABLE_USER_DATA_CREATE);
    }
}
