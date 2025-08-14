package com.ft.sdk.sessionreplay.internal.recorder;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.R;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueRefs;
import com.ft.sdk.sessionreplay.internal.recorder.callback.DefaultInteropViewCallback;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.LinkedList;
import java.util.List;

public class SnapshotProducer {

    private static final String TAG = "SnapshotProducer";
    private final ImageWireframeHelper imageWireframeHelper;
    private final TreeViewTraversal treeViewTraversal;
    private final OptionSelectorDetector optionSelectorDetector;
    private final TouchPrivacyManager touchPrivacyManager;
    private final InternalLogger internalLogger;

    public SnapshotProducer(
            ImageWireframeHelper imageWireframeHelper,
            TreeViewTraversal treeViewTraversal,
            OptionSelectorDetector optionSelectorDetector,
            TouchPrivacyManager touchPrivacyManager,
            InternalLogger internalLogger
    ) {
        this.imageWireframeHelper = imageWireframeHelper;
        this.treeViewTraversal = treeViewTraversal;
        this.optionSelectorDetector = optionSelectorDetector;
        this.touchPrivacyManager = touchPrivacyManager;
        this.internalLogger = internalLogger;
    }

    @UiThread
    public Node produce(
            View rootView,
            SystemInformation systemInformation,
            TextAndInputPrivacy textAndInputPrivacy,
            ImagePrivacy imagePrivacy,
            RecordedDataQueueRefs recordedDataQueueRefs
    ) {
        return convertViewToNode(
                rootView,
                new MappingContext(systemInformation, imageWireframeHelper,
                        false, textAndInputPrivacy, imagePrivacy,
                        touchPrivacyManager, new DefaultInteropViewCallback(
                        treeViewTraversal, recordedDataQueueRefs
                )),
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
        MappingContext localMappingContext = resolvePrivacyOverrides(view, mappingContext);
        TreeViewTraversal.TraversedTreeView traversedTreeView = treeViewTraversal.traverse(view, localMappingContext, recordedDataQueueRefs);
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
                nextTraversalStrategy == TraversalStrategy.TRAVERSE_ALL_CHILDREN) {

            MappingContext childMappingContext = resolveChildMappingContext((ViewGroup) view, localMappingContext);
            LinkedList<Wireframe> parentsCopy = new LinkedList<>(parents);
            parentsCopy.addAll(resolvedWireframes);

            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View viewChild = viewGroup.getChildAt(i);
                if (viewChild == null) continue;

                Node childNode = convertViewToNode(viewChild, childMappingContext, parentsCopy, recordedDataQueueRefs);
                if (childNode != null) {
                    childNodes.add(childNode);
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

    private MappingContext resolvePrivacyOverrides(View view, MappingContext mappingContext) {
        ImagePrivacy imagePrivacy;
        try {
            String privacy = (String) view.getTag(R.id.ft_image_privacy);
            if (privacy == null) {
                imagePrivacy = mappingContext.getImagePrivacy();
            } else {
                imagePrivacy = ImagePrivacy.valueOf(privacy);
            }
        } catch (IllegalArgumentException e) {
            logInvalidPrivacyLevelError(e);
            imagePrivacy = mappingContext.getImagePrivacy();
        }

        TextAndInputPrivacy textAndInputPrivacy;
        try {
            String privacy = (String) view.getTag(R.id.ft_text_and_input_privacy);
            if (privacy == null) {
                textAndInputPrivacy = mappingContext.getTextAndInputPrivacy();
            } else {
                textAndInputPrivacy = TextAndInputPrivacy.valueOf(privacy);
            }
        } catch (IllegalArgumentException e) {
            logInvalidPrivacyLevelError(e);
            textAndInputPrivacy = mappingContext.getTextAndInputPrivacy();
        }

        return mappingContext.copy(imagePrivacy, textAndInputPrivacy);
    }

    private void logInvalidPrivacyLevelError(Exception e) {
        internalLogger.e(TAG, "logInvalidPrivacyLevelError:" + Log.getStackTraceString(e));
    }
}
