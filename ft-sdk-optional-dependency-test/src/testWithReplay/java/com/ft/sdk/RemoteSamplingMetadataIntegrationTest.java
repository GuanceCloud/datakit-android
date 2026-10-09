package com.ft.sdk;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class RemoteSamplingMetadataIntegrationTest {
    @After
    public void tearDown() {
        FTSdk.shutDown();
    }

    @Test
    public void remoteZeroToFullSamplingRefreshesSessionAndAllFourReportedRates() throws Exception {
        LogUtils.setDebug(false);
        FTSdk.install(FTSDKConfig.builder("http://127.0.0.1:1")
                .setOnlySupportMainProcess(false).setAutoSync(false)
                .setEnableAccessAndroidID(false).setEnableDataFilter(false));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId("sampling-test")
                .setSessionSamplingRate(0f).setSessionOnErrorSamplingRate(0f)
                .setEnableTraceWebView(false));
        FTSessionReplayConfig replayConfig = new FTSessionReplayConfig()
                .setSessionReplaySamplingRate(0f).setSessionReplayOnErrorSamplingRate(0f);
        replayConfig.setExternalRecorderMode(true);
        FTSdk.initSessionReplayConfig(replayConfig);
        assertTrue(FTSdk.isSessionReplaySupport());

        FTRUMInnerManager rum = FTRUMInnerManager.get();
        String originalSession = rum.getSessionId();
        assertEquals(CollectType.NOT_COLLECT, rum.checkSessionWillCollect(originalSession));
        assertRates(0f);

        applyRemote("{\"rumSampleRate\":1,\"rumSessionOnErrorSampleRate\":1,"
                + "\"sessionReplaySampleRate\":1,\"sessionReplayOnErrorSampleRate\":1}");

        assertNotEquals(originalSession, rum.getSessionId());
        assertEquals(CollectType.COLLECT_BY_SAMPLE, rum.checkSessionWillCollect(rum.getSessionId()));
        assertRates(100f);

        String sampledSession = rum.getSessionId();
        applyRemote("{\"sessionSamplingRate\":100,\"sessionOnErrorSamplingRate\":100,"
                + "\"sessionReplaySamplingRate\":100,\"sessionReplayOnErrorSamplingRate\":100}");
        assertEquals(sampledSession, rum.getSessionId());
        assertRates(100f);

        applyRemote("{\"sessionSamplingRate\":12.5,\"sessionOnErrorSamplingRate\":12.5,"
                + "\"sessionReplaySamplingRate\":12.5,\"sessionReplayOnErrorSamplingRate\":12.5}");

        assertEquals(sampledSession, rum.getSessionId());
        assertRates(12.5f);

        applyRemote("{\"sessionSamplingRate\":1,\"sessionOnErrorSamplingRate\":1,"
                + "\"sessionReplaySamplingRate\":1,\"sessionReplayOnErrorSamplingRate\":1}");
        assertEquals(sampledSession, rum.getSessionId());
        assertRates(1f);

        applyRemote("{\"sessionSamplingRate\":0,\"sessionOnErrorSamplingRate\":0,"
                + "\"sessionReplaySamplingRate\":0,\"sessionReplayOnErrorSamplingRate\":0}");
        assertNotEquals(sampledSession, rum.getSessionId());
        assertEquals(CollectType.NOT_COLLECT, rum.checkSessionWillCollect(rum.getSessionId()));
        assertRates(0f);
    }

    private void applyRemote(String content) throws Exception {
        Method notify = FTRemoteConfigManager.class.getDeclaredMethod("notifyHotUpdate", RemoteConfigBean.class);
        notify.setAccessible(true);
        notify.invoke(new FTRemoteConfigManager(60, null),
                RemoteConfigBean.buildFromConfigJson("{\"content\":" + content + "}"));
    }

    private void assertRates(float expected) {
        HashMap<String, Object> fields = new HashMap<>();
        String line = FTTrackInner.getInstance().getCurrentDataHelper().getBodyContent(
                "view", new HashMap<>(), fields, 123L, DataType.RUM_APP, "uuid");
        assertFalse(line.isEmpty());
        for (String key : new String[]{"session_sample_rate", "session_on_error_sample_rate",
                "session_replay_sample_rate", "session_replay_on_error_sample_rate"}) {
            assertNotNull(key, fields.get(key));
            assertEquals(key, expected, ((Number) fields.get(key)).floatValue(), .0000001f);
        }
    }
}
