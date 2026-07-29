package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.db.InsertResult;
import com.ft.sdk.garble.utils.Constants;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HistoricalAnrReporterTest {

    @Test
    public void reportUsesOnlyPersistedHistoricalRumContext() {
        RecordingSink sink = new RecordingSink();
        DefaultHistoricalAnrReporter reporter = new DefaultHistoricalAnrReporter(sink);
        HistoricalRumContext oldContext = new HistoricalRumContext(
                "com.example.app:worker",
                "old-run",
                1_000,
                "old-session",
                "old-view",
                "Checkout",
                10_000_000_000L,
                0,
                false);
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app:worker",
                123,
                20_000,
                ProcessExitRecord.REASON_ANR,
                100,
                null);

        HistoricalAnrReporter.ReportResult result =
                reporter.report(exit, oldContext, "blocked main", "dedupe");

        assertEquals(HistoricalAnrReporter.ReportResult.INSERTED, result);
        assertEquals(20_000_000_000L, sink.occurredAtNs);
        assertEquals("old-session", sink.tags.get(Constants.KEY_RUM_SESSION_ID));
        assertEquals("old-view", sink.tags.get(Constants.KEY_RUM_VIEW_ID));
        assertEquals("Checkout", sink.tags.get(Constants.KEY_RUM_VIEW_NAME));
        assertEquals(true, sink.fields.get("historical"));
        assertEquals("blocked main", sink.fields.get(Constants.KEY_RUM_ERROR_STACK));
        assertFalse(sink.tags.containsKey(Constants.KEY_RUM_ACTION_ID));
        assertEquals("old-view", sink.viewId);
        assertEquals("dedupe", sink.dedupeKey);
        assertEquals(CollectType.COLLECT_BY_SAMPLE, sink.collectType);
    }

    @Test
    public void reportRespectsHistoricalSessionSamplingDecision() {
        RecordingSink sink = new RecordingSink();
        DefaultHistoricalAnrReporter reporter = new DefaultHistoricalAnrReporter(sink);
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app", 123, 20_000,
                ProcessExitRecord.REASON_ANR, 100, null);
        HistoricalRumContext notCollected = new HistoricalRumContext(
                "com.example.app", "run", 1_000, "session", "view", "Home",
                10_000_000_000L, 0, false, CollectType.NOT_COLLECT);
        HistoricalRumContext errorSampled = new HistoricalRumContext(
                "com.example.app", "run", 1_000, "session", "view", "Home",
                10_000_000_000L, 0, false, CollectType.COLLECT_BY_ERROR_SAMPLE);

        assertEquals(HistoricalAnrReporter.ReportResult.ALREADY_EXISTS,
                reporter.report(exit, notCollected, "blocked", "dedupe-one"));
        assertEquals(0, sink.calls);

        assertEquals(HistoricalAnrReporter.ReportResult.INSERTED,
                reporter.report(exit, errorSampled, "blocked", "dedupe-two"));
        assertEquals(1, sink.calls);
        assertEquals(CollectType.COLLECT_BY_ERROR_SAMPLE, sink.collectType);
    }

    @Test
    public void reportAppliesIssueProviderFieldsBeforePersisting() {
        RecordingSink sink = new RecordingSink();
        final boolean[] called = {false};
        DefaultHistoricalAnrReporter reporter = new DefaultHistoricalAnrReporter(
                sink,
                new DefaultHistoricalAnrReporter.IssueFieldsEnricher() {
                    @Override
                    public void enrich(String stack,
                                       long occurredAtNs,
                                       com.ft.sdk.garble.bean.AppState state,
                                       HashMap<String, Object> tags,
                                       HashMap<String, Object> fields) {
                        called[0] = true;
                        assertEquals("blocked", stack);
                        assertEquals(20_000_000_000L, occurredAtNs);
                        assertEquals("run", state.toString());
                        fields.put("provider_field", "historical-anr");
                    }
                });
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app", 123, 20_000,
                ProcessExitRecord.REASON_ANR, 100, null);
        HistoricalRumContext context = new HistoricalRumContext(
                "com.example.app", "run", 1_000, "session", "view", "Home",
                10_000_000_000L, 0, false);

        assertEquals(HistoricalAnrReporter.ReportResult.INSERTED,
                reporter.report(exit, context, "blocked", "dedupe"));

        assertTrue(called[0]);
        assertEquals("historical-anr", sink.fields.get("provider_field"));
    }

    private static final class RecordingSink implements HistoricalAnrSink {
        private long occurredAtNs;
        private HashMap<String, Object> tags;
        private HashMap<String, Object> fields;
        private String dedupeKey;
        private String viewId;
        private CollectType collectType;
        private int calls;

        @Override
        public InsertResult persist(long occurredAtNs,
                                    HashMap<String, Object> tags,
                                    HashMap<String, Object> fields,
                                    String dedupeKey,
                                    String viewId,
                                    CollectType collectType) {
            calls++;
            this.occurredAtNs = occurredAtNs;
            this.tags = tags;
            this.fields = fields;
            this.dedupeKey = dedupeKey;
            this.viewId = viewId;
            this.collectType = collectType;
            return InsertResult.INSERTED;
        }
    }
}
