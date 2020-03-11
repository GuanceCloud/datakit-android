package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
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
    private volatile AtomicInteger errorCount = new AtomicInteger(0);
    private volatile boolean running;

    /**
     * 警告!!! 该方法仅用于测试使用!!!
     *
     * @param running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    private SyncTaskManager() {

    }

    public synchronized static SyncTaskManager get() {
        if (instance == null) {
            instance = new SyncTaskManager();
        }
        return instance;
    }

    /**
     * 触发延迟轮询同步
     */
    public void executeSyncPoll() {
        if (running) {
            return;
        }
        running = true;
        if (!ThreadPoolUtils.get().poolRunning()) {
            ThreadPoolUtils.get().reStartPool();
        }
        errorCount.set(0);
        ThreadPoolUtils.get().execute(() -> {
            waitUserBind();
            List<RecordData> recordDataList = queryFromData();
            //当数据库中有数据是执行轮询同步操作
            while (recordDataList != null && !recordDataList.isEmpty()) {
                if (!Utils.isNetworkAvailable()) {
                    LogUtils.d(">>>网络未连接<<<");
                    break;
                }
                if (errorCount.get() >= CLOSE_TIME) {
                    LogUtils.d(">>>连续同步失败5次，停止当前轮询同步<<<");
                    break;
                }
                LogUtils.d(">>>同步轮询线程<<< 程序正在执行同步操作");
                handleSyncOpt(recordDataList);
                recordDataList = queryFromData();
            }
            running = false;
        });
    }

    /**
     * 等待用户绑定用户信息(阻塞方法)
     */
    private void waitUserBind() {
        int count = 0;
        do {
            if (count >= 1) {
                LogUtils.e("正在等待用户数据绑定...");
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            //如果开启了用户数据绑定，就等待用户绑定数据完成
        } while (FTUserConfig.get().isNeedBindUser() && !FTUserConfig.get().isUserDataBinded());
    }


    /**
     * 执行同步操作
     */
    private void handleSyncOpt(final List<RecordData> requestDatas) {
        if (requestDatas == null || requestDatas.isEmpty()) {
            return;
        }
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(requestDatas);
        printUpdateData(body);
        requestNet(body, isSuccess -> {
            if (isSuccess) {
                LogUtils.d("同步数据成功");
                deleteLastQuery(requestDatas);
                errorCount.set(0);
            } else {
                LogUtils.d("同步数据失败");
                errorCount.getAndIncrement();
            }
        });
        //LogUtils.d("同步后查询" + queryFromData());
    }

    private List<RecordData> queryFromData() {
        return FTDBManager.get().queryDataByDescLimit(10);
    }

    private void deleteLastQuery(List<RecordData> list) {
        List<String> ids = new ArrayList<>();
        for (RecordData r : list) {
            ids.add(r.getId() + "");
        }
        FTDBManager.get().delete(ids);
    }

    private void requestNet(String body, final SyncCallback syncCallback) {
        FTResponseData result = HttpBuilder.Builder()
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync(FTResponseData.class);

        try {
            syncCallback.isSuccess(result.getCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            syncCallback.isSuccess(false);
            LogUtils.e("请在混淆文件中添加 -keep class * extends com.ft.sdk.garble.http.ResponseData{\n" +
                    "     *;\n" +
                    "}");
        }

    }

    /**
     * 将上传的数据格式化（供打印日志使用）
     *
     * @param body
     */
    private void printUpdateData(String body) {
        try {
            StringBuffer sb = new StringBuffer();
            String[] counts = body.split("\n");
            for (String str : counts) {
                str = str.replaceAll("\\\\ ", "_");
                String[] strArr = str.split(" ");
                sb.append("{\n ");
                if (strArr.length == 3) {
                    sb.append("measurement{\n\t");
                    String str1 = strArr[0].replaceFirst(",", "\n },tags{\n\t");
                    str1 = str1.replaceAll(",", ",\n\t");
                    str1 = str1.replaceFirst(",\n\t", ",\n ");
                    sb.append(str1);
                    sb.append("\n },\n ");
                    sb.append("fields{\n\t");
                    String str2 = strArr[1].replaceAll(",", ",\n\t");
                    sb.append(str2);
                    sb.append("\n },\n ");
                    sb.append("time{\n\t");
                    sb.append(strArr[2]);
                    sb.append("\n }\n");
                }
                sb.append("},\n");
            }
            LogUtils.d("同步的数据\n" + sb.toString());
        } catch (Exception e) {
            LogUtils.d("同步的数据\n" + body);
        }
    }
}
