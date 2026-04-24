/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import android.graphics.Rect
import android.util.Log
import androidx.annotation.UiThread
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.ft.sdk.sessionreplay.compose.internal.data.UiContext
import com.ft.sdk.sessionreplay.compose.internal.utils.SemanticsUtils
import com.ft.sdk.sessionreplay.compose.internal.utils.withinComposeBenchmarkSpan
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager
import com.ft.sdk.sessionreplay.recorder.InteropViewCallback
import com.ft.sdk.sessionreplay.recorder.MappingContext
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter

internal class RootSemanticsNodeMapper(
    private val colorStringFormatter: ColorStringFormatter,
    private val semanticsUtils: SemanticsUtils = SemanticsUtils(),
    private val semanticsNodeMapper: Map<Role, SemanticsNodeMapper> = mapOf(
        Role.RadioButton to RadioButtonSemanticsNodeMapper(colorStringFormatter, semanticsUtils),
        Role.Tab to TabSemanticsNodeMapper(colorStringFormatter, semanticsUtils),
        Role.Button to ButtonSemanticsNodeMapper(colorStringFormatter, semanticsUtils),
        Role.Image to ImageSemanticsNodeMapper(colorStringFormatter, semanticsUtils),
        Role.Checkbox to CheckboxSemanticsNodeMapper(colorStringFormatter, semanticsUtils),
        Role.Switch to SwitchSemanticsNodeMapper(colorStringFormatter, semanticsUtils)
    ),
    // Text doesn't have a role in semantics, so it should be a fallback mapper.
    private val textSemanticsNodeMapper: TextSemanticsNodeMapper = TextSemanticsNodeMapper(
        colorStringFormatter
    ),
    private val textFieldSemanticsNodeMapper: TextFieldSemanticsNodeMapper = TextFieldSemanticsNodeMapper(
        colorStringFormatter
    ),
    private val containerSemanticsNodeMapper: ContainerSemanticsNodeMapper = ContainerSemanticsNodeMapper(
        colorStringFormatter,
        semanticsUtils
    ),
    private val composeHiddenMapper: ComposeHiddenMapper = ComposeHiddenMapper(
        colorStringFormatter,
        semanticsUtils
    ),
    private val sliderSemanticsNodeMapper: SliderSemanticsNodeMapper = SliderSemanticsNodeMapper(
        colorStringFormatter,
        semanticsUtils
    )
) {
    private var hasLoggedInteropGetter = false
    private var hasLoggedInteropFieldFallback = false
    private var hasLoggedInteropMissing = false

    @UiThread
    internal fun createComposeWireframes(
        semanticsNode: SemanticsNode,
        density: Float,
        mappingContext: MappingContext,
        asyncJobStatusCallback: AsyncJobStatusCallback
    ): List<Wireframe> {
        val wireframes = mutableListOf<Wireframe>()
        withinComposeBenchmarkSpan(ROOT_NODE_SPAN_NAME, true) {
            createComposerWireframes(
                semanticsNode = semanticsNode,
                wireframes = wireframes,
                touchPrivacyManager = mappingContext.touchPrivacyManager,
                parentUiContext = UiContext(
                    parentContentColor = null,
                    density = density,
                    imagePrivacy = mappingContext.imagePrivacy,
                    textAndInputPrivacy = mappingContext.textAndInputPrivacy,
                    imageWireframeHelper = mappingContext.imageWireframeHelper
                ),
                asyncJobStatusCallback = asyncJobStatusCallback,
                mappingContext = mappingContext
            )
        }
        return wireframes
    }

    @Suppress("ReturnCount")
    @UiThread
    private fun createComposerWireframes(
        semanticsNode: SemanticsNode,
        touchPrivacyManager: TouchPrivacyManager,
        wireframes: MutableList<Wireframe>,
        parentUiContext: UiContext,
        asyncJobStatusCallback: AsyncJobStatusCallback,
        mappingContext: MappingContext
    ) {
        if (semanticsUtils.isNodePositionUnavailable(semanticsNode)) {
            // If we cant get the real position, we skip the node.
            // This is to prevent visual artifacts of leaf nodes at 0,0 in the replays.
            return
        }

        // If Hidden node is detected, add placeholder wireframe and return
        if (semanticsUtils.isNodeHidden(semanticsNode)) {
            composeHiddenMapper.map(
                semanticsNode,
                parentUiContext,
                asyncJobStatusCallback
            )?.let {
                wireframes.addAll(it.wireframes)
            }
            return
        }

        val interopView = semanticsUtils.getInteropView(semanticsNode)

        if (interopView != null) {
            wireframes.addAll(
                resolveInteropViewCallback(mappingContext)?.map(interopView, mappingContext).orEmpty()
            )
            return
        }

        val mapper = getSemanticsNodeMapper(semanticsNode)
        updateTouchOverrideAreas(
            touchPrivacyManager = touchPrivacyManager,
            semanticsNode = semanticsNode
        )
        withinComposeBenchmarkSpan(
            mapper::class.java.simpleName,
            isContainer = mapper is ContainerSemanticsNodeMapper
        ) {
            val semanticsWireframe = mapper.map(
                semanticsNode = semanticsNode,
                parentContext = parentUiContext,
                asyncJobStatusCallback = asyncJobStatusCallback
            )
            var currentUiContext = parentUiContext
            semanticsWireframe?.let {
                wireframes.addAll(it.wireframes)
                currentUiContext = it.uiContext ?: currentUiContext
            }
            val children = semanticsNode.children
            children.forEach {
                createComposerWireframes(
                    semanticsNode = it,
                    touchPrivacyManager = touchPrivacyManager,
                    wireframes = wireframes,
                    parentUiContext = currentUiContext,
                    asyncJobStatusCallback = asyncJobStatusCallback,
                    mappingContext = mappingContext
                )
            }
        }
    }

    private fun getSemanticsNodeMapper(
        semanticsNode: SemanticsNode
    ): SemanticsNodeMapper {
        val role = semanticsNode.config.getOrNull(SemanticsProperties.Role)
        return semanticsNodeMapper[role] ?: when {
            isTextFieldNode(semanticsNode) -> textFieldSemanticsNodeMapper
            isTextNode(semanticsNode) -> textSemanticsNodeMapper
            isSliderNode(semanticsNode) -> sliderSemanticsNodeMapper
            else -> containerSemanticsNodeMapper
        }
    }

    private fun isTextNode(semanticsNode: SemanticsNode): Boolean {
        // Some text semantics nodes don't have an explicit `Role` but the text exists in the config
        return semanticsNode.config.getOrNull(SemanticsProperties.Text)?.isNotEmpty() == true
    }

    private fun isTextFieldNode(semanticsNode: SemanticsNode): Boolean {
        return semanticsNode.config.contains(SemanticsActions.SetText)
    }

    private fun isSliderNode(semanticsNode: SemanticsNode): Boolean {
        return semanticsUtils.getProgressBarRangeInfo(semanticsNode) != null
    }

    private fun resolveInteropViewCallback(mappingContext: MappingContext): InteropViewCallback? {
        val contextClass = mappingContext.javaClass
        val getterCallback = runCatching {
            contextClass.getMethod(INTEROP_VIEW_CALLBACK_GETTER)
                .invoke(mappingContext) as? InteropViewCallback
        }.getOrNull()

        if (getterCallback != null) {
            logInteropCallbackResolved("getter")
            return getterCallback
        }

        val fieldCallback = runCatching {
            val field = contextClass.getDeclaredField(INTEROP_VIEW_CALLBACK_FIELD)
            field.isAccessible = true
            field.get(mappingContext) as? InteropViewCallback
        }.getOrNull()

        if (fieldCallback != null) {
            logInteropCallbackResolved("field fallback")
            return fieldCallback
        }

        logInteropCallbackMissing()
        return null
    }

    private fun logInteropCallbackResolved(strategy: String) {
        when (strategy) {
            "getter" -> {
                if (!hasLoggedInteropGetter) {
                    Log.d(LOG_TAG, "Compose AndroidView interop callback resolved by getter")
                    hasLoggedInteropGetter = true
                }
            }
            else -> {
                if (!hasLoggedInteropFieldFallback) {
                    Log.d(LOG_TAG, "Compose AndroidView interop callback resolved by field fallback")
                    hasLoggedInteropFieldFallback = true
                }
            }
        }
    }

    private fun logInteropCallbackMissing() {
        if (!hasLoggedInteropMissing) {
            Log.w(LOG_TAG, "Compose AndroidView interop callback is unavailable; skip interop mapping")
            hasLoggedInteropMissing = true
        }
    }

    @UiThread
    private fun updateTouchOverrideAreas(
        semanticsNode: SemanticsNode,
        touchPrivacyManager: TouchPrivacyManager
    ) {
        semanticsUtils.getTouchPrivacyOverride(semanticsNode)?.let { touchPrivacy ->
            val rectF = semanticsNode.boundsInRoot.toAndroidRectF()
            val viewArea = Rect(
                rectF.left.toInt(),
                rectF.top.toInt(),
                rectF.right.toInt(),
                rectF.bottom.toInt()
            )
            touchPrivacyManager.addTouchOverrideArea(viewArea, touchPrivacy)
        }
    }

    companion object {
        private const val ROOT_NODE_SPAN_NAME = "RootNode"
        private const val LOG_TAG = "FTSRComposeInterop"
        private const val INTEROP_VIEW_CALLBACK_GETTER = "getInteropViewCallback"
        private const val INTEROP_VIEW_CALLBACK_FIELD = "interopViewCallback"
    }
}
