package com.ft.sdk.sessionreplay.internal.recorder;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

public class Debouncer {

    private final Handler handler;
    private final long maxRecordDelayInNs;
    private long lastTimeRecordWasPerformed = 0L;
    private boolean firstRequest = true;

    public Debouncer() {
        this(new Handler(Looper.getMainLooper()), MAX_DELAY_THRESHOLD_NS);
    }

    public Debouncer(Handler handler, long maxRecordDelayInNs) {
        this.handler = handler;
        this.maxRecordDelayInNs = maxRecordDelayInNs;
    }

    public void debounce(Runnable runnable) {
        if (firstRequest) {
            // Initialize the lastTimeRecordWasPerformed here to the current time in nano
            // In case the component was initialized earlier than the first debounce request,
            // it will execute the runnable directly and will not pass through the handler.
            lastTimeRecordWasPerformed = System.nanoTime();
            firstRequest = false;
        }
        handler.removeCallbacksAndMessages(null);
        long timePassedSinceLastExecution = System.nanoTime() - lastTimeRecordWasPerformed;
        if (timePassedSinceLastExecution >= maxRecordDelayInNs) {
            executeRunnable(runnable);
        } else {
            handler.postDelayed(() -> executeRunnable(runnable), DEBOUNCE_TIME_IN_MS);
        }
    }

    private void executeRunnable(Runnable runnable) {
        runnable.run();
        lastTimeRecordWasPerformed = System.nanoTime();
    }

    private static final long MAX_DELAY_THRESHOLD_NS = TimeUnit.MILLISECONDS.toNanos(64);
    private static final long DEBOUNCE_TIME_IN_MS = 64L;
}
