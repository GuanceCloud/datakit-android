package com.ft.sdk.garble.db;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:40
 * Description:
 */
public class FTSQL {
    public static final String FT_TABLE_NAME = "sync_data";
    public static final String RECORD_COLUMN_ID = "_id";
    public static final String RECORD_COLUMN_TM = "tm";
    public static final String RECORD_COLUMN_DATA = "data";
    public static final String RECORD_COLUMN_SESSION_ID = "session_id";
    public static final String RECORD_COLUMN_DATA_TYPE = "type";

    public static final String FT_TABLE_CREATE = "CREATE TABLE if not exists " + FT_TABLE_NAME +
            " (" +
            RECORD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RECORD_COLUMN_TM + " INTEGER," +
            RECORD_COLUMN_DATA + " TEXT," +
            RECORD_COLUMN_SESSION_ID + " TEXT,"+
            RECORD_COLUMN_DATA_TYPE + " TEXT"+
            ")";

    public static final String FT_TABLE_USER_DATA = "user_session_data";
    public static final String USER_COLUMN_SESSION_ID = "session_id";
    public static final String USER_COLUMN_DATA = "user_data";

    public static final String FT_TABLE_USER_DATA_CREATE = "CREATE TABLE if not exists "+FT_TABLE_USER_DATA +
            " (" +
            USER_COLUMN_DATA + " TEXT,"+
            USER_COLUMN_SESSION_ID + " TEXT"+
            ")";
}
