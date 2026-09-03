package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedDataProcessor;
import com.ft.sdk.sessionreplay.internal.processor.RumContextDataHandler;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecordedDataQueueHandlerBoundaryTest {

    private final List<ExecutorService> executors = new ArrayList<>();

    @After
    public void tearDown() {
        for (ExecutorService executor : executors) {
            executor.shutdownNow();
        }
    }

    @Test
    public void pauseBoundaryKeepsAcceptedSnapshotBeforeViewEndAndRejectsLaterSnapshots() {
        ConcurrentLinkedQueue<RecordedDataQueueItem> queue = new ConcurrentLinkedQueue<>();
        RecordedDataQueueHandler handler = newHandler(queue);

        SnapshotRecordedDataQueueItem accepted = handler.addSnapshotItem(null);
        assertNotNull(accepted);

        handler.pauseRecordingAndCloseCurrentView(1234L);

        assertNull(handler.addSnapshotItem(null));
        assertNotNull(handler.addResourceItem(
                "accepted-snapshot-resource",
                new byte[]{1},
                accepted.getRecordedQueuedItemContext()
        ));
        assertEquals(3, queue.size());
        assertSame(accepted, queue.poll());
        RecordedDataQueueItem boundary = queue.poll();
        assertTrue(boundary instanceof ViewEndRecordedDataQueueItem);
        assertEquals(1234L, ((ViewEndRecordedDataQueueItem) boundary).getTimestamp());
        assertTrue(queue.poll() instanceof ResourceRecordedDataQueueItem);
    }

    @Test
    public void repeatedPauseAddsOneBoundaryAndResumeAcceptsAForcedNativeSnapshot() {
        ConcurrentLinkedQueue<RecordedDataQueueItem> queue = new ConcurrentLinkedQueue<>();
        RecordedDataQueueHandler handler = newHandler(queue);

        assertNotNull(handler.addSnapshotItem(null));
        handler.pauseRecordingAndCloseCurrentView(1234L);
        handler.pauseRecordingAndCloseCurrentView(2345L);

        assertEquals(2, queue.size());
        assertNull(handler.addSnapshotItem(null));

        handler.forceFullSnapshot();
        handler.resumeRecording();

        assertNotNull(handler.addSnapshotItem(null));
        assertEquals(3, queue.size());
    }

    private RecordedDataQueueHandler newHandler(ConcurrentLinkedQueue<RecordedDataQueueItem> queue) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executors.add(executor);
        RecordedDataProcessor processor = new RecordedDataProcessor(
                null,
                null,
                null,
                record -> { },
                null,
                false,
                null
        );
        RumContextDataHandler contextHandler = new RumContextDataHandler(
                () -> new SessionReplayRumContext("app-id", "session-id", "native-view-id"),
                () -> 42L,
                null
        );
        return new RecordedDataQueueHandler(
                processor,
                contextHandler,
                new NoOpInternalLogger(),
                executor,
                queue
        );
    }
}
