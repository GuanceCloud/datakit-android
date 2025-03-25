package com.ft.sdk.sessionreplay.internal.recorder;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.R;
import com.ft.sdk.sessionreplay.TouchPrivacy;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueRefs;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.HiddenViewMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.QueueStatusCallback;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.TraverseAllChildrenMapper;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.NoOpAsyncJobStatusCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeViewTraversal {
    private static final String TAG = "TreeViewTraversal";

    private final List<MapperTypeWrapper<?>> mappers;
    private final WireframeMapper<View> defaultViewMapper;
    private final WireframeMapper<View> decorViewMapper;
    private final ViewUtilsInternal viewUtilsInternal;
    private final InternalLogger internalLogger;
    private final HiddenViewMapper hiddenViewMapper;


    public TreeViewTraversal(
            List<MapperTypeWrapper<?>> mappers,
            WireframeMapper<View> defaultViewMapper,
            WireframeMapper<View> decorViewMapper,
            ViewUtilsInternal viewUtilsInternal,
            HiddenViewMapper hiddenViewMapper,
            InternalLogger internalLogger
    ) {
        this.mappers = mappers;
        this.defaultViewMapper = defaultViewMapper;
        this.decorViewMapper = decorViewMapper;
        this.viewUtilsInternal = viewUtilsInternal;
        this.internalLogger = internalLogger;
        this.hiddenViewMapper = hiddenViewMapper;
    }

    @SuppressWarnings("ReturnCount")
    @UiThread
    public TraversedTreeView traverse(
            View view,
            MappingContext mappingContext,
            RecordedDataQueueRefs recordedDataQueueRefs) {
        if (viewUtilsInternal.isNotVisible(view) ||
                viewUtilsInternal.isSystemNoise(view)) {
            return new TraversedTreeView(new ArrayList<>(), TraversalStrategy.STOP_AND_DROP_NODE);
        }

        TraversalStrategy traversalStrategy;

        AsyncJobStatusCallback jobStatusCallback;
        AsyncJobStatusCallback noOpCallback = new NoOpAsyncJobStatusCallback();

        // try to resolve from the exhaustive type mappers
        WireframeMapper<View> mapper = findMapperForView(view);
        updateTouchOverrideAreas(view, mappingContext);

        if (isHidden(view)) {
            traversalStrategy = TraversalStrategy.STOP_AND_RETURN_NODE;
            mapper = hiddenViewMapper;
            jobStatusCallback = noOpCallback;
        } else if (mapper != null) {
            jobStatusCallback = new QueueStatusCallback(recordedDataQueueRefs);
            traversalStrategy = (mapper instanceof TraverseAllChildrenMapper)
                    ? TraversalStrategy.TRAVERSE_ALL_CHILDREN
                    : TraversalStrategy.STOP_AND_RETURN_NODE;
        } else if (isDecorView(view)) {
            traversalStrategy = TraversalStrategy.TRAVERSE_ALL_CHILDREN;
            mapper = decorViewMapper;
            jobStatusCallback = noOpCallback;
        } else if (view instanceof ViewGroup) {
            traversalStrategy = TraversalStrategy.TRAVERSE_ALL_CHILDREN;
            mapper = defaultViewMapper;
            jobStatusCallback = noOpCallback;
        } else {
            traversalStrategy = TraversalStrategy.STOP_AND_RETURN_NODE;
            mapper = defaultViewMapper;
            jobStatusCallback = noOpCallback;
            String viewType = view.getClass().getCanonicalName();
            internalLogger.i(TAG, "No mapper found for view " + viewType + ","
                    + new HashMap<String, Object>() {{
                put("replay.widget.type", viewType);
            }}, true);
        }
        List<Wireframe> resolvedWireframes = mapper.map(view, mappingContext, jobStatusCallback, internalLogger);
        return new TraversedTreeView(resolvedWireframes, traversalStrategy);
    }

    private boolean isDecorView(View view) {
        ViewParent viewParent = view.getParent();
        return viewParent == null || !View.class.isAssignableFrom(viewParent.getClass());
    }

    private WireframeMapper<View> findMapperForView(View view) {
        for (MapperTypeWrapper<?> wrapper : mappers) {
            if (wrapper.supportsView(view)) {
                return (WireframeMapper<View>) wrapper.getUnsafeMapper();
            }
        }
        return null;
    }

    private boolean isHidden(View view) {
        return ((Boolean) true).equals(view.getTag(R.id.ft_hidden));
    }

    @UiThread
    private void updateTouchOverrideAreas(View view, MappingContext mappingContext) {
        Object touchPrivacy = view.getTag(R.id.ft_touch_privacy);

        if (touchPrivacy != null) {
            int[] locationOnScreen = new int[2];
            view.getLocationOnScreen(locationOnScreen);
            int x = locationOnScreen[0];
            int y = locationOnScreen[1];
            Rect viewArea = new Rect(
                    x - view.getPaddingLeft(),
                    y - view.getPaddingTop(),
                    x + view.getWidth() + view.getPaddingRight(),
                    y + view.getHeight() + view.getPaddingBottom()
            );

            try {
                TouchPrivacy privacyLevel = TouchPrivacy.valueOf(touchPrivacy.toString().toUpperCase());
                mappingContext.getTouchPrivacyManager().addTouchOverrideArea(viewArea, privacyLevel);
            } catch (IllegalArgumentException e) {
                internalLogger.e(TAG,
                        "INVALID_PRIVACY_LEVEL_ERROR:" + Log.getStackTraceString(e)
                );
            }
        }
    }


    public static class TraversedTreeView {
        private final List<Wireframe> mappedWireframes;
        private final TraversalStrategy nextActionStrategy;

        public TraversedTreeView(List<Wireframe> mappedWireframes, TraversalStrategy nextActionStrategy) {
            this.mappedWireframes = mappedWireframes;
            this.nextActionStrategy = nextActionStrategy;
        }

        public List<Wireframe> getMappedWireframes() {
            return mappedWireframes;
        }

        public TraversalStrategy getNextActionStrategy() {
            return nextActionStrategy;
        }
    }

}

