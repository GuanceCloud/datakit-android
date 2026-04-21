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
import com.ft.sdk.sessionreplay.utils.InternalLogger
import com.ft.sdk.sessionreplay.ImagePrivacy
import com.ft.sdk.sessionreplay.TextAndInputPrivacy
import com.ft.sdk.sessionreplay.compose.internal.data.SemanticsWireframe
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.ColorUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.PathUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils.Companion.DEFAULT_COLOR_BLACK
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils.Companion.DEFAULT_COLOR_WHITE
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter
import com.ft.sdk.sessionreplay.utils.GlobalBounds
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger

internal class CheckboxSemanticsNodeMapper(
    colorStringFormatter: ColorStringFormatter,
    private val semanticsUtils: SemanticsUtils = SemanticsUtils(),
    private val colorUtils: ColorUtils = ColorUtils(),
    private val logger: InternalLogger = NoOpInternalLogger(),
    private val pathUtils: PathUtils = PathUtils(logger)
) : AbstractSemanticsNodeMapper(colorStringFormatter, semanticsUtils) {

    override fun map(
        semanticsNode: SemanticsNode,
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): SemanticsWireframe {
        val globalBounds = resolveBounds(semanticsNode)

        val checkableWireframes = if (isCheckboxMasked(parentContext)) {
            listOf(
                resolveMaskedCheckable(
                    semanticsNode = semanticsNode,
                    globalBounds = globalBounds
                )
            )
        } else {
            createCheckboxWireframes(
                parentContext = parentContext,
                asyncJobStatusCallback = asyncJobStatusCallback,
                semanticsNode = semanticsNode,
                globalBounds = globalBounds,
                currentIndex = 0
            )
        }

        return SemanticsWireframe(
            uiContext = null,
            wireframes = checkableWireframes
        )
    }

    private fun isCheckboxMasked(parentContext: UiContext): Boolean =
        parentContext.textAndInputPrivacy != TextAndInputPrivacy.MASK_SENSITIVE_INPUTS

    private fun resolveMaskedCheckable(
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds
    ): Wireframe {
        // TODO RUM-5118: Decide how to display masked checkbox, Currently use old unchecked shape wireframe,
        return createUncheckedState(
            semanticsNode = semanticsNode,
            globalBounds = globalBounds,
            backgroundColor = DEFAULT_COLOR_WHITE,
            borderColor = DEFAULT_COLOR_BLACK,
            currentIndex = 0
        )
    }

    @Suppress("LongMethod")
    private fun createCheckboxWireframes(
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback,
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds,
        currentIndex: Int
    ): List<Wireframe> {
        val borderColor = resolveBorderColor(semanticsNode)
        val rawFillColor = semanticsUtils.resolveCheckboxFillColor(semanticsNode)
        val rawCheckmarkColor = semanticsUtils.resolveCheckmarkColor(semanticsNode)
        val fillColorRgba = rawFillColor?.let { convertColor(it) } ?: DEFAULT_COLOR_WHITE
        val fallbackColor = parentContext.parentContentColor?.takeIf { colorUtils.isDarkColor(it) }?.let {
            DEFAULT_COLOR_WHITE
        } ?: DEFAULT_COLOR_BLACK
        val checkmarkColorRgba = rawCheckmarkColor?.let { convertColor(it) }
            ?: fallbackColor
        val parsedFillColor = colorUtils.parseColorSafe(fillColorRgba)
        val isChecked = isCheckboxChecked(semanticsNode)
        val checkmarkColor = resolveCheckmarkColor(isChecked, checkmarkColorRgba, parsedFillColor)

        val wireframes = mutableListOf<Wireframe>()

        if (parsedFillColor != null && checkmarkColor != null) {
            val composePath = semanticsUtils
                .resolveCheckPath(semanticsNode)

            val androidPath = composePath?.let { checkPath ->
                pathUtils.asAndroidPathSafe(checkPath)
            }

            if (androidPath != null) {
                parentContext.imageWireframeHelper.createImageWireframeByPath(
                    resolveId(semanticsNode, currentIndex),
                    globalBounds,
                    androidPath,
                    checkmarkColor,
                    STROKE_WIDTH_DP.toInt(),
                    CHECKBOX_SIZE_DP,
                    CHECKBOX_SIZE_DP,
                    parentContext.density,
                    false,
                    ImagePrivacy.MASK_NONE,
                    asyncJobStatusCallback,
                    null,
                    ShapeStyle(fillColorRgba, 1f, CHECKBOX_CORNER_RADIUS),
                    ShapeBorder(borderColor, BOX_BORDER_WIDTH_DP),
                    null
                )?.let { imageWireframe ->
                    wireframes.add(imageWireframe)
                }
            }
        }

        if (wireframes.isNotEmpty()) {
            return wireframes
        }

        // if we failed to create a wireframe from the path
        return createManualCheckedWireframes(
            parentContext = parentContext,
            semanticsNode = semanticsNode,
            globalBounds = globalBounds,
            backgroundColor = fillColorRgba,
            borderColor = borderColor
        )
    }

    private fun resolveCheckmarkColor(isChecked: Boolean, checkmarkColorRgba: String, fillColor: Int?): Int? =
        if (isChecked) {
            colorUtils.parseColorSafe(checkmarkColorRgba)
        } else {
            fillColor
        }

    private fun resolveBorderColor(semanticsNode: SemanticsNode): String {
        return semanticsUtils.resolveBorderColor(semanticsNode)
            ?.let { rawColor ->
                convertColor(rawColor)
            } ?: DEFAULT_COLOR_BLACK
    }

    private fun createManualCheckedWireframes(
        parentContext: UiContext,
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds,
        backgroundColor: String,
        borderColor: String
    ): List<Wireframe> {
        val strokeColor = parentContext.parentContentColor?.takeIf { colorUtils.isDarkColor(it) }?.let {
            DEFAULT_COLOR_WHITE
        } ?: DEFAULT_COLOR_BLACK

        val wireframes = mutableListOf<Wireframe>()

        val background = createUncheckedState(
            semanticsNode = semanticsNode,
            globalBounds = globalBounds,
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            currentIndex = 0
        )

        wireframes.add(background)

        val checkmarkWidth = globalBounds.width * CHECKMARK_SIZE_FACTOR
        val checkmarkHeight = globalBounds.height * CHECKMARK_SIZE_FACTOR
        val xPos = globalBounds.x + ((globalBounds.width / 2) - (checkmarkWidth / 2))
        val yPos = globalBounds.y + ((globalBounds.height / 2) - (checkmarkHeight / 2))
        val foreground = ShapeWireframe(
            resolveId(semanticsNode, 1),
            xPos.toLong(),
            yPos.toLong(),
            checkmarkWidth.toLong(),
            checkmarkHeight.toLong(),
            null,
            ShapeStyle(strokeColor, 1f, CHECKBOX_CORNER_RADIUS),
            ShapeBorder(DEFAULT_COLOR_BLACK, BOX_BORDER_WIDTH_DP)
        )

        wireframes.add(foreground)
        return wireframes
    }

    private fun createUncheckedState(
        semanticsNode: SemanticsNode,
        globalBounds: GlobalBounds,
        backgroundColor: String,
        borderColor: String,
        currentIndex: Int
    ) = ShapeWireframe(
        resolveId(semanticsNode, currentIndex),
        globalBounds.x,
        globalBounds.y,
        CHECKBOX_SIZE_DP.toLong(),
        CHECKBOX_SIZE_DP.toLong(),
        null,
        ShapeStyle(backgroundColor, 1f, CHECKBOX_CORNER_RADIUS),
        ShapeBorder(borderColor, BOX_BORDER_WIDTH_DP)
    )

    private fun isCheckboxChecked(semanticsNode: SemanticsNode): Boolean =
        semanticsNode.config.getOrNull(SemanticsProperties.ToggleableState) == ToggleableState.On

    internal companion object {
        // when we create the checkmark manually, what % of the checkbox size should it be
        internal const val CHECKMARK_SIZE_FACTOR = 0.5

        // values from Compose Checkbox sourcecode
        internal const val BOX_BORDER_WIDTH_DP = 2L
        internal const val STROKE_WIDTH_DP = 2f
        internal const val CHECKBOX_SIZE_DP = 20
        internal const val CHECKBOX_CORNER_RADIUS = 2f
    }
}
