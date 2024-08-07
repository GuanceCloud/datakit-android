package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.storage.EventBatchWriter;

public interface EventBatchWriterCallback {
    void callBack(EventBatchWriter writer);
}
