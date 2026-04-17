package com.ft.sdk.sessionreplay.internal.storage;


import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.feature.DataConsumerCallback;
import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;
import com.ft.sdk.storage.EventBatchWriter;

import java.util.Collections;
import java.util.Map;

public class SessionReplayResourcesWriter implements ResourcesWriter {

    private final FeatureSdkCore sdkCore;

    public SessionReplayResourcesWriter(FeatureSdkCore sdkCore) {
        this.sdkCore = sdkCore;
    }

    @Override
    public void write(EnrichedResource enrichedResource) {
        sdkCore.getFeature(Feature.SESSION_REPLAY_RESOURCES_FEATURE_NAME).withWriteContext(false,
                new DataConsumerCallback() {
                    @Override
                    public void onConsume(SessionReplayContext context, EventBatchWriter eventBatchWriter) {
                        synchronized (this) {
                            byte[] serializedMetadata = enrichedResource.asBinaryMetadata(
                                    context.getAppId(),
                                    getCurrentLinkContext()
                            );
                            eventBatchWriter.write(
                                    new RawBatchEvent(enrichedResource.getResource(), serializedMetadata),
                                    null,
                                    EventType.DEFAULT
                            );
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCurrentLinkContext() {
        Map<String, Object> rumContext = sdkCore.getFeatureContext(Feature.RUM_FEATURE_NAME);
        if (rumContext == null) {
            return Collections.emptyMap();
        }
        Object context = rumContext.get("globalContext");
        if (context instanceof Map) {
            return (Map<String, Object>) context;
        }
        return Collections.emptyMap();
    }
}
