package com.ft;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.service.HistoricalAnrConsumerService;
import com.ft.service.HistoricalAnrConsumerServiceB;
import com.ft.utils.HistoricalAnrAcceptanceStore;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controller and result page for manual ApplicationExitInfo ANR acceptance.
 */
public class HistoricalAnrAcceptanceActivity extends AppCompatActivity {
    private TextView statusView;
    private boolean receiverRegistered;

    private final BroadcastReceiver acceptanceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            renderStatus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_anr_acceptance);
        setTitle(R.string.historical_anr_acceptance_title);
        statusView = findViewById(R.id.historical_anr_acceptance_status);
        findViewById(R.id.historical_anr_start).setOnClickListener(view -> startScenario());
        findViewById(R.id.historical_anr_consume).setOnClickListener(view -> startConsumer());
        findViewById(R.id.historical_anr_refresh).setOnClickListener(view -> renderStatus());
        findViewById(R.id.historical_anr_reset).setOnClickListener(view -> {
            HistoricalAnrAcceptanceStore.reset(this);
            renderStatus();
        });
        registerIssueObservedReceiver();
        renderStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderStatus();
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(acceptanceReceiver);
        }
        super.onDestroy();
    }

    private void startScenario() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            statusView.setText(R.string.historical_anr_requires_api_30);
            return;
        }
        HistoricalAnrAcceptanceStore.begin(this);
        renderStatus();
        startActivity(new Intent(this, HistoricalAnrWorkerActivity.class));
    }

    private void startConsumer() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            statusView.setText(R.string.historical_anr_requires_api_30);
            return;
        }
        HistoricalAnrAcceptanceStore.markConsumerStarted(this);
        startService(new Intent(this, HistoricalAnrConsumerService.class));
        startService(new Intent(this, HistoricalAnrConsumerServiceB.class));
        renderStatus();
    }

    private void registerIssueObservedReceiver() {
        IntentFilter filter = new IntentFilter(
                HistoricalAnrAcceptanceStore.ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(acceptanceReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(acceptanceReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void renderStatus() {
        HistoricalAnrAcceptanceStore.Snapshot snapshot =
                HistoricalAnrAcceptanceStore.snapshot(this);
        ExitEvidence exitEvidence = findExitEvidence(snapshot);
        LocalEvidence localEvidence = findLocalEvidence(snapshot);

        StringBuilder text = new StringBuilder();
        text.append("Android API: ").append(Build.VERSION.SDK_INT).append('\n');
        text.append("Stage: ").append(snapshot.stage).append("\n\n");
        text.append("Expected old RUM context\n");
        text.append("process: ").append(value(snapshot.expectedProcessName)).append('\n');
        text.append("processRunId: ").append(value(snapshot.expectedProcessRunId)).append('\n');
        text.append("sessionId: ").append(value(snapshot.expectedSessionId)).append('\n');
        text.append("viewId: ").append(value(snapshot.expectedViewId)).append('\n');
        text.append("viewName: ").append(value(snapshot.expectedViewName)).append("\n\n");
        text.append("System exit evidence\n");
        text.append(exitEvidence.description).append("\n\n");
        text.append("SDK recovery evidence\n");
        text.append("historical provider calls: ")
                .append(snapshot.historicalIssueCount).append(" (expected: 1)\n");
        text.append("emitted historical payloads: ")
                .append(snapshot.payloadCount).append(" (expected: 1)\n");
        text.append("errorType: ").append(value(snapshot.issueErrorType)).append('\n');
        text.append("occurredAtNs: ").append(snapshot.issueOccurredAtNs).append('\n');
        text.append("provider timestamp matches system exit: ")
                .append(issueMatchesExit(snapshot, exitEvidence)).append('\n');
        text.append("payload sessionId: ").append(value(snapshot.payloadSessionId)).append('\n');
        text.append("payload viewId: ").append(value(snapshot.payloadViewId)).append('\n');
        text.append("payload process: ").append(value(snapshot.payloadProcessName)).append('\n');
        text.append("payload matches expected old context: ")
                .append(payloadMatchesExpected(snapshot)).append('\n');
        text.append("local cached matching errors: ")
                .append(localEvidence.matchingCount).append('\n');
        text.append("local cached historical errors: ")
                .append(localEvidence.totalCount).append("\n\n");
        text.append(verdict(snapshot, exitEvidence, localEvidence));
        statusView.setText(text.toString());
    }

    private ExitEvidence findExitEvidence(HistoricalAnrAcceptanceStore.Snapshot snapshot) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return new ExitEvidence(false, "Unavailable below API 30");
        }
        if (snapshot.expectedProcessName == null || snapshot.triggeredAtMs == 0) {
            return new ExitEvidence(false, "No acceptance scenario has triggered an ANR yet");
        }
        return findExitEvidenceApi30(snapshot);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private ExitEvidence findExitEvidenceApi30(
            HistoricalAnrAcceptanceStore.Snapshot snapshot) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ApplicationExitInfo> exits =
                manager.getHistoricalProcessExitReasons(getPackageName(), 0, 64);
        ApplicationExitInfo matched = null;
        for (ApplicationExitInfo exit : exits) {
            if (exit.getReason() != ApplicationExitInfo.REASON_ANR) {
                continue;
            }
            if (snapshot.expectedProcessName != null
                    && !snapshot.expectedProcessName.equals(exit.getProcessName())) {
                continue;
            }
            if (snapshot.triggeredAtMs > 0
                    && exit.getTimestamp() + 5_000 < snapshot.triggeredAtMs) {
                continue;
            }
            if (matched == null || exit.getTimestamp() > matched.getTimestamp()) {
                matched = exit;
            }
        }
        if (matched != null) {
            String time = DateFormat.getDateTimeInstance().format(
                    new Date(matched.getTimestamp()));
            return new ExitEvidence(true,
                    "ANR found: process=" + matched.getProcessName()
                            + ", pid=" + matched.getPid()
                            + ", time=" + time,
                    matched.getTimestamp());
        }
        return new ExitEvidence(false, "No matching ApplicationExitInfo ANR yet");
    }

    private LocalEvidence findLocalEvidence(
            HistoricalAnrAcceptanceStore.Snapshot snapshot) {
        int total = 0;
        int matching = 0;
        try {
            List<SyncData> records = FTDBManager.get().queryDataByDescLimit(0);
            for (SyncData record : records) {
                String dedupeKey = record.getDedupeKey();
                if (dedupeKey == null || !dedupeKey.startsWith("historical-anr:v1:")) {
                    continue;
                }
                total++;
                String data = record.getDataString();
                if (data != null
                        && snapshot.expectedSessionId != null
                        && snapshot.expectedViewId != null
                        && data.contains(snapshot.expectedSessionId)
                        && data.contains(snapshot.expectedViewId)) {
                    matching++;
                }
            }
        } catch (RuntimeException ignored) {
            // The final event may already be syncing; the provider callback remains observable.
        }
        return new LocalEvidence(total, matching);
    }

    private String verdict(HistoricalAnrAcceptanceStore.Snapshot snapshot,
                           ExitEvidence exitEvidence,
                           LocalEvidence localEvidence) {
        if (snapshot.historicalIssueCount > 1) {
            return "FAIL: the historical ANR provider was invoked more than once.";
        }
        if (snapshot.payloadCount > 1) {
            return "FAIL: more than one historical ANR payload was emitted.";
        }
        if (snapshot.historicalIssueCount == 1 && exitEvidence.found) {
            if (!issueMatchesExit(snapshot, exitEvidence)) {
                return "FAIL: the provider callback timestamp does not match the system ANR.";
            }
            if (snapshot.payloadCount == 0) {
                return "PENDING: provider callback observed; final Error payload not observed yet.";
            }
            if (!payloadMatchesExpected(snapshot)) {
                return "FAIL: the emitted Error is not bound to the expected old worker context.";
            }
            if (localEvidence.matchingCount == 1) {
                return "PASS: one historical ANR is cached and bound to the old worker View.";
            }
            return "PASS: one historical ANR payload was emitted with the old worker context. "
                    + "The local record has already entered sync.";
        }
        if (exitEvidence.found) {
            return "PENDING: Android recorded the ANR, but SDK recovery has not been observed.";
        }
        return "PENDING: start the scenario, close the worker from the Android ANR dialog, "
                + "then relaunch the App.";
    }

    private boolean issueMatchesExit(HistoricalAnrAcceptanceStore.Snapshot snapshot,
                                     ExitEvidence exitEvidence) {
        return exitEvidence.found
                && snapshot.issueOccurredAtNs == exitEvidence.timestampMs * 1_000_000L;
    }

    private boolean payloadMatchesExpected(HistoricalAnrAcceptanceStore.Snapshot snapshot) {
        return snapshot.expectedSessionId != null
                && snapshot.expectedSessionId.equals(snapshot.payloadSessionId)
                && snapshot.expectedViewId != null
                && snapshot.expectedViewId.equals(snapshot.payloadViewId)
                && snapshot.expectedViewName != null
                && snapshot.expectedViewName.equals(snapshot.payloadViewName)
                && snapshot.expectedProcessName != null
                && snapshot.expectedProcessName.equals(snapshot.payloadProcessName);
    }

    private String value(String value) {
        return value == null || value.length() == 0 ? "-" : value;
    }

    private static final class ExitEvidence {
        final boolean found;
        final String description;
        final long timestampMs;

        ExitEvidence(boolean found, String description) {
            this(found, description, 0);
        }

        ExitEvidence(boolean found, String description, long timestampMs) {
            this.found = found;
            this.description = description;
            this.timestampMs = timestampMs;
        }
    }

    private static final class LocalEvidence {
        final int totalCount;
        final int matchingCount;

        LocalEvidence(int totalCount, int matchingCount) {
            this.totalCount = totalCount;
            this.matchingCount = matchingCount;
        }
    }
}
