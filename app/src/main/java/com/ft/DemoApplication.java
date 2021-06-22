package com.ft;

import android.app.Application;
import android.content.Context;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceConfigManager;
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
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this,
                AccountUtils.ACCESS_SERVER_URL))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setSamplingRate(1f)
                .setEnableLinkRumData(true)
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setEnableLinkRumData(true)
        );

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setSamplingRate(1f)
                .setRumAppId(AccountUtils.getProperty(this, AccountUtils.RUM_APP_ID))
                .setEnableTraceUserAction(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setExtraMonitorTypeWithError(MonitorType.ALL));

        FTSdk.bindRumUserData("brandon.test.userid");


        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setSamplingRate(1f)
                .setEnableLinkRUMData(true)
                .setTraceType(TraceType.ZIPKIN));

    }

}
