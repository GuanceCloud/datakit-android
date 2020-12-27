package com.ft.sdk.garble.utils;

import com.ft.sdk.garble.bean.OP;

import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:41
 * Description:
 */
public class Constants {
    public static final String FT_KEY_VALUE_NULL = "null";
    public static final String UNKNOWN = "N/A";
    public static final String FLOW_ROOT = "root";
    public static final String MEASUREMENT = "measurement";
    public static final String FIELDS = "fields";
    public static final String TAGS = "tags";
    public static final String SEPARATION_PRINT = "--temp_separation--";
    public static final String SEPARATION = " ";
    public static final String SEPARATION_LINE_BREAK = "--line_break_temp--";//换行标志符，用于日志显示
    public static final String SEPARATION_REALLY_LINE_BREAK = "\n";//换行标志符，用于日志显示

    public static final String USER_AGENT = "ft_mobile_sdk_android";
    public static final String FT_LOG_DEFAULT_MEASUREMENT = USER_AGENT;

    public static final String FT_SDK_INIT_UUID = "ft.sdk.init.uuid";
    public static final String FT_USER_SESSION_ID = "ft.user.session.id";
    public static final String FT_USER_USER_ID= "ft.user.userid";
    public static final String FT_USER_USER_NAME= "ft.user.username";
    public static final String FT_USER_USER_EXT= "ft.user.extdata";
    public static final String FT_SHARE_PER_FILE = "ftSDKShareFile";

    public static final String FT_MEASUREMENT_PAGE_EVENT = "mobile_tracker";
    public static final String FT_MEASUREMENT_TIME_COST_CLIENT = "mobile_client_time_cost";
    public static final String FT_MEASUREMENT_MONITOR = "mobile_monitor";

    public static final String FT_MEASUREMENT_RUM_INFLUX_APP_START_UP = "rum_app_startup";
    public static final String FT_MEASUREMENT_RUM_INFLUX_APP_VIEW = "rum_app_view";
    public static final String FT_MEASUREMENT_RUM_INFLUX_APP_FREEZE = "rum_app_freeze";
    public static final String FT_MEASUREMENT_RUM_INFLUX_APP_RESOURCE_PERFORMANCE = "rum_app_resource_performance";

    public static final String FT_MEASUREMENT_RUM_ES_VIEW ="view";
    public static final String FT_MEASUREMENT_RUM_ES_CRASH ="crash";
    public static final String FT_MEASUREMENT_RUM_FREEZE="freeze";
    public static final String FT_MEASUREMENT_RUM_RESOURCE="resource";

    public static final String PERFIX = "ft_parent_not_fragment";

    public static final String MOCK_SON_PAGE_DATA = "mock_son_page_data";
    public static final String SHARE_PRE_STEP_DATE = "share_pre_step_date";
    public static final String SHARE_PRE_STEP_HISTORY = "share_pre_step_history";
    public static final String SHARE_PRE_START_TIME = "share_pre_start_time";

    public static final String URL_MODEL_TRACK_INFLUX = "v1/write/metric";//指标数据上传路径
    public static final String URL_MODEL_RUM = "v1/write/rum";//
    public static final String URL_MODEL_LOG = "v1/write/logging";//日志数据上传路径
    public static final String URL_MODEL_TRACING = "v1/write/tracing";//链路上传
    public static final String URL_MODEL_OBJECT = "v1/write/object";//对象数据上传路径

    public static final String DEFAULT_OBJECT_CLASS = "Mobile_Device";//默认的对象名
    public static final String DEFAULT_LOG_SERVICE_NAME = "dataflux sdk";
    public static final int MAX_DB_CACHE_NUM = 5000;//数据库最大缓存容量

    public static final String KEY_EVENT_ID = "event_id";
    public static final String KEY_EVENT = "event";
    
    public static final String KEY_PAGE_EVENT_CURRENT_PAGE_NAME = "current_page_name";
    public static final String KEY_PAGE_EVENT_ROOT_PAGE_NAME = "root_page_name";

    public static final String KEY_PAGE_EVENT_PAGE_DESC = "page_desc";
    public static final String KEY_PAGE_EVENT_USER_NAME = "ud_name";

    public static final String KEY_TIME_COST_DURATION = "duration";

    public static final String KEY_BATTERY_TOTAL = "battery_total";
    public static final String KEY_BATTERY_CHARGE_TYPE = "battery_charge_type";
    public static final String KEY_BATTERY_STATUS = "battery_status";
    public static final String KEY_BATTERY_USE = "battery_use";

    public static final String KEY_MEMORY_TOTAL = "memory_total";
    public static final String KEY_MEMORY_USE = "memory_use";
    public static final String KEY_CPU_NO = "cpu_no";
    public static final String KEY_CPU_USE = "cpu_use";
    public static final String KEY_CPU_TEMPERATURE = "cpu_temperature";
    public static final String KEY_CPU_HZ = "cpu_hz";

    public static final String KEY_GPU_MODEL = "gpu_model";
    public static final String KEY_GPU_HZ = "gpu_hz";
    public static final String KEY_GPU_RATE = "gpu_rate";

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
//    public static final String KEY_NETWORK_ERROR_RATE = "network_error_rate";

    public static final String KEY_LOCATION_PROVINCE = "province";
    public static final String KEY_LOCATION_CITY = "city";
    public static final String KEY_LOCATION_COUNTRY = "country";
    public static final String KEY_LOCATION_LATITUDE = "latitude";
    public static final String KEY_LOCATION_LONGITUDE = "longitude";
    public static final String KEY_LOCATION_GPS_OPEN = "gps_open";

    public static final String KEY_DEVICE_OPEN_TIME = "device_open_time";
    public static final String KEY_DEVICE_NAME = "device_name";

    public static final String KEY_BT_DEVICE = "bt_device";
    public static final String KEY_BT_OPEN = "bt_open";

//    public static final String KEY_SENSOR_BRIGHTNESS = "screen_brightness";
//    public static final String KEY_SENSOR_LIGHT = "light";
//    public static final String KEY_SENSOR_PROXIMITY = "proximity";
//    public static final String KEY_SENSOR_STEPS = "steps";
//    public static final String KEY_SENSOR_ROTATION_X = "rotation_x";
//    public static final String KEY_SENSOR_ROTATION_Y = "rotation_y";
//    public static final String KEY_SENSOR_ROTATION_Z = "rotation_z";
//    public static final String KEY_SENSOR_ACCELERATION_X = "acceleration_x";
//    public static final String KEY_SENSOR_ACCELERATION_Y = "acceleration_y";
//    public static final String KEY_SENSOR_ACCELERATION_Z = "acceleration_z";
//    public static final String KEY_SENSOR_MAGNETIC_X = "magnetic_x";
//    public static final String KEY_SENSOR_MAGNETIC_Y = "magnetic_y";
//    public static final String KEY_SENSOR_MAGNETIC_Z = "magnetic_z";

    public static final String KEY_FPS = "fps";
    public static final String KEY_TORCH = "torch";

    public static final String KEY_DEVICE_UUID = "device_uuid";
    public static final String KEY_APPLICATION_UUID = "application_uuid";
    public static final String KEY_DEVICE_APPLICATION_ID = "app_identifiedid";
    public static final String KEY_DEVICE_OS = "os";
    public static final String KEY_DEVICE_OS_VERSION = "os_version";
    public static final String KEY_DEVICE_DEVICE_BAND = "device";
    public static final String KEY_DEVICE_DEVICE_MODEL = "model";
    public static final String KEY_DEVICE_DISPLAY = "screen_size";
    public static final String KEY_DEVICE_CARRIER = "carrier";
    public static final String KEY_DEVICE_LOCALE = "locale";
    public static final String KEY_DEVICE_OAID = "oaid";
    public static final String KEY_APP_VERSION_NAME = "version";

    public static final String EVENT_NAME_LAUNCH = "launch";
    public static final String EVENT_NAME_OPEN = "open";
    public static final String EVENT_NAME_CLICK = "click";
    public static final String EVENT_NAME_LEAVE = "leave";
    public static final String EVENT_NAME_ENTER = "enter";

    public static final String KEY_RUM_IS_SIGNIN = "is_signin";
    public static final String KEY_RUM_USER_ID = "userid";
    public static final String KEY_RUM_APP_ID = "app_id";

    public static final String KEY_RUM_RESOURCE_URL_HOST = "resource_url_host";
    public static final String KEY_RUM_RESOURCE_TYPE = "resource_type";
    public static final String KEY_RUM_RESPONSE_CONNECTION = "response_connection";
    public static final String KEY_RUM_RESPONSE_CONTENT_TYPE = "response_content_type";
    public static final String KEY_RUM_RESPONSE_CONTENT_ENCODING = "response_content_encoding";
    public static final String KEY_RUM_RESOURCE_METHOD = "resource_method";
    public static final String KEY_RUM_TERMINAL = "terminal";
    public static final String KEY_RUM_SDK_PACKAGE_AGENT = "sdk_package_agent";
    public static final String KEY_RUM_SDK_PACKAGE_TRACK = "sdk_package_track";
    public static final String KEY_RUM_SDK_PACKAGE_NATIVE = "sdk_package_native";
    public static final String KEY_RUM_APP_STARTUP_TYPE = "app_startup_type";
    public static final String KEY_RUM_APP_APDEX_LEVEL = "app_apdex_level";
    public static final String KEY_RUM_APP_STARTUP_DURATION = "app_startup_duration";
    public static final String KEY_RUM_VIEW_ID = "view_id";
    public static final String KEY_RUM_VIEW_PARENT = "view_parent";
    public static final String KEY_RUM_VIEW_NAME = "view_name";
    public static final String KEY_RUM_VIEW_FPS = "view_fps";
    public static final String KEY_RUM_VIEW_LOAD = "view_load";
    public static final String KEY_RUM_RESOURCE_STATUS = "resource_status";
    public static final String KEY_RUM_RESOURCE_STATUS_GROUP = "resource_status_group";
    public static final String KEY_RUM_RESOURCE_SIZE = "resource_size";
    public static final String KEY_RUM_RESOURCE_LOAD = "resource_load";
    public static final String KEY_RUM_RESOURCE_DNS = "resource_dns";
    public static final String KEY_RUM_RESOURCE_TCP = "resource_tcp";
    public static final String KEY_RUM_RESOURCE_SSL = "resource_ssl";
    public static final String KEY_RUM_RESOURCE_TTFB = "resource_ttfb";
    public static final String KEY_RUM_RESOURCE_TRANS = "resource_trans";
    public static final String KEY_RUM_RESOURCE_URL_PATH = "resource_url_path";
    public static final String KEY_RUM_CRASH_TYPE = "crash_type";
    public static final String KEY_RUM_CRASH_SITUATION = "crash_situation";
    public static final String KEY_RUM_APPLICATION_UUID = "application_uuid";
    public static final String KEY_RUM_CRASH_MESSAGE = "crash_message";
    public static final String KEY_RUM_CRASH_STACK = "crash_stack";
    public static final String KEY_RUM_FREEZE_TYPE = "freeze_type";
    public static final String KEY_RUM_FREEZE_DURATION = "freeze_duration";
    public static final String KEY_RUM_FREEZE_STACK = "freeze_stack";
    public static final String KEY_RUM_ENV = "env";
    public static final String KEY_RUM_NETWORK_TYPE = "network_type";
    public static final String KEY_RUM_ORIGIN_ID = "origin_id";



    /**
     * OP EVENT 数据对照
     */
    public final static HashMap<OP, String> OP_EVENT_MAPS = new HashMap<>();
    public static final String KEY_RUM_APPLICATION_IDENTIFIER = "application_identifier";


    static {
        OP_EVENT_MAPS.put(OP.LANC, Constants.EVENT_NAME_LAUNCH);
        OP_EVENT_MAPS.put(OP.CLK, Constants.EVENT_NAME_CLICK);
        OP_EVENT_MAPS.put(OP.CLS_FRA, Constants.EVENT_NAME_LEAVE);
        OP_EVENT_MAPS.put(OP.CLS_ACT, Constants.EVENT_NAME_LEAVE);
        OP_EVENT_MAPS.put(OP.OPEN_ACT, Constants.EVENT_NAME_ENTER);
        OP_EVENT_MAPS.put(OP.OPEN_FRA, Constants.EVENT_NAME_ENTER);
    }


    /**
     * 需要监控数据的 OP 类型
     */
    public static final OP[] MERGE_MONITOR_EVENTS = new OP[]{
            OP.LANC,
            OP.CLK,
            OP.CLS_FRA,
            OP.CLS_ACT,
            OP.OPEN_ACT,
            OP.OPEN_FRA,
    };


}
