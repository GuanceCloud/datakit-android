package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;

/**
 * Provides the context captured for the snapshot that requested a resource.
 */
public interface ResourceContextProvider {
    RecordedQueuedItemContext getResourceContext();
}
