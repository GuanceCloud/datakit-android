package com.ft.sdk.sessionreplay;

public enum TouchPrivacy implements PrivacyLevel {
    /**
     * All touch interactions will be recorded.
     */
    SHOW,

    /**
     * No touch interactions will be recorded.
     */
    HIDE;

    public boolean isSensitive() {
        return this == HIDE;
    }

    public static TouchPrivacy fromBoolean(boolean isPrivate) {
        return isPrivate ? HIDE : SHOW;
    }
}