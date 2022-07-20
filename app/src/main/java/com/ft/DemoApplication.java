package com.ft;

import android.app.Application;
import android.content.Context;

import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.ErrorMonitorType;
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
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setSamplingRate(1f)
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setEnableLinkRumData(true)
        );

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setSamplingRate(1f)
                .setRumAppId(AccountUtils.getProperty(this, AccountUtils.RUM_APP_ID))
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
                .setEnableTraceUserResource(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL)
                .addGlobalContext("track_id", AccountUtils.getProperty(this, AccountUtils.TRACK_ID))
                .addGlobalContext("custom_tag", "any tags")
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL));

        FTSdk.bindRumUserData("brandon.test.userid");


        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setSamplingRate(1f)
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(true)
                .setTraceType(TraceType.DDTRACE));

    }

}
