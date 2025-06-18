package com.ft.sdk.sessionreplay.internal.processor;

import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.async.ResourceRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.SnapshotRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.TouchEventRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.model.Data;
import com.ft.sdk.sessionreplay.model.Data1;
import com.ft.sdk.sessionreplay.model.Data2;
import com.ft.sdk.sessionreplay.model.FocusRecord;
import com.ft.sdk.sessionreplay.model.MetaRecord;
import com.ft.sdk.sessionreplay.model.MobileMutationData;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.ViewEndRecord;
import com.ft.sdk.sessionreplay.model.ViewportResizeData;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecordedDataProcessor implements Processor {
    static String TAG = "RecordedDataProcessor";

    private final ResourceDataStoreManager resourceDataStoreManager;
    private final ResourcesWriter resourcesWriter;
    private final RecordWriter writer;
    private final MutationResolver mutationResolver;
    private final NodeFlattener nodeFlattener;

    private List<Wireframe> prevSnapshot = new LinkedList<>();
    private long lastSnapshotTimestamp = 0L;
    private int previousOrientation = Configuration.ORIENTATION_UNDEFINED;
    private SessionReplayRumContext prevRumContext = new SessionReplayRumContext();

    public RecordedDataProcessor(ResourceDataStoreManager resourceDataStoreManager, ResourcesWriter resourcesWriter, RecordWriter writer,
                                 MutationResolver mutationResolver) {
        this(resourceDataStoreManager, resourcesWriter, writer, mutationResolver, new NodeFlattener());
    }

    public RecordedDataProcessor(ResourceDataStoreManager resourceDataStoreManager, ResourcesWriter resourcesWriter, RecordWriter writer,
                                 MutationResolver mutationResolver, NodeFlattener nodeFlattener) {
        this.resourceDataStoreManager = resourceDataStoreManager;
        this.resourcesWriter = resourcesWriter;
        this.writer = writer;
        this.mutationResolver = mutationResolver;
        this.nodeFlattener = nodeFlattener;
    }

    @Override
    @WorkerThread
    public void processResources(ResourceRecordedDataQueueItem item) {
        String resourceHash = item.getIdentifier();
        boolean isKnownResource = resourceDataStoreManager.isPreviouslySentResource(resourceHash);
        if (!isKnownResource) {
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
        //printNodesTree(item.getNodes());
        
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
        /*if(false){
            // 打印newRumContext参数
            Log.d(TAG, "========== handleSnapshots Parameters ==========");
            Log.d(TAG, "newRumContext:");
            Log.d(TAG, "  └─ applicationId: " + newRumContext.getApplicationId());
            Log.d(TAG, "  └─ sessionId: " + newRumContext.getSessionId());
            Log.d(TAG, "  └─ viewId: " + newRumContext.getViewId());
            Log.d(TAG, "  └─ isValid: " + newRumContext.isValid());
            Log.d(TAG, "  └─ toString: " + newRumContext.toString());

            // 打印systemInformation参数
            Log.d(TAG, "systemInformation:");
            Log.d(TAG, "  └─ screenBounds: " + systemInformation.getScreenBounds().getHeight()+"x"+systemInformation.getScreenBounds().getWidth());
            Log.d(TAG, "  └─ screenOrientation: " + systemInformation.getScreenOrientation());
            Log.d(TAG, "  └─ screenDensity: " + systemInformation.getScreenDensity());
            Log.d(TAG, "  └─ themeColor: " + systemInformation.getThemeColor());

            // 打印其他参数
            Log.d(TAG, "timestamp: " + timestamp);
            Log.d(TAG, "snapshots size: " + snapshots.size());
            Log.d(TAG, "========== End Parameters ==========");
        }*/

        List<Wireframe> wireframes = new LinkedList<>();
        for (Node node : snapshots) {
            wireframes.addAll(nodeFlattener.flattenNode(node));
        }

        if (wireframes.isEmpty()) {
            return;
        }

        List<MobileRecord> records = new LinkedList<>();
        boolean isNewView = isNewView(newRumContext);
        boolean isTimeForFullSnapshot = isTimeForFullSnapshot();
        boolean screenOrientationChanged = systemInformation.getScreenOrientation() != previousOrientation;
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
            writer.write(bundleRecordInEnrichedRecord(newRumContext, records));
        }
    }
    @WorkerThread
    public void handleExternalFullSnapshot(MobileRecord.MobileFullSnapshotRecord mobileRecord, SessionReplayRumContext rumContext) { //added by zzq
        EnrichedRecord enrichedRecord = bundleRecordInEnrichedRecord(rumContext, List.of(mobileRecord));
        writer.write(enrichedRecord);
    }

    @WorkerThread
    public void handleExternalIncrementalUpdate(MobileRecord.MobileIncrementalSnapshotRecord mobileRecord, SessionReplayRumContext rumContext) { //added by zzq
        EnrichedRecord enrichedRecord = bundleRecordInEnrichedRecord(rumContext, List.of(mobileRecord));
        writer.write(enrichedRecord);
    }

    private boolean isTimeForFullSnapshot() {
        if (System.nanoTime() - lastSnapshotTimestamp >= FULL_SNAPSHOT_INTERVAL_IN_NS) {
            lastSnapshotTimestamp = System.nanoTime();
            return true;
        }
        return false;
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
                rumContext.getViewId(),
                records
        );
    }

    private boolean isNewView(SessionReplayRumContext newContext) {
        return !newContext.equals(prevRumContext);
    }

    // Constants and additional methods omitted for brevity

    private static final long FULL_SNAPSHOT_INTERVAL_IN_NS = TimeUnit.MILLISECONDS.toNanos(3000);

    // 添加打印nodes树的方法
    private void printNodesTree(List<Node> nodes) {
        Log.d(TAG,"========== Nodes Tree Structure ==========");
        for (int i = 0; i < nodes.size(); i++) {
            Log.d(TAG, "Root Node[" + i + "]:");
            printNodeRecursive(nodes.get(i), 0);
        }
        Log.d(TAG,"========== End Nodes Tree ==========");
    }

    private void printNodeRecursive(Node node, int depth) {
        String indent = "  ".repeat(depth);
        String treePrefix = depth == 0 ? "" : (depth == 1 ? "├── " : "│   ".repeat(depth - 1) + "├── ");
        
        // 打印当前节点开始标记
        Log.d(TAG,indent + treePrefix + "【Level " + depth + " Node】{");
        
        // 打印当前节点的wireframes信息
        Log.d(TAG,indent + "│   Wireframes(" + node.getWireframes().size() + "):");
        for (int i = 0; i < node.getWireframes().size(); i++) {
            Wireframe wireframe = node.getWireframes().get(i);
            String wireframeInfo = indent + "│     └─ [" + i + "] id=" + wireframe.getId() + 
                             ", type=" + wireframe.getClass().getSimpleName();
            
            // 如果是TextWireframe，添加文本内容
            if (wireframe instanceof TextWireframe) {
                TextWireframe textWireframe = (TextWireframe) wireframe;
                String text = textWireframe.getText();
                if (text != null && !text.isEmpty()) {
                    wireframeInfo += ", text=\"" + text + "\"";
                } else {
                    wireframeInfo += ", text=<empty>";
                }
            }
            
            Log.d(TAG, wireframeInfo);
        }
        
        // 打印parents信息
        Log.d(TAG,indent + "│   Parents(" + node.getParents().size() + "):");
        for (int i = 0; i < node.getParents().size(); i++) {
            Wireframe parent = node.getParents().get(i);
            Log.d(TAG,indent + "│     └─ [" + i + "] id=" + parent.getId() +
                             ", type=" + parent.getClass().getSimpleName());
        }
        
        // 打印children数量
        Log.d(TAG,indent + "│   Children(" + node.getChildren().size() + "):");
        
        // 递归打印子节点
        for (int i = 0; i < node.getChildren().size(); i++) {
            boolean isLastChild = (i == node.getChildren().size() - 1);
            String childPrefix = isLastChild ? "└── " : "├── ";
            Log.d(TAG,indent + "│   " + childPrefix + "Child[" + i + "]:");
            printNodeRecursive(node.getChildren().get(i), depth + 1);
        }
        
        // 打印当前节点结束标记
        Log.d(TAG,indent + "└─ 【End Level " + depth + " Node】");
        if (depth == 0) {
            Log.d(TAG,""); // 根节点后添加空行
        }
    }
}
