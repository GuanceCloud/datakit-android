package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-16 11:30
 * Description: 定义网络相关的错误与错误码
 */
public class NetCodeStatus {

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
     * 超时
     */
    public static final int FILE_TIMEOUT_CODE = 10004;

    /**
     * 未知问题
     */
    public static final int UNKNOWN_EXCEPTION_CODE = 11000;

}
