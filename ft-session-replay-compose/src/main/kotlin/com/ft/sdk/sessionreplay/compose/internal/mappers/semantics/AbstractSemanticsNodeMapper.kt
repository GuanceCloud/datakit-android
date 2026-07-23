/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.style.TextAlign
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.BackgroundInfo
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter
import com.ft.sdk.sessionreplay.utils.GlobalBounds
import kotlin.math.roundToInt

internal abstract class AbstractSemanticsNodeMapper(
    private val colorStringFormatter: ColorStringFormatter,
    private val semanticsUtils: SemanticsUtils = SemanticsUtils()
) : SemanticsNodeMapper {

    protected val defaultTextStyle = TextStyle(
        DEFAULT_FONT_FAMILY,
        DEFAULT_FONT_SIZE,
        DEFAULT_TEXT_COLOR
    )
    protected fun resolveId(semanticsNode: SemanticsNode, currentIndex: Int = 0): Long {
        // Use semantics node intrinsic id as the higher endian of Long type and the index of
        // the wireframe inside the node as the lower endian to generate a unique id.
        return (semanticsNode.id.toLong() shl SEMANTICS_ID_BIT_SHIFT) + currentIndex
    }

    protected fun resolveBounds(semanticsNode: SemanticsNode): GlobalBounds {
        return semanticsUtils.resolveInnerBounds(semanticsNode)
    }

    protected fun resolveModifierWireframes(
        semanticsNode: SemanticsNode
    ): List<Wireframe> {
        return semanticsUtils.resolveBackgroundInfo(semanticsNode)
            .mapIndexed { index, backgroundInfo ->
                convertBackgroundInfoToWireframes(
                    semanticsNode = semanticsNode,
                    backgroundInfo = backgroundInfo,
                    index = index
                )
            }
    }

    protected fun resolveModifierShapeStyle(
        semanticsNode: SemanticsNode
    ): ShapeStyle? {
        val backgroundColor = semanticsUtils.resolveBackgroundColor(semanticsNode)?.let { convertColor(it) }
        val cornerRadius = semanticsUtils.resolveClipCornerRadius(semanticsNode)
            ?: semanticsUtils.resolveBackgroundInfo(semanticsNode).lastOrNull()?.cornerRadius
        if (backgroundColor == null && cornerRadius == null) {
            return null
        }
        return ShapeStyle(backgroundColor, null, cornerRadius)
    }

    private fun convertBackgroundInfoToWireframes(
        semanticsNode: SemanticsNode,
        backgroundInfo: BackgroundInfo,
        index: Int
    ): Wireframe {
        val shapeStyle = ShapeStyle(
            backgroundInfo.color?.let { convertColor(it) },
            null,
            backgroundInfo.cornerRadius
        )
        return ShapeWireframe(
            resolveId(semanticsNode, index),
            backgroundInfo.globalBounds.x,
            backgroundInfo.globalBounds.y,
            backgroundInfo.globalBounds.width,
            backgroundInfo.globalBounds.height,
            null,
            shapeStyle,
            null
        )
    }

    protected fun resolveTextLayoutInfoToTextStyle(
        parentContext: UiContext,
        textLayoutInfo: TextLayoutInfo
    ): TextStyle {
        return TextStyle(
            when (val value = textLayoutInfo.fontFamily) {
                is GenericFontFamily -> value.name
                else -> DEFAULT_FONT_FAMILY
            },
            textLayoutInfo.fontSize,
            convertColor(textLayoutInfo.color.toLong()) ?: parentContext.parentContentColor
                ?: DEFAULT_TEXT_COLOR
        )
    }

    protected fun resolveTextAlign(textLayoutInfo: TextLayoutInfo): TextPosition {
        val align = when (textLayoutInfo.textAlign) {
            TextAlign.Start,
            TextAlign.Left -> Horizontal.LEFT

            TextAlign.End,
            TextAlign.Right -> Horizontal.RIGHT

            TextAlign.Justify,
            TextAlign.Center -> Horizontal.CENTER

            else -> Horizontal.LEFT
        }
        return TextPosition(
            null,
            Alignment(align, null)
        )
    }

    protected fun convertColor(color: Long): String? {
        return if (color == UNSPECIFIED_COLOR) {
            null
        } else {
            val c = Color(color shr COMPOSE_COLOR_SHIFT)
            colorStringFormatter.formatColorAndAlphaAsHexString(
                c.toArgb(),
                (c.alpha * MAX_ALPHA).roundToInt()
            )
        }
    }

    companion object {
        /** As defined in Compose's ColorSpaces. */
        private const val UNSPECIFIED_COLOR = 16L
        private const val COMPOSE_COLOR_SHIFT = 32
        private const val MAX_ALPHA = 255
        private const val SEMANTICS_ID_BIT_SHIFT = 32
        private const val DEFAULT_FONT_SIZE = 12L
        private const val DEFAULT_FONT_FAMILY = "Roboto, sans-serif"
        protected const val DEFAULT_TEXT_COLOR = "#000000FF"
    }
}
