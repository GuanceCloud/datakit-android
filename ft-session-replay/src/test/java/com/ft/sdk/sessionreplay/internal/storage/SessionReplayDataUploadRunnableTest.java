package com.ft.sdk.sessionreplay.internal.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SessionReplayDataUploadRunnableTest {

    @Test
    public void retryBackoffDoublesFromDefaultDelayAndCapsAtMax() {
        assertEquals(2500L, SessionReplayDataUploadRunnable.getRetryBackoffDelayMs(2500L, 30000L, 1));
        assertEquals(5000L, SessionReplayDataUploadRunnable.getRetryBackoffDelayMs(2500L, 30000L, 2));
        assertEquals(10000L, SessionReplayDataUploadRunnable.getRetryBackoffDelayMs(2500L, 30000L, 3));
        assertEquals(30000L, SessionReplayDataUploadRunnable.getRetryBackoffDelayMs(2500L, 30000L, 8));
    }

    @Test
    public void retryBackoffTreatsInvalidRetryCountAsFirstRetry() {
        assertEquals(2500L, SessionReplayDataUploadRunnable.getRetryBackoffDelayMs(2500L, 30000L, 0));
    }
}
