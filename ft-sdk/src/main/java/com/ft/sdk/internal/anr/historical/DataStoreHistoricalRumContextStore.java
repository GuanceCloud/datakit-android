package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDataStore;

import java.util.ArrayList;
import java.util.List;

final class DataStoreHistoricalRumContextStore implements HistoricalRumContextStore {
    private final FTDataStore dataStore;

    DataStoreHistoricalRumContextStore(FTDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<HistoricalRumContext> load(String processName) {
        ArrayList<HistoricalRumContext> contexts = new ArrayList<>();
        for (ViewBean view : dataStore.querySumView(0, true)) {
            if (processName == null || !processName.equals(view.getProcessName())) {
                continue;
            }
            contexts.add(new HistoricalRumContext(
                    view.getProcessName(),
                    view.getProcessRunId(),
                    view.getProcessStartMs(),
                    view.getSessionId(),
                    view.getId(),
                    view.getViewName(),
                    view.getStartTime(),
                    view.getTimeSpent(),
                    view.isClose(),
                    view.getCollectType()));
        }
        return contexts;
    }
}
