package com.ft.sdk.internal.anr.historical;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.db.FTDBConfig;
import com.ft.sdk.garble.db.FTSQL;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.sdk.garble.db.file.FTHistoricalFileStore;
import com.ft.sdk.garble.db.file.FTFileStorePaths;
import com.ft.sdk.garble.utils.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
@SuppressWarnings("deprecation")
public class DatabaseV4HistoricalMigrationTest {
    private Context context;
    private String databaseName;
    private FTFileStorePaths paths;
    private String originalDatabaseName;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        originalDatabaseName = FTDBConfig.DATABASE_NAME;
        databaseName = "real-v4-" + System.nanoTime() + ".db";
        FTDBConfig.DATABASE_NAME = databaseName;
        paths = new FTFileStorePaths(context);
        new FTHistoricalFileStore(paths).deleteAll();
    }

    @After
    public void tearDown() {
        context.deleteDatabase(databaseName);
        FTDBConfig.DATABASE_NAME = originalDatabaseName;
        new FTHistoricalFileStore(paths).deleteAll();
    }

    @Test
    public void realVersionFourStateMigratesToSidecarsAndStableUuid() throws Exception {
        SQLiteDatabase v4 = context.openOrCreateDatabase(
                databaseName, Context.MODE_PRIVATE, null);
        createVersionFourSchema(v4);
        String dedupeKey = "historical-anr:v1:preview-exit";
        String retryDedupeKey = "historical-anr:v1:preview-retry";
        v4.execSQL("INSERT INTO " + FTSQL.FT_TABLE_VIEW + " ("
                        + FTSQL.RUM_COLUMN_ID + "," + FTSQL.RUM_COLUMN_VIEW_NAME + ","
                        + FTSQL.RUM_COLUMN_START_TIME + "," + FTSQL.RUM_COLUMN_IS_CLOSE + ","
                        + FTSQL.RUM_COLUMN_SESSION_ID + "," + FTSQL.RUM_COLUMN_VIEW_TIME_SPENT + ","
                        + FTSQL.RUM_COLUMN_EXTRA_ATTR + "," + FTSQL.RUM_COLUMN_PROCESS_NAME + ","
                        + FTSQL.RUM_COLUMN_PROCESS_RUN_ID + ","
                        + FTSQL.RUM_COLUMN_PROCESS_START_MS
                        + ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                new Object[]{"view", "Home", 10_000_000_000L, 1, "session",
                        2_000_000_000L,
                        "{\"collect_type\":\"collect_by_sample\"}", "old-process",
                        "old-run", 9_000L});
        v4.execSQL("INSERT INTO " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " ("
                        + FTSQL.RECORD_COLUMN_TM + "," + FTSQL.RECORD_COLUMN_DATA_UUID + ","
                        + FTSQL.RECORD_COLUMN_DATA + "," + FTSQL.RECORD_COLUMN_DATA_TYPE + ","
                        + FTSQL.RECORD_COLUMN_DEDUPE_KEY + ") VALUES (?,?,?,?,?)",
                new Object[]{1L, "preview-uuid", "sdk_data_id=preview-uuid",
                        DataType.RUM_APP.getValue(), dedupeKey});
        v4.execSQL("INSERT INTO " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " ("
                        + FTSQL.RECORD_COLUMN_TM + "," + FTSQL.RECORD_COLUMN_DATA_UUID + ","
                        + FTSQL.RECORD_COLUMN_DATA + "," + FTSQL.RECORD_COLUMN_DATA_TYPE + ","
                        + FTSQL.RECORD_COLUMN_DEDUPE_KEY + ") VALUES (?,?,?,?,?)",
                new Object[]{2L, "retry-preview-uuid", "sdk_data_id=retry-preview-uuid",
                        DataType.RUM_APP.getValue(), retryDedupeKey});
        v4.execSQL("INSERT INTO " + FTSQL.FT_HISTORICAL_EXIT_CLAIM_TABLE
                        + " (exit_key,state,owner_run_id,lease_until_ms,attempt_count,process_name,"
                        + "pid,exit_time_ms,reason,session_id,view_id,view_name,collect_type,"
                        + "event_dedupe_key,view_error_counted,updated_at_ms)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{"preview-exit", FTSQL.HISTORICAL_EXIT_STATE_COMMITTED, "old-run",
                        0L, 1, "old-process", 123, 12_000L, ProcessExitRecord.REASON_ANR,
                        "session", "view", "Home", "collect_by_sample", dedupeKey,
                        1, 13_000L});
        v4.execSQL("INSERT INTO " + FTSQL.FT_HISTORICAL_EXIT_CLAIM_TABLE
                        + " (exit_key,state,owner_run_id,lease_until_ms,attempt_count,process_name,"
                        + "pid,exit_time_ms,reason,session_id,view_id,view_name,collect_type,"
                        + "event_dedupe_key,view_error_counted,updated_at_ms)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{"preview-retry", FTSQL.HISTORICAL_EXIT_STATE_RETRY, "old-run",
                        0L, 1, "old-process", 124, 12_001L, ProcessExitRecord.REASON_ANR,
                        "session", "view", "Home", "collect_by_sample", retryDedupeKey,
                        1, 13_001L});
        v4.setVersion(4);
        v4.close();

        V4MigratingHistoricalRumContextStore migratingStore =
                new V4MigratingHistoricalRumContextStore(
                        context, new FileHistoricalRumContextStore(paths));
        List<HistoricalRumContext> contexts = migratingStore.load("old-process");
        DatabaseHelper helper = new DatabaseHelper(context, databaseName, 3);
        SQLiteDatabase downgraded = helper.getWritableDatabase();
        assertEquals(1, contexts.size());
        assertEquals("view", contexts.get(0).getViewId());
        assertEquals("old-run", contexts.get(0).getProcessRunId());
        assertEquals(12_000L, contexts.get(0).getViewEndMs());

        FileHistoricalExitLedger ledger = new FileHistoricalExitLedger(paths);
        HistoricalExitLedger.Claim claim = ledger.tryClaim(
                "preview-exit", "new-run", 20_000L, 80_000L,
                new ProcessExitRecord(
                        "old-process", 123, 12_000L, ProcessExitRecord.REASON_ANR, 100,
                        new ProcessExitRecord.TraceSource() {
                            @Override
                            public ByteArrayInputStream open() {
                                return new ByteArrayInputStream(
                                        "trace".getBytes(StandardCharsets.UTF_8));
                            }
                        }),
                contexts.get(0), dedupeKey);
        assertFalse(claim.isAcquired());

        Cursor sync = downgraded.query(
                FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                new String[]{FTSQL.RECORD_COLUMN_DATA_UUID, FTSQL.RECORD_COLUMN_DATA},
                FTSQL.RECORD_COLUMN_DEDUPE_KEY + "=?",
                new String[]{dedupeKey}, null, null, null);
        try {
            assertTrue(sync.moveToFirst());
            String stableUuid = Utils.toMD5(dedupeKey).substring(0, 16);
            assertEquals(stableUuid, sync.getString(0));
            assertEquals("sdk_data_id=" + stableUuid, sync.getString(1));
        } finally {
            sync.close();
        }

        Cursor retry = downgraded.query(
                FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                new String[]{FTSQL.RECORD_COLUMN_DATA_UUID, FTSQL.RECORD_COLUMN_DATA_TYPE},
                FTSQL.RECORD_COLUMN_DEDUPE_KEY + "=?",
                new String[]{retryDedupeKey}, null, null, null);
        try {
            assertTrue(retry.moveToFirst());
            assertEquals(
                    Utils.toMD5(retryDedupeKey).substring(0, 16), retry.getString(0));
            assertEquals(
                    HistoricalAnrDataId.pendingType(
                            DataType.RUM_APP),
                    retry.getString(1));
        } finally {
            retry.close();
            helper.close();
        }
    }

    @Test
    public void failedSidecarMigrationLeavesVersionFourForRetry() throws Exception {
        SQLiteDatabase v4 = context.openOrCreateDatabase(
                databaseName, Context.MODE_PRIVATE, null);
        createVersionFourSchema(v4);
        v4.execSQL("INSERT INTO " + FTSQL.FT_TABLE_VIEW + " ("
                        + FTSQL.RUM_COLUMN_ID + "," + FTSQL.RUM_COLUMN_PROCESS_RUN_ID
                        + ") VALUES (?,?)",
                new Object[]{"view", "old-run"});
        v4.setVersion(4);
        v4.close();

        final File invalidFilesDir = File.createTempFile(
                "historical-anr-files", ".tmp", context.getCacheDir());
        Context failingContext = new ContextWrapper(context) {
            @Override
            public Context getApplicationContext() {
                return this;
            }

            @Override
            public File getFilesDir() {
                return invalidFilesDir;
            }
        };
        DatabaseHelper helper = new DatabaseHelper(failingContext, databaseName, 3);
        try {
            helper.getWritableDatabase();
            throw new AssertionError("Expected migration failure");
        } catch (RuntimeException expected) {
            // The downgrade transaction must stay at v4 so the next open retries migration.
        } finally {
            helper.close();
            invalidFilesDir.delete();
        }

        SQLiteDatabase unchanged = context.openOrCreateDatabase(
                databaseName, Context.MODE_PRIVATE, null);
        assertEquals(4, unchanged.getVersion());
        unchanged.close();
    }

    private void createVersionFourSchema(SQLiteDatabase db) {
        db.execSQL(FTSQL.FT_TABLE_SYNC_CREATE);
        db.execSQL(FTSQL.FT_TABLE_VIEW_CREATE);
        db.execSQL(FTSQL.FT_TABLE_ACTION_CREATE);
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN "
                + FTSQL.RUM_COLUMN_PROCESS_NAME + " TEXT");
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN "
                + FTSQL.RUM_COLUMN_PROCESS_RUN_ID + " TEXT");
        db.execSQL("ALTER TABLE " + FTSQL.FT_TABLE_VIEW + " ADD COLUMN "
                + FTSQL.RUM_COLUMN_PROCESS_START_MS + " BIGINT DEFAULT 0");
        db.execSQL("ALTER TABLE " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME + " ADD COLUMN "
                + FTSQL.RECORD_COLUMN_DEDUPE_KEY + " TEXT");
        db.execSQL(FTSQL.FT_SYNC_DEDUPE_INDEX_CREATE);
        db.execSQL(FTSQL.FT_HISTORICAL_EXIT_CLAIM_CREATE);
    }
}
