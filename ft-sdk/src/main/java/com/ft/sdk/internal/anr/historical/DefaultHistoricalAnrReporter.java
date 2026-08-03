package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.FTTrackInner;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.ErrorSource;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.db.InsertResult;
import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;

final class DefaultHistoricalAnrReporter implements HistoricalAnrReporter {
    private final HistoricalAnrSink sink;
    private final IssueFieldsEnricher issueFieldsEnricher;

    DefaultHistoricalAnrReporter() {
        this(new HistoricalAnrSink() {
            @Override
            public InsertResult persist(long occurredAtNs,
                                        HashMap<String, Object> tags,
                                        HashMap<String, Object> fields,
                                        String dedupeKey,
                                        String viewId,
                                        CollectType collectType) {
                return FTTrackInner.getInstance().persistHistoricalRum(
                        occurredAtNs, tags, fields, dedupeKey, viewId, collectType);
            }
        }, new IssueFieldsEnricher() {
            @Override
            public void enrich(String stack,
                               long occurredAtNs,
                               AppState state,
                               HashMap<String, Object> tags,
                               HashMap<String, Object> fields) {
                FTRUMInnerManager.get().enrichHistoricalAnrFields(
                        stack, occurredAtNs, state, tags, fields);
            }
        });
    }

    DefaultHistoricalAnrReporter(HistoricalAnrSink sink) {
        this(sink, null);
    }

    DefaultHistoricalAnrReporter(HistoricalAnrSink sink,
                                 IssueFieldsEnricher issueFieldsEnricher) {
        this.sink = sink;
        this.issueFieldsEnricher = issueFieldsEnricher;
    }

    @Override
    public ReportResult report(ProcessExitRecord exit, HistoricalRumContext context,
                               String stack, String eventDedupeKey) {
        if (context.getCollectType() == CollectType.NOT_COLLECT) {
            return ReportResult.ALREADY_EXISTS;
        }
        AppState state = exit.getImportance() >= 400
                ? AppState.BACKGROUND
                : AppState.RUN;
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_ERROR_TYPE, ErrorType.ANR_CRASH.toString());
        tags.put(Constants.KEY_RUM_ERROR_SOURCE, ErrorSource.LOGGER.toString());
        tags.put(Constants.KEY_RUM_ERROR_SITUATION, state.toString());
        tags.put(Constants.KEY_RUM_SESSION_ID, context.getSessionId());
        tags.put(Constants.KEY_RUM_VIEW_ID, context.getViewId());
        tags.put(Constants.KEY_RUM_VIEW_NAME, context.getViewName());
        tags.put("process_name", exit.getProcessName());

        HashMap<String, Object> fields = new HashMap<>();
        fields.put(Constants.KEY_RUM_ERROR_MESSAGE, "android_anr");
        fields.put(Constants.KEY_RUM_ERROR_STACK, stack);
        fields.put("historical", true);

        long occurredAtNs = exit.getTimestampMs() * 1_000_000L;
        if (issueFieldsEnricher != null) {
            issueFieldsEnricher.enrich(stack, occurredAtNs, state, tags, fields);
        }

        InsertResult result = sink.persist(
                occurredAtNs,
                tags,
                fields,
                eventDedupeKey,
                context.getViewId(),
                context.getCollectType());
        if (result == InsertResult.INSERTED) {
            return ReportResult.INSERTED;
        }
        if (result == InsertResult.ALREADY_EXISTS) {
            return ReportResult.ALREADY_EXISTS;
        }
        return ReportResult.FAILED;
    }

    @Override
    public boolean onCommitted(ReportResult result,
                               String eventDedupeKey,
                               CollectType collectType) {
        return FTTrackInner.getInstance().commitHistoricalRum(
                eventDedupeKey, collectType) != InsertResult.FAILED;
    }

    interface IssueFieldsEnricher {
        void enrich(String stack,
                    long occurredAtNs,
                    AppState state,
                    HashMap<String, Object> tags,
                    HashMap<String, Object> fields);
    }
}
