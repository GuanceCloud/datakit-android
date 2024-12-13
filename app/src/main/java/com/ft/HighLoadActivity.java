package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.threadpool.ThreadPoolHandler;
import com.ft.utils.RequestUtils;

/**
 * 高速写入数据
 * 1秒 100 条日志
 * 1秒 10次网络请求
 */
public class HighLoadActivity extends NameTitleActivity {
    private static final String TAG = "HighLoadActivity";
    /**
     * 36000/2，并发一个线程 http 数量
     */
    private static final int HTTP_DATA_COUNT = 500;
    /**
     * 360000/2，并发线程一个线程日志量
     */
    private static final int LOG_DATA_COUNT = 500;
    /**
     * 1000/50，并发2线程平均 10 ms 一次
     */
    public static final int LOG_SLEEP = 20;

    /**
     * 1000/5 ，并发 2 线程平均100 毫秒 1次
     */
    public static final int HTTP_REQUEST_SLEEP = 200;

    /**
     * 日志同步锁
     */
    private final Object logLock = new Object();

    /**
     * http 同步锁
     */
    private final Object httpLock = new Object();

    /**
     * 日志调用次数
     */
    private int logCount = 0;
    /**
     * 网络请求次数
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
    }

    /**
     * 批量日志
     * 控制台日志，{@link #LOG_DATA_COUNT}
     * 自定义日志, {@link #LOG_DATA_COUNT}
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
     * 批量 http 请求 {@link #LOG_DATA_COUNT}
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
}
