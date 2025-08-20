package com.ft.sdk.garble.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.db.base.DataBaseCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

/**
 * @date 2025-08-17
 * @description FTContentProvider is a content provider for the FT SDK.
 * It provides a unified interface for accessing the database.
 */
public class FTContentProvider extends ContentProvider {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTContentProvider";
    public static final String PACKAGE_AUTHORITY_SUBFIX = "com.ft.sdk.provider";

    // URI path definitions
    public static final String PATH_SYNC_DATA = "sync_data";
    public static final String PATH_SYNC_DATA_FLAT = "sync_data_flat";
    public static final String PATH_VIEW_DATA = "view_data";
    public static final String PATH_ACTION_DATA = "action_data";

    // Special operation paths
    public static final String PATH_EXEC_SQL = "exec_sql";
    public static final String PATH_EXEC_SQL_BATCH = "exec_sql_batch";

    // Call method name constants
    public static final String METHOD_EXEC_SQL = "execSQL";
    public static final String METHOD_EXEC_SQL_BATCH = "execSQLBatch";

    // Complete URI constants
    private static volatile String authority;
    private static volatile Uri URI_SYNC_DATA;
    private static volatile Uri URI_SYNC_DATA_FLAT;
    private static volatile Uri URI_VIEW_DATA;
    private static volatile Uri URI_ACTION_DATA;

    // URI match codes
    private static final int SYNC_DATA = 1;
    private static final int SYNC_DATA_FLAT = 2;
    private static final int VIEW_DATA = 3;
    private static final int ACTION_DATA = 4;
    private static final int EXEC_SQL = 5;
    private static final int EXEC_SQL_BATCH = 6;

    private UriMatcher uriMatcher;
    private FTDBManager dbManager;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        Context context = getContext();
        if (context != null) {
            initContentUri(context);

            uriMatcher.addURI(authority, PATH_SYNC_DATA, SYNC_DATA);
            uriMatcher.addURI(authority, PATH_SYNC_DATA_FLAT, SYNC_DATA_FLAT);
            uriMatcher.addURI(authority, PATH_VIEW_DATA, VIEW_DATA);
            uriMatcher.addURI(authority, PATH_ACTION_DATA, ACTION_DATA);
            uriMatcher.addURI(authority, PATH_EXEC_SQL, EXEC_SQL);
            uriMatcher.addURI(authority, PATH_EXEC_SQL_BATCH, EXEC_SQL_BATCH);
        }
        // Use FTDBManager instead of directly using DatabaseHelper
        dbManager = FTDBManager.get();
        return true;
    }

    static void initContentUri(Context context) {
        authority = ProviderHelper.getAuthority(context);
        URI_SYNC_DATA = Uri.parse("content://" + authority + "/" + PATH_SYNC_DATA);
        URI_SYNC_DATA_FLAT = Uri.parse("content://" + authority + "/" + PATH_SYNC_DATA_FLAT);
        URI_VIEW_DATA = Uri.parse("content://" + authority + "/" + PATH_VIEW_DATA);
        URI_ACTION_DATA = Uri.parse("content://" + authority + "/" + PATH_ACTION_DATA);
    }

    public static Uri getUriSyncData() {
        ensureUriInitialized();
        return URI_SYNC_DATA;
    }

    public static Uri getUriSyncDataFlat() {
        ensureUriInitialized();
        return URI_SYNC_DATA_FLAT;
    }

    public static Uri getUriViewData() {
        ensureUriInitialized();
        return URI_VIEW_DATA;
    }

    public static Uri getUriActionData() {
        ensureUriInitialized();
        return URI_ACTION_DATA;
    }

    private static synchronized void ensureUriInitialized() {
        if (Utils.isNullOrEmpty(authority)) {
            //if uri not initialized, use default authority
            initContentUri(FTApplication.getApplication());
        }
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        switch (uriMatcher.match(uri)) {
            case SYNC_DATA:
            case SYNC_DATA_FLAT:
            case VIEW_DATA:
            case ACTION_DATA:
            default:
                return handleStandardQuery(uri, projection, selection, selectionArgs, sortOrder);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final String tableName = getTableName(uri);
        if (tableName == null) {
            return null;
        }

        final long[] resultId = new long[1];
        final ContentValues finalValues = values;

        try {
            // Use DBManager's thread-safe mechanism for write operations
            dbManager.getDB(true, new DataBaseCallBack() {
                @Override
                public void run(SQLiteDatabase db) {
                    try {
                        resultId[0] = db.insert(tableName, null, finalValues);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Insert failed: " + e.getMessage());
                        resultId[0] = -1;
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Insert operation failed: " + e.getMessage());
            return null;
        }

        if (resultId[0] > 0) {
            return Uri.withAppendedPath(uri, String.valueOf(resultId[0]));
        }
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final String tableName = getTableName(uri);
        if (tableName == null) {
            return 0;
        }

        final int[] resultCount = new int[1];
        final ContentValues finalValues = values;
        final String finalSelection = selection;
        final String[] finalSelectionArgs = selectionArgs;

        try {
            // Use DBManager's thread-safe mechanism for write operations
            dbManager.getDB(true, new DataBaseCallBack() {
                @Override
                public void run(SQLiteDatabase db) {
                    try {
                        resultCount[0] = db.update(tableName, finalValues, finalSelection, finalSelectionArgs);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Update failed: " + e.getMessage());
                        resultCount[0] = 0;
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Update operation failed: " + e.getMessage());
            return 0;
        }

        return resultCount[0];
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final String tableName = getTableName(uri);
        if (tableName == null) {
            return 0;
        }

        final int[] resultCount = new int[1];
        final String finalSelection = selection;
        final String[] finalSelectionArgs = selectionArgs;

        try {
            // Use DBManager's thread-safe mechanism for write operations
            dbManager.getDB(true, new DataBaseCallBack() {
                @Override
                public void run(SQLiteDatabase db) {
                    try {
                        resultCount[0] = db.delete(tableName, finalSelection, finalSelectionArgs);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Delete failed: " + e.getMessage());
                        resultCount[0] = 0;
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Delete operation failed: " + e.getMessage());
            return 0;
        }

        return resultCount[0];
    }

    /**
     * Handle standard query operations
     */
    private Cursor handleStandardQuery(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder) {
        final String tableName = getTableName(uri);
        if (tableName == null) {
            return null;
        }

        final Cursor[] resultCursor = new Cursor[1];
        final String[] finalProjection = projection;
        final String finalSelection = selection;
        final String[] finalSelectionArgs = selectionArgs;
        final String finalSortOrder = sortOrder;

        try {
            dbManager.getDB(false, new DataBaseCallBack() {
                @Override
                public void run(SQLiteDatabase db) {
                    try {
                        resultCursor[0] = db.query(tableName, finalProjection, finalSelection,
                                finalSelectionArgs, null, null, finalSortOrder);
                    } catch (Exception e) {
                        if (e.getMessage() != null && e.getMessage().contains("no such table: sync_data")) {
                            LogUtils.d(TAG, "There is no old cache in 'sync_data', ignore this error");
                        } else {
                            LogUtils.e(TAG, "Query failed: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Query operation failed: " + e.getMessage());
        }

        return resultCursor[0];
    }


    /**
     * Implement execSQL operations through call method
     */
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        switch (method) {
            case METHOD_EXEC_SQL:
                return handleExecSQL(arg, extras);
            case METHOD_EXEC_SQL_BATCH:
                return handleExecSQLBatch(arg, extras);
            default:
                return super.call(method, arg, extras);
        }
    }

    /**
     * Handle execSQL calls
     */
    private Bundle handleExecSQL(String arg, Bundle extras) {
        Bundle result = new Bundle();

        try {
            if (extras != null) {
                String sql = extras.getString("sql");
                Parcelable[] bindArgs = extras.getParcelableArray("bind_args");

                if (sql != null && !sql.trim().isEmpty()) {
                    final String finalSql = sql;
                    final Parcelable[] finalBindArgs = bindArgs;

                    dbManager.getDB(true, new DataBaseCallBack() {
                        @Override
                        public void run(SQLiteDatabase db) {
                            try {
                                if (finalBindArgs != null && finalBindArgs.length > 0) {
                                    // Convert Parcelable[] to Object[]
                                    Object[] objectArgs = new Object[finalBindArgs.length];
                                    for (int i = 0; i < finalBindArgs.length; i++) {
                                        objectArgs[i] = finalBindArgs[i];
                                    }
                                    db.execSQL(finalSql, objectArgs);
                                } else {
                                    db.execSQL(finalSql);
                                }
                                //LogUtils.d(TAG, "ExecSQL executed successfully: " + finalSql);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ExecSQL failed: " + finalSql + ", error: " + e.getMessage());
                                throw e;
                            }
                        }
                    });

                    result.putBoolean("success", true);
                    result.putString("message", "ExecSQL executed successfully");
                } else {
                    result.putBoolean("success", false);
                    result.putString("message", "SQL statement is null or empty");
                }
            } else {
                result.putBoolean("success", false);
                result.putString("message", "Extras bundle is null");
            }
        } catch (Exception e) {
            result.putBoolean("success", false);
            result.putString("message", "ExecSQL failed: " + e.getMessage());
            LogUtils.e(TAG, "ExecSQL operation failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Handle execSQLBatch calls
     */
    private Bundle handleExecSQLBatch(String arg, Bundle extras) {
        Bundle result = new Bundle();

        try {
            if (extras != null) {
                String[] sqlArray = extras.getStringArray("sql_array");
                Parcelable[] bindArgsArray = extras.getParcelableArray("bind_args_array");

                if (sqlArray != null && sqlArray.length > 0) {
                    final String[] finalSqlArray = sqlArray;
                    final Parcelable[] finalBindArgsArray = bindArgsArray;

                    dbManager.getDB(true, new DataBaseCallBack() {
                        @Override
                        public void run(SQLiteDatabase db) {
                            try {
                                for (int i = 0; i < finalSqlArray.length; i++) {
                                    String sql = finalSqlArray[i];
                                    Object[] bindArgs = null;

                                    // Handle binding parameter arrays
                                    if (finalBindArgsArray != null && i < finalBindArgsArray.length) {
                                        // Get corresponding parameter array directly from Bundle
                                        String key = "bind_args_" + i;
                                        if (extras.containsKey(key)) {
                                            Object[] array = extras.getParcelableArray(key);
                                            if (array != null) {
                                                bindArgs = array;
                                            }
                                        }
                                    }

                                    if (sql != null && !sql.trim().isEmpty()) {
                                        if (bindArgs != null && bindArgs.length > 0) {
                                            db.execSQL(sql, bindArgs);
                                        } else {
                                            db.execSQL(sql);
                                        }
                                    }
                                }
                                //LogUtils.d(TAG, "Batch ExecSQL executed successfully, count: " + finalSqlArray.length);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "Batch ExecSQL failed: " + e.getMessage());
                                throw e;
                            }
                        }
                    });

                    result.putBoolean("success", true);
                    result.putString("message", "Batch ExecSQL executed successfully");
                    result.putInt("executed_count", sqlArray.length);
                } else {
                    result.putBoolean("success", false);
                    result.putString("message", "SQL array is null or empty");
                }
            } else {
                result.putBoolean("success", false);
                result.putString("message", "Extras bundle is null");
            }
        } catch (Exception e) {
            result.putBoolean("success", false);
            result.putString("message", "Batch ExecSQL failed: " + e.getMessage());
            LogUtils.e(TAG, "Batch ExecSQL operation failed: " + e.getMessage());
        }

        return result;
    }

    private String getTableName(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SYNC_DATA:
                return FTSQL.FT_SYNC_OLD_CACHE_TABLE_NAME;
            case SYNC_DATA_FLAT:
                return FTSQL.FT_SYNC_DATA_FLAT_TABLE_NAME;
            case VIEW_DATA:
                return FTSQL.FT_TABLE_VIEW;
            case ACTION_DATA:
                return FTSQL.FT_TABLE_ACTION;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
