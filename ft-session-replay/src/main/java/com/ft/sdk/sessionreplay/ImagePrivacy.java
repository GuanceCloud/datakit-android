package com.ft.sdk.sessionreplay;

public enum ImagePrivacy implements PrivacyLevel {

    /**
     * All images will be recorded, including those downloaded from the Internet during app runtime.
     */
    MASK_NONE,

    /**
     * Mask images that we consider to be content images based on them being larger than 100x100 dp.
     * In the replay, such images will be replaced with placeholders with the label: Content Image.
     */
    MASK_LARGE_ONLY,

    /**
     * No images will be recorded.
     * In the replay, images will be replaced with placeholders with the label: Image.
     */
    MASK_ALL;
}