package com.ft.sdk.internal.anr.historical;

import java.util.List;

interface HistoricalRumContextStore {
    List<HistoricalRumContext> load(String processName) throws Exception;
}
