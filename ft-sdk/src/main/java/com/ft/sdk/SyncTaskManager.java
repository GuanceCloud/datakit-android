package com.ft.sdk;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
 * Description:同步
 */
public class SyncTaskManager {
    public final static String TAG = "SyncTaskManager";
    private static final int CLOSE_TIME = 5;
    private static final int LIMIT_SIZE = 10;
    private static final int SLEEP_TIME = 10 * 1000;
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private volatile boolean running;

    private static final int MSG_SYNC = 1;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SYNC) {
                executePoll(true);
            }
        }
    };


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

    private void executePoll(boolean withSleep) {
        if (running) {
            return;
        }
        synchronized (this) {
            LogUtils.d(TAG, "=========executeSyncPoll===");
            running = true;
            errorCount.set(0);
            DataUploaderThreadPool.get().execute(() -> {
                try {

                    LogUtils.d(TAG, " \n*******************************************************\n" +
                            "******************数据同步线程运行中*******************\n" +
                            "*******************************************************\n");
                    if (withSleep) {
                        Thread.sleep(SLEEP_TIME);
                    }

                    for (DataType dataType : SYNC_MAP) {
                        List<SyncJsonData> dataList = queryFromData(dataType);

                        if (dataList.isEmpty()) {
                            continue;
                        }
                        handleSyncOpt(dataType, dataList);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    running = false;
                    LogUtils.d(TAG, " \n********************************************************\n" +
                            "******************数据同步线程已结束********************\n" +
                            "********************************************************\n");
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

        if (errorCount.get() >= CLOSE_TIME) {
            LogUtils.e(TAG, " \n************连续同步失败5次，停止当前轮询同步***********");
            return;
        }

        if (requestDatas == null || requestDatas.isEmpty()) {
            return;
        }
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(dataType, requestDatas);
        LogUtils.d(TAG, body);
        requestNet(dataType, body, (code, response) -> {
            if (code >= 200 && code < 500) {
                LogUtils.d(TAG, "\n**********************同步数据成功**********************");
                deleteLastQuery(requestDatas);
                if (dataType == DataType.LOG) {
                    FTDBCachePolicy.get().optCount(-requestDatas.size());
                }
                errorCount.set(0);
                if (code > 200) {
                    LogUtils.e(TAG, "同步数据出错(忽略)-[code:" + code + ",response:" + response + "]");
                }
            } else {
                LogUtils.e(TAG, "同步数据失败-[code:" + code + ",response:" + response + "]");
                errorCount.getAndIncrement();
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
     * @param dataType
     * @param body
     * @param syncCallback
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
                .setBodyString(body).executeSync(FTResponseData.class);

        try {
            syncCallback.onResponse(result.getCode(), result.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            syncCallback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage());
            LogUtils.e(TAG, "上传错误：" + e.getLocalizedMessage());
        }

    }

    public static void release() {
        DataUploaderThreadPool.get().shutDown();
    }
}
