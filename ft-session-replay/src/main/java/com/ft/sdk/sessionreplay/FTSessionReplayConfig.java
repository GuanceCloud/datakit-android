package com.ft.sdk.sessionreplay;

import androidx.annotation.FloatRange;

import com.ft.sdk.sessionreplay.internal.NoOpExtensionSupport;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.List;

/**
 * Session Replay 条件配置
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

    private ExtensionSupport extensionSupport = new NoOpExtensionSupport();

    /**
     * 设置采集率，取值范围 [0,1]，0 表示不采集，1 表示全采集，默认值为 1。
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
     * {@link SessionReplayPrivacy#ALLOW} 不进行屏蔽隐私数据, {@link SessionReplayPrivacy#MASK} 屏蔽所有数据，包括文字、CheckBox，RadioButton，Switch；
     * {@link SessionReplayPrivacy#MASK_USER_INPUT} （推荐）屏蔽用户输入的部份数据,包括输入框中文字、CheckBox，RadioButton，Switch,
     * 默认，为 `SessionReplayPrivacy.MASK`。
     * <p>
     * **即将废弃，可以兼容使用，建议优先使用 `setTouchPrivacy` 、`setTextAndInputPrivacy` 进行屏蔽设置**
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
     * {@link TextAndInputPrivacy#MASK_SENSITIVE_INPUTS} 只对密码等信息进行屏蔽,
     * {@link TextAndInputPrivacy#MASK_ALL_INPUTS} 屏蔽用户输入的部份数据，
     * 包括输入框中文字、CheckBox，RadioButton，Switch，{@link TextAndInputPrivacy#MASK_ALL}，
     * 屏蔽所有数据，包括文字、CheckBox，RadioButton，Switch。默认 {@link TextAndInputPrivacy#MASK_ALL} ，
     * **设置后覆盖 `setPrivacy` 的配置**。
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
     * 设置屏蔽的图像
     * {@link ImagePrivacy#MASK_ALL} 屏蔽所有
     * {@link ImagePrivacy#MASK_LARGE_ONLY} 只屏蔽大图片内容
     * {@link ImagePrivacy#MASK_NONE} 不进行屏蔽
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
     * {@link TouchPrivacy#SHOW`} 不进行触控数据屏蔽, {@link TouchPrivacy#HIDE`}  屏蔽触控数据。
     * **设置后覆盖 `setPrivacy` 的配置
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
