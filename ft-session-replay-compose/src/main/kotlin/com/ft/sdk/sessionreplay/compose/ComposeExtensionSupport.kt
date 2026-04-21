/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.ft.sdk.sessionreplay.compose

import androidx.compose.ui.platform.AndroidComposeView
import androidx.compose.ui.platform.ComposeView
import com.ft.sdk.sessionreplay.ExtensionSupport
import com.ft.sdk.sessionreplay.MapperTypeWrapper
import com.ft.sdk.sessionreplay.compose.internal.mappers.semantics.AndroidComposeViewMapper
import com.ft.sdk.sessionreplay.compose.internal.mappers.semantics.ComposeViewMapper
import com.ft.sdk.sessionreplay.compose.internal.mappers.semantics.RootSemanticsNodeMapper
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapperFactory
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver
import java.util.Collections

/**
 * Jetpack Compose extension support implementation to be used in the Session Replay
 * configuration.
 */
class ComposeExtensionSupport : ExtensionSupport {

    private val viewIdentifierResolver: ViewIdentifierResolver = DefaultViewIdentifierResolver.get()
    private val colorStringFormatter: ColorStringFormatter = DefaultColorStringFormatter.get()
    private val viewBoundsResolver: ViewBoundsResolver = DefaultViewBoundsResolver.get()
    private val drawableToColorMapper: DrawableToColorMapper =
        DrawableToColorMapperFactory.getDefault(Collections.emptyList())
    private val rootSemanticsNodeMapper = RootSemanticsNodeMapper(colorStringFormatter)

    override fun getCustomViewMappers(): List<MapperTypeWrapper<*>> {
        return listOf(
            MapperTypeWrapper(
                ComposeView::class.java,
                ComposeViewMapper(
                    viewIdentifierResolver,
                    colorStringFormatter,
                    viewBoundsResolver,
                    drawableToColorMapper,
                    rootSemanticsNodeMapper = rootSemanticsNodeMapper
                )
            ),
            MapperTypeWrapper(
                AndroidComposeView::class.java,
                AndroidComposeViewMapper(
                    viewIdentifierResolver,
                    colorStringFormatter,
                    viewBoundsResolver,
                    drawableToColorMapper,
                    rootSemanticsNodeMapper
                )
            )
        )
    }

    override fun getOptionSelectorDetectors(): List<OptionSelectorDetector> {
        return emptyList()
    }

    override fun getCustomDrawableMapper(): List<DrawableToColorMapper> {
        return emptyList()
    }
}
