package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.widget.CheckBox;

import androidx.annotation.NonNull;

import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

public class CheckBoxMapper extends CheckableCompoundButtonMapper<CheckBox> {

    public CheckBoxMapper(
            @NonNull TextViewMapper<CheckBox> textWireframeMapper,
            @NonNull ViewIdentifierResolver viewIdentifierResolver,
            @NonNull ColorStringFormatter colorStringFormatter,
            @NonNull ViewBoundsResolver viewBoundsResolver,
            @NonNull DrawableToColorMapper drawableToColorMapper,
            InternalLogger internalLogger
    ) {
        super(textWireframeMapper, viewIdentifierResolver, colorStringFormatter, viewBoundsResolver,
                drawableToColorMapper, internalLogger);
    }

    // Additional methods or overrides can be added here
}
