package com.ft;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;

/** Real Activity lifecycle and loopback HTTP requests for network_settled_time. */
public class NetworkSettledTimingActivity extends Activity {
    public static final String EXTRA_SCENARIO = "network_settled_scenario";
    public static final String INITIAL = "initial";
    public static final String LATE_ONLY = "late_only";
    public static final String LEAVE_EARLY = "leave_early";
    private static final String TAG = "NetworkSettledSample";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final Runnable leave = this::finish;
    private NetworkSettledSampleRequests requests;
    private TextView status;
    private String scenario;
    private boolean started;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_network_settled_timing);
        setTitle(R.string.network_settled_title);
        status = findViewById(R.id.network_settled_status);
        scenario = getIntent().getStringExtra(EXTRA_SCENARIO);
        if (!LATE_ONLY.equals(scenario) && !LEAVE_EARLY.equals(scenario)) scenario = INITIAL;
        TextView expectation = findViewById(R.id.network_settled_expectation);
        expectation.setText(LATE_ONLY.equals(scenario) ? R.string.network_settled_expect_late
                : LEAVE_EARLY.equals(scenario) ? R.string.network_settled_expect_leave
                : R.string.network_settled_expect_initial);
        findViewById(R.id.network_settled_close).setOnClickListener(v -> finish());
        try {
            // Prepare the server and executor before the automatically tracked View begins.
            requests = new NetworkSettledSampleRequests(message -> main.post(() -> {
                if (!isDestroyed()) {
                    status.append("\n" + message);
                    LogUtils.d(TAG, message);
                }
            }));
        } catch (IOException error) {
            status.setText(getString(R.string.network_settled_start_failed, error.toString()));
        }
    }

    @Override protected void onPostResume() {
        super.onPostResume();
        if (started || requests == null) return;
        started = true;
        FTRUMConfig config = FTRUMConfigManager.get().getConfig();
        if (!config.isRumEnable() || !config.isEnableTraceUserView()
                || !config.isEnableTraceUserResource()) {
            status.setText(R.string.network_settled_collection_disabled);
            requests.close();
            return;
        }
        // onActivityResumed has already registered the actual page. Do not manufacture a View
        // timestamp or set network_settled_time manually.
        String viewId = FTRUMInnerManager.get().getViewId();
        if (viewId == null) {
            status.setText(R.string.network_settled_collection_disabled);
            requests.close();
            return;
        }
        requests.start(scenario);
        status.setText(getString(R.string.network_settled_started, scenario, viewId));
        LogUtils.d(TAG, "scenario=" + scenario + ", view_id=" + viewId);
        if (LEAVE_EARLY.equals(scenario)) main.postDelayed(leave, 450);
    }

    @Override protected void onPause() {
        main.removeCallbacks(leave);
        // Do not start a delayed request on the next page if the user leaves early.
        if (requests != null) requests.cancelPendingStart();
        super.onPause();
    }

    @Override protected void onDestroy() {
        main.removeCallbacksAndMessages(null);
        if (requests != null) {
            // Already dispatched requests finish naturally, so the early-leave case also
            // exercises original View ownership after destruction. The run has an 8 s deadline.
            requests.detach();
            if (!started) requests.close();
        }
        super.onDestroy();
    }
}
