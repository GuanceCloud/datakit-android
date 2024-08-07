package com.ft.sdk.sessionreplay.utils;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.ft.sdk.sessionreplay.model.ShapeBorder;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;

import java.util.List;

public interface ImageWireframeHelper {

    Wireframe createImageWireframe(
            View view,
            int currentWireframeIndex,
            long x,
            long y,
            int width,
            int height,
            boolean usePIIPlaceholder,
            Drawable drawable,
            AsyncJobStatusCallback asyncJobStatusCallback,
            WireframeClip clipping,
            ShapeStyle shapeStyle,
            ShapeBorder border,
            String prefix
    );

    /**
     * Creates the wireframes for the compound drawables in a [TextView].
     *
     * @param textView               The TextView for which compound drawables are to be created.
     * @param mappingContext         The mapping context.
     * @param prevWireframeIndex     The previous wireframe index.
     * @param asyncJobStatusCallback The callback for asynchronous job status.
     * @return A list of wireframes for the compound drawables.
     */
    List<Wireframe> createCompoundDrawableWireframes(
            TextView textView,
            MappingContext mappingContext,
            int prevWireframeIndex,
            AsyncJobStatusCallback asyncJobStatusCallback
    );


}
