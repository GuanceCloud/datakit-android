package com.ft.sdk.garble.db.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.db.FTSQL;
import com.ft.sdk.internal.anr.historical.HistoricalAnrV4Migration;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:19
 * Description: Data management creation and upgrade
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private final Context context;
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
        this.context = context == null || context.getApplicationContext() == null
                ? context
                : context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create database
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            update1to2(db);
        }
        if (oldVersion < 3) {
            update2to3(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 4 && newVersion == 3) {
            try {
                HistoricalAnrV4Migration.migrate(context, db);
            } catch (Exception e) {
                throw new IllegalStateException("Historical ANR v4 migration failed", e);
            }
            return;
        }
        if (oldVersion == 3 && newVersion < 3) {
            createTable(db);
        } else {
            super.onDowngrade(db, oldVersion, newVersion);
        }
    }

    /**
     * When the application is created, create {@link FTSQL#FT_TABLE_SYNC_CREATE)},
     * {@link FTSQL#FT_TABLE_VIEW_CREATE)},{@link FTSQL#FT_TABLE_ACTION_CREATE)}
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
     * Add new {@link FTSQL#RUM_DATA_UPLOAD_TIME},{@link FTSQL#RUM_DATA_UPLOAD_TIME},{@link FTSQL#RUM_VIEW_UPDATE_TIME}
     *
     * @param db
     */
    private void update1to2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN  " + FTSQL.RUM_DATA_UPLOAD_TIME + " BIGINT DEFAULT 0");
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN  " + FTSQL.RUM_DATA_UPDATE_TIME + " BIGINT DEFAULT 0");
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN  " + FTSQL.RUM_VIEW_UPDATE_TIME + " BIGINT DEFAULT 1");

    }

    private void update2to3(SQLiteDatabase db) {
        db.execSQL(FTSQL.FT_TABLE_SYNC_CREATE);
    }

}
