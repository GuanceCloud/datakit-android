package com.ft.sdk.feature;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.storage.EventBatchWriter;

public interface DataConsumerCallback {

    void onConsume(SessionReplayContext context, EventBatchWriter writer);
}
