package com.ft.sdk.garble.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ft.sdk.garble.db.base.DatabaseHelper;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class DatabaseVersion3CompatibilityTest {
    private final List<String> databaseNames = new ArrayList<>();

    @After
    public void tearDown() {
        Context context = RuntimeEnvironment.getApplication();
        for (String name : databaseNames) {
            context.deleteDatabase(name);
        }
    }

    @Test
    public void databaseVersionRemainsThreeWithoutHistoricalAnrSchema() {
        assertEquals(3, FTDBConfig.DATABASE_VERSION);
        String name = "new-v3-" + System.nanoTime() + ".db";
        databaseNames.add(name);

        DatabaseHelper helper = new DatabaseHelper(
                RuntimeEnvironment.getApplication(), name, FTDBConfig.DATABASE_VERSION);
        SQLiteDatabase db = helper.getWritableDatabase();

        assertEquals(3, db.getVersion());
        assertHistoricalAnrSchemaIsAbsent(db);
        helper.close();
    }

    @Test
    public void versionsOneTwoAndThreeOpenWithoutLosingExistingRows() {
        for (int oldVersion = 1; oldVersion <= 3; oldVersion++) {
            String name = "upgrade-" + oldVersion + "-" + System.nanoTime() + ".db";
            databaseNames.add(name);
            createLegacyDatabase(name, oldVersion);

            DatabaseHelper helper = new DatabaseHelper(
                    RuntimeEnvironment.getApplication(), name, 3);
            SQLiteDatabase db = helper.getWritableDatabase();

            assertEquals(3, db.getVersion());
            assertEquals(1, count(db, FTSQL.FT_TABLE_VIEW));
            assertEquals(1, count(db, FTSQL.FT_TABLE_ACTION));
            assertHistoricalAnrSchemaIsAbsent(db);
            if (oldVersion < 3) {
                assertEquals(1, count(db, FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME));
            } else {
                assertEquals(1, count(db, FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME));
            }
            helper.close();
        }
    }

    @Test
    public void versionFourPreviewDatabaseCanReturnToThreeWithoutDataLoss() {
        String name = "downgrade-v4-" + System.nanoTime() + ".db";
        databaseNames.add(name);
        Context context = RuntimeEnvironment.getApplication();
        SQLiteDatabase db = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null);
        db.execSQL(FTSQL.FT_TABLE_SYNC_CREATE);
        db.execSQL(FTSQL.FT_TABLE_VIEW_CREATE);
        db.execSQL(FTSQL.FT_TABLE_ACTION_CREATE);
        db.execSQL("INSERT INTO " + FTSQL.FT_TABLE_VIEW
                + " (" + FTSQL.RUM_COLUMN_ID + ") VALUES ('view')");
        db.setVersion(4);
        db.close();

        DatabaseHelper helper = new DatabaseHelper(context, name, 3);
        SQLiteDatabase downgraded = helper.getWritableDatabase();

        assertEquals(3, downgraded.getVersion());
        assertEquals(1, count(downgraded, FTSQL.FT_TABLE_VIEW));
        helper.close();
    }

    private void assertHistoricalAnrSchemaIsAbsent(SQLiteDatabase db) {
        assertFalse(hasColumn(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_NAME));
        assertFalse(hasColumn(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_RUN_ID));
        assertFalse(hasColumn(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_START_MS));
        assertFalse(hasColumn(db, FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                FTSQL.RECORD_COLUMN_DEDUPE_KEY));
        assertFalse(tableExists(db, "historical_exit_claim"));
    }

    private void createLegacyDatabase(String name, int version) {
        Context context = RuntimeEnvironment.getApplication();
        SQLiteDatabase db = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null);
        db.execSQL(legacyViewCreate(version));
        db.execSQL(FTSQL.FT_TABLE_ACTION_CREATE);
        db.execSQL("INSERT INTO " + FTSQL.FT_TABLE_VIEW
                + " (" + FTSQL.RUM_COLUMN_ID + "," + FTSQL.RUM_COLUMN_SESSION_ID
                + ") VALUES ('view','session')");
        db.execSQL("INSERT INTO " + FTSQL.FT_TABLE_ACTION
                + " (" + FTSQL.RUM_COLUMN_ID + ") VALUES ('action')");
        if (version < 3) {
            db.execSQL("CREATE TABLE " + FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,tm INTEGER,data TEXT,type TEXT)");
            db.execSQL("INSERT INTO " + FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME
                    + " (tm,data,type) VALUES (1,'legacy','rum')");
        } else {
            db.execSQL("CREATE TABLE " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,tm INTEGER,uuid TEXT,"
                    + "data TEXT,type TEXT)");
            db.execSQL("INSERT INTO " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                    + " (tm,uuid,data,type) VALUES (1,'uuid','flat','rum')");
        }
        db.setVersion(version);
        db.close();
    }

    private String legacyViewCreate(int version) {
        String suffix = version >= 2
                ? "," + FTSQL.RUM_DATA_UPDATE_TIME + " BIGINT DEFAULT 0"
                + "," + FTSQL.RUM_DATA_UPLOAD_TIME + " BIGINT DEFAULT 0"
                + "," + FTSQL.RUM_VIEW_UPDATE_TIME + " BIGINT DEFAULT 1"
                : "";
        return "CREATE TABLE " + FTSQL.FT_TABLE_VIEW + " ("
                + FTSQL.RUM_COLUMN_ID + " TEXT PRIMARY KEY,"
                + FTSQL.RUM_COLUMN_VIEW_NAME + " TEXT,"
                + FTSQL.RUM_COLUMN_VIEW_REFERRER + " TEXT,"
                + FTSQL.RUM_COLUMN_START_TIME + " BIGINT,"
                + FTSQL.RUM_COLUMN_IS_CLOSE + " INTEGER,"
                + FTSQL.RUM_COLUMN_SESSION_ID + " TEXT,"
                + FTSQL.RUM_COLUMN_ACTION_COUNT + " INTEGER,"
                + FTSQL.RUM_COLUMN_ERROR_COUNT + " INTEGER,"
                + FTSQL.RUM_COLUMN_LONG_TASK_COUNT + " INTEGER,"
                + FTSQL.RUM_COLUMN_VIEW_LOAD_TIME + " BIGINT,"
                + FTSQL.RUM_COLUMN_VIEW_TIME_SPENT + " BIGINT,"
                + FTSQL.RUM_COLUMN_RESOURCE_COUNT + " INTEGER,"
                + FTSQL.RUM_COLUMN_PENDING_RESOURCE + " INTEGER,"
                + FTSQL.RUM_COLUMN_EXTRA_ATTR + " TEXT"
                + suffix + ")";
    }

    private int count(SQLiteDatabase db, String table) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
        try {
            assertTrue(cursor.moveToFirst());
            return cursor.getInt(0);
        } finally {
            cursor.close();
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

    private boolean tableExists(SQLiteDatabase db, String table) {
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{table});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }
}
