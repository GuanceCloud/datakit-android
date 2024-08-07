package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueRefs;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;

import java.util.LinkedList;
import java.util.List;

public class SnapshotProducer {
    private final ImageWireframeHelper imageWireframeHelper;
    private final TreeViewTraversal treeViewTraversal;
    private final OptionSelectorDetector optionSelectorDetector;

    public SnapshotProducer(
            ImageWireframeHelper imageWireframeHelper,
            TreeViewTraversal treeViewTraversal,
            OptionSelectorDetector optionSelectorDetector
    ) {
        this.imageWireframeHelper = imageWireframeHelper;
        this.treeViewTraversal = treeViewTraversal;
        this.optionSelectorDetector = optionSelectorDetector;
    }

    @UiThread
    public Node produce(
            View rootView,
            SystemInformation systemInformation,
            SessionReplayPrivacy privacy,
            RecordedDataQueueRefs recordedDataQueueRefs
    ) {
        return convertViewToNode(
                rootView,
                new MappingContext(systemInformation, imageWireframeHelper, privacy,
                        false),
                new LinkedList<>(),
                recordedDataQueueRefs
        );
    }

    @UiThread
    private Node convertViewToNode(
            View view,
            MappingContext mappingContext,
            LinkedList<Wireframe> parents,
            RecordedDataQueueRefs recordedDataQueueRefs
    ) {
        TreeViewTraversal.TraversedTreeView traversedTreeView = treeViewTraversal.traverse(view, mappingContext, recordedDataQueueRefs);
        TraversalStrategy nextTraversalStrategy = traversedTreeView.getNextActionStrategy();
        List<Wireframe> resolvedWireframes = traversedTreeView.getMappedWireframes();

        if (nextTraversalStrategy == TraversalStrategy.STOP_AND_DROP_NODE) {
            return null;
        }
        if (nextTraversalStrategy == TraversalStrategy.STOP_AND_RETURN_NODE) {
            return new Node(resolvedWireframes, null, parents);
        }

        LinkedList<Node> childNodes = new LinkedList<>();
        if (view instanceof ViewGroup &&
                ((ViewGroup) view).getChildCount() > 0 &&
                nextTraversalStrategy == TraversalStrategy.TRAVERSE_ALL_CHILDREN
        ) {
            MappingContext childMappingContext = resolveChildMappingContext((ViewGroup) view, mappingContext);
            LinkedList<Wireframe> parentsCopy = new LinkedList<>(parents);
            parentsCopy.addAll(resolvedWireframes);
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View viewChild = ((ViewGroup) view).getChildAt(i);
                if (viewChild != null) {
                    Node childNode = convertViewToNode(viewChild, childMappingContext, parentsCopy, recordedDataQueueRefs);
                    if (childNode != null) {
                        childNodes.add(childNode);
                    }
                }
            }
        }
        return new Node(resolvedWireframes, childNodes, parents);
    }

    private MappingContext resolveChildMappingContext(
            ViewGroup parent,
            MappingContext parentMappingContext
    ) {
        if (optionSelectorDetector.isOptionSelector(parent)) {
            return parentMappingContext.copy(true);
        } else {
            return parentMappingContext;
        }
    }
}
