package com.ft.sdk;

import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class WebViewLogEventMapperTest {

    @Test
    public void mapsBrowserLogDataMapAndCustomFields() throws Exception {
        JSONObject event = new JSONObject()
                .put("date", 1_700_000_000_123L)
                .put("type", "logger")
                .put("_gc", new JSONObject()
                        .put("sdk_name", "df_web_rum_sdk")
                        .put("sdk_version", "3.3.6")
                        .put("configuration", new JSONObject()
                                .put("session_sample_rate", 100)))
                .put("application", new JSONObject().put("id", "browser-app-id"))
                .put("env", "web")
                .put("service", "browser-service")
                .put("session", new JSONObject().put("id", "browser-session"))
                .put("view", new JSONObject()
                        .put("id", "browser-view")
                        .put("url", "https://example.com/path?q=1")
                        .put("url_query", "q=1"))
                .put("device", new JSONObject()
                        .put("browser", "Chrome")
                        .put("browser_version", "126"))
                .put("user_action", new JSONObject().put("id", "browser-action"))
                .put("error", new JSONObject()
                        .put("source", "source")
                        .put("type", "TypeError")
                        .put("message", "failure")
                        .put("stack", "stack"))
                .put("message", "hello")
                .put("status", "warn")
                .put("custom_number", 42)
                .put("custom_object", new JSONObject().put("nested", true))
                .put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, false);

        LogBean bean = WebViewLogEventMapper.map(event);

        Assert.assertNotNull(bean);
        Assert.assertEquals(Constants.FT_LOG_DEFAULT_MEASUREMENT, bean.getMeasurement());
        Assert.assertEquals(1_700_000_000_123_000_000L, bean.getTimeNano());
        Assert.assertEquals(Status.WARNING.name, bean.getStatus());

        HashMap<String, Object> tags = bean.getAllTags();
        Assert.assertEquals("df_web_rum_sdk", tags.get(Constants.KEY_SDK_NAME));
        Assert.assertEquals("3.3.6", tags.get(Constants.KEY_SDK_VERSION));
        Assert.assertEquals("browser-app-id", tags.get("app_id"));
        Assert.assertEquals("browser-service", tags.get(Constants.KEY_SERVICE));
        Assert.assertEquals("browser-session", tags.get(Constants.KEY_RUM_SESSION_ID));
        Assert.assertEquals("browser-view", tags.get(Constants.KEY_RUM_VIEW_ID));
        Assert.assertEquals("browser-action", tags.get("action_id"));
        Assert.assertEquals("warning", tags.get(Constants.KEY_STATUS));
        Assert.assertEquals(true, tags.get(Constants.KEY_RUM_VIEW_IS_WEB_VIEW));

        HashMap<String, Object> fields = bean.getAllFields();
        Assert.assertEquals("hello", fields.get(Constants.KEY_MESSAGE));
        Assert.assertEquals("failure", fields.get("error_message"));
        Assert.assertEquals("stack", fields.get("error_stack"));
        Assert.assertEquals("q=1", fields.get("view_url_query"));
        Assert.assertEquals(42, fields.get("custom_number"));
        Assert.assertEquals("{\"nested\":true}", fields.get("custom_object"));
        Assert.assertTrue(String.valueOf(fields.get("_gc")).contains("sdk_version"));
        Assert.assertFalse(fields.containsKey(Constants.KEY_RUM_VIEW_IS_WEB_VIEW));
        Assert.assertFalse(fields.containsKey("service"));
    }

    @Test
    public void requiresMessageAndDefaultsStatus() throws Exception {
        Assert.assertNull(WebViewLogEventMapper.map(new JSONObject().put("status", "info")));

        LogBean bean = WebViewLogEventMapper.map(new JSONObject().put("message", 123));

        Assert.assertNotNull(bean);
        Assert.assertEquals("123", bean.getContent());
        Assert.assertEquals(Status.INFO.name, bean.getStatus());
    }

    @Test
    public void invalidDateFallsBackToNativeReceiveTime() throws Exception {
        long before = System.currentTimeMillis() * 1_000_000L;

        LogBean bean = WebViewLogEventMapper.map(new JSONObject()
                .put("message", "fallback")
                .put("date", "invalid"));

        long after = (System.currentTimeMillis() + 1_000) * 1_000_000L;
        Assert.assertNotNull(bean);
        Assert.assertTrue(bean.getTimeNano() >= before);
        Assert.assertTrue(bean.getTimeNano() <= after);
    }

    @Test
    public void preservesBrowserCustomStatus() throws Exception {
        LogBean bean = WebViewLogEventMapper.map(new JSONObject()
                .put("message", "custom")
                .put("status", "notice"));

        Assert.assertNotNull(bean);
        Assert.assertEquals("notice", bean.getStatus());
    }
}
