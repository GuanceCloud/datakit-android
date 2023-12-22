package com.ft.sdk;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * BY huangDianHua
 * DATE:2019-12-05 20:41
 * Description:数据同步管理，将数据存储
 */
public class SyncTaskManager {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncTaskManager";
    /**
     * 最大容忍错误次数
     */
    public static final int MAX_ERROR_COUNT = 5;
    /**
     * 一个同步周期内一次请求包含数据条目数量
     */
    private static final int LIMIT_SIZE = 10;
    /**
     * 传输间歇休眠时间
     */
    private static final int SLEEP_TIME = 10000;

    /**
     * 重试等待时间
     */
    private static final int RETRY_DELAY_SLEEP_TIME = 100;
    /**
     * 统计一个周期内错误的次
     */
    private final AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * 是否正处于同步中，避免重复执行
     */
    private volatile boolean running;

    private boolean isStop = false;

    private static final int MSG_SYNC = 1;

    private int dataSyncMaxRetryCount;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SYNC) {
                executePoll(true);
            }
        }
    };


    /**
     * 同步数据类型
     */
    private final static DataType[] SYNC_MAP = DataType.values();

    /**
     * For AndroidTest
     *
     * @param running
     */
    private void setRunning(boolean running) {
        this.running = running;
    }

    private SyncTaskManager() {

    }

    private static class SingletonHolder {
        private static final SyncTaskManager INSTANCE = new SyncTaskManager();
    }

    public static SyncTaskManager get() {
        return SyncTaskManager.SingletonHolder.INSTANCE;
    }

    /**
     * For AndroidTest
     */
    private void executePoll() {
        executePoll(false);
    }

    private void executePoll(final boolean withSleep) {
        if (running || isStop) {
            return;
        }
        synchronized (this) {
            LogUtils.d(TAG, "=========executeSyncPoll=========");
            running = true;
            errorCount.set(0);
            DataUploaderThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        LogUtils.d(TAG, "******************数据同步线程运行中*******************>>>\n");
                        if (withSleep) {
                            Thread.sleep(SLEEP_TIME);
                        }

                        for (DataType dataType : SYNC_MAP) {
                            List<SyncJsonData> dataList = SyncTaskManager.this.queryFromData(dataType);

                            if (dataList.isEmpty()) {
                                continue;
                            }
                            SyncTaskManager.this.handleSyncOpt(dataType, dataList);
                        }

                    } catch (Exception e) {
                        LogUtils.e(TAG, Log.getStackTraceString(e));
                    } finally {
                        running = false;
                        LogUtils.d(TAG, "<<<**********************数据同步线程已结束**********************\n");
                    }
                }
            });
        }
    }

    /**
     * 触发延迟轮询同步
     */
    void executeSyncPoll() {
        mHandler.removeMessages(MSG_SYNC);
        mHandler.sendEmptyMessageDelayed(MSG_SYNC, 100);
    }

    /**
     * 执行存储数据同步操作
     */
    private synchronized void handleSyncOpt(final DataType dataType, final List<SyncJsonData> requestDatas) {

        if (!Utils.isNetworkAvailable()) {
            LogUtils.e(TAG, " \n**********************网络未连接************************");
            return;
        }

        if (dataSyncMaxRetryCount > 0) {
            if (errorCount.get() >= dataSyncMaxRetryCount) {
                LogUtils.e(TAG, " \n************连续同步失败" + dataSyncMaxRetryCount + "次，停止当前轮询同步***********");
                return;
            }
        }

        if (requestDatas == null || requestDatas.isEmpty()) {
            return;
        }
        SyncDataHelper syncData = new SyncDataHelper();
        String body = syncData.getBodyContent(dataType, requestDatas);
        LogUtils.d(TAG, body);
        requestNet(dataType, body, new AsyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                if (dataSyncMaxRetryCount == 0 || (code >= 200 && code < 500)) {
                    LogUtils.d(TAG, "\n<<<**********************同步数据成功**********************");
                    SyncTaskManager.this.deleteLastQuery(requestDatas);
                    if (dataType == DataType.LOG) {
                        FTDBCachePolicy.get().optCount(-requestDatas.size());
                    }
                    errorCount.set(0);
                    if ((dataSyncMaxRetryCount == 0 && code != 200) || code > 200) {
                        LogUtils.e(TAG, "同步数据出错(忽略)-[code:" + code + ",response:" + response + "]");
                    } else {
                        LogUtils.d(TAG, "[code:" + code + ",response:" + response + "]");
                    }
                } else {
                    LogUtils.e(TAG, errorCount.get() + ":同步数据失败-[code:" + code + ",response:" + response + "]");
                    errorCount.getAndIncrement();

                    if (errorCount.get() > 0) {
                        try {
                            Thread.sleep((long) errorCount.get() * RETRY_DELAY_SLEEP_TIME);
                        } catch (InterruptedException e) {
                            LogUtils.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
            }
        });

        if (requestDatas.size() < LIMIT_SIZE) {
            //do nothing
        } else {
            List<SyncJsonData> nextList = queryFromData(dataType);
            handleSyncOpt(dataType, nextList);

        }
    }

    /**
     * 查询
     *
     * @param dataType
     * @return
     */
    private List<SyncJsonData> queryFromData(DataType dataType) {
        return FTDBManager.get().queryDataByDataByTypeLimit(LIMIT_SIZE, dataType);
    }

    /**
     * 删除已经上传的数据
     *
     * @param list
     */
    private void deleteLastQuery(List<SyncJsonData> list) {
        List<String> ids = new ArrayList<>();
        for (SyncJsonData r : list) {
            ids.add(r.getId() + "");
        }
        FTDBManager.get().delete(ids);
    }

    /**
     * 上传数据
     *
     * @param dataType     数据类型
     * @param body         数据行协议结果
     * @param syncCallback 异步对象
     */
    public synchronized void requestNet(DataType dataType, String body, final AsyncCallback syncCallback) {
        String model;
        switch (dataType) {
            case TRACE:
                model = Constants.URL_MODEL_TRACING;
                break;
            case LOG:
                model = Constants.URL_MODEL_LOG;
                break;
            default:
            case RUM_APP:
            case RUM_WEBVIEW:
                model = Constants.URL_MODEL_RUM;
                break;
        }
        String content_type = "text/plain";
        FTResponseData result = HttpBuilder.Builder()
                .addHeadParam("Content-Type", content_type)
                .setModel(model)
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync();

        try {
            syncCallback.onResponse(result.getCode(), result.getMessage());
        } catch (Exception e) {
            LogUtils.e(TAG, "上传错误：\n" + Log.getStackTraceString(e));
            syncCallback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage());
        }

    }

    public void init(FTSDKConfig config) {
        isStop = false;
        dataSyncMaxRetryCount = config.getDataSyncRetryCount();
    }

    /**
     * 释放上传同步资源
     */
    public void release() {
        DataUploaderThreadPool.get().shutDown();
        mHandler.removeMessages(MSG_SYNC);
        isStop = true;
    }
}
