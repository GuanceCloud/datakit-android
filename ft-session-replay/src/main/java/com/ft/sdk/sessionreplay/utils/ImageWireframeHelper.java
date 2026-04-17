package com.ft.sdk.sessionreplay.utils;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.model.ShapeBorder;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;

import java.util.List;

public interface ImageWireframeHelper {

    /**
     * Creates an image wireframe based on a given path.
     */
    @Nullable
    Wireframe createImageWireframeByPath(
            long id,
            @NonNull GlobalBounds globalBounds,
            @NonNull Path path,
            int strokeColor,
            int strokeWidth,
            int targetWidth,
            int targetHeight,
            float density,
            boolean isContextualImage,
            @NonNull ImagePrivacy imagePrivacy,
            @NonNull AsyncJobStatusCallback asyncJobStatusCallback,
            @Nullable WireframeClip clipping,
            @Nullable ShapeStyle shapeStyle,
            @Nullable ShapeBorder border,
            @Nullable String customResourceIdCacheKey
    );

    /**
     * Creates an image wireframe based on a given bitmap.
     */
    @Nullable
    Wireframe createImageWireframeByBitmap(
            long id,
            @NonNull GlobalBounds globalBounds,
            @NonNull Bitmap bitmap,
            float density,
            boolean isContextualImage,
            @NonNull ImagePrivacy imagePrivacy,
            @NonNull AsyncJobStatusCallback asyncJobStatusCallback,
            @Nullable WireframeClip clipping,
            @Nullable ShapeStyle shapeStyle,
            @Nullable ShapeBorder border
    );

    /**
     * Creates an image wireframe and processes the provided drawable in the background.
     */
    @Nullable
    Wireframe createImageWireframeByDrawable(
            @NonNull View view,
            @NonNull ImagePrivacy imagePrivacy,
            int currentWireframeIndex,
            long x,
            long y,
            int width,
            int height,
            boolean usePIIPlaceholder,
            @NonNull Drawable drawable,
            @NonNull DrawableCopier drawableCopier,
            @NonNull AsyncJobStatusCallback asyncJobStatusCallback,
            @Nullable WireframeClip clipping,
            @Nullable ShapeStyle shapeStyle,
            @Nullable ShapeBorder border,
            @Nullable String prefix,
            @Nullable String customResourceIdCacheKey
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
            String customResourceIdCacheKey,
            AsyncJobStatusCallback asyncJobStatusCallback
    );


}
