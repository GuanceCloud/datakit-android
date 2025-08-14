package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;

public interface ResourcesWriter {
    /**
     * Writes the resource to disk.
     * @param enrichedResource to write
     */
    void write(EnrichedResource enrichedResource);
}
