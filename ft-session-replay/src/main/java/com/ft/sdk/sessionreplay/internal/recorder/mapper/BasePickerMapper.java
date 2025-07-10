package com.ft.sdk.sessionreplay.internal.recorder.mapper;


import android.os.Build;
import android.widget.NumberPicker;

import androidx.annotation.RequiresApi;

import com.ft.sdk.sessionreplay.model.Alignment;
import com.ft.sdk.sessionreplay.model.Horizontal;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.TextPosition;
import com.ft.sdk.sessionreplay.model.TextStyle;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.Vertical;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseWireframeMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

@RequiresApi(Build.VERSION_CODES.Q)

public abstract class BasePickerMapper extends BaseWireframeMapper<NumberPicker> {

    protected BasePickerMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    protected long resolveTextSize(NumberPicker view, float screenDensity) {
        return Utils.densityNormalized(view.getTextSize(), screenDensity);
    }

    protected long resolvePadding(float screenDensity) {
        return  Utils.densityNormalized(PADDING_IN_PX, screenDensity);
    }

    protected long resolveDividerPaddingStart(NumberPicker view, float screenDensity) {
        return  Utils.densityNormalized(view.getPaddingStart(), screenDensity);
    }

    protected long resolveDividerPaddingEnd(NumberPicker view, float screenDensity) {
        return Utils.densityNormalized(view.getPaddingEnd(), screenDensity);
    }

    protected long resolveDividerHeight(float screenDensity) {
        return Utils.densityNormalized(DIVIDER_HEIGHT_IN_PX, screenDensity);
    }

    protected long resolveSelectedLabelYPos(GlobalBounds viewGlobalBounds, long labelHeight) {
        return viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - labelHeight) / 2;
    }

    protected String resolveSelectedTextColor(NumberPicker view) {
        return colorStringFormatter.formatColorAndAlphaAsHexString(view.getTextColor(), OPAQUE_ALPHA_VALUE);
    }

    protected TextWireframe provideLabelWireframe(
            long id,
            long x,
            long y,
            long height,
            long width,
            String labelValue,
            double textSize,
            String textColor) {
        return new TextWireframe(
                id,
                x,
                y,
                width,
                height,
                null, null, null,
                labelValue,
                new TextStyle(FONT_FAMILY, textSize, textColor),
                new TextPosition(null, new Alignment(Horizontal.CENTER, Vertical.CENTER))
        );
    }

    protected ShapeWireframe provideDividerWireframe(
            long id,
            long x,
            long y,
            long width,
            long height,
            String color) {
        return new ShapeWireframe(
                id,
                x,
                y,
                width,
                height,
                null,
                new ShapeStyle(color, null, null), null
        );
    }

    public static final String PREV_INDEX_KEY_NAME = "numeric_picker_prev_index";
    public static final String SELECTED_INDEX_KEY_NAME = "numeric_picker_selected_index";
    public static final String NEXT_INDEX_KEY_NAME = "numeric_picker_next_index";
    public static final String DIVIDER_TOP_KEY_NAME = "numeric_picker_divider_top";
    public static final String DIVIDER_BOTTOM_KEY_NAME = "numeric_picker_divider_bottom";
    private static final long DIVIDER_HEIGHT_IN_PX = 6L;
    private static final long PADDING_IN_PX = 10L;
    private static final String FONT_FAMILY = "Roboto, sans-serif";

    private static final int OPAQUE_ALPHA_VALUE = 255;

}
