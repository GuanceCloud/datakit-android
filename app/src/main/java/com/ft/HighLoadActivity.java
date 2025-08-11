package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
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
    private static final int HTTP_DATA_COUNT = 18000;
    /**
     * 360000/2, log count per concurrent thread
     */
    private static final int LOG_DATA_COUNT = 180000;

    /**
     * Send add custom Action events
     */
    private static final int ADD_ACTION_COUNT = 10000;
    /**
     * 1000/50, average 10ms per concurrent 2 threads
     */
    public static final int LOG_SLEEP = 20;

    /**
     * 1000/5, average 100ms per concurrent 2 threads
     */
    public static final int HTTP_REQUEST_SLEEP = 200;

    /**
     * Log synchronization lock
     */
    private final Object logLock = new Object();

    /**
     * HTTP synchronization lock
     */
    private final Object httpLock = new Object();

    /**
     * Log call count
     */
    private int logCount = 0;
    /**
     * Network request count
     */
    private int httpCount = 0;

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
    }

    /**
     * Batch logging
     * Console logs, {@link #LOG_DATA_COUNT}
     * Custom logs, {@link #LOG_DATA_COUNT}
     */
    private void batchLog() {

        ThreadPoolHandler.get().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < LOG_DATA_COUNT; i++) {
                    try {
                        Thread.sleep(LOG_SLEEP);
                        synchronized (logLock) {
                            LogUtils.d(TAG, Thread.currentThread().getId() + ",batchLog" + (++logCount));
                            Log.e(TAG, "count:" + logCount + "," + Constants.LOG_TEST_DATA_512_BYTE);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });

        ThreadPoolHandler.get().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < LOG_DATA_COUNT; i++) {
                    try {
                        Thread.sleep(LOG_SLEEP);
                        synchronized (logLock) {
                            LogUtils.d(TAG, Thread.currentThread().getId() + ",batchLog" + (++logCount));
                            FTLogger.getInstance().logBackground("count:" + logCount + "," + Constants.LOG_TEST_DATA_512_BYTE, Status.ERROR);
                        }
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
                }
            }
        });

    }
}
