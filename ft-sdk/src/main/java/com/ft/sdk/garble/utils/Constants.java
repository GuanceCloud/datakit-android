package com.ft.sdk.garble.utils;

import com.ft.sdk.garble.bean.OP;

import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:41
 * Description:
 */
public class Constants {
    public static final String SDK_NAME = "df_android_rum_sdk";
    public static final String FT_KEY_VALUE_NULL = "null";
    public static final String UNKNOWN = "N/A";
    public static final String FLOW_ROOT = "root";
    public static final String MEASUREMENT = "measurement";
    public static final String TIME = "time";
    public static final String FIELDS = "fields";
    public static final String TAGS = "tags";
    public static final String SEPARATION_PRINT = "--temp_separation--";
    public static final String SEPARATION = " ";
    public static final String SEPARATION_LINE_BREAK = "--line_break_temp--";//换行标志符，用于日志显示
    public static final String SEPARATION_REALLY_LINE_BREAK = "\n";//换行标志符，用于日志显示

    public static final String USER_AGENT = SDK_NAME;
    public static final String FT_LOG_DEFAULT_MEASUREMENT = "df_rum_android_log";

    public static final String FT_SDK_INIT_UUID = "ft.sdk.init.uuid";
    public static final String FT_RANDOM_USER_ID = "ft.user.session.id";
    public static final String FT_USER_USER_ID = "ft.user.userid";
    public static final String FT_USER_USER_NAME = "ft.user.username";
    public static final String FT_USER_USER_EXT = "ft.user.extdata";
    public static final String FT_SHARE_PER_FILE = "ftSDKShareFile";

    public static final String FT_MEASUREMENT_TIME_COST_CLIENT = "mobile_client_time_cost";
    public static final String FT_MEASUREMENT_MONITOR = "mobile_monitor";

    public static final String FT_MEASUREMENT_RUM_VIEW = "view";
    public static final String FT_MEASUREMENT_RUM_ERROR = "error";
    public static final String FT_MEASUREMENT_RUM_LONG_TASK = "long_task";
    public static final String FT_MEASUREMENT_RUM_RESOURCE = "resource";
    public static final String FT_MEASUREMENT_RUM_ACTION = "action";


    public static final String URL_MODEL_TRACK_INFLUX = "v1/write/metric";//指标数据上传路径
    public static final String URL_MODEL_RUM = "v1/write/rum";//
    public static final String URL_MODEL_LOG = "v1/write/logging";//日志数据上传路径
    public static final String URL_MODEL_TRACING = "v1/write/tracing";//链路上传

    public static final String DEFAULT_OBJECT_CLASS = "Mobile_Device";//默认的对象名
    public static final String DEFAULT_LOG_SERVICE_NAME = "df_rum_android";
    public static final int MAX_DB_CACHE_NUM = 5000;//数据库最大缓存容量

//    public static final String KEY_EVENT_ID = "event_id";
//    public static final String KEY_EVENT = "event";
//
//    public static final String KEY_PAGE_EVENT_CURRENT_PAGE_NAME = "current_page_name";
//    public static final String KEY_PAGE_EVENT_ROOT_PAGE_NAME = "root_page_name";
//
//    public static final String KEY_PAGE_EVENT_PAGE_DESC = "page_desc";
//    public static final String KEY_PAGE_EVENT_USER_NAME = "ud_name";

    public static final String KEY_TIME_COST_DURATION = "duration";
    public static final String KEY_STATUS = "status";

    public static final String KEY_BATTERY_TOTAL = "battery_total";
    public static final String KEY_BATTERY_CHARGE_TYPE = "battery_charge_type";
    public static final String KEY_BATTERY_STATUS = "battery_status";
    public static final String KEY_BATTERY_USE = "battery_use";

    public static final String KEY_MEMORY_TOTAL = "memory_total";
    public static final String KEY_MEMORY_USE = "memory_use";
    public static final String KEY_CPU_USE = "cpu_use";

    public static final String KEY_CPU_TICK_COUNT = "cpu_tick_count";
    public static final String KEY_CPU_TICK_COUNT_PER_SECOND = "cpu_tick_count_per_second";
    public static final String KEY_MEMORY_AVG = "memory_avg";
    public static final String KEY_MEMORY_MAX = "memory_max";
    public static final String KEY_FPS_MINI = "fps_mini";
    public static final String KEY_FPS_AVG = "fps_avg";
    public static final String KEY_BATTERY_CURRENT_AVG = "battery_current_avg";
    public static final String KEY_BATTERY_CURRENT_MAX = "battery_current_max";

    public static final String KEY_NETWORK_TYPE = "network_type";
    public static final String KEY_NETWORK_STRENGTH = "network_strength";
    public static final String KEY_NETWORK_IN_RATE = "network_in_rate";
    public static final String KEY_NETWORK_OUT_RATE = "network_out_rate";
    public static final String KEY_NETWORK_PROXY = "network_proxy";
    public static final String KEY_NETWORK_DNS = "dns";
    public static final String KEY_NETWORK_ROAM = "roam";
    public static final String KEY_NETWORK_WIFI_SSID = "wifi_ssid";
    public static final String KEY_NETWORK_WIFI_IP = "wifi_ip";
    public static final String KEY_INNER_NETWORK_TCP_TIME = "_network_tcp_time";
    public static final String KEY_INNER_NETWORK_DNS_TIME = "_network_dns_time";
    public static final String KEY_INNER_NETWORK_RESPONSE_TIME = "_network_response_time";
    public static final String KEY_NETWORK_TCP_TIME = "network_tcp_time";
    public static final String KEY_NETWORK_DNS_TIME = "network_dns_time";
    public static final String KEY_NETWORK_RESPONSE_TIME = "network_response_time";



    public static final String KEY_SERVICE = "service";
    public static final String KEY_ENV = "env";
    public static final String KEY_APPLICATION_UUID = "application_uuid";
    public static final String KEY_APP_VERSION_NAME = "version";
    public static final String KEY_SDK_NAME = "sdk_name";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_BACKENDSAMPLE = "backend_sample";

    public static final String KEY_DEVICE_UUID = "device_uuid";
    public static final String KEY_DEVICE_OS = "os";
    public static final String KEY_DEVICE_OS_VERSION = "os_version";
    public static final String KEY_DEVICE_OS_VERSION_MAJOR = "os_version_major";
    public static final String KEY_DEVICE_DEVICE_BAND = "device";
    public static final String KEY_DEVICE_DEVICE_MODEL = "model";
    public static final String KEY_DEVICE_DISPLAY = "screen_size";
    public static final String KEY_DEVICE_CARRIER = "carrier";
    public static final String KEY_DEVICE_LOCALE = "locale";

    public static final String EVENT_NAME_LAUNCH = "launch";
    public static final String EVENT_NAME_OPEN = "open";
    public static final String EVENT_NAME_CLICK = "click";
    public static final String EVENT_NAME_LEAVE = "leave";
    public static final String EVENT_NAME_ENTER = "enter";

    public static final String KEY_RUM_IS_SIGN_IN = "is_signin";
    public static final String KEY_RUM_USER_ID = "userid";
    public static final String KEY_RUM_APP_ID = "app_id";
    public static final String KEY_RUM_CUSTOM_KEYS = "custom_keys";

    public static final String KEY_RUM_RESOURCE_URL = "resource_url";
    public static final String KEY_RUM_RESOURCE_URL_HOST = "resource_url_host";
    public static final String KEY_RUM_RESOURCE_TYPE = "resource_type";
    public static final String KEY_RUM_RESPONSE_CONNECTION = "response_connection";
    public static final String KEY_RUM_RESPONSE_CONTENT_TYPE = "response_content_type";
    public static final String KEY_RUM_RESPONSE_CONTENT_ENCODING = "response_content_encoding";
    public static final String KEY_RUM_RESOURCE_METHOD = "resource_method";
    public static final String KEY_RUM_RESPONSE_HEADER = "response_header";
    public static final String KEY_RUM_REQUEST_HEADER = "request_header";
    public static final String KEY_RUM_SDK_PACKAGE_AGENT = "sdk_package_agent";
    public static final String KEY_RUM_SDK_PACKAGE_TRACK = "sdk_package_track";
    public static final String KEY_RUM_SDK_PACKAGE_NATIVE = "sdk_package_native";
    public static final String KEY_SDK_VERSION = "sdk_version";

    public static final String KEY_RUM_RESOURCE_STATUS = "resource_status";
    public static final String KEY_RUM_RESOURCE_STATUS_GROUP = "resource_status_group";
    public static final String KEY_RUM_RESOURCE_SIZE = "resource_size";
    public static final String KEY_RUM_RESOURCE_DURATION = "duration";
    public static final String KEY_RUM_RESOURCE_DNS = "resource_dns";
    public static final String KEY_RUM_RESOURCE_TCP = "resource_tcp";
    public static final String KEY_RUM_RESOURCE_SSL = "resource_ssl";
    public static final String KEY_RUM_RESOURCE_TTFB = "resource_ttfb";
    public static final String KEY_RUM_RESOURCE_FIRST_BYTE = "resource_first_byte";
    public static final String KEY_RUM_RESOURCE_SPAN_ID = "span_id";
    public static final String KEY_RUM_RESOURCE_TRACE_ID = "trace_id";
    public static final String KEY_RUM_RESOURCE_TRANS = "resource_trans";
    public static final String KEY_RUM_RESOURCE_URL_PATH = "resource_url_path";
    public static final String KEY_RUM_RESOURCE_URL_PATH_GROUP = "resource_url_path_group";
    public static final String KEY_RUM_ERROR_MESSAGE = "error_message";
    public static final String KEY_RUM_ERROR_STACK = "error_stack";
    public static final String KEY_RUM_ERROR_SOURCE = "error_source";
    public static final String KEY_RUM_ERROR_TYPE = "error_type";
    public static final String KEY_RUM_ERROR_SITUATION = "error_situation";
    public static final String KEY_RUM_LONG_TASK_DURATION = "duration";
    public static final String KEY_RUM_LONG_TASK_STACK = "long_task_stack";
    public static final String KEY_RUM_NETWORK_TYPE = "network_type";

    public static final String KEY_RUM_SESSION_ID = "session_id";
    public static final String KEY_RUM_SESSION_TYPE = "session_type";
    public static final String KEY_RUM_VIEW_ID = "view_id";
    public static final String KEY_RUM_VIEW_REFERRER = "view_referrer";
    public static final String KEY_RUM_VIEW_NAME = "view_name";
    public static final String KEY_RUM_VIEW_LOAD = "loading_time";
    public static final String KEY_RUM_VIEW_LONG_TASK_COUNT = "view_long_task_count";
    public static final String KEY_RUM_VIEW_RESOURCE_COUNT = "view_resource_count";
    public static final String KEY_RUM_VIEW_ERROR_COUNT = "view_error_count";
    public static final String KEY_RUM_VIEW_ACTION_COUNT = "view_action_count";
    public static final String KEY_RUM_VIEW_TIME_SPENT = "time_spent";
    public static final String KEY_RUM_VIEW_IS_ACTIVE = "is_active";
    public static final String KEY_RUM_VIEW_IS_WEB_VIEW = "is_web_view";

    public static final String KEY_RUM_ACTION_ID = "action_id";
    public static final String KEY_RUM_ACTION_NAME = "action_name";
    public static final String KEY_RUM_ACTION_TYPE = "action_type";
    public static final String KEY_RUM_ACTION_LONG_TASK_COUNT = "action_long_task_count";
    public static final String KEY_RUM_ACTION_RESOURCE_COUNT = "action_resource_count";
    public static final String KEY_RUM_ACTION_ERROR_COUNT = "action_error_count";
    public static final String KEY_RUM_ACTION_DURATION = "duration";


    /**
     * OP EVENT 数据对照
     */
    public final static HashMap<OP, String> OP_EVENT_MAPS = new HashMap<>();

    static {
        OP_EVENT_MAPS.put(OP.CLK, Constants.EVENT_NAME_CLICK);
    }


}
