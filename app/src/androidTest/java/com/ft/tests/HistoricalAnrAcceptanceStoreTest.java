package com.ft.tests;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.HistoricalAnrAcceptanceReceiver;
import com.ft.utils.HistoricalAnrAcceptanceStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HistoricalAnrAcceptanceStoreTest {
    private Context context;
    private HistoricalAnrAcceptanceReceiver receiver;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        receiver = new HistoricalAnrAcceptanceReceiver();
        HistoricalAnrAcceptanceStore.reset(context);
    }

    @After
    public void tearDown() {
        HistoricalAnrAcceptanceStore.reset(context);
    }

    @Test
    public void receiverCountsEveryCrossProcessIssueCallback() {
        HistoricalAnrAcceptanceStore.begin(context);
        receiver.onReceive(context, new Intent(
                HistoricalAnrAcceptanceStore.ACTION_WORKER_VIEW_READY)
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_EXPECTED_SESSION_ID, "session")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_EXPECTED_VIEW_ID, "view")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_EXPECTED_VIEW_NAME, "name")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_EXPECTED_PROCESS_NAME, "process")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_EXPECTED_PROCESS_RUN_ID, "run"));
        receiver.onReceive(context, new Intent(
                HistoricalAnrAcceptanceStore.ACTION_ANR_TRIGGERED)
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_TRIGGERED_AT_MS, 123L));

        receiver.onReceive(context, observedIssueIntent());
        receiver.onReceive(context, observedIssueIntent());
        receiver.onReceive(context, observedPayloadIntent());
        receiver.onReceive(context, observedPayloadIntent());
        HistoricalAnrAcceptanceStore.markConsumerStarted(context);

        HistoricalAnrAcceptanceStore.Snapshot snapshot =
                HistoricalAnrAcceptanceStore.snapshot(context);
        assertEquals(HistoricalAnrAcceptanceStore.STAGE_HISTORICAL_ISSUE_OBSERVED,
                snapshot.stage);
        assertEquals("session", snapshot.expectedSessionId);
        assertEquals("view", snapshot.expectedViewId);
        assertEquals("run", snapshot.expectedProcessRunId);
        assertEquals(123L, snapshot.triggeredAtMs);
        assertEquals(2, snapshot.historicalIssueCount);
        assertEquals("anr_crash", snapshot.issueErrorType);
        assertEquals(456L, snapshot.issueOccurredAtNs);
        assertEquals(2, snapshot.payloadCount);
        assertEquals("session", snapshot.payloadSessionId);
        assertEquals("view", snapshot.payloadViewId);
        assertEquals("process", snapshot.payloadProcessName);
    }

    private Intent observedIssueIntent() {
        return new Intent(HistoricalAnrAcceptanceStore.ACTION_HISTORICAL_ISSUE_OBSERVED)
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_ISSUE_ERROR_TYPE, "anr_crash")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_ISSUE_OCCURRED_AT_NS, 456L);
    }

    private Intent observedPayloadIntent() {
        return new Intent(HistoricalAnrAcceptanceStore.ACTION_HISTORICAL_PAYLOAD_OBSERVED)
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_PAYLOAD_SESSION_ID, "session")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_PAYLOAD_VIEW_ID, "view")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_PAYLOAD_VIEW_NAME, "name")
                .putExtra(HistoricalAnrAcceptanceStore.EXTRA_PAYLOAD_PROCESS_NAME, "process");
    }
}
