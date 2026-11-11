package com.ft.sdk.internal.anr;


import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.ExtraLogCatSetting;
import com.ft.sdk.FTActivityManager;
import com.ft.sdk.FTANRDetector;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;

/**
 * ANR error monitoring, continuously monitors whether the execution time difference between two Runners exceeds 
 * {@link #ANR_DETECT_DURATION_MS}, if exceeded, adds an ANR error message
 */
public final class ANRDetectRunnable implements Runnable {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "ANRDetectRunnable";

    private static final IssueReporter LEGACY_ISSUE_REPORTER = new IssueReporter() {
        @Override
        public void report(String stack, long occurredAtNanoseconds, String threadName) {
            report(stack, occurredAtNanoseconds, threadName, FTActivityManager.get().getAppState());
        }

        @Override
        public void report(String stack, long occurredAtNanoseconds, String threadName, AppState state) {
            FTANRDetector.reportAnr(stack, occurredAtNanoseconds, threadName, state);
        }
    };

    /**
     * Monitoring cycle
     */
    public static final int ANR_DETECT_DURATION_MS = 5000;

    private final ExtraLogCatSetting extraLogCatSetting;

    private final MainThreadPoster mainThreadPoster;

    private final IssueReporter issueReporter;

    private final long detectDurationMs;

    /**
     * Creates a detector using the SDK's automatic ANR reporting path.
     *
     * @deprecated Configure ANR collection through the SDK instead of constructing the detector.
     */
    @Deprecated
    public ANRDetectRunnable(ExtraLogCatSetting extraLogCatSetting) {
        this(extraLogCatSetting, LEGACY_ISSUE_REPORTER);
    }

    public ANRDetectRunnable(ExtraLogCatSetting extraLogCatSetting, IssueReporter issueReporter) {
        this(extraLogCatSetting, new AndroidMainThreadPoster(), issueReporter, ANR_DETECT_DURATION_MS);
    }

    ANRDetectRunnable(ExtraLogCatSetting extraLogCatSetting,
                      MainThreadPoster mainThreadPoster,
                      IssueReporter issueReporter,
                      long detectDurationMs) {
        this.extraLogCatSetting = extraLogCatSetting;
        this.mainThreadPoster = mainThreadPoster;
        this.issueReporter = issueReporter;
        this.detectDurationMs = detectDurationMs;
    }

    private final CallbackRunnable runnable = new CallbackRunnable();

    private volatile boolean isClose = false;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final long occurredAtNanoseconds;
                final AppState occurredAppState;
                synchronized (runnable) {
                    if (isClose) {
                        break;
                    }
                    runnable.reset();
                    // The unacknowledged probe is our observable approximation of the
                    // blocking interval's start. Keep its time and state together;
                    // detection and stack/logcat collection may happen after a transition.
                    occurredAtNanoseconds = Utils.getCurrentNanoTime();
                    occurredAppState = FTActivityManager.get().getAppState();
                    if (!mainThreadPoster.post(runnable)) {
                        return;
                    }
                    long deadline = System.nanoTime() + detectDurationMs * 1_000_000L;
                    long remainingMs = detectDurationMs;
                    // Keep the configured detection cadence across early or spurious wakeups.
                    while (!isClose && remainingMs > 0) {
                        runnable.wait(remainingMs);
                        long remainingNanos = deadline - System.nanoTime();
                        remainingMs = remainingNanos <= 0
                                ? 0
                                : Math.max(1L, remainingNanos / 1_000_000L);
                    }
                    if (isClose) {
                        break;
                    }
                    if (runnable.isCalled()) {
                        continue;
                    }
                }

                Thread mainThread = mainThreadPoster.getThread();
                if (isClose || Thread.currentThread().isInterrupted()) {
                    break;
                }
                String stackTrace =
                        StringUtils.getStringFromStackTraceElement(mainThread.getStackTrace())
                                + "\n" + Utils.getAllThreadStack();
                if (extraLogCatSetting != null) {
                    stackTrace += Utils.getLogcat(extraLogCatSetting.getLogcatMainLines(),
                            extraLogCatSetting.getLogcatSystemLines(),
                            extraLogCatSetting.getLogcatEventsLines());
                }

                if (isClose || Thread.currentThread().isInterrupted()) {
                    break;
                }
                try {
                    issueReporter.report(stackTrace, occurredAtNanoseconds, mainThread.getName(), occurredAppState);
                } catch (Throwable throwable) {
                    LogUtils.e(TAG, "ANR report failed: " + throwable.getClass().getSimpleName());
                }
                // A filtered background report must not end detection permanently.
                // Wait for this probe to recover so one continuous block produces one candidate.
                synchronized (runnable) {
                    while (!isClose && !runnable.isCalled()) {
                        runnable.wait();
                    }
                    if (isClose) {
                        break;
                    }
                }
                Thread.sleep(ANR_RECOVERY_DELAY_MS);
            } catch (InterruptedException e) {
                LogUtils.e(TAG, "ANR Thread interrupt");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static final long ANR_RECOVERY_DELAY_MS = 500;

    /**
     * Shutdown Runner
     */
    public void shutdown() {
        synchronized (runnable) {
            isClose = true;
            runnable.notifyAll();
        }
    }

    /**
     * Receives a confirmed watchdog ANR without exposing a public RUM reporting API.
     */
    public interface IssueReporter {
        void report(String stack, long occurredAtNanoseconds, String threadName);

        /** Time and state captured when the unacknowledged main-thread probe was posted. */
        default void report(String stack, long occurredAtNanoseconds, String threadName, AppState state) {
            report(stack, occurredAtNanoseconds, threadName);
        }
    }

    interface MainThreadPoster {
        boolean post(Runnable runnable);

        Thread getThread();
    }

    private static final class AndroidMainThreadPoster implements MainThreadPoster {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public boolean post(Runnable runnable) {
            return handler.post(runnable);
        }

        @Override
        public Thread getThread() {
            return handler.getLooper().getThread();
        }
    }

    /**
     * Check if the Runner object is called
     */
    public static class CallbackRunnable implements Runnable {
        /**
         * Whether it is called
         *
         * @return
         */
        public synchronized boolean isCalled() {
            return called;
        }

        private boolean called = false;

        @Override
        public synchronized void run() {
            called = true;
            notifyAll();
        }

        /**
         * Reset
         */
        public synchronized void reset() {
            called = false;
        }


    }
}
