package com.ft.sdk.sessionreplay;

import androidx.annotation.FloatRange;

import com.ft.sdk.sessionreplay.internal.NoOpExtensionSupport;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.List;

/**
 * Configuration object used to enable and customize Session Replay recording.
 * <p>
 * Create an instance, configure the sampling and privacy options, then pass it to
 * the SDK initialization API.
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
    private float sessionReplayOnErrorSampleRate = 0f;
    private boolean delayInit;
    private String[] rumLinkKeys = new String[]{};
    private boolean fineGrainedMaskingSet = false;
    private boolean dynamicOptimizationEnabled = true;
    private SessionReplayInternalCallback internalCallback = new NoSessionReplayInternalCallback();

    /**
     * Sets the Session Replay sampling rate.
     * <p>
     * The value range is [0, 1]. Use 0 to disable regular collection, and 1 to
     * collect all eligible sessions. The default value is 1.
     *
     * @param sampleRate the sampling rate to apply
     * @return the current configuration instance
     */
    public FTSessionReplayConfig setSampleRate(@FloatRange(from = 0.0, to = 1.0) float sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * Sets the sampling rate used to collect Session Replay data for sessions that
     * contain RUM errors.
     *
     * @param sessionReplayOnErrorSampleRate the error-session sampling rate to apply
     * @return the current configuration instance
     */
    public FTSessionReplayConfig setSessionReplayOnErrorSampleRate(@FloatRange(from = 0.0, to = 1.0) float sessionReplayOnErrorSampleRate) {
        this.sessionReplayOnErrorSampleRate = sessionReplayOnErrorSampleRate;
        return this;
    }

    /**
     * Adds custom Session Replay extension support, such as custom view mappers,
     * option selector detectors, or drawable color mappers.
     *
     * @param extensionSupport extension support to merge into this configuration
     * @return the current configuration instance
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
     * @param privacy legacy privacy level to apply
     * @return the current configuration instance
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
     * @param privacy text and input privacy level to apply
     * @return the current configuration instance
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
     * @param privacy image privacy level to apply
     * @return the current configuration instance
     */
    public FTSessionReplayConfig setImagePrivacy(ImagePrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.imagePrivacy = privacy;
        return this;
    }

    /**
     * {@link TouchPrivacy#SHOW} Do not screen touch data, {@link TouchPrivacy#HIDE} Screen touch data.
     * **Setting overrides `setPrivacy` configuration**
     *
     * @param privacy touch privacy level to apply
     * @return the current configuration instance
     */
    public FTSessionReplayConfig setTouchPrivacy(TouchPrivacy privacy) {
        fineGrainedMaskingSet = true;
        this.touchPrivacy = privacy;
        return this;
    }

    /**
     * Enables linking Session Replay records to additional RUM keys.
     *
     * @param rumLinkKeys RUM keys that should be attached to replay records
     * @return the current configuration instance
     */
    public FTSessionReplayConfig enableLinkRUMKeys(String[] rumLinkKeys) {
        this.rumLinkKeys = rumLinkKeys;
        return this;
    }

    /**
     * Returns the configured RUM link keys.
     */
    public String[] getRumLinkKeys() {
        return rumLinkKeys;
    }

    /**
     * Returns the configured image privacy level.
     */
    public ImagePrivacy getImagePrivacy() {
        return imagePrivacy;
    }

    /**
     * Returns the configured touch privacy level.
     */
    public TouchPrivacy getTouchPrivacy() {
        return touchPrivacy;
    }

    /**
     * Returns the configured text and input privacy level.
     */
    public TextAndInputPrivacy getTextAndInputPrivacy() {
        return textAndInputPrivacy;
    }

    /**
     * Returns the custom upload endpoint URL, when one is configured.
     */
    public String getCustomEndpointUrl() {
        return customEndpointUrl;
    }

    /**
     * Returns the legacy privacy level.
     */
    public SessionReplayPrivacy getPrivacy() {
        return privacy;
    }

    /**
     * Returns the custom view mappers configured for Session Replay.
     */
    public List<MapperTypeWrapper<?>> getCustomMappers() {
        return customMappers;
    }

    /**
     * Returns the custom option selector detectors configured for Session Replay.
     */
    public List<OptionSelectorDetector> getCustomOptionSelectorDetectors() {
        return customOptionSelectorDetectors;
    }

    /**
     * Returns the custom drawable color mappers configured for Session Replay.
     */
    public List<DrawableToColorMapper> getCustomDrawableMapper() {
        return customDrawableMapper;
    }

    /**
     * Returns whether Session Replay initialization should be delayed.
     */
    public boolean isDelayInit() {
        return delayInit;
    }

    /**
     * Sets whether Session Replay initialization should be delayed.
     *
     * @param delayInit true to delay initialization, false to initialize immediately
     * @return the current configuration instance
     */
    public FTSessionReplayConfig setDelayInit(boolean delayInit) {
        this.delayInit = delayInit;
        return this;
    }

    /**
     * Returns the regular Session Replay sampling rate.
     */
    public float getSampleRate() {
        return sampleRate;
    }

    /**
     * Returns the error-session sampling rate.
     */
    public float getSessionReplayOnErrorSampleRate() {
        return sessionReplayOnErrorSampleRate;
    }

    /**
     * Returns whether dynamic optimization is enabled.
     */
    public boolean isDynamicOptimizationEnabled() {
        return dynamicOptimizationEnabled;
    }

    /**
     * Returns the callback used to provide internal Session Replay integration data.
     */
    public SessionReplayInternalCallback getInternalCallback() {
        return internalCallback;
    }

    /**
     * Enables or disables dynamic optimization during recording.
     *
     * @param dynamicOptimizationEnabled true to enable dynamic optimization
     * @return the current configuration instance
     */
    public FTSessionReplayConfig setDynamicOptimizationEnabled(boolean dynamicOptimizationEnabled) {
        this.dynamicOptimizationEnabled = dynamicOptimizationEnabled;
        return this;
    }

    /**
     * Sets a callback used by Session Replay to retrieve integration data that is
     * not available through the standard Android lifecycle.
     *
     * @param internalCallback callback to use during recording
     * @return the current configuration instance
     */
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
