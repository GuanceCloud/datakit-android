package com.ft.sdk.garble.manager;

import android.content.Context;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTHttpClient;
import com.ft.sdk.garble.http.HttpCallback;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LogUtils.d(">>>同步轮询线程<<< 开始运行");
                List<RecordData> recordDataList = queryFromData();
                //当数据库中有数据是执行轮询同步操作
                while (recordDataList != null && !recordDataList.isEmpty()) {
                    if (!Utils.isNetworkAvailable()) {
                        LogUtils.d(">>>网络未连接<<<");
                        break;
                    }
                    if(errorCount.get() >= CLOSE_TIME){
                        LogUtils.d(">>>连续同步失败5次，停止当前轮询同步<<<");
                        break;
                    }
                    if (FTActivityManager.get().isForeground()) {//程序在前台执行
                        LogUtils.d(">>>同步轮询线程<<< 程序正在 前 台执行同步操作");
                        handleSyncOpt(recordDataList);
                        recordDataList = queryFromData();
                    } else {//程序退到后台，关闭同步线程
                        recordDataList = null;
                        LogUtils.d(">>>同步轮询线程<<< 程序正在 后 台执行同步操作");
                    }
                }
                running = false;
                LogUtils.d(">>>同步轮询线程<<< 结束运行");
            }
        });
    }


    /**
     * 执行同步操作
     */
    private void handleSyncOpt(final List<RecordData> requestDatas) {
        if (requestDatas == null || requestDatas.isEmpty()) {
            return;
        }
        LogUtils.d("同步的数据" + requestDatas);
        //TODO 添加网络请求
        requestNet(requestDatas, new SyncCallback() {
            @Override
            public void isSuccess(boolean isSuccess) {
                if(isSuccess){
                    deleteLastQuery(requestDatas);
                    errorCount.set(0);
                }else{
                    errorCount.getAndIncrement();
                }
            }
        });
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

    private void requestNet(List<RecordData> requestDatas, final SyncCallback syncCallback) {
        FTHttpClient.Builder().setMethod(RequestMethod.POST).setBodyString(getBodyContent(requestDatas)).execute(new HttpCallback<ResponseData>() {
            @Override
            public void onComplete(ResponseData result) {
                syncCallback.isSuccess(result.getCode() == HttpURLConnection.HTTP_OK);
            }
        });
    }

    private String getBodyContent(List<RecordData> recordDatas) {
        StringBuffer sb = new StringBuffer();
        String device = parseHashToString(getDeviceInfo());
        for (RecordData recordData : recordDatas) {
            sb.append(device);
            sb.append(recordData.composeUpdateData());
            sb.append("\n");
        }
        return sb.toString();
    }

    private String parseHashToString(HashMap<String, Object> param) {
        StringBuffer sb = new StringBuffer();
        if (param != null) {
            Iterator<String> keys = param.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (keys.hasNext()) {
                    if (param.get(key) != null) {
                        sb.append(key + "=" + param.get(key) + ",");
                    } else {
                        sb.append(key + ",");
                    }
                } else {
                    if (param.get(key) != null) {
                        sb.append(key + "=" + param.get(key));
                    } else {
                        sb.append(key);
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * 获取必要的设备信息
     *
     * @return
     */
    private HashMap<String, Object> getDeviceInfo() {
        Context context = FTApplication.getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("mobile_tracker", null);
        objectHashMap.put("device_uuid", DeviceUtils.getUuid(context));
        objectHashMap.put("application_identifier", DeviceUtils.getApplicationId(context));
        objectHashMap.put("application_name", DeviceUtils.getAppName(context));
        objectHashMap.put("sdk_version", DeviceUtils.getSDKVersion());
        objectHashMap.put("imei", DeviceUtils.getImei(context));
        objectHashMap.put("os", DeviceUtils.getOSName());
        objectHashMap.put("os_version", DeviceUtils.getOSVersion());
        objectHashMap.put("device_band", DeviceUtils.getDeviceBand());
        objectHashMap.put("device_model", DeviceUtils.getDeviceModel());
        objectHashMap.put("display", DeviceUtils.getDisplay(context));
        objectHashMap.put("carrier", DeviceUtils.getCarrier(context));
        return objectHashMap;
    }

    interface SyncCallback {
        void isSuccess(boolean isSuccess);
    }
}
