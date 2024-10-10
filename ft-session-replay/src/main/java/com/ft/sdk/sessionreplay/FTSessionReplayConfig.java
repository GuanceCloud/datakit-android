package com.ft.sdk.sessionreplay;

import androidx.annotation.FloatRange;

import com.ft.sdk.sessionreplay.internal.NoOpExtensionSupport;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;

import java.util.List;

public class FTSessionReplayConfig {
    private String customEndpointUrl;
    private final ExtensionSupport DEFAULT_EXTENSIONSUPPORT = new NoOpExtensionSupport();
    private SessionReplayPrivacy privacy = SessionReplayPrivacy.MASK;
    private List<MapperTypeWrapper<?>> customMappers = DEFAULT_EXTENSIONSUPPORT.getCustomViewMappers();
    private List<OptionSelectorDetector> customOptionSelectorDetectors = DEFAULT_EXTENSIONSUPPORT.getOptionSelectorDetectors();
    @FloatRange(from = 0.0, to = 1.0)
    private float sampleRate = 1f;
    private boolean delayInit;

    private ExtensionSupport extensionSupport = new NoOpExtensionSupport();


    public FTSessionReplayConfig setSampleRate(@FloatRange(from = 0.0, to = 1.0) float sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public FTSessionReplayConfig addExtensionSupport(ExtensionSupport extensionSupport) {
        this.extensionSupport = extensionSupport;
        this.customMappers = extensionSupport.getCustomViewMappers();
        this.customOptionSelectorDetectors = extensionSupport.getOptionSelectorDetectors();
        return this;
    }

//        public Builder useCustomEndpoint(String endpoint) {
//            customEndpointUrl = endpoint;
//            return this;
//        }

    public FTSessionReplayConfig setPrivacy(SessionReplayPrivacy privacy) {
        this.privacy = privacy;
        return this;
    }

    private List<MapperTypeWrapper<?>> customMappers() {
        return extensionSupport.getCustomViewMappers();
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

    public boolean isDelayInit() {
        return delayInit;
    }

    /**
     * 延迟初始化
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
}
