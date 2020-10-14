package com.ft.sdk.garble.manager;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.AnrWatch;

/**
 * author: huangDianHua
 * time: 2020/9/28 16:27:14
 * description: Anr 监控管理类
 */
public class FTAnrWatchManager {
    private static FTAnrWatchManager mInstance;
    private AnrWatch anrWatch;
    private FTSDKConfig mConfig;

    private FTAnrWatchManager() {

    }

    public synchronized static FTAnrWatchManager getInstance() {
        if (mInstance == null) {
            mInstance = new FTAnrWatchManager();
        }
        return mInstance;
    }

    public synchronized void startMonitorAnr(FTSDKConfig config) {
        if(!config.isEnableTrackAppANR()){
            return;
        }
        mConfig = config;


        anrWatch = new AnrWatch.Builder().timeout(5000)
                .anrListener(() -> {
                    FTAutoTrack.appAnr();
                    LogBean logBean = new LogBean("------ ANR ERROR ------", System.currentTimeMillis());
                    logBean.setStatus(Status.CRITICAL);
                    logBean.setEnv(mConfig.getEnv());
                    logBean.setServiceName(mConfig.getTraceServiceName());
                    FTTrackInner.getInstance().logBackground(logBean);
                }).build();
        anrWatch.start();
    }

    public synchronized void stopMonitorAnr() {
        if (anrWatch != null) {
            anrWatch.stopAnrWatch();
        }
    }

    public static void release() {
        if (mInstance != null) {
            mInstance.stopMonitorAnr();
            mInstance = null;
        }
    }

}
