/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.semantics.SemanticsNode
import com.ft.sdk.sessionreplay.TextAndInputPrivacy
import com.ft.sdk.sessionreplay.compose.internal.data.SemanticsWireframe
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.transformCapturedText
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter

internal open class TextSemanticsNodeMapper(
    colorStringFormatter: ColorStringFormatter,
    private val semanticsUtils: SemanticsUtils = SemanticsUtils()
) : AbstractSemanticsNodeMapper(colorStringFormatter, semanticsUtils) {

    override fun map(
        semanticsNode: SemanticsNode,
        parentContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): SemanticsWireframe {
        val wireframes = mutableListOf<Wireframe>()
        val textAndInputPrivacy = semanticsUtils.getTextAndInputPrivacyOverride(semanticsNode)
            ?: parentContext.textAndInputPrivacy
        val textWireframe = resolveTextWireFrame(parentContext, semanticsNode, textAndInputPrivacy)
        val backgroundWireframes = resolveModifierWireframes(semanticsNode)
        wireframes.addAll(backgroundWireframes)
        textWireframe?.let {
            wireframes.add(it)
        }
        return SemanticsWireframe(
            wireframes = wireframes.toList(),
            parentContext.copy(textAndInputPrivacy = textAndInputPrivacy)
        )
    }

    protected fun resolveTextWireFrame(
        parentContext: UiContext,
        semanticsNode: SemanticsNode,
        textAndInputPrivacy: TextAndInputPrivacy
    ): TextWireframe? {
        val textLayoutInfo = semanticsUtils.resolveTextLayoutInfo(semanticsNode)
        val capturedText = textLayoutInfo?.text?.let {
            transformCapturedText(it, textAndInputPrivacy)
        }
        val bounds = resolveBounds(semanticsNode)
        return capturedText?.let { text ->
            TextWireframe(
                semanticsNode.id.toLong(),
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                null,
                null,
                null,
                text,
                resolveTextStyle(parentContext, textLayoutInfo),
                resolveTextAlign(textLayoutInfo)
            )
        }
    }

    protected fun resolveTextStyle(
        parentContext: UiContext,
        textLayoutInfo: TextLayoutInfo?
    ): TextStyle {
        return textLayoutInfo?.let {
            resolveTextLayoutInfoToTextStyle(parentContext, it)
        } ?: defaultTextStyle
    }
}
