package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.RequestUtils;

public class HighLoadActivity extends NameTitleActivity {
    private static final String TAG = "HighLoadActivity";
    private static final int HTTP_DATA_COUNT = 18000;
    private static final int LOG_DATA_COUNT = 180000;

    private final Object logLock = new Object();
    private final Object httpLock = new Object();
    private int logCount = 0;
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
    }

    private void batchLog() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < LOG_DATA_COUNT; i++) {
                    try {
                        Thread.sleep(20);
                        Log.e(TAG, Constants.LOG_TEST_DATA_512_BYTE);
                        synchronized (logLock) {
                            LogUtils.d(TAG, "batchLog" + (++logCount));

                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < LOG_DATA_COUNT; i++) {
                    try {
                        Thread.sleep(20);
                        FTLogger.getInstance().logBackground(Constants.LOG_TEST_DATA_512_BYTE, Status.ERROR);
                        synchronized (logLock) {
                            LogUtils.d(TAG, "batchLog" + (++logCount));
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();

    }

    private void batchHttpRequest() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < HTTP_DATA_COUNT; i++) {
                    synchronized (httpLock) {
                        LogUtils.d(TAG, "batchHttpRequest:" + (++httpCount));

                    }
                    RequestUtils.requestUrl(BuildConfig.TRACE_URL + httpCount);


                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();

    }
}
