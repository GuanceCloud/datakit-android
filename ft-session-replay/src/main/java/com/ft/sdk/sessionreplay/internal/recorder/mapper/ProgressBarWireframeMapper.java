/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;
import static com.ft.sdk.sessionreplay.ColorConstant.PARTIALLY_OPAQUE_ALPHA_VALUE;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseAsyncBackgroundWireframeMapper;
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

public class ProgressBarWireframeMapper<P extends ProgressBar> extends BaseAsyncBackgroundWireframeMapper<P> {

    private final boolean showProgressWhenMaskUserInput;

    public ProgressBarWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper,
            boolean showProgressWhenMaskUserInput
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
        this.showProgressWhenMaskUserInput = showProgressWhenMaskUserInput;
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            P view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        List<Wireframe> wireframes = new ArrayList<>();

        // add background if needed
        wireframes.addAll(super.map(view, mappingContext, asyncJobStatusCallback, internalLogger));

        float screenDensity = mappingContext.getSystemInformation().getScreenDensity();
        GlobalBounds viewPaddedBounds = viewBoundsResolver.resolveViewPaddedBounds(view, screenDensity);
        long trackHeight = (long) Utils.densityNormalized(TRACK_HEIGHT_IN_PX, screenDensity);
        GlobalBounds trackBounds = new GlobalBounds(
                viewPaddedBounds.getX(),
                viewPaddedBounds.getY() + (viewPaddedBounds.getHeight() - trackHeight) / 2,
                viewPaddedBounds.getWidth(),
                trackHeight
        );

        int defaultColor = getDefaultColor(view);
        Integer trackColor = getColor(view.getProgressTintList(), view.getDrawableState());
        if (trackColor == null) {
            trackColor = defaultColor;
        }

        Wireframe nonActiveTrackWireframe = buildNonActiveTrackWireframe(view, trackBounds, trackColor);
        if (nonActiveTrackWireframe != null) {
            wireframes.add(nonActiveTrackWireframe);
        }

        boolean hasProgress = !view.isIndeterminate();
        boolean showProgress = (mappingContext.getTextAndInputPrivacy() == TextAndInputPrivacy.MASK_SENSITIVE_INPUTS) ||
                (mappingContext.getTextAndInputPrivacy() == TextAndInputPrivacy.MASK_ALL_INPUTS && showProgressWhenMaskUserInput);

        if (hasProgress && showProgress) {
            float normalizedProgress = normalizedProgress(view);
            mapDeterminate(
                    wireframes,
                    view,
                    mappingContext,
                    asyncJobStatusCallback,
                    internalLogger,
                    trackBounds,
                    trackColor,
                    normalizedProgress
            );
        }

        return wireframes;
    }

    protected void mapDeterminate(
            List<Wireframe> wireframes,
            P view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger,
            GlobalBounds trackBounds,
            int trackColor,
            float normalizedProgress
    ) {
        Wireframe activeTrackWireframe = buildActiveTrackWireframe(view, trackBounds, normalizedProgress, trackColor);
        if (activeTrackWireframe != null) {
            wireframes.add(activeTrackWireframe);
        }
    }

    private Wireframe buildNonActiveTrackWireframe(
            P view,
            GlobalBounds trackBounds,
            int trackColor
    ) {
        Long nonActiveTrackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, NON_ACTIVE_TRACK_KEY_NAME);
        if (nonActiveTrackId == null) {
            return null;
        }
        String backgroundColor = colorStringFormatter.formatColorAndAlphaAsHexString(
                trackColor,
                PARTIALLY_OPAQUE_ALPHA_VALUE
        );
        return new ShapeWireframe(
                nonActiveTrackId,
                trackBounds.getX(),
                trackBounds.getY(),
                trackBounds.getWidth(),
                trackBounds.getHeight(),
                null,
                new ShapeStyle(
                        backgroundColor,
                        null,
                        view.getAlpha()
                ), null
        );
    }

    private Wireframe buildActiveTrackWireframe(
            P view,
            GlobalBounds trackBounds,
            float normalizedProgress,
            int trackColor
    ) {
        Long activeTrackId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, ACTIVE_TRACK_KEY_NAME);
        if (activeTrackId == null) {
            return null;
        }
        String backgroundColor = colorStringFormatter.formatColorAndAlphaAsHexString(
                trackColor,
                OPAQUE_ALPHA_VALUE
        );
        return new ShapeWireframe(
                activeTrackId,
                trackBounds.getX(),
                trackBounds.getY(),
                (long) (trackBounds.getWidth() * normalizedProgress),
                trackBounds.getHeight(),
                null,
                new ShapeStyle(
                        backgroundColor,
                        null,
                        view.getAlpha()
                ), null
        );
    }

    private float normalizedProgress(P view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return normalizedProgressAndroidO(view);
        } else {
            return normalizedProgressLegacy(view);
        }
    }

    private float normalizedProgressLegacy(P view) {
        float range = view.getMax();
        if (view.getMax() == 0) {
            return 0f;
        } else {
            return view.getProgress() / range;
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private float normalizedProgressAndroidO(P view) {
        float range = view.getMax() - view.getMin();
        if (range == 0f) {
            return 0f;
        } else {
            return (view.getProgress() - view.getMin()) / range;
        }
    }

    protected Integer getColor(ColorStateList colorStateList, int[] state) {
        if (colorStateList != null) {
            return colorStateList.getColorForState(state, colorStateList.getDefaultColor());
        }
        return null;
    }

    protected int getDefaultColor(P view) {
        int uiModeFlags = view.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (uiModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            return NIGHT_MODE_COLOR;
        } else {
            return DAY_MODE_COLOR;
        }
    }

    private static final int NIGHT_MODE_COLOR = 0xffffff; // White
    private static final int DAY_MODE_COLOR = 0; // Black
    private static final String ACTIVE_TRACK_KEY_NAME = "seekbar_active_track";
    private static final String NON_ACTIVE_TRACK_KEY_NAME = "seekbar_non_active_track";
    private static final String THUMB_KEY_NAME = "seekbar_thumb";

    private static final int THUMB_SHAPE_CORNER_RADIUS = 10;
    public static final long TRACK_HEIGHT_IN_PX = 8L;
}