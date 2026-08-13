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

    private static void markAsWebViewLog(LogBean logBean) {
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);
        logBean.appendTags(tags);
    }
}
