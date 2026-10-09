package com.ft.sdk;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.sessionreplay.RateBasedSampler;
import com.ft.sdk.sessionreplay.SDKFeature;
import com.ft.sdk.sessionreplay.SessionReplayFeature;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("deprecation")
public class SamplingRateMetadataTest {
    private final FTRemoteConfigManager remote = new FTRemoteConfigManager(60, null);
    private final FTRUMConfig rum = new FTRUMConfig()
            .setSessionSamplingRate(0f).setSessionOnErrorSamplingRate(0f);
    private final SyncDataHelper helper = new SyncDataHelper();
    private SessionReplayManager replay;
    private SessionReplayFeature previousReplay;
    private Map<String, SDKFeature> features;
    private Map<String, SDKFeature> previousFeatures;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        LogUtils.setDebug(false);
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(rum);
        replay = SessionReplayManager.get();
        previousReplay = (SessionReplayFeature) field("sessionReplayFeature").get(replay);
        features = (Map<String, SDKFeature>) field("features").get(replay);
        previousFeatures = new HashMap<>(features);
        features.clear();
        field("sessionReplayFeature").set(replay, null);
    }

    @After
    public void tearDown() throws Exception {
        features.clear();
        features.putAll(previousFeatures);
        field("sessionReplayFeature").set(replay, previousReplay);
    }

    @Test
    public void remoteZeroToFullSamplingUpdatesNewRecordsAndKeepsSerializedRecords() throws Exception {
        HashMap<String, Object> oldFields = serialize(DataType.RUM_APP);
        updateRum("{\"rumSampleRate\":1,\"rumSessionOnErrorSampleRate\":1}");

        assertRumRates(serialize(DataType.RUM_APP), 100f, 100f);
        assertRumRates(serialize(DataType.RUM_APP_ERROR_SAMPLED), 100f, 100f);
        assertRumRates(oldFields, 0f, 0f);
    }

    @Test
    public void partialFractionalRemoteUpdatesPreserveOmittedAndInvalidRates() throws Exception {
        updateRum("{\"sessionSamplingRate\":12.5,\"sessionOnErrorSamplingRate\":25}");
        assertRumRates(serialize(DataType.RUM_APP), 12.5f, 25f);
        updateRum("{\"sessionOnErrorSamplingRate\":0}");
        assertRumRates(serialize(DataType.RUM_APP), 12.5f, 0f);
        updateRum("{\"sessionSamplingRate\":101,\"rumSampleRate\":1}");
        assertRumRates(serialize(DataType.RUM_APP), 12.5f, 0f);
        updateRum("{}");
        assertRumRates(serialize(DataType.RUM_APP), 12.5f, 0f);
    }

    @Test
    public void activeReplayRatesFollowRuntimeUpdatesIncludingZero() throws Exception {
        enableReplay();
        HashMap<String, Object> oldFields = serialize(DataType.RUM_APP);
        assertReplayRates(oldFields, 0f, 0f);

        replay.setSampleRate(1f);
        replay.setSessionReplayOnErrorSampleRate(1f);
        assertReplayRates(serialize(DataType.RUM_APP), 100f, 100f);
        assertReplayRates(oldFields, 0f, 0f);

        replay.setSessionReplaySamplingRate(12.5f);
        assertReplayRates(serialize(DataType.RUM_APP_ERROR_SAMPLED), 12.5f, 100f);
        replay.setSessionReplayOnErrorSamplingRate(0f);
        assertReplayRates(serialize(DataType.RUM_APP), 12.5f, 0f);

        features.clear();
        HashMap<String, Object> disabledFields = serialize(DataType.RUM_APP);
        assertFalse(disabledFields.containsKey("session_replay_sample_rate"));
        assertFalse(disabledFields.containsKey("session_replay_on_error_sample_rate"));
    }

    @Test
    public void nativeSamplingMetadataUsesPercentagesForAllFourFields() throws Exception {
        enableReplay();
        for (float percentage : new float[]{0f, .1f, 1f, 12.5f, 99.9f, 100f}) {
            rum.setSessionSamplingRate(percentage).setSessionOnErrorSamplingRate(percentage);
            replay.setSessionReplaySamplingRate(percentage);
            replay.setSessionReplayOnErrorSamplingRate(percentage);
            for (DataType type : new DataType[]{DataType.RUM_APP, DataType.RUM_APP_ERROR_SAMPLED}) {
                HashMap<String, Object> fields = serialize(type);
                assertRumRates(fields, percentage, percentage);
                assertReplayRates(fields, percentage, percentage);
            }
        }
    }

    @Test
    public void uninitializedReplayDoesNotAddSamplingFields() {
        HashMap<String, Object> fields = serialize(DataType.RUM_APP);
        assertFalse(fields.containsKey("session_replay_sample_rate"));
        assertFalse(fields.containsKey("session_replay_on_error_sample_rate"));
    }

    @Test
    public void webViewSamplingMetadataRemainsOwnedByBrowser() throws Exception {
        enableReplay();
        updateRum("{\"rumSampleRate\":1,\"rumSessionOnErrorSampleRate\":1}");
        replay.setSampleRate(1f);
        for (DataType type : new DataType[]{DataType.RUM_WEBVIEW, DataType.RUM_WEBVIEW_ERROR_SAMPLED}) {
            HashMap<String, Object> fields = new HashMap<>();
            fields.put("session_sample_rate", 10f);
            fields.put("session_on_error_sample_rate", 20f);
            fields.put("session_replay_sample_rate", 30f);
            fields.put("session_replay_on_error_sample_rate", 40f);
            serialize(type, fields);
            assertRumRates(fields, 10f, 20f);
            assertReplayRates(fields, 30f, 40f);
        }
    }

    @Test
    public void modifiersReceiveLatestNativeSamplingValues() throws Exception {
        enableReplay();
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false)
                .setDataModifier((key, value) -> key.endsWith("sample_rate")
                        ? ((Number) value).floatValue() / 2f : value)
                .setLineDataModifier((measurement, data) -> {
                    assertEquals(50f, ((Number) data.get("session_sample_rate")).floatValue(), 0f);
                    assertEquals(25f, ((Number) data.get("session_replay_sample_rate")).floatValue(), 0f);
                    return null;
                }));
        helper.initRUMConfig(rum);
        updateRum("{\"rumSampleRate\":1,\"rumSessionOnErrorSampleRate\":1}");
        replay.setSampleRate(.5f);
        replay.setSessionReplayOnErrorSampleRate(.5f);
        HashMap<String, Object> fields = serialize(DataType.RUM_APP);
        assertRumRates(fields, 50f, 50f);
        assertReplayRates(fields, 25f, 25f);
    }

    private void updateRum(String content) throws Exception {
        Field cache = FTRemoteConfigManager.class.getDeclaredField("mRemoteConfig");
        cache.setAccessible(true);
        cache.set(remote, RemoteConfigBean.buildFromConfigJson("{\"content\":" + content + "}"));
        remote.mergeRUMConfigFromCache(rum);
    }

    private void enableReplay() throws Exception {
        SessionReplayFeature feature = new SessionReplayFeature(replay, null, null, null, null,
                new RateBasedSampler(0f), new RateBasedSampler(0f), null, null, null);
        field("sessionReplayFeature").set(replay, feature);
        features.put("session-replay", new SDKFeature(replay, feature, new NoOpInternalLogger(), null));
        assertTrue(SessionReplayBridge.isReplayEnabled());
    }

    private Field field(String name) throws Exception {
        Field field = SessionReplayManager.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private HashMap<String, Object> serialize(DataType type) {
        HashMap<String, Object> fields = new HashMap<>();
        serialize(type, fields);
        return fields;
    }

    private void serialize(DataType type, HashMap<String, Object> fields) {
        String line = helper.getBodyContent("view", new HashMap<>(), fields, 123L, type, "uuid");
        assertFalse(line.isEmpty());
    }

    private void assertRumRates(Map<String, Object> fields, float regular, float error) {
        assertEquals(regular, ((Number) fields.get("session_sample_rate")).floatValue(), .0001f);
        assertEquals(error, ((Number) fields.get("session_on_error_sample_rate")).floatValue(), .0001f);
    }

    private void assertReplayRates(Map<String, Object> fields, float regular, float error) {
        assertNotNull(fields.get("session_replay_sample_rate"));
        assertNotNull(fields.get("session_replay_on_error_sample_rate"));
        assertEquals(regular, ((Number) fields.get("session_replay_sample_rate")).floatValue(), .0001f);
        assertEquals(error, ((Number) fields.get("session_replay_on_error_sample_rate")).floatValue(), .0001f);
    }
}
