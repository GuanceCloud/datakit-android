package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SnapshotRecordedDataQueueItem extends RecordedDataQueueItem {

    private volatile List<Node> nodes = List.of(); // or Collections.emptyList() for pre-Java 9
    private volatile boolean isFinishedTraversal = false;
    private final AtomicInteger pendingJobs = new AtomicInteger(0);

    public SnapshotRecordedDataQueueItem(RecordedQueuedItemContext recordedQueuedItemContext, SystemInformation systemInformation) {
        super(recordedQueuedItemContext);
        this.systemInformation = systemInformation;
    }

    @Override
    public boolean isValid() {
        if (!isFinishedTraversal) {
            // item is always valid unless traversal has finished
            return true;
        }

        return !nodes.isEmpty();
    }

    @Override
    public boolean isReady() {
        return isFinishedTraversal && pendingJobs.get() == 0;
    }

    public void incrementPendingJobs() {
        //System.out.println("[FT-SDK] " + getCreationTimeStampInNs() + ",incrementPendingJobs:" + pendingJobs.get());
        pendingJobs.incrementAndGet();
    }

    public void decrementPendingJobs() {
        //System.out.println("[FT-SDK] " + getCreationTimeStampInNs() + ",decrementPendingJobs:" + pendingJobs.get());
        pendingJobs.decrementAndGet();
    }

    // Fields
    private final SystemInformation systemInformation;

    public SystemInformation getSystemInformation() {
        return systemInformation;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setFinishedTraversal(boolean finishedTraversal) {
        isFinishedTraversal = finishedTraversal;
    }
}
