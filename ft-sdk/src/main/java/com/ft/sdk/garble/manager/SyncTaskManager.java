package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * BY huangDianHua
 * DATE:2019-12-05 20:41
 * Description:同步
 */
public class SyncTaskManager {
    private static volatile SyncTaskManager instance;
    private final int CLOSE_TIME = 5;
    private final int SLEEP_TIME = 10 * 1000;
    private volatile AtomicInteger closeCount = new AtomicInteger(0);
    private volatile boolean running;

    private SyncTaskManager() {

    }

    public synchronized static SyncTaskManager get() {
        if (instance == null) {
            instance = new SyncTaskManager();
        }
        return instance;
    }

    /**
     * 开启应用是自动开启轮询线程主动同步数据库数据
     */
    public void executeSyncPoll() {
        if (running) {
            return;
        }
        running = true;
        if (!ThreadPoolUtils.get().poolRunning()) {
            ThreadPoolUtils.get().reStartPool();
        }

        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(">>>同步轮询线程<<< 开始运行");
                List<RecordData> recordDataList = queryFromData();
                //当数据库中有数据是执行轮询同步操作
                while (recordDataList != null && !recordDataList.isEmpty()) {
                    if (FTActivityManager.get().isForeground()) {//程序在前台执行
                        LogUtils.d(">>>同步轮询线程<<< 程序正在 前 台执行同步操作");
                        closeCount.set(0);
                        handleSyncOpt(recordDataList);
                        recordDataList = queryFromData();
                    } else {//程序只是退到后台，延迟一段时间后再关闭同步线程
                        if (closeCount.getAndIncrement() < CLOSE_TIME) {
                            handleSyncOpt(recordDataList);
                            recordDataList = queryFromData();
                        } else {
                            recordDataList = null;
                        }
                        LogUtils.d(">>>同步轮询线程<<< 程序正在 后 台执行同步操作");
                    }
                }
                closeCount.set(0);
                running = false;
                LogUtils.d(">>>同步轮询线程<<< 结束运行");
            }
        });
    }

    /**
     * 点击触发延迟同步
     */
    public void executeSync() {
        if (running) {
            return;
        }
        running = true;
        if (!ThreadPoolUtils.get().poolRunning()) {
            ThreadPoolUtils.get().reStartPool();
        }
        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handleSyncOpt(null);
                running = false;
            }
        });
    }

    /**
     * 执行同步操作
     */
    private void handleSyncOpt(List<RecordData> requestDatas) {
        if (requestDatas == null || requestDatas.isEmpty()) {
            requestDatas = queryFromData();
        }
        if (requestDatas == null || requestDatas.isEmpty()) {
            return;
        }
        LogUtils.d("同步的数据" + requestDatas);
        try {
            Thread.sleep(2000);//模拟网络请求
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deleteLastQuery(requestDatas);
        LogUtils.d("同步后查询" + queryFromData());
    }

    private List<RecordData> queryFromData() {
        return FTDBManager.get().queryDataByDescLimit("10");
    }

    private void deleteLastQuery(List<RecordData> list) {
        List<String> ids = new ArrayList<>();
        for (RecordData r : list) {
            ids.add(r.getId() + "");
        }
        FTDBManager.get().delete(ids);
    }

}
