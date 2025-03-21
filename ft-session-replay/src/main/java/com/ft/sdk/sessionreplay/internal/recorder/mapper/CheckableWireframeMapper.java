package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.view.View;
import android.widget.Checkable;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseWireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.List;


public abstract class CheckableWireframeMapper<T extends View & Checkable> extends BaseWireframeMapper<T> {

    public CheckableWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger) {
        List<Wireframe> mainWireframes = resolveMainWireframes(
                view, mappingContext, asyncJobStatusCallback, internalLogger);

        List<Wireframe> checkableWireframes;
        if (mappingContext.getTextAndInputPrivacy() != TextAndInputPrivacy.MASK_SENSITIVE_INPUTS) {
            checkableWireframes = resolveMaskedCheckable(view, mappingContext);
        } else {
            checkableWireframes = resolveCheckable(view, mappingContext, asyncJobStatusCallback);
        }

        if (checkableWireframes != null) {
            mainWireframes.addAll(checkableWireframes);
        }

        return mainWireframes;
    }

    protected ImagePrivacy mapInputPrivacyToImagePrivacy(TextAndInputPrivacy inputPrivacy) {
        switch (inputPrivacy) {
            case MASK_SENSITIVE_INPUTS:
                return ImagePrivacy.MASK_NONE;
            case MASK_ALL_INPUTS:
            case MASK_ALL:
                return ImagePrivacy.MASK_ALL;
            default:
                throw new IllegalArgumentException("Unknown TextAndInputPrivacy: " + inputPrivacy);
        }
    }

    @UiThread
    protected abstract List<Wireframe> resolveMainWireframes(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger);

    @UiThread
    protected abstract List<Wireframe> resolveMaskedCheckable(
            T view,
            MappingContext mappingContext);

    @UiThread
    protected abstract List<Wireframe> resolveCheckable(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback);
}
