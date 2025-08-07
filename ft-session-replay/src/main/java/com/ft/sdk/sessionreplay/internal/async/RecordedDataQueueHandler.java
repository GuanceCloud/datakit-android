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

    public RecordedDataQueueHandler(RecordedDataProcessor processor,
                                    RumContextDataHandler rumContextDataHandler,
                                    InternalLogger internalLogger,
                                    ExecutorService executorService,
                                    Queue<RecordedDataQueueItem> recordedDataQueue) {
        this.processor = processor;
        this.rumContextDataHandler = rumContextDataHandler;
        this.internalLogger = internalLogger;
        this.executorService = executorService;
        this.recordedDataQueue = recordedDataQueue;
    }

    @Override
    @MainThread
    public synchronized void clearAndStopProcessingQueue() {
        recordedDataQueue.clear();
        executorService.shutdown();
    }

    @Override
    @MainThread
    public ResourceRecordedDataQueueItem addResourceItem(String identifier, String applicationId, byte[] resourceData) {
        RecordedQueuedItemContext rumContextData = rumContextDataHandler.createRumContextData();
        if (rumContextData == null) {
            return null;
        }

        ResourceRecordedDataQueueItem item = new ResourceRecordedDataQueueItem(
                rumContextData,
                identifier,
                applicationId,
                resourceData
        );

        insertIntoRecordedDataQueue(item);

        return item;
    }

    @Override
    @MainThread
    public TouchEventRecordedDataQueueItem addTouchEventItem(List<MobileRecord> pointerInteractions) {
        Log.d(TAG, "enter addTouchEventItem touchData");
        RecordedQueuedItemContext rumContextData = rumContextDataHandler.createRumContextData();
        if (rumContextData == null) {
            return null;
        }

        TouchEventRecordedDataQueueItem item = new TouchEventRecordedDataQueueItem(
                rumContextData,
                pointerInteractions
        );

        insertIntoRecordedDataQueue(item);

        Log.d(TAG, "exit addTouchEventItem touchData");
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
    @MainThread
    public void tryToConsumeItems() {
        Log.d(TAG, "enter tryToConsumeItems touchData");
        if (recordedDataQueue.isEmpty()) {
            Log.d(TAG, "tryToConsumeItems is empty touchData");
            return;
        }
        ExecutorUtils.executeSafe(executorService, "Recorded Data queue processing", internalLogger, this::triggerProcessingLoop);
    }

    @WorkerThread
    private synchronized void triggerProcessingLoop() {
        Log.d(TAG, "enter triggerProcessingLoop touchData");
        while (!recordedDataQueue.isEmpty()) {
            RecordedDataQueueItem nextItem = recordedDataQueue.peek();

            if (nextItem != null) {
                long nextItemAgeInNs = System.nanoTime() - nextItem.getCreationTimeStampInNs();
                if (!nextItem.isValid()) {
                    internalLogger.e(TAG, String.format(ITEM_DROPPED_INVALID_MESSAGE, nextItem.getClass().getSimpleName()));
                    Log.e(TAG, String.format(ITEM_DROPPED_INVALID_MESSAGE, nextItem.getClass().getSimpleName()) + "touchData");
                    recordedDataQueue.poll();
                } else if (nextItemAgeInNs > MAX_DELAY_NS) {
                    internalLogger.e(TAG, String.format(Locale.US, ITEM_DROPPED_EXPIRED_MESSAGE, nextItemAgeInNs));
                    Log.e(TAG, String.format(Locale.US, ITEM_DROPPED_EXPIRED_MESSAGE, nextItemAgeInNs) + "touchData");
                    recordedDataQueue.poll();
                } else if (nextItem.isReady()) {
                    processItem(recordedDataQueue.poll());
                } else {
                    break;
                }
            }
        }
    }

    @WorkerThread
    private void processItem(RecordedDataQueueItem nextItem) {
        if (nextItem instanceof SnapshotRecordedDataQueueItem) {
            Log.d(TAG, "processTouchEventsRecords SnapshotRecordedDataQueueItem");
            processor.processScreenSnapshots((SnapshotRecordedDataQueueItem) nextItem);
        } else if (nextItem instanceof TouchEventRecordedDataQueueItem) {
            processor.processTouchEventsRecords((TouchEventRecordedDataQueueItem) nextItem);
            Log.d(TAG, "processTouchEventsRecords TouchEventRecordedDataQueueItem touchData");
        } else if (nextItem instanceof ResourceRecordedDataQueueItem) {
            Log.d(TAG, "processTouchEventsRecords ResourceRecordedDataQueueItem");
            processor.processResources((ResourceRecordedDataQueueItem) nextItem);
        }
    }

    private void insertIntoRecordedDataQueue(RecordedDataQueueItem recordedDataQueueItem) {

        Log.d(TAG, "enter insertIntoRecordedDataQueue touchData");
        try {
            recordedDataQueue.offer(recordedDataQueueItem);
        } catch (Exception e) {
            Log.d(TAG, "insertIntoRecordedDataQueue exception touchData");
            logAddToQueueException(e);
        }
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
            "SR RecordedDataQueueHandler: dropped item from the queue. age=%d ms";
}
