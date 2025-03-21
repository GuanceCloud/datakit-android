package com.ft.sdk.sessionreplay.internal.recorder.mapper;


import android.view.View;

import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.Collections;
import java.util.List;

public class HiddenViewMapper implements WireframeMapper<View> {

    private final ViewIdentifierResolver viewIdentifierResolver;
    private final ViewBoundsResolver viewBoundsResolver;

    public HiddenViewMapper(ViewIdentifierResolver viewIdentifierResolver, ViewBoundsResolver viewBoundsResolver) {
        this.viewIdentifierResolver = viewIdentifierResolver;
        this.viewBoundsResolver = viewBoundsResolver;
    }

    public List<Wireframe> map(
            View view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger) {

        Long id = viewIdentifierResolver.resolveChildUniqueIdentifier(view, HIDDEN_KEY_NAME);
        if (id == null) {
            return Collections.emptyList();
        }

        float density = mappingContext.getSystemInformation().getScreenDensity();
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, density);

        return Collections.singletonList(
                new PlaceholderWireframe(
                        id,
                        viewGlobalBounds.getX(),
                        viewGlobalBounds.getY(),
                        viewGlobalBounds.getWidth(),
                        viewGlobalBounds.getHeight(),
                        null,
                        HIDDEN_VIEW_PLACEHOLDER_TEXT
                )
        );
    }

    private static final String HIDDEN_VIEW_PLACEHOLDER_TEXT = "Hidden";
    private static final String HIDDEN_KEY_NAME = "hidden";
}