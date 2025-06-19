package com.ft.sdk.api;

import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;

public class NoOpConsentProvider implements ConsentProvider{
    @Override
    public TrackingConsent getConsent() {
        return TrackingConsent.GRANTED;
    }

    @Override
    public void setConsent(TrackingConsent consent) {

    }

    @Override
    public void registerCallback(TrackingConsentProviderCallback callback) {

    }

    @Override
    public void unregisterCallback(TrackingConsentProviderCallback callback) {

    }

    @Override
    public void unregisterAllCallbacks() {

    }
}
