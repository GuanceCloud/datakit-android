package com.ft.sdk.garble.utils;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:41
 * Description:
 */
public class Constants {
    public static final String FT_SDK_INIT_UUID = "ft.sdk.init.uuid";
    public static final String FT_USER_SESSION_ID = "ft.user.session.id";
    public static final String FT_SHARE_PER_FILE = "ftSDKShareFile";
    public static final String FT_KEY_VALUE_NULL = "null";
    public static final String FT_DEFAULT_MEASUREMENT = "mobile_tracker";
    public static final String FT_MONITOR_MEASUREMENT = "mobile_monitor";
    public static final String USER_AGENT= "ft_mobile_sdk_android";
    public static final String UNKNOWN = "N/A";
    public static final String FLOW_ROOT = "root";
    public static final String MEASUREMENT= "measurement";
    public static final String FIELDS= "fields";
    public static final String TAGS= "tags";
    public static final String SEPARATION_PRINT = "--temp_separation--";
    public static final String SEPARATION = " ";
    public static final String SEPARATION_LINE_BREAK = "--line_break_temp--";//换行标志符，用于日志显示
    public static final String SEPARATION_REALLY_LINE_BREAK = "\n";//换行标志符，用于日志显示
    public static final String PERFIX = "ft_parent_not_fragment";
    public static final String MOCK_SON_PAGE_DATA = "mock_son_page_data";
    public static final String SHARE_PRE_STEP_DATE = "share_pre_step_date";
    public static final String SHARE_PRE_STEP_HISTORY = "share_pre_step_history";

    public static final String URL_MODEL_TRACK = "v1/write/metrics";//指标数据上传路径
    public static final String URL_MODEL_LOG = "v1/write/logging";//日志数据上传路径
    public static final String URL_MODEL_KEY_EVENT = "v1/write/keyevent";//事件数据上传路径
    public static final String URL_MODEL_OBJECT = "v1/write/object";//对象数据上传路径
    public static final String URL_MODEL_TOKEN_CHECK = "v1/check/token";//验证token是否合法

    public static final String DEFAULT_OBJECT_CLASS = "Dataflux Android SDK";//默认的对象名
    public static final String DEFAULT_LOG_SERVICE_NAME = "dataflux sdk";
    public static final int MAX_DB_CACHE_NUM = 5000;//数据库最大缓存容量

}
