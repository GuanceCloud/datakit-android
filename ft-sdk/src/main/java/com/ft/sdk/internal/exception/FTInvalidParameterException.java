package com.ft.sdk.internal.exception;

import java.security.InvalidParameterException;

public class FTInvalidParameterException extends InvalidParameterException implements FTException {

    public FTInvalidParameterException(String msg) {
        super(msg);
    }
}
