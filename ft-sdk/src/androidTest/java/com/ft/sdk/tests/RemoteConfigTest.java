package com.ft.sdk.tests;

import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.test.base.FTBaseTest;

import org.junit.Test;

public class RemoteConfigTest extends FTBaseTest {

    private final static String normalUrl = "{\n" +
            "  \"content\": {\n" +
            "    \"serviceName\": \"testService\",\n" +
            "    \"env\": \"prod\",\n" +
            "    \"autoSync\": true,\n" +
            "    \"compressIntakeRequests\": false,\n" +
            "    \"syncPageSize\": 100,\n" +
            "    \"syncSleepTime\": 5000,\n" +
            "    \"rumSampleRate\": 0.8,\n" +
            "    \"rumSessionOnErrorSampleRate\": 0.9,\n" +
            "    \"rumEnableTraceUserAction\": true,\n" +
            "    \"rumEnableTraceUserView\": true,\n" +
            "    \"rumEnableTraceUserResource\": false,\n" +
            "    \"rumEnableResourceHostIP\": true,\n" +
            "    \"rumEnableTrackAppUIBlock\": true,\n" +
            "    \"rumBlockDurationMs\": 3000,\n" +
            "    \"rumEnableTrackAppCrash\": true,\n" +
            "    \"rumEnableTrackAppANR\": false,\n" +
            "    \"rumEnableTraceWebView\": true,\n" +
            "    \"rumAllowWebViewHost\": [\"example.com\", \"api.example.com\"],\n" +
            "    \"logSampleRate\": 0.5,\n" +
            "    \"logLevelFilters\": [\"ERROR\", \"WARN\", \"INFO\"],\n" +
            "    \"logEnableCustomLog\": true,\n" +
            "    \"logEnableConsoleLog\": false,\n" +
            "    \"traceSampleRate\": 0.7,\n" +
            "    \"traceEnableAutoTrace\": true,\n" +
            "    \"traceType\": \"zipkin\",\n" +
            "    \"sessionReplaySampleRate\": 0.3,\n" +
            "    \"sessionReplayOnErrorSampleRate\": 0.6,\n" +
            "    \"md5\": \"xxx\",\n" +
            "    \"sessionSampleRate\": 0.4\n" +
            "  }\n" +
            "}";


    @Test
    public void normalConfig() {
        RemoteConfigBean conf = RemoteConfigBean.buildFromConfigJson("");
    }


    @Test
    public void invalidConfig() {
    }

    @Test
    public void remoteConfigMiniUpdateIntervalTest() {

    }

    @Test
    public void localCacheTest() {

    }

    @Test
    public void remoteConfigTest() {

    }
}
