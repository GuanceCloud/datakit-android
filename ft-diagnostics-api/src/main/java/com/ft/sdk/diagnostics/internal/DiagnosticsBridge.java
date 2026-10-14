package com.ft.sdk.diagnostics.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Internal observation boundary. No storage, threads or network when unregistered. */
public final class DiagnosticsBridge {
    public interface Sink { void record(Map<String, Object> event); }
    private static volatile Sink sink;
    private static final ThreadLocal<Transfer> transfers = new ThreadLocal<>();
    public static final class Transfer { public long tx = -1, rx = -1; }
    public static Transfer transfer() { return transfers.get(); }
    public static void transfer(Transfer value) { if(value==null)transfers.remove();else transfers.set(value); }
    public static java.io.OutputStream output(java.io.OutputStream out) {
        final Transfer t=transfer(); if(t==null)return out; if(t.tx<0)t.tx=0;
        return new java.io.FilterOutputStream(out) {
            public void write(int b)throws java.io.IOException {out.write(b);t.tx++;}
            public void write(byte[] b,int off,int len)throws java.io.IOException {out.write(b,off,len);t.tx+=len;}
        };
    }
    public static java.io.InputStream input(java.io.InputStream in) {
        final Transfer t=transfer(); if(t==null || in==null)return in; if(t.rx<0)t.rx=0;
        return new java.io.FilterInputStream(in) {
            public int read()throws java.io.IOException {int n=in.read();if(n>=0)t.rx++;return n;}
            public int read(byte[] b,int off,int len)throws java.io.IOException {int n=in.read(b,off,len);if(n>0)t.rx+=n;return n;}
        };
    }

    private DiagnosticsBridge() { }
    public static synchronized boolean install(Sink value) {
        if (sink != null) return false;
        sink = value;
        return true;
    }
    public static synchronized void uninstall(Sink value) { if (sink == value) sink = null; }
    public static boolean enabled() { return sink != null; }
    public static String channel(String value) {
        if (value == null) return "other";
        String v = value.toLowerCase(java.util.Locale.US);
        if (v.contains("replay") || v.contains("resource") || v.contains("snapshot")) return "replay";
        if (v.contains("log")) return "log";
        if (v.contains("rum")) return "rum";
        return "other";
    }
    public static Map<String, Object> event(String kind, String channel) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("event_type", kind); e.put("channel", channel(channel));
        e.put("time_ms", System.currentTimeMillis());
        return e;
    }
    private static void deliver(Sink target, Map<String, Object> event) {
        if (target != null) try { target.record(Collections.unmodifiableMap(event)); }
        catch (Throwable ignored) { /* Diagnostics must not change SDK outcomes. */ }
    }
    public static void record(String kind, String channel, long count, long bytes, String reason) {
        Sink target = sink;
        if (target == null) return;
        Map<String, Object> e = event(kind, channel);
        if (count >= 0) e.put("count", count);
        if (bytes >= 0) e.put("bytes", bytes);
        e.put("reason", reason);
        deliver(target, e);
    }
    public static void batch(String channel,long count,long bytes,long ageMs,String source) {
        Sink target=sink;if(target==null)return;
        Map<String,Object> e=event("batch",channel);
        if(count>=0)e.put("count",count);if(bytes>=0)e.put("bytes",bytes);
        if(ageMs>=0)e.put("age_ms",ageMs);e.put("age_source",source);
        deliver(target,e);
    }
    public static void backlog(String channel, long count) {
        if (count >= 0) record("backlog", channel, count, -1, "observed_storage");
    }
    /** Captures the observer so an old operation never completes into a new session. */
    public static Attempt begin(String channel, long payloadBytes) {
        Sink target = sink;
        return target == null ? null : new Attempt(target, channel, payloadBytes);
    }
    public static final class Attempt {
        private final Sink target;
        private final String channel, id = UUID.randomUUID().toString();
        private final long start = System.nanoTime();
        private boolean finished;
        private Attempt(Sink target, String channel, long bytes) {
            this.target = target; this.channel = channel;
            Map<String, Object> e = event("upload_start", channel);
            e.put("attempt_id", id);
            if (bytes >= 0) e.put("payload_bytes", bytes);
            deliver(target, e);
        }
        public synchronized void finish(int status, long tx, long rx, String source) {
            if (finished) return;
            finished = true;
            Map<String, Object> e = event("upload_end", channel);
            e.put("attempt_id", id);
            e.put("duration_ms", (System.nanoTime() - start) / 1e6);
            e.put("status_code", status);
            e.put("outcome", status >= 200 && status < 300 ? "success" : "failure");
            if (tx >= 0) e.put("request_body_bytes", tx);
            if (rx >= 0) e.put("response_body_bytes", rx);
            e.put("byte_source", source);
            e.put("byte_availability", tx >= 0 && rx >= 0 ? "observed" : "unavailable_or_partial");
            deliver(target, e);
        }
    }
}
