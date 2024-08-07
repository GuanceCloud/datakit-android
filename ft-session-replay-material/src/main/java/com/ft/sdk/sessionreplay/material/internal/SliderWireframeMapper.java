package com.ft.sdk.sessionreplay.material.internal;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;
import static com.ft.sdk.sessionreplay.ColorConstant.PARTIALLY_OPAQUE_ALPHA_VALUE;

import android.content.res.ColorStateList;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class SliderWireframeMapper implements WireframeMapper<Slider> {

    private final ViewIdentifierResolver viewIdentifierResolver;
    private final ColorStringFormatter colorStringFormatter;
    private final ViewBoundsResolver viewBoundsResolver;

    public SliderWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver
    ) {
        this.viewIdentifierResolver = viewIdentifierResolver;
        this.colorStringFormatter = colorStringFormatter;
        this.viewBoundsResolver = viewBoundsResolver;
    }

    @SuppressWarnings("LongMethod")
    @UiThread
    @Override
    public List<Wireframe> map(
            Slider view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        Long activeTrackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, TRACK_ACTIVE_KEY_NAME);
        Long nonActiveTrackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, TRACK_NON_ACTIVE_KEY_NAME);
        Long thumbId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, THUMB_KEY_NAME);

        if (activeTrackId == null || thumbId == null || nonActiveTrackId == null) {
            return new ArrayList<>();
        }

        float screenDensity = mappingContext.getSystemInformation().getScreenDensity();
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, screenDensity);
        float normalizedSliderValue = normalizedValue(view);
        float viewAlpha = view.getAlpha();

        // padding
        long trackLeftPadding = trackLeftPadding(view, screenDensity);
        long trackTopPadding = Utils.densityNormalized(view.getPaddingTop(), screenDensity);

        // colors
        int[] drawableState = view.getDrawableState();
        int trackActiveColor = getColor(view.getTrackActiveTintList(), drawableState);
        int trackNonActiveColor = getColor(view.getTrackInactiveTintList(), drawableState);
        int thumbColor = getColor(view.getThumbTintList(), drawableState);
        String trackActiveColorAsHexa = colorStringFormatter.formatColorAndAlphaAsHexString(trackActiveColor, OPAQUE_ALPHA_VALUE);
        String trackNonActiveColorAsHexa = colorStringFormatter.formatColorAndAlphaAsHexString(trackNonActiveColor, PARTIALLY_OPAQUE_ALPHA_VALUE);
        String thumbColorAsHexa = colorStringFormatter.formatColorAndAlphaAsHexString(thumbColor, OPAQUE_ALPHA_VALUE);

        // track dimensions
        long trackWidth = Utils.densityNormalized(view.getTrackWidth(), screenDensity);
        long trackHeight = Utils.densityNormalized(view.getTrackHeight(), screenDensity);
        long trackActiveWidth = (long) (trackWidth * normalizedSliderValue);

        // track positions
        long trackXPos = viewGlobalBounds.getX() + trackLeftPadding;
        long trackYPos = viewGlobalBounds.getY() + trackTopPadding + (viewGlobalBounds.getHeight() - trackHeight) / 2;

        // thumb dimensions
        long thumbHeight = Utils.densityNormalized(view.getThumbRadius() * 2, screenDensity);

        // thumb positions
        long thumbXPos = (long) (trackXPos + trackWidth * normalizedSliderValue);
        long thumbYPos = viewGlobalBounds.getY() + trackTopPadding + (viewGlobalBounds.getHeight() - thumbHeight) / 2;

        ShapeWireframe trackNonActiveWireframe = new ShapeWireframe(
                nonActiveTrackId,
                trackXPos,
                trackYPos,
                trackWidth,
                trackHeight,
                null,
                new ShapeStyle(trackNonActiveColorAsHexa, viewAlpha, null),
                null
        );
        ShapeWireframe trackActiveWireframe = new ShapeWireframe(
                activeTrackId,
                trackXPos,
                trackYPos,
                trackActiveWidth,
                trackHeight,
                null,
                new ShapeStyle(trackActiveColorAsHexa, viewAlpha, null), null
        );
        ShapeWireframe thumbWireframe = new ShapeWireframe(
                thumbId,
                thumbXPos,
                thumbYPos,
                thumbHeight,
                thumbHeight,
                null,
                new ShapeStyle(thumbColorAsHexa, viewAlpha, THUMB_SHAPE_CORNER_RADIUS),
                null
        );

        List<Wireframe> wireframes = new ArrayList<>();
        if (mappingContext.getPrivacy() == SessionReplayPrivacy.ALLOW) {
            wireframes.add(trackNonActiveWireframe);
            wireframes.add(trackActiveWireframe);
            wireframes.add(thumbWireframe);
        } else {
            wireframes.add(trackNonActiveWireframe);
        }

        return wireframes;
    }

    private int getColor(ColorStateList colorStateList, int[] state) {
        return colorStateList.getColorForState(state, colorStateList.getDefaultColor());
    }


    private long trackLeftPadding(Slider slider, float screenDensity) {
        return Utils.densityNormalized(slider.getTrackSidePadding(), screenDensity) +
                Utils.densityNormalized(slider.getPaddingStart(), screenDensity);
    }

    private float normalizedValue(Slider slider) {
        float range = slider.getValueTo() - slider.getValueFrom();
        return range == 0 ? 0 : (slider.getValue() - slider.getValueFrom()) / range;
    }

    private static final String TRACK_ACTIVE_KEY_NAME = "slider_active_track";
    private static final String TRACK_NON_ACTIVE_KEY_NAME = "slider_non_active_track";
    private static final String THUMB_KEY_NAME = "slider_thumb";
    private static final int THUMB_SHAPE_CORNER_RADIUS = 10;
}