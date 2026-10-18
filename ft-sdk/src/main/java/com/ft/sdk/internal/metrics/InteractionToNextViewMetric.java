package com.ft.sdk.internal.metrics;

import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, process-local history. Interactions are ordered by accepted write callbacks. */
public final class InteractionToNextViewMetric {
    private static final int HISTORY_SIZE = 4;
    private static final long MAX_INTERVAL_NS = 3_000_000_000L;
    private final LinkedHashMap<String, Long> views = new LinkedHashMap<>();
    private final LinkedHashMap<String, Long> interactions = new LinkedHashMap<>();

    public synchronized void viewCreated(String id, long start) {
        views.put(id, start);
        trim(views);
    }

    public synchronized boolean actionStored(String viewId, String type, long timestamp) {
        if (viewId == null || !("tap".equals(type) || "swipe".equals(type)
                || "click".equals(type) || "back".equals(type))) return false;
        interactions.put(viewId, timestamp);
        trim(interactions);
        return true;
    }

    public synchronized Long resolve(String viewId) {
        String previous = null;
        for (Map.Entry<String, Long> entry : views.entrySet()) {
            if (entry.getKey().equals(viewId)) {
                Long interaction = interactions.get(previous);
                if (interaction == null) return null;
                long duration = entry.getValue() - interaction;
                return duration > 0 && duration < MAX_INTERVAL_NS ? duration : null;
            }
            previous = entry.getKey();
        }
        return null;
    }

    public synchronized void clear() {
        views.clear();
        interactions.clear();
    }

    private static void trim(LinkedHashMap<String, Long> map) {
        if (map.size() > HISTORY_SIZE) map.remove(map.keySet().iterator().next());
    }
}
