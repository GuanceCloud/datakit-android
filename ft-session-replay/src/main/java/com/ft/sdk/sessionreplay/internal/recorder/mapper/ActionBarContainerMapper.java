/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import androidx.annotation.UiThread;
import androidx.appcompat.widget.ActionBarContainer;
import androidx.appcompat.widget.FTSDKContainerAccessor;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseViewGroupMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.Collections;
import java.util.List;

public class ActionBarContainerMapper extends BaseViewGroupMapper<ActionBarContainer> {

    public ActionBarContainerMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            @SuppressLint("RestrictedApi") ActionBarContainer view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger) {

        Drawable background = new FTSDKContainerAccessor(view).getBackgroundDrawable();
        ShapeStyle shapeStyle = (background != null) ?
                resolveShapeStyle(background, view.getAlpha(), internalLogger) : null;
        Long id = viewIdentifierResolver.resolveChildUniqueIdentifier(view, PREFIX_BACKGROUND_DRAWABLE);

        if (shapeStyle != null && id != null) {
            float density = mappingContext.getSystemInformation().getScreenDensity();
            GlobalBounds bounds = viewBoundsResolver.resolveViewGlobalBounds(view, density);

            return Collections.singletonList(
                    new ShapeWireframe(
                            id,
                            bounds.getX(),
                            bounds.getY(),
                            bounds.getWidth(),
                            bounds.getHeight()
                            , null,
                            shapeStyle,
                            null
                    )
            );
        } else {
            return List.of();
        }
    }
}
