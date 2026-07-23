/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.unit.dp
import com.ft.sdk.sessionreplay.compose.internal.data.SemanticsWireframe
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter

internal class SliderSemanticsNodeMapper(
    colorStringFormatter: ColorStringFormatter,
    private val semanticsUtils: SemanticsUtils = SemanticsUtils()
) : AbstractSemanticsNodeMapper(
    colorStringFormatter,
    semanticsUtils
) {
    override fun map(
        semanticsNode: SemanticsNode,
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): SemanticsWireframe {
        val modifierWireframes = resolveModifierWireframes(semanticsNode)
        val trackWireframe = resolveTrackWireframe(parentContext, semanticsNode, 0)
        val thumbWireframe = resolveThumbWireframe(parentContext, semanticsNode, 1)
        return SemanticsWireframe(
            modifierWireframes + listOfNotNull(trackWireframe, thumbWireframe),
            null
        )
    }

    private fun resolveThumbWireframe(
        parentContext: UiContext,
        semanticsNode: SemanticsNode,
        index: Int
    ): Wireframe? {
        val globalBounds = resolveBounds(semanticsNode)
        val progressBarRangeInfo = semanticsUtils.getProgressBarRangeInfo(semanticsNode)

        val progress = progressBarRangeInfo?.let {
            val rangeAbs = it.range.endInclusive - it.range.start
            if (rangeAbs > 0) {
                it.current / rangeAbs
            } else {
                null
            }
        }
        val thumbHeight = DEFAULT_THUMB_RADIUS.value * 2 * parentContext.density
        val yOffset = (globalBounds.height - thumbHeight).toLong() / 2
        return progress?.let {
            val xOffset = progress * globalBounds.width + globalBounds.x
            ShapeWireframe(
                resolveId(semanticsNode, index),
                xOffset.toLong(),
                globalBounds.y + yOffset,
                thumbHeight.toLong(),
                thumbHeight.toLong(),
                null,
                ShapeStyle(DEFAULT_COLOR, null, thumbHeight / 2),
                null
            )
        }
    }

    private fun resolveTrackWireframe(
        parentContext: UiContext,
        semanticsNode: SemanticsNode,
        index: Int
    ): Wireframe {
        val globalBounds = resolveBounds(semanticsNode)
        val trackHeight = DEFAULT_TRACK_HEIGHT.value * parentContext.density
        val yOffset = (globalBounds.height - trackHeight).toLong() / 2
        return ShapeWireframe(
            resolveId(semanticsNode, index),
            globalBounds.x,
            globalBounds.y + yOffset,
            globalBounds.width,
            trackHeight.toLong(),
            null,
            ShapeStyle(DEFAULT_COLOR, null, trackHeight / 2),
            null
        )
    }

    companion object {
        // TODO RUM-7467: Use contrast color of parent color
        private const val DEFAULT_COLOR = "#000000FF"
        private val DEFAULT_THUMB_RADIUS = 4.dp
        private val DEFAULT_TRACK_HEIGHT = 4.dp
    }
}
