package com.ft.sdk.garble.manager;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.utils.AnrWatch;

/**
 * author: huangDianHua
 * time: 2020/9/28 16:27:14
 * description: Anr 监控管理类
 */
public class FTAnrWatchManager {
    private static FTAnrWatchManager mInstance;
    private AnrWatch anrWatch;

    private FTAnrWatchManager() {

    }

    public synchronized static FTAnrWatchManager getInstance() {
        if (mInstance == null) {
            mInstance = new FTAnrWatchManager();
        }
        return mInstance;
    }

    public synchronized void startMonitorAnr() {
        anrWatch = new AnrWatch.Builder().timeout(5000)
                .anrListener(new AnrWatch.AnrListener() {
                    @Override
                    public void onAnrHappened() {
                        FTAutoTrack.appAnr();
                    }
                }).build();
        anrWatch.start();
    }

    public synchronized void stopMonitorAnr(){
        if(anrWatch != null) {
            anrWatch.stopAnrWatch();
        }
    }

    public static void release(){
        if(mInstance != null){
            mInstance.stopMonitorAnr();
            mInstance = null;
        }
    }

}
