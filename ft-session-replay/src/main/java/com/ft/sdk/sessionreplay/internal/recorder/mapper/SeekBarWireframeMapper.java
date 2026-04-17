package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.widget.SeekBar;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.List;

public class SeekBarWireframeMapper extends ProgressBarWireframeMapper<SeekBar> {

    public SeekBarWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper, false);
    }

    @UiThread
    @Override
    protected void mapDeterminate(
            List<Wireframe> wireframes,
            SeekBar view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger,
            GlobalBounds trackBounds,
            int trackColor,
            float normalizedProgress
    ) {
        super.mapDeterminate(
                wireframes,
                view,
                mappingContext,
                asyncJobStatusCallback,
                internalLogger,
                trackBounds,
                trackColor,
                normalizedProgress
        );

        if (mappingContext.getTextAndInputPrivacy() == TextAndInputPrivacy.MASK_SENSITIVE_INPUTS) {
            float screenDensity = mappingContext.getSystemInformation().getScreenDensity();
            long trackHeight = Utils.densityNormalized(ProgressBarWireframeMapper.TRACK_HEIGHT_IN_PX, screenDensity);
            Integer thumbColor = getColor(view.getThumbTintList(), view.getDrawableState());
            if (thumbColor == null || thumbColor == 0) {
                thumbColor = getDefaultColor(view);
            }

            Wireframe thumbWireframe = buildThumbWireframe(
                    view,
                    trackBounds,
                    normalizedProgress,
                    trackHeight,
                    screenDensity,
                    thumbColor
            );
            if (thumbWireframe != null) {
                wireframes.add(thumbWireframe);
            }
        }
    }

    private Wireframe buildThumbWireframe(
            SeekBar view,
            GlobalBounds trackBounds,
            float normalizedProgress,
            long trackHeight,
            float screenDensity,
            int thumbColor
    ) {
        Long thumbId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, THUMB_KEY_NAME);
        if (thumbId == null) {
            return null;
        }
        String backgroundColor = colorStringFormatter.formatColorAndAlphaAsHexString(thumbColor, OPAQUE_ALPHA_VALUE);

        long thumbWidth = Utils.densityNormalized(view.getThumb().getBounds().width(), screenDensity);
        long thumbHeight = Utils.densityNormalized(view.getThumb().getBounds().height(), screenDensity);
        return new ShapeWireframe(
                thumbId,
                (trackBounds.getX() + (long) (trackBounds.getWidth() * normalizedProgress) - (thumbWidth / 2)),
                trackBounds.getY() + (trackHeight / 2) - (thumbHeight / 2),
                thumbWidth,
                thumbHeight,
                null,
                new ShapeStyle(
                        backgroundColor,
                        view.getAlpha(),
                        Math.max(thumbWidth / 2, thumbHeight / 2)
                ), null
        );
    }

    private static final int NIGHT_MODE_COLOR = 0xffffff; // White
    private static final int DAY_MODE_COLOR = 0; // Black
    private static final String ACTIVE_TRACK_KEY_NAME = "seekbar_active_track";
    private static final String NON_ACTIVE_TRACK_KEY_NAME = "seekbar_non_active_track";
    private static final String THUMB_KEY_NAME = "seekbar_thumb";

    private static final int THUMB_SHAPE_CORNER_RADIUS = 10;
    private static final long TRACK_HEIGHT_IN_PX = 8L;
}