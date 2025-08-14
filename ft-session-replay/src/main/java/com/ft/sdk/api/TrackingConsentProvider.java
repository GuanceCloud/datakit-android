package com.ft.sdk.api;

import androidx.annotation.NonNull;

import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;

import java.util.LinkedList;
import java.util.List;

public class TrackingConsentProvider implements ConsentProvider {

    private final List<TrackingConsentProviderCallback> callbacks = new LinkedList<>();

    private volatile TrackingConsent consent;

    public TrackingConsentProvider(@NonNull TrackingConsent consent) {
        this.consent = consent;
    }

    // region ConsentProvider

    @Override
    public synchronized @NonNull TrackingConsent getConsent() {
        return consent;
    }

    @Override
    public synchronized void setConsent(@NonNull TrackingConsent newConsent) {
        if (newConsent == this.consent) {
            return;
        }
        TrackingConsent previous = this.consent;
        this.consent = newConsent;
        notifyCallbacks(previous, newConsent);
    }

    @Override
    public synchronized void registerCallback(@NonNull TrackingConsentProviderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public synchronized void unregisterCallback(@NonNull TrackingConsentProviderCallback callback) {
        callbacks.remove(callback);
    }

    @Override
    public synchronized void unregisterAllCallbacks() {
        callbacks.clear();
    }

    // endregion

    // region Internal

    private void notifyCallbacks(@NonNull TrackingConsent previous, @NonNull TrackingConsent newConsent) {
        for (TrackingConsentProviderCallback callback : callbacks) {
            callback.onConsentUpdated(previous, newConsent);
        }
    }

    // endregion
}