package com.ft.sdk.feature;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.storage.EventBatchWriter;

public abstract class DataConsumerCallback {

    public DataConsumerCallback() {
        this(false);
    }

    public DataConsumerCallback(boolean webviewType) {
        this.isWebview = webviewType;
    }

    boolean isWebview;

    public boolean isWebview() {
        return isWebview;
    }


    public abstract void onConsume(SessionReplayContext context, EventBatchWriter writer);
}
