package com.ft.sdk.garble;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description: SDK 内部请求配置
 */
public class FTHttpConfigManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTHttpConfigManager";
    private static volatile FTHttpConfigManager instance;
    /**
     * datakit 服务端请求地址
     */
    private String datakitUrl;

    /**
     * dataway 服务端请求地址
     */
    private String datawayUrl;


    /**
     * dataway 使用 token
     */
    private String clientToken;

    /**
     * 请求 http USER-AGENT header
     */
    private String userAgent;

    /**
     * http 发送连接时间
     */
    private int sendOutTime = 30000;

    private boolean compressIntakeRequests = false;

    /**
     * http 请求返回读取
     */
    private int readOutTime = 40000;//大于 Dataway Datakit 30秒时间

    private FTHttpConfigManager() {

    }

    public synchronized static FTHttpConfigManager get() {
        if (instance == null) {
            instance = new FTHttpConfigManager();
        }
        return instance;
    }

    /**
     * 配置初始化
     *
     * @param ftsdkConfig
     */
    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        String defaultUserAgent = System.getProperty("http.agent", "DefaultUA");
        userAgent = defaultUserAgent + " " + USER_AGENT + "/" + BuildConfig.FT_SDK_VERSION;
        sendOutTime = Math.max(sendOutTime, ftsdkConfig.getPageSize() * 1000);
        datakitUrl = ftsdkConfig.getDatakitUrl();
        datawayUrl = ftsdkConfig.getDatawayUrl();
        clientToken = ftsdkConfig.getClientToken();
        compressIntakeRequests = ftsdkConfig.isCompressIntakeRequests();

        if (!Utils.isNullOrEmpty(datakitUrl)) {
            LogUtils.d(TAG, "serverUrl ==>\nDatakit Url:" + datakitUrl);
        } else {
            String maskToken = StringUtils.maskHalfCharacter(clientToken);
            LogUtils.d(TAG, "serverUrl ==>  " + "\nDataway Url:"
                    + datawayUrl + ",clientToken:" + maskToken);
        }
    }

    public String getDatakitUrl() {
        return datakitUrl;
    }

    public String getDatawayUrl() {
        return datawayUrl;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getSendOutTime() {
        return sendOutTime;
    }

    public int getReadOutTime() {
        return readOutTime;
    }

    public boolean isCompressIntakeRequests() {
        return compressIntakeRequests;
    }

    /**
     * 释放 SDK 相关
     */
    public static void release() {
        instance = null;
    }
}
