package com.ft.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;

import com.ft.sdk.FTIssueInfo;
import com.ft.sdk.garble.bean.ViewBean;

import java.util.Map;

/**
 * Persists the state of the debug-only historical ANR manual acceptance flow.
 *
 * <p>The controller, worker, and recovery consumer run in different process lifetimes, so the
 * acceptance evidence must not live only in memory.</p>
 */
public final class HistoricalAnrAcceptanceStore {
    private static final String PREFERENCES = "historical_anr_acceptance";
    private static final String KEY_ARMED = "armed";
    private static final String KEY_STAGE = "stage";
    private static final String KEY_EXPECTED_SESSION_ID = "expected_session_id";
    private static final String KEY_EXPECTED_VIEW_ID = "expected_view_id";
    private static final String KEY_EXPECTED_VIEW_NAME = "expected_view_name";
    private static final String KEY_EXPECTED_PROCESS_NAME = "expected_process_name";
    private static final String KEY_EXPECTED_PROCESS_RUN_ID = "expected_process_run_id";
    private static final String KEY_TRIGGERED_AT_MS = "triggered_at_ms";
    private static final String KEY_HISTORICAL_ISSUE_COUNT = "historical_issue_count";
    private static final String KEY_ISSUE_ERROR_TYPE = "issue_error_type";
    private static final String KEY_ISSUE_OCCURRED_AT_NS = "issue_occurred_at_ns";
    private static final String KEY_PAYLOAD_COUNT = "payload_count";
    private static final String KEY_PAYLOAD_SESSION_ID = "payload_session_id";
    private static final String KEY_PAYLOAD_VIEW_ID = "payload_view_id";
    private static final String KEY_PAYLOAD_VIEW_NAME = "payload_view_name";
    private static final String KEY_PAYLOAD_PROCESS_NAME = "payload_process_name";

    public static final String ACTION_HISTORICAL_ISSUE_OBSERVED =
            "com.ft.historical_anr.acceptance.ISSUE_OBSERVED";
    public static final String ACTION_HISTORICAL_PAYLOAD_OBSERVED =
            "com.ft.historical_anr.acceptance.PAYLOAD_OBSERVED";
    public static final String ACTION_WORKER_VIEW_READY =
            "com.ft.historical_anr.acceptance.WORKER_VIEW_READY";
    public static final String ACTION_ANR_TRIGGERED =
            "com.ft.historical_anr.acceptance.ANR_TRIGGERED";
    public static final String ACTION_STATE_CHANGED =
            "com.ft.historical_anr.acceptance.STATE_CHANGED";
    public static final String EXTRA_ISSUE_ERROR_TYPE = "issue_error_type";
    public static final String EXTRA_ISSUE_OCCURRED_AT_NS = "issue_occurred_at_ns";
    public static final String EXTRA_SOURCE_PID = "source_pid";
    public static final String EXTRA_EXPECTED_SESSION_ID = "expected_session_id";
    public static final String EXTRA_EXPECTED_VIEW_ID = "expected_view_id";
    public static final String EXTRA_EXPECTED_VIEW_NAME = "expected_view_name";
    public static final String EXTRA_EXPECTED_PROCESS_NAME = "expected_process_name";
    public static final String EXTRA_EXPECTED_PROCESS_RUN_ID = "expected_process_run_id";
    public static final String EXTRA_TRIGGERED_AT_MS = "triggered_at_ms";
    public static final String EXTRA_PAYLOAD_SESSION_ID = "payload_session_id";
    public static final String EXTRA_PAYLOAD_VIEW_ID = "payload_view_id";
    public static final String EXTRA_PAYLOAD_VIEW_NAME = "payload_view_name";
    public static final String EXTRA_PAYLOAD_PROCESS_NAME = "payload_process_name";

    public static final String STAGE_IDLE = "idle";
    public static final String STAGE_STARTING_WORKER = "starting_worker";
    public static final String STAGE_WORKER_READY = "worker_ready";
    public static final String STAGE_ANR_TRIGGERED = "anr_triggered";
    public static final String STAGE_CONSUMER_STARTED = "consumer_started";
    public static final String STAGE_HISTORICAL_ISSUE_OBSERVED =
            "historical_issue_observed";

    private HistoricalAnrAcceptanceStore() {
    }

    public static void begin(Context context) {
        preferences(context).edit()
                .clear()
                .putString(KEY_STAGE, STAGE_STARTING_WORKER)
                .commit();
    }

    public static void recordWorkerViewAndArm(Context context, ViewBean view) {
        persistWorkerView(context,
                view.getSessionId(),
                view.getId(),
                view.getViewName(),
                view.getProcessName(),
                view.getProcessRunId());
        context.sendBroadcast(new Intent(ACTION_WORKER_VIEW_READY)
                .setPackage(context.getPackageName())
                .putExtra(EXTRA_EXPECTED_SESSION_ID, view.getSessionId())
                .putExtra(EXTRA_EXPECTED_VIEW_ID, view.getId())
                .putExtra(EXTRA_EXPECTED_VIEW_NAME, view.getViewName())
                .putExtra(EXTRA_EXPECTED_PROCESS_NAME, view.getProcessName())
                .putExtra(EXTRA_EXPECTED_PROCESS_RUN_ID, view.getProcessRunId())
                .putExtra(EXTRA_SOURCE_PID, Process.myPid()));
    }

    public static void mergeWorkerView(Context context, Intent intent) {
        persistWorkerView(context,
                intent.getStringExtra(EXTRA_EXPECTED_SESSION_ID),
                intent.getStringExtra(EXTRA_EXPECTED_VIEW_ID),
                intent.getStringExtra(EXTRA_EXPECTED_VIEW_NAME),
                intent.getStringExtra(EXTRA_EXPECTED_PROCESS_NAME),
                intent.getStringExtra(EXTRA_EXPECTED_PROCESS_RUN_ID));
    }

    private static void persistWorkerView(Context context,
                                          String sessionId,
                                          String viewId,
                                          String viewName,
                                          String processName,
                                          String processRunId) {
        preferences(context).edit()
                .putBoolean(KEY_ARMED, true)
                .putString(KEY_STAGE, STAGE_WORKER_READY)
                .putString(KEY_EXPECTED_SESSION_ID, sessionId)
                .putString(KEY_EXPECTED_VIEW_ID, viewId)
                .putString(KEY_EXPECTED_VIEW_NAME, viewName)
                .putString(KEY_EXPECTED_PROCESS_NAME, processName)
                .putString(KEY_EXPECTED_PROCESS_RUN_ID, processRunId)
                .commit();
    }

    public static void markAnrTriggered(Context context) {
        long triggeredAtMs = System.currentTimeMillis();
        preferences(context).edit()
                .putString(KEY_STAGE, STAGE_ANR_TRIGGERED)
                .putLong(KEY_TRIGGERED_AT_MS, triggeredAtMs)
                .commit();
        context.sendBroadcast(new Intent(ACTION_ANR_TRIGGERED)
                .setPackage(context.getPackageName())
                .putExtra(EXTRA_TRIGGERED_AT_MS, triggeredAtMs)
                .putExtra(EXTRA_SOURCE_PID, Process.myPid()));
    }

    public static void mergeAnrTriggered(Context context, Intent intent) {
        preferences(context).edit()
                .putString(KEY_STAGE, STAGE_ANR_TRIGGERED)
                .putLong(KEY_TRIGGERED_AT_MS,
                        intent.getLongExtra(EXTRA_TRIGGERED_AT_MS, 0))
                .commit();
    }

    public static void markConsumerStarted(Context context) {
        SharedPreferences preferences = preferences(context);
        if (preferences.getInt(KEY_HISTORICAL_ISSUE_COUNT, 0) == 0) {
            preferences.edit()
                    .putString(KEY_STAGE, STAGE_CONSUMER_STARTED)
                    .commit();
        }
    }

    public static void recordHistoricalIssue(Context context, FTIssueInfo issue) {
        if (!issue.isHistorical() || !"anr_crash".equals(issue.getErrorType())) {
            return;
        }
        SharedPreferences preferences = preferences(context);
        if (!preferences.getBoolean(KEY_ARMED, false)) {
            return;
        }
        context.sendBroadcast(new Intent(ACTION_HISTORICAL_ISSUE_OBSERVED)
                .setPackage(context.getPackageName())
                .putExtra(EXTRA_ISSUE_ERROR_TYPE, issue.getErrorType())
                .putExtra(EXTRA_ISSUE_OCCURRED_AT_NS, issue.getOccurredAtNanoseconds())
                .putExtra(EXTRA_SOURCE_PID, Process.myPid()));
    }

    public static void mergeObservedIssue(Context context, Intent intent) {
        SharedPreferences preferences = preferences(context);
        if (!preferences.getBoolean(KEY_ARMED, false)) {
            return;
        }
        int mergedCount = preferences.getInt(KEY_HISTORICAL_ISSUE_COUNT, 0) + 1;
        preferences.edit()
                .putString(KEY_STAGE, STAGE_HISTORICAL_ISSUE_OBSERVED)
                .putInt(KEY_HISTORICAL_ISSUE_COUNT, mergedCount)
                .putString(KEY_ISSUE_ERROR_TYPE,
                        intent.getStringExtra(EXTRA_ISSUE_ERROR_TYPE))
                .putLong(KEY_ISSUE_OCCURRED_AT_NS,
                        intent.getLongExtra(EXTRA_ISSUE_OCCURRED_AT_NS, 0))
                .commit();
    }

    public static void recordHistoricalPayload(Context context,
                                               String measurement,
                                               Map<String, Object> data) {
        if (!"error".equals(measurement)
                || !"anr_crash".equals(String.valueOf(data.get("error_type")))
                || !Boolean.TRUE.equals(data.get("demo_issue_historical"))) {
            return;
        }
        if (!preferences(context).getBoolean(KEY_ARMED, false)) {
            return;
        }
        context.sendBroadcast(new Intent(ACTION_HISTORICAL_PAYLOAD_OBSERVED)
                .setPackage(context.getPackageName())
                .putExtra(EXTRA_PAYLOAD_SESSION_ID, stringValue(data.get("session_id")))
                .putExtra(EXTRA_PAYLOAD_VIEW_ID, stringValue(data.get("view_id")))
                .putExtra(EXTRA_PAYLOAD_VIEW_NAME, stringValue(data.get("view_name")))
                .putExtra(EXTRA_PAYLOAD_PROCESS_NAME, stringValue(data.get("process_name")))
                .putExtra(EXTRA_SOURCE_PID, Process.myPid()));
    }

    public static void mergeObservedPayload(Context context, Intent intent) {
        SharedPreferences preferences = preferences(context);
        if (!preferences.getBoolean(KEY_ARMED, false)) {
            return;
        }
        preferences.edit()
                .putInt(KEY_PAYLOAD_COUNT, preferences.getInt(KEY_PAYLOAD_COUNT, 0) + 1)
                .putString(KEY_PAYLOAD_SESSION_ID,
                        intent.getStringExtra(EXTRA_PAYLOAD_SESSION_ID))
                .putString(KEY_PAYLOAD_VIEW_ID,
                        intent.getStringExtra(EXTRA_PAYLOAD_VIEW_ID))
                .putString(KEY_PAYLOAD_VIEW_NAME,
                        intent.getStringExtra(EXTRA_PAYLOAD_VIEW_NAME))
                .putString(KEY_PAYLOAD_PROCESS_NAME,
                        intent.getStringExtra(EXTRA_PAYLOAD_PROCESS_NAME))
                .commit();
    }

    public static void reset(Context context) {
        preferences(context).edit()
                .clear()
                .putString(KEY_STAGE, STAGE_IDLE)
                .commit();
    }

    public static Snapshot snapshot(Context context) {
        SharedPreferences preferences = preferences(context);
        return new Snapshot(
                preferences.getString(KEY_STAGE, STAGE_IDLE),
                preferences.getString(KEY_EXPECTED_SESSION_ID, null),
                preferences.getString(KEY_EXPECTED_VIEW_ID, null),
                preferences.getString(KEY_EXPECTED_VIEW_NAME, null),
                preferences.getString(KEY_EXPECTED_PROCESS_NAME, null),
                preferences.getString(KEY_EXPECTED_PROCESS_RUN_ID, null),
                preferences.getLong(KEY_TRIGGERED_AT_MS, 0),
                preferences.getInt(KEY_HISTORICAL_ISSUE_COUNT, 0),
                preferences.getString(KEY_ISSUE_ERROR_TYPE, null),
                preferences.getLong(KEY_ISSUE_OCCURRED_AT_NS, 0),
                preferences.getInt(KEY_PAYLOAD_COUNT, 0),
                preferences.getString(KEY_PAYLOAD_SESSION_ID, null),
                preferences.getString(KEY_PAYLOAD_VIEW_ID, null),
                preferences.getString(KEY_PAYLOAD_VIEW_NAME, null),
                preferences.getString(KEY_PAYLOAD_PROCESS_NAME, null));
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static final class Snapshot {
        public final String stage;
        public final String expectedSessionId;
        public final String expectedViewId;
        public final String expectedViewName;
        public final String expectedProcessName;
        public final String expectedProcessRunId;
        public final long triggeredAtMs;
        public final int historicalIssueCount;
        public final String issueErrorType;
        public final long issueOccurredAtNs;
        public final int payloadCount;
        public final String payloadSessionId;
        public final String payloadViewId;
        public final String payloadViewName;
        public final String payloadProcessName;

        Snapshot(String stage,
                 String expectedSessionId,
                 String expectedViewId,
                 String expectedViewName,
                 String expectedProcessName,
                 String expectedProcessRunId,
                 long triggeredAtMs,
                 int historicalIssueCount,
                 String issueErrorType,
                 long issueOccurredAtNs,
                 int payloadCount,
                 String payloadSessionId,
                 String payloadViewId,
                 String payloadViewName,
                 String payloadProcessName) {
            this.stage = stage;
            this.expectedSessionId = expectedSessionId;
            this.expectedViewId = expectedViewId;
            this.expectedViewName = expectedViewName;
            this.expectedProcessName = expectedProcessName;
            this.expectedProcessRunId = expectedProcessRunId;
            this.triggeredAtMs = triggeredAtMs;
            this.historicalIssueCount = historicalIssueCount;
            this.issueErrorType = issueErrorType;
            this.issueOccurredAtNs = issueOccurredAtNs;
            this.payloadCount = payloadCount;
            this.payloadSessionId = payloadSessionId;
            this.payloadViewId = payloadViewId;
            this.payloadViewName = payloadViewName;
            this.payloadProcessName = payloadProcessName;
        }
    }
}
