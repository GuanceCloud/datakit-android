package com.ft.sdk.garble.bean;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class RemoteConfigBeanTest {

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
}
