package com.ft.sdk.garble;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.Proxy;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description: SDK internal request configuration
 */
public class FTHttpConfigManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTHttpConfigManager";
    private static volatile FTHttpConfigManager instance;
    /**
     * datakit server request address
     */
    private String datakitUrl;

    /**
     * dataway server request address
     */
    private String datawayUrl;


    /**
     * dataway token
     */
    private String clientToken;

    /**
     * Request http USER-AGENT header
     */
    private String userAgent;

    /**
     * http send connection time
     */
    private int sendOutTime = 30000;

    private boolean compressIntakeRequests = false;

    /**
     * http request return read
     */
    private int readOutTime = 40000;//Greater than Dataway Datakit 30 second time

    private FTHttpConfigManager() {

    }

    public synchronized static FTHttpConfigManager get() {
        if (instance == null) {
            instance = new FTHttpConfigManager();
        }
        return instance;
    }

    private Object dns;
    private Proxy proxy;
    private Object authenticator;

    public Object getDns() {
        return dns;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Object getAuthenticator() {
        return authenticator;
    }

    /**
     * Configuration initialization
     *
     * @param ftsdkConfig
     */
    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        userAgent = USER_AGENT + "/" + BuildConfig.FT_SDK_VERSION;
        sendOutTime = Math.max(sendOutTime, ftsdkConfig.getPageSize() * 1000);
        datakitUrl = ftsdkConfig.getDatakitUrl();
        datawayUrl = ftsdkConfig.getDatawayUrl();
        clientToken = ftsdkConfig.getClientToken();
        compressIntakeRequests = ftsdkConfig.isCompressIntakeRequests();
        dns = ftsdkConfig.getDns();
        proxy = ftsdkConfig.getProxy();
        authenticator = ftsdkConfig.getAuthenticator();

        if (!Utils.isNullOrEmpty(datakitUrl)) {
            LogUtils.d(TAG, "serverUrl ==>\nDatakit Url:" + datakitUrl);
        } else {
            String maskToken = StringUtils.maskHalfCharacter(clientToken);
            LogUtils.d(TAG, "serverUrl ==>  " + "\nDataway Url:"
                    + datawayUrl + ",clientToken:" + maskToken);
        }
    }

    public void setCompressIntakeRequests(boolean compressIntakeRequests) {
        this.compressIntakeRequests = compressIntakeRequests;
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

    public String getUserAgentForSR() {
        return userAgent + " (Mode=Replay; Version=" + com.ft.sdk.sessionreplay.BuildConfig.VERSION_NAME + ")";
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
     * Release SDK related
     */
    public static void release() {
        instance = null;
    }
}
