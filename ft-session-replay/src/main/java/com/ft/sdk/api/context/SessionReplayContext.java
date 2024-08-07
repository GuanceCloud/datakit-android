package com.ft.sdk.api.context;

import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;

public class SessionReplayContext {

    private final String requestUrl;
    private final String env;
    private final String sdkVersion;
    private final TrackingConsent trackingConsent;
    private final String appId;

    public SessionReplayContext(String requestUrl,
                                String env,
                                String sdkVersion,
                                TrackingConsent trackingConsent,
                                String appId) {
        this.requestUrl = requestUrl;
        this.env = env;
        this.sdkVersion = sdkVersion;
        this.trackingConsent = trackingConsent;
        this.appId = appId;
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
