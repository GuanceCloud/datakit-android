package com.ft.sdk.sessionreplay.material.internal;

import static com.ft.sdk.sessionreplay.internal.recorder.mapper.ImageViewMapper.DRAWABLE_CHILD_NAME;

import android.graphics.Rect;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.resources.DefaultDrawableCopier;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChipWireframeMapper extends TextViewMapper<Chip> {

    public ChipWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    public List<Wireframe> map(
            Chip view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        List<Wireframe> wireframes = new ArrayList<>();

        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                view,
                mappingContext.getSystemInformation().getScreenDensity()
        );

        float density = mappingContext.getSystemInformation().getScreenDensity();
        Rect drawableBounds = view.getChipDrawable().getBounds();

        Wireframe backgroundWireframe = mappingContext.getImageWireframeHelper().createImageWireframeByDrawable(
                view,
                ImagePrivacy.MASK_NONE,
                0,
                viewGlobalBounds.getX() + Utils.densityNormalized(drawableBounds.left, density),
                viewGlobalBounds.getY() + Utils.densityNormalized(drawableBounds.top, density),
                view.getChipDrawable().getIntrinsicWidth(),
                view.getChipDrawable().getIntrinsicHeight(),
                false,
                view.getChipDrawable(),
                new DefaultDrawableCopier(),
                asyncJobStatusCallback,
                null,
                null,
                null,
                DRAWABLE_CHILD_NAME,
                null

        );

        if (backgroundWireframe != null) {
            wireframes.add(backgroundWireframe);
        }

        wireframes.add(super.createTextWireframe(view, mappingContext, viewGlobalBounds));
        return Collections.unmodifiableList(wireframes);
    }

}