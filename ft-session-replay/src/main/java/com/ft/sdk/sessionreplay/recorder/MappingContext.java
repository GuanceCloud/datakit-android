package com.ft.sdk.sessionreplay.recorder;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;

public class MappingContext {
    private final SystemInformation systemInformation;
    private final ImageWireframeHelper imageWireframeHelper;
    private final TextAndInputPrivacy textAndInputPrivacy;
    private final ImagePrivacy imagePrivacy;
    private final TouchPrivacyManager touchPrivacyManager;
    private final InteropViewCallback interopViewCallback;
    private final boolean hasOptionSelectorParent;

    public MappingContext(SystemInformation systemInformation, ImageWireframeHelper imageWireframeHelper
            , boolean hasOptionSelectorParent,
                          TextAndInputPrivacy textAndInputPrivacy, ImagePrivacy imagePrivacy,
                          TouchPrivacyManager touchPrivacyManager, InteropViewCallback interopViewCallback) {
        this.systemInformation = systemInformation;
        this.imageWireframeHelper = imageWireframeHelper;
        this.hasOptionSelectorParent = hasOptionSelectorParent;
        this.textAndInputPrivacy = textAndInputPrivacy;
        this.imagePrivacy = imagePrivacy;
        this.touchPrivacyManager = touchPrivacyManager;
        this.interopViewCallback = interopViewCallback;
    }

    public SystemInformation getSystemInformation() {
        return systemInformation;
    }

    public ImageWireframeHelper getImageWireframeHelper() {
        return imageWireframeHelper;
    }

    public boolean isHasOptionSelectorParent() {
        return hasOptionSelectorParent;
    }

    public TextAndInputPrivacy getTextAndInputPrivacy() {
        return textAndInputPrivacy;
    }

    public ImagePrivacy getImagePrivacy() {
        return imagePrivacy;
    }

    public TouchPrivacyManager getTouchPrivacyManager() {
        return touchPrivacyManager;
    }

    public MappingContext copy(boolean hasOptionSelectorParent) {
        return new MappingContext(systemInformation,
                imageWireframeHelper, hasOptionSelectorParent, textAndInputPrivacy, imagePrivacy,
                touchPrivacyManager, interopViewCallback);
    }

    public MappingContext copy(ImagePrivacy imagePrivacy, TextAndInputPrivacy textAndInputPrivacy) {
        return new MappingContext(systemInformation, imageWireframeHelper,
                hasOptionSelectorParent, textAndInputPrivacy, imagePrivacy,
                touchPrivacyManager, interopViewCallback);
    }

}
