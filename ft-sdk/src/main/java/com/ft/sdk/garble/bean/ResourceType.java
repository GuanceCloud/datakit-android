package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Resource type enumeration
 * Represents different types of resources in RUM (Real User Monitoring)
 *
 * @author Brandon
 */
public enum ResourceType {
    /**
     * Image type resource (e.g., image/png, image/jpeg)
     */
    IMAGE("image"),

    /**
     * Media type resource (video or audio)
     */
    MEDIA("media"),

    /**
     * Font type resource
     */
    FONT("font"),

    /**
     * CSS stylesheet resource
     */
    CSS("css"),

    /**
     * JavaScript resource
     */
    JS("js"),

    /**
     * Native type resource (default for Android native network requests)
     */
    NATIVE("native");


    /**
     * Resource type value, final character content displayed in line protocol
     */
    private final String value;

    ResourceType(String value) {
        this.value = value;
    }

    /**
     * Parse resource type from MIME type (Content-Type)
     * Converts string representation of MIME type into ResourceType
     *
     * @param mimeType MIME type to convert (e.g., "image/png", "text/css")
     * @return ResourceType based on MIME type, defaults to NATIVE if unknown
     */
    public static ResourceType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return NATIVE;
        }

        String lowerMimeType = mimeType.toLowerCase(Locale.US);
        int slashIndex = lowerMimeType.indexOf('/');
        if (slashIndex == -1) {
            return NATIVE;
        }

        String baseType = lowerMimeType.substring(0, slashIndex).trim();
        String subtypeWithParams = lowerMimeType.substring(slashIndex + 1);
        int semicolonIndex = subtypeWithParams.indexOf(';');
        String subtype = semicolonIndex == -1
                ? subtypeWithParams.trim()
                : subtypeWithParams.substring(0, semicolonIndex).trim();

        switch (baseType) {
            case "image":
                return IMAGE;
            case "video":
            case "audio":
                return MEDIA;
            case "font":
                return FONT;
            case "text":
                if ("css".equals(subtype)) {
                    return CSS;
                } else if ("javascript".equals(subtype) || "ecmascript".equals(subtype)) {
                    return JS;
                }
                break;
        }

        return NATIVE;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get the string value of this resource type
     *
     * @return resource type string value
     */
    public String getValue() {
        return value;
    }
}

