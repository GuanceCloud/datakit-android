package com.ft.sdk.sessionreplay.recorder.mapper;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

public abstract class BaseWireframeMapper<T extends View> implements WireframeMapper<T> {

    protected final ViewIdentifierResolver viewIdentifierResolver;
    protected final ColorStringFormatter colorStringFormatter;
    protected final ViewBoundsResolver viewBoundsResolver;
    protected final DrawableToColorMapper drawableToColorMapper;

    public BaseWireframeMapper(ViewIdentifierResolver viewIdentifierResolver,
                               ColorStringFormatter colorStringFormatter,
                               ViewBoundsResolver viewBoundsResolver,
                               DrawableToColorMapper drawableToColorMapper) {
        this.viewIdentifierResolver = viewIdentifierResolver;
        this.colorStringFormatter = colorStringFormatter;
        this.viewBoundsResolver = viewBoundsResolver;
        this.drawableToColorMapper = drawableToColorMapper;
    }

    /**
     * Resolves the [View] unique id to be used in the mapped [MobileSegment.Wireframe].
     */
    protected long resolveViewId(View view) {
        return viewIdentifierResolver.resolveViewId(view);
    }

    /**
     * Resolves the [MobileSegment.ShapeStyle] based on the [View] drawables.
     */
    protected ShapeStyle resolveShapeStyle(
            Drawable drawable,
            float viewAlpha,
            InternalLogger internalLogger
    ) {
        Integer color = drawableToColorMapper.mapDrawableToColor(drawable, internalLogger);
        if (color != null) {
            String stringColor = colorStringFormatter.formatColorAsHexString(color);
            return new ShapeStyle(stringColor, viewAlpha, null);
        } else {
            return null;
        }
    }
}
