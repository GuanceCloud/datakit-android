package com.ft;

import android.app.Application;
import android.content.Context;

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
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setNeedBindUser(false)//是否需要绑定用户信息
                .setMonitorType(MonitorType.ALL)//设置监控项
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppUIBlock(true)
                .setEnv("dev")
                .setTraceSamplingRate(1f)
                .setNetworkTrace(true)
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setTraceType(TraceType.SKYWALKING_V2)
                .setOnlySupportMainProcess(true);
        FTSdk.install(ftSDKConfig);
    }

}
