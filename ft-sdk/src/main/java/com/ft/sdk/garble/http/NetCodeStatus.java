package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-16 11:30
 * Description:
 */
public class NetCodeStatus {
    public static final int NET_UNKNOWN_ERR = 0;
    /**
     * 网络未连接
     */
    public static final int NET_STATUS_UNCONNECT = 1;

    /**
     * 网络未链接错误
     */
    public static final String NET_STATUS_UNCONNECT_ERR = "net.connect.not.code";

    /**
     * 网络返回数据不是json
     */
    public static final int NET_STATUS_RESPONSE_NOT_JSON = 2;

    /**
     * 网络返回数据不是json
     */
    public static final String NET_STATUS_RESPONSE_NOT_JSON_ERR = "net.response.not.json";
}
