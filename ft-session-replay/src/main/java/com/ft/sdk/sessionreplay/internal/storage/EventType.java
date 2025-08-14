package com.ft.sdk.sessionreplay.internal.storage;

public enum EventType {

    /** A generic customer event (e.g.: log, span, …). */
    DEFAULT,

    /** A customer event related to a crash. */
    CRASH,

    /** An internal telemetry event to monitor the SDK's behavior and performances. */
    TELEMETRY;
}
