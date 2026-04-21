/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.SemanticsNode
import com.ft.sdk.sessionreplay.compose.internal.data.BitmapInfo
import com.ft.sdk.sessionreplay.compose.internal.data.SemanticsWireframe
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter
import com.ft.sdk.sessionreplay.utils.GlobalBounds

internal class ImageSemanticsNodeMapper(
    colorStringFormatter: ColorStringFormatter,
    private val semanticsUtils: SemanticsUtils
) : AbstractSemanticsNodeMapper(colorStringFormatter, semanticsUtils) {

    override fun map(
        semanticsNode: SemanticsNode,
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): SemanticsWireframe {
        val containerBounds = resolveBounds(semanticsNode)
        val bitmapInfo = semanticsUtils.resolveSemanticsPainter(semanticsNode)
        val containerFrames = resolveModifierWireframes(semanticsNode).toMutableList()
        val imagePrivacy =
            semanticsUtils.getImagePrivacyOverride(semanticsNode) ?: parentContext.imagePrivacy
        val imageShapeStyle = resolveModifierShapeStyle(semanticsNode)?.let {
            ShapeStyle(null, null, it.cornerRadius)
        }
        val imageWireframe = if (bitmapInfo != null) {
            val scaledImageInfo = calculateScaledImageBounds(
                containerBounds = containerBounds,
                bitmapInfo = bitmapInfo,
                density = parentContext.density
            )
            parentContext.imageWireframeHelper.createImageWireframeByBitmap(
                semanticsNode.id.toLong(),
                scaledImageInfo.bounds,
                bitmapInfo.bitmap,
                parentContext.density,
                bitmapInfo.isContextualImage,
                imagePrivacy,
                asyncJobStatusCallback,
                scaledImageInfo.clipping,
                imageShapeStyle,
                null
            )
        } else {
            null
        }

        imageWireframe?.let {
            containerFrames.add(it)
        }
        return SemanticsWireframe(
            wireframes = containerFrames,
            uiContext = null
        )
    }

    internal fun calculateScaledImageBounds(
        containerBounds: GlobalBounds,
        bitmapInfo: BitmapInfo,
        density: Float
    ): ScaledImageInfo {
        val contentScale = bitmapInfo.contentScale
        val alignment = bitmapInfo.alignment ?: Alignment.Center

        val bitmapWidthDp = bitmapInfo.bitmap.width / density
        val bitmapHeightDp = bitmapInfo.bitmap.height / density
        val containerWidthDp = containerBounds.width.toFloat()
        val containerHeightDp = containerBounds.height.toFloat()

        if (hasInvalidDimensions(bitmapWidthDp, bitmapHeightDp, containerWidthDp, containerHeightDp)) {
            return ScaledImageInfo(bounds = containerBounds, clipping = null)
        }

        val scaledSize = calculateScaledSize(
            contentScale = contentScale,
            bitmapWidthDp = bitmapWidthDp,
            bitmapHeightDp = bitmapHeightDp,
            containerWidthDp = containerWidthDp,
            containerHeightDp = containerHeightDp
        )

        val (offsetX, offsetY) = calculateAlignmentOffset(
            alignment = alignment,
            scaledWidth = scaledSize.width,
            scaledHeight = scaledSize.height,
            containerWidth = containerWidthDp,
            containerHeight = containerHeightDp
        )

        val imageBounds = GlobalBounds(
            containerBounds.x + offsetX.toLong(),
            containerBounds.y + offsetY.toLong(),
            scaledSize.width.toLong(),
            scaledSize.height.toLong()
        )

        val clipping = calculateClipping(
            containerBounds = containerBounds,
            imageBounds = imageBounds
        )

        return ScaledImageInfo(bounds = imageBounds, clipping = clipping)
    }

    private fun calculateScaledSize(
        contentScale: ContentScale?,
        bitmapWidthDp: Float,
        bitmapHeightDp: Float,
        containerWidthDp: Float,
        containerHeightDp: Float
    ): ScaledSize {
        val scaleX = containerWidthDp / bitmapWidthDp
        val scaleY = containerHeightDp / bitmapHeightDp

        return when (contentScale) {
            ContentScale.Crop -> {
                val scaleFactor = maxOf(scaleX, scaleY)
                ScaledSize(
                    width = bitmapWidthDp * scaleFactor,
                    height = bitmapHeightDp * scaleFactor
                )
            }
            ContentScale.Fit -> {
                val scaleFactor = minOf(scaleX, scaleY)
                ScaledSize(
                    width = bitmapWidthDp * scaleFactor,
                    height = bitmapHeightDp * scaleFactor
                )
            }
            ContentScale.FillHeight -> {
                ScaledSize(
                    width = bitmapWidthDp * scaleY,
                    height = containerHeightDp
                )
            }
            ContentScale.FillWidth -> {
                ScaledSize(
                    width = containerWidthDp,
                    height = bitmapHeightDp * scaleX
                )
            }
            ContentScale.Inside -> {
                if (bitmapWidthDp <= containerWidthDp && bitmapHeightDp <= containerHeightDp) {
                    ScaledSize(width = bitmapWidthDp, height = bitmapHeightDp)
                } else {
                    val scaleFactor = minOf(scaleX, scaleY)
                    ScaledSize(
                        width = bitmapWidthDp * scaleFactor,
                        height = bitmapHeightDp * scaleFactor
                    )
                }
            }
            ContentScale.None -> {
                ScaledSize(width = bitmapWidthDp, height = bitmapHeightDp)
            }
            ContentScale.FillBounds -> {
                ScaledSize(width = containerWidthDp, height = containerHeightDp)
            }
            else -> {
                val scaleFactor = minOf(scaleX, scaleY)
                ScaledSize(
                    width = bitmapWidthDp * scaleFactor,
                    height = bitmapHeightDp * scaleFactor
                )
            }
        }
    }

    private fun calculateAlignmentOffset(
        alignment: Alignment,
        scaledWidth: Float,
        scaledHeight: Float,
        containerWidth: Float,
        containerHeight: Float
    ): Pair<Float, Float> {
        val horizontalSpace = containerWidth - scaledWidth
        val verticalSpace = containerHeight - scaledHeight

        val offsetX = when (alignment) {
            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> 0f
            Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> horizontalSpace / 2f
            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> horizontalSpace
            else -> horizontalSpace / 2f
        }

        val offsetY = when (alignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> 0f
            Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd -> verticalSpace / 2f
            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> verticalSpace
            else -> verticalSpace / 2f
        }

        return Pair(offsetX, offsetY)
    }

    private fun calculateClipping(
        containerBounds: GlobalBounds,
        imageBounds: GlobalBounds
    ): WireframeClip? {
        val left = if (imageBounds.x < containerBounds.x) {
            containerBounds.x - imageBounds.x
        } else {
            0L
        }
        val top = if (imageBounds.y < containerBounds.y) {
            containerBounds.y - imageBounds.y
        } else {
            0L
        }
        val right = if (imageBounds.x + imageBounds.width > containerBounds.x + containerBounds.width) {
            (imageBounds.x + imageBounds.width) - (containerBounds.x + containerBounds.width)
        } else {
            0L
        }
        val bottom = if (imageBounds.y + imageBounds.height > containerBounds.y + containerBounds.height) {
            (imageBounds.y + imageBounds.height) - (containerBounds.y + containerBounds.height)
        } else {
            0L
        }

        val needsClipping = hasClipping(left, top, right, bottom)
        return if (needsClipping) {
            WireframeClip(left, top, right, bottom)
        } else {
            null
        }
    }

    private fun hasInvalidDimensions(
        bitmapWidth: Float,
        bitmapHeight: Float,
        containerWidth: Float,
        containerHeight: Float
    ): Boolean {
        return bitmapWidth <= 0 || bitmapHeight <= 0 || containerWidth <= 0 || containerHeight <= 0
    }

    private fun hasClipping(left: Long, top: Long, right: Long, bottom: Long): Boolean {
        return left > 0 || top > 0 || right > 0 || bottom > 0
    }

    internal data class ScaledSize(val width: Float, val height: Float)

    internal data class ScaledImageInfo(
        val bounds: GlobalBounds,
        val clipping: WireframeClip?
    )
}
