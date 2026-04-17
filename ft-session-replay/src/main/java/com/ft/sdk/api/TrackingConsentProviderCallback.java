package com.ft.sdk.api;

import androidx.annotation.NonNull;

import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;

public interface TrackingConsentProviderCallback {

    /**
     * Notifies whenever the {@link TrackingConsent} was changed.
     *
     * @param previousConsent the previous value of the {@link TrackingConsent}
     * @param newConsent      the new value of the {@link TrackingConsent}
     */
    void onConsentUpdated(@NonNull TrackingConsent previousConsent, @NonNull TrackingConsent newConsent);
}