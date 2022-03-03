package com.ft.sdk.garble.service;

import com.ft.sdk.FTMonitorConfigManager;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.net.HttpURLConnection;

/**
 * create: by huangDianHua
 * time: 2020/4/28 15:00:02
 * description:
 */
public class FTMonitorManager {
    public static final String TAG = "FTMonitorManager";
    //轮训周期
    private int period = 10;
    private static FTMonitorManager instance;
    private MonitorThread mThread;

    private FTMonitorManager() {
    }

    public static FTMonitorManager install(int period) {
        if (instance == null) {
            instance = new FTMonitorManager();
        }
        instance.period = period;
        instance.stopMonitor();
        instance.startMonitor();
        return instance;
    }

    public static FTMonitorManager get() {
        return instance;
    }

    /**
     * 开启监控
     */
    public void startMonitor() {
        if (FTMonitorConfigManager.get().getMonitorType() == 0) {
            LogUtils.e(TAG, "没有设置监控项，无法启用监控");
        } else {
            mThread = new MonitorThread("监控轮训", period);
            mThread.start();
            LogUtils.d(TAG, "监控轮训线程启动...");
        }
    }

    /**
     * 停止监控
     */
    private void stopMonitor() {
        if (mThread != null && mThread.isAlive()) {
            LogUtils.d(TAG, "关闭监控轮训线程");
            mThread.interrupt();
            mThread = null;
        }
    }

    public void release() {
        stopMonitor();
        instance = null;
    }

    static class MonitorThread extends Thread {
        //轮训周期
        private int period;

        public MonitorThread(String name, int period) {
            setName(name);
            this.period = period;
        }

        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    Thread.sleep(period * 1000);
//                    if (!TokenCheck.get().checkToken()) {
//                        continue;
//                    }
                    try {
                        String body = new SyncDataHelper().getMonitorUploadData();
                        SyncDataHelper.printUpdateData(false, body);
                        ResponseData result = HttpBuilder.Builder()
                                .setModel(Constants.URL_MODEL_TRACK_INFLUX)
                                .setMethod(RequestMethod.POST)
                                .setBodyString(body).executeSync(ResponseData.class);
                        if (result.getHttpCode() != HttpURLConnection.HTTP_OK) {
                            LogUtils.d(TAG, "监控轮训线程上传数据出错(message：" + result.getData() + ")");
                        } else {
                            System.out.println("轮训监控上报数据成功");
                        }
                    } catch (Exception e) {
                        LogUtils.d(TAG, "监控轮训线程执行错误(message：" + e.getMessage() + ")");
                    }
                }
            } catch (InterruptedException e) {
                LogUtils.w(TAG, "监控轮训线程被关闭");
            }
        }
    }
}
