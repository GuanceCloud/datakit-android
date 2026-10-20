package com.ft.sdk.internal.metrics;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/** Keeps callback failures local to a timing metric and bounds diagnostic logging. */
final class ViewTimingCallback {
    private ViewTimingCallback() {
    }

    static void failed(Throwable failure, AtomicBoolean reported) {
        if (failure instanceof VirtualMachineError) throw (VirtualMachineError) failure;
        if (failure instanceof ThreadDeath) throw (ThreadDeath) failure;
        if (reported.compareAndSet(false, true)) {
            LogUtils.w(Constants.LOG_TAG_PREFIX + "ViewTiming", "Timing identifier failed: "
                    + failure.getClass().getSimpleName());
        }
    }
}
