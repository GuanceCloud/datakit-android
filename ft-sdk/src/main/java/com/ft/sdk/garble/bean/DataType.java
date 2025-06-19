package com.ft.sdk.garble.bean;

/**
 * create: by huangDianHua
 * time: 2020/6/5 13:10:45
 * description:上传的数据类型
 * <p>
 */
public enum DataType {

    /**
     * 原生 App RUM 数据
     */
    RUM_APP,

    /**
     * 未被采样的数据缓存
     */
    RUM_APP_ERROR_SAMPLED,
    /**
     * WebView 中 JS 产生的 RUM 数据需要配置，需要配置 <a href="https://github.com/GuanceCloud/datakit-js"> JS SDK</a>
     */
    RUM_WEBVIEW,

    /**
     * 未被采样的数据缓存
     */
    RUM_WEBVIEW_ERROR_SAMPLED,

    /**
     * SDK Log 数据
     */
    LOG,
//    /**
//     * SDK Trace 数据，目前不在使用
//     */
//    TRACE
    ;

    public final static String ERROR_SAMPLED_SUFFIX = "_error_sampled";

    public String getValue() {
        return toString().toLowerCase();
    }

}
