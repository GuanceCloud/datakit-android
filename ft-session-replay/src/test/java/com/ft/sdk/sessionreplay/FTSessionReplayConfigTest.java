package com.ft.sdk.sessionreplay;

import android.view.View;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FTSessionReplayConfigTest {

    @Test
    public void setPrivacy_allow_shouldApplyLegacyPreset() {
        FTSessionReplayConfig config = new FTSessionReplayConfig();

        config.setPrivacy(SessionReplayPrivacy.ALLOW);

        assertEquals(TouchPrivacy.SHOW, config.getTouchPrivacy());
        assertEquals(ImagePrivacy.MASK_NONE, config.getImagePrivacy());
        assertEquals(TextAndInputPrivacy.MASK_SENSITIVE_INPUTS, config.getTextAndInputPrivacy());
    }

    @Test
    public void setPrivacy_shouldNotOverrideFineGrainedSettings() {
        // Fine-grained privacy settings are documented to take precedence over the legacy preset.
        FTSessionReplayConfig config = new FTSessionReplayConfig()
                .setTextAndInputPrivacy(TextAndInputPrivacy.MASK_ALL_INPUTS)
                .setTouchPrivacy(TouchPrivacy.SHOW)
                .setImagePrivacy(ImagePrivacy.MASK_NONE);

        config.setPrivacy(SessionReplayPrivacy.MASK);

        assertEquals(TouchPrivacy.SHOW, config.getTouchPrivacy());
        assertEquals(ImagePrivacy.MASK_NONE, config.getImagePrivacy());
        assertEquals(TextAndInputPrivacy.MASK_ALL_INPUTS, config.getTextAndInputPrivacy());
    }

    @Test
    public void addExtensionSupport_shouldAppendAllExtensionComponents() {
        FTSessionReplayConfig config = new FTSessionReplayConfig();
        int originalMapperCount = config.getCustomMappers().size();
        int originalDetectorCount = config.getCustomOptionSelectorDetectors().size();
        int originalDrawableMapperCount = config.getCustomDrawableMapper().size();

        MapperTypeWrapper<View> mapperWrapper =
                new MapperTypeWrapper<>(View.class, new NoOpWireframeMapper());
        OptionSelectorDetector detector = new NoOpOptionSelectorDetector();
        DrawableToColorMapper drawableToColorMapper = new NoOpDrawableToColorMapper();

        config.addExtensionSupport(new TestExtensionSupport(
                Collections.<MapperTypeWrapper<?>>singletonList(mapperWrapper),
                Collections.singletonList(detector),
                Collections.singletonList(drawableToColorMapper)
        ));

        assertEquals(originalMapperCount + 1, config.getCustomMappers().size());
        assertEquals(originalDetectorCount + 1, config.getCustomOptionSelectorDetectors().size());
        assertEquals(originalDrawableMapperCount + 1, config.getCustomDrawableMapper().size());
        assertSame(mapperWrapper, config.getCustomMappers().get(config.getCustomMappers().size() - 1));
        assertSame(detector, config.getCustomOptionSelectorDetectors()
                .get(config.getCustomOptionSelectorDetectors().size() - 1));
        assertSame(drawableToColorMapper, config.getCustomDrawableMapper()
                .get(config.getCustomDrawableMapper().size() - 1));
    }

    @Test
    public void enableLinkRumKeys_shouldExposeConfiguredKeys() {
        FTSessionReplayConfig config = new FTSessionReplayConfig();
        String[] rumKeys = new String[]{"view_id", "session_id"};

        config.enableLinkRUMKeys(rumKeys);

        assertSame(rumKeys, config.getRumLinkKeys());
        assertTrue(config.toString().contains("sampleRate="));
    }

    // Minimal extension implementation used to verify list append behavior without depending on
    // any Android widget specifics.
    private static class TestExtensionSupport implements ExtensionSupport {
        private final List<MapperTypeWrapper<?>> mappers;
        private final List<OptionSelectorDetector> detectors;
        private final List<DrawableToColorMapper> drawableMappers;

        private TestExtensionSupport(
                List<MapperTypeWrapper<?>> mappers,
                List<OptionSelectorDetector> detectors,
                List<DrawableToColorMapper> drawableMappers
        ) {
            this.mappers = mappers;
            this.detectors = detectors;
            this.drawableMappers = drawableMappers;
        }

        @Override
        public List<MapperTypeWrapper<?>> getCustomViewMappers() {
            return mappers;
        }

        @Override
        public List<OptionSelectorDetector> getOptionSelectorDetectors() {
            return detectors;
        }

        @Override
        public List<DrawableToColorMapper> getCustomDrawableMapper() {
            return drawableMappers;
        }
    }

    private static class NoOpOptionSelectorDetector implements OptionSelectorDetector {
        @Override
        public boolean isOptionSelector(android.view.ViewGroup view) {
            return false;
        }
    }

    private static class NoOpDrawableToColorMapper implements DrawableToColorMapper {
        @Override
        public Integer mapDrawableToColor(android.graphics.drawable.Drawable drawable,
                                          InternalLogger internalLogger) {
            return null;
        }
    }

    private static class NoOpWireframeMapper implements WireframeMapper<View> {
        @Override
        public List<com.ft.sdk.sessionreplay.model.Wireframe> map(
                View view,
                com.ft.sdk.sessionreplay.recorder.MappingContext mappingContext,
                com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback asyncJobStatusCallback,
                InternalLogger internalLogger
        ) {
            return Collections.emptyList();
        }
    }
}
