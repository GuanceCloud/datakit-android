package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.widget.Button;

import com.ft.sdk.sessionreplay.model.ShapeBorder;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class ButtonMapper extends TextViewMapper<Button> {

    private static final String BLACK_COLOR = "#000000ff";

    public ButtonMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    public List<Wireframe> map(
            Button view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        List<Wireframe> wireframes = super.map(view, mappingContext, asyncJobStatusCallback, internalLogger);
        List<Wireframe> modifiedWireframes = new ArrayList<>();
        for (Wireframe wireframe : wireframes) {
            if (wireframe instanceof TextWireframe) {
                TextWireframe textWireframe = (TextWireframe) wireframe;
                if (textWireframe.getShapeStyle() == null && textWireframe.getBorder() == null) {
                    ShapeBorder border = new ShapeBorder(BLACK_COLOR, 1);
                    textWireframe.setBorder(border);
                }
                modifiedWireframes.add(textWireframe);
            } else {
                modifiedWireframes.add(wireframe);
            }
        }
        return modifiedWireframes;
    }
}
