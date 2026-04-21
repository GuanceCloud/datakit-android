package com.ft.sdk.sessionreplay.utils

import android.graphics.Bitmap
import android.graphics.Path
import com.ft.sdk.sessionreplay.ImagePrivacy
import com.ft.sdk.sessionreplay.model.ShapeBorder
import com.ft.sdk.sessionreplay.model.ShapeStyle
import com.ft.sdk.sessionreplay.model.Wireframe
import com.ft.sdk.sessionreplay.model.WireframeClip

@Suppress("FunctionName")
fun ImageWireframeHelper.createImageWireframeByPath(
    id: Long,
    globalBounds: GlobalBounds,
    path: Path,
    strokeColor: Int,
    strokeWidth: Int,
    targetWidth: Int,
    targetHeight: Int,
    density: Float,
    isContextualImage: Boolean,
    imagePrivacy: ImagePrivacy,
    asyncJobStatusCallback: AsyncJobStatusCallback,
    clipping: WireframeClip? = null,
    shapeStyle: ShapeStyle? = null,
    border: ShapeBorder? = null,
    customResourceIdCacheKey: String? = null
): Wireframe? {
    return createImageWireframeByPath(
        id,
        globalBounds,
        path,
        strokeColor,
        strokeWidth,
        targetWidth,
        targetHeight,
        density,
        isContextualImage,
        imagePrivacy,
        asyncJobStatusCallback,
        clipping,
        shapeStyle,
        border,
        customResourceIdCacheKey
    )
}

@Suppress("FunctionName")
fun ImageWireframeHelper.createImageWireframeByBitmap(
    id: Long,
    globalBounds: GlobalBounds,
    bitmap: Bitmap,
    density: Float,
    isContextualImage: Boolean,
    imagePrivacy: ImagePrivacy,
    asyncJobStatusCallback: AsyncJobStatusCallback,
    clipping: WireframeClip? = null,
    shapeStyle: ShapeStyle? = null,
    border: ShapeBorder? = null
): Wireframe? {
    return createImageWireframeByBitmap(
        id,
        globalBounds,
        bitmap,
        density,
        isContextualImage,
        imagePrivacy,
        asyncJobStatusCallback,
        clipping,
        shapeStyle,
        border
    )
}
