package com.ft.sdk.sessionreplay;

/**
 * Defines the Session Replay privacy policy when recording text and input fields.
 * @see TextAndInputPrivacy#MASK_SENSITIVE_INPUTS
 * @see TextAndInputPrivacy#MASK_ALL_INPUTS
 * @see TextAndInputPrivacy#MASK_ALL
 */
public enum TextAndInputPrivacy implements PrivacyLevel {

    /**
     * All text and inputs considered sensitive will be masked.
     * Sensitive text includes passwords, emails, and phone numbers.
     */
    MASK_SENSITIVE_INPUTS,

    /**
     * All inputs will be masked.
     */
    MASK_ALL_INPUTS,

    MASK_NONE, //zzq

    /**
     * All text and inputs will be masked.
     */
    MASK_ALL;

}