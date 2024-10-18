package com.ft.sdk.garble.db;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:40
 * Description:数据库表名，字段名，以及建表 sql
 */
public class FTSQL {
    /**
     * 同步数据表,旧版本 DB version 1，2
     */
    public static final String FT_SYNC_OLD_CACHE_TABLE_NAME = "sync_data";

    /**
     * 同步数据表，db version 3
     */
    public static final String FT_SYNC_DATA_FLAT_TABLE_NAME = "sync_data_flat";

    /**
     * 自增 id
     */
    public static final String RECORD_COLUMN_ID = "_id";

    /**
     * 时间线
     */
    public static final String RECORD_COLUMN_TM = "tm";

    /**
     * 数据 UUID
     */
    public static final String RECORD_COLUMN_DATA_UUID = "uuid";

    /**
     * 数据内容，json 格式
     */
    public static final String RECORD_COLUMN_DATA = "data";
    /**
     * 数据类型
     */
    public static final String RECORD_COLUMN_DATA_TYPE = "type";

    /**
     * 同步数据建表表结构
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
     * 正在请求中的数量
     * {@link com.ft.sdk.garble.bean.ViewBean#resourceCount}
     * {@link com.ft.sdk.garble.bean.ActionBean#resourceCount}
     */
    public static final String RUM_COLUMN_PENDING_RESOURCE = "pending_resource_count";
    /**
     * 数据上传时间
     */
    public static final String RUM_DATA_UPLOAD_TIME = "data_upload_time";
    /**
     * 数据更新时间
     */
    public static final String RUM_DATA_UPDATE_TIME = "data_update_time";
    /**
     * 数据生成次数
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
     * Action 表名,{@link com.ft.sdk.garble.bean.ActionBean}
     */
    public static final String FT_TABLE_ACTION = "rum_action";

    /**
     * View 表名,{@link com.ft.sdk.garble.bean.ViewBean}
     */
    public static final String FT_TABLE_VIEW = "rum_view";

    /**
     * Action 建表表结构
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
     * View 建表表结构
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
