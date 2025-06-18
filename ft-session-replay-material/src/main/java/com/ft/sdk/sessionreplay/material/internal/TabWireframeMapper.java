package com.ft.sdk.sessionreplay.material.internal;

import android.widget.TextView;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TabWireframeMapper implements WireframeMapper<TabLayout.TabView> {

    private final ViewIdentifierResolver viewIdentifierResolver;
    private final ViewBoundsResolver viewBoundsResolver;
    private final WireframeMapper<TextView> textViewMapper;

    public TabWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ViewBoundsResolver viewBoundsResolver,
            WireframeMapper<TextView> textViewMapper
    ) {
        this.viewIdentifierResolver = viewIdentifierResolver;
        this.viewBoundsResolver = viewBoundsResolver;
        this.textViewMapper = textViewMapper;
    }

    public TabWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        this(
                viewIdentifierResolver,
                viewBoundsResolver,
                new TextViewMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        );
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            TabLayout.TabView view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        List<Wireframe> labelWireframes = findAndResolveLabelWireframes(
                view, mappingContext, asyncJobStatusCallback, internalLogger
        );
        if (view.isSelected()) {
            Wireframe selectedTabIndicatorWireframe = resolveTabIndicatorWireframe(
                    view, mappingContext.getSystemInformation(), labelWireframes.isEmpty() ? null : labelWireframes.get(0)
            );
            if (selectedTabIndicatorWireframe != null) {
                labelWireframes.add(selectedTabIndicatorWireframe);
            }
        }
        return labelWireframes;
    }

    protected Wireframe resolveTabIndicatorWireframe(
            TabLayout.TabView view,
            SystemInformation systemInformation,
            Wireframe wireframe
    ) {
        Long selectorId = viewIdentifierResolver.resolveChildUniqueIdentifier(
                view, SELECTED_TAB_INDICATOR_KEY_NAME
        );
        if (selectorId == null) {
            return null;
        }
        float screenDensity = systemInformation.getScreenDensity();
        GlobalBounds viewBounds = viewBoundsResolver.resolveViewGlobalBounds(view, screenDensity);
        long selectionIndicatorHeight = (long) Utils.densityNormalized(SELECTED_TAB_INDICATOR_HEIGHT_IN_PX, screenDensity);
        long paddingStart = (long) Utils.densityNormalized(view.getPaddingStart(), screenDensity);
        long paddingEnd = (long) Utils.densityNormalized(view.getPaddingEnd(), screenDensity);
        long selectionIndicatorXPos = viewBounds.getX() + paddingStart;
        long selectionIndicatorYPos = viewBounds.getY() + viewBounds.getHeight() - selectionIndicatorHeight;
        long selectionIndicatorWidth = viewBounds.getWidth() - paddingStart - paddingEnd;
        String selectionIndicatorColor = (wireframe instanceof TextWireframe) ?
                ((TextWireframe) wireframe).getTextStyle().getColor() :
                SELECTED_TAB_INDICATOR_DEFAULT_COLOR;
        ShapeStyle selectionIndicatorShapeStyle = new ShapeStyle(
                selectionIndicatorColor, view.getAlpha(), null
        );
        return new ShapeWireframe(
                selectorId,
                selectionIndicatorXPos,
                selectionIndicatorYPos,
                selectionIndicatorWidth,
                selectionIndicatorHeight,
                null,
                selectionIndicatorShapeStyle,
                null
        );
    }

    @UiThread
    private List<Wireframe> findAndResolveLabelWireframes(
            TabLayout.TabView view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        for (int i = 0; i < view.getChildCount(); i++) {
            android.view.View viewChild = view.getChildAt(i);
            if (viewChild instanceof TextView) {
                return textViewMapper.map(
                        (TextView) viewChild, mappingContext, asyncJobStatusCallback, internalLogger
                );
            }
        }
        return new ArrayList<>();
    }

    private static final String SELECTED_TAB_INDICATOR_KEY_NAME = "selected_tab_indicator";
    private static final String SELECTED_TAB_INDICATOR_DEFAULT_COLOR = "#000000";
    private static final long SELECTED_TAB_INDICATOR_HEIGHT_IN_PX = 5L;
}
