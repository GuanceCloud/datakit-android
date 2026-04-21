/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.compose.internal.utils

import android.graphics.Color
import com.ft.sdk.sessionreplay.utils.InternalLogger
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger
import java.util.Locale
import kotlin.math.pow

internal class ColorUtils(
    private val logger: InternalLogger = NoOpInternalLogger()
) {
    internal fun parseColorSafe(color: String): Int? {
        return try {
            @Suppress("UnsafeThirdPartyFunctionCall") // handling IllegalArgumentException
            Color.parseColor(color)
        } catch (e: IllegalArgumentException) {
            logger.w("ColorUtils", COLOR_PARSE_ERROR.format(Locale.US, color), e)
            null
        }
    }

    // Luma formula: L=0.2126×R+0.7152×G+0.0722×B
    @Suppress("MagicNumber")
    internal fun isDarkColor(hexColor: String): Boolean {
        return parseColorSafe(hexColor)?.let {
            val red = Color.red(it) / 255.0
            val green = Color.green(it) / 255.0
            val blue = Color.blue(it) / 255.0

            // Linearize the RGB components
            val r = linearize(red)
            val g = linearize(green)
            val b = linearize(blue)

            // Calculate luminance
            val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
            luminance <= 0.5
        } ?: false
    }

    @Suppress("MagicNumber")
    private fun linearize(channel: Double): Double {
        return if (channel <= 0.03928) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)
    }

    internal companion object {
        internal const val COLOR_PARSE_ERROR = "Failed to parse color: %s"
    }
}
