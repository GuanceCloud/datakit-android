package com.ft.sdk.sessionreplay.internal.storage;


import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.feature.DataConsumerCallback;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;
import com.ft.sdk.storage.EventBatchWriter;

public class SessionReplayResourcesWriter implements ResourcesWriter {

    private final FeatureSdkCore sdkCore;
    private static final String SESSION_REPLAY_RESOURCES_FEATURE_NAME = "session-replay-resources";

    public SessionReplayResourcesWriter(FeatureSdkCore sdkCore) {
        this.sdkCore = sdkCore;
    }

    @Override
    public void write(EnrichedResource enrichedResource) {
        sdkCore.getFeature(SESSION_REPLAY_RESOURCES_FEATURE_NAME).withWriteContext(false,
                new DataConsumerCallback() {
                    @Override
                    public void onConsume(SessionReplayContext context, EventBatchWriter eventBatchWriter) {
                        synchronized (this) {
                            byte[] serializedMetadata = enrichedResource.asBinaryMetadata();
                            eventBatchWriter.write(
                                    new RawBatchEvent(enrichedResource.getResource(), serializedMetadata),
                                    null,
                                    EventType.DEFAULT
                            );
                        }
                    }
                });
    }
}
