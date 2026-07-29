package com.ft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ft.utils.HistoricalAnrAcceptanceStore;

/**
 * Serializes acceptance evidence from SDK processes into the main process.
 */
public class HistoricalAnrAcceptanceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (HistoricalAnrAcceptanceStore.ACTION_WORKER_VIEW_READY.equals(action)) {
            HistoricalAnrAcceptanceStore.mergeWorkerView(context, intent);
        } else if (HistoricalAnrAcceptanceStore.ACTION_ANR_TRIGGERED.equals(action)) {
            HistoricalAnrAcceptanceStore.mergeAnrTriggered(context, intent);
        } else if (HistoricalAnrAcceptanceStore.ACTION_HISTORICAL_ISSUE_OBSERVED.equals(action)) {
            HistoricalAnrAcceptanceStore.mergeObservedIssue(context, intent);
        } else if (HistoricalAnrAcceptanceStore.ACTION_HISTORICAL_PAYLOAD_OBSERVED.equals(action)) {
            HistoricalAnrAcceptanceStore.mergeObservedPayload(context, intent);
        } else {
            return;
        }
        context.sendBroadcast(new Intent(HistoricalAnrAcceptanceStore.ACTION_STATE_CHANGED)
                .setPackage(context.getPackageName()));
    }
}
