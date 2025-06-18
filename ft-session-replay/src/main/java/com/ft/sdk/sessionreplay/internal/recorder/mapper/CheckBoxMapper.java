package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.widget.CheckBox;

import androidx.annotation.NonNull;

import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import android.graphics.drawable.Drawable;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;

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

    private Wireframe createButtonWireframe(
            CheckBox view,
            int prevIndex,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback) {
        GlobalBounds buttonBounds = resolveCheckableBounds(view, mappingContext.getSystemInformation().getScreenDensity());
        if (buttonBounds == null) return null;

        Drawable buttonDrawable = view.getButtonDrawable();
        DrawableCopier drawableCopier = (originalDrawable, resources) -> {
            Drawable newDrawable = originalDrawable.getConstantState().newDrawable(resources);
            newDrawable.setState(view.getButtonDrawable().getState());
            newDrawable.setBounds(view.getButtonDrawable().getBounds());
            if (view.getButtonTintList() != null) {
                newDrawable.setTintList(view.getButtonTintList());
            }
            return newDrawable;
        };

        return mappingContext.getImageWireframeHelper().createImageWireframeByDrawable(
                view,
                mapInputPrivacyToImagePrivacy(mappingContext.getTextAndInputPrivacy()),
                prevIndex + 1,
                buttonBounds.getX(),
                buttonBounds.getY(),
                (int) buttonBounds.getWidth(),
                (int) buttonBounds.getHeight(),
                true,
                buttonDrawable,
                drawableCopier,
                asyncJobStatusCallback,
                null,
                null,
                null, null, null
        );
    }

    // Additional methods or overrides can be added here
}
