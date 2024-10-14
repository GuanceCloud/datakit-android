package com.ft.sdk.sessionreplay.recorder.mapper;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.List;

public abstract class BaseAsyncBackgroundWireframeMapper<T extends View> extends BaseWireframeMapper<T> {

    public static final String PREFIX_BACKGROUND_DRAWABLE = "backgroundDrawable";
    private final DefaultViewIdentifierResolver uniqueIdentifierGenerator = new DefaultViewIdentifierResolver();

    public BaseAsyncBackgroundWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    public List<Wireframe> map(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        Wireframe backgroundWireframe = resolveViewBackground(view, mappingContext, asyncJobStatusCallback, internalLogger);
        return backgroundWireframe != null ? List.of(backgroundWireframe) : List.of();
    }

    @UiThread
    private Wireframe resolveViewBackground(
            View view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        ShapeStyle shapeStyle = view.getBackground() != null ? resolveShapeStyle(view.getBackground(), view.getAlpha(), internalLogger) : null;
        GlobalBounds bounds = viewBoundsResolver.resolveViewGlobalBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        int width = view.getWidth();
        int height = view.getHeight();

        if (shapeStyle == null) {
            return resolveBackgroundAsImageWireframe(view, bounds, width, height, mappingContext, asyncJobStatusCallback);
        } else {
            return resolveBackgroundAsShapeWireframe(view, bounds, width, height, shapeStyle);
        }
    }

    private ShapeWireframe resolveBackgroundAsShapeWireframe(
            View view,
            GlobalBounds bounds,
            long width,
            long height,
            ShapeStyle shapeStyle
    ) {
        Long id = uniqueIdentifierGenerator.resolveChildUniqueIdentifier(view, PREFIX_BACKGROUND_DRAWABLE);
        if (id == null) {
            return null;
        }

        float density = view.getResources().getDisplayMetrics().density;
        if (density != 0f) {
            width = (long) (width / density);
            height = (long) (height / density);
        }


        return new ShapeWireframe(
                id,
                bounds.getX(),
                bounds.getY(),
                width,
                height,
                null,
                shapeStyle,
                null
        );
    }

    @UiThread
    private Wireframe resolveBackgroundAsImageWireframe(
            View view,
            GlobalBounds bounds,
            int width,
            int height,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback
    ) {
        if (view.getBackground() == null || view.getBackground().getConstantState() == null) {
            return null;
        }

        Drawable drawableCopy = view.getBackground().getConstantState().newDrawable(view.getResources());
        if (drawableCopy == null) {
            return null;
        }

        return mappingContext.getImageWireframeHelper().createImageWireframe(
                view,
                0,
                bounds.getX(),
                bounds.getY(),
                width,
                height,
                false,
                drawableCopy,
                asyncJobStatusCallback,
                new WireframeClip(null, null, null, null),
                null,
                null,
                PREFIX_BACKGROUND_DRAWABLE
        );
    }
}
