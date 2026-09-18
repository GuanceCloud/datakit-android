package com.ft.sdk.garble.bean;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class RemoteConfigBeanTest {

    @org.junit.Before
    public void disableAndroidLogging() {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
    }

    @Test
    public void parsesAndSerializesWebViewLogSwitch() throws Exception {
        RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(
                "{\"content\":{\"logEnableWebViewLog\":true}}");

        Assert.assertTrue(bean.isValid());
        Assert.assertEquals(Boolean.TRUE, bean.getLogEnableWebViewLog());

        JSONObject serialized = new JSONObject(bean.toJsonString());
        Assert.assertTrue(serialized.getJSONObject("content")
                .getBoolean(RemoteConfigBean.KEY_LOG_ENABLE_WEB_VIEW_LOG));
    }

    @Test
    public void newSamplingKeysWinAndSurviveCacheRoundTrip() {
        RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(
                "{\"content\":{\"rumSampleRate\":1,\"sessionSamplingRate\":0," +
                "\"logSampleRate\":1,\"logSamplingRate\":0.1," +
                "\"traceSampleRate\":1,\"traceSamplingRate\":20}}");
        Assert.assertEquals(0f, bean.getRumSampleRate(), 0f);
        Assert.assertEquals(.001f, bean.getLogSampleRate(), .0000001f);
        Assert.assertEquals(.2f, bean.getTraceSampleRate(), 0f);
        RemoteConfigBean restored = RemoteConfigBean.buildFromConfigJson(bean.toJsonString());
        Assert.assertEquals(0f, restored.getSessionSamplingRate(), 0f);
        Assert.assertEquals(.1f, restored.getLogSamplingRate(), .00001f);
        Assert.assertEquals(20f, restored.getTraceSamplingRate(), .00001f);
    }

    @Test
    public void invalidNewSamplingKeyNeverFallsBackToLegacyValue() {
        for (String value : new String[]{"-1", "100.0000001", "1e100", "null", "\"20\"", "true"}) {
            RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(
                    "{\"content\":{\"rumSampleRate\":1,\"sessionSamplingRate\":" + value +
                    ",\"logSampleRate\":1,\"logSamplingRate\":" + value +
                    ",\"traceSampleRate\":1,\"traceSamplingRate\":" + value + "}}");
            Assert.assertNull(bean.getRumSampleRate());
            Assert.assertNull(bean.getLogSampleRate());
            Assert.assertNull(bean.getTraceSampleRate());
            Assert.assertNull(RemoteConfigBean.buildFromConfigJson(bean.toJsonString()).getRumSampleRate());
        }
    }

    @Test
    public void callbackOverrideIsNotUndoneByCachedNewKey() {
        RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(
                "{\"content\":{\"sessionSamplingRate\":20,\"logSamplingRate\":20,\"traceSamplingRate\":20}}");
        bean.setRumSampleRate(1f);
        bean.setLogSampleRate(1f);
        bean.setTraceSampleRate(1f);
        RemoteConfigBean restored = RemoteConfigBean.buildFromConfigJson(bean.toJsonString());
        Assert.assertEquals(1f, restored.getRumSampleRate(), 0f);
        Assert.assertEquals(1f, restored.getLogSampleRate(), 0f);
        Assert.assertEquals(1f, restored.getTraceSampleRate(), 0f);
    }
}
