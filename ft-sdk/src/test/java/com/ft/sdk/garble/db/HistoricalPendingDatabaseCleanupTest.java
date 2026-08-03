package com.ft.sdk.garble.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.internal.anr.historical.HistoricalAnrDataId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class HistoricalPendingDatabaseCleanupTest {
    @Test
    public void totalSizeCleanupDoesNotDeletePendingHistoricalRum() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        db.execSQL(FTSQL.FT_TABLE_SYNC_CREATE);
        String pendingType = HistoricalAnrDataId.pendingType(DataType.RUM_APP);
        db.execSQL("INSERT INTO " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                        + " (tm,uuid,data,type) VALUES (?,?,?,?)",
                new Object[]{1, "pending", "pending", pendingType});
        db.execSQL("INSERT INTO " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                        + " (tm,uuid,data,type) VALUES (?,?,?,?)",
                new Object[]{2, "visible", "visible", DataType.RUM_APP.getValue()});

        assertEquals(1, FTDBManager.deleteOldestSyncData(db, null, 1));
        assertEquals(1, count(db, pendingType));
        assertEquals(0, count(db, DataType.RUM_APP.getValue()));
        db.close();
    }

    private int count(SQLiteDatabase db, String type) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                        + " WHERE " + FTSQL.RECORD_COLUMN_DATA_TYPE + "=?",
                new String[]{type});
        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }
}
