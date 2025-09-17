package com.ft.sdk.sessionreplay;

import androidx.annotation.FloatRange;

import com.ft.sdk.sessionreplay.internal.NoOpExtensionSupport;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.List;

/**
 * Session Replay condition configuration
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
    private float sessionReplayOnErrorSampleRate = 1f;
    private boolean delayInit;
    private boolean fineGrainedMaskingSet = false;
    private boolean dynamicOptimizationEnabled = true;
    private SessionReplayInternalCallback internalCallback = new NoSessionReplayInternalCallback();

    /**
     * Set the collection rate, the value range is [0,1], 0 means not collected, 1 means full collection, the default value is 1.
     *
     * @param sampleRate
     * @return
     */
    public FTSessionReplayConfig setSampleRate(@FloatRange(from = 0.0, to = 1.0) float sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * @param sessionReplayOnErrorSampleRate
     * @return
     */
    public FTSessionReplayConfig setSessionReplayOnErrorSampleRate(@FloatRange(from = 0.0, to = 1.0) float sessionReplayOnErrorSampleRate) {
        this.sessionReplayOnErrorSampleRate = sessionReplayOnErrorSampleRate;
        return this;
    }

    /**
     * @param extensionSupport
     * @return
     */
    public FTSessionReplayConfig addExtensionSupport(ExtensionSupport extensionSupport) {
        this.customMappers.addAll(extensionSupport.getCustomViewMappers());
        this.customOptionSelectorDetectors.addAll(extensionSupport.getOptionSelectorDetectors());
        this.customDrawableMapper.addAll(extensionSupport.getCustomDrawableMapper());
        return this;
    }

//        public Builder useCustomEndpoint(String endpoint) {
//            customEndpointUrl = endpoint;
//            return this;
//        }

    /**
     * {@link SessionReplayPrivacy#ALLOW} Do not screen privacy data, {@link SessionReplayPrivacy#MASK} Screen all data, including text, CheckBox, RadioButton, Switch;
     * {@link SessionReplayPrivacy#MASK_USER_INPUT} (recommended) Screen part of the data, including text, CheckBox, RadioButton, Switch,
     * Default, is `SessionReplayPrivacy.MASK`.
     * <p>
     * **Deprecated, can be used compatibly, it is recommended to use `setTouchPrivacy` and `setTextAndInputPrivacy` for screening settings**
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

    /**
     * {@link TextAndInputPrivacy#MASK_SENSITIVE_INPUTS} Only screen sensitive information,
     * {@link TextAndInputPrivacy#MASK_ALL_INPUTS} Screen part of the data,
     * Including text, CheckBox, RadioButton, Switch, {@link TextAndInputPrivacy#MASK_ALL},
     * Screen all data, including text, CheckBox, RadioButton, Switch. Default {@link TextAndInputPrivacy#MASK_ALL},
     * **Setting overrides `setPrivacy` configuration**
     *
     * @param privacy
     * @return
     */
    public FTSessionReplayConfig setTextAndInputPrivacy(TextAndInputPrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.textAndInputPrivacy = privacy;
        return this;
    }


    /**
     * Set the screen image
     * {@link ImagePrivacy#MASK_ALL} Screen all
     * {@link ImagePrivacy#MASK_LARGE_ONLY} Only screen large image content
     * {@link ImagePrivacy#MASK_NONE} Do not screen
     *
     * @param privacy
     * @return
     */
    public FTSessionReplayConfig setImagePrivacy(ImagePrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.imagePrivacy = privacy;
        return this;
    }

    /**
     * {@link TouchPrivacy#SHOW`} Do not screen touch data, {@link TouchPrivacy#HIDE`} Screen touch data.
     * **Setting overrides `setPrivacy` configuration**
     *
     * @param privacy
     * @return
     */
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
     * Delay initialization
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

    public float getSessionReplayOnErrorSampleRate() {
        return sessionReplayOnErrorSampleRate;
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

    @Override
    public String toString() {
        return "FTSessionReplayConfig{" +
                "imagePrivacy=" + imagePrivacy +
                ", touchPrivacy=" + touchPrivacy +
                ", textAndInputPrivacy=" + textAndInputPrivacy +
                ", sampleRate=" + sampleRate +
                ", sessionReplayOnErrorSampleRate=" + sessionReplayOnErrorSampleRate +
                '}';
    }
}
