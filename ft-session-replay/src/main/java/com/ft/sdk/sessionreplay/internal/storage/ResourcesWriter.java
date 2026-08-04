package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;

public interface ResourcesWriter {
    /**
     * Writes the resource to disk.
     * @param enrichedResource to write
     */
    void write(EnrichedResource enrichedResource);

    /**
     * Writes the resource to disk and reports whether the local write was accepted.
     */
    default void write(EnrichedResource enrichedResource, WriteCallback callback) {
        write(enrichedResource);
        if (callback != null) {
            callback.onComplete(true);
        }
    }

    interface WriteCallback {
        void onComplete(boolean success);
    }
}
