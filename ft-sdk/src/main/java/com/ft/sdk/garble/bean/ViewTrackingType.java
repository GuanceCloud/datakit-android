package com.ft.sdk.garble.bean;

/** Lifecycle ownership for Views reported by framework integrations. */
public enum ViewTrackingType {
    /** The caller controls the View lifecycle, including background transitions. */
    MANUAL,
    /** Native closes the View on background; the caller starts a new View on foreground. */
    AUTOMATIC
}
