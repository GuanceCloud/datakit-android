/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import com.ft.sdk.sessionreplay.TextAndInputPrivacy
import com.ft.sdk.sessionreplay.compose.internal.data.SemanticsWireframe
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.ColorUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils.Companion.DEFAULT_COLOR_BLACK
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils.Companion.DEFAULT_COLOR_WHITE
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter
import com.ft.sdk.sessionreplay.utils.GlobalBounds

internal class SwitchSemanticsNodeMapper(
    colorStringFormatter: ColorStringFormatter,
    semanticsUtils: SemanticsUtils = SemanticsUtils(),
    private val colorUtils: ColorUtils = ColorUtils()
) : AbstractSemanticsNodeMapper(colorStringFormatter, semanticsUtils) {
    override fun map(
        semanticsNode: SemanticsNode,
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): SemanticsWireframe {
        val modifierWireframes = resolveModifierWireframes(semanticsNode)
        val isSwitchOn = isSwitchOn(semanticsNode)
        val globalBounds = resolveBounds(semanticsNode)
        val isDarkBackground =
            parentContext.parentContentColor?.let { colorUtils.isDarkColor(it) } ?: false
        val switchWireframes = if (isSwitchMasked(parentContext)) {
            listOf(
                resolveMaskedWireframes(
                    semanticsNode = semanticsNode,
                    globalBounds = globalBounds,
                    wireframeIndex = 0,
                    isDarkBackground = isDarkBackground
                )
            )
        } else {
            val trackWireframe = createTrackWireframe(
                semanticsNode = semanticsNode,
                globalBounds = globalBounds,
                wireframeIndex = 0,
                isSwitchOn = isSwitchOn,
                isDarkBackground = isDarkBackground
            )

            val thumbWireframe = createThumbWireframe(
                semanticsNode = semanticsNode,
                globalBounds = globalBounds,
                wireframeIndex = 1,
                isSwitchOn = isSwitchOn,
                isDarkBackground = isDarkBackground
            )

            listOfNotNull(trackWireframe, thumbWireframe)
        }

        return SemanticsWireframe(
            uiContext = null,
            wireframes = modifierWireframes + switchWireframes
        )
    }

    private fun createTrackWireframe(
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds,
        wireframeIndex: Int,
        isSwitchOn: Boolean,
        isDarkBackground: Boolean
    ): Wireframe {
        val trackColor = if (isSwitchOn != isDarkBackground) {
            DEFAULT_COLOR_BLACK
        } else {
            DEFAULT_COLOR_WHITE
        }

        @Suppress("MagicNumber")
        return ShapeWireframe(
            resolveId(semanticsNode, wireframeIndex),
            globalBounds.x,
            globalBounds.y + (globalBounds.height / 4),
            TRACK_WIDTH_DP,
            THUMB_DIAMETER_DP.toLong() / 2,
            null,
            ShapeStyle(trackColor, null, CORNER_RADIUS_DP),
            ShapeBorder(getContentColor(isDarkBackground), BORDER_WIDTH_DP)
        )
    }

    private fun createThumbWireframe(
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds,
        wireframeIndex: Int,
        isSwitchOn: Boolean,
        isDarkBackground: Boolean
    ): Wireframe {
        val xPosition = if (!isSwitchOn) {
            globalBounds.x
        } else {
            globalBounds.x + globalBounds.width - THUMB_DIAMETER_DP
        }

        @Suppress("MagicNumber")
        val yPosition = globalBounds.y + (globalBounds.height / 4) - (THUMB_DIAMETER_DP / 4)

        val thumbColor = if (isSwitchOn != isDarkBackground) {
            DEFAULT_COLOR_BLACK
        } else {
            DEFAULT_COLOR_WHITE
        }

        return ShapeWireframe(
            resolveId(semanticsNode, wireframeIndex),
            xPosition,
            yPosition,
            THUMB_DIAMETER_DP.toLong(),
            THUMB_DIAMETER_DP.toLong(),
            null,
            ShapeStyle(thumbColor, null, CORNER_RADIUS_DP),
            ShapeBorder(getContentColor(isDarkBackground), BORDER_WIDTH_DP)
        )
    }

    private fun isSwitchOn(semanticsNode: SemanticsNode): Boolean =
        semanticsNode.config.getOrNull(SemanticsProperties.ToggleableState) == ToggleableState.On

    private fun isSwitchMasked(parentContext: UiContext): Boolean =
        parentContext.textAndInputPrivacy != TextAndInputPrivacy.MASK_SENSITIVE_INPUTS

    private fun resolveMaskedWireframes(
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds,
        wireframeIndex: Int,
        isDarkBackground: Boolean
    ): Wireframe {
        // TODO RUM-5118: Decide how to display masked, currently use empty track,
        return createTrackWireframe(
            semanticsNode = semanticsNode,
            globalBounds = globalBounds,
            wireframeIndex = wireframeIndex,
            isSwitchOn = false,
            isDarkBackground
        )
    }

    private fun getContentColor(isDarkBackground: Boolean): String {
        return if (isDarkBackground) {
            DEFAULT_COLOR_WHITE
        } else {
            DEFAULT_COLOR_BLACK
        }
    }

    internal companion object {
        const val TRACK_WIDTH_DP = 34L
        const val CORNER_RADIUS_DP = 20
        const val THUMB_DIAMETER_DP = 20
        const val BORDER_WIDTH_DP = 1L
    }
}
