/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.semantics.SemanticsNode
import com.ft.sdk.sessionreplay.compose.internal.data.SemanticsWireframe
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter

internal class TabSemanticsNodeMapper(
    colorStringFormatter: ColorStringFormatter,
    semanticsUtils: SemanticsUtils = SemanticsUtils()
) : AbstractSemanticsNodeMapper(colorStringFormatter, semanticsUtils) {

    override fun map(
        semanticsNode: SemanticsNode,
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): SemanticsWireframe {
        val modifierWireframes = resolveModifierWireframes(semanticsNode)
        if (modifierWireframes.isNotEmpty()) {
            return SemanticsWireframe(
                wireframes = modifierWireframes,
                uiContext = parentContext
            )
        }
        val globalBounds = resolveBounds(semanticsNode)
        val shapeStyle =
            ShapeStyle(parentContext.parentContentColor, null, null)
        val wireframe = ShapeWireframe(
            semanticsNode.id.toLong(),
            globalBounds.x,
            globalBounds.y,
            globalBounds.width,
            globalBounds.height,
            null,
            shapeStyle,
            null
        )
        return SemanticsWireframe(
            wireframes = listOf(wireframe),
            uiContext = parentContext
        )
    }
}
