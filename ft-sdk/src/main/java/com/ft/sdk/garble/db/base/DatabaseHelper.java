package com.ft.sdk.garble.db.base;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.db.FTSQL;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:19
 * Description: Data management creation and upgrade
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
        if (oldVersion < 4) {
            update3to4(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= 4 && newVersion < 4) {
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
        db.execSQL(FTSQL.FT_SYNC_DEDUPE_INDEX_CREATE);
        db.execSQL(FTSQL.FT_HISTORICAL_EXIT_CLAIM_CREATE);
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

    private void update3to4(SQLiteDatabase db) {
        addColumnIfMissing(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_NAME,
                "TEXT");
        addColumnIfMissing(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_RUN_ID,
                "TEXT");
        addColumnIfMissing(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_START_MS,
                "BIGINT DEFAULT 0");
        addColumnIfMissing(db, FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                FTSQL.RECORD_COLUMN_DEDUPE_KEY, "TEXT");
        db.execSQL(FTSQL.FT_SYNC_DEDUPE_INDEX_CREATE);
        db.execSQL(FTSQL.FT_HISTORICAL_EXIT_CLAIM_CREATE);
    }

    private void addColumnIfMissing(SQLiteDatabase db, String table, String column,
                                    String definition) {
        if (!hasColumn(db, table, column)) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private boolean hasColumn(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (column.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }
}
