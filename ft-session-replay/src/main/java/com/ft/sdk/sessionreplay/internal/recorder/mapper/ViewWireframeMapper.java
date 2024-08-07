package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.view.View;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseWireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class ViewWireframeMapper extends BaseWireframeMapper<View> {

    public ViewWireframeMapper(
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
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                view,
                mappingContext.getSystemInformation().getScreenDensity()
        );
        ShapeStyle shapeStyle = view.getBackground() != null
                ? resolveShapeStyle(view.getBackground(), view.getAlpha(), internalLogger)
                : null;

        if (shapeStyle != null) {
            ArrayList<Wireframe> list = new ArrayList<>();
            list.add(new ShapeWireframe(
                    resolveViewId(view),
                    viewGlobalBounds.getX(),
                    viewGlobalBounds.getY(),
                    viewGlobalBounds.getWidth(),
                    viewGlobalBounds.getHeight(),
                    null,
                    shapeStyle,
                    null
            ));
            return list;
        } else {
            return new ArrayList<>();
        }
    }
}