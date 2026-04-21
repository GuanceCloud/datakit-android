/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.compose.internal.utils

/**
 * A wrap function of [withinComposeBenchmarkSpan] dedicated to session replay span recording.
 */
internal inline fun <T : Any?> withinComposeBenchmarkSpan(
    spanName: String,
    isContainer: Boolean = false,
    block: () -> T
): T {
    return block()
}
