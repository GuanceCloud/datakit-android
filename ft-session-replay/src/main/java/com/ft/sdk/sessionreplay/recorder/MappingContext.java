package com.ft.sdk.sessionreplay.recorder;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.utils.ImageWireframeHelper;

public class MappingContext {
    private final SystemInformation systemInformation;
    private final ImageWireframeHelper imageWireframeHelper;
    private final SessionReplayPrivacy privacy;
    private final boolean hasOptionSelectorParent;

    public MappingContext(SystemInformation systemInformation, ImageWireframeHelper imageWireframeHelper, SessionReplayPrivacy privacy, boolean hasOptionSelectorParent) {
        this.systemInformation = systemInformation;
        this.imageWireframeHelper = imageWireframeHelper;
        this.privacy = privacy;
        this.hasOptionSelectorParent = hasOptionSelectorParent;
    }

    public SystemInformation getSystemInformation() {
        return systemInformation;
    }

    public ImageWireframeHelper getImageWireframeHelper() {
        return imageWireframeHelper;
    }

    public SessionReplayPrivacy getPrivacy() {
        return privacy;
    }

    public boolean isHasOptionSelectorParent() {
        return hasOptionSelectorParent;
    }

    public MappingContext copy(boolean hasOptionSelectorParent) {
        return new MappingContext(systemInformation,
                imageWireframeHelper, privacy, hasOptionSelectorParent);
    }
}
