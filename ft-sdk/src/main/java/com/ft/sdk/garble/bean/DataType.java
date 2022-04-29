package com.ft.sdk.garble.bean;

/**
 * create: by huangDianHua
 * time: 2020/6/5 13:10:45
 * description:上传的数据类型
 * <p>
 */
public enum DataType {
    RUM_APP,
    RUM_WEBVIEW,
    LOG,
    TRACE;

    public String getValue() {
        return toString().toLowerCase();
    }

}
