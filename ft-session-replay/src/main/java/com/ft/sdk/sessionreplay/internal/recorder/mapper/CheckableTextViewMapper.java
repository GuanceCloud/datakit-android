package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.graphics.drawable.Drawable;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.ShapeBorder;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.Collections;
import java.util.List;

public abstract class CheckableTextViewMapper<T extends TextView & Checkable> extends CheckableWireframeMapper<T> {

    private final TextViewMapper<T> textWireframeMapper;

    public CheckableTextViewMapper(
            TextViewMapper<T> textWireframeMapper,
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
        this.textWireframeMapper = textWireframeMapper;
    }

    @UiThread
    @Override
    public List<Wireframe> resolveMainWireframes(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        return textWireframeMapper.map(view, mappingContext, asyncJobStatusCallback, internalLogger);
    }

    @UiThread
    @Override
    public List<Wireframe> resolveCheckable(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback
    ) {
        return Collections.singletonList(createCheckableDrawableWireFrames(view,
                mappingContext, asyncJobStatusCallback));
    }

    @UiThread
    @Override
    public List<Wireframe> resolveCheckedCheckable(
            T view,
            MappingContext mappingContext) {
        Long checkableId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, CHECKABLE_KEY_NAME);
        if (checkableId == null) {
            return null;
        }

        String checkBoxColor = resolveCheckableColor(view);
        GlobalBounds checkBoxBounds = resolveCheckableBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        ShapeStyle shapeStyle = resolveCheckedShapeStyle(view, checkBoxColor);
        ShapeBorder shapeBorder = resolveCheckedShapeBorder(view, checkBoxColor);

        return Collections.singletonList(new ShapeWireframe(
                checkableId,
                checkBoxBounds.getX(),
                checkBoxBounds.getY(),
                checkBoxBounds.getWidth(),
                checkBoxBounds.getHeight(),
                null,
                shapeStyle,
                shapeBorder
        ));
    }

    @UiThread
    @Override
    public List<Wireframe> resolveNotCheckedCheckable(
            T view,
            MappingContext mappingContext) {
        Long checkableId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, CHECKABLE_KEY_NAME);
        if (checkableId == null) {
            return null;
        }

        String checkBoxColor = resolveCheckableColor(view);
        GlobalBounds checkBoxBounds = resolveCheckableBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        ShapeBorder shapeBorder = resolveNotCheckedShapeBorder(view, checkBoxColor);
        ShapeStyle shapeStyle = resolveNotCheckedShapeStyle(view, checkBoxColor);

        return Collections.singletonList(new ShapeWireframe(
                checkableId,
                checkBoxBounds.getX(),
                checkBoxBounds.getY(),
                checkBoxBounds.getWidth(),
                checkBoxBounds.getHeight(),
                null,
                shapeStyle,
                shapeBorder
        ));
    }


    @UiThread
    @Override
    public List<Wireframe> resolveMaskedCheckable(T view, MappingContext mappingContext) {
        Long checkableId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, CHECKABLE_KEY_NAME);
        if (checkableId == null) return null;

        String checkBoxColor = resolveCheckableColor(view);
        GlobalBounds checkBoxBounds = resolveCheckableBounds(view,
                mappingContext.getSystemInformation().getScreenDensity());
        ShapeBorder shapeBorder = resolveNotCheckedShapeBorder(view, checkBoxColor);
        ShapeStyle shapeStyle = resolveNotCheckedShapeStyle(view, checkBoxColor);

        return List.of(
                new ShapeWireframe(
                        checkableId,
                        checkBoxBounds.getX(),
                        checkBoxBounds.getY(),
                        checkBoxBounds.getWidth(),
                        checkBoxBounds.getHeight(),
                        null,
                        shapeStyle,
                        shapeBorder
                )
        );
    }

    @UiThread
    public abstract GlobalBounds resolveCheckableBounds(T view, float pixelsDensity);

    @UiThread
    private Wireframe createCheckableDrawableWireFrames(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback
    ) {
        Drawable drawable = getCheckableDrawable(view);
        if (drawable == null) return null;

        GlobalBounds checkBoxBounds = resolveCheckableBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        DrawableCopier drawableCopier = (src, dest) -> cloneCheckableDrawable(view, drawable);

        return mappingContext.getImageWireframeHelper().createImageWireframeByDrawable(
                view,
                mapInputPrivacyToImagePrivacy(mappingContext.getTextAndInputPrivacy()),
                0,
                checkBoxBounds.getX(),
                checkBoxBounds.getY(),
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                true,
                drawable,
                drawableCopier,
                asyncJobStatusCallback,
                new WireframeClip(null, null, null, null),
                null,
                null,
                null,
                null
        );
    }

    @UiThread
    protected ShapeStyle resolveCheckedShapeStyle(T view, String checkBoxColor) {
        return new ShapeStyle(checkBoxColor, view.getAlpha(), null);
    }

    @UiThread
    protected ShapeBorder resolveCheckedShapeBorder(T view, String checkBoxColor) {
        return new ShapeBorder(checkBoxColor, CHECKABLE_BORDER_WIDTH);
    }


    public abstract Drawable cloneCheckableDrawable(T view, Drawable drawable);

    @UiThread
    public abstract Drawable getCheckableDrawable(T view);

    protected String resolveCheckableColor(T view) {
        return colorStringFormatter.formatColorAndAlphaAsHexString(view.getCurrentTextColor(), OPAQUE_ALPHA_VALUE);
    }

    protected ShapeStyle resolveNotCheckedShapeStyle(T view, String checkBoxColor) {
        return null;
    }

    protected ShapeBorder resolveNotCheckedShapeBorder(T view, String checkBoxColor) {
        return new ShapeBorder(checkBoxColor, CHECKABLE_BORDER_WIDTH);
    }

    private static final String CHECKABLE_KEY_NAME = "checkable";
    private static final long CHECKABLE_BORDER_WIDTH = 1L;
    public static final int CHECK_BOX_CHECKED_DRAWABLE_INDEX = 0;
    public static final int CHECK_BOX_NOT_CHECKED_DRAWABLE_INDEX = 1;
}
