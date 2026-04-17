package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.view.View;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueRefs;
import com.ft.sdk.sessionreplay.internal.recorder.TreeViewTraversal;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.InteropViewCallback;
import com.ft.sdk.sessionreplay.recorder.MappingContext;

import java.util.List;

public class DefaultInteropViewCallback implements InteropViewCallback {

    private final TreeViewTraversal treeViewTraversal;
    private final RecordedDataQueueRefs recordedDataQueueRefs;

    public DefaultInteropViewCallback(TreeViewTraversal treeViewTraversal, RecordedDataQueueRefs recordedDataQueueRefs) {
        this.treeViewTraversal = treeViewTraversal;
        this.recordedDataQueueRefs = recordedDataQueueRefs;
    }

    @UiThread
    @Override
    public List<Wireframe> map(View view, MappingContext mappingContext) {
        return treeViewTraversal.traverse(view, mappingContext, recordedDataQueueRefs).getMappedWireframes();
    }
}