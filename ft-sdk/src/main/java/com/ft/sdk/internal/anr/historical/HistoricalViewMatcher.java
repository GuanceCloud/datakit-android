package com.ft.sdk.internal.anr.historical;

import java.util.ArrayList;
import java.util.List;

final class HistoricalViewMatcher {
    private HistoricalViewMatcher() {
    }

    static HistoricalRumContext match(List<HistoricalRumContext> views,
                                      ProcessExitRecord exit,
                                      String currentProcessRunId,
                                      long clockToleranceMs,
                                      long maxDistanceMs) {
        if (views == null || exit == null || exit.getProcessName() == null) {
            return null;
        }
        ArrayList<HistoricalRumContext> eligible = new ArrayList<>();
        for (HistoricalRumContext view : views) {
            if (isEligible(view, exit, currentProcessRunId, clockToleranceMs)) {
                eligible.add(view);
            }
        }

        HistoricalRumContext open = latestUnique(eligible, false, exit, clockToleranceMs);
        if (open != null || hasAmbiguousLatest(eligible, false, exit, clockToleranceMs)) {
            return open;
        }

        HistoricalRumContext containing = latestUnique(eligible, true, exit, clockToleranceMs);
        if (containing != null || hasAmbiguousLatest(eligible, true, exit, clockToleranceMs)) {
            return containing;
        }

        HistoricalRumContext nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        boolean ambiguous = false;
        for (HistoricalRumContext view : eligible) {
            long startMs = view.getViewStartMs();
            if (!view.isViewClosed() || startMs > exit.getTimestampMs()) {
                continue;
            }
            long distance = Math.max(0, exit.getTimestampMs() - view.getViewEndMs());
            if (distance > maxDistanceMs) {
                continue;
            }
            if (distance < nearestDistance) {
                nearest = view;
                nearestDistance = distance;
                ambiguous = false;
            } else if (distance == nearestDistance) {
                ambiguous = true;
            }
        }
        return ambiguous ? null : nearest;
    }

    private static boolean isEligible(HistoricalRumContext view,
                                      ProcessExitRecord exit,
                                      String currentProcessRunId,
                                      long toleranceMs) {
        if (view == null
                || view.getProcessName() == null
                || !view.getProcessName().equals(exit.getProcessName())
                || isEmpty(view.getProcessRunId())
                || view.getProcessRunId().equals(currentProcessRunId)
                || isEmpty(view.getSessionId())
                || isEmpty(view.getViewId())) {
            return false;
        }
        long latestAllowedStart = exit.getTimestampMs() + Math.max(0, toleranceMs);
        return view.getViewStartMs() <= latestAllowedStart
                && view.getProcessStartMs() <= latestAllowedStart;
    }

    private static HistoricalRumContext latestUnique(List<HistoricalRumContext> views,
                                                     boolean containingClosed,
                                                     ProcessExitRecord exit,
                                                     long toleranceMs) {
        HistoricalRumContext latest = null;
        long latestStart = Long.MIN_VALUE;
        boolean ambiguous = false;
        for (HistoricalRumContext view : views) {
            if (!matchesPriority(view, containingClosed, exit, toleranceMs)) {
                continue;
            }
            long start = view.getViewStartMs();
            if (start > latestStart) {
                latest = view;
                latestStart = start;
                ambiguous = false;
            } else if (start == latestStart) {
                ambiguous = true;
            }
        }
        return ambiguous ? null : latest;
    }

    private static boolean hasAmbiguousLatest(List<HistoricalRumContext> views,
                                              boolean containingClosed,
                                              ProcessExitRecord exit,
                                              long toleranceMs) {
        long latestStart = Long.MIN_VALUE;
        int latestCount = 0;
        for (HistoricalRumContext view : views) {
            if (!matchesPriority(view, containingClosed, exit, toleranceMs)) {
                continue;
            }
            long start = view.getViewStartMs();
            if (start > latestStart) {
                latestStart = start;
                latestCount = 1;
            } else if (start == latestStart) {
                latestCount++;
            }
        }
        return latestCount > 1;
    }

    private static boolean matchesPriority(HistoricalRumContext view,
                                           boolean containingClosed,
                                           ProcessExitRecord exit,
                                           long toleranceMs) {
        if (!containingClosed) {
            return !view.isViewClosed();
        }
        if (!view.isViewClosed()) {
            return false;
        }
        long tolerance = Math.max(0, toleranceMs);
        return exit.getTimestampMs() >= view.getViewStartMs() - tolerance
                && exit.getTimestampMs() <= view.getViewEndMs() + tolerance;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}
