package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;

public class NoOpResourcesWriter implements ResourcesWriter {

    @Override
    public void write(EnrichedResource enrichedResource) {
        // no-op
    }

    @Override
    public void write(EnrichedResource enrichedResource, WriteCallback callback) {
        if (callback != null) {
            callback.onComplete(false);
        }
    }
}
