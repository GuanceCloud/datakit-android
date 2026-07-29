package com.ft;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.internal.anr.historical.ProcessRunIdentity;
import com.ft.utils.HistoricalAnrAcceptanceStore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs in {@code :anr_worker}, persists an old RUM View, and causes a real input-dispatch ANR.
 */
public class HistoricalAnrWorkerActivity extends AppCompatActivity {
    private static final String VIEW_NAME = "HistoricalAnrManualWorker";
    private static final long VIEW_WAIT_TIMEOUT_MS = 10_000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView statusView;
    private Button triggerButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_anr_worker);
        setTitle(R.string.historical_anr_worker_title);
        statusView = findViewById(R.id.historical_anr_worker_status);
        triggerButton = findViewById(R.id.historical_anr_trigger);
        triggerButton.setEnabled(false);
        triggerButton.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
                return false;
            }
            view.performClick();
            triggerFatalAnr();
            return true;
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FTRUMGlobalManager.get().startView(VIEW_NAME);
                waitForPersistedWorkerView();
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
        super.onDestroy();
    }

    private void waitForPersistedWorkerView() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ProcessRunIdentity identity = ProcessRunIdentity.get();
                long deadline = System.currentTimeMillis() + VIEW_WAIT_TIMEOUT_MS;
                ViewBean matched = null;
                while (matched == null && System.currentTimeMillis() < deadline) {
                    List<ViewBean> views = FTDBManager.get().querySumView(0, true);
                    for (ViewBean view : views) {
                        if (identity != null
                                && identity.getProcessRunId().equals(view.getProcessRunId())
                                && VIEW_NAME.equals(view.getViewName())) {
                            matched = view;
                            break;
                        }
                    }
                    if (matched == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                final ViewBean workerView = matched;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onWorkerViewReady(workerView);
                    }
                });
            }
        });
    }

    private void onWorkerViewReady(ViewBean workerView) {
        if (workerView == null) {
            statusView.setText(R.string.historical_anr_worker_view_failed);
            return;
        }
        HistoricalAnrAcceptanceStore.recordWorkerViewAndArm(this, workerView);
        statusView.setText("Worker View persisted\n"
                + "process: " + workerView.getProcessName() + '\n'
                + "processRunId: " + workerView.getProcessRunId() + '\n'
                + "sessionId: " + workerView.getSessionId() + '\n'
                + "viewId: " + workerView.getId() + '\n'
                + "viewName: " + workerView.getViewName() + "\n\n"
                + getString(R.string.historical_anr_worker_ready));
        triggerButton.setEnabled(true);
    }

    private void triggerFatalAnr() {
        HistoricalAnrAcceptanceStore.markAnrTriggered(this);
        triggerButton.setEnabled(false);
        blockMainThread();
    }

    private void blockMainThread() {
        Object blocker = new Object();
        synchronized (blocker) {
            while (true) {
                try {
                    blocker.wait();
                } catch (InterruptedException ignored) {
                    // Keep the UI event unfinished until Android records and terminates the ANR.
                }
            }
        }
    }
}
