package com.ft.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SyncTaskManagerTest {

    @After
    public void tearDown() {
        FTHttpConfigManager.release();
    }

    @Test
    public void shouldBackoffIgnoredClientErrorMatchesOnly403And429() {
        assertFalse(SyncTaskManager.shouldBackoffIgnoredClientError(HttpURLConnection.HTTP_BAD_REQUEST));
        assertFalse(SyncTaskManager.shouldBackoffIgnoredClientError(HttpURLConnection.HTTP_UNAUTHORIZED));
        assertTrue(SyncTaskManager.shouldBackoffIgnoredClientError(HttpURLConnection.HTTP_FORBIDDEN));
        assertFalse(SyncTaskManager.shouldBackoffIgnoredClientError(HttpURLConnection.HTTP_NOT_FOUND));
        assertTrue(SyncTaskManager.shouldBackoffIgnoredClientError(429));
        assertFalse(SyncTaskManager.shouldBackoffIgnoredClientError(499));
        assertFalse(SyncTaskManager.shouldBackoffIgnoredClientError(HttpURLConnection.HTTP_INTERNAL_ERROR));
    }

    @Test
    public void ignoredClientErrorBackoffIsCapped() {
        assertEquals(500L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(1));
        assertEquals(1000L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(2));
        assertEquals(8000L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(5));
        assertEquals(8000L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(6));
    }

    @Test
    public void syncOrderPrioritizesRumBeforeLogs() {
        assertArrayEquals(new DataType[]{
                DataType.RUM_APP,
                DataType.RUM_WEBVIEW,
                DataType.LOG
        }, SyncTaskManager.getSyncMap());
    }

    @Test
    public void syncPageBudgetKeepsLogFromDrainingWholeRound() {
        assertEquals(2, SyncTaskManager.getMaxPagesPerRound(DataType.RUM_APP));
        assertEquals(1, SyncTaskManager.getMaxPagesPerRound(DataType.RUM_WEBVIEW));
        assertEquals(1, SyncTaskManager.getMaxPagesPerRound(DataType.LOG));
    }

    @Test
    public void disableServerFilterParamOnlyAppliesToDatakitUpload() {
        assertFalse(SyncTaskManager.shouldAddDisableServerFilterParam(false));

        FTHttpConfigManager.get().setDatawayUrl("https://dataway.example.com", "test-token");
        assertFalse(SyncTaskManager.shouldAddDisableServerFilterParam(true));

        FTHttpConfigManager.get().setDatakitUrl("https://datakit.example.com");
        assertTrue(SyncTaskManager.shouldAddDisableServerFilterParam(true));
    }

    @Test
    public void requestBodyUsesOnlyUploadDataList() {
        SyncData filtered = syncData("drop_uuid",
                "custom_log,sdk_data_id=drop_uuid message=\"drop\" 100\n");
        SyncData uploaded = syncData("keep_uuid",
                "custom_log,sdk_data_id=keep_uuid message=\"keep\" 101\n");

        List<SyncData> uploadDataList = new ArrayList<>();
        uploadDataList.add(uploaded);

        String body = SyncTaskManager.buildRequestBody(uploadDataList, "pkg");

        assertFalse(body.contains("drop"));
        assertFalse(body.contains(filtered.getUuid()));
        assertTrue(body.contains("keep"));
        assertTrue(body.contains("pkg.keep_uuid"));
    }

    private SyncData syncData(String uuid, String lineProtocol) {
        SyncData data = new SyncData(DataType.LOG);
        data.setUuid(uuid);
        data.setDataString(lineProtocol);
        return data;
    }
}
