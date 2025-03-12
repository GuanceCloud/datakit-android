package com.ft.sdk.api.context;

import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.utils.MapUtils;

import java.util.HashMap;

public class SessionReplayContext {

    private final String requestUrl;
    private final String env;
    private final String sdkVersion;
    private final TrackingConsent trackingConsent;
    private final String appId;
    private final String userAgent;
    private final String appVersion;

    public SessionReplayContext(String requestUrl,
                                String env,
                                String sdkVersion,
                                TrackingConsent trackingConsent,
                                String appId) {
        this(requestUrl, env, sdkVersion, trackingConsent, appId, "", "");
    }

    public SessionReplayContext(String requestUrl,
                                String env,
                                String sdkVersion,
                                TrackingConsent trackingConsent,
                                String appId, String userAgent, String appVersion) {
        this.requestUrl = requestUrl;
        this.env = env;
        this.sdkVersion = sdkVersion;
        this.trackingConsent = trackingConsent;
        this.appId = appId;
        this.userAgent = userAgent;
        this.appVersion = appVersion;
    }

    /**
     * SDK 跨版本
     *
     * @param hashMap
     */
    public static SessionReplayContext createFromMap(HashMap<String, Object> hashMap) {
        String requestUrl = MapUtils.getString(hashMap, "requestUrl");
        String env = MapUtils.getString(hashMap, "env");
        String sdkVersion = MapUtils.getString(hashMap, "sdkVersion");
        String trackingConsentString = MapUtils.getString(hashMap, "trackingConsent");
        TrackingConsent trackingConsent = TrackingConsent.valueOf(trackingConsentString.toUpperCase());
        String appId = MapUtils.getString(hashMap, "appId");
        String userAgent = MapUtils.getString(hashMap, "userAgent");
        String appVersion = MapUtils.getString(hashMap, "appVersion");
        return new SessionReplayContext(requestUrl, env, sdkVersion, trackingConsent,
                appId, userAgent, appVersion);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getEnv() {
        return env;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public TrackingConsent getTrackingConsent() {
        return trackingConsent;
    }

    public String getAppId() {
        return appId;
    }


}
