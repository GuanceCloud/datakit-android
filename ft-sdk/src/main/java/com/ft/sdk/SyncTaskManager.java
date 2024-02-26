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
import com.ft.sdk.internal.exception.FTNetworkNoAvailableException;

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
     * 传输间歇休眠时间
     */
    private static final int SLEEP_TIME = 10000;

    /**
     * 重试等待时间
     */
    private static final int RETRY_DELAY_SLEEP_TIME = 500;
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

    private boolean autoSync;

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

    private int pageSize = SyncPageSize.MEDIUM.getValue();

    /**
     * For AndroidTest
     */
    void executePoll() {
        executePoll(false);
    }

    private void executePoll(final boolean withSleep) {
        if (running || isStop) {
            return;
        }
        synchronized (this) {
            LogUtils.d(TAG, "******************* Execute Sync Poll *******************");
            running = true;
            errorCount.set(0);
            DataUploaderThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (withSleep) {
                            LogUtils.d(TAG, "******************* Sync Poll Waiting *******************>>>\n");
                            Thread.sleep(SLEEP_TIME);
                        }
                        LogUtils.d(TAG, "******************* Sync Poll Running *******************>>>\n");

                        for (DataType dataType : SYNC_MAP) {
                            SyncTaskManager.this.handleSyncOpt(dataType);
                        }

                    } catch (Exception e) {
                        if (e instanceof FTNetworkNoAvailableException) {
                            LogUtils.e(TAG, "Network not available Stop poll");
                        } else {
                            LogUtils.e(TAG, Log.getStackTraceString(e));

                        }
                    } finally {
                        running = false;
                        FTDBManager.get().closeDB();
                        LogUtils.d(TAG, "<<<******************* Sync Poll Finish *******************\n");
                    }
                }
            });
        }
    }

    /**
     * 触发延迟轮询同步
     */
    void executeSyncPoll() {
        if (autoSync) {
            mHandler.removeMessages(MSG_SYNC);
            mHandler.sendEmptyMessageDelayed(MSG_SYNC, 100);
        }
    }

    /**
     * 执行存储数据同步操作
     */
    private synchronized void handleSyncOpt(final DataType dataType) throws FTNetworkNoAvailableException {
        final List<SyncJsonData> requestDataList = new ArrayList<>();

        while (true) {
            List<SyncJsonData> cacheDataList = queryFromData(dataType);

            requestDataList.addAll(cacheDataList);

            if (requestDataList.isEmpty()) {
                break;
            }

            LogUtils.d(TAG, "Sync Data Count:" + requestDataList.size());

            SyncDataHelper syncData = new SyncDataHelper();
            String body = syncData.getBodyContent(dataType, requestDataList);
            LogUtils.d(TAG, body);
            requestNet(dataType, body, new AsyncCallback() {
                @Override
                public void onResponse(int code, String response, String errorCode) {
                    if (dataSyncMaxRetryCount == 0 || (code >= 200 && code < 500)) {
                        SyncTaskManager.this.deleteLastQuery(requestDataList);
                        if (dataType == DataType.LOG) {
                            FTDBCachePolicy.get().optCount(-requestDataList.size());
                        }
                        errorCount.set(0);
                        if ((dataSyncMaxRetryCount == 0 && code != 200) || code > 200) {
                            LogUtils.e(TAG, "Sync Fail (Ignore)-[code:" + code + ",errorCode:" + errorCode + ",response:" + response + "]");
                        } else {
                            LogUtils.d(TAG, "Sync Success-[code:" + code + ",response:" + response + "]");
                        }
                    } else {
                        errorCount.getAndIncrement();
                        LogUtils.e(TAG, errorCount.get() + ":Sync Fail-[code:" + code + ",response:" + response + "]");

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
            requestDataList.clear();

            if (dataSyncMaxRetryCount > 0) {
                if (errorCount.get() >= dataSyncMaxRetryCount) {
                    LogUtils.e(TAG, " \n************ Sync Fail: " + dataSyncMaxRetryCount + " times，stop poll ***********");
                    break;
                }
            }

            //当前缓存数据已获取完毕，等待下一次数据触发
            if (cacheDataList.size() < pageSize) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * 查询
     *
     * @param dataType
     * @return
     */
    private List<SyncJsonData> queryFromData(DataType dataType) {
        return FTDBManager.get().queryDataByDataByTypeLimit(pageSize, dataType);
    }

    /**
     * 删除已经上传的数据
     *
     * @param list
     */
    private void deleteLastQuery(List<SyncJsonData> list) {
        List<String> ids = new ArrayList<>();
        for (SyncJsonData r : list) {
            ids.add(String.valueOf(r.getId()));
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
    public synchronized void requestNet(DataType dataType, String body, final AsyncCallback syncCallback) throws FTNetworkNoAvailableException {
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

        if (result.getCode() == NetCodeStatus.NETWORK_EXCEPTION_CODE) {
            throw new FTNetworkNoAvailableException();
        }

        try {
            syncCallback.onResponse(result.getCode(), result.getMessage(), result.getErrorCode());
        } catch (Exception e) {
            LogUtils.e(TAG, "requestNet：\n" + Log.getStackTraceString(e));
            syncCallback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage(), "");
        }

    }

    public void init(FTSDKConfig config) {
        isStop = false;
        dataSyncMaxRetryCount = config.getDataSyncRetryCount();
        pageSize = config.getPageSize();
        autoSync = config.isAutoSync();
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
