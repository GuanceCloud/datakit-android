package com.ft.sdk.tests;

import android.content.SharedPreferences;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class RemoteConfigTest extends FTBaseTest {

    private static final String VALUE_MD5 = "xxx";
    private static final String VALUE_SERVICE_NAME = "testService";
    private static final String VALUE_ENV = "prod";
    private static final boolean VALUE_AUTO_SYNC = true;
    private static final boolean VALUE_COMPRESS_INTAKE_REQUESTS = true;
    private static final int VALUE_SYNC_PAGE_SIZE = 100;
    private static final int VALUE_SYNC_SLEEP_TIME = 5000;
    private static final float VALUE_RUM_SAMPLE_RATE = 0.8f;
    private static final float VALUE_RUM_SESSION_ON_ERROR_SAMPLE_RATE = 0.9f;
    private static final boolean VALUE_RUM_ENABLE_TRACE_USER_ACTION = true;
    private static final boolean VALUE_RUM_ENABLE_TRACE_USER_VIEW = true;
    private static final boolean VALUE_RUM_ENABLE_RESOURCE_HOST_IP = true;
    private static final boolean VALUE_RUM_ENABLE_TRACE_USER_RESOURCE = true;
    private static final boolean VALUE_RUM_ENABLE_TRACK_APP_UIBLOCK = true;
    private static final int VALUE_RUM_BLOCK_DURATION_MS = 3000;
    private static final boolean VALUE_RUM_ENABLE_TRACK_APP_CRASH = true;
    private static final boolean VALUE_RUM_ENABLE_TRACK_APP_ANR = true;
    private static final boolean VALUE_RUM_ENABLE_TRACE_WEB_VIEW = true;
    private static final String[] VALUE_RUM_ALLOW_WEB_VIEW_HOST = new String[]{"example.com", "api.example.com"};
    private static final float VALUE_LOG_SAMPLE_RATE = 0.5f;
    private static final String[] VALUE_LOG_LEVEL_FILTERS = new String[]{"error", "warn", "info"};
    private static final boolean VALUE_LOG_ENABLE_CUSTOM_LOG = true;
    private static final boolean VALUE_LOG_ENABLE_CONSOLE_LOG = true;
    private static final float VALUE_TRACE_SAMPLE_RATE = 0.7f;
    private static final boolean VALUE_TRACE_ENABLE_AUTO_TRACE = true;
    private static final String VALUE_TRACE_TYPE = "zipkin_single_header";
    private static final float VALUE_SESSION_REPLAY_SAMPLE_RATE = 0.3f;
    private static final float VALUE_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE = 0.6f;

    private final static String NORMAL_JSON_STRING = "{\n" +
            "  \"" + RemoteConfigBean.KEY_MD5 + "\": \"" + VALUE_MD5 + "\"," +
            "  \"content\": {\n" +
            "    \"" + RemoteConfigBean.KEY_SERVICE_NAME + "\": \"" + VALUE_SERVICE_NAME + "\",\n" +
            "    \"" + RemoteConfigBean.KEY_ENV + "\": \"" + VALUE_ENV + "\",\n" +
            "    \"" + RemoteConfigBean.KEY_AUTO_SYNC + "\": " + VALUE_AUTO_SYNC + ",\n" +
            "    \"" + RemoteConfigBean.KEY_COMPRESS_INTAKE_REQUESTS + "\": " + VALUE_COMPRESS_INTAKE_REQUESTS + ",\n" +
            "    \"" + RemoteConfigBean.KEY_SYNC_PAGE_SIZE + "\": " + VALUE_SYNC_PAGE_SIZE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_SYNC_SLEEP_TIME + "\": " + VALUE_SYNC_SLEEP_TIME + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_SAMPLE_RATE + "\": " + VALUE_RUM_SAMPLE_RATE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE + "\": " + VALUE_RUM_SESSION_ON_ERROR_SAMPLE_RATE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_USER_ACTION + "\": " + VALUE_RUM_ENABLE_TRACE_USER_ACTION + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_USER_VIEW + "\": " + VALUE_RUM_ENABLE_TRACE_USER_VIEW + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_USER_RESOURCE + "\": " + VALUE_RUM_ENABLE_TRACE_USER_RESOURCE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_RESOURCE_HOST_IP + "\": " + VALUE_RUM_ENABLE_RESOURCE_HOST_IP + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACK_APP_UIBLOCK + "\": " + VALUE_RUM_ENABLE_TRACK_APP_UIBLOCK + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_BLOCK_DURATION_MS + "\": " + VALUE_RUM_BLOCK_DURATION_MS + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACK_APP_CRASH + "\": " + VALUE_RUM_ENABLE_TRACK_APP_CRASH + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACK_APP_ANR + "\": " + VALUE_RUM_ENABLE_TRACK_APP_ANR + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_WEB_VIEW + "\": " + VALUE_RUM_ENABLE_TRACE_WEB_VIEW + ",\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ALLOW_WEB_VIEW_HOST + "\": " + Utils.setToJsonString(Arrays.asList(VALUE_RUM_ALLOW_WEB_VIEW_HOST)) + ",\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_SAMPLE_RATE + "\": " + VALUE_LOG_SAMPLE_RATE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_LEVEL_FILTERS + "\": " + Utils.setToJsonString(Arrays.asList(VALUE_LOG_LEVEL_FILTERS)) + ",\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_ENABLE_CUSTOM_LOG + "\": " + VALUE_LOG_ENABLE_CUSTOM_LOG + ",\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_ENABLE_CONSOLE_LOG + "\": " + VALUE_LOG_ENABLE_CONSOLE_LOG + ",\n" +
            "    \"" + RemoteConfigBean.KEY_TRACE_SAMPLE_RATE + "\": " + VALUE_TRACE_SAMPLE_RATE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_TRACE_ENABLE_AUTO_TRACE + "\": " + VALUE_TRACE_ENABLE_AUTO_TRACE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_TRACE_TYPE + "\": \"" + VALUE_TRACE_TYPE + "\",\n" +
            "    \"" + RemoteConfigBean.KEY_SESSION_REPLAY_SAMPLE_RATE + "\": " + VALUE_SESSION_REPLAY_SAMPLE_RATE + ",\n" +
            "    \"" + RemoteConfigBean.KEY_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE + "\": " + VALUE_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE + "\n" +
            "  }\n" +
            "}";

    private final static String INVALID_JSON_STRING = "{\n" +
            "  \"" + RemoteConfigBean.KEY_MD5 + "\": -1\"," +
            "  \"content\": {\n" +
            "    \"" + RemoteConfigBean.KEY_SERVICE_NAME + "\":-1,\n" +
            "    \"" + RemoteConfigBean.KEY_ENV + "\":-1,\n" +
            "    \"" + RemoteConfigBean.KEY_AUTO_SYNC + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_COMPRESS_INTAKE_REQUESTS + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_SYNC_PAGE_SIZE + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_SYNC_SLEEP_TIME + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_SAMPLE_RATE + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_USER_ACTION + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_USER_VIEW + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_USER_RESOURCE + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_RESOURCE_HOST_IP + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACK_APP_UIBLOCK + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_BLOCK_DURATION_MS + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACK_APP_CRASH + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACK_APP_ANR + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ENABLE_TRACE_WEB_VIEW + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_RUM_ALLOW_WEB_VIEW_HOST + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_SAMPLE_RATE + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_LEVEL_FILTERS + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_ENABLE_CUSTOM_LOG + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_LOG_ENABLE_CONSOLE_LOG + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_TRACE_SAMPLE_RATE + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_TRACE_ENABLE_AUTO_TRACE + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_TRACE_TYPE + "\": -1,\n" +
            "    \"" + RemoteConfigBean.KEY_SESSION_REPLAY_SAMPLE_RATE + "\": error_value,\n" +
            "    \"" + RemoteConfigBean.KEY_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE + "\": error_value\n" +
            "  }\n" +
            "}";


    @Test
    public void normalConfig() {
        RemoteConfigBean config = RemoteConfigBean.buildFromConfigJson(NORMAL_JSON_STRING);
        Assert.assertEquals(config.getMd5(), VALUE_MD5);
        Assert.assertEquals(config.getServiceName(), VALUE_SERVICE_NAME);
        Assert.assertEquals(config.getEnv(), VALUE_ENV);
        Assert.assertEquals(config.getAutoSync(), VALUE_AUTO_SYNC);
        Assert.assertEquals(config.getCompressIntakeRequests(), VALUE_COMPRESS_INTAKE_REQUESTS);
        Assert.assertEquals(config.getSyncPageSize().intValue(), VALUE_SYNC_PAGE_SIZE);
        Assert.assertEquals(config.getSyncSleepTime().intValue(), VALUE_SYNC_SLEEP_TIME);
        Assert.assertEquals(config.getRumSampleRate(), VALUE_RUM_SAMPLE_RATE, 0.0);
        Assert.assertEquals(config.getRumSessionOnErrorSampleRate(), VALUE_RUM_SESSION_ON_ERROR_SAMPLE_RATE, 0.0);
        Assert.assertEquals(config.getRumEnableTraceUserAction(), VALUE_RUM_ENABLE_TRACE_USER_ACTION);
        Assert.assertEquals(config.getRumEnableTraceUserView(), VALUE_RUM_ENABLE_TRACE_USER_VIEW);
        Assert.assertEquals(config.getRumEnableTraceUserResource(), VALUE_RUM_ENABLE_TRACE_USER_RESOURCE);
        Assert.assertEquals(config.getRumEnableResourceHostIP(), VALUE_RUM_ENABLE_RESOURCE_HOST_IP);
        Assert.assertEquals(config.getRumEnableTrackAppUIBlock(), VALUE_RUM_ENABLE_TRACK_APP_UIBLOCK);
        Assert.assertEquals(config.getRumBlockDurationMs().intValue(), VALUE_RUM_BLOCK_DURATION_MS);
        Assert.assertEquals(config.getRumEnableTrackAppCrash(), VALUE_RUM_ENABLE_TRACK_APP_CRASH);
        Assert.assertEquals(config.getRumEnableTrackAppANR(), VALUE_RUM_ENABLE_TRACK_APP_ANR);
        Assert.assertEquals(config.getRumEnableTraceWebView(), VALUE_RUM_ENABLE_TRACE_WEB_VIEW);
        Assert.assertArrayEquals(config.getRumAllowWebViewHost(), VALUE_RUM_ALLOW_WEB_VIEW_HOST);
        Assert.assertEquals(config.getLogSampleRate(), VALUE_LOG_SAMPLE_RATE, 0.0);
        Assert.assertArrayEquals(config.getLogLevelFilters(), VALUE_LOG_LEVEL_FILTERS);
        Assert.assertEquals(config.getLogEnableCustomLog(), VALUE_LOG_ENABLE_CUSTOM_LOG);
        Assert.assertEquals(config.getLogEnableConsoleLog(), VALUE_LOG_ENABLE_CONSOLE_LOG);
        Assert.assertEquals(config.getTraceSampleRate(), VALUE_TRACE_SAMPLE_RATE, 0.0);
        Assert.assertEquals(config.getTraceEnableAutoTrace(), VALUE_TRACE_ENABLE_AUTO_TRACE);
        Assert.assertEquals(config.getTraceType(), VALUE_TRACE_TYPE);
        Assert.assertEquals(config.getSessionReplaySampleRate(), VALUE_SESSION_REPLAY_SAMPLE_RATE, 0.0);
        Assert.assertEquals(config.getSessionReplayOnErrorSampleRate(), VALUE_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE, 0.0);
    }


    @Test
    public void invalidConfig() {
        RemoteConfigBean config = RemoteConfigBean.buildFromConfigJson(INVALID_JSON_STRING);
        Assert.assertNull(config.getCompressIntakeRequests());
        Assert.assertNull(config.getEnv());
        Assert.assertNull(config.getAutoSync());
        Assert.assertNull(config.getCompressIntakeRequests());
        Assert.assertNull(config.getSyncPageSize());
        Assert.assertNull(config.getSyncSleepTime());
        Assert.assertNull(config.getRumSampleRate());
        Assert.assertNull(config.getRumSessionOnErrorSampleRate());
        Assert.assertNull(config.getRumEnableTraceUserAction());
        Assert.assertNull(config.getRumEnableTraceUserView());
        Assert.assertNull(config.getRumEnableTraceUserResource());
        Assert.assertNull(config.getRumEnableResourceHostIP());
        Assert.assertNull(config.getRumEnableTrackAppUIBlock());
        Assert.assertNull(config.getRumBlockDurationMs());
        Assert.assertNull(config.getRumEnableTrackAppCrash());
        Assert.assertNull(config.getRumEnableTrackAppANR());
        Assert.assertNull(config.getRumEnableTraceWebView());
        Assert.assertNull(config.getRumAllowWebViewHost());
        Assert.assertNull(config.getLogSampleRate());
        Assert.assertNull(config.getLogLevelFilters());
        Assert.assertNull(config.getLogEnableCustomLog());
        Assert.assertNull(config.getLogEnableConsoleLog());
        Assert.assertNull(config.getTraceSampleRate());
        Assert.assertNull(config.getTraceEnableAutoTrace());
        Assert.assertNull(config.getTraceType());
        Assert.assertNull(config.getSessionReplaySampleRate());
        Assert.assertNull(config.getSessionReplayOnErrorSampleRate());

    }

    @Test
    public void localCacheTest() {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putString(Constants.FT_REMOTE_CONFIG, NORMAL_JSON_STRING).commit();

        FTSDKConfig config = FTSDKConfig.builder(TEST_FAKE_URL).setRemoteConfiguration(true);
        FTSdk.install(config);
        FTRUMConfig rumConfig = new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID);
        FTSdk.initRUMWithConfig(rumConfig);
        FTLoggerConfig logConfig = new FTLoggerConfig();
        FTSdk.initLogWithConfig(logConfig);
        FTTraceConfig traceConfig = new FTTraceConfig();
        FTSdk.initTraceWithConfig(traceConfig);

        Assert.assertEquals(config.getServiceName(), VALUE_SERVICE_NAME);
        Assert.assertEquals(config.getEnv(), VALUE_ENV);
        Assert.assertEquals(config.isAutoSync(), VALUE_AUTO_SYNC);
        Assert.assertEquals(config.isCompressIntakeRequests(), VALUE_COMPRESS_INTAKE_REQUESTS);
        Assert.assertEquals(config.getPageSize(), VALUE_SYNC_PAGE_SIZE);
        Assert.assertEquals(config.getSyncSleepTime(), VALUE_SYNC_SLEEP_TIME);
        Assert.assertEquals(rumConfig.getSamplingRate(), VALUE_RUM_SAMPLE_RATE, 0.0);
        Assert.assertEquals(rumConfig.getSessionErrorSampleRate(), VALUE_RUM_SESSION_ON_ERROR_SAMPLE_RATE, 0.0);
        Assert.assertEquals(rumConfig.isEnableTraceUserAction(), VALUE_RUM_ENABLE_TRACE_USER_ACTION);
        Assert.assertEquals(rumConfig.isEnableTraceUserView(), VALUE_RUM_ENABLE_TRACE_USER_VIEW);
        Assert.assertEquals(rumConfig.isEnableTraceUserResource(), VALUE_RUM_ENABLE_TRACE_USER_RESOURCE);
        Assert.assertEquals(rumConfig.isEnableResourceHostIP(), VALUE_RUM_ENABLE_RESOURCE_HOST_IP);
        Assert.assertEquals(rumConfig.isEnableTrackAppUIBlock(), VALUE_RUM_ENABLE_TRACK_APP_UIBLOCK);
        Assert.assertEquals(rumConfig.getBlockDurationMS(), VALUE_RUM_BLOCK_DURATION_MS);
        Assert.assertEquals(rumConfig.isEnableTrackAppCrash(), VALUE_RUM_ENABLE_TRACK_APP_CRASH);
        Assert.assertEquals(rumConfig.isEnableTrackAppANR(), VALUE_RUM_ENABLE_TRACK_APP_ANR);
        Assert.assertEquals(rumConfig.isEnableTraceWebView(), VALUE_RUM_ENABLE_TRACE_WEB_VIEW);
        Assert.assertArrayEquals(rumConfig.getAllowWebViewHost(), VALUE_RUM_ALLOW_WEB_VIEW_HOST);
        Assert.assertEquals(logConfig.getSamplingRate(), VALUE_LOG_SAMPLE_RATE, 0.0);
        Assert.assertEquals(logConfig.getLogLevelFilters(), Arrays.asList(VALUE_LOG_LEVEL_FILTERS));
        Assert.assertEquals(logConfig.isEnableCustomLog(), VALUE_LOG_ENABLE_CUSTOM_LOG);
        Assert.assertEquals(logConfig.isEnableConsoleLog(), VALUE_LOG_ENABLE_CONSOLE_LOG);
        Assert.assertEquals(traceConfig.getSamplingRate(), VALUE_TRACE_SAMPLE_RATE, 0.0);
        Assert.assertEquals(traceConfig.isEnableAutoTrace(), VALUE_TRACE_ENABLE_AUTO_TRACE);
        Assert.assertEquals(traceConfig.getTraceType().value(), VALUE_TRACE_TYPE);
    }


}
