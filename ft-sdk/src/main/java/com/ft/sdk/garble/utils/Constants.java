package com.ft.sdk.garble.utils;

import android.os.Build;

import com.ft.sdk.garble.bean.ErrorType;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:41
 * Description: SDK constant declarations
 */
public class Constants {

    public static final String LOG_TAG_PREFIX = "[FT-SDK]";
    /**
     * SDK name
     */
    public static final String SDK_NAME = "df_android_rum_sdk";

    /**
     * Data synchronization link propagation header key
     */
    public static final String SYNC_DATA_TRACE_HEADER = "X-Pkg-Id";

    /**
     * Data synchronization link propagation header value
     */
    public static final String SYNC_DATA_TRACE_HEADER_FORMAT = "rumm-%s";
    /**
     *
     */
    public static final String SYNC_DATA_CONTENT_TYPE_HEADER = "Content-Type";
    public static final String SYNC_DATA_USER_AGENT_HEADER = "User-Agent";
    public static final String SYNC_DATA_CONTENT_ENCODING_HEADER = "Content-Encoding";
    public static final String SYNC_DATA_CONTENT_TYPE_VALUE = "text/plain";
    /**
     * Illegal value
     */
    public static final String FT_KEY_VALUE_NULL = "null";

    /**
     * Line protocol, measurement
     */
    public static final String MEASUREMENT = "measurement";
    /**
     * Line protocol time
     */
    public static final String TIME = "time";
    /**
     * All measurement data in line protocol
     */
    public static final String FIELDS = "fields";
    /**
     * All tag data in line protocol
     */
    public static final String TAGS = "tags";

    /**
     * Final data separator
     */
    public static final String SEPARATION = " ";
    /**
     * Final data line protocol single data separator
     */
    public static final String SEPARATION_REAL_LINE_BREAK = "\n";//Line break symbol, used for log display

    /**
     * Synchronized data http request header user-agent
     */
    public static final String USER_AGENT = "DF-RUM-Android";

    /**
     * Log default measurement name
     */
    public static final String FT_LOG_DEFAULT_MEASUREMENT = "df_rum_android_log";

    /**
     * Store {@link  android.content.SharedPreferences} {@link #KEY_RUM_SESSION_ID} key value
     */
    public static final String FT_RANDOM_USER_ID = "ft.user.session.id";
    /**
     * Store {@link  android.content.SharedPreferences} {@link #KEY_RUM_USER_ID} key value
     */
    public static final String FT_USER_USER_ID = "ft.user.userid";
    /**
     * Store {@link  android.content.SharedPreferences} {@link #KEY_RUM_USER_NAME} key value
     */
    public static final String FT_USER_USER_NAME = "ft.user.username";
    /**
     * Store {@link  android.content.SharedPreferences} {@link #FT_USER_USER_EMAIL} key value
     */
    public static final String FT_USER_USER_EMAIL = "ft.user.email";
    /**
     * Store {@link  android.content.SharedPreferences} {@link #FT_USER_USER_EXT} key value
     */
    public static final String FT_USER_USER_EXT = "ft.user.extdata";
    /**
     *
     */
    public static final String FT_REMOTE_CONFIG = "ft.localCache.remoteConfig";
    /**
     *
     */
    public static final String FT_REMOTE_CONFIG_FETCH_TIME = "ft.localCache.remoteConfigFetchTime";
    /**
     * Store error occurrence time
     */
    public static final String FT_RUM_ERROR_TIMELINE = "ft.error.timeline";

    /**
     * Store {@link  android.content.SharedPreferences} data storage key value
     */

    public static final String FT_SHARE_PER_FILE = "ftSDKShareFile";

    /**
     * Page data measurement, temporarily refers to Activity lifecycle
     */
    public static final String FT_MEASUREMENT_RUM_VIEW = "view";
    /**
     * Error data measurement, such as crash, network error and user-defined error, {@link ErrorType}
     */
    public static final String FT_MEASUREMENT_RUM_ERROR = "error";
    /**
     * Long delay operation measurement, used to record application lag problems
     */
    public static final String FT_MEASUREMENT_RUM_LONG_TASK = "long_task";
    /**
     * Resource measurement, records network request duration, status and error content
     */
    public static final String FT_MEASUREMENT_RUM_RESOURCE = "resource";
    /**
     * Action behavior measurement, refers to user clicks or custom behaviors
     */
    public static final String FT_MEASUREMENT_RUM_ACTION = "action";

    /**
     * RUM, user access monitoring, datakit request address
     **/
    public static final String URL_MODEL_RUM = "v1/write/rum";
    /**
     * Log, datakit request address
     **/
    public static final String URL_MODEL_LOG = "v1/write/logging";
//    /**
//     * Trace link datakit request address
//     */
//    public static final String URL_MODEL_TRACING = "v1/write/tracing";
    /**
     * Dynamic variable loading address
     */
    public static final String URL_ENV_VARIABLE = "v1/env_variable";

    /**
     * Default service name, field service
     */
    public static final String DEFAULT_SERVICE_NAME = "df_rum_android";

    /**
     * Log maximum cache number limit
     */
    public static final int DEFAULT_DB_LOG_CACHE_NUM = 5000;
    /**
     * Log minimum cache number limit
     */
    public static final int MINI_DB_LOG_CACHE_NUM = 1000;

    /**
     * RUM maximum cache number limit, Action, View, LongTask, Resource, theoretical size is about 200MB, if it is network Error data, it is about 400MB
     */
    public static final int DEFAULT_DB_RUM_CACHE_NUM = 100_000;

    /**
     * RUM minimum limit
     */
    public static final int MINI_DB_RUM_CACHE_NUM = 10_000;

    /**
     * Set total cache size limit
     */
    public static final int DEFAULT_DB_SIZE_LIMIT = 104857600;//100MB
    public static final int MINI_DB_SIZE_LIMIT = 31457280;//30MB
    public static final int DB_OLD_CACHE_REMOVE_COUNT = 100;


//    public static final String KEY_EVENT_ID = "event_id";
//    public static final String KEY_EVENT = "event";
//
//    public static final String KEY_PAGE_EVENT_CURRENT_PAGE_NAME = "current_page_name";
//    public static final String KEY_PAGE_EVENT_ROOT_PAGE_NAME = "root_page_name";
//
//    public static final String KEY_PAGE_EVENT_PAGE_DESC = "page_desc";
//    public static final String KEY_PAGE_EVENT_USER_NAME = "ud_name";

    /**
     * Duration, unit nanoseconds
     */
    public static final String KEY_TIME_COST_DURATION = "duration";
    /**
     * Log level, {@link com.ft.sdk.garble.bean.Status}
     */
    public static final String KEY_STATUS = "status";

    //    public static final String KEY_BATTERY_TOTAL = "battery_total";
//    public static final String KEY_BATTERY_CHARGE_TYPE = "battery_charge_type";
//    public static final String KEY_BATTERY_STATUS = "battery_status";
    public static final String KEY_BATTERY_USE = "battery_use";

    /* Page cycle start =======> */

    /**
     * Total memory size, device total capacity, unit GB
     */
    public static final String KEY_MEMORY_TOTAL = "memory_total";

    /**
     * Memory usage, percentage, current device total usage rate
     */
    public static final String KEY_MEMORY_USE = "memory_use";
    /**
     * Current application CPU load
     */
    public static final String KEY_CPU_USE = "cpu_use";

    /**
     * Current application CPU tick count
     */
    public static final String KEY_CPU_TICK_COUNT = "cpu_tick_count";
    /**
     * Current application CPU ticks per second
     */
    public static final String KEY_CPU_TICK_COUNT_PER_SECOND = "cpu_tick_count_per_second";
    /**
     * Average value in memory
     */
    public static final String KEY_MEMORY_AVG = "memory_avg";
    /**
     * Maximum value in memory
     */
    public static final String KEY_MEMORY_MAX = "memory_max";
    /**
     * FPS minimum, the lower the value, the more serious the lag
     */
    public static final String KEY_FPS_MINI = "fps_mini";
    /**
     * FPS average, the lower the value, the worse the user experience
     */
    public static final String KEY_FPS_AVG = "fps_avg";
    /**
     * Current application battery average value
     */
    public static final String KEY_BATTERY_CURRENT_AVG = "battery_current_avg";
    /**
     * Current application battery maximum consumption
     */
    public static final String KEY_BATTERY_CURRENT_MAX = "battery_current_max";

    /* <======= Page cycle end */

//    public static final String KEY_NETWORK_TYPE = "network_type";
//    public static final String KEY_NETWORK_STRENGTH = "network_strength";
//    public static final String KEY_NETWORK_IN_RATE = "network_in_rate";
//    public static final String KEY_NETWORK_OUT_RATE = "network_out_rate";
//    public static final String KEY_NETWORK_PROXY = "network_proxy";
//    public static final String KEY_NETWORK_DNS = "dns";
//    public static final String KEY_NETWORK_ROAM = "roam";
//    public static final String KEY_NETWORK_WIFI_SSID = "wifi_ssid";
//    public static final String KEY_NETWORK_WIFI_IP = "wifi_ip";
//    public static final String KEY_INNER_NETWORK_TCP_TIME = "_network_tcp_time";
//    public static final String KEY_INNER_NETWORK_DNS_TIME = "_network_dns_time";
//    public static final String KEY_INNER_NETWORK_RESPONSE_TIME = "_network_response_time";
//    public static final String KEY_NETWORK_TCP_TIME = "network_tcp_time";
//    public static final String KEY_NETWORK_DNS_TIME = "network_dns_time";
//    public static final String KEY_NETWORK_RESPONSE_TIME = "network_response_time";


    /**
     * Service name
     */
    public static final String KEY_SERVICE = "service";
    /**
     * Environment, {@link com.ft.sdk.EnvType}
     */
    public static final String KEY_ENV = "env";

    /**
     * Application uuid, same Build application_uuid
     */
    public static final String KEY_APPLICATION_UUID = "application_uuid";
    /**
     * Application app version number, build.gradle android.defaultConfig.versionName
     */
    public static final String KEY_APP_VERSION_NAME = "version";

    /**
     * Use sdk name, fixed to {@link #SDK_NAME}
     */
    public static final String KEY_SDK_NAME = "sdk_name";

    /**
     * Log, main content
     */
    public static final String KEY_MESSAGE = "message";
//    public static final String KEY_BACKENDSAMPLE = "backend_sample";

    /**
     * Set uuid, use {@link android.provider.Settings.Secure#ANDROID_ID}
     */
    public static final String KEY_DEVICE_UUID = "device_uuid";

    /**
     * System, fixed to android
     */
    public static final String KEY_DEVICE_OS = "os";
    /**
     * os version
     */
    public static final String KEY_DEVICE_OS_VERSION = "os_version";
    /**
     * os main version
     */
    public static final String KEY_DEVICE_OS_VERSION_MAJOR = "os_version_major";
    /**
     * {@link Build#BRAND}   Mobile device manufacturer
     */
    public static final String KEY_DEVICE_DEVICE_BAND = "device";
    /**
     * {@link  Build#MODEL}  Mobile device model
     */
    public static final String KEY_DEVICE_DEVICE_MODEL = "model";
    /**
     * os.arch
     */
    public static final String KEY_DEVICE_DEVICE_ARCH = "arch";
    /**
     * Screen size
     */
    public static final String KEY_DEVICE_DISPLAY = "screen_size";
    /**
     * Mobile cellular operator
     */
    public static final String KEY_DEVICE_CARRIER = "carrier";
    /**
     *
     */
    public static final String KEY_DEVICE_LOCALE = "locale";
    /**
     * Click event
     */
    public static final String EVENT_NAME_CLICK = "click";
    public static final String EVENT_NAME_ACTION_NAME_BACK = "back";
    public static final String EVENT_NAME_ACTION_NAME_DPAD_CENTER = "dpad_center";
    public static final String EVENT_NAME_ACTION_NAME_MENU = "menu";

    /**
     * Whether it is logged in
     */
    public static final String KEY_RUM_IS_SIGN_IN = "is_signin";

    /**
     * User id, usually the unique id of the account
     */
    public static final String KEY_RUM_USER_ID = "userid";
    /**
     * User name
     */
    public static final String KEY_RUM_USER_NAME = "user_name";
    /**
     * User email
     */
    public static final String KEY_RUM_USER_EMAIL = "user_email";

    /**
     * RUM app_id, user access monitoring uses this id to distinguish
     */
    public static final String KEY_RUM_APP_ID = "app_id";
    /**
     * RUM custom key numerical value
     */
    public static final String KEY_RUM_CUSTOM_KEYS = "custom_keys";

    /**
     * Resource request address
     */
    public static final String KEY_RUM_RESOURCE_URL = "resource_url";

    /**
     * Resource request host address
     */
    public static final String KEY_RUM_RESOURCE_URL_HOST = "resource_url_host";
    /**
     * Resource type, http header  Content-Type
     */
    public static final String KEY_RUM_RESOURCE_TYPE = "resource_type";
    /**
     * Request return, http return header Connection
     */
    public static final String KEY_RUM_RESPONSE_CONNECTION = "response_connection";
    /**
     * http header  Content-Type
     */
    public static final String KEY_RUM_RESPONSE_CONTENT_TYPE = "response_content_type";

    /**
     * http header  Encoding
     */
    public static final String KEY_RUM_RESPONSE_CONTENT_ENCODING = "response_content_encoding";
    /**
     * Request method GET POST etc
     */
    public static final String KEY_RUM_RESOURCE_METHOD = "resource_method";
    /**
     * Resource return header
     */
    public static final String KEY_RUM_RESPONSE_HEADER = "response_header";
    /**
     * Resource request header
     */
    public static final String KEY_RUM_REQUEST_HEADER = "request_header";

    /**
     * Release version information set
     */
    public static final String KEY_RUM_SDK_PACKAGE_INFO = "sdk_pkg_info";
    /**
     * ft-sdk module release version
     */
    public static final String KEY_RUM_SDK_PACKAGE_AGENT = "agent";
    /**
     * ft-plugin module release version
     */
    public static final String KEY_RUM_SDK_PACKAGE_TRACK = "track";

    /**
     * ft-native module release version
     */
    public static final String KEY_RUM_SDK_PACKAGE_NATIVE = "native";

    /**
     * Web view uses web sdk release version
     */
    public static final String KEY_RUM_SDK_PACKAGE_WEB = "web";

    /**
     * SDK data marker [packageId].[pid].[pkg_dataCount].[uuid]
     * <p>
     * {@link ID36Generator}
     * {@link Utils#randomUUID()}
     */
    public static final String KEY_SDK_DATA_FLAG = "sdk_data_id";

    /**
     * SDK View update time
     */
    public static final String KEY_SDK_VIEW_UPDATE_TIME = "view_update_time";

    /**
     * SDK version number, here is agent version {@link #KEY_RUM_SDK_PACKAGE_AGENT}
     */
    public static final String KEY_SDK_VERSION = "sdk_version";

    /**
     * Resource return status code {@link javax.net.ssl.HttpsURLConnection#HTTP_OK} etc
     */
    public static final String KEY_RUM_RESOURCE_STATUS = "resource_status";
    /**
     * Resource type grouping, distinguished by the first digit of the request code, for example 200 is 2xx, 400,401 is 4xx
     */
    public static final String KEY_RUM_RESOURCE_STATUS_GROUP = "resource_status_group";
    /**
     * Resource size
     */
    public static final String KEY_RUM_RESOURCE_SIZE = "resource_size";

    /**
     * Resource duration, unit nanoseconds
     */
    public static final String KEY_RUM_RESOURCE_DURATION = "duration";

    /**
     * Resource dns resolution duration
     */
    public static final String KEY_RUM_RESOURCE_DNS = "resource_dns";
    public static final String KEY_RUM_RESOURCE_DNS_TIME = "resource_dns_time";
    /**
     * tcp connection duration
     */
    public static final String KEY_RUM_RESOURCE_TCP = "resource_tcp";
    public static final String KEY_RUM_RESOURCE_CONNECT_TIME = "resource_connect_time";
    /**
     * ssl connection duration
     */
    public static final String KEY_RUM_RESOURCE_SSL = "resource_ssl";
    public static final String KEY_RUM_RESOURCE_SSL_TIME = "resource_ssl_time";
    /**
     * ttfb value
     */
    public static final String KEY_RUM_RESOURCE_TTFB = "resource_ttfb";
    /**
     * First byte time
     */
    public static final String KEY_RUM_RESOURCE_FIRST_BYTE = "resource_first_byte";
    public static final String KEY_RUM_RESOURCE_FIRST_BYTE_TIME = "resource_first_byte_time";

    public static final String KEY_RUM_DOWNLOAD_TIME = "resource_download_time";
    /**
     * Link Span id
     */
    public static final String KEY_RUM_RESOURCE_SPAN_ID = "span_id";
    /**
     * Link id
     */
    public static final String KEY_RUM_RESOURCE_TRACE_ID = "trace_id";
    public static final String KEY_RUM_RESOURCE_TRANS = "resource_trans";
    /**
     * Resource url path numerical value
     */
    public static final String KEY_RUM_RESOURCE_URL_PATH = "resource_url_path";
    /**
     * Resource url path grouping, automatically grouping similar urls /?/path/query?params=x1
     */
    public static final String KEY_RUM_RESOURCE_URL_PATH_GROUP = "resource_url_path_group";

    /**
     * host IP address
     */
    public static final String KEY_RUM_RESOURCE_HOST_IP = "resource_host_ip";

    /**
     * Short error description
     */
    public static final String KEY_RUM_ERROR_MESSAGE = "error_message";
    /**
     * Error content stack
     */
    public static final String KEY_RUM_ERROR_STACK = "error_stack";
    /**
     * Error source type {@link com.ft.sdk.garble.bean.ErrorSource}
     */
    public static final String KEY_RUM_ERROR_SOURCE = "error_source";
    /**
     * Error type {@link ErrorType}
     */
    public static final String KEY_RUM_ERROR_TYPE = "error_type";
    /**
     * Status when error occurs, {@link com.ft.sdk.garble.bean.AppState}
     */
    public static final String KEY_RUM_ERROR_SITUATION = "error_situation";
    /**
     * Long task duration, nanoseconds, {@link #FT_MEASUREMENT_RUM_LONG_TASK}
     */
    public static final String KEY_RUM_LONG_TASK_DURATION = "duration";
    /**
     * Long task stack
     */
    public static final String KEY_RUM_LONG_TASK_STACK = "long_task_stack";
    /**
     * Network type, WIFI, 3G, 4G, etc.
     */
    public static final String KEY_RUM_NETWORK_TYPE = "network_type";

    /**
     * Session ID
     */
    public static final String KEY_RUM_SESSION_ID = "session_id";

    /**
     * Data collected from error
     */

    public static final String KEY_SAMPLED_FOR_ERROR_SESSION = "sampled_for_error_session";

    /**
     * Session error time
     */
    public static final String KEY_SESSION_ERROR_TIMESTAMP = "session_error_timestamp";
    /**
     * Session rum sampling rate
     */
    public static final String KEY_SESSION_SAMPLE_RATE = "session_sample_rate";
    /**
     * Session error sampling rate
     */
    public static final String KEY_SESSION_ON_ERROR_SAMPLE_RATE = "session_on_error_sample_rate";

    /**
     * Session type
     */
    public static final String KEY_RUM_SESSION_TYPE = "session_type";
    /**
     * View ID, {@link com.ft.sdk.garble.bean.ViewBean}
     */
    public static final String KEY_RUM_VIEW_ID = "view_id";
    /**
     * Page source, parent of the page
     */
    public static final String KEY_RUM_VIEW_REFERRER = "view_referrer";
    /**
     * Page name
     */
    public static final String KEY_RUM_VIEW_NAME = "view_name";

    /**
     * View root path default value
     */
    public static final String VIEW_NAME_ROOT = "root";

    /**
     * Page load time
     */
    public static final String KEY_RUM_VIEW_LOAD = "loading_time";
    /**
     * Number of long tasks in page cycle, {@link #FT_MEASUREMENT_RUM_LONG_TASK}
     */
    public static final String KEY_RUM_VIEW_LONG_TASK_COUNT = "view_long_task_count";
    /**
     * Number of resource requests in page cycle, {@link #FT_MEASUREMENT_RUM_RESOURCE}
     */
    public static final String KEY_RUM_VIEW_RESOURCE_COUNT = "view_resource_count";
    /**
     * Number of errors in page cycle, {@link #FT_MEASUREMENT_RUM_ERROR}
     */
    public static final String KEY_RUM_VIEW_ERROR_COUNT = "view_error_count";

    /**
     * Number of actions in page cycle, {@link #FT_MEASUREMENT_RUM_ACTION}
     */
    public static final String KEY_RUM_VIEW_ACTION_COUNT = "view_action_count";

    /**
     * Page cycle, page stay time
     */
    public static final String KEY_RUM_VIEW_TIME_SPENT = "time_spent";
    /**
     * Whether the page is in active state
     */
    public static final String KEY_RUM_VIEW_IS_ACTIVE = "is_active";
    /**
     * Whether it is uploaded data from webview
     */
    public static final String KEY_RUM_VIEW_IS_WEB_VIEW = "is_web_view";

    /**
     * Action ID, {@link #FT_MEASUREMENT_RUM_ACTION}
     */
    public static final String KEY_RUM_ACTION_ID = "action_id";
    /**
     * Action name
     */
    public static final String KEY_RUM_ACTION_NAME = "action_name";

    /**
     * Action type
     */
    public static final String KEY_RUM_ACTION_TYPE = "action_type";

    /**
     * Action cycle content long duration count statistics, {@link Constants#FT_MEASUREMENT_RUM_LONG_TASK}
     */
    public static final String KEY_RUM_ACTION_LONG_TASK_COUNT = "action_long_task_count";

    /**
     * Action life cycle, {@link Constants#FT_MEASUREMENT_RUM_RESOURCE} times
     */
    public static final String KEY_RUM_ACTION_RESOURCE_COUNT = "action_resource_count";
    /**
     * Action life cycle, {@link Constants#FT_MEASUREMENT_RUM_ERROR} times
     */
    public static final String KEY_RUM_ACTION_ERROR_COUNT = "action_error_count";
    /**
     * Action duration, unit nanoseconds
     */
    public static final String KEY_RUM_ACTION_DURATION = "duration";

    /**
     * Line protocol data, additional attributes
     */
    public static final String KEY_RUM_PROPERTY = "property";

    /**
     * Line protocol data tags, temporarily store dynamic tags
     */
    public static final String KEY_RUM_TAGS = "tags";


    /**
     * Collected according to sampling rate
     */
    public static final String KEY_COLLECT_TYPE = "collect_type";

    /**
     * Cold start, action_type
     */
    public static final String ACTION_TYPE_LAUNCH_COLD = "launch_cold";
    /**
     * Hot start, action_type
     */
    public static final String ACTION_TYPE_LAUNCH_HOT = "launch_hot";

    /**
     * Cold start, action_name
     */
    public static final String ACTION_NAME_LAUNCH_COLD = "app cold start";
    /**
     * Hot start, action_name
     */
    public static final String ACTION_NAME_LAUNCH_HOT = "app hot start";

}
