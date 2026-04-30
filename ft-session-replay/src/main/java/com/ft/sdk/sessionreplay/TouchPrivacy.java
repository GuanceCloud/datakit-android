package com.ft.sdk.sessionreplay;

/**
 * Defines whether Session Replay records touch interactions.
 */
public enum TouchPrivacy implements PrivacyLevel {
    /**
     * All touch interactions will be recorded.
     */
    SHOW,

    /**
     * No touch interactions will be recorded.
     */
    HIDE;

    /**
     * Returns true when touch interactions should be hidden.
     */
    public boolean isSensitive() {
        return this == HIDE;
    }

    /**
     * Converts a privacy flag into a touch privacy level.
     *
     * @param isPrivate true to hide touch interactions, false to show them
     * @return the matching touch privacy level
     */
    public static TouchPrivacy fromBoolean(boolean isPrivate) {
        return isPrivate ? HIDE : SHOW;
    }
}
