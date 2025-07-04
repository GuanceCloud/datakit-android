package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-16 11:30
 * Description: Define network related errors and error codes
 */
public class NetCodeStatus {

    /**
     * Network problem
     */
    public static final int NETWORK_EXCEPTION_CODE = 10001;

    /**
     * Parameter problem
     */
    public static final int INVALID_PARAMS_EXCEPTION_CODE = 10002;

    /**
     * File IO problem
     */
    public static final int FILE_IO_EXCEPTION_CODE = 10003;



    /**
     * Timeout
     */
    public static final int FILE_TIMEOUT_CODE = 10004;

    /**
     * Unknown problem
     */
    public static final int UNKNOWN_EXCEPTION_CODE = 11000;

}
