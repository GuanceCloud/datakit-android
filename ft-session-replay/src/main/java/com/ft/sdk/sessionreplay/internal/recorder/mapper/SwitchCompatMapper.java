package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;
import static com.ft.sdk.sessionreplay.internal.recorder.mapper.ImageViewMapper.DRAWABLE_CHILD_NAME;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Pair;

import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.GlobalBoundsInPx;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class SwitchCompatMapper extends CheckableWireframeMapper<SwitchCompat> {

    public static final String TRACK_KEY_NAME = "track";
    private final TextViewMapper<SwitchCompat> textWireframeMapper;

    public SwitchCompatMapper(
            TextViewMapper<SwitchCompat> textWireframeMapper,
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
        this.textWireframeMapper = textWireframeMapper;
    }

    @UiThread
    @Override
    public List<Wireframe> resolveMainWireframes(
            SwitchCompat view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger) {
        return textWireframeMapper.map(view, mappingContext, asyncJobStatusCallback, internalLogger);
    }

    @UiThread
    @Override
    public List<Wireframe> resolveMaskedCheckable(
            SwitchCompat view,
            MappingContext mappingContext
    ) {
        float pixelsDensity = mappingContext.getSystemInformation().getScreenDensity();
        List<Wireframe> wireframes = new ArrayList<>();
        GlobalBoundsInPx trackBounds = resolveTrackBounds(view, pixelsDensity);
        if (trackBounds == null) {
            return null;
        }
        String checkableColor = resolveCheckableColor(view);

        Long trackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, TRACK_KEY_NAME);
        if (trackId != null) {
            ShapeStyle trackShapeStyle = resolveTrackShapeStyle(view, checkableColor);
            ShapeWireframe trackWireframe = new ShapeWireframe(
                    trackId,
                    Utils.densityNormalized(trackBounds.getX(), pixelsDensity),
                    Utils.densityNormalized(trackBounds.getY(), pixelsDensity),
                    Utils.densityNormalized(trackBounds.getWidth(), pixelsDensity),
                    Utils.densityNormalized(trackBounds.getHeight(), pixelsDensity),
                    null,
                    trackShapeStyle, null
            );
            wireframes.add(trackWireframe);
        }

        return wireframes;
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


    @Override
    protected List<Wireframe> resolveCheckable(SwitchCompat view, MappingContext mappingContext, AsyncJobStatusCallback asyncJobStatusCallback) {
        return createSwitchCompatDrawableWireFrames(view, mappingContext, asyncJobStatusCallback);
    }

    private ShapeStyle resolveTrackShapeStyle(SwitchCompat view, String checkBoxColor) {
        return new ShapeStyle(
                checkBoxColor,
                view.getAlpha(), null
        );
    }

    // Internal method
    private String resolveCheckableColor(SwitchCompat view) {
        return colorStringFormatter.formatColorAndAlphaAsHexString(view.getCurrentTextColor(), OPAQUE_ALPHA_VALUE);
    }

    private List<Wireframe> createSwitchCompatDrawableWireFrames(
            SwitchCompat view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback) {
        int index = 0;
        Wireframe thumbWireframe = createThumbWireframe(view, index, mappingContext, asyncJobStatusCallback);
        if (thumbWireframe != null) {
            index++;
        }
        Wireframe trackWireframe = createTrackWireframe(view, index, mappingContext, asyncJobStatusCallback);

        List<Wireframe> wireframes = new ArrayList<>();
        if (trackWireframe != null) wireframes.add(trackWireframe);
        if (thumbWireframe != null) wireframes.add(thumbWireframe);

        return wireframes;
    }

    private Wireframe createTrackWireframe(
            SwitchCompat view,
            int prevIndex,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback) {
        GlobalBoundsInPx trackBounds = resolveTrackBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        if (trackBounds == null) return null;

        Drawable trackDrawable = view.getTrackDrawable();
        DrawableCopier drawableCopier = (originalDrawable, resources) -> {
            Drawable newDrawable = originalDrawable.getConstantState().newDrawable(resources);
            newDrawable.setState(view.getTrackDrawable().getState());
            newDrawable.setBounds(view.getTrackDrawable().getBounds());
            if (view.getTrackTintList() != null) {
                newDrawable.setTintList(view.getTrackTintList());
            }
            return newDrawable;
        };

        return mappingContext.getImageWireframeHelper().createImageWireframeByDrawable(
                view,
                mapInputPrivacyToImagePrivacy(mappingContext.getTextAndInputPrivacy()),
                prevIndex + 1,
                Utils.densityNormalized(trackBounds.getX(), mappingContext.getSystemInformation().getScreenDensity()),
                Utils.densityNormalized(trackBounds.getY(), mappingContext.getSystemInformation().getScreenDensity()),
                trackBounds.getWidth(),
                trackBounds.getHeight(),
                true,
                trackDrawable,
                drawableCopier,
                asyncJobStatusCallback,
                null,
                null,
                null, null, null
        );
    }

    private Wireframe createThumbWireframe(
            SwitchCompat view,
            int prevIndex,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback) {
        GlobalBoundsInPx thumbBounds = resolveThumbBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        if (thumbBounds == null) return null;

        Drawable thumbDrawable = view.getThumbDrawable();
        DrawableCopier drawableCopier = (originalDrawable, resources) -> {
            Drawable newDrawable = originalDrawable.getConstantState().newDrawable(resources);
            newDrawable.setState(view.getThumbDrawable().getState());
            newDrawable.setBounds(view.getThumbDrawable().getBounds());
            if (view.getThumbTintList() != null) {
                newDrawable.setTintList(view.getThumbTintList());
            }
            return newDrawable;
        };

        return mappingContext.getImageWireframeHelper().createImageWireframeByDrawable(
                view,
                mapInputPrivacyToImagePrivacy(mappingContext.getTextAndInputPrivacy()),
                prevIndex + 1,
                Utils.densityNormalized(thumbBounds.getX(), mappingContext.getSystemInformation().getScreenDensity()),
                Utils.densityNormalized(thumbBounds.getY(), mappingContext.getSystemInformation().getScreenDensity()),
                thumbDrawable.getIntrinsicWidth(),
                thumbDrawable.getIntrinsicHeight(),
                true,
                thumbDrawable,
                drawableCopier,
                asyncJobStatusCallback,
                null,
                null,
                null, DRAWABLE_CHILD_NAME,
                null

        );
    }

    private GlobalBoundsInPx resolveThumbBounds(SwitchCompat view, float pixelsDensity) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);
        Pair<Integer, Integer> thumbDimensions = resolveThumbSizeInPx(view);
        if (thumbDimensions == null) return null;

        int thumbLeft = (int) (viewGlobalBounds.getX() * pixelsDensity) + view.getThumbDrawable().getBounds().left;
        int thumbTop = (int) (viewGlobalBounds.getY() * pixelsDensity) + view.getThumbDrawable().getBounds().top;

        return new GlobalBoundsInPx(thumbLeft, thumbTop, thumbDimensions.first, thumbDimensions.second);
    }

    private Pair<Integer, Integer> resolveThumbSizeInPx(SwitchCompat view) {
        Drawable thumbDrawable = view.getThumbDrawable();
        return thumbDrawable != null ? new Pair<>(thumbDrawable.getIntrinsicWidth(), thumbDrawable.getIntrinsicHeight()) : null;
    }

    private GlobalBoundsInPx resolveTrackBounds(SwitchCompat view, float pixelsDensity) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);
        Pair<Integer, Integer> trackSize = resolveTrackSizeInPx(view);
        if (trackSize == null) return null;

        return view.getTrackDrawable() != null ?
                new GlobalBoundsInPx(
                        (int) (viewGlobalBounds.getX() * pixelsDensity) + view.getTrackDrawable().getBounds().left,
                        (int) (viewGlobalBounds.getY() * pixelsDensity) + view.getTrackDrawable().getBounds().top,
                        trackSize.first,
                        trackSize.second
                ) : null;
    }

    private ShapeStyle resolveThumbShapeStyle(SwitchCompat view, String checkBoxColor) {
        return new ShapeStyle(
                checkBoxColor,
                view.getAlpha(),
                THUMB_CORNER_RADIUS
        );
    }


    private Pair<Integer, Integer> resolveTrackSizeInPx(SwitchCompat view) {
        Drawable trackDrawable = view.getTrackDrawable();
        return trackDrawable != null ? new Pair<>(trackDrawable.getBounds().width(), trackDrawable.getBounds().height()) : null;
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
    private static final int THUMB_CORNER_RADIUS = 10;

}