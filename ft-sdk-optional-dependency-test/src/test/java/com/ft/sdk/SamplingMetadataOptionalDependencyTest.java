package com.ft.sdk;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.LogUtils;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class SamplingMetadataOptionalDependencyTest {
    @Test
    public void nativeRumSerializationToleratesAbsentOrUninitializedReplay() {
        LogUtils.setDebug(false);
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig().setSessionSamplingRate(12.5f)
                .setSessionOnErrorSamplingRate(25f));
        HashMap<String, Object> fields = new HashMap<>();
        String line = helper.getBodyContent("view", new HashMap<>(), fields, 123L,
                DataType.RUM_APP, "uuid");

        assertFalse(line.isEmpty());
        assertEquals(12.5f, ((Number) fields.get("session_sample_rate")).floatValue(), 0f);
        assertEquals(25f, ((Number) fields.get("session_on_error_sample_rate")).floatValue(), 0f);
        assertFalse(fields.containsKey("session_replay_sample_rate"));
        assertFalse(fields.containsKey("session_replay_on_error_sample_rate"));
    }
}
