package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.internal.recorder.ViewUtilsInternal;
import com.ft.sdk.sessionreplay.model.ImageWireframe;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.ShapeBorder;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class DefaultImageWireframeHelper implements ImageWireframeHelper {

    private static final String TAG = "DefaultImageWireframeHe";
    private final InternalLogger logger;
    private final ResourceResolver resourceResolver;
    private final ViewIdentifierResolver viewIdentifierResolver;
    private final ViewUtilsInternal viewUtilsInternal;
    private final ImageTypeResolver imageTypeResolver;

    public DefaultImageWireframeHelper(
            InternalLogger logger,
            ResourceResolver resourceResolver,
            ViewIdentifierResolver viewIdentifierResolver,
            ViewUtilsInternal viewUtilsInternal,
            ImageTypeResolver imageTypeResolver) {
        this.logger = logger;
        this.resourceResolver = resourceResolver;
        this.viewIdentifierResolver = viewIdentifierResolver;
        this.viewUtilsInternal = viewUtilsInternal;
        this.imageTypeResolver = imageTypeResolver;
    }

    @Override
    public Wireframe createImageWireframeByPath(
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
    ) {

        if (imagePrivacy == ImagePrivacy.MASK_ALL) {
            return createContentPlaceholderWireframe(
                    id,
                    globalBounds.getX(),
                    globalBounds.getY(),
                    targetWidth,
                    targetHeight,
                    MASK_ALL_CONTENT_LABEL,
                    clipping
            );
        }

        if (shouldMaskContextualImage(
                imagePrivacy,
                isContextualImage,
                Utils.densityNormalized(targetWidth, density),
                Utils.densityNormalized(targetHeight, density)
        )) {
            return createContentPlaceholderWireframe(
                    id,
                    globalBounds.getX(),
                    globalBounds.getY(),
                    targetWidth,
                    targetHeight,
                    MASK_CONTEXTUAL_CONTENT_LABEL,
                    clipping
            );
        }

        ImageWireframe imageWireframe = new ImageWireframe(
                id,
                globalBounds.getX(),
                globalBounds.getY(),
                (long) targetWidth,
                (long) targetHeight,
                clipping,
                shapeStyle,
                border,
                null, null, null,
                true
        );

        asyncJobStatusCallback.jobStarted();

        resourceResolver.resolveResourceIdFromPath(
                path,
                strokeColor,
                strokeWidth,
                targetWidth,
                targetHeight,
                customResourceIdCacheKey,
                new ResourceResolverCallback() {
                    @Override
                    public void onSuccess(String resourceId) {
                        populateResourceIdInWireframe(resourceId, imageWireframe);
                        asyncJobStatusCallback.jobFinished();
                    }

                    @Override
                    public void onFailure() {
                        asyncJobStatusCallback.jobFinished();
                    }
                }
        );

        return imageWireframe;
    }

    @Override
    public Wireframe createImageWireframeByBitmap(
            long id,
            GlobalBounds globalBounds,
            Bitmap bitmap,
            float density,
            boolean isContextualImage,
            ImagePrivacy imagePrivacy,
            AsyncJobStatusCallback asyncJobStatusCallback,
            WireframeClip clipping,
            ShapeStyle shapeStyle,
            ShapeBorder border) {

        if (imagePrivacy == ImagePrivacy.MASK_ALL) {
            return createContentPlaceholderWireframe(
                    id,
                    globalBounds,
                    MASK_ALL_CONTENT_LABEL
            );
        }

        if (isContextualImage && imagePrivacy == ImagePrivacy.MASK_LARGE_ONLY) {
            return createContentPlaceholderWireframe(
                    id,
                    globalBounds,
                    MASK_CONTEXTUAL_CONTENT_LABEL
            );
        }

        ImageWireframe imageWireframe = new ImageWireframe(
                id,
                globalBounds.getX(),
                globalBounds.getY(),
                globalBounds.getWidth(),
                globalBounds.getHeight(),
                clipping,
                shapeStyle,
                border,
                null, null, null, true
        );

        asyncJobStatusCallback.jobStarted();

        resourceResolver.resolveResourceIdFromBitmap(
                bitmap,
                new ResourceResolverCallback() {
                    @Override
                    public void onSuccess(String resourceId) {
                        populateResourceIdInWireframe(resourceId, imageWireframe);
                        asyncJobStatusCallback.jobFinished();
                    }

                    @Override
                    public void onFailure() {
                        asyncJobStatusCallback.jobFinished();
                    }
                }
        );

        return imageWireframe;
    }

    @Override
    public Wireframe createImageWireframeByDrawable(
            View view,
            ImagePrivacy imagePrivacy,
            int currentWireframeIndex,
            long x,
            long y,
            int width,
            int height,
            boolean usePIIPlaceholder,
            Drawable drawable,
            DrawableCopier drawableCopier,
            AsyncJobStatusCallback asyncJobStatusCallback,
            WireframeClip clipping,
            ShapeStyle shapeStyle,
            ShapeBorder border,
            String prefix,
            String customResourceIdCacheKey) {

        Long id = viewIdentifierResolver.resolveChildUniqueIdentifier(view, prefix + currentWireframeIndex);
        DrawableProperties drawableProperties = resolveDrawableProperties(view, drawable, width, height);

        if (id == null || !drawableProperties.isValid()) {
            return null;
        }

        android.content.res.Resources resources = view.getResources();

        if (resources == null) {
            logger.e(TAG,
                    String.format(RESOURCES_NULL_ERROR, view.getClass().getCanonicalName())
            );
            return null;
        }

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Context applicationContext = view.getContext().getApplicationContext();

        if (applicationContext == null) {
            logger.e(
                    TAG,
                    String.format(APPLICATION_CONTEXT_NULL_ERROR, view.getClass().getCanonicalName())
            );
            return null;
        }

        float density = displayMetrics.density;
        long drawableWidthDp = Utils.densityNormalized(drawableProperties.drawableWidth, density);
        long drawableHeightDp = Utils.densityNormalized(drawableProperties.drawableHeight, density);

        if (imagePrivacy == ImagePrivacy.MASK_ALL) {
            return createContentPlaceholderWireframe(
                    id,
                    x,
                    y,
                    drawableWidthDp,
                    drawableHeightDp,
                    MASK_ALL_CONTENT_LABEL,
                    clipping
            );
        }

        if (shouldMaskContextualImage(imagePrivacy, usePIIPlaceholder, drawable, density)) {
            return createContentPlaceholderWireframe(
                    id,
                    x,
                    y,
                    drawableWidthDp,
                    drawableHeightDp,
                    MASK_CONTEXTUAL_CONTENT_LABEL,
                    clipping
            );
        }

        ImageWireframe imageWireframe = new ImageWireframe(
                id,
                x,
                y,
                drawableWidthDp,
                drawableHeightDp,
                clipping,
                shapeStyle,
                border,
                null,
                null,
                null,
                true
        );

        asyncJobStatusCallback.jobStarted();

        resourceResolver.resolveResourceIdFromDrawable(
                resources,
                applicationContext,
                displayMetrics,
                drawableProperties.drawable,
                drawableCopier,
                width,
                height,
                customResourceIdCacheKey,
                new ResourceResolverCallback() {
                    @Override
                    public void onSuccess(String resourceId) {
                        populateResourceIdInWireframe(resourceId, imageWireframe);
                        asyncJobStatusCallback.jobFinished();
                    }

                    @Override
                    public void onFailure() {
                        asyncJobStatusCallback.jobFinished();
                    }
                }
        );

        return imageWireframe;
    }

    @Override
    public List<Wireframe> createCompoundDrawableWireframes(
            TextView textView,
            MappingContext mappingContext,
            int prevWireframeIndex,
            String customResourceIdCacheKey,
            AsyncJobStatusCallback asyncJobStatusCallback) {

        List<Wireframe> result = new ArrayList<>();
        int wireframeIndex = prevWireframeIndex;
        float density = mappingContext.getSystemInformation().getScreenDensity();

        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        for (int compoundDrawableIndex = 0; compoundDrawableIndex < compoundDrawables.length; compoundDrawableIndex++) {
            if (compoundDrawableIndex > CompoundDrawablePositions.values().length) {
                continue;
            }

            CompoundDrawablePositions compoundDrawablePosition = convertIndexToCompoundDrawablePosition(compoundDrawableIndex);
            if (compoundDrawablePosition == null) {
                continue;
            }

            Drawable drawable = compoundDrawables[compoundDrawableIndex];
            if (drawable != null) {
                GlobalBounds drawableCoordinates = viewUtilsInternal.resolveCompoundDrawableBounds(
                        textView,
                        drawable,
                        density,
                        compoundDrawablePosition
                );

                String resourceCacheKey = customResourceIdCacheKey != null ?
                        customResourceIdCacheKey + "_" + compoundDrawableIndex : null;

                Wireframe resultWireframe = createImageWireframeByDrawable(
                        textView,
                        mappingContext.getImagePrivacy(),
                        ++wireframeIndex,
                        drawableCoordinates.getX(),
                        drawableCoordinates.getY(),
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        true,
                        drawable,
                        null,
                        asyncJobStatusCallback,
                        new WireframeClip(null, null, null, null),
                        null,
                        null, null,
                        resourceCacheKey
                );

                if (resultWireframe != null) {
                    result.add(resultWireframe);
                }
            }
        }

        return result;
    }

    private DrawableProperties resolveDrawableProperties(View view, Drawable drawable, int width, int height) {
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            if (layerDrawable.getNumberOfLayers() > 0) {
                return resolveDrawableProperties(view, layerDrawable.getDrawable(0), width, height);
            }
        } else if (drawable instanceof InsetDrawable) {
            InsetDrawable insetDrawable = (InsetDrawable) drawable;
            Drawable internalDrawable = insetDrawable.getDrawable();
            if (internalDrawable != null) {
                return resolveDrawableProperties(view, internalDrawable, width, height);
            }
        }
        return new DrawableProperties(drawable, width, height);
    }

    private PlaceholderWireframe createContentPlaceholderWireframe(
            long id,
            GlobalBounds globalBounds,
            String label) {
        return new PlaceholderWireframe(
                id,
                globalBounds.getX(),
                globalBounds.getY(),
                globalBounds.getWidth(),
                globalBounds.getHeight(),
                null,
                label
        );
    }

    private PlaceholderWireframe createContentPlaceholderWireframe(
            long id,
            long x,
            long y,
            long width,
            long height,
            String label,
            WireframeClip clipping) {
        return new PlaceholderWireframe(
                id,
                x,
                y,
                width,
                height,
                clipping,
                label
        );
    }


    private CompoundDrawablePositions convertIndexToCompoundDrawablePosition(int compoundDrawableIndex) {
        switch (compoundDrawableIndex) {
            case 0:
                return CompoundDrawablePositions.LEFT;
            case 1:
                return CompoundDrawablePositions.TOP;
            case 2:
                return CompoundDrawablePositions.RIGHT;
            case 3:
                return CompoundDrawablePositions.BOTTOM;
            default:
                return null;
        }
    }

    private boolean shouldMaskContextualImage(
            ImagePrivacy imagePrivacy,
            boolean usePIIPlaceholder,
            int width,
            int height) {
        return imagePrivacy == ImagePrivacy.MASK_LARGE_ONLY &&
                usePIIPlaceholder &&
                imageTypeResolver.isPIIByDimensions(width, height);
    }

    private boolean shouldMaskContextualImage(
            ImagePrivacy imagePrivacy,
            boolean usePIIPlaceholder,
            Drawable drawable,
            float density) {
        return imagePrivacy == ImagePrivacy.MASK_LARGE_ONLY &&
                usePIIPlaceholder &&
                imageTypeResolver.isDrawablePII(drawable, density);
    }

    private void populateResourceIdInWireframe(String resourceId, ImageWireframe wireframe) {
        wireframe.setResourceId(resourceId);
        wireframe.setEmpty(false);
    }

    public enum CompoundDrawablePositions {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    }

    private static class DrawableProperties {
        private final Drawable drawable;
        private final int drawableWidth;
        private final int drawableHeight;

        public DrawableProperties(Drawable drawable, int drawableWidth, int drawableHeight) {
            this.drawable = drawable;
            this.drawableWidth = drawableWidth;
            this.drawableHeight = drawableHeight;
        }

        public boolean isValid() {
            return drawableWidth > 0 && drawableHeight > 0;
        }
    }

    @VisibleForTesting
    static final String MASK_CONTEXTUAL_CONTENT_LABEL = "Content Image";

    @VisibleForTesting
    static final String MASK_ALL_CONTENT_LABEL = "Image";

    @VisibleForTesting
    static final String APPLICATION_CONTEXT_NULL_ERROR = "Application context is null for view %s";

    @VisibleForTesting
    static final String RESOURCES_NULL_ERROR = "Resources is null for view %s";
}
