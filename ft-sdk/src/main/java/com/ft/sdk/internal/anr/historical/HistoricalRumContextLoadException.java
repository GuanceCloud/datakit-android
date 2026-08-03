package com.ft.sdk.internal.anr.historical;

import java.util.Collections;
import java.util.List;

final class HistoricalRumContextLoadException extends Exception {
    private final List<HistoricalRumContext> loadedContexts;

    HistoricalRumContextLoadException(List<HistoricalRumContext> loadedContexts,
                                      Throwable cause) {
        super(cause);
        this.loadedContexts = Collections.unmodifiableList(loadedContexts);
    }

    List<HistoricalRumContext> getLoadedContexts() {
        return loadedContexts;
    }
}
