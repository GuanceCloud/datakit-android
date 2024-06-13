package com.ft.sdk.garble.db.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.db.FTSQL;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:19
 * Description:数据管理创建、升级
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
        //创建数据库
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级数据库
        if (oldVersion == 1 && newVersion == 2) {
            update1to2(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 3 && newVersion < 3) {
            //ignore improve for performance version
        } else {
            super.onDowngrade(db, oldVersion, newVersion);
        }
    }

    /**
     * 在应用创建时，创建 {@link FTSQL#FT_TABLE_SYNC_CREATE)},{@link FTSQL#FT_TABLE_VIEW_CREATE)},{@link FTSQL#FT_TABLE_ACTION_CREATE)}
     *
     * @param db
     */
    private void createTable(SQLiteDatabase db) {
        db.execSQL(FTSQL.FT_TABLE_SYNC_CREATE);
        db.execSQL(FTSQL.FT_TABLE_VIEW_CREATE);
        db.execSQL(FTSQL.FT_TABLE_ACTION_CREATE);
//        db.execSQL(FTSQL.FT_TABLE_USER_DATA_CREATE);
    }

    /**
     * 新增 {@link FTSQL#RUM_DATA_UPLOAD_TIME},{@link FTSQL#RUM_DATA_UPLOAD_TIME},{@link FTSQL#RUM_VIEW_UPDATE_TIME}
     *
     * @param db
     */
    private void update1to2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN  " + FTSQL.RUM_DATA_UPLOAD_TIME + " BIGINT DEFAULT 0");
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN  " + FTSQL.RUM_DATA_UPDATE_TIME + " BIGINT DEFAULT 0");
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN  " + FTSQL.RUM_VIEW_UPDATE_TIME + " BIGINT DEFAULT 1");

    }
}
