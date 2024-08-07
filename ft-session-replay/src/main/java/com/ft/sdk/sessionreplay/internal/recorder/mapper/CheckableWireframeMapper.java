package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.view.View;
import android.widget.Checkable;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
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
        List<Wireframe> mainWireframes = resolveMainWireframes(view, mappingContext, asyncJobStatusCallback, internalLogger);
        List<Wireframe> checkableWireframes = null;

        if (mappingContext.getPrivacy() != SessionReplayPrivacy.ALLOW) {
            checkableWireframes = resolveMaskedCheckable(view, mappingContext);
        } else if (view.isChecked()) {
            checkableWireframes = resolveCheckedCheckable(view, mappingContext);
        } else {
            checkableWireframes = resolveNotCheckedCheckable(view, mappingContext);
        }

        if (checkableWireframes != null) {
            mainWireframes.addAll(checkableWireframes);
        }

        return mainWireframes;
    }

    @UiThread
    public abstract List<Wireframe> resolveMainWireframes(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger);

    @UiThread
    public abstract List<Wireframe> resolveMaskedCheckable(
            T view,
            MappingContext mappingContext);

    @UiThread
    public abstract List<Wireframe> resolveNotCheckedCheckable(
            T view,
            MappingContext mappingContext);

    @UiThread
    public abstract List<Wireframe> resolveCheckedCheckable(
            T view,
            MappingContext mappingContext);
}
