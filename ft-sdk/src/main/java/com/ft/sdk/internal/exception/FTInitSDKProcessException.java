package com.ft.sdk.internal.exception;

/**
 * author: huangDianHua
 * time: 2020/8/3 14:37:43
 * description: SDK 启动错误类型，在 SDK 未正常初始化时抛出
 */
public class FTInitSDKProcessException extends RuntimeException implements FTException {

    public FTInitSDKProcessException(String s) {
        super(s);
    }
}
