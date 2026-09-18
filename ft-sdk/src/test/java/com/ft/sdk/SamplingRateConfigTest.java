package com.ft.sdk;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class SamplingRateConfigTest {
    @Test public void defaultsAndLegacyValuesRemainCompatible() {
        Assert.assertEquals(100f, new FTRUMConfig().getSessionSamplingRate(), 0f);
        Assert.assertEquals(100f, new FTLoggerConfig().getLogSamplingRate(), 0f);
        Assert.assertEquals(100f, new FTTraceConfig().getTraceSamplingRate(), 0f);
        Assert.assertEquals(20f, new FTRUMConfig().setSamplingRate(.2f).getSessionSamplingRate(), .0001f);
        Assert.assertEquals(20f, new FTLoggerConfig().setSamplingRate(.2f).getLogSamplingRate(), .0001f);
        Assert.assertEquals(1f, new FTTraceConfig().setSamplingRate(1f).getSamplingRate(), 0f);
    }

    @Test public void percentagesKeepFractionsAndExplicitZeroInEitherOrder() {
        for (float value : new float[]{0f, .1f, 1f, 20f, 99.9f, 100f}) {
            Assert.assertEquals(value / 100f, new FTRUMConfig().setSamplingRate(1f)
                    .setSessionSamplingRate(value).getSamplingRate(), 0f);
            Assert.assertEquals(value / 100f, new FTRUMConfig().setSessionSamplingRate(value)
                    .setSamplingRate(1f).getSamplingRate(), 0f);
            Assert.assertEquals(value / 100f, new FTLoggerConfig().setLogSamplingRate(value)
                    .setSamplingRate(1f).getSamplingRate(), 0f);
            Assert.assertEquals(value / 100f, new FTTraceConfig().setTraceSamplingRate(value)
                    .setSamplingRate(1f).getSamplingRate(), 0f);
        }
    }

    @Test public void remoteLegacyUpdatesStillOverrideNewLocalValues() {
        FTRUMConfig rum = new FTRUMConfig().setSessionSamplingRate(20f);
        FTLoggerConfig log = new FTLoggerConfig().setLogSamplingRate(20f);
        FTTraceConfig trace = new FTTraceConfig().setTraceSamplingRate(20f);
        rum.setRemoteSamplingRate(.5f);
        log.setRemoteSamplingRate(.5f);
        trace.setRemoteSamplingRate(.5f);
        Assert.assertEquals(50f, rum.getSessionSamplingRate(), 0f);
        Assert.assertEquals(50f, log.getLogSamplingRate(), 0f);
        Assert.assertEquals(50f, trace.getTraceSamplingRate(), 0f);
        rum.setRemoteSamplingRate(0f);
        Assert.assertEquals(0f, rum.getSessionSamplingRate(), 0f);
    }

    @Test public void remoteCacheMergePreservesSourcePrecedenceAndIgnoresInvalidNewKeys() throws Exception {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
        FTRemoteConfigManager manager = new FTRemoteConfigManager(60, null);
        java.lang.reflect.Field cache = FTRemoteConfigManager.class.getDeclaredField("mRemoteConfig");
        cache.setAccessible(true);
        FTRUMConfig rum = new FTRUMConfig().setSessionSamplingRate(20f);
        FTLoggerConfig log = new FTLoggerConfig().setLogSamplingRate(20f);
        FTTraceConfig trace = new FTTraceConfig().setTraceSamplingRate(20f);
        String[] payloads = {
            "{\"content\":{\"rumSampleRate\":0.5,\"logSampleRate\":0.5,\"traceSampleRate\":0.5}}",
            "{\"content\":{\"sessionSamplingRate\":0,\"logSamplingRate\":0,\"traceSamplingRate\":0}}",
            "{\"content\":{\"sessionSamplingRate\":-1,\"rumSampleRate\":1,\"logSamplingRate\":null,\"logSampleRate\":1,\"traceSamplingRate\":101,\"traceSampleRate\":1}}"
        };
        float[] expected = {50f, 0f, 0f};
        for (int i = 0; i < payloads.length; i++) {
            cache.set(manager, com.ft.sdk.garble.bean.RemoteConfigBean.buildFromConfigJson(payloads[i]));
            manager.mergeRUMConfigFromCache(rum);
            manager.mergeLogConfigFromCache(log);
            manager.mergeTraceConfigFromCache(trace);
            Assert.assertEquals(expected[i], rum.getSessionSamplingRate(), 0f);
            Assert.assertEquals(expected[i], log.getLogSamplingRate(), 0f);
            Assert.assertEquals(expected[i], trace.getTraceSamplingRate(), 0f);
        }
    }

    @Test public void invalidPercentagesDoNotChangeThePreviousValue() {
        FTRUMConfig rum = new FTRUMConfig().setSessionSamplingRate(20f);
        FTLoggerConfig log = new FTLoggerConfig().setLogSamplingRate(20f);
        FTTraceConfig trace = new FTTraceConfig().setTraceSamplingRate(20f);
        for (float value : new float[]{-1f, 101f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            Assert.assertThrows(IllegalArgumentException.class, () -> rum.setSessionSamplingRate(value));
            Assert.assertThrows(IllegalArgumentException.class, () -> log.setLogSamplingRate(value));
            Assert.assertThrows(IllegalArgumentException.class, () -> trace.setTraceSamplingRate(value));
        }
        Assert.assertEquals(20f, rum.getSessionSamplingRate(), .0001f);
        Assert.assertEquals(20f, log.getLogSamplingRate(), .0001f);
        Assert.assertEquals(20f, trace.getTraceSamplingRate(), .0001f);
    }
}
