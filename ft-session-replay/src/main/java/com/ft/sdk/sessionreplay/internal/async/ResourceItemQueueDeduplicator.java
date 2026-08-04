package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.resources.ResourceUploadKey;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps resource queue deduplication scoped to the main space or one wgt_id.
 */
public class ResourceItemQueueDeduplicator {
    private static final int DEFAULT_MAX_ENTRIES = 10_000;

    private final int maxEntries;
    private final LinkedHashMap<ResourceUploadKey, State> queuedResources;

    public ResourceItemQueueDeduplicator() {
        this(DEFAULT_MAX_ENTRIES);
    }

    ResourceItemQueueDeduplicator(int maxEntries) {
        this.maxEntries = maxEntries;
        this.queuedResources = new LinkedHashMap<ResourceUploadKey, State>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ResourceUploadKey, State> eldest) {
                return size() > ResourceItemQueueDeduplicator.this.maxEntries;
            }
        };
    }

    public synchronized boolean shouldQueue(String resourceHash, SessionReplayRumContext rumContext) {
        ResourceUploadKey uploadKey = ResourceUploadKey.from(resourceHash, rumContext);
        if (queuedResources.get(uploadKey) != null) {
            return false;
        }

        queuedResources.put(uploadKey, State.PENDING);
        return true;
    }

    public synchronized void onWriteFinished(String resourceHash,
                                             SessionReplayRumContext rumContext,
                                             boolean success) {
        ResourceUploadKey uploadKey = ResourceUploadKey.from(resourceHash, rumContext);
        if (!success) {
            if (queuedResources.get(uploadKey) == State.PENDING) {
                queuedResources.remove(uploadKey);
            }
            return;
        }
        queuedResources.put(uploadKey, State.COMPLETED);
        if (uploadKey.isRouted()) {
            queuedResources.put(ResourceUploadKey.main(resourceHash), State.COMPLETED);
        }
    }

    private enum State {
        PENDING,
        COMPLETED
    }
}
