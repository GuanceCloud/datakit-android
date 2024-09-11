package com.ft.sdk.sessionreplay;

import androidx.annotation.FloatRange;

import com.ft.sdk.sessionreplay.internal.NoOpExtensionSupport;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;

import java.util.List;

public class SessionReplayConfiguration {
    private final String customEndpointUrl;
    private final SessionReplayPrivacy privacy;
    private final List<MapperTypeWrapper<?>> customMappers;
    private final List<OptionSelectorDetector> customOptionSelectorDetectors;
    private final float sampleRate;

    public SessionReplayConfiguration(String customEndpointUrl, SessionReplayPrivacy privacy,
                                      List<MapperTypeWrapper<?>> customMappers,
                                      List<OptionSelectorDetector> customOptionSelectorDetectors,
                                      float sampleRate) {
        this.customEndpointUrl = customEndpointUrl;
        this.privacy = privacy;
        this.customMappers = customMappers;
        this.customOptionSelectorDetectors = customOptionSelectorDetectors;
        this.sampleRate = sampleRate;
    }

    public static class Builder {
        @FloatRange(from = 0.0, to = 1.0)
        private final float sampleRate;
        private String customEndpointUrl;
        private SessionReplayPrivacy privacy = SessionReplayPrivacy.MASK;
        private ExtensionSupport extensionSupport = new NoOpExtensionSupport();

        public Builder(@FloatRange(from = 0.0, to = 1.0) float sampleRate) {
            this.sampleRate = sampleRate;
        }

        public Builder addExtensionSupport(ExtensionSupport extensionSupport) {
            this.extensionSupport = extensionSupport;
            return this;
        }

//        public Builder useCustomEndpoint(String endpoint) {
//            customEndpointUrl = endpoint;
//            return this;
//        }

        public Builder setPrivacy(SessionReplayPrivacy privacy) {
            this.privacy = privacy;
            return this;
        }

        public SessionReplayConfiguration build() {
            return new SessionReplayConfiguration(
                    customEndpointUrl,
                    privacy,
                    customMappers(),
                    extensionSupport.getOptionSelectorDetectors(),
                    sampleRate
            );
        }

        private List<MapperTypeWrapper<?>> customMappers() {
            return extensionSupport.getCustomViewMappers();
        }
    }

    String getCustomEndpointUrl() {
        return customEndpointUrl;
    }

    SessionReplayPrivacy getPrivacy() {
        return privacy;
    }

    List<MapperTypeWrapper<?>> getCustomMappers() {
        return customMappers;
    }

    public List<OptionSelectorDetector> getCustomOptionSelectorDetectors() {
        return customOptionSelectorDetectors;
    }

    public float getSampleRate() {
        return sampleRate;
    }
}
