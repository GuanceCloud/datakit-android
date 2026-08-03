package com.ft.sdk.internal.anr.historical;

import android.content.Context;

import java.util.List;

final class V4MigratingHistoricalRumContextStore implements HistoricalRumContextStore {
    private final Context context;
    private final HistoricalRumContextStore delegate;
    private boolean migrationComplete;

    V4MigratingHistoricalRumContextStore(
            Context context, HistoricalRumContextStore delegate) {
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext == null ? context : applicationContext;
        this.delegate = delegate;
    }

    @Override
    public synchronized void ensureReady() throws Exception {
        if (!migrationComplete) {
            try {
                HistoricalAnrV4Migration.ensureDatabaseMigrated(context);
            } catch (Exception e) {
                throw new HistoricalAnrMigrationException(e);
            }
            migrationComplete = true;
        }
    }

    @Override
    public List<HistoricalRumContext> load(String processName) throws Exception {
        ensureReady();
        return delegate.load(processName);
    }
}
