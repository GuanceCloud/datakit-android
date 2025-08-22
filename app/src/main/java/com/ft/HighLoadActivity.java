package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.service.StressTestConfig;
import com.ft.threadpool.ThreadPoolHandler;
import com.ft.utils.RequestUtils;

/**
 * High-speed data writing
 * 100 logs per second
 * 10 network requests per second
 */
public class HighLoadActivity extends NameTitleActivity {
    private static final String TAG = "HighLoadActivity";
    /**
     * 36000/2, concurrent HTTP requests per thread
     */
    private static final int HTTP_DATA_COUNT = 900;
    /**
     * 360000/2, log count per concurrent thread
     */
    private static final int LOG_DATA_COUNT = 900;

    /**
     * Send add custom Action events
     */
    private static final int ADD_ACTION_COUNT = 1000;
    /**
     * 1000/50, average 10ms per concurrent 2 threads
     */
    public static final int PRODUCE_SLEEP = 20;

    /**
     * 1000/5, average 100ms per concurrent 2 threads
     */
    public static final int HTTP_REQUEST_SLEEP = 200;

    /**
     * TestService stress test related constants
     */
    private static final int TEST_SERVICE_HTTP_COUNT = 500;  // HTTP request count
    private static final int TEST_SERVICE_LOG_COUNT = 1000;  // Log count
    private static final int TEST_SERVICE_ACTION_COUNT = 300; // Action count
    private static final int TEST_SERVICE_SLEEP = 50;         // Stress test interval

    /**
     * Log synchronization lock
     */
    private final Object logLock = new Object();

    /**
     * HTTP synchronization lock
     */
    private final Object httpLock = new Object();

    /**
     * TestService stress test lock
     */
    private final Object testServiceLock = new Object();

    /**
     * Log call count
     */
    private int logCount = 0;
    /**
     * Network request count
     */
    private int httpCount = 0;
    /**
     * TestService stress test counter
     */
    private int testServiceCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_load);

        findViewById(R.id.high_load_log_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchLog();
            }
        });

        findViewById(R.id.high_load_http_request_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchHttpRequest();
                batchHttpRequest();
            }
        });

        findViewById(R.id.high_load_to_repeat_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HighLoadActivity.this, RepeatActivity.class));

            }
        });

        findViewById(R.id.high_load_to_repeat_add_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchAddActions();
            }
        });

        // New: Start real TestService stress test service button
        findViewById(R.id.high_load_real_test_service_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRealTestServiceStressTest();
            }
        });

        // New: Custom stress test button
        findViewById(R.id.high_load_custom_stress_test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCustomStressTest();
            }
        });
    }

    /**
     * Batch logging
     * Console logs, {@link #LOG_DATA_COUNT}
     * Custom logs, {@link #LOG_DATA_COUNT}
     */
    private void batchLog() {

        ThreadPoolHandler.get().getExecutor().
                execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < LOG_DATA_COUNT; i++) {
                            synchronized (logLock) {
                                LogUtils.d(TAG, "batchLog:" + (++logCount));
                                FTLogger.getInstance().logBackground("Custom Log:" + logCount, Status.ERROR);
                            }

                            try {
                                Thread.sleep(PRODUCE_SLEEP);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                });

    }

    /**
     * Batch HTTP requests {@link #LOG_DATA_COUNT}
     */
    private void batchHttpRequest() {
        ThreadPoolHandler.get().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < HTTP_DATA_COUNT; i++) {
                    synchronized (httpLock) {
                        LogUtils.d(TAG, "batchHttpRequest:" + (++httpCount));
                        RequestUtils.requestUrl(BuildConfig.TRACE_URL + httpCount);
                    }

                    try {
                        Thread.sleep(HTTP_REQUEST_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });

    }

    private void batchAddActions() {
        ThreadPoolHandler.get().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ADD_ACTION_COUNT; i++) {
                    FTRUMGlobalManager.get().addAction("Custom Add Action:" + i,
                            "custom_type");
                    try {
                        Thread.sleep(PRODUCE_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * Start real TestService stress test service
     * Use special parameters to enable stress test mode
     */
    private void startRealTestServiceStressTest() {
        try {
            // Use medium stress test configuration
            StressTestConfig.Config config = StressTestConfig.getConfig(StressTestConfig.StressTestScenario.MEDIUM);

            Intent serviceIntent = config.createIntent(this);
            startService(serviceIntent);

            LogUtils.d(TAG, "TestService stress test service started - " +
                    StressTestConfig.getScenarioDescription(StressTestConfig.StressTestScenario.MEDIUM));
            Toast.makeText(this, "TestService stress test service started - " +
                            StressTestConfig.getScenarioDescription(StressTestConfig.StressTestScenario.MEDIUM),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            LogUtils.e(TAG, "Failed to start TestService stress test service: " + e.getMessage());
            Toast.makeText(this, "Failed to start TestService stress test service", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start custom stress test configuration
     */
    private void startCustomStressTest() {
        try {
            // Custom stress test parameters
            StressTestConfig.Config config = StressTestConfig.getCustomConfig(
                    300,   // HTTP request count
                    800,   // Log count
                    150,   // Action count
                    90000  // Duration 90 seconds
            );

            Intent serviceIntent = config.createIntent(this);
            startService(serviceIntent);

            LogUtils.d(TAG, "Custom stress test service started - HTTP:300, Log:800, Action:150, Duration:90s");
            Toast.makeText(this, "Custom stress test service started, duration 90 seconds", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            LogUtils.e(TAG, "Failed to start custom stress test service: " + e.getMessage());
            Toast.makeText(this, "Failed to start custom stress test service", Toast.LENGTH_SHORT).show();
        }
    }
}
