package com.ft.sdk.internal.anr.historical;

import java.util.List;

interface HistoricalRumContextStore {
    default void ensureReady() throws Exception {
    }

    List<HistoricalRumContext> load(String processName) throws Exception;
}
