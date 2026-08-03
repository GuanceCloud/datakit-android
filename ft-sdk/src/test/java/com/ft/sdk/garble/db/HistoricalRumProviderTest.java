package com.ft.sdk.garble.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.internal.anr.historical.HistoricalAnrDataId;

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
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class HistoricalRumProviderTest {
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
    public void historicalRumInsertAndViewCountAreAtomicAndIdempotent() {
        Uri viewUri = FTContentProvider.getUriViewData();
        ContentValues view = new ContentValues();
        view.put(FTSQL.RUM_COLUMN_ID, "view");
        view.put(FTSQL.RUM_COLUMN_ERROR_COUNT, 0);
        assertNotNull(provider.insert(viewUri, view));

        Bundle first = provider.call(
                FTContentProvider.METHOD_HISTORICAL_RUM_PERSIST,
                null,
                historicalRum("first-uuid", "historical-anr:v1:key", "first"));
        assertNotNull(first);
        assertTrue(first.getBoolean("success"));
        assertTrue(first.getBoolean("inserted"));

        Bundle duplicate = provider.call(
                FTContentProvider.METHOD_HISTORICAL_RUM_PERSIST,
                null,
                historicalRum("second-uuid", "historical-anr:v1:key", "second"));
        assertNotNull(duplicate);
        assertTrue(duplicate.getBoolean("success"));
        assertFalse(duplicate.getBoolean("inserted"));

        Uri syncUri = FTContentProvider.getUriSyncDataFlat();
        Cursor hidden = provider.query(
                syncUri,
                new String[]{FTSQL.RECORD_COLUMN_ID},
                FTSQL.RECORD_COLUMN_DATA_TYPE + "=?",
                new String[]{DataType.RUM_APP.getValue()},
                null);
        try {
            assertFalse(hidden.moveToFirst());
        } finally {
            hidden.close();
        }

        Bundle committed = provider.call(
                FTContentProvider.METHOD_HISTORICAL_RUM_COMMIT,
                null,
                historicalRumCommit());
        assertNotNull(committed);
        assertTrue(committed.getBoolean("success"));
        assertTrue(committed.getBoolean("promoted"));

        Cursor sync = provider.query(
                syncUri,
                new String[]{FTSQL.RECORD_COLUMN_DATA},
                FTSQL.RECORD_COLUMN_DATA_UUID + "=?",
                new String[]{HistoricalAnrDataId.fromDedupeKey("historical-anr:v1:key")},
                null);
        try {
            assertTrue(sync.moveToFirst());
            assertEquals("first", sync.getString(0));
            assertFalse(sync.moveToNext());
        } finally {
            sync.close();
        }

        Cursor countedView = provider.query(
                viewUri,
                new String[]{FTSQL.RUM_COLUMN_ERROR_COUNT},
                FTSQL.RUM_COLUMN_ID + "=?",
                new String[]{"view"},
                null);
        try {
            assertTrue(countedView.moveToFirst());
            assertEquals(1, countedView.getInt(0));
        } finally {
            countedView.close();
        }
    }

    private Bundle historicalRum(String uuid, String dedupeKey, String body) {
        Bundle values = new Bundle();
        values.putLong(FTSQL.RECORD_COLUMN_TM, 1);
        values.putString(FTSQL.RECORD_COLUMN_DATA_UUID, uuid);
        values.putString(FTSQL.RECORD_COLUMN_DATA, body);
        values.putString(FTSQL.RECORD_COLUMN_DATA_TYPE,
                HistoricalAnrDataId.pendingType(DataType.RUM_APP));
        values.putString(FTSQL.RECORD_COLUMN_DEDUPE_KEY, dedupeKey);
        values.putString(FTSQL.RUM_COLUMN_VIEW_ID, "view");
        return values;
    }

    private Bundle historicalRumCommit() {
        Bundle values = new Bundle();
        values.putString(
                FTSQL.RECORD_COLUMN_DATA_UUID,
                HistoricalAnrDataId.fromDedupeKey("historical-anr:v1:key"));
        values.putString("pending_type",
                HistoricalAnrDataId.pendingType(DataType.RUM_APP));
        values.putString(
                FTSQL.RECORD_COLUMN_DATA_TYPE,
                DataType.RUM_APP.getValue());
        return values;
    }
}
