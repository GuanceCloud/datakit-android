package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.internal.utils.ImageViewUtils;
import com.ft.sdk.sessionreplay.internal.utils.RectExt;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseAsyncBackgroundWireframeMapper;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class ImageViewMapper extends BaseAsyncBackgroundWireframeMapper<ImageView> {

    private final ImageViewUtils imageViewUtils;
    private final DrawableCopier drawableCopier;

    public ImageViewMapper(
            @NonNull ImageViewUtils imageViewUtils,
            @NonNull DrawableCopier drawableCopier,
            @NonNull ViewIdentifierResolver viewIdentifierResolver,
            @NonNull ColorStringFormatter colorStringFormatter,
            @NonNull ViewBoundsResolver viewBoundsResolver,
            @NonNull DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
        this.imageViewUtils = imageViewUtils;
        this.drawableCopier = drawableCopier;
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            @NonNull ImageView view,
            @NonNull MappingContext mappingContext,
            @NonNull AsyncJobStatusCallback asyncJobStatusCallback,
            @NonNull InternalLogger internalLogger
    ) {
        List<Wireframe> wireframes = new ArrayList<>();

        // Add background wireframes if any
        wireframes.addAll(super.map(view, mappingContext, asyncJobStatusCallback, internalLogger));

        Drawable drawable = (view.getDrawable() != null) ? view.getDrawable().getCurrent() : null;
        if (drawable == null) {
            return wireframes;
        }

        // Resolve parent and content rectangles
        Rect parentRect = imageViewUtils.resolveParentRectAbsPosition(view);
        Rect contentRect = imageViewUtils.resolveContentRectWithScaling(view, drawable);

        Resources resources = view.getResources();
        float density = resources.getDisplayMetrics().density;
        WireframeClip clipping = RectExt.toWireframeClip(imageViewUtils.calculateClipping(parentRect, contentRect, density));

        long contentXPosInDp = Utils.densityNormalized(contentRect.left, density);
        long contentYPosInDp = Utils.densityNormalized(contentRect.top, density);
        int contentWidthPx = contentRect.width();
        int contentHeightPx = contentRect.height();
        Drawable.ConstantState constantState = drawable.getConstantState();
        Drawable contentDrawable = constantState != null ? constantState.newDrawable(resources) : null;

        if (contentDrawable != null) {
            // Resolve foreground wireframe
            Wireframe imageWireframe = mappingContext.getImageWireframeHelper().createImageWireframeByDrawable(
                    view,
                    mappingContext.getImagePrivacy(),
                    wireframes.size(),
                    contentXPosInDp,
                    contentYPosInDp,
                    contentWidthPx,
                    contentHeightPx,
                    true,
                    contentDrawable,
                    drawableCopier,
                    asyncJobStatusCallback,
                    clipping,
                    null,
                    null,
                    DRAWABLE_CHILD_NAME,
                    null


            );
            if (imageWireframe != null) {
                wireframes.add(imageWireframe);
            }
        }

        return wireframes;
    }

    public static final String DRAWABLE_CHILD_NAME = "drawable";

}
