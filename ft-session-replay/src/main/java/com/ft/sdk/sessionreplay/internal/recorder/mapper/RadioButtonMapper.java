package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.widget.RadioButton;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

public class RadioButtonMapper extends CheckableCompoundButtonMapper<RadioButton> {

    private static final int CORNER_RADIUS = 10;

    public RadioButtonMapper(
            TextViewMapper<RadioButton> textWireframeMapper,
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(textWireframeMapper, viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public ShapeStyle resolveNotCheckedShapeStyle(RadioButton view, String checkBoxColor) {
        return new ShapeStyle(
                null,
                view.getAlpha(),
                CORNER_RADIUS
        );
    }
}