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
import com.ft.sdk.LineDataModifier;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.RUMCacheDiscard;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.CrossProcessSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
                .setCustomSyncPageSize(10)
                .setOnlySupportMainProcess(CrossProcessSetting.isOnlyMainProcess(context))
                .setNeedTransformOldCache(true)
                .setCompressIntakeRequests(true)
                .setSyncSleepTime(100)
//                .setDataModifier(new DataModifier() {
//                    @Override
//                    public Object modify(String key, Object value) {
//                        if(key.equals("userid")){
//                            return "xxx";
//                        }
//                        return value;
//                    }
//                })
//                .enableLimitWithDbSize(1024 * 1024)
//                .setDbCacheDiscard(DBCacheDiscard.DISCARD_OLDEST)
//                .setLineDataModifier(new LineDataModifier() {
//                    @Override
//                    public Map<String, Object> modify(String measurement, HashMap<String, Object> data) {
//                        if (measurement.equals("view")) {
//                            Object viewName = data.get("view_name");
//                            if (Objects.equals(viewName, "DebugMainActivity")) {
//                                HashMap<String, Object> changeValue = new HashMap<>();
//                                changeValue.put("view_name", "xxx");
//                                return changeValue;
//                            }
//                        }
//                        return null;
//
//                    }
//                })
                .setEnv(EnvType.valueOf(BuildConfig.ENV.toUpperCase()));
//        try {
//            URL url = new URL(BuildConfig.PROXY_ADDRESS);
//            ftSDKConfig.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), url.getPort())));
//
//        } catch (MalformedURLException ignored) {
//        }

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
                .setTraceType(TraceType.DDTRACE));


    }

}
