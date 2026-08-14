package com.ft.sdk;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class WebViewLogSyncDataTest {

    @Test
    public void linkedWebViewLogUsesWebViewRumCollisionRules() {
        FTSDKConfig baseConfig = FTSDKConfig.builder()
                .addGlobalContext(Constants.KEY_SDK_VERSION, "native-version")
                .addGlobalContext(Constants.KEY_SDK_NAME, "native-sdk")
                .addGlobalContext(Constants.KEY_SDK_PACKAGE_INFO,
                        "{\"agent\":\"native-version\"}");
        FTLoggerConfig loggerConfig = new FTLoggerConfig()
                .setEnableLinkRumData(true)
                .addGlobalContext(Constants.KEY_SERVICE, "native-service");
        FTRUMConfig rumConfig = new FTRUMConfig()
                .addGlobalContext(Constants.KEY_RUM_APP_ID, "native-app-id")
                .addGlobalContext("native_rum_context", "native-view");
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(baseConfig);
        helper.initLogConfig(loggerConfig);
        helper.initRUMConfig(rumConfig);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_SERVICE, "browser-service");
        tags.put(Constants.KEY_SDK_VERSION, "3.3.6");
        tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(Constants.KEY_MESSAGE, "message");

        String body = helper.getBodyContent(Constants.FT_LOG_DEFAULT_MEASUREMENT,
                tags, fields, 123L, DataType.LOG, "uuid");

        Assert.assertTrue(body.contains("service=browser-service"));
        Assert.assertTrue(body.contains("sdk_version=native-version"));
        Assert.assertTrue(body.contains("sdk_name=native-sdk"));
        Assert.assertTrue(body.contains("is_web_view=true"));
        Assert.assertTrue(body.contains("sdk_pkg_info="));
        Assert.assertTrue(body.contains("web"));
        Assert.assertTrue(body.contains("3.3.6"));
        Assert.assertTrue(body.contains("app_id=native-app-id"));
        Assert.assertTrue(body.contains("native_rum_context=native-view"));
    }

    @Test
    public void unlinkedWebViewLogUsesNativeLogTagsWithoutRumTags() {
        FTSDKConfig baseConfig = FTSDKConfig.builder()
                .addGlobalContext(Constants.KEY_SDK_VERSION, "native-version")
                .addGlobalContext(Constants.KEY_SDK_NAME, "native-sdk");
        FTLoggerConfig loggerConfig = new FTLoggerConfig()
                .setEnableLinkRumData(false)
                .addGlobalContext("native_log_context", "native-log");
        FTRUMConfig rumConfig = new FTRUMConfig()
                .addGlobalContext(Constants.KEY_RUM_APP_ID, "native-app-id")
                .addGlobalContext("native_rum_context", "native-view");
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(baseConfig);
        helper.initLogConfig(loggerConfig);
        helper.initRUMConfig(rumConfig);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_SERVICE, "browser-service");
        tags.put(Constants.KEY_SDK_VERSION, "3.3.6");
        tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(Constants.KEY_MESSAGE, "message");

        String body = helper.getBodyContent(Constants.FT_LOG_DEFAULT_MEASUREMENT,
                tags, fields, 123L, DataType.LOG, "uuid");

        Assert.assertTrue(body.contains("service=browser-service"));
        Assert.assertTrue(body.contains("sdk_version=native-version"));
        Assert.assertTrue(body.contains("native_log_context=native-log"));
        Assert.assertFalse(body.contains("app_id=native-app-id"));
        Assert.assertFalse(body.contains("native_rum_context"));
    }
}
