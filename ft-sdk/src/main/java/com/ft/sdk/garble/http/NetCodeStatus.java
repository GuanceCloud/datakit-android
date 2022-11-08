package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-16 11:30
 * Description:
 */
public class NetCodeStatus {
    /**
     * 网络返回数据不是json
     */
    public static final int NET_STATUS_RESPONSE_NOT_JSON = 2;

    /**
     * 网络返回数据不是json
     */
    public static final String NET_STATUS_RESPONSE_NOT_JSON_ERR = "net.response.not.json";


    /**
     * 网络问题
     */
    public static final int NETWORK_EXCEPTION_CODE = 10001;

    /**
     * 参数问题
     */
    public static final int INVALID_PARAMS_EXCEPTION_CODE = 10002;

    /**
     * 文件 IO 问题
     */
    public static final int FILE_IO_EXCEPTION_CODE = 10003;

    /**
     * 未知问题
     */
    public static final int UNKNOWN_EXCEPTION_CODE = 10004;

}
