package com.ft.sdk.internal.exception;

/**
 * author: huangDianHua
 * time: 2020/8/3 14:37:43
 * description:
 */
public class FTInitSDKProcessException extends RuntimeException implements FTException {

    public FTInitSDKProcessException(String s) {
        super(s);
    }
}
