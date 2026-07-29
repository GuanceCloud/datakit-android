package com.ft.sdk.garble.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class HistoricalExitProviderTest {
    private String originalDatabaseName;
    private String databaseName;
    private FTContentProvider provider;

    @Before
    public void setUp() {
        originalDatabaseName = FTDBConfig.DATABASE_NAME;
        databaseName = "historical-provider-" + System.nanoTime() + ".db";
        FTDBConfig.DATABASE_NAME = databaseName;
        FTDBManager.release();
        provider = Robolectric.buildContentProvider(FTContentProvider.class).create().get();
    }

    @After
    public void tearDown() {
        FTDBManager.release();
        RuntimeEnvironment.getApplication().deleteDatabase(databaseName);
        FTDBConfig.DATABASE_NAME = originalDatabaseName;
    }

    @Test
    public void claimIsAtomicLeaseCanBeTakenOverAndCommittedIsTerminal() {
        Bundle firstClaim = claim("owner-one", 100, 200);
        assertTrue(provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                firstClaim).getBoolean("acquired"));
        assertFalse(provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                claim("owner-two", 150, 250)).getBoolean("acquired"));
        Bundle takeover = claim("owner-two", 201, 301);
        takeover.putString("session_id", "new-session");
        takeover.putString("view_id", "new-view");
        takeover.putString("view_name", "Checkout");
        takeover.putString("collect_type", "not_collect");
        Bundle takeoverResult = provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                takeover);
        assertTrue(takeoverResult.getBoolean("acquired"));
        assertEquals("session", takeoverResult.getString("session_id"));
        assertEquals("view", takeoverResult.getString("view_id"));
        assertEquals("Home", takeoverResult.getString("view_name"));
        assertEquals("collect_by_sample", takeoverResult.getString("collect_type"));

        Bundle commit = new Bundle();
        commit.putString("exit_key", "exit");
        commit.putString("owner_run_id", "owner-two");
        commit.putInt("state", FTSQL.HISTORICAL_EXIT_STATE_COMMITTED);
        commit.putLong("lease_until_ms", 0);
        commit.putLong("updated_at_ms", 220);
        assertTrue(provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_UPDATE,
                null,
                commit).getBoolean("success"));

        assertFalse(provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                claim("owner-one", 500, 600)).getBoolean("acquired"));
    }

    @Test
    public void dedupeUniqueIndexRejectsSecondSyncInsert() {
        Uri uri = FTContentProvider.getUriSyncDataFlat();
        ContentValues first = syncValues("first");
        ContentValues second = syncValues("second");

        assertNotNull(provider.insert(uri, first));
        assertNull(provider.insert(uri, second));
        Cursor cursor = provider.query(
                uri,
                new String[]{"COUNT(*)"},
                FTSQL.RECORD_COLUMN_DEDUPE_KEY + "=?",
                new String[]{"historical-anr:v1:key"},
                null);
        try {
            cursor.moveToFirst();
            assertEquals(1, cursor.getInt(0));
        } finally {
            cursor.close();
        }
    }

    @Test
    public void historicalViewErrorCountIsAppliedOncePerDedupeKey() {
        Bundle exitClaim = claim("owner", 100, 200);
        exitClaim.putString("event_dedupe_key", "historical-anr:v1:exit");
        assertTrue(provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                exitClaim).getBoolean("acquired"));

        Uri viewUri = FTContentProvider.getUriViewData();
        ContentValues view = new ContentValues();
        view.put(FTSQL.RUM_COLUMN_ID, "view");
        view.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
        assertNotNull(provider.insert(viewUri, view));

        Bundle count = new Bundle();
        count.putString("view_id", "view");
        count.putString("event_dedupe_key", "historical-anr:v1:exit");
        assertTrue(provider.call(
                FTContentProvider.METHOD_HISTORICAL_VIEW_ERROR_COUNT,
                null,
                count).getBoolean("applied"));

        Bundle takeover = claim("owner-two", 201, 301);
        takeover.putString("event_dedupe_key", "historical-anr:v1:exit");
        assertTrue(provider.call(
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                takeover).getBoolean("acquired"));

        assertFalse(provider.call(
                FTContentProvider.METHOD_HISTORICAL_VIEW_ERROR_COUNT,
                null,
                count).getBoolean("applied"));

        Cursor cursor = provider.query(
                viewUri,
                new String[]{FTSQL.RUM_COLUMN_ERROR_COUNT},
                FTSQL.RUM_COLUMN_ID + "=?",
                new String[]{"view"},
                null);
        try {
            assertTrue(cursor.moveToFirst());
            assertEquals(1, cursor.getInt(0));
        } finally {
            cursor.close();
        }
    }

    private Bundle claim(String owner, long nowMs, long leaseUntilMs) {
        Bundle claim = new Bundle();
        claim.putString("exit_key", "exit");
        claim.putString("owner_run_id", owner);
        claim.putLong("now_ms", nowMs);
        claim.putLong("lease_until_ms", leaseUntilMs);
        claim.putString("process_name", "com.example.app");
        claim.putInt("pid", 123);
        claim.putLong("exit_time_ms", 20_000);
        claim.putInt("reason", 6);
        claim.putString("session_id", "session");
        claim.putString("view_id", "view");
        claim.putString("view_name", "Home");
        claim.putString("collect_type", "collect_by_sample");
        claim.putString("event_dedupe_key", "dedupe");
        return claim;
    }

    private ContentValues syncValues(String uuid) {
        ContentValues values = new ContentValues();
        values.put(FTSQL.RECORD_COLUMN_TM, 1);
        values.put(FTSQL.RECORD_COLUMN_DATA_UUID, uuid);
        values.put(FTSQL.RECORD_COLUMN_DATA, uuid);
        values.put(FTSQL.RECORD_COLUMN_DATA_TYPE, "rum");
        values.put(FTSQL.RECORD_COLUMN_DEDUPE_KEY, "historical-anr:v1:key");
        return values;
    }
}
