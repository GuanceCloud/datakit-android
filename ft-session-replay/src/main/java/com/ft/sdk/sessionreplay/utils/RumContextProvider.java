package com.ft.sdk.sessionreplay.utils;

public interface RumContextProvider {
    /**
     * Returns the current RUM context.
     */
    SessionReplayRumContext getRumContext();
}
