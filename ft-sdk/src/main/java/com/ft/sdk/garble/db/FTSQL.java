package com.ft.sdk.garble.db;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:40
 * Description: Database table names, field names, and table creation SQL
 */
public class FTSQL {
    /**
     * Synchronized data table, old DB version 1, 2
     */
    public static final String FT_SYNC_OLD_CACHE_TABLE_NAME = "sync_data";

    /**
     * Synchronized data table, DB version 3
     */
    public static final String FT_SYNC_DATA_FLAT_TABLE_NAME = "sync_data_flat";

    /**
     * Auto-increment id
     */
    public static final String RECORD_COLUMN_ID = "_id";

    /**
     * Timeline
     */
    public static final String RECORD_COLUMN_TM = "tm";

    /**
     * Data UUID
     */
    public static final String RECORD_COLUMN_DATA_UUID = "uuid";

    /**
     * Data content, in JSON format
     */
    public static final String RECORD_COLUMN_DATA = "data";
    /**
     * Data type
     */
    public static final String RECORD_COLUMN_DATA_TYPE = "type";

    /**
     * Table structure for synchronized data
     */
    public static final String FT_TABLE_SYNC_CREATE = "CREATE TABLE if not exists " + FT_SYNC_DATA_FLAT_TABLE_NAME +
            " (" +
            RECORD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RECORD_COLUMN_TM + " INTEGER," +
            RECORD_COLUMN_DATA_UUID + " TEXT," +
            RECORD_COLUMN_DATA + " TEXT," +
            RECORD_COLUMN_DATA_TYPE + " TEXT" +
            ")";

    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#id}
     * {@link com.ft.sdk.garble.bean.ActionBean#id}
     */
    public static final String RUM_COLUMN_ID = "id";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#startTime}
     * {@link com.ft.sdk.garble.bean.ActionBean#startTime}
     */
    public static final String RUM_COLUMN_START_TIME = "start_time";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#isClose}
     * {@link com.ft.sdk.garble.bean.ActionBean#isClose}
     */
    public static final String RUM_COLUMN_IS_CLOSE = "is_close";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#longTaskCount}
     * {@link com.ft.sdk.garble.bean.ActionBean#longTaskCount}
     */
    public static final String RUM_COLUMN_LONG_TASK_COUNT = "long_task_count";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#errorCount}
     * {@link com.ft.sdk.garble.bean.ActionBean#errorCount}
     */
    public static final String RUM_COLUMN_ERROR_COUNT = "error_count";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#resourceCount}
     * {@link com.ft.sdk.garble.bean.ActionBean#resourceCount}
     */
    public static final String RUM_COLUMN_RESOURCE_COUNT = "resource_count";
    /**
     * Number of ongoing requests
     * {@link com.ft.sdk.garble.bean.ViewBean#resourceCount}
     * {@link com.ft.sdk.garble.bean.ActionBean#resourceCount}
     */
    public static final String RUM_COLUMN_PENDING_RESOURCE = "pending_resource_count";
    /**
     * Data upload time
     */
    public static final String RUM_DATA_UPLOAD_TIME = "data_upload_time";
    /**
     * Data update time
     */
    public static final String RUM_DATA_UPDATE_TIME = "data_update_time";
    /**
     * Number of times data is generated
     */
    public static final String RUM_VIEW_UPDATE_TIME = "view_update_time";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#actionCount}
     */
    public static final String RUM_COLUMN_ACTION_COUNT = "action_count";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#sessionId}
     * {@link com.ft.sdk.garble.bean.ActionBean#sessionId}
     */
    public static final String RUM_COLUMN_SESSION_ID = "session_id";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#id}
     */
    public static final String RUM_COLUMN_VIEW_ID = "view_id";

    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#viewName}
     */
    public static final String RUM_COLUMN_VIEW_NAME = "view_name";
    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#viewReferrer}
     */
    public static final String RUM_COLUMN_VIEW_REFERRER = "view_referrer";

    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#loadTime}
     */
    public static final String RUM_COLUMN_VIEW_LOAD_TIME = "load_time";

    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#timeSpent}
     */
    public static final String RUM_COLUMN_VIEW_TIME_SPENT = "time_spent";

    /**
     * {@link com.ft.sdk.garble.bean.ActionBean#actionName}
     */
    public static final String RUM_COLUMN_ACTION_NAME = "action_name";

    /**
     * {@link com.ft.sdk.garble.bean.ActionBean#actionType}
     */
    public static final String RUM_COLUMN_ACTION_TYPE = "action_type";
    /**
     * {@link com.ft.sdk.garble.bean.ActionBean#duration}
     */
    public static final String RUM_COLUMN_ACTION_DURATION = "duration";

    /**
     * {@link com.ft.sdk.garble.bean.ViewBean#getAttrJsonString()}
     */
    public static final String RUM_COLUMN_EXTRA_ATTR = "extra_attr";

    /**
     * Action table name,{@link com.ft.sdk.garble.bean.ActionBean}
     */
    public static final String FT_TABLE_ACTION = "rum_action";

    /**
     * View table name,{@link com.ft.sdk.garble.bean.ViewBean}
     */
    public static final String FT_TABLE_VIEW = "rum_view";

    /**
     * Table structure for Action
     */
    public static final String FT_TABLE_ACTION_CREATE = "CREATE TABLE if not exists " + FT_TABLE_ACTION +
            " (" +
            RUM_COLUMN_ID + " TEXT PRIMARY KEY," +
            RUM_COLUMN_START_TIME + " BIGINT," +
            RUM_COLUMN_ACTION_DURATION + " BIGINT," +
            RUM_COLUMN_IS_CLOSE + " INTEGER," +
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
    /**
     * Table structure for View
     */
    public static final String FT_TABLE_VIEW_CREATE = "CREATE TABLE if not exists " + FT_TABLE_VIEW +
            " (" +
            RUM_COLUMN_ID + " TEXT PRIMARY KEY," +
            RUM_COLUMN_VIEW_NAME + " TEXT," +
            RUM_COLUMN_VIEW_REFERRER + " TEXT," +
            RUM_COLUMN_START_TIME + " BIGINT," +
            RUM_COLUMN_IS_CLOSE + " INTEGER," +
            RUM_COLUMN_SESSION_ID + " TEXT," +
            RUM_COLUMN_ACTION_COUNT + " INTEGER," +
            RUM_COLUMN_ERROR_COUNT + " INTEGER," +
            RUM_COLUMN_LONG_TASK_COUNT + " INTEGER," +
            RUM_COLUMN_VIEW_LOAD_TIME + " BIGINT," +
            RUM_COLUMN_VIEW_TIME_SPENT + " BIGINT," +
            RUM_COLUMN_RESOURCE_COUNT + " INTEGER," +
            RUM_COLUMN_PENDING_RESOURCE + " INTEGER," +
            RUM_DATA_UPDATE_TIME + " BIGINT DEFAULT 0," +
            RUM_DATA_UPLOAD_TIME + " BIGINT DEFAULT 0," +
            RUM_VIEW_UPDATE_TIME + " BIGINT DEFAULT 1," +
            RUM_COLUMN_EXTRA_ATTR + " TEXT" +
            ")";


}
