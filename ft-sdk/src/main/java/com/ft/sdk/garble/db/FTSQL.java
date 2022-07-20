package com.ft.sdk.garble.db;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:40
 * Description:
 */
public class FTSQL {
    public static final String FT_SYNC_TABLE_NAME = "sync_data";

    public static final String RECORD_COLUMN_ID = "_id";
    public static final String RECORD_COLUMN_TM = "tm";
    public static final String RECORD_COLUMN_DATA = "data";
    public static final String RECORD_COLUMN_DATA_TYPE = "type";

    public static final String FT_TABLE_SYNC_CREATE = "CREATE TABLE if not exists " + FT_SYNC_TABLE_NAME +
            " (" +
            RECORD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RECORD_COLUMN_TM + " INTEGER," +
            RECORD_COLUMN_DATA + " TEXT," +
            RECORD_COLUMN_DATA_TYPE + " TEXT" +
            ")";

    public static final String RUM_COLUMN_ID = "id";
    public static final String RUM_COLUMN_START_TIME = "start_time";
    public static final String RUM_COLUMN_IS_CLOSE = "is_close";
    public static final String RUM_COLUMN_LONG_TASK_COUNT = "long_task_count";
    public static final String RUM_COLUMN_ERROR_COUNT = "error_count";
    public static final String RUM_COLUMN_RESOURCE_COUNT = "resource_count";
    public static final String RUM_COLUMN_PENDING_RESOURCE = "pending_resource_count";
    public static final String RUM_COLUMN_ACTION_COUNT = "action_count";

    public static final String RUM_COLUMN_SESSION_ID = "session_id";
    public static final String RUM_COLUMN_VIEW_ID = "view_id";

    public static final String RUM_COLUMN_VIEW_NAME = "view_name";
    public static final String RUM_COLUMN_VIEW_REFERRER = "view_referrer";
    public static final String RUM_COLUMN_VIEW_LOAD_TIME = "load_time";
    public static final String RUM_COLUMN_VIEW_TIME_SPENT = "time_spent";

    public static final String RUM_COLUMN_ACTION_NAME = "action_name";
    public static final String RUM_COLUMN_ACTION_TYPE = "action_type";
    public static final String RUM_COLUMN_ACTION_DURATION = "duration";
    public static final String RUM_COLUMN_EXTRA_ATTR = "extra_attr";

    public static final String FT_TABLE_ACTION = "rum_action";

    public static final String FT_TABLE_VIEW = "rum_view";

    public static final String FT_TABLE_ACTION_CREATE = "CREATE TABLE if not exists " + FT_TABLE_ACTION +
            " (" +
            RUM_COLUMN_ID + " TEXT PRIMARY KEY," +
            RUM_COLUMN_START_TIME + " BIGINT," +
            RUM_COLUMN_ACTION_DURATION + " BIGINT," +
            RUM_COLUMN_IS_CLOSE + " TEXT," +
            RUM_COLUMN_ERROR_COUNT + " INTEGER," +
            RUM_COLUMN_LONG_TASK_COUNT + " INTEGER," +
            RUM_COLUMN_RESOURCE_COUNT + " INTEGER," +
            RUM_COLUMN_PENDING_RESOURCE + " INTEGER," +
            RUM_COLUMN_SESSION_ID + " TEXT," +
            RUM_COLUMN_VIEW_NAME + " TEXT," +
            RUM_COLUMN_VIEW_ID + " TEXT," +
            RUM_COLUMN_VIEW_REFERRER + " TEXT," +
            RUM_COLUMN_ACTION_NAME + " TEXT," +
            RUM_COLUMN_ACTION_TYPE + " TEXT," +
            RUM_COLUMN_EXTRA_ATTR + " TEXT" +
            ")";
    public static final String FT_TABLE_VIEW_CREATE = "CREATE TABLE if not exists " + FT_TABLE_VIEW +
            " (" +
            RUM_COLUMN_ID + " TEXT PRIMARY KEY," +
            RUM_COLUMN_VIEW_NAME + " TEXT," +
            RUM_COLUMN_VIEW_REFERRER + " TEXT," +
            RUM_COLUMN_START_TIME + " BIGINT," +
            RUM_COLUMN_IS_CLOSE + " TEXT," +
            RUM_COLUMN_SESSION_ID + " TEXT," +
            RUM_COLUMN_ACTION_COUNT + " INTEGER," +
            RUM_COLUMN_ERROR_COUNT + " INTEGER," +
            RUM_COLUMN_LONG_TASK_COUNT + " INTEGER," +
            RUM_COLUMN_VIEW_LOAD_TIME + " BIGINT," +
            RUM_COLUMN_VIEW_TIME_SPENT + " BIGINT," +
            RUM_COLUMN_RESOURCE_COUNT + " INTEGER," +
            RUM_COLUMN_PENDING_RESOURCE + " INTEGER," +
            RUM_COLUMN_EXTRA_ATTR + " TEXT" +
            ")";


}
