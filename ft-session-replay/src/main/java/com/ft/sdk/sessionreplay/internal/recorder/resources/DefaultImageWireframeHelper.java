package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;

import com.ft.sdk.sessionreplay.internal.recorder.ViewUtilsInternal;
import com.ft.sdk.sessionreplay.model.ImageWireframe;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.ShapeBorder;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DefaultImageWireframeHelper implements ImageWireframeHelper {

    private static final String TAG = "DefaultImageWireframeHe";
    private final InternalLogger logger;
    private final ResourceResolver resourceResolver;
    private final ViewIdentifierResolver viewIdentifierResolver;
    private final ViewUtilsInternal viewUtilsInternal;
    private final ImageTypeResolver imageTypeResolver;

    @VisibleForTesting
    static final String PLACEHOLDER_CONTENT_LABEL = "Content Image";

    @VisibleForTesting
    static final String APPLICATION_CONTEXT_NULL_ERROR = "Application context is null for view %s";

    @VisibleForTesting
    static final String RESOURCES_NULL_ERROR = "Resources is null for view %s";

    public DefaultImageWireframeHelper(
            InternalLogger logger,
            ResourceResolver resourceResolver,
            ViewIdentifierResolver viewIdentifierResolver,
            ViewUtilsInternal viewUtilsInternal,
            ImageTypeResolver imageTypeResolver
    ) {
        this.logger = logger;
        this.resourceResolver = resourceResolver;
        this.viewIdentifierResolver = viewIdentifierResolver;
        this.viewUtilsInternal = viewUtilsInternal;
        this.imageTypeResolver = imageTypeResolver;
    }

    @UiThread
    @Override
    public Wireframe createImageWireframe(
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
    ) {
        Long id = viewIdentifierResolver.resolveChildUniqueIdentifier(view, prefix + currentWireframeIndex);
        DrawableProperties drawableProperties = resolveDrawableProperties(view, drawable);

        if (id == null || !drawableProperties.isValid()) return null;

        Resources resources = view.getResources();

        if (resources == null) {
            logger.e(TAG, String.format(Locale.US, RESOURCES_NULL_ERROR, view.getClass().getCanonicalName()));
            return null;
        }

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Context applicationContext = view.getContext().getApplicationContext();

        if (applicationContext == null) {
            logger.e(TAG, String.format(Locale.US, APPLICATION_CONTEXT_NULL_ERROR, view.getClass().getCanonicalName()));
            return null;
        }

        float density = displayMetrics.density;

        if (usePIIPlaceholder && imageTypeResolver.isDrawablePII(drawable, density)) {
            return createContentPlaceholderWireframe(view, id, density);
        }

        long drawableWidthDp = Math.round(width / density);
        long drawableHeightDp = Math.round(height / density);

        ImageWireframe imageWireframe =
                new ImageWireframe(
                        id,
                        x,
                        y,
                        drawableWidthDp,
                        drawableHeightDp,
                        clipping,
                        shapeStyle,
                        border,
                        null
                        , null,
                        null,
                        true
                );

        asyncJobStatusCallback.jobStarted();

        resourceResolver.resolveResourceId(
                resources,
                applicationContext,
                displayMetrics,
                drawableProperties.drawable,
                width,
                height,
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

    @UiThread
    @Override
    public List<Wireframe> createCompoundDrawableWireframes(
            TextView textView,
            MappingContext mappingContext,
            int prevWireframeIndex,
            AsyncJobStatusCallback asyncJobStatusCallback
    ) {
        List<Wireframe> result = new ArrayList<>();
        int wireframeIndex = prevWireframeIndex;
        float density = mappingContext.getSystemInformation().getScreenDensity();

        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        for (int compoundDrawableIndex = 0; compoundDrawableIndex < compoundDrawables.length; compoundDrawableIndex++) {
            CompoundDrawablePositions compoundDrawablePosition = convertIndexToCompoundDrawablePosition(compoundDrawableIndex);
            if (compoundDrawablePosition == null) return result;

            Drawable drawable = compoundDrawables[compoundDrawableIndex];
            if (drawable != null) {
                GlobalBounds drawableCoordinates = viewUtilsInternal.resolveCompoundDrawableBounds(
                        textView,
                        drawable,
                        density,
                        compoundDrawablePosition
                );

                Wireframe imageWireframe = createImageWireframe(
                        textView,
                        ++wireframeIndex,
                        drawableCoordinates.getX(),
                        drawableCoordinates.getY(),
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        true,
                        drawable,
                        asyncJobStatusCallback,
                        new WireframeClip(null, null, null, null),
                        null,
                        null,
                        null
                );

                if (imageWireframe != null) {
                    result.add(imageWireframe);
                }
            }
        }

        return result;
    }

    private DrawableProperties resolveDrawableProperties(View view, Drawable drawable) {
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            if (layerDrawable.getNumberOfLayers() > 0) {
                Drawable firstLayer = layerDrawable.getDrawable(0);
                return resolveDrawableProperties(view, firstLayer);
            } else {
                return new DrawableProperties(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
        } else if (drawable instanceof InsetDrawable) {
            Drawable internalDrawable = ((InsetDrawable) drawable).getDrawable();
            if (internalDrawable != null) {
                return resolveDrawableProperties(view, internalDrawable);
            } else {
                return new DrawableProperties(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
        } else if (drawable instanceof GradientDrawable) {
            return new DrawableProperties(drawable, view.getWidth(), view.getHeight());
        } else {
            return new DrawableProperties(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
    }

    private Wireframe createContentPlaceholderWireframe(View view, Long id, float density) {
        int[] coordinates = new int[2];
        view.getLocationOnScreen(coordinates);
        long viewX = Math.round(coordinates[0] / density);
        long viewY = Math.round(coordinates[1] / density);

        return new PlaceholderWireframe(
                id,
                viewX,
                viewY,
                Math.round(view.getWidth() / density),
                Math.round(view.getHeight() / density),
                null,
                PLACEHOLDER_CONTENT_LABEL
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

    private void populateResourceIdInWireframe(String resourceId, ImageWireframe wireframe) {
        wireframe.setResourceId(resourceId);
        wireframe.setEmpty(false);
    }

    private static class DrawableProperties {
        final Drawable drawable;
        final int drawableWidth;
        final int drawableHeight;

        DrawableProperties(Drawable drawable, int drawableWidth, int drawableHeight) {
            this.drawable = drawable;
            this.drawableWidth = drawableWidth;
            this.drawableHeight = drawableHeight;
        }

        boolean isValid() {
            return drawableWidth > 0 && drawableHeight > 0;
        }
    }

    public enum CompoundDrawablePositions {
        LEFT, TOP, RIGHT, BOTTOM
    }
}
