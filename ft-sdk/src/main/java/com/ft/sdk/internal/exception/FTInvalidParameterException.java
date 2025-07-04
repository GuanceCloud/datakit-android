package com.ft.sdk.internal.exception;

import java.security.InvalidParameterException;

/**
 * Parameter error exception, thrown when parameters are not standardized during SDK Config initialization
 *
 * @author Brandon
 */
public class FTInvalidParameterException extends InvalidParameterException implements FTException {

    public FTInvalidParameterException(String msg) {
        super(msg);
    }
}
