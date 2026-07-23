/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.ft.sdk.sessionreplay.compose.internal.mappers.semantics
import com.ft.sdk.sessionreplay.model.*

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign

internal data class TextLayoutInfo(
    val text: String,
    val color: ULong,
    val fontSize: Long,
    val fontFamily: FontFamily?,
    val textAlign: TextAlign? = TextAlign.Start,
    val textOverflow: TruncationMode? = null
)
