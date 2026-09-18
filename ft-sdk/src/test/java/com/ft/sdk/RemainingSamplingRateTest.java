package com.ft.sdk;

import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayFeature;
import com.ft.sdk.sessionreplay.RateBasedSampler;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

@SuppressWarnings("deprecation")
public class RemainingSamplingRateTest {
    @Before public void quietLogs() { LogUtils.setDebug(false); }

    @Test public void errorSamplingKeepsDefaultsFractionsAndSetterOrder() {
        assertEquals(0f, new FTRUMConfig().getSessionOnErrorSamplingRate(), 0f);
        assertEquals(20f, new FTRUMConfig().setSessionErrorSampleRate(.2f).getSessionOnErrorSamplingRate(), .0001f);
        for (float value : new float[]{0f, .1f, 1f, 20f, 99.9f, 100f}) {
            assertEquals(value / 100f, new FTRUMConfig().setSessionErrorSampleRate(1f)
                .setSessionOnErrorSamplingRate(value).getSessionErrorSampleRate(), 0f);
            assertEquals(value / 100f, new FTRUMConfig().setSessionOnErrorSamplingRate(value)
                .setSessionErrorSampleRate(1f).getSessionErrorSampleRate(), 0f);
        }
        FTRUMConfig config = new FTRUMConfig().setSessionOnErrorSamplingRate(20f);
        for (float invalid : new float[]{-1f, 101f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> config.setSessionOnErrorSamplingRate(invalid));
            assertEquals(20f, config.getSessionOnErrorSamplingRate(), .0001f);
        }
    }

    @Test public void remoteErrorAndReplayOverrideNewLocalSettingsThroughRealBridge() throws Exception {
        FTRemoteConfigManager manager = new FTRemoteConfigManager(60, null);
        Field cache = FTRemoteConfigManager.class.getDeclaredField("mRemoteConfig");
        cache.setAccessible(true);
        FTRUMConfig rum = new FTRUMConfig().setSessionOnErrorSamplingRate(20f);
        FTSessionReplayConfig replay = new FTSessionReplayConfig()
            .setSessionReplaySamplingRate(20f).setSessionReplayOnErrorSamplingRate(20f);
        for (String json : new String[]{
            "{\"content\":{\"rumSessionOnErrorSampleRate\":0.5,\"sessionReplaySampleRate\":0.5,\"sessionReplayOnErrorSampleRate\":0.5}}",
            "{\"content\":{\"sessionOnErrorSamplingRate\":0,\"sessionReplaySamplingRate\":0,\"sessionReplayOnErrorSamplingRate\":0}}"}) {
            RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(json);
            cache.set(manager, bean);
            manager.mergeRUMConfigFromCache(rum);
            manager.mergeSessionReplayConfigFromCache(replay);
            assertEquals(bean.getSessionOnErrorSamplingRate(), rum.getSessionOnErrorSamplingRate(), .0001f);
            assertEquals(bean.getSessionReplaySamplingRate(), replay.getSessionReplaySamplingRate(), .0001f);
            assertEquals(bean.getSessionReplayOnErrorSamplingRate(), replay.getSessionReplayOnErrorSamplingRate(), .0001f);
        }
        cache.set(manager, RemoteConfigBean.buildFromConfigJson("{\"content\":{\"sessionOnErrorSamplingRate\":-1,\"rumSessionOnErrorSampleRate\":1,\"sessionReplaySamplingRate\":null,\"sessionReplaySampleRate\":1}}"));
        manager.mergeRUMConfigFromCache(rum);
        manager.mergeSessionReplayConfigFromCache(replay);
        assertEquals(0f, rum.getSessionOnErrorSamplingRate(), 0f);
        assertEquals(0f, replay.getSessionReplaySamplingRate(), 0f);
    }

    public static class OldReplayConfig {
        float regular, error;
        public void setSampleRate(float value) { regular = value; }
        public void setSessionReplayOnErrorSampleRate(float value) { error = value; }
    }
    @Test public void oldReplayDependenciesKeepTheirProbabilityProtocol() {
        OldReplayConfig old = new OldReplayConfig();
        SessionReplayBridge.mergeConfigFromCache(old, .001f, .002f);
        assertEquals(.001f, old.regular, 0f);
        assertEquals(.002f, old.error, 0f);
    }

    @Test public void managerAndFeaturePercentageApisPreserveLegacyRuntimeUpdates() throws Exception {
        SessionReplayFeature feature = new SessionReplayFeature(null, null, null, null, null,
            new RateBasedSampler(1f), new RateBasedSampler(0f), null, null, null);
        SessionReplayManager manager = SessionReplayManager.get();
        Field field = SessionReplayManager.class.getDeclaredField("sessionReplayFeature");
        field.setAccessible(true);
        Object previous = field.get(manager);
        field.set(manager, feature);
        try {
            // Optional lifecycle hints must tolerate a missing field.
            manager.hotUpdateSamplingRates(null, null);
            manager.hotUpdateSamplingRates(0f, null);
            manager.setSessionReplaySamplingRate(.1f);
            manager.setSessionReplayOnErrorSamplingRate(.2f);
            assertEquals(.001f, feature.getSampleRate(), .00000001f);
            assertEquals(.2f, manager.getSessionReplayOnErrorSamplingRate(), .00001f);
            feature.updateSessionReplaySamplingRate(25f);
            feature.updateSessionReplayOnErrorSamplingRate(50f);
            assertEquals(25f, manager.getSessionReplaySamplingRate(), .0001f);
            assertEquals(50f, manager.getSessionReplayOnErrorSamplingRate(), .0001f);
            // Runtime remote updates retain their existing last-update semantics.
            manager.setSampleRate(0f);
            manager.setSessionReplayOnErrorSampleRate(1f);
            assertEquals(0f, manager.getSessionReplaySamplingRate(), 0f);
            assertEquals(100f, manager.getSessionReplayOnErrorSamplingRate(), 0f);
            for (float bad : new float[]{-1f, 101f, Float.NaN, Float.POSITIVE_INFINITY}) {
                assertThrows(IllegalArgumentException.class, () -> manager.setSessionReplaySamplingRate(bad));
                assertThrows(IllegalArgumentException.class, () -> manager.hotUpdateSamplingRates(bad, null));
                assertThrows(IllegalArgumentException.class, () -> manager.hotUpdateSamplingRates(null, bad));
                assertThrows(IllegalArgumentException.class, () -> manager.setSessionReplayOnErrorSamplingRate(bad));
                assertThrows(IllegalArgumentException.class, () -> feature.updateSessionReplaySamplingRate(bad));
                assertThrows(IllegalArgumentException.class, () -> feature.updateSessionReplayOnErrorSamplingRate(bad));
            }
            assertEquals(0f, manager.getSessionReplaySamplingRate(), 0f);
            assertEquals(100f, manager.getSessionReplayOnErrorSamplingRate(), 0f);
        } finally { field.set(manager, previous); }
    }
}
