package com.ft.sdk;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/** Lightweight state only: never retains a business connection, stream or body. */
final class FTURLConnectionRequestState {
    interface Reporter {
        boolean start(FTURLConnectionRequestState state);
        void finish(FTURLConnectionRequestState state, ResourceParams params, NetStatusBean timing);
        void discard(FTURLConnectionRequestState state);
    }

    String id = UUID.randomUUID().toString();
    final String url;
    final long generation;
    final Reporter reporter;
    final ResourceParams params = new ResourceParams();
    boolean started;
    boolean terminal;
    long startedAt;
    long startedWallTime;
    long lastProgress;
    long activityVersion;
    long responseBytes;
    long requestBytes;
    boolean sizeKnown = true;

    FTURLConnectionRequestState(String url, long generation) {
        this(url, generation, FTURLConnectionResourceReporter.REPORTER);
    }

    FTURLConnectionRequestState(String url, long generation, Reporter reporter) {
        this.url = url;
        this.generation = generation;
        this.reporter = reporter;
        params.url = url;
    }

    void begin(String method, HashMap<String, java.util.List<String>> headers) {
        begin(method, headers, System.nanoTime(), Utils.getCurrentNanoTime());
    }

    void begin(String method, HashMap<String, java.util.List<String>> headers, long observedStart, long wallTime) {
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (started || terminal) return;
            params.resourceMethod = method;
            params.requestHeaderMap = headers;
            startedAt = observedStart;
            startedWallTime = wallTime;
            lastProgress = System.nanoTime();
            try {
                started = reporter.start(this);
                if (!started) terminal = true;
            } catch (Throwable ignored) {
                discard();
            }
        }
    }

    void progress(long read, long written) {
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (!started || terminal) return;
            lastProgress = System.nanoTime();
            activityVersion++;
            responseBytes = saturatedAdd(responseBytes, read);
            requestBytes = saturatedAdd(requestBytes, written);
        }
    }

    private static long saturatedAdd(long value, long amount) {
        return amount > 0 ? (Long.MAX_VALUE - value < amount ? Long.MAX_VALUE : value + amount) : value;
    }

    void finish(String completion, boolean complete, IOException error) {
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (!started || terminal) return;
            terminal = true;
            params.enableResourceSize = complete && sizeKnown;
            params.property = new HashMap<>();
            params.property.put("resource_collection_source", "http_url_connection");
            params.property.put("resource_completion", completion);
            params.property.put("resource_body_complete", complete);
            params.property.put("resource_response_bytes_observed", responseBytes);
            params.property.put("resource_request_bytes_observed", requestBytes);
            if (error != null) {
                params.forceNetworkError = true;
                // Do not retain the exception (which can retain transport objects) or log its payload.
                params.requestErrorMsg = error.getClass().getSimpleName();
                params.requestErrorStack = error.getClass().getName();
            }
            NetStatusBean timing = new NetStatusBean();
            timing.callStartTime = startedAt;
            timing.bodyEndTime = System.nanoTime();
            timing.responseBodySize = responseBytes;
            timing.requestBodySize = requestBytes;
            timing.connectionReuseKnown = false;
            timing.phaseTimingKnown = false;
            try {
                reporter.finish(this, params, timing);
            } catch (Throwable ignored) {
                try { reporter.discard(this); } catch (Throwable ignoredAgain) { }
            }
        }
    }

    /** A null first error-stream probe proves no response; undo only this otherwise unused attempt. */
    void cancelUnobservedProbe(long version) {
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (!started || terminal || activityVersion != version) return;
            try {
                reporter.discard(this);
                started = false;
                id = UUID.randomUUID().toString();
            } catch (Throwable ignored) {
                terminal = true;
            }
        }
    }

    void discard() {
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            terminal = true;
            try { reporter.discard(this); } catch (Throwable ignored) { }
        }
    }
}
