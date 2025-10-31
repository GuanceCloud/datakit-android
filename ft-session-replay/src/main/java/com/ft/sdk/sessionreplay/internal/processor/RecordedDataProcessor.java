package com.ft.sdk.sessionreplay.internal.processor;

import android.content.res.Configuration;

import androidx.annotation.WorkerThread;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.internal.async.ResourceRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.SnapshotRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.TouchEventRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.model.Data;
import com.ft.sdk.sessionreplay.model.Data1;
import com.ft.sdk.sessionreplay.model.Data2;
import com.ft.sdk.sessionreplay.model.FocusRecord;
import com.ft.sdk.sessionreplay.model.MetaRecord;
import com.ft.sdk.sessionreplay.model.MobileMutationData;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.model.ViewEndRecord;
import com.ft.sdk.sessionreplay.model.ViewportResizeData;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecordedDataProcessor implements Processor {

    private static final String TAG = "RecordedDataProcessor";
    private final ResourceDataStoreManager resourceDataStoreManager;
    private final ResourcesWriter resourcesWriter;
    private final RecordWriter writer;
    private final MutationResolver mutationResolver;
    private final NodeFlattener nodeFlattener;

    private List<Wireframe> prevSnapshot = new LinkedList<>();
    private long lastSnapshotTimestamp = 0L;
    private boolean forceNewNextViewForLinkView = false;
    private int previousOrientation = Configuration.ORIENTATION_UNDEFINED;
    private SessionReplayRumContext prevRumContext = new SessionReplayRumContext();
    private final FeatureSdkCore sdkCore;

    public RecordedDataProcessor(FeatureSdkCore sdkCore, ResourceDataStoreManager resourceDataStoreManager, ResourcesWriter resourcesWriter, RecordWriter writer,
                                 MutationResolver mutationResolver) {
        this(sdkCore, resourceDataStoreManager, resourcesWriter, writer, mutationResolver, new NodeFlattener());
    }

    public RecordedDataProcessor(FeatureSdkCore sdkCore, ResourceDataStoreManager resourceDataStoreManager, ResourcesWriter resourcesWriter, RecordWriter writer,
                                 MutationResolver mutationResolver, NodeFlattener nodeFlattener) {
        this.resourceDataStoreManager = resourceDataStoreManager;
        this.resourcesWriter = resourcesWriter;
        this.writer = writer;
        this.mutationResolver = mutationResolver;
        this.nodeFlattener = nodeFlattener;
        this.sdkCore = sdkCore;
    }

    @Override
    @WorkerThread
    public void processResources(ResourceRecordedDataQueueItem item) {
        String resourceHash = item.getIdentifier();
        boolean isKnownResource = resourceDataStoreManager.isPreviouslySentResource(resourceHash);
        if (!isKnownResource) {
            if (resourceDataStoreManager.isReady()) {
                resourceDataStoreManager.cacheResourceHash(resourceHash);
            }

            EnrichedResource enrichedResource = new EnrichedResource(
                    item.getResourceData(),
                    item.getApplicationId(),
                    resourceHash
            );
            resourcesWriter.write(enrichedResource);
        }
    }

    @Override
    @WorkerThread
    public void processScreenSnapshots(SnapshotRecordedDataQueueItem item) {
        handleSnapshots(
                item.getRecordedQueuedItemContext().getNewRumContext(),
                item.getRecordedQueuedItemContext().getTimestamp(),
                item.getNodes(),
                item.getSystemInformation()
        );
        prevRumContext = item.getRecordedQueuedItemContext().getNewRumContext();
    }

    @Override
    @WorkerThread
    public void processTouchEventsRecords(TouchEventRecordedDataQueueItem item) {
        handleTouchRecords(
                item.getRecordedQueuedItemContext().getNewRumContext(),
                item.getTouchData()
        );
    }

    // Internal methods

    @WorkerThread
    private void handleTouchRecords(SessionReplayRumContext rumContext,
                                    List<MobileRecord> touchData) {
        EnrichedRecord enrichedRecord = bundleRecordInEnrichedRecord(rumContext, touchData);
        writer.write(enrichedRecord);
    }

    @WorkerThread
    private void handleSnapshots(SessionReplayRumContext newRumContext, long timestamp,
                                 List<Node> snapshots, SystemInformation systemInformation) {
        List<Wireframe> wireframes = new LinkedList<>();
        for (Node node : snapshots) {
            wireframes.addAll(nodeFlattener.flattenNode(node));
        }

        if (wireframes.isEmpty()) {
            return;
        }

        List<MobileRecord> records = new LinkedList<>();
        boolean isNewViewForContextLink = forceNewNextViewForLinkView && !newRumContext.getGlobalContext().isEmpty();
        boolean isNewView = isNewView(newRumContext) || isNewViewForContextLink;
        boolean isTimeForFullSnapshot = isTimeForFullSnapshot();
        boolean screenOrientationChanged = systemInformation.getScreenOrientation() != previousOrientation;
        boolean isSessionReplayErrorSampled = sdkCore.getConsentProvider() == TrackingConsent.SAMPLED_ON_ERROR_SESSION;
        boolean fullSnapshotRequired = isNewView || isTimeForFullSnapshot || screenOrientationChanged;


        if (isNewView) {
            handleViewEndRecord(timestamp);
            MetaRecord metaRecord = new MetaRecord(
                    timestamp,
                    null,
                    new Data1(systemInformation.getScreenBounds().getWidth(),
                            systemInformation.getScreenBounds().getHeight(), null)
            );
            FocusRecord focusRecord = new FocusRecord(
                    timestamp,
                    null,
                    new Data2(true)
            );
            records.add(metaRecord);
            records.add(focusRecord);
            if (isNewViewForContextLink) {
                sdkCore.getInternalLogger().d(TAG, "forceNewNextView:" + newRumContext.getViewId());
                forceNewNextViewForLinkView = false;
            }
        }

        if (screenOrientationChanged) {
            ViewportResizeData viewportResizeData =
                    new ViewportResizeData(
                            systemInformation.getScreenBounds().getWidth(),
                            systemInformation.getScreenBounds().getHeight()
                    );
            MobileRecord.MobileIncrementalSnapshotRecord viewportRecord =
                    new MobileRecord.MobileIncrementalSnapshotRecord(
                            timestamp,
                            viewportResizeData
                    );
            records.add(viewportRecord);
        }

        if (fullSnapshotRequired) {
            if (isSessionReplayErrorSampled) {
                MetaRecord metaRecord = new MetaRecord(
                        timestamp,
                        null,
                        new Data1(systemInformation.getScreenBounds().getWidth(),
                                systemInformation.getScreenBounds().getHeight(), null)
                );
                records.add(metaRecord);
            }
            records.add(
                    new MobileRecord.MobileFullSnapshotRecord(
                            timestamp,
                            new Data(wireframes)
                    )
            );
        } else {
            MobileMutationData incrementalData = mutationResolver.resolveMutations(prevSnapshot, wireframes);
            if (incrementalData != null) {
                records.add(
                        new MobileRecord.MobileIncrementalSnapshotRecord(
                                timestamp,
                                incrementalData
                        )
                );
            }
        }

        prevSnapshot = wireframes;
        previousOrientation = systemInformation.getScreenOrientation();

        if (!records.isEmpty()) {
            EnrichedRecord record = bundleRecordInEnrichedRecord(newRumContext, records);
            writer.write(record);
//            sdkCore.getInternalLogger().e(TAG, "records"+record.toJson());
        }
    }

    private boolean isTimeForFullSnapshot() {
        if (System.nanoTime() - lastSnapshotTimestamp >= FULL_SNAPSHOT_INTERVAL_IN_NS) {
            lastSnapshotTimestamp = System.nanoTime();
            return true;
        }
        return false;
    }

    public void forceNewNextViewForLinkView() {
        forceNewNextViewForLinkView = true;
    }

    private void handleViewEndRecord(long timestamp) {
        if (prevRumContext.isValid()) {
            ViewEndRecord viewEndRecord =
                    new ViewEndRecord(timestamp, null);
            writer.write(bundleRecordInEnrichedRecord(prevRumContext, List.of(viewEndRecord)));
        }
    }

    private EnrichedRecord bundleRecordInEnrichedRecord(
            SessionReplayRumContext rumContext, List<MobileRecord> records) {
        return new EnrichedRecord(
                rumContext.getApplicationId(),
                rumContext.getSessionId(),
                rumContext.getViewId(), false,
                records, rumContext.getGlobalContext()
        );
    }

    private boolean isNewView(SessionReplayRumContext newContext) {
        return !newContext.equals(prevRumContext);
    }

    // Constants and additional methods omitted for brevity

    private static final long FULL_SNAPSHOT_INTERVAL_IN_NS = TimeUnit.MILLISECONDS.toNanos(3000);
}
