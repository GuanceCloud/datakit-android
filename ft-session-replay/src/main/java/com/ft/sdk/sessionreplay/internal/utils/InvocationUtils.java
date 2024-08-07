package com.ft.sdk.sessionreplay.internal.utils;

import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;

import java.util.concurrent.Callable;

public class InvocationUtils {

    public <R> R safeCallWithErrorLogging(
            InternalLogger logger,
            Callable<R> call,
            String failureMessage
    ) {
        try {
            return call.call();
        } catch (Exception e) {
            logger.e("InvocationUtils", failureMessage);
        }
        return null;
    }

    public <R> R safeCallWithErrorLogging(
            Callable<R> call,
            String failureMessage
    ) {
        return safeCallWithErrorLogging(
                new NoOpInternalLogger(),
                call,
                failureMessage
        );
    }
}
