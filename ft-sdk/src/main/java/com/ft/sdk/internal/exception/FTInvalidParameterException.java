package com.ft.sdk.internal.exception;

import java.security.InvalidParameterException;

/**
 * 参数错误异常，在 SDK Config 初始化过程中参数不规范时抛出
 *
 * @author Brandon
 */
public class FTInvalidParameterException extends InvalidParameterException implements FTException {

    public FTInvalidParameterException(String msg) {
        super(msg);
    }
}
