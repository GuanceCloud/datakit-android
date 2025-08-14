package com.ft.sdk.sessionreplay.recorder.mapper;

import android.view.ViewGroup;

import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

public abstract class BaseViewGroupMapper<T extends ViewGroup> extends BaseAsyncBackgroundWireframeMapper<T> implements TraverseAllChildrenMapper<T> {

    public BaseViewGroupMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }
}
