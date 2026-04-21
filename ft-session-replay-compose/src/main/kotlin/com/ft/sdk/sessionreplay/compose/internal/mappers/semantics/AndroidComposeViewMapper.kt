/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.annotation.UiThread
import androidx.compose.ui.platform.AndroidComposeView
import com.ft.sdk.sessionreplay.utils.InternalLogger
import com.ft.sdk.sessionreplay.recorder.MappingContext
import com.ft.sdk.sessionreplay.recorder.mapper.BaseWireframeMapper
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver

internal class AndroidComposeViewMapper(
    viewIdentifierResolver: ViewIdentifierResolver,
    colorStringFormatter: ColorStringFormatter,
    viewBoundsResolver: ViewBoundsResolver,
    drawableToColorMapper: DrawableToColorMapper,
    private val rootSemanticsNodeMapper: RootSemanticsNodeMapper
) : BaseWireframeMapper<AndroidComposeView>(
    viewIdentifierResolver,
    colorStringFormatter,
    viewBoundsResolver,
    drawableToColorMapper
) {
    @UiThread
    override fun map(
        view: AndroidComposeView,
        mappingContext: MappingContext,
        asyncJobStatusCallback: AsyncJobStatusCallback,
        internalLogger: InternalLogger
    ): List<Wireframe> {
        val density =
            mappingContext.systemInformation.screenDensity.let { if (it == 0.0f) 1.0f else it }
        return rootSemanticsNodeMapper.createComposeWireframes(
            view.semanticsOwner.unmergedRootSemanticsNode,
            density,
            mappingContext,
            asyncJobStatusCallback
        )
    }
}
