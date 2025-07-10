package com.ft.sdk.sessionreplay.material.internal;

import androidx.cardview.widget.CardView;

import com.ft.sdk.sessionreplay.model.ShapeBorder;
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
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.List;

public class CardWireframeMapper extends BaseViewGroupMapper<CardView> {

    public CardWireframeMapper( ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    public List<Wireframe> map(
            CardView view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                view,
                mappingContext.getSystemInformation().getScreenDensity()
        );
        ShapeStyle shapeStyle = resolveShapeStyle(view, mappingContext);

        // Only MaterialCardView can have a built-in border.
        ShapeBorder shapeBorder = (view instanceof MaterialCardView)
                ? resolveShapeBorder((MaterialCardView) view, mappingContext)
                : null;

        return Collections.singletonList(
                new ShapeWireframe(
                        resolveViewId(view),
                        viewGlobalBounds.getX(),
                        viewGlobalBounds.getY(),
                        viewGlobalBounds.getWidth(),
                        viewGlobalBounds.getHeight(),
                        null,
                        shapeStyle,
                        shapeBorder
                )
        );
    }

    private ShapeBorder resolveShapeBorder(
            MaterialCardView view,
            MappingContext mappingContext
    ) {
        int strokeColor = (view.getStrokeColorStateList() != null)
                ? view.getStrokeColorStateList().getDefaultColor()
                : view.getStrokeColor();
        return new ShapeBorder(
                colorStringFormatter.formatColorAsHexString(strokeColor),
                Utils.densityNormalized(view.getStrokeWidth(),
                        mappingContext.getSystemInformation().getScreenDensity())
        );
    }

    private ShapeStyle resolveShapeStyle(
            CardView view,
            MappingContext mappingContext
    ) {
        int backgroundColor = view.getCardBackgroundColor().getDefaultColor();
        return new ShapeStyle(
                colorStringFormatter.formatColorAsHexString(backgroundColor),
                view.getAlpha(),
                Utils.densityNormalized(view.getRadius(),
                        mappingContext.getSystemInformation().getScreenDensity())
        );
    }

}