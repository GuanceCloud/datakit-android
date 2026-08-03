package com.ft.sdk.internal.anr.historical;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBConfig;
import com.ft.sdk.garble.db.FTSQL;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.sdk.garble.db.file.FTFileStorePaths;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("deprecation")
public final class HistoricalAnrV4Migration {
    private HistoricalAnrV4Migration() {
    }

    public static void migrate(Context context, SQLiteDatabase db) throws Exception {
        if (context == null || db == null) {
            return;
        }
        FTFileStorePaths paths = new FTFileStorePaths(context);
        migrateViewContexts(db, new FileHistoricalRumContextStore(paths));
        migrateClaims(db, new FileHistoricalExitLedger(paths));
        migrateStableUuids(db);
    }

    static void ensureDatabaseMigrated(Context context) throws Exception {
        if (context == null
                || !context.getDatabasePath(FTDBConfig.DATABASE_NAME).exists()) {
            return;
        }
        DatabaseHelper helper = new DatabaseHelper(
                context, FTDBConfig.DATABASE_NAME, FTDBConfig.DATABASE_VERSION);
        try {
            helper.getWritableDatabase();
        } finally {
            helper.close();
        }
    }

    private static void migrateViewContexts(
            SQLiteDatabase db, FileHistoricalRumContextStore store) throws Exception {
        if (!hasColumn(db, FTSQL.FT_TABLE_VIEW, FTSQL.RUM_COLUMN_PROCESS_RUN_ID)) {
            return;
        }
        Cursor cursor = db.query(
                FTSQL.FT_TABLE_VIEW,
                new String[]{
                        FTSQL.RUM_COLUMN_ID,
                        FTSQL.RUM_COLUMN_VIEW_NAME,
                        FTSQL.RUM_COLUMN_START_TIME,
                        FTSQL.RUM_COLUMN_IS_CLOSE,
                        FTSQL.RUM_COLUMN_SESSION_ID,
                        FTSQL.RUM_COLUMN_VIEW_TIME_SPENT,
                        FTSQL.RUM_COLUMN_EXTRA_ATTR,
                        FTSQL.RUM_COLUMN_PROCESS_NAME,
                        FTSQL.RUM_COLUMN_PROCESS_RUN_ID,
                        FTSQL.RUM_COLUMN_PROCESS_START_MS
                },
                FTSQL.RUM_COLUMN_PROCESS_RUN_ID + " IS NOT NULL",
                null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                ViewBean view = new ViewBean();
                view.setId(cursor.getString(0));
                view.setViewName(cursor.getString(1));
                view.setStartTime(cursor.getLong(2));
                view.setClose(cursor.getInt(3) != 0);
                view.setSessionId(cursor.getString(4));
                view.setTimeSpent(cursor.getLong(5));
                String attributes = cursor.getString(6);
                if (attributes != null && attributes.length() > 0) {
                    view.setFromAttrJsonString(attributes);
                }
                view.setProcessName(cursor.getString(7));
                view.setProcessRunId(cursor.getString(8));
                view.setProcessStartMs(cursor.getLong(9));
                store.saveOrThrow(view);
            }
        } finally {
            cursor.close();
        }
    }

    private static void migrateClaims(
            SQLiteDatabase db, FileHistoricalExitLedger ledger) throws Exception {
        if (!tableExists(db, FTSQL.FT_HISTORICAL_EXIT_CLAIM_TABLE)) {
            return;
        }
        Cursor cursor = db.query(
                FTSQL.FT_HISTORICAL_EXIT_CLAIM_TABLE,
                null, null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                JSONObject claim = new JSONObject();
                putString(cursor, claim, "exit_key");
                putLong(cursor, claim, "state");
                putString(cursor, claim, "owner_run_id");
                putLong(cursor, claim, "lease_until_ms");
                putLong(cursor, claim, "attempt_count");
                putString(cursor, claim, "process_name");
                putLong(cursor, claim, "pid");
                putLong(cursor, claim, "exit_time_ms");
                putLong(cursor, claim, "reason");
                putString(cursor, claim, "session_id");
                putString(cursor, claim, "view_id");
                putString(cursor, claim, "view_name");
                putString(cursor, claim, "collect_type");
                putString(cursor, claim, "event_dedupe_key");
                putLong(cursor, claim, "view_error_counted");
                putString(cursor, claim, "drop_reason");
                putLong(cursor, claim, "updated_at_ms");
                ledger.importV4Claim(claim);
            }
        } finally {
            cursor.close();
        }
    }

    private static void migrateStableUuids(SQLiteDatabase db) {
        if (!hasColumn(db, FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                FTSQL.RECORD_COLUMN_DEDUPE_KEY)) {
            return;
        }
        Map<String, Integer> claimStates = loadClaimStates(db);
        Cursor cursor = db.query(
                FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME,
                new String[]{
                        FTSQL.RECORD_COLUMN_ID,
                        FTSQL.RECORD_COLUMN_DATA_UUID,
                        FTSQL.RECORD_COLUMN_DATA,
                        FTSQL.RECORD_COLUMN_DEDUPE_KEY,
                        FTSQL.RECORD_COLUMN_DATA_TYPE
                },
                FTSQL.RECORD_COLUMN_DEDUPE_KEY + " IS NOT NULL",
                null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String oldUuid = cursor.getString(1);
                String dedupeKey = cursor.getString(3);
                String storedType = cursor.getString(4);
                String stableUuid = HistoricalAnrDataId.fromDedupeKey(dedupeKey);
                if (oldUuid == null || stableUuid == null) {
                    continue;
                }
                String rewritten = cursor.getString(2);
                if (!stableUuid.equals(oldUuid) && rewritten != null) {
                    SyncData data = new SyncData(DataType.RUM_APP);
                    data.setUuid(oldUuid);
                    data.setDataString(rewritten);
                    rewritten = data.getDataString(stableUuid);
                }
                String migratedType = pendingTypeForClaim(
                        storedType, claimStates.get(dedupeKey));
                if (stableUuid.equals(oldUuid)
                        && (storedType == null || storedType.equals(migratedType))) {
                    continue;
                }
                db.execSQL(
                        "UPDATE " + FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME
                                + " SET " + FTSQL.RECORD_COLUMN_DATA_UUID + "=?,"
                                + FTSQL.RECORD_COLUMN_DATA + "=?,"
                                + FTSQL.RECORD_COLUMN_DATA_TYPE + "=? WHERE "
                                + FTSQL.RECORD_COLUMN_ID + "=?",
                        new Object[]{
                                stableUuid, rewritten, migratedType, cursor.getLong(0)});
            }
        } finally {
            cursor.close();
        }
    }

    private static Map<String, Integer> loadClaimStates(SQLiteDatabase db) {
        Map<String, Integer> states = new HashMap<>();
        if (!tableExists(db, FTSQL.FT_HISTORICAL_EXIT_CLAIM_TABLE)) {
            return states;
        }
        Cursor cursor = db.query(
                FTSQL.FT_HISTORICAL_EXIT_CLAIM_TABLE,
                new String[]{"event_dedupe_key", "state"},
                "event_dedupe_key IS NOT NULL",
                null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                states.put(cursor.getString(0), cursor.getInt(1));
            }
        } finally {
            cursor.close();
        }
        return states;
    }

    private static String pendingTypeForClaim(String storedType, Integer claimState) {
        if (claimState == null
                || (claimState != FTSQL.HISTORICAL_EXIT_STATE_CLAIMED
                && claimState != FTSQL.HISTORICAL_EXIT_STATE_RETRY)) {
            return storedType;
        }
        for (DataType type : DataType.values()) {
            if (type.getValue().equals(storedType)) {
                String pendingType = HistoricalAnrDataId.pendingType(type);
                return pendingType == null ? storedType : pendingType;
            }
        }
        return storedType;
    }

    private static void putString(Cursor cursor, JSONObject target, String column)
            throws Exception {
        int index = cursor.getColumnIndex(column);
        if (index >= 0 && !cursor.isNull(index)) {
            target.put(column, cursor.getString(index));
        }
    }

    private static void putLong(Cursor cursor, JSONObject target, String column)
            throws Exception {
        int index = cursor.getColumnIndex(column);
        if (index >= 0 && !cursor.isNull(index)) {
            target.put(column, cursor.getLong(index));
        }
    }

    private static boolean hasColumn(SQLiteDatabase db, String table, String column) {
        if (!tableExists(db, table)) {
            return false;
        }
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

    private static boolean tableExists(SQLiteDatabase db, String table) {
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
