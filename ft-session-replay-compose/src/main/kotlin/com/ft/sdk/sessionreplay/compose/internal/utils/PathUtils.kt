/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.compose.internal.utils

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import com.ft.sdk.sessionreplay.utils.InternalLogger

internal class PathUtils(
    private val logger: InternalLogger
) {
    internal fun asAndroidPathSafe(path: Path): android.graphics.Path? {
        return try {
            @Suppress("UnsafeThirdPartyFunctionCall") // handling UnsupportedOperationException
            path.asAndroidPath()
        } catch (e: UnsupportedOperationException) {
            logger.w("PathUtils", PATH_CONVERSION_ERROR, e)
            null
        }
    }

    internal companion object {
        internal const val PATH_CONVERSION_ERROR = "Failed to convert Compose Path to Android Path"
    }
}
