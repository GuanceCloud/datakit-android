package com.ft.sdk.api.context;

import com.ft.sdk.sessionreplay.utils.MapUtils;

import java.util.HashMap;

public class SessionReplayContext {

    private final String env;
    private final String sdkVersion;
    private final String appId;
    private final String appVersion;

    public SessionReplayContext(
            String env,
            String sdkVersion,
            String appId, String appVersion) {
        this.env = env;
        this.sdkVersion = sdkVersion;
        this.appId = appId;
        this.appVersion = appVersion;
    }

    /**
     *
     * @param hashMap
     */
    public static SessionReplayContext createFromMap(HashMap<String, Object> hashMap) {
        String env = MapUtils.getString(hashMap, "env");
        String sdkVersion = MapUtils.getString(hashMap, "sdkVersion");
        String appId = MapUtils.getString(hashMap, "appId");
        String appVersion = MapUtils.getString(hashMap, "appVersion");
        return new SessionReplayContext(env, sdkVersion,
                appId, appVersion);
    }


    public String getAppVersion() {
        return appVersion;
    }


    public String getEnv() {
        return env;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getAppId() {
        return appId;
    }


}
