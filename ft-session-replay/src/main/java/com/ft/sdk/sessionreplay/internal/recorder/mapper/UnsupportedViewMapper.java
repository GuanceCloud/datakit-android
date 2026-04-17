package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.view.View;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.internal.recorder.ViewUtilsInternal;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseWireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.Collections;
import java.util.List;

public class UnsupportedViewMapper extends BaseWireframeMapper<View> {

    private static final String TOOLBAR_LABEL = "Toolbar";
    private static final String DEFAULT_LABEL = "Unsupported view";

    public UnsupportedViewMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            View view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        float pixelsDensity = mappingContext.getSystemInformation().getScreenDensity();
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);

        return Collections.singletonList(
                new PlaceholderWireframe(
                        resolveViewId(view),
                        viewGlobalBounds.getX(),
                        viewGlobalBounds.getY(),
                        viewGlobalBounds.getWidth(),
                        viewGlobalBounds.getHeight(),
                        null,
                        resolveViewTitle(view)
                )
        );
    }

    private String resolveViewTitle(View view) {
        ViewUtilsInternal viewUtilsInternal = new ViewUtilsInternal();
        if (viewUtilsInternal.isToolbar(view)) {
            return TOOLBAR_LABEL;
        } else {
            return DEFAULT_LABEL;
        }
    }
}