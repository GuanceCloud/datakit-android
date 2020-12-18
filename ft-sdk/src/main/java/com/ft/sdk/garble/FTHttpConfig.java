package com.ft.sdk.garble;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.http.EngineFactory;
import com.ft.sdk.garble.utils.DeviceUtils;

import java.util.Arrays;
import java.util.List;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description:
 */
public class FTHttpConfig {
    private static volatile FTHttpConfig instance;
    public String serverUrl;
    public boolean enableRequestSigning;
    public String akId;
    public String akSecret;
//    public String dataWayToken;//非必须参数，Sass 版本
    public String version;
    public String uuid;
    public String userAgent;
    public boolean useOaid;
    public int sendOutTime = 10 * 1000;
    public int readOutTime = 10 * 1000;
    //是否开启网络日志上报
    public boolean networkTrace;
    public TraceType traceType;
    //支持的采集类型
    public List<String> traceContentType = Arrays.asList("application/json",
            "application/javascript", "application/xml", "application/x-www-form-urlencoded",
            "text/html", "text/xml", "text/plain",
            "multipart/form-data"
    );

    private FTHttpConfig() {

    }

    public synchronized static FTHttpConfig get() {
        if (instance == null) {
            instance = new FTHttpConfig();
        }
        return instance;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        serverUrl = ftsdkConfig.getServerUrl();
        useOaid = ftsdkConfig.isUseOAID();
        version = BuildConfig.FT_SDK_VERSION;
        uuid = DeviceUtils.getSDKUUid(FTApplication.getApplication());
        userAgent = USER_AGENT;
        EngineFactory.setNetWorkTrace(ftsdkConfig.isNetworkTrace());
        networkTrace = ftsdkConfig.isNetworkTrace();
        traceType = ftsdkConfig.getTraceType();
        if (ftsdkConfig.getTraceContentType() != null) {
            traceContentType = ftsdkConfig.getTraceContentType();
        }
    }

    public static void release() {
        instance = null;
    }
}
