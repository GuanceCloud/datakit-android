package com.ft;

import android.content.Context;
import android.os.Bundle;

import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.ErrorMonitorType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.CrossProcessSetting;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.tencent.bugly.crashreport.CrashReport;
import com.uc.crashsdk.export.CrashApi;
import com.umeng.commonsdk.UMConfigure;

import java.util.HashMap;

import io.sentry.android.core.SentryAndroid;

/**
 * BY huangDianHua
 * DATE:2019-12-02 15:15
 * Description:
 */
public class DemoApplication extends BaseApplication {

    public DemoApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.LAZY_INIT) {
            LogUtils.registerInnerLogCacheToFile();
            initThirdParty();
            initFTSDK(this);
        }

    }

    private void initThirdParty() {
        //umeng
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "5f4366edd3093221547c3e73", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");

        final Bundle customInfo = new Bundle();
        customInfo.putBoolean("mCallNativeDefaultHandler", true);
        CrashApi.getInstance().updateCustomInfo(customInfo);


        SAConfigOptions saConfigOptions = new SAConfigOptions("http://127.0.0.1");
        saConfigOptions.enableTrackAppCrash();
// 需要在主线程初始化神策 SDK
        SensorsDataAPI.startWithConfigOptions(this, saConfigOptions);

        SentryAndroid.init(this, options -> {
            options.setDsn("___PUBLIC_DSN___");
            options.setEnvironment("production");
            options.setEnableUncaughtExceptionHandler(true);
            options.setEnabled(false);
        });

        CrashReport.initCrashReport(this,"d2e9a8c798",true);

    }

    static void initFTSDK(Context context) {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(BuildConfig.DATAWAY_URL, BuildConfig.CLIENT_TOKEN)
                .setDebug(true)//设置是否是 debug
                .setAutoSync(true)
                .setCustomSyncPageSize(100)
                .setOnlySupportMainProcess(CrossProcessSetting.isOnlyMainProcess(context))
                .setEnv(EnvType.valueOf(BuildConfig.ENV.toUpperCase()));
        FTSdk.install(ftSDKConfig);

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setSamplingRate(1f)
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setLogCacheLimitCount(10000)
                .setPrintCustomLogToConsole(true)
                .setLogLevelFilters(new Status[]{Status.ERROR,Status.DEBUG})
                .setEnableLinkRumData(true)
        );

//        LogUtils.registerInnerLogHandler(new FTInnerLogHandler() {
//            @Override
//            public void printInnerLog(String level, String tag, String logContent) {
//                if(level.equals("E")){
//                    FTLogger.getInstance().logBackground(logContent,Status.ERROR,true);
////                }
//            }
//        });

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setSamplingRate(1f)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
                .setEnableTraceUserResource(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
                .setResourceUrlHandler(url -> false)
//                .addGlobalContext("track_id", BuildConfig.TRACK_ID)
//                .addGlobalContext("custom_tag", "any tags")
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue()));


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
