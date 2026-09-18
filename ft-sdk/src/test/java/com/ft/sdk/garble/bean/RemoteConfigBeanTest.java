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

    @Test public void allSixRemoteFieldsUsePercentagesAndCallbackOverridesSurviveCache() throws Exception {
        String[][] fields = {
            {"rumSampleRate", "sessionSamplingRate", "RumSampleRate", "SessionSamplingRate"},
            {"logSampleRate", "logSamplingRate", "LogSampleRate", "LogSamplingRate"},
            {"traceSampleRate", "traceSamplingRate", "TraceSampleRate", "TraceSamplingRate"},
            {"rumSessionOnErrorSampleRate", "sessionOnErrorSamplingRate", "RumSessionOnErrorSampleRate", "SessionOnErrorSamplingRate"},
            {"sessionReplaySampleRate", "sessionReplaySamplingRate", "SessionReplaySampleRate", "SessionReplaySamplingRate"},
            {"sessionReplayOnErrorSampleRate", "sessionReplayOnErrorSamplingRate", "SessionReplayOnErrorSampleRate", "SessionReplayOnErrorSamplingRate"}
        };
        for (String[] field : fields) {
            for (double value : new double[]{0, .1, 1, 20, 99.9, 100}) {
                JSONObject content = new JSONObject().put(field[0], 1).put(field[1], value);
                RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(new JSONObject().put("content", content).toString());
                Assert.assertEquals(value / 100, ((Float)RemoteConfigBean.class.getMethod("get" + field[2]).invoke(bean)).doubleValue(), .000001);
                RemoteConfigBean cached = RemoteConfigBean.buildFromConfigJson(bean.toJsonString());
                Assert.assertEquals(value, ((Float)RemoteConfigBean.class.getMethod("get" + field[3]).invoke(cached)).doubleValue(), .00002);
            }
            for (Object value : new Object[]{-1, 100.0000001, JSONObject.NULL, "20", true}) {
                JSONObject content = new JSONObject().put(field[0], 1).put(field[1], value);
                RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson(new JSONObject().put("content", content).toString());
                Assert.assertNull(RemoteConfigBean.class.getMethod("get" + field[3]).invoke(bean));
            }
            RemoteConfigBean bean = RemoteConfigBean.buildFromConfigJson("{\"content\":{}}");
            RemoteConfigBean.class.getMethod("set" + field[3], float.class).invoke(bean, .1f);
            RemoteConfigBean.class.getMethod("set" + field[2], Float.class).invoke(bean, 1f);
            RemoteConfigBean cached = RemoteConfigBean.buildFromConfigJson(bean.toJsonString());
            Assert.assertEquals(.1f, (Float)RemoteConfigBean.class.getMethod("get" + field[3]).invoke(cached), .00001f);
            for (float bad : new float[]{-1f, 101f, Float.NaN, Float.POSITIVE_INFINITY}) {
                try {
                    RemoteConfigBean.class.getMethod("set" + field[3], float.class).invoke(bean, bad);
                    Assert.fail("invalid callback percentage accepted");
                } catch (java.lang.reflect.InvocationTargetException e) {
                    Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
                }
            }
            // Legacy callbacks still override fetched values as an independent source.
            bean = RemoteConfigBean.buildFromConfigJson(new JSONObject().put("content", new JSONObject().put(field[1], .1)).toString());
            RemoteConfigBean.class.getMethod("set" + field[2], Float.class).invoke(bean, 1f);
            cached = RemoteConfigBean.buildFromConfigJson(bean.toJsonString());
            Assert.assertEquals(100f, (Float)RemoteConfigBean.class.getMethod("get" + field[3]).invoke(cached), .00001f);
        }
    }
}
