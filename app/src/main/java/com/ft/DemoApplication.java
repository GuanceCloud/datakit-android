package com.ft;

import android.app.Application;
import android.content.Context;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;

/**
 * BY huangDianHua
 * DATE:2019-12-02 15:15
 * Description:
 */
public class DemoApplication extends Application {
    private static Context instance;

    public static Context getContext() {
        return instance;
    }

    public DemoApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initFTSDK();
    }

    private void initFTSDK() {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_URL))
                .setRumAppId(AccountUtils.getProperty(this,AccountUtils.RUM_APP_ID))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setMonitorType(MonitorType.ALL)//设置监控项
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppUIBlock(true)
                .setEnv(EnvType.GRAY)
                .setSamplingRate(1f)
                .setNetworkTrace(true)
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setTraceType(TraceType.ZIPKIN)
                .setOnlySupportMainProcess(true);
        FTSdk.install(ftSDKConfig);

        FTSdk.get().bindUserData("brandon.test.userid");
    }

}
