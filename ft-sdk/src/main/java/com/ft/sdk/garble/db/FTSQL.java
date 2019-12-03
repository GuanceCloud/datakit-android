package com.ft.sdk.garble.db;

/**
 * BY huangDianHua
 * DATE:2019-12-02 10:40
 * Description:
 */
public class FTSQL {
    public static final String FT_TABLE_NAME = "ft_operation_record";
    public static final String RECORD_COLUMN_ID = "_id";
    public static final String RECORD_COLUMN_TM = "tm";
    public static final String RECORD_COLUMN_DATA = "data";

    public static final String FT_TABLE_CREATE = "CREATE TABLE " + FT_TABLE_NAME +
            " (" +
            RECORD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RECORD_COLUMN_TM + " INTEGER," +
            RECORD_COLUMN_DATA + " TEXT" +
            ")";
}
