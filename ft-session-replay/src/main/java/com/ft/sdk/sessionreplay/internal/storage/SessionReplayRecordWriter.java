package com.ft.sdk.sessionreplay.internal.storage;


import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.feature.DataConsumerCallback;
import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.internal.RecordCallback;
import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;
import com.ft.sdk.storage.EventBatchWriter;

import kotlin.text.Charsets;

public class SessionReplayRecordWriter implements RecordWriter {

    private static final String TAG = "SessionReplayRecordWriter";

    private final FeatureSdkCore sdkCore;
    private final RecordCallback recordCallback;
    private String viewId = "";

    public SessionReplayRecordWriter(FeatureSdkCore sdkCore, RecordCallback recordCallback) {
        this.sdkCore = sdkCore;
        this.recordCallback = recordCallback;
    }

    @Override
    public void write(EnrichedRecord record) {
        boolean forceNew = !viewId.equals(record.getViewId());
        if (forceNew) {
            viewId = record.getViewId();
            sdkCore.getInternalLogger().i(TAG, "forceNew:viewId:" + viewId);
        }

        sdkCore.getFeature(Feature.SESSION_REPLAY_FEATURE_NAME).withWriteContext(forceNew, new DataConsumerCallback() {
            @Override
            public void onConsume(SessionReplayContext context, EventBatchWriter writer) {
                byte[] serializedRecord = record.toJson().getBytes(Charsets.UTF_8);
                RawBatchEvent rawBatchEvent = new RawBatchEvent(serializedRecord, null);
                synchronized (this) {
                    if (writer.write(rawBatchEvent, null, EventType.DEFAULT)) {
                        updateViewSent(record);
                    }
                }
            }
        });
    }

    private void updateViewSent(EnrichedRecord record) {

        recordCallback.onRecordForViewSent(record);

    }
}
