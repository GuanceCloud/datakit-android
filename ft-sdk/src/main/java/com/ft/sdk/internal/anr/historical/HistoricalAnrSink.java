package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.db.InsertResult;

import java.util.HashMap;

interface HistoricalAnrSink {
    InsertResult persist(long occurredAtNs,
                         HashMap<String, Object> tags,
                         HashMap<String, Object> fields,
                         String dedupeKey,
                         String viewId,
                         CollectType collectType);
}
