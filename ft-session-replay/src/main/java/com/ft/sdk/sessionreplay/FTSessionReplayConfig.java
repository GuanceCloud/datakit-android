package com.ft.sdk.sessionreplay;

import androidx.annotation.FloatRange;

import com.ft.sdk.sessionreplay.internal.NoOpExtensionSupport;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.List;

/**
 *
 */
public class FTSessionReplayConfig {
    private String customEndpointUrl;
    private final ExtensionSupport DEFAULT_EXTENSIONSUPPORT = new NoOpExtensionSupport();
    private SessionReplayPrivacy privacy = SessionReplayPrivacy.MASK;
    private ImagePrivacy imagePrivacy = ImagePrivacy.MASK_ALL;
    private TouchPrivacy touchPrivacy = TouchPrivacy.HIDE;
    private TextAndInputPrivacy textAndInputPrivacy = TextAndInputPrivacy.MASK_ALL;
    private List<MapperTypeWrapper<?>> customMappers = DEFAULT_EXTENSIONSUPPORT.getCustomViewMappers();
    private List<OptionSelectorDetector> customOptionSelectorDetectors = DEFAULT_EXTENSIONSUPPORT.getOptionSelectorDetectors();
    private List<DrawableToColorMapper> customDrawableMapper = DEFAULT_EXTENSIONSUPPORT.getCustomDrawableMapper();
    @FloatRange(from = 0.0, to = 1.0)
    private float sampleRate = 1f;
    private boolean delayInit;
    private boolean fineGrainedMaskingSet = false;
    private boolean dynamicOptimizationEnabled = true;
    private SessionReplayInternalCallback internalCallback = new NoSessionReplayInternalCallback();

    private ExtensionSupport extensionSupport = new NoOpExtensionSupport();


    public FTSessionReplayConfig setSampleRate(@FloatRange(from = 0.0, to = 1.0) float sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public FTSessionReplayConfig addExtensionSupport(ExtensionSupport extensionSupport) {
        this.extensionSupport = extensionSupport;
        this.customMappers = extensionSupport.getCustomViewMappers();
        this.customOptionSelectorDetectors = extensionSupport.getOptionSelectorDetectors();
        this.customDrawableMapper = extensionSupport.getCustomDrawableMapper();
        return this;
    }

//        public Builder useCustomEndpoint(String endpoint) {
//            customEndpointUrl = endpoint;
//            return this;
//        }

    /**
     * 使用 setImagePrivacy，setTouchPrivacy，setTextAndInputPrivacy 替代
     *
     * @param privacy
     * @return
     */
    @Deprecated
    public FTSessionReplayConfig setPrivacy(SessionReplayPrivacy privacy) {
        if (fineGrainedMaskingSet) return this;
        this.privacy = privacy;
        switch (privacy) {
            case ALLOW:
                this.touchPrivacy = TouchPrivacy.SHOW;
                this.imagePrivacy = ImagePrivacy.MASK_NONE;
                this.textAndInputPrivacy = TextAndInputPrivacy.MASK_SENSITIVE_INPUTS;
                break;

            case MASK_USER_INPUT:
                this.touchPrivacy = TouchPrivacy.HIDE;
                this.imagePrivacy = ImagePrivacy.MASK_LARGE_ONLY;
                this.textAndInputPrivacy = TextAndInputPrivacy.MASK_ALL_INPUTS;
                break;

            case MASK:
                this.touchPrivacy = TouchPrivacy.HIDE;
                this.imagePrivacy = ImagePrivacy.MASK_ALL;
                this.textAndInputPrivacy = TextAndInputPrivacy.MASK_ALL;
                break;
        }
        return this;
    }

    public FTSessionReplayConfig setTextAndInputPrivacy(TextAndInputPrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.textAndInputPrivacy = privacy;
        return this;
    }

    public FTSessionReplayConfig setImagePrivacy(ImagePrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.imagePrivacy = privacy;
        return this;
    }

    public FTSessionReplayConfig setTouchPrivacy(TouchPrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.touchPrivacy = privacy;
        return this;
    }

    public ImagePrivacy getImagePrivacy() {
        return imagePrivacy;
    }

    public TouchPrivacy getTouchPrivacy() {
        return touchPrivacy;
    }

    public TextAndInputPrivacy getTextAndInputPrivacy() {
        return textAndInputPrivacy;
    }

    public String getCustomEndpointUrl() {
        return customEndpointUrl;
    }

    public SessionReplayPrivacy getPrivacy() {
        return privacy;
    }

    public List<MapperTypeWrapper<?>> getCustomMappers() {
        return customMappers;
    }

    public List<OptionSelectorDetector> getCustomOptionSelectorDetectors() {
        return customOptionSelectorDetectors;
    }

    public List<DrawableToColorMapper> getCustomDrawableMapper() {
        return customDrawableMapper;
    }

    public boolean isDelayInit() {
        return delayInit;
    }

    /**
     * 延迟初始化
     *
     * @param delayInit
     * @return
     */
    public FTSessionReplayConfig setDelayInit(boolean delayInit) {
        this.delayInit = delayInit;
        return this;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public boolean isDynamicOptimizationEnabled() {
        return dynamicOptimizationEnabled;
    }

    public SessionReplayInternalCallback getInternalCallback() {
        return internalCallback;
    }

    public FTSessionReplayConfig setDynamicOptimizationEnabled(boolean dynamicOptimizationEnabled) {
        this.dynamicOptimizationEnabled = dynamicOptimizationEnabled;
        return this;
    }

    public FTSessionReplayConfig setInternalCallback(SessionReplayInternalCallback internalCallback) {
        this.internalCallback = internalCallback;
        return this;

    }
}
