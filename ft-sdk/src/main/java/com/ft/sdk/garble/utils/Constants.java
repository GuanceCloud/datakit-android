package com.ft.sdk.garble.utils;

import android.os.Build;

import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.OP;

import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:41
 * Description: SDK 常量声明
 */
public class Constants {

    public static final String LOG_TAG_PREFIX = "[FT-SDK]";
    /**
     * SDK 名称
     */
    public static final String SDK_NAME = "df_android_rum_sdk";

    /**
     * 数据同步链路 propagation header key
     */
    public static final String SYNC_DATA_TRACE_HEADER = "X-Pkg-Id";

    /**
     * 数据同步链路 propagation header value
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
     * 非法数值
     */
    public static final String FT_KEY_VALUE_NULL = "null";

    /**
     * 行协议，指标
     */
    public static final String MEASUREMENT = "measurement";
    /**
     * 行协议时间
     */
    public static final String TIME = "time";
    /**
     * 行协议所有指标数据
     */
    public static final String FIELDS = "fields";
    /**
     * 行协议所有标签数据
     */
    public static final String TAGS = "tags";

    /**
     * 最终数据数据分隔符
     */
    public static final String SEPARATION = " ";
    /**
     * 最终数据行协议单条数据分隔
     */
    public static final String SEPARATION_REAL_LINE_BREAK = "\n";//换行标志符，用于日志显示

    /**
     * 同步数据 http 请求 header user-agent
     */
    public static final String USER_AGENT = "DF-RUM-Android";

    /**
     * 日志默认指标名
     */
    public static final String FT_LOG_DEFAULT_MEASUREMENT = "df_rum_android_log";

    /**
     * 存储 {@link  android.content.SharedPreferences} {@link #KEY_RUM_SESSION_ID} 键值
     */
    public static final String FT_RANDOM_USER_ID = "ft.user.session.id";
    /**
     * 存储 {@link  android.content.SharedPreferences} {@link #KEY_RUM_USER_ID} 键值
     */
    public static final String FT_USER_USER_ID = "ft.user.userid";
    /**
     * 存储 {@link  android.content.SharedPreferences} {@link #KEY_RUM_USER_NAME} 键值
     */
    public static final String FT_USER_USER_NAME = "ft.user.username";
    /**
     * 存储 {@link  android.content.SharedPreferences} {@link #FT_USER_USER_EMAIL} 键值
     */
    public static final String FT_USER_USER_EMAIL = "ft.user.email";
    /**
     * 存储 {@link  android.content.SharedPreferences} {@link #FT_USER_USER_EXT} 键值
     */
    public static final String FT_USER_USER_EXT = "ft.user.extdata";

    /**
     * 存储 {@link  android.content.SharedPreferences} 数据存储键值
     */

    public static final String FT_SHARE_PER_FILE = "ftSDKShareFile";

    /**
     * 页面数据指标，暂时指 Activity 生命周期
     */
    public static final String FT_MEASUREMENT_RUM_VIEW = "view";
    /**
     * 发生错的数据指标，例如崩溃、网络错误及用户自定义错误,{@link ErrorType}
     */
    public static final String FT_MEASUREMENT_RUM_ERROR = "error";
    /**
     * 长延迟操作指标，用于记录应用运行卡顿问题
     */
    public static final String FT_MEASUREMENT_RUM_LONG_TASK = "long_task";
    /**
     * 资源指标，记录网络请求耗时指标及状态和错误内容
     */
    public static final String FT_MEASUREMENT_RUM_RESOURCE = "resource";
    /**
     * 操作行为指标，指用户点击、或者自定义行为
     */
    public static final String FT_MEASUREMENT_RUM_ACTION = "action";

    /**
     * RUM，用户访问监测，datakit 请求地址
     **/
    public static final String URL_MODEL_RUM = "v1/write/rum";
    /**
     * Log，日志，datakit 请求地址
     **/
    public static final String URL_MODEL_LOG = "v1/write/logging";
    /**
     * Trace 链路 datakit 请求地址
     */
    public static final String URL_MODEL_TRACING = "v1/write/tracing";

    /**
     * 默认服务名，字段 service
     */
    public static final String DEFAULT_SERVICE_NAME = "df_rum_android";

    /**
     * 日志最大限制缓存数
     */
    public static final int DEFAULT_DB_LOG_CACHE_NUM = 5000;
    /**
     * 日志缓存最小缓存数限制
     */
    public static final int MINI_DB_LOG_CACHE_NUM = 1000;

    /**
     * RUM 最大限制缓存数，Action，View，LongTask，Resource，理论大小是 200MB 左右，如果是网络 Error 数据大约是 400MB
     */
    public static final int DEFAULT_DB_RUM_CACHE_NUM = 100_000;

    /**
     * RUM 最小限制
     */
    public static final int MINI_DB_RUM_CACHE_NUM = 10_000;

    /**
     * 设置总缓存限制大小
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
     * 耗时，单位纳秒
     */
    public static final String KEY_TIME_COST_DURATION = "duration";
    /**
     * 日志等级，{@link com.ft.sdk.garble.bean.Status}
     */
    public static final String KEY_STATUS = "status";

    //    public static final String KEY_BATTERY_TOTAL = "battery_total";
//    public static final String KEY_BATTERY_CHARGE_TYPE = "battery_charge_type";
//    public static final String KEY_BATTERY_STATUS = "battery_status";
    public static final String KEY_BATTERY_USE = "battery_use";

    /* 页面周期 start =======> */

    /**
     * 内存总大小，设备总容量，单位 GB
     */
    public static final String KEY_MEMORY_TOTAL = "memory_total";

    /**
     * 内存消耗量，百分比, 当前设备总占用率
     */
    public static final String KEY_MEMORY_USE = "memory_use";
    /**
     * 当前应用 CPU 负载
     */
    public static final String KEY_CPU_USE = "cpu_use";

    /**
     * 当前应用 CPU 跳动次数
     */
    public static final String KEY_CPU_TICK_COUNT = "cpu_tick_count";
    /**
     * 当前应用 CPU 每秒次数
     */
    public static final String KEY_CPU_TICK_COUNT_PER_SECOND = "cpu_tick_count_per_second";
    /**
     * 内存中平均值
     */
    public static final String KEY_MEMORY_AVG = "memory_avg";
    /**
     * 内存最大值
     */
    public static final String KEY_MEMORY_MAX = "memory_max";
    /**
     * FPS 最地址，数值越低代表卡顿越严重
     */
    public static final String KEY_FPS_MINI = "fps_mini";
    /**
     * FPS 平均值，数值越低代表帧数用户体验越差
     */
    public static final String KEY_FPS_AVG = "fps_avg";
    /**
     * 当前应用 battery 平均值
     */
    public static final String KEY_BATTERY_CURRENT_AVG = "battery_current_avg";
    /**
     * 当前应用 battery 最大消耗量
     */
    public static final String KEY_BATTERY_CURRENT_MAX = "battery_current_max";


    public static final String KEY_HAS_REPLAY = "session_has_replay";

    /* <======= 页面周期 end */

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
     * 服务名
     */
    public static final String KEY_SERVICE = "service";
    /**
     * 环境， {@link com.ft.sdk.EnvType}
     */
    public static final String KEY_ENV = "env";

    /**
     * 应用 uuid，同一 Build application_uuid 相同
     */
    public static final String KEY_APPLICATION_UUID = "application_uuid";
    /**
     * 应用 app 版本号，build.gradle android.defaultConfig.versionName
     */
    public static final String KEY_APP_VERSION_NAME = "version";

    /**
     * 使用 sdk 的名称，固定为 {@link #SDK_NAME}
     */
    public static final String KEY_SDK_NAME = "sdk_name";

    /**
     * 日志，主体内容
     */
    public static final String KEY_MESSAGE = "message";
//    public static final String KEY_BACKENDSAMPLE = "backend_sample";

    /**
     * 设置 uuid，使用的 {@link android.provider.Settings.Secure#ANDROID_ID}
     */
    public static final String KEY_DEVICE_UUID = "device_uuid";

    /**
     * 系统，固定为 android
     */
    public static final String KEY_DEVICE_OS = "os";
    /**
     * os 版本
     */
    public static final String KEY_DEVICE_OS_VERSION = "os_version";
    /**
     * os 主要版本
     */
    public static final String KEY_DEVICE_OS_VERSION_MAJOR = "os_version_major";
    /**
     * {@link Build#BRAND}  移动设备厂商
     */
    public static final String KEY_DEVICE_DEVICE_BAND = "device";
    /**
     * {@link  Build#MODEL} 移动设备型号
     */
    public static final String KEY_DEVICE_DEVICE_MODEL = "model";
    /**
     * os.arch
     */
    public static final String KEY_DEVICE_DEVICE_ARCH = "arch";
    /**
     * 屏幕尺寸
     */
    public static final String KEY_DEVICE_DISPLAY = "screen_size";
    /**
     * 手机蜂窝运营商
     */
    public static final String KEY_DEVICE_CARRIER = "carrier";
    /**
     *
     */
    public static final String KEY_DEVICE_LOCALE = "locale";
    /**
     * 点击事件
     */
    public static final String EVENT_NAME_CLICK = "click";
    public static final String EVENT_NAME_ACTION_NAME_BACK = "back";
    public static final String EVENT_NAME_ACTION_NAME_DPAD_CENTER = "dpad_center";
    public static final String EVENT_NAME_ACTION_NAME_MENU = "menu";

    /**
     * 是否为已登录状态
     */
    public static final String KEY_RUM_IS_SIGN_IN = "is_signin";

    /**
     * 用户 id，一般为该账号唯一id
     */
    public static final String KEY_RUM_USER_ID = "userid";
    /**
     * 用户名称
     */
    public static final String KEY_RUM_USER_NAME = "user_name";
    /**
     * 用户邮箱
     */
    public static final String KEY_RUM_USER_EMAIL = "user_email";

    /**
     * RUM app_id, 用户访问监测使用这个 id 区分
     */
    public static final String KEY_RUM_APP_ID = "app_id";
    /**
     * RUM 自定义 key 数值
     */
    public static final String KEY_RUM_CUSTOM_KEYS = "custom_keys";

    /**
     * 资源请求地址
     */
    public static final String KEY_RUM_RESOURCE_URL = "resource_url";

    /**
     * 资源请求主机地址
     */
    public static final String KEY_RUM_RESOURCE_URL_HOST = "resource_url_host";
    /**
     * 资源类型，http header  Content-Type
     */
    public static final String KEY_RUM_RESOURCE_TYPE = "resource_type";
    /**
     * 请求返回，http 返回 header Connection
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
     * 请求方法 GET POST 等
     */
    public static final String KEY_RUM_RESOURCE_METHOD = "resource_method";
    /**
     * 资源返回头
     */
    public static final String KEY_RUM_RESPONSE_HEADER = "response_header";
    /**
     * 资源请求头
     */
    public static final String KEY_RUM_REQUEST_HEADER = "request_header";

    /**
     * 发布版本信息集合
     */
    public static final String KEY_RUM_SDK_PACKAGE_INFO = "sdk_pkg_info";
    /**
     * ft-sdk module 发布版本
     */
    public static final String KEY_RUM_SDK_PACKAGE_AGENT = "agent";
    /**
     * ft-plugin module 发布版本
     */
    public static final String KEY_RUM_SDK_PACKAGE_TRACK = "track";

    /**
     * ft-native module 发布版本
     */
    public static final String KEY_RUM_SDK_PACKAGE_NATIVE = "native";

    /**
     * ft-session-replay module 发布版本
     */
    public static final String KEY_RUM_SDK_PACKAGE_REPLAY = "replay";

    /**
     * ft-session-replay-material module
     */
    public static final String KEY_RUM_SDK_PACKAGE_REPLAY_MATERIAL = "replay_mtr";

    /**
     * web view 使用的 web sdk 发布版本
     */
    public static final String KEY_RUM_SDK_PACKAGE_WEB = "web";

    /**
     * SDK 数据标记 [packageId].[pid].[pkg_dataCount].[uuid]
     * <p>
     * {@link ID36Generator}
     * {@link Utils#randomUUID()}
     */
    public static final String KEY_SDK_DATA_FLAG = "sdk_data_id";

    /**
     * SDK View 更新时间
     */
    public static final String KEY_SDK_VIEW_UPDATE_TIME = "view_update_time";

    /**
     * SDK 版本号，这里为 agent 版本 {@link #KEY_RUM_SDK_PACKAGE_AGENT}
     */
    public static final String KEY_SDK_VERSION = "sdk_version";

    /**
     * 资源返回状态码 {@link javax.net.ssl.HttpsURLConnection#HTTP_OK} 等
     */
    public static final String KEY_RUM_RESOURCE_STATUS = "resource_status";
    /**
     * 资源类型分组，以请求码首位进去区分，例如 200 为 2xx，400，401 为 4xx
     */
    public static final String KEY_RUM_RESOURCE_STATUS_GROUP = "resource_status_group";
    /**
     * 资源大小
     */
    public static final String KEY_RUM_RESOURCE_SIZE = "resource_size";

    /**
     * 资源耗时，单位纳秒
     */
    public static final String KEY_RUM_RESOURCE_DURATION = "duration";

    /**
     * 资源 dns 解析时长
     */
    public static final String KEY_RUM_RESOURCE_DNS = "resource_dns";
    public static final String KEY_RUM_RESOURCE_DNS_TIME = "resource_dns_time";
    /**
     * tcp 连接时长
     */
    public static final String KEY_RUM_RESOURCE_TCP = "resource_tcp";
    public static final String KEY_RUM_RESOURCE_CONNECT_TIME = "resource_connect_time";
    /**
     * ssl 连接时长
     */
    public static final String KEY_RUM_RESOURCE_SSL = "resource_ssl";
    public static final String KEY_RUM_RESOURCE_SSL_TIME = "resource_ssl_time";
    /**
     * ttfb 数值
     */
    public static final String KEY_RUM_RESOURCE_TTFB = "resource_ttfb";
    /**
     * 首字节时间
     */
    public static final String KEY_RUM_RESOURCE_FIRST_BYTE = "resource_first_byte";
    public static final String KEY_RUM_RESOURCE_FIRST_BYTE_TIME = "resource_first_byte_time";

    public static final String KEY_RUM_DOWNLOAD_TIME = "resource_download_time";
    /**
     * 链路 Span id
     */
    public static final String KEY_RUM_RESOURCE_SPAN_ID = "span_id";
    /**
     * 链路 id
     */
    public static final String KEY_RUM_RESOURCE_TRACE_ID = "trace_id";
    public static final String KEY_RUM_RESOURCE_TRANS = "resource_trans";
    /**
     * 资源 url path 数值
     */
    public static final String KEY_RUM_RESOURCE_URL_PATH = "resource_url_path";
    /**
     * 资源 url path 分组，对类似 url 自动分组 /?/path/query?params=x1
     */
    public static final String KEY_RUM_RESOURCE_URL_PATH_GROUP = "resource_url_path_group";

    /**
     * host IP 地址
     */
    public static final String KEY_RUM_RESOURCE_HOST_IP = "resource_host_ip";

    /**
     * 错误简短描述
     */
    public static final String KEY_RUM_ERROR_MESSAGE = "error_message";
    /**
     * 错误内容堆栈
     */
    public static final String KEY_RUM_ERROR_STACK = "error_stack";
    /**
     * 错误源类型 {@link com.ft.sdk.garble.bean.ErrorSource}
     */
    public static final String KEY_RUM_ERROR_SOURCE = "error_source";
    /**
     * 错误类型 {@link ErrorType}
     */
    public static final String KEY_RUM_ERROR_TYPE = "error_type";
    /**
     * 发生错误时候的状态 ，{@link com.ft.sdk.garble.bean.AppState}
     */
    public static final String KEY_RUM_ERROR_SITUATION = "error_situation";
    /**
     * long task 耗时，纳秒，{@link #FT_MEASUREMENT_RUM_LONG_TASK}
     */
    public static final String KEY_RUM_LONG_TASK_DURATION = "duration";
    /**
     * long task 堆栈
     */
    public static final String KEY_RUM_LONG_TASK_STACK = "long_task_stack";
    /**
     * 网络类型 ，WIFI，3G ，4G 等等
     */
    public static final String KEY_RUM_NETWORK_TYPE = "network_type";

    /**
     * 会话 ID
     */
    public static final String KEY_RUM_SESSION_ID = "session_id";

    /**
     * 会话类型
     */
    public static final String KEY_RUM_SESSION_TYPE = "session_type";
    /**
     * view ID，{@link com.ft.sdk.garble.bean.ViewBean}
     */
    public static final String KEY_RUM_VIEW_ID = "view_id";
    /**
     * 页面来源，页面的父级
     */
    public static final String KEY_RUM_VIEW_REFERRER = "view_referrer";
    /**
     * 页面名称
     */
    public static final String KEY_RUM_VIEW_NAME = "view_name";

    /**
     * View 根路径默认值
     */
    public static final String VIEW_NAME_ROOT = "root";

    /**
     * 页面加载时间
     */
    public static final String KEY_RUM_VIEW_LOAD = "loading_time";
    /**
     * 页面周期内，发生 long task 次数, {@link #FT_MEASUREMENT_RUM_LONG_TASK}
     */
    public static final String KEY_RUM_VIEW_LONG_TASK_COUNT = "view_long_task_count";
    /**
     * 页面周期内，资源请求数，{@link #FT_MEASUREMENT_RUM_RESOURCE}
     */
    public static final String KEY_RUM_VIEW_RESOURCE_COUNT = "view_resource_count";
    /**
     * 页面周期内，发生 error 次数 ，{@link #FT_MEASUREMENT_RUM_ERROR}
     */
    public static final String KEY_RUM_VIEW_ERROR_COUNT = "view_error_count";

    /**
     * 页面周期内，发生 action 次数，{@link #FT_MEASUREMENT_RUM_ACTION}
     */
    public static final String KEY_RUM_VIEW_ACTION_COUNT = "view_action_count";

    /**
     * 页面周期内，页面停留时间
     */
    public static final String KEY_RUM_VIEW_TIME_SPENT = "time_spent";
    /**
     * 页面是否，正处在激活状态
     */
    public static final String KEY_RUM_VIEW_IS_ACTIVE = "is_active";
    /**
     * 是否为 webview 上传数据
     */
    public static final String KEY_RUM_VIEW_IS_WEB_VIEW = "is_web_view";

    /**
     * 操作 ID ，{@link #FT_MEASUREMENT_RUM_ACTION}
     */
    public static final String KEY_RUM_ACTION_ID = "action_id";
    /**
     * 操作名称
     */
    public static final String KEY_RUM_ACTION_NAME = "action_name";

    /**
     * 操作类型
     */
    public static final String KEY_RUM_ACTION_TYPE = "action_type";

    /**
     * action 周期内容长耗时数量统计，{@link Constants#FT_MEASUREMENT_RUM_LONG_TASK}
     */
    public static final String KEY_RUM_ACTION_LONG_TASK_COUNT = "action_long_task_count";

    /**
     * action 生命周期中, 发生{@link Constants#FT_MEASUREMENT_RUM_RESOURCE} 次数
     */
    public static final String KEY_RUM_ACTION_RESOURCE_COUNT = "action_resource_count";
    /**
     * action 生命周期中，发生{@link Constants#FT_MEASUREMENT_RUM_ERROR} 次数
     */
    public static final String KEY_RUM_ACTION_ERROR_COUNT = "action_error_count";
    /**
     * action 耗时，单位纳秒
     */
    public static final String KEY_RUM_ACTION_DURATION = "duration";

    /**
     * 行协议数据，附加属性
     */
    public static final String KEY_RUM_PROPERTY = "property";

    public static final String SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY = "type";
    public static final String RUM_SESSION_RENEWED_BUS_MESSAGE = "rum_session_renewed";
    public static final String RUM_KEEP_SESSION_BUS_MESSAGE_KEY = "keepSession";
    public static final String RUM_SESSION_ID_BUS_MESSAGE_KEY = "sessionId";

    /**
     * 行协议数据 tags，临时存储动态 tags
     */
    public static final String KEY_RUM_TAGS = "tags";

    /**
     * 冷启动，action_type
     */
    public static final String ACTION_TYPE_LAUNCH_COLD = "launch_cold";
    /**
     * 热启动，action_type
     */
    public static final String ACTION_TYPE_LAUNCH_HOT = "launch_hot";

    /**
     * 冷启动，action_name
     */
    public static final String ACTION_NAME_LAUNCH_COLD = "app cold start";
    /**
     * 热启动，action_name
     */
    public static final String ACTION_NAME_LAUNCH_HOT = "app hot start";


    /**
     * OP EVENT 数据对照
     */
    public final static HashMap<OP, String> OP_EVENT_MAPS = new HashMap<>();

    static {
        OP_EVENT_MAPS.put(OP.CLK, Constants.EVENT_NAME_CLICK);
    }


}
