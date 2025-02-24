package com.ft;

import android.content.Context;

import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.ErrorMonitorType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.RUMCacheDiscard;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.material.MaterialExtensionSupport;
import com.ft.utils.CrossProcessSetting;

import java.util.HashMap;

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
            initFTSDK(this);
        }
    }


    static void initFTSDK(Context context) {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(BuildConfig.DATAKIT_URL)
                .setDebug(true)//设置是否是 debug
                .setAutoSync(true)
                .setCustomSyncPageSize(100)
                .setOnlySupportMainProcess(CrossProcessSetting.isOnlyMainProcess(context))
                .setNeedTransformOldCache(true)
                .setCompressIntakeRequests(true)
                .setDataSyncRetryCount(0)
//                .enableLimitWithDbSize(1024 * 1024)
//                .setDbCacheDiscard(DBCacheDiscard.DISCARD_OLDEST)
                .setEnv(EnvType.valueOf(BuildConfig.ENV.toUpperCase()));
        FTSdk.install(ftSDKConfig);

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setSamplingRate(1f)
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setLogCacheLimitCount(1000)
                .setLogCacheDiscardStrategy(LogCacheDiscard.DISCARD)
                .setPrintCustomLogToConsole(true)
                .setLogLevelFilters(new Status[]{Status.ERROR, Status.DEBUG})
                .setEnableLinkRumData(true)
        );


        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setSamplingRate(1f)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
                .setRumCacheLimitCount(1000)
                .setRumCacheDiscardStrategy(RUMCacheDiscard.DISCARD)
                .setEnableTraceUserResource(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true, 100)
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
                .setTraceType(TraceType.JAEGER));

        FTSdk.initSessionReplayConfig(new FTSessionReplayConfig().setSampleRate(1f)
                .setPrivacy(SessionReplayPrivacy.ALLOW)
                .addExtensionSupport(new MaterialExtensionSupport()));

    }

}
