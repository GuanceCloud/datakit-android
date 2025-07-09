package com.ft.sdk.sessionreplay.internal.processor;

import android.content.res.Configuration;
import android.util.Log;

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
import com.ft.sdk.sessionreplay.model.ImageWireframe;
import com.ft.sdk.sessionreplay.model.MetaRecord;
import com.ft.sdk.sessionreplay.model.MobileMutationData;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.TextStyle;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.ViewEndRecord;
import com.ft.sdk.sessionreplay.model.ViewportResizeData;
import com.ft.sdk.sessionreplay.model.WebviewWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
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
        printNodesTree(item.getNodes());
        
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

    // 辅助方法：重复字符串
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // 添加打印nodes树的方法
    private void printNodesTree(List<Node> nodes) {
        Log.d(TAG, "=== zzq debug: new printNodesTree ===");
        Log.d(TAG,"==================== Node Tree Structure ====================");
        for (int i = 0; i < nodes.size(); i++) {
            printNodeRecursive(nodes.get(i), 0);
        }
        Log.d(TAG,"============================================================");
    }

    private void printNodeRecursive(Node node, int depth) {
        String indent = repeatString("  ", depth);
        String treePrefix = depth == 0 ? "" : (depth == 1 ? "├── " : repeatString("│   ", depth - 1) + "├── ");
        
        // 打印当前节点
        for (int i = 0; i < node.getWireframes().size(); i++) {
            Wireframe wireframe = node.getWireframes().get(i);
            String nodeInfo = indent + treePrefix + "🔹 Node[depth:" + depth + "][" + node.getWireframes().size() + " wireframes] ";
            
            // 获取wireframe的标签信息
            String label = getWireframeLabel(wireframe);
            String bounds = getWireframeBounds(wireframe);
            nodeInfo += label + "(" + bounds + ")[ID:" + wireframe.getId() + "]";
            
            // 添加children和parents信息
            if (!node.getChildren().isEmpty()) {
                nodeInfo += " [" + node.getChildren().size() + " children]";
            }
            if (!node.getParents().isEmpty()) {
                nodeInfo += " [" + node.getParents().size() + " parents]";
            }
            
            Log.d(TAG, nodeInfo);
            
            // 打印wireframe详细信息
            String wireframeDetail = indent + "       📄 [ID:" + wireframe.getId() + "] " + 
                                   getWireframeDetail(wireframe);
            Log.d(TAG, wireframeDetail);
            
            // 打印parents信息
            if (!node.getParents().isEmpty()) {
                Log.d(TAG, indent + "       👆 Parents[" + node.getParents().size() + "]:");
                for (int j = 0; j < node.getParents().size(); j++) {
                    Wireframe parent = node.getParents().get(j);
                    String parentInfo = indent + "          " + (j + 1) + ". 📄 [ID:" + parent.getId() + "] " + 
                                      getWireframeDetail(parent);
                    Log.d(TAG, parentInfo);
                }
            }
        }
        
        // 递归打印子节点
        for (int i = 0; i < node.getChildren().size(); i++) {
            boolean isLastChild = (i == node.getChildren().size() - 1);
            String childPrefix = isLastChild ? "└── " : "├── ";
            printNodeRecursive(node.getChildren().get(i), depth + 1);
        }
    }
    
    private String getWireframeLabel(Wireframe wireframe) {
        if (wireframe instanceof TextWireframe) {
            TextWireframe textWireframe = (TextWireframe) wireframe;
            String text = textWireframe.getText();
            String colorInfo = "";
            
            // 获取文本颜色信息
            TextStyle textStyle = textWireframe.getTextStyle();
            if (textStyle != null && textStyle.getColor() != null) {
                colorInfo = "[color:" + textStyle.getColor() + "]";
            }
            
            if (text != null && !text.isEmpty()) {
                return "Text('" + text + "')" + colorInfo;
            } else {
                return "Text('')" + colorInfo;
            }
        } else if (wireframe instanceof PlaceholderWireframe) {
            PlaceholderWireframe placeholderWireframe = (PlaceholderWireframe) wireframe;
            String label = placeholderWireframe.getLabel();
            if (label != null && !label.isEmpty()) {
                return "Placeholder('" + label + "')";
            } else {
                return "Placeholder('')";
            }
        } else if (wireframe instanceof ShapeWireframe) {
            ShapeWireframe shapeWireframe = (ShapeWireframe) wireframe;
            String backgroundColorInfo = "";
            
            // 获取背景颜色信息
            ShapeStyle shapeStyle = shapeWireframe.getShapeStyle();
            if (shapeStyle != null && shapeStyle.getBackgroundColor() != null) {
                backgroundColorInfo = "[backgroundColor:" + shapeStyle.getBackgroundColor() + "]";
            }
            
            return "Shape" + backgroundColorInfo;
        } else if (wireframe instanceof ImageWireframe) {
            return "Image";
        } else if (wireframe instanceof WebviewWireframe) {
            return "Webview";
        } else {
            return wireframe.getClass().getSimpleName();
        }
    }
    
    private String getWireframeBounds(Wireframe wireframe) {
        long x, y, width, height;
        
        if (wireframe instanceof TextWireframe) {
            TextWireframe textWireframe = (TextWireframe) wireframe;
            x = textWireframe.getX();
            y = textWireframe.getY();
            width = textWireframe.getWidth();
            height = textWireframe.getHeight();
        } else if (wireframe instanceof PlaceholderWireframe) {
            PlaceholderWireframe placeholderWireframe = (PlaceholderWireframe) wireframe;
            x = placeholderWireframe.getX();
            y = placeholderWireframe.getY();
            width = placeholderWireframe.getWidth();
            height = placeholderWireframe.getHeight();
        } else if (wireframe instanceof ShapeWireframe) {
            ShapeWireframe shapeWireframe = (ShapeWireframe) wireframe;
            x = shapeWireframe.getX();
            y = shapeWireframe.getY();
            width = shapeWireframe.getWidth();
            height = shapeWireframe.getHeight();
        } else if (wireframe instanceof ImageWireframe) {
            ImageWireframe imageWireframe = (ImageWireframe) wireframe;
            x = imageWireframe.getX();
            y = imageWireframe.getY();
            width = imageWireframe.getWidth();
            height = imageWireframe.getHeight();
        } else if (wireframe instanceof WebviewWireframe) {
            WebviewWireframe webviewWireframe = (WebviewWireframe) wireframe;
            x = webviewWireframe.getX();
            y = webviewWireframe.getY();
            width = webviewWireframe.getWidth();
            height = webviewWireframe.getHeight();
        } else {
            // 对于未知类型的wireframe，返回默认值
            x = y = width = height = 0;
        }
        
        return x + "," + y + "," + width + "×" + height;
    }
    
    private String getWireframeDetail(Wireframe wireframe) {
        String type = wireframe.getClass().getSimpleName();
        String bounds = getWireframeBounds(wireframe);
        
        if (wireframe instanceof TextWireframe) {
            TextWireframe textWireframe = (TextWireframe) wireframe;
            String text = textWireframe.getText();
            String textInfo = text != null ? "'" + text + "'" : "'<empty>'";
            
            // 获取文本样式信息
            TextStyle textStyle = textWireframe.getTextStyle();
            String fontInfo = "";
            String colorInfo = "";
            
            if (textStyle != null) {
                String family = textStyle.getFamily();
                double size = textStyle.getSize();
                String color = textStyle.getColor();
                
                fontInfo = "font:" + (family != null ? family : "default") + " size:" + size;
                colorInfo = "color:" + (color != null ? color : "#000000");
            }
            
            return type + "(" + textInfo + " " + bounds + ") [" + fontInfo + " " + colorInfo + "]";
        } else if (wireframe instanceof ShapeWireframe) {
            ShapeWireframe shapeWireframe = (ShapeWireframe) wireframe;
            String backgroundColorInfo = "";
            
            // 获取背景颜色信息
            ShapeStyle shapeStyle = shapeWireframe.getShapeStyle();
            if (shapeStyle != null && shapeStyle.getBackgroundColor() != null) {
                backgroundColorInfo = " [backgroundColor:" + shapeStyle.getBackgroundColor() + "]";
            }
            
            return type + "(" + bounds + ")" + backgroundColorInfo;
        } else if (wireframe instanceof PlaceholderWireframe) {
            PlaceholderWireframe placeholderWireframe = (PlaceholderWireframe) wireframe;
            String label = placeholderWireframe.getLabel();
            String labelInfo = label != null ? "'" + label + "'" : "'<empty>'";
            return type + "(" + labelInfo + " " + bounds + ")";
        } else if (wireframe instanceof ImageWireframe) {
            return type + "(" + bounds + ")";
        } else if (wireframe instanceof WebviewWireframe) {
            return type + "(" + bounds + ")";
        } else {
            return type + "(" + bounds + ")";
        }
    }
}
