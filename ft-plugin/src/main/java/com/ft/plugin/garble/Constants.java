
package com.ft.plugin.garble;


import java.util.UUID;

/**
 * author: huangDianHua
 * time: 2020/8/5 13:37:00
 * description: Constants
 */
public class Constants {
    /**
     * SDK package name
     */
    public static final String FT_SDK_PACKAGE = "com/ft/sdk";
    /**
     * SDK native library
     */
    public static final String FT_NATIVE_PACKAGE = "ftnative/";

    /**
     * TingYun path
     */
    public static final String CLASS_NAME_TING_YUN_PACKAGE = "com/networkbench";

    /**
     * Hook class in SDK
     */
    public static final String FT_SDK_HOOK_CLASS = "com/ft/sdk/FTAutoTrack";
    /**
     * {@link System}
     */
    public static final String CLASS_NAME_SYSTEM = "java/lang/System";
    /**
     * Android system log class
     */
    public static final String CLASS_NAME_LOG = "android/util/Log";
    /**
     * Android WebView class name
     */
    public static final String CLASS_NAME_WEBVIEW = "android/webkit/WebView";

    /**
     * React Native WebView class name
     */
    public static final String CLASS_NAME_RN_WEBVIEW = "com/reactnativecommunity/webview/RNCWebView";

    /**
     * Tencent WebView
     */
    public static final String CLASS_NAME_TENCENT_WEBVIEW = "com/tencent/smtt/sdk/WebView";

    /**
     * Taobao WebView
     */
    public static final String CLASS_NAME_TAOBAO_WEBVIEW = "com/taobao/weex/ui/view/WXWebView";

    /**
     * Dcloud Webview
     */
    public static final String CLASS_NAME_DCLOUD_WEBVIEW = "io/dcloud/common/adapter/ui/webview/DCWebView";

    /**
     * Tencent SDK path
     */
    public static final String CLASS_NAME_TENCENT_PATH = "com/tencent/smtt/sdk/";

    /**
     * Taobao Weex path
     */
    public static final String CLASS_NAME_TAOBAO_PATH = "com/taobao/weex/";

    /**
     * Dcloud path
     */
    public static final String CLASS_NAME_DCLOUD_PATH = "io/dcloud/";

    /**
     * Okhttp3 path
     */

    public static final String CLASS_NAME_OKHTTP3_PATH = "okhttp3/";

    /**
     * OkHttpClient$Builder
     */
    public static final String CLASS_NAME_OKHTTP_BUILDER = "okhttp3/OkHttpClient$Builder";

    /**
     * Request$Builder
     */
    public static final String CLASS_NAME_REQUEST_BUILDER = "okhttp3/Request$Builder";

    /**
     * org/apache/hc/client5/http/impl/classic/HttpClientBuilder
     */
    public static final String CLASS_NAME_HTTP_CLIENT_BUILDER = "org/apache/hc/client5/http/impl/classic/HttpClientBuilder";

    /**
     * Log hook class in SDK
     */
    public static final String CLASS_NAME_TRACKLOG = "com/ft/sdk/garble/utils/TrackLog";
    /**
     * int A(String,String) method description
     */
    public static final String METHOD_DESC_S_S_I = "(Ljava/lang/String;Ljava/lang/String;)I";

    /**
     * int (int,String,String)
     */
    public static final String METHOD_DESC_I_S_S_I = "(ILjava/lang/String;Ljava/lang/String;)I";
    /**
     * int v(String tag, String msg, Throwable tr) method description
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
    public static final String OBJECT_DESC = "Ljava/lang/Object;";

    /**
     * Used to form application_uuid, one build will generate a fixed uuid
     */
    public static final String PACKAGE_UUID = UUID.randomUUID().toString();

    /**
     * ft-sdk {@link com.ft.sdk.garble.annotation.IgnoreAOP} ASM bytecode write marker
     */
    public static final String IGNORE_ANNOTATION = "Lcom/ft/sdk/garble/annotation/IgnoreAOP;";

    /**
     * Name of method accessed by super method inside class
     */
    public static final String INNER_CLASS_METHOD_PREFIX = "access$";

}
