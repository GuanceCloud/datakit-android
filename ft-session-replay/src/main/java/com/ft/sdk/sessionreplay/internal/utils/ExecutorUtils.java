/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.utils;


import android.util.Log;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
    private static final String TAG = "ExecutorUtils";

    private static final String ERROR_TASK_REJECTED = "Unable to schedule %s task on the executor";

    /**
     * Executes runnable without throwing [RejectedExecutionException] if it cannot be accepted
     * for execution.
     *
     * @param operationName  Name of the task.
     * @param internalLogger Internal logger.
     * @param runnable       Task to run.
     */
    public static void executeSafe(Executor executor, String operationName, InternalLogger internalLogger, Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (RejectedExecutionException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_TASK_REJECTED, operationName) + "," + Log.getStackTraceString(e));
        }
    }

    /**
     * Executes runnable without throwing [RejectedExecutionException] if it cannot be accepted
     * for execution.
     *
     * @param operationName  Name of the task.
     * @param delay          Task scheduling delay.
     * @param unit           Delay unit.
     * @param internalLogger Internal logger.
     * @param runnable       Task to run.
     */
    public static ScheduledFuture<?> scheduleSafe(ScheduledExecutorService executor, String operationName, long delay, TimeUnit unit, InternalLogger internalLogger, Runnable runnable) {
        try {
            return executor.schedule(runnable, delay, unit);
        } catch (RejectedExecutionException e) {
            internalLogger.e(TAG, String.format(ERROR_TASK_REJECTED, operationName) + "," + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Submit runnable without throwing [RejectedExecutionException] if it cannot be accepted
     * for execution.
     *
     * @param operationName  Name of the task.
     * @param internalLogger Internal logger.
     * @param runnable       Task to run.
     */
    public static Future<?> submitSafe(ExecutorService executor, String operationName, InternalLogger internalLogger, Runnable runnable) {
        try {
            return executor.submit(runnable);
        } catch (RejectedExecutionException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_TASK_REJECTED,
                    operationName) + "," + Log.getStackTraceString(e));
            return null;
        }
    }
}