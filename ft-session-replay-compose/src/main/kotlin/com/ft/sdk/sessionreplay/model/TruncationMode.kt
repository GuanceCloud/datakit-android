package com.ft.sdk.sessionreplay.model

/**
 * Defines how text is truncated when it cannot fit inside the recorded bounds.
 */
enum class TruncationMode {
    /** Clip overflowing text without adding ellipsis. */
    CLIP,

    /** Keep the beginning of the text and truncate the tail. */
    TAIL,

    /** Keep the end of the text and truncate the head. */
    HEAD,

    /** Keep both ends of the text and truncate the middle. */
    MIDDLE
}
