package com.ft.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.bean.DataType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpURLConnection;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SyncTaskManagerTest {

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
}
