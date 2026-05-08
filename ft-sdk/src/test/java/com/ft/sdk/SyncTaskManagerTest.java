package com.ft.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpURLConnection;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SyncTaskManagerTest {

    @Test
    public void isIgnoredClientErrorMatchesOnly4xx() {
        assertFalse(SyncTaskManager.isIgnoredClientError(HttpURLConnection.HTTP_MULT_CHOICE));
        assertTrue(SyncTaskManager.isIgnoredClientError(HttpURLConnection.HTTP_BAD_REQUEST));
        assertTrue(SyncTaskManager.isIgnoredClientError(499));
        assertFalse(SyncTaskManager.isIgnoredClientError(HttpURLConnection.HTTP_INTERNAL_ERROR));
    }

    @Test
    public void ignoredClientErrorBackoffIsCapped() {
        assertEquals(500L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(1));
        assertEquals(1000L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(2));
        assertEquals(8000L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(5));
        assertEquals(8000L, SyncTaskManager.getIgnoredClientErrorBackoffTimeMs(6));
    }
}
