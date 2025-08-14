package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;
import com.ft.sdk.sessionreplay.model.MobileRecord;

import java.util.ArrayList;
import java.util.List;

public class TouchEventRecordedDataQueueItem extends RecordedDataQueueItem {

    private List<MobileRecord> touchData;

    public TouchEventRecordedDataQueueItem(RecordedQueuedItemContext recordedQueuedItemContext) {
        super(recordedQueuedItemContext);
        this.touchData = new ArrayList<>(); // Initialize with empty list
    }

    public TouchEventRecordedDataQueueItem(RecordedQueuedItemContext recordedQueuedItemContext, List<MobileRecord> touchData) {
        super(recordedQueuedItemContext);
        this.touchData = touchData != null ? touchData : new ArrayList<>();
    }

    @Override
    public boolean isValid() {
        return touchData != null && !touchData.isEmpty();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    public List<MobileRecord> getTouchData() {
        return touchData;
    }

    public void setTouchData(List<MobileRecord> touchData) {
        this.touchData = touchData != null ? touchData : new ArrayList<>();
    }
}
