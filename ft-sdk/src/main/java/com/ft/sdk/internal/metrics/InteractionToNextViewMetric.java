package com.ft.sdk.internal.metrics;

import com.ft.sdk.FTLastInteractionIdentifier;
import com.ft.sdk.FTPreviousViewLastInteractionContext;
import com.ft.sdk.FTTimeBasedInteractionIdentifier;
import com.ft.sdk.garble.utils.Constants;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Bounded history with cached results. Interactions are ordered by accepted write callbacks. */
public final class InteractionToNextViewMetric {
    private static final int HISTORY_SIZE = 4;
    private final FTLastInteractionIdentifier identifier;
    private final LinkedHashMap<String, ViewState> views = new LinkedHashMap<>();
    private final AtomicBoolean callbackFailureReported = new AtomicBoolean();

    public InteractionToNextViewMetric() {
        this(new FTTimeBasedInteractionIdentifier());
    }

    public InteractionToNextViewMetric(FTLastInteractionIdentifier identifier) {
        this.identifier = identifier;
    }

    public void viewCreated(String id, long start) {
        if (identifier == null) return;
        ViewState target;
        Interaction candidate;
        synchronized (this) {
            if (views.containsKey(id)) return;
            String previous = null;
            for (String viewId : views.keySet()) previous = viewId;
            ViewState source = views.get(previous);
            target = new ViewState(id, start, previous);
            candidate = source == null ? null : source.lastInteraction;
            target.candidate = candidate;
            views.put(id, target);
            if (views.size() > HISTORY_SIZE) views.remove(views.keySet().iterator().next());
        }
        evaluate(target, candidate);
    }

    public boolean actionStored(String viewId, String type, long timestamp) {
        // Automatic clicks and the back key both use action_type=click.
        if (identifier == null || viewId == null || !Constants.EVENT_NAME_CLICK.equals(type)) return false;
        ViewState target = null;
        Interaction candidate = new Interaction(type, timestamp);
        synchronized (this) {
            ViewState source = views.get(viewId);
            if (source == null) return false;
            source.lastInteraction = candidate;
            for (ViewState view : views.values()) {
                if (viewId.equals(view.previousId)) {
                    target = view;
                    target.candidate = candidate;
                    target.value = null;
                    break;
                }
            }
        }
        if (target != null) evaluate(target, candidate);
        return true;
    }

    /** Reads a cached value only; serialization and history eviction never invoke user callbacks. */
    public synchronized Long resolve(String viewId) {
        ViewState view = views.get(viewId);
        return view == null ? null : view.value;
    }

    public synchronized void clear() {
        views.clear();
    }

    private void evaluate(ViewState target, Interaction candidate) {
        if (candidate == null) return;
        boolean accepted = false;
        try {
            accepted = identifier.validate(new FTPreviousViewLastInteractionContext(candidate.type,
                    candidate.timestamp, target.start));
        } catch (Throwable failure) {
            ViewTimingCallback.failed(failure, callbackFailureReported);
        }
        long duration = target.start - candidate.timestamp;
        synchronized (this) {
            // Reentrant callbacks, concurrent candidates and SDK shutdown cannot restore stale results.
            if (views.get(target.id) == target && target.candidate == candidate) {
                target.value = accepted && duration > 0 ? duration : null;
            }
        }
    }

    private static final class ViewState {
        final String id;
        final long start;
        final String previousId;
        Interaction lastInteraction;
        Interaction candidate;
        Long value;

        ViewState(String id, long start, String previousId) {
            this.id = id;
            this.start = start;
            this.previousId = previousId;
        }
    }

    private static final class Interaction {
        final String type;
        final long timestamp;

        Interaction(String type, long timestamp) {
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
