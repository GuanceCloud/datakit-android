package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourceItemQueueDeduplicatorTest {

    @Test
    public void shouldQueueResourceOnceForMainAndEachWgtId() {
        ResourceItemQueueDeduplicator deduplicator = new ResourceItemQueueDeduplicator();

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext(null)));
        assertFalse(deduplicator.shouldQueue("hash-1", rumContext(null)));
        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
        assertFalse(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-2")));
    }

    @Test
    public void routedResourceShouldAlsoCoverMainSpace() {
        ResourceItemQueueDeduplicator deduplicator = new ResourceItemQueueDeduplicator();

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
        deduplicator.onWriteFinished("hash-1", rumContext("widget-1"), true);
        assertFalse(deduplicator.shouldQueue("hash-1", rumContext(null)));
    }

    @Test
    public void failedMainWriteShouldNotRemoveCoverageFromSuccessfulRoute() {
        ResourceItemQueueDeduplicator deduplicator = new ResourceItemQueueDeduplicator();

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext(null)));
        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
        deduplicator.onWriteFinished("hash-1", rumContext("widget-1"), true);
        deduplicator.onWriteFinished("hash-1", rumContext(null), false);

        assertFalse(deduplicator.shouldQueue("hash-1", rumContext(null)));
    }

    @Test
    public void failedWriteShouldAllowResourceToBeQueuedAgain() {
        ResourceItemQueueDeduplicator deduplicator = new ResourceItemQueueDeduplicator();

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
        deduplicator.onWriteFinished("hash-1", rumContext("widget-1"), false);

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
    }

    @Test
    public void evictedResourceShouldBeAllowedToQueueAgain() {
        ResourceItemQueueDeduplicator deduplicator = new ResourceItemQueueDeduplicator(2);

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
        assertTrue(deduplicator.shouldQueue("hash-2", rumContext("widget-1")));
        assertTrue(deduplicator.shouldQueue("hash-3", rumContext("widget-1")));

        assertTrue(deduplicator.shouldQueue("hash-1", rumContext("widget-1")));
    }

    private SessionReplayRumContext rumContext(String wgtId) {
        Map<String, Object> globalContext;
        if (wgtId == null) {
            globalContext = Collections.emptyMap();
        } else {
            globalContext = new HashMap<>();
            globalContext.put("wgt_id", wgtId);
        }
        return new SessionReplayRumContext("app-id", "session-id", "view-id", globalContext);
    }
}
