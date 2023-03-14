package com.ft.sdk.garble;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description: SDK 内部请求配置
 */
public class FTHttpConfigManager {
    private static final String TAG = "[FT-SDK]FTHttpConfigManager";
    private static volatile FTHttpConfigManager instance;
    /**
     *  datakit 服务端请求地址
     */
    public String serverUrl;
    /**
     * 64位 随机uuid
     */
    public String uuid;
    /**
     * 请求 http USER-AGENT header
     */
    public String userAgent;

    /**
     * http 发送连接时间
     */
    public int sendOutTime = 10000;
    /**
     * http 请求返回读取
     */
    public int readOutTime = 10000;


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
     * @param ftsdkConfig
     */
    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        serverUrl = ftsdkConfig.getMetricsUrl();
        uuid = DeviceUtils.getSDKUUid(FTApplication.getApplication());
        userAgent = USER_AGENT;

        LogUtils.d(TAG, "serverUrl:" + serverUrl);

    }


    /**
     * 释放 SDK 相关
     */
    public static void release() {
        instance = null;
    }
}
