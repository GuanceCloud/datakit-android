package com.ft.sdk.sessionreplay.recorder;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;

/**
 * Context passed to custom view mappers during a Session Replay recording pass.
 */
public class MappingContext {
    private final SystemInformation systemInformation;
    private final ImageWireframeHelper imageWireframeHelper;
    private final TextAndInputPrivacy textAndInputPrivacy;
    private final ImagePrivacy imagePrivacy;
    private final TouchPrivacyManager touchPrivacyManager;
    private final InteropViewCallback interopViewCallback;
    private final boolean hasOptionSelectorParent;

    /**
     * Creates a mapping context.
     *
     * @param systemInformation device and screen information
     * @param imageWireframeHelper helper used to create image wireframes
     * @param hasOptionSelectorParent true when the current view is inside an option selector
     * @param textAndInputPrivacy active text and input privacy level
     * @param imagePrivacy active image privacy level
     * @param touchPrivacyManager active touch privacy manager
     * @param interopViewCallback callback used to map interop views
     */
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

    /**
     * Returns device and screen information for the current recording pass.
     */
    public SystemInformation getSystemInformation() {
        return systemInformation;
    }

    /**
     * Returns the helper used to create image wireframes.
     */
    public ImageWireframeHelper getImageWireframeHelper() {
        return imageWireframeHelper;
    }

    /**
     * Returns whether the current view is inside an option selector.
     */
    public boolean isHasOptionSelectorParent() {
        return hasOptionSelectorParent;
    }

    /**
     * Returns the active text and input privacy level.
     */
    public TextAndInputPrivacy getTextAndInputPrivacy() {
        return textAndInputPrivacy;
    }

    /**
     * Returns the active image privacy level.
     */
    public ImagePrivacy getImagePrivacy() {
        return imagePrivacy;
    }

    /**
     * Returns the active touch privacy manager.
     */
    public TouchPrivacyManager getTouchPrivacyManager() {
        return touchPrivacyManager;
    }

    /**
     * Returns the callback used to map interop views.
     */
    public InteropViewCallback getInteropViewCallback() {
        return interopViewCallback;
    }

    /**
     * Returns a copy of this context with a different option selector parent flag.
     *
     * @param hasOptionSelectorParent true when the copied context should mark an option selector parent
     * @return a mapping context copy
     */
    public MappingContext copy(boolean hasOptionSelectorParent) {
        return new MappingContext(systemInformation,
                imageWireframeHelper, hasOptionSelectorParent, textAndInputPrivacy, imagePrivacy,
                touchPrivacyManager, interopViewCallback);
    }

    /**
     * Returns a copy of this context with different privacy levels.
     *
     * @param imagePrivacy image privacy level for the copy
     * @param textAndInputPrivacy text and input privacy level for the copy
     * @return a mapping context copy
     */
    public MappingContext copy(ImagePrivacy imagePrivacy, TextAndInputPrivacy textAndInputPrivacy) {
        return new MappingContext(systemInformation, imageWireframeHelper,
                hasOptionSelectorParent, textAndInputPrivacy, imagePrivacy,
                touchPrivacyManager, interopViewCallback);
    }

}
