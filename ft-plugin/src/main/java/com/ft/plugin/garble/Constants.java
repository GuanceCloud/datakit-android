
package com.ft.plugin.garble;


import java.util.UUID;

/**
 * author: huangDianHua
 * time: 2020/8/5 13:37:00
 * description:常量
 */
public class Constants {
    /**
     * SDK 包名
     */
    public static final String FT_SDK_PACKAGE = "com/ft/sdk";
    /**
     * SDK native 库
     */
    public static final String FT_NATIVE_PACKAGE = "ftnative/";

    /**
     * TingYun 路径
     */
    public static final String CLASS_NAME_TING_YUN_PACKAGE = "com/networkbench";

    /**
     * SDK 中的插桩类
     */
    public static final String FT_SDK_HOOK_CLASS = "com/ft/sdk/FTAutoTrack";
    /**
     * {@link System}
     */
    public static final String CLASS_NAME_SYSTEM = "java/lang/System";
    /**
     * Android 系统日志类
     */
    public static final String CLASS_NAME_LOG = "android/util/Log";
    /**
     * Android WebView 类名
     */
    public static final String CLASS_NAME_WEBVIEW = "android/webkit/WebView";

    /**
     * React Native WebView 类名
     */
    public static final String CLASS_NAME_RN_WEBVIEW = "com/reactnativecommunity/webview/RNCWebView";

    /**
     * Tencent WebView
     */
    public static final String CLASS_NAME_TENCENT_WEBVIEW = "com/tencent/smtt/sdk/WebView";

    /**
     * Tencent SDK 路径
     */
    public static final String CLASS_NAME_TENCENT_PATH = "com/tencent/smtt/sdk/";

    /**
     * Taobao Weex 路径
     */
    public static final String CLASS_NAME_TAOBAO_PATH = "com/taobao/weex/";

    /**
     * Dcloud 路径
     */
    public static final String CLASS_NAME_DCLOUD_PATH = "io/dcloud/";

    /**
     * OkHttpClient$Builder
     */
    public static final String CLASS_NAME_OKHTTP_BUILDER = "okhttp3/OkHttpClient$Builder";
    /**
     * org/apache/hc/client5/http/impl/classic/HttpClientBuilder
     */
    public static final String CLASS_NAME_HTTP_CLIENT_BUILDER = "org/apache/hc/client5/http/impl/classic/HttpClientBuilder";

    /**
     * SDK 中日志插桩类
     */
    public static final String CLASS_NAME_TRACKLOG = "com/ft/sdk/garble/utils/TrackLog";
    /**
     * int A(String,String) 方法的描述
     */
    public static final String METHOD_DESC_S_S_I = "(Ljava/lang/String;Ljava/lang/String;)I";

    /**
     * int (int,String,String)
     */
    public static final String METHOD_DESC_I_S_S_I = "(ILjava/lang/String;Ljava/lang/String;)I";
    /**
     * int v(String tag, String msg, Throwable tr) 方法描述
     */
    public static final String METHOD_DESC_S_S_T_I = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I";

    /**
     * int w(String tag, Throwable tr)
     */
    public static final String METHOD_DESC_S_T_I = "(Ljava/lang/String;Ljava/lang/Throwable;)I";
    /**
     * View
     */
    public static final String VIEW_DESC = "Landroid/view/View;";

    /**
     * 用于形成 application_uuid ，一次 build 会生成一个固定的 uuid
     */
    public static final String PACKAGE_UUID = UUID.randomUUID().toString();

    /**
     * ft-sdk 中 {@link com.ft.sdk.garble.annotation.IgnoreAOP} ASM 字节码写入标记
     */
    public static final String IGNORE_ANNOTATION = "Lcom/ft/sdk/garble/annotation/IgnoreAOP;";

    /**
     * 类内部 super 方式访问的方法的名
     */
    public static final String INNER_CLASS_METHOD_PREFIX = "access$";

}
