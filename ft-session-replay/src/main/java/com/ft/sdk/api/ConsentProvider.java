package com.ft.sdk.api;

import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;

public interface ConsentProvider {

    TrackingConsent getConsent();

    void setConsent(TrackingConsent consent);

    void registerCallback(TrackingConsentProviderCallback callback);

    void unregisterCallback(TrackingConsentProviderCallback callback);

    void unregisterAllCallbacks();
}