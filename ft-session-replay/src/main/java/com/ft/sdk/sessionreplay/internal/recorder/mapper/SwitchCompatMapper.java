package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.graphics.Rect;

import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class SwitchCompatMapper extends CheckableWireframeMapper<SwitchCompat> {

    private final TextViewMapper<SwitchCompat> textWireframeMapper;

    public SwitchCompatMapper(
            TextViewMapper<SwitchCompat> textWireframeMapper,
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
            SwitchCompat view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        return textWireframeMapper.map(view, mappingContext, asyncJobStatusCallback, internalLogger);
    }

    @UiThread
    @Override
    public List<Wireframe> resolveCheckedCheckable(
            SwitchCompat view,
            MappingContext mappingContext
    ) {
        long[] trackThumbDimensions = resolveThumbAndTrackDimensions(view, mappingContext.getSystemInformation());
        if (trackThumbDimensions == null) {
            return null;
        }

        List<Wireframe> wireframes = new ArrayList<>();

        long trackWidth = trackThumbDimensions[TRACK_WIDTH_INDEX];
        long trackHeight = trackThumbDimensions[TRACK_HEIGHT_INDEX];
        long thumbHeight = trackThumbDimensions[THUMB_HEIGHT_INDEX];
        long thumbWidth = trackThumbDimensions[THUMB_WIDTH_INDEX];
        String checkableColor = resolveCheckableColor(view);
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                view,
                mappingContext.getSystemInformation().getScreenDensity()
        );

        Long trackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, TRACK_KEY_NAME);
        if (trackId != null) {
            ShapeStyle trackShapeStyle = resolveTrackShapeStyle(view, checkableColor);
            ShapeWireframe trackWireframe = new ShapeWireframe(
                    trackId,
                    viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - trackWidth,
                    viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - trackHeight) / 2,
                    trackWidth,
                    trackHeight,
                    null,
                    trackShapeStyle,
                    null
            );
            wireframes.add(trackWireframe);
        }

        Long thumbId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, THUMB_KEY_NAME);
        if (thumbId != null) {
            ShapeStyle thumbShapeStyle = resolveThumbShapeStyle(view, checkableColor);
            ShapeWireframe thumbWireframe = new ShapeWireframe(
                    thumbId,
                    viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - thumbWidth,
                    viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - thumbHeight) / 2,
                    thumbWidth,
                    thumbHeight,
                    null,
                    thumbShapeStyle, null
            );
            wireframes.add(thumbWireframe);
        }
        return wireframes;
    }

    @UiThread
    @Override
    public List<Wireframe> resolveNotCheckedCheckable(
            SwitchCompat view,
            MappingContext mappingContext
    ) {
        long[] trackThumbDimensions = resolveThumbAndTrackDimensions(view, mappingContext.getSystemInformation());
        if (trackThumbDimensions == null) {
            return null;
        }

        List<Wireframe> wireframes = new ArrayList<>();

        long trackWidth = trackThumbDimensions[TRACK_WIDTH_INDEX];
        long trackHeight = trackThumbDimensions[TRACK_HEIGHT_INDEX];
        long thumbHeight = trackThumbDimensions[THUMB_HEIGHT_INDEX];
        long thumbWidth = trackThumbDimensions[THUMB_WIDTH_INDEX];
        String checkableColor = resolveCheckableColor(view);
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                view,
                mappingContext.getSystemInformation().getScreenDensity()
        );

        Long trackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, TRACK_KEY_NAME);
        if (trackId != null) {
            ShapeStyle trackShapeStyle = resolveTrackShapeStyle(view, checkableColor);
            ShapeWireframe trackWireframe = new ShapeWireframe(
                    trackId,
                    viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - trackWidth,
                    viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - trackHeight) / 2,
                    trackWidth,
                    trackHeight,
                    null,
                    trackShapeStyle, null
            );
            wireframes.add(trackWireframe);
        }

        Long thumbId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, THUMB_KEY_NAME);
        if (thumbId != null) {
            ShapeStyle thumbShapeStyle = resolveThumbShapeStyle(view, checkableColor);
            ShapeWireframe thumbWireframe = new ShapeWireframe(
                    thumbId,
                    viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - trackWidth,
                    viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - thumbHeight) / 2,
                    thumbWidth,
                    thumbHeight,
                    null,
                    thumbShapeStyle, null
            );
            wireframes.add(thumbWireframe);
        }
        return wireframes;
    }

    @UiThread
    @Override
    public List<Wireframe> resolveMaskedCheckable(
            SwitchCompat view,
            MappingContext mappingContext
    ) {
        long[] trackThumbDimensions = resolveThumbAndTrackDimensions(view, mappingContext.getSystemInformation());
        if (trackThumbDimensions == null) {
            return null;
        }

        List<Wireframe> wireframes = new ArrayList<>();

        long trackWidth = trackThumbDimensions[TRACK_WIDTH_INDEX];
        long trackHeight = trackThumbDimensions[TRACK_HEIGHT_INDEX];
        String checkableColor = resolveCheckableColor(view);
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                view,
                mappingContext.getSystemInformation().getScreenDensity()
        );

        Long trackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, TRACK_KEY_NAME);
        if (trackId != null) {
            ShapeStyle trackShapeStyle = resolveTrackShapeStyle(view, checkableColor);
            ShapeWireframe trackWireframe = new ShapeWireframe(
                    trackId,
                    viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - trackWidth,
                    viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - trackHeight) / 2,
                    trackWidth,
                    trackHeight,
                    null,
                    trackShapeStyle, null
            );
            wireframes.add(trackWireframe);
        }

        return wireframes;
    }

    protected String resolveCheckableColor(SwitchCompat view) {
        return colorStringFormatter.formatColorAndAlphaAsHexString(view.getCurrentTextColor(), OPAQUE_ALPHA_VALUE);
    }

    private ShapeStyle resolveThumbShapeStyle(SwitchCompat view, String checkBoxColor) {
        return new ShapeStyle(
                checkBoxColor,
                view.getAlpha(),
                THUMB_CORNER_RADIUS
        );
    }

    protected ShapeStyle resolveTrackShapeStyle(SwitchCompat view, String checkBoxColor) {
        return new ShapeStyle(
                checkBoxColor,
                view.getAlpha()
                , null
        );
    }

    protected long[] resolveThumbAndTrackDimensions(
            SwitchCompat view,
            SystemInformation systemInformation
    ) {
        float density = systemInformation.getScreenDensity();
        long thumbWidth;
        long trackHeight;
        // based on the implementation there is nothing drawn in the switcher area if one of
        // these are null
        if (view.getThumbDrawable() == null || view.getTrackDrawable() == null) {
            return null;
        }
        Rect paddingRect = new Rect();
        view.getThumbDrawable().getPadding(paddingRect);
        long totalHorizontalPadding =
                Utils.densityNormalized(paddingRect.left, density) +
                        Utils.densityNormalized(paddingRect.right, density);
        thumbWidth = Utils.densityNormalized(view.getThumbDrawable().getIntrinsicWidth(), density) -
                totalHorizontalPadding;
        long thumbHeight = thumbWidth;
        // for some reason there is no padding added in the trackDrawable
        // in order to normalise with the padding applied to the width we will have to
        // use the horizontal padding applied.
        trackHeight = Utils.densityNormalized(view.getTrackDrawable().getIntrinsicHeight(), density) -
                totalHorizontalPadding;
        long trackWidth = thumbWidth * 2;
        long[] dimensions = new long[NUMBER_OF_DIMENSIONS];
        dimensions[THUMB_WIDTH_INDEX] = thumbWidth;
        dimensions[THUMB_HEIGHT_INDEX] = thumbHeight;
        dimensions[TRACK_WIDTH_INDEX] = trackWidth;
        dimensions[TRACK_HEIGHT_INDEX] = trackHeight;
        return dimensions;
    }


    private static final int NUMBER_OF_DIMENSIONS = 4;
    private static final int THUMB_WIDTH_INDEX = 0;
    private static final int THUMB_HEIGHT_INDEX = 1;

    private static final int TRACK_WIDTH_INDEX = 2;
    private static final int TRACK_HEIGHT_INDEX = 3;
    private static final String THUMB_KEY_NAME = "thumb";
    private static final String TRACK_KEY_NAME = "track";
    private static final int THUMB_CORNER_RADIUS = 10;
}