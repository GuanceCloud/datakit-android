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
import com.ft.sdk.garble.bean.UserData;

import java.util.HashMap;

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
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(BuildConfig.ACCESS_SERVER_URL)
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.valueOf(BuildConfig.ENV.toUpperCase()));
        FTSdk.install(ftSDKConfig);

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setSamplingRate(1f)
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setEnableLinkRumData(true)
        );

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setSamplingRate(1f)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
                .setEnableTraceUserResource(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL)
                .addGlobalContext("track_id", BuildConfig.TRACK_ID)
                .addGlobalContext("custom_tag", "any tags")
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL));


        UserData userData = new UserData();
        userData.setName("brandon");
        userData.setId("brandon.test.userid");
        userData.setEmail("brandon@mail.com");
        HashMap<String, String> extMap = new HashMap<>();
        extMap.put("ft_key", "ft_value");
        userData.setExts(extMap);
        FTSdk.bindRumUserData(userData);

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setSamplingRate(1f)
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(true)
                .setTraceType(TraceType.DDTRACE));

    }

}
