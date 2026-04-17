package com.ft.sdk.sessionreplay.internal.persistence;

public enum TrackingConsent {
    /**
     * The permission to persist and dispatch data to the Datadog Endpoints was granted.
     * Any previously stored pending data will be marked as ready for sent.
     */
    GRANTED,

    SAMPLED_ON_ERROR_SESSION,

    /**
     * Any previously stored pending data will be deleted and any Log, Rum, Trace event will
     * be dropped from now on without persisting it in any way.
     */
    NOT_GRANTED,

    /**
     * Any Log, Rum, Trace event will be persisted in a special location and will be pending there
     * until we receive one of the [TrackingConsent.GRANTED] or
     * [TrackingConsent.NOT_GRANTED] flags.
     * Based on the value of the consent flag we will decide what to do
     * with the pending stored data.
     */
    PENDING
}