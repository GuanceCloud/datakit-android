package com.ft.sdk;

import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class TrackLogManagerTest {

    @Test
    public void webViewLogUsesNativeSwitchAndLevelFilter() {
        FTLoggerConfig config = new FTLoggerConfig()
                .setEnableWebViewLog(true)
                .setLogLevelFilters(new String[]{"error"});
        LogBean webViewLog = new LogBean("message", 1L);
        markAsWebViewLog(webViewLog);
        webViewLog.setStatus("info");

        Assert.assertFalse(TrackLogManager.isLogEnabledForSource(config, webViewLog));

        webViewLog.setStatus("error");
        Assert.assertTrue(TrackLogManager.isLogEnabledForSource(config, webViewLog));

        config.setEnableWebViewLog(false);
        Assert.assertFalse(TrackLogManager.isLogEnabledForSource(config, webViewLog));
    }

    @Test
    public void webViewLogNeverAttachesNativeRumData() {
        FTLoggerConfig config = new FTLoggerConfig().setEnableLinkRumData(true);
        LogBean nativeLog = new LogBean("native", 1L);
        LogBean webViewLog = new LogBean("web", 1L);
        markAsWebViewLog(webViewLog);

        Assert.assertTrue(TrackLogManager.shouldAttachNativeRumData(config, nativeLog));
        Assert.assertFalse(TrackLogManager.shouldAttachNativeRumData(config, webViewLog));
    }

    @Test
    public void webViewRumLinkDataFollowsNativeLogConfig() {
        FTLoggerConfig config = new FTLoggerConfig().setEnableLinkRumData(true);
        LogBean linkedLog = newWebViewLinkedLog();

        TrackLogManager.applyWebViewRumLinkConfiguration(
                config, linkedLog, "native-app", "native-session");

        Assert.assertEquals("native-app",
                linkedLog.getTags().get(Constants.KEY_RUM_APP_ID));
        Assert.assertEquals("native-session",
                linkedLog.getTags().get(Constants.KEY_RUM_SESSION_ID));
        Assert.assertEquals("web-view",
                linkedLog.getTags().get(Constants.KEY_RUM_VIEW_ID));
        Assert.assertTrue(linkedLog.getFields().containsKey("view"));
        Assert.assertTrue(String.valueOf(linkedLog.getFields().get("application"))
                .contains("native-app"));
        Assert.assertTrue(String.valueOf(linkedLog.getFields().get("session"))
                .contains("native-session"));

        config.setEnableLinkRumData(false);
        LogBean unlinkedLog = newWebViewLinkedLog();

        TrackLogManager.applyWebViewRumLinkConfiguration(config, unlinkedLog, null, null);

        Assert.assertFalse(unlinkedLog.getTags().containsKey(Constants.KEY_RUM_APP_ID));
        Assert.assertFalse(unlinkedLog.getTags().containsKey(Constants.KEY_RUM_SESSION_ID));
        Assert.assertFalse(unlinkedLog.getTags().containsKey(Constants.KEY_RUM_VIEW_ID));
        Assert.assertFalse(unlinkedLog.getFields().containsKey("application"));
        Assert.assertFalse(unlinkedLog.getFields().containsKey("session"));
        Assert.assertFalse(unlinkedLog.getFields().containsKey("view"));
        Assert.assertEquals("Chrome", unlinkedLog.getTags().get("browser"));
        Assert.assertEquals(Boolean.TRUE,
                unlinkedLog.getTags().get(Constants.KEY_RUM_VIEW_IS_WEB_VIEW));
    }

    private static LogBean newWebViewLinkedLog() {
        LogBean logBean = new LogBean("web", 1L);
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);
        tags.put(Constants.KEY_RUM_APP_ID, "web-app");
        tags.put(Constants.KEY_RUM_SESSION_ID, "web-session");
        tags.put(Constants.KEY_RUM_VIEW_ID, "web-view");
        tags.put("browser", "Chrome");
        logBean.appendTags(tags);
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("application", "{\"id\":\"web-app\"}");
        fields.put("session", "{\"id\":\"web-session\"}");
        fields.put("view", "{\"id\":\"web-view\"}");
        logBean.appendFields(fields);
        return logBean;
    }

    private static void markAsWebViewLog(LogBean logBean) {
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);
        logBean.appendTags(tags);
    }
}
