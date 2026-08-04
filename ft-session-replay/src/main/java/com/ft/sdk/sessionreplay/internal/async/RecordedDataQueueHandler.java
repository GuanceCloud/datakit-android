package com.ft.sdk.sessionreplay.internal.async;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.processor.RecordedDataProcessor;
import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;
import com.ft.sdk.sessionreplay.internal.processor.RumContextDataHandler;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class RecordedDataQueueHandler implements DataQueueHandler {

    private static final String TAG = "RecordedDataQueueHandle";
    private final RecordedDataProcessor processor;
    private final RumContextDataHandler rumContextDataHandler;
    private final InternalLogger internalLogger;
    private final ExecutorService executorService;
    private final Queue<RecordedDataQueueItem> recordedDataQueue;
    private final ResourceItemQueueDeduplicator resourceItemQueueDeduplicator;
    private boolean isStopped;

    public RecordedDataQueueHandler(RecordedDataProcessor processor,
                                    RumContextDataHandler rumContextDataHandler,
                                    InternalLogger internalLogger,
                                    ExecutorService executorService,
                                    Queue<RecordedDataQueueItem> recordedDataQueue) {
        this(processor, rumContextDataHandler, internalLogger, executorService, recordedDataQueue,
                new ResourceItemQueueDeduplicator());
    }

    RecordedDataQueueHandler(RecordedDataProcessor processor,
                             RumContextDataHandler rumContextDataHandler,
                             InternalLogger internalLogger,
                             ExecutorService executorService,
                             Queue<RecordedDataQueueItem> recordedDataQueue,
                             ResourceItemQueueDeduplicator resourceItemQueueDeduplicator) {
        this.processor = processor;
        this.rumContextDataHandler = rumContextDataHandler;
        this.internalLogger = internalLogger;
        this.executorService = executorService;
        this.recordedDataQueue = recordedDataQueue;
        this.resourceItemQueueDeduplicator = resourceItemQueueDeduplicator;
    }

    @Override
    @MainThread
    public synchronized void clearAndStopProcessingQueue() {
        isStopped = true;
        for (RecordedDataQueueItem item : recordedDataQueue) {
            releaseResourceItem(item);
        }
        recordedDataQueue.clear();
        executorService.shutdown();
    }

    @Override
    @MainThread
    public ResourceRecordedDataQueueItem addResourceItem(String identifier, byte[] resourceData) {
        RecordedQueuedItemContext rumContextData = rumContextDataHandler.createRumContextData();
        return addResourceItemWithContext(identifier, resourceData, rumContextData);
    }

    @Override
    @MainThread
    public ResourceRecordedDataQueueItem addResourceItem(String identifier,
                                                         byte[] resourceData,
                                                         RecordedQueuedItemContext rumContextData) {
        if (rumContextData == null) {
            return addResourceItem(identifier, resourceData);
        }
        return addResourceItemWithContext(identifier, resourceData, rumContextData);
    }

    private synchronized ResourceRecordedDataQueueItem addResourceItemWithContext(
            String identifier,
            byte[] resourceData,
            RecordedQueuedItemContext rumContextData) {
        if (isStopped || rumContextData == null) {
            return null;
        }

        if (resourceData == null || resourceData.length == 0
                || !resourceItemQueueDeduplicator.shouldQueue(
                identifier,
                rumContextData.getNewRumContext())) {
            return null;
        }

        ResourceRecordedDataQueueItem item = new ResourceRecordedDataQueueItem(
                rumContextData,
                identifier,
                resourceData,
                new ResourceRecordedDataQueueItem.WriteCompletionCallback() {
                    @Override
                    public void onComplete(boolean success) {
                        resourceItemQueueDeduplicator.onWriteFinished(
                                identifier,
                                rumContextData.getNewRumContext(),
                                success
                        );
                    }
                }
        );

        if (!insertIntoRecordedDataQueue(item)) {
            item.onWriteFinished(false);
            return null;
        }

        return item;
    }

    @Override
    @MainThread
    public TouchEventRecordedDataQueueItem addTouchEventItem(List<MobileRecord> pointerInteractions) {
        RecordedQueuedItemContext rumContextData = rumContextDataHandler.createRumContextData();
        if (rumContextData == null) {
            return null;
        }

        TouchEventRecordedDataQueueItem item = new TouchEventRecordedDataQueueItem(
                rumContextData,
                pointerInteractions
        );

        insertIntoRecordedDataQueue(item);

        return item;
    }

    @Override
    @MainThread
    public SnapshotRecordedDataQueueItem addSnapshotItem(SystemInformation systemInformation) {
        RecordedQueuedItemContext rumContextData = rumContextDataHandler.createRumContextData();
        if (rumContextData == null) {
            return null;
        }

        SnapshotRecordedDataQueueItem item = new SnapshotRecordedDataQueueItem(
                rumContextData,
                systemInformation
        );

        insertIntoRecordedDataQueue(item);

        return item;
    }

    @Override
    public void tryToConsumeItems() {
        if (recordedDataQueue.isEmpty()) {
            return;
        }
        ExecutorUtils.executeSafe(executorService, "Recorded Data queue processing", internalLogger, this::triggerProcessingLoop);
    }

    @WorkerThread
    private synchronized void triggerProcessingLoop() {
        while (!recordedDataQueue.isEmpty()) {
            RecordedDataQueueItem nextItem = recordedDataQueue.peek();

            if (nextItem != null) {
                long nextItemAgeInNs = System.nanoTime() - nextItem.getCreationTimeStampInNs();
                if (!nextItem.isValid()) {
                    internalLogger.e(TAG, String.format(ITEM_DROPPED_INVALID_MESSAGE, nextItem.getClass().getSimpleName()));
                    releaseResourceItem(recordedDataQueue.poll());
                } else if (nextItemAgeInNs > MAX_DELAY_NS) {
//                    internalLogger.e(TAG, "getCreationTimeStampInNs drop:"+nextItem.getCreationTimeStampInNs());
                    internalLogger.e(TAG, String.format(Locale.US, ITEM_DROPPED_EXPIRED_MESSAGE, nextItemAgeInNs));
                    releaseResourceItem(recordedDataQueue.poll());
                } else if (nextItem.isReady()) {
//                    internalLogger.i(TAG, "getCreationTimeStampInNs finish:"+nextItem.getCreationTimeStampInNs()+","+nextItemAgeInNs);
                    processItem(recordedDataQueue.poll());
                } else {
                    break;
                }
            }
        }
    }

    @WorkerThread
    private void processItem(RecordedDataQueueItem nextItem) {
        try {
            if (nextItem instanceof SnapshotRecordedDataQueueItem) {
                processor.processScreenSnapshots((SnapshotRecordedDataQueueItem) nextItem);
            } else if (nextItem instanceof TouchEventRecordedDataQueueItem) {
                processor.processTouchEventsRecords((TouchEventRecordedDataQueueItem) nextItem);
            } else if (nextItem instanceof ResourceRecordedDataQueueItem) {
                processor.processResources((ResourceRecordedDataQueueItem) nextItem);
            }
        } catch (RuntimeException exception) {
            releaseResourceItem(nextItem);
            throw exception;
        }
    }

    private boolean insertIntoRecordedDataQueue(RecordedDataQueueItem recordedDataQueueItem) {
        try {
            return recordedDataQueue.offer(recordedDataQueueItem);
        } catch (Exception e) {
            logAddToQueueException(e);
            return false;
        }
    }

    private void releaseResourceItem(RecordedDataQueueItem item) {
        if (item instanceof ResourceRecordedDataQueueItem) {
            ((ResourceRecordedDataQueueItem) item).onWriteFinished(false);
        }
    }

    public void forceFullSnapshotForLinkView() {
        processor.forceNewNextViewForLinkView();
    }

    private void logAddToQueueException(Exception e) {
        internalLogger.e(TAG, FAILED_TO_ADD_RECORDS_TO_QUEUE_ERROR_MESSAGE + "," + Log.getStackTraceString(e));
    }

    @VisibleForTesting
    static final long MAX_DELAY_NS = 1_000_000_000L; // 1 second in ns

    static final String FAILED_TO_ADD_RECORDS_TO_QUEUE_ERROR_MESSAGE =
            "SR RecordedDataQueueHandler: failed to add records into the queue";

    @VisibleForTesting
    static final String ITEM_DROPPED_INVALID_MESSAGE =
            "SR RecordedDataQueueHandler: dropped item from the queue. isValid=false, type=%s";

    @VisibleForTesting
    static final String ITEM_DROPPED_EXPIRED_MESSAGE =
            "SR RecordedDataQueueHandler: dropped item from the queue. age=%d ns";
}
