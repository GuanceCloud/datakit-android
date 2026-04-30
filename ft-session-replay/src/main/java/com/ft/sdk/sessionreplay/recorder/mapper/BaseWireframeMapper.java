package com.ft.sdk.sessionreplay.recorder.mapper;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

/**
 * Base class for custom mappers that need common view id, bounds, color, and
 * drawable helpers.
 *
 * @param <T> the view type supported by the mapper
 */
public abstract class BaseWireframeMapper<T extends View> implements WireframeMapper<T> {

    protected final ViewIdentifierResolver viewIdentifierResolver;
    protected final ColorStringFormatter colorStringFormatter;
    protected final ViewBoundsResolver viewBoundsResolver;
    protected final DrawableToColorMapper drawableToColorMapper;

    /**
     * Creates a base mapper.
     *
     * @param viewIdentifierResolver resolver for stable Session Replay view ids
     * @param colorStringFormatter formatter for wireframe color strings
     * @param viewBoundsResolver resolver for global view bounds
     * @param drawableToColorMapper mapper used to extract colors from drawables
     */
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
     * Resolves the {@link View} unique id to be used in mapped wireframes.
     */
    protected long resolveViewId(View view) {
        return viewIdentifierResolver.resolveViewId(view);
    }

    /**
     * Resolves the {@link ShapeStyle} based on the {@link View} drawables.
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
