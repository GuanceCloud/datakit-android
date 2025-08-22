package com.ft.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ft.BuildConfig;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.RequestUtils;

import okhttp3.Request;

/**
 * Test service, okhttp network request tracking situation, {@link FTSDKConfig#isOnlySupportMainProcess()}
 * Enhanced with stress testing capabilities
 */
public class TestService extends Service {
    /**
     * Send and receive using Action Filter
     */
    public static final String ACTION_MESSAGE = "com.ft.action.MESSAGE";
    private static final String TAG = "TestService";
    /**
     * SDK installation status
     */
    public static final String INSTALLED_STATE = "installed";

    /**
     * Stress test related constants
     */
    private static final String EXTRA_STRESS_TEST = "stress_test";
    private static final String EXTRA_HTTP_COUNT = "http_count";
    private static final String EXTRA_LOG_COUNT = "log_count";
    private static final String EXTRA_ACTION_COUNT = "action_count";
    private static final String EXTRA_DURATION = "duration";

    /**
     * Default stress test parameters
     */
    private static final int DEFAULT_HTTP_COUNT = 100;
    private static final int DEFAULT_LOG_COUNT = 200;
    private static final int DEFAULT_ACTION_COUNT = 50;
    private static final int DEFAULT_DURATION = 30000; // 30 seconds

    /**
     * Stress test status
     */
    private boolean isStressTesting = false;
    private Thread stressTestThread;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialization operation
        boolean sdkInstalled = FTSdk.get() != null;

        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra(INSTALLED_STATE, sdkInstalled);
        // Cross-process sending
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // Check if stress test mode is enabled
            boolean enableStressTest = intent.getBooleanExtra(EXTRA_STRESS_TEST, false);
            if (enableStressTest) {
                startStressTest(intent);
            } else {
                // Original simple test logic
                performBasicTest();
            }
        } else {
            performBasicTest();
        }

        return START_STICKY;
    }

    /**
     * Perform basic test
     */
    private void performBasicTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = RequestUtils.requestUrl(BuildConfig.TRACE_URL);
                LogUtils.d(TAG, "header=" + request.headers().toString());
            }
        }).start();
    }

    /**
     * Start stress test
     */
    private void startStressTest(Intent intent) {
        if (isStressTesting) {
            LogUtils.w(TAG, "Stress test is already running, ignoring duplicate start");
            return;
        }

        // Get stress test parameters
        int httpCount = intent.getIntExtra(EXTRA_HTTP_COUNT, DEFAULT_HTTP_COUNT);
        int logCount = intent.getIntExtra(EXTRA_LOG_COUNT, DEFAULT_LOG_COUNT);
        int actionCount = intent.getIntExtra(EXTRA_ACTION_COUNT, DEFAULT_ACTION_COUNT);
        int duration = intent.getIntExtra(EXTRA_DURATION, DEFAULT_DURATION);

        LogUtils.d(TAG, "Starting stress test - HTTP:" + httpCount + ", Log:" + logCount +
                ", Action:" + actionCount + ", Duration:" + duration + "ms");

        isStressTesting = true;
        stressTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    performStressTest(httpCount, logCount, actionCount, duration);
                } catch (Exception e) {
                    LogUtils.e(TAG, "Stress test execution exception: " + e.getMessage());
                } finally {
                    isStressTesting = false;
                }
            }
        });
        stressTestThread.start();
    }

    /**
     * Execute stress test
     */
    private void performStressTest(int httpCount, int logCount, int actionCount, int duration) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + duration;

        int httpCounter = 0;
        int logCounter = 0;
        int actionCounter = 0;

        LogUtils.d(TAG, "Stress test started, target duration: " + duration + "ms");

        while (System.currentTimeMillis() < endTime && isStressTesting) {
            try {
                // HTTP request stress test
                if (httpCounter < httpCount) {
                    performHttpRequest(httpCounter++);
                }

                // Log stress test
                if (logCounter < logCount) {
                    performLogGeneration(logCounter++);
                }

                // Action event stress test
                if (actionCounter < actionCount) {
                    performActionGeneration(actionCounter++);
                }

                // Control stress test frequency
                Thread.sleep(10);

            } catch (InterruptedException e) {
                LogUtils.d(TAG, "Stress test interrupted");
                break;
            } catch (Exception e) {
                LogUtils.e(TAG, "Stress test execution exception: " + e.getMessage());
            }
        }

        long actualDuration = System.currentTimeMillis() - startTime;
        LogUtils.d(TAG, "Stress test completed - Actual duration: " + actualDuration + "ms, " +
                "HTTP:" + httpCounter + ", Log:" + logCounter + ", Action:" + actionCounter);
    }

    /**
     * Execute HTTP request
     */
    private void performHttpRequest(int index) {
        try {
            String url = BuildConfig.TRACE_URL + "_stress_test_" + index;
            Request request = RequestUtils.requestUrl(url);
            LogUtils.d(TAG, "HTTP stress test " + index + " - URL: " + url);
        } catch (Exception e) {
            LogUtils.e(TAG, "HTTP stress test exception " + index + ": " + e.getMessage());
        }
    }

    /**
     * Generate log
     */
    private void performLogGeneration(int index) {
        try {
            String logMessage = "TestService stress test log - Index:" + index + " - Time:" + System.currentTimeMillis();

            // Use FTLogger to record logs
            FTLogger.getInstance().logBackground(logMessage, Status.ERROR);

            // Console log
            Log.d(TAG, logMessage);

        } catch (Exception e) {
            LogUtils.e(TAG, "Log stress test exception " + index + ": " + e.getMessage());
        }
    }

    /**
     * Generate Action event
     */
    private void performActionGeneration(int index) {
        try {
            String actionName = "TestService stress test Action - Index:" + index;
            String actionType = "stress_test";

            FTRUMGlobalManager.get().addAction(actionName, actionType);

            LogUtils.d(TAG, "Action stress test " + index + " - Name:" + actionName);

        } catch (Exception e) {
            LogUtils.e(TAG, "Action stress test exception " + index + ": " + e.getMessage());
        }
    }

    /**
     * Stop stress test
     */
    public void stopStressTest() {
        if (isStressTesting && stressTestThread != null) {
            isStressTesting = false;
            stressTestThread.interrupt();
            LogUtils.d(TAG, "Stress test stopped");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Service doesn't need binding, return null
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop stress test
        stopStressTest();

        // After ending the process, ProcessConfigTest can perform the next 
        // Application.onCreate re-call
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Convenient method to create stress test Intent
     */
    public static Intent createStressTestIntent(android.content.Context context,
                                                boolean enableStressTest,
                                                int httpCount,
                                                int logCount,
                                                int actionCount,
                                                int duration) {
        Intent intent = new Intent(context, TestService.class);
        intent.putExtra(EXTRA_STRESS_TEST, enableStressTest);
        intent.putExtra(EXTRA_HTTP_COUNT, httpCount);
        intent.putExtra(EXTRA_LOG_COUNT, logCount);
        intent.putExtra(EXTRA_ACTION_COUNT, actionCount);
        intent.putExtra(EXTRA_DURATION, duration);
        return intent;
    }
}






