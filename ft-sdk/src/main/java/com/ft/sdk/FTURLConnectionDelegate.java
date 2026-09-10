package com.ft.sdk;

import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Observes calls made by the application without performing I/O to complete missing metrics. */
final class FTURLConnectionDelegate {
    final HttpURLConnection connection;
    final FTURLConnectionRequestState state;
    private final IdentityHashMap<InputStream, FTURLConnectionInputStream> inputs = new IdentityHashMap<>();
    private OutputStream originalOutput;
    private FTURLConnectionOutputStream output;

    FTURLConnectionDelegate(HttpURLConnection connection, FTURLConnectionRequestState state) {
        this.connection = connection;
        this.state = state;
    }

    private StartSnapshot prepareStart() {
        try {
            synchronized (FTURLConnectionResourceReporter.LOCK) {
                if (state.started || state.terminal) return null;
                long now = System.nanoTime();
                long wallTime = Utils.getCurrentNanoTime();
                HashMap<String, List<String>> headers = null;
                try { headers = snapshot(connection.getRequestProperties()); } catch (Exception ignored) { }
                return new StartSnapshot(connection.getRequestMethod(), headers, now, wallTime);
            }
        } catch (Throwable ignored) { state.discard(); return null; }
    }

    private void begin() {
        begin(prepareStart());
        state.progress(0, 0);
    }

    private void begin(StartSnapshot start) {
        if (start != null) state.begin(start.method, start.headers, start.nanoTime, start.wallTime);
    }

    private static final class StartSnapshot {
        final String method;
        final HashMap<String, List<String>> headers;
        final long nanoTime;
        final long wallTime;
        StartSnapshot(String method, HashMap<String, List<String>> headers, long nanoTime, long wallTime) {
            this.method = method;
            this.headers = headers;
            this.nanoTime = nanoTime;
            this.wallTime = wallTime;
        }
    }

    private void method() {
        try {
            synchronized (FTURLConnectionResourceReporter.LOCK) {
                if (!state.terminal) state.params.resourceMethod = connection.getRequestMethod();
            }
        } catch (Throwable ignored) { }
        state.progress(0, 0);
    }

    void connect() throws IOException {
        begin();
        try { connection.connect(); method(); }
        catch (IOException e) { failure(e); throw e; }
    }

    void disconnect() {
        try { connection.disconnect(); }
        finally { state.finish("disconnected", false, null); }
    }

    OutputStream getOutputStream() throws IOException {
        if (!connection.getDoOutput()) return connection.getOutputStream();
        begin();
        try {
            OutputStream stream = connection.getOutputStream();
            method(); // The platform may switch its default GET to POST here.
            if (stream == null) return null;
            synchronized (inputs) {
                if (originalOutput == stream) return output;
                if (originalOutput != null) { state.discard(); return stream; }
                originalOutput = stream;
                output = new FTURLConnectionOutputStream(stream, this);
                return output;
            }
        } catch (IOException e) { failure(e); throw e; }
    }

    InputStream getInputStream() throws IOException {
        if (!connection.getDoInput()) return connection.getInputStream();
        begin();
        try {
            InputStream stream = connection.getInputStream();
            responseAvailable();
            return input(stream);
        } catch (IOException e) {
            inputFailure(e);
            throw e;
        }
    }

    InputStream getErrorStream() {
        // Android may connect here, so reserve the original View/Action before entering I/O.
        // A null first probe rolls back that reservation, without a Resource or network error.
        StartSnapshot start;
        long version;
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            start = prepareStart();
            begin(start);
            state.progress(0, 0);
            version = state.activityVersion;
        }
        InputStream stream = connection.getErrorStream();
        if (stream != null) responseAvailable();
        else if (start != null) state.cancelUnobservedProbe(version);
        return input(stream);
    }

    int getResponseCode() throws IOException {
        begin();
        try {
            int code = connection.getResponseCode();
            method();
            if (code > 0) {
                try { observe(code, connection.getHeaderFields()); } catch (Throwable ignored) { }
            }
            return code;
        } catch (IOException e) { failure(e); throw e; }
    }

    String getResponseMessage() throws IOException {
        begin();
        try {
            String message = connection.getResponseMessage();
            if (message != null) responseAvailable();
            return message;
        } catch (IOException e) { failure(e); throw e; }
    }

    Object getContent() throws IOException {
        begin();
        try { return content(connection.getContent()); }
        catch (IOException e) { inputFailure(e); throw e; }
    }

    @SuppressWarnings("rawtypes")
    Object getContent(Class[] classes) throws IOException {
        begin();
        try { return content(connection.getContent(classes)); }
        catch (IOException e) { inputFailure(e); throw e; }
    }

    private Object content(Object value) {
        if (value != null) responseAvailable();
        if (value instanceof InputStream) return input((InputStream) value);
        // Custom content handlers may consume an arbitrary amount. No complete size is inferred.
        state.finish("content", false, null);
        return value;
    }

    private InputStream input(InputStream stream) {
        if (stream == null) return null;
        synchronized (inputs) {
            FTURLConnectionInputStream wrapped = inputs.get(stream);
            if (wrapped != null) return wrapped;
            if (inputs.size() >= 4) { state.discard(); return stream; }
            if (!inputs.isEmpty()) {
                // Different stream objects can alias the same transport with independent reset
                // positions. Keep observed consumption, but do not claim a complete body size.
                synchronized (FTURLConnectionResourceReporter.LOCK) { state.sizeKnown = false; }
            }
            wrapped = new FTURLConnectionInputStream(stream, this);
            inputs.put(stream, wrapped);
            return wrapped;
        }
    }

    /** Only after the caller has obtained a response; these getters read cached response metadata. */
    private void responseAvailable() {
        try {
            method();
            int code = connection.getResponseCode();
            if (code > 0) observe(code, connection.getHeaderFields());
        } catch (Throwable ignored) { }
    }

    private void observe(int code, Map<String, List<String>> headers) {
        try {
            synchronized (FTURLConnectionResourceReporter.LOCK) {
                if (state.terminal) return;
                state.params.resourceStatus = code;
                state.params.responseHeaderMap = snapshot(headers);
                state.params.responseContentType = header(headers, "Content-Type");
                state.params.responseContentEncoding = header(headers, "Content-Encoding");
                state.params.responseConnection = header(headers, "Connection");
                if (code >= 200 && ("HEAD".equals(state.params.resourceMethod) || code == 204 || code == 304)) {
                    state.finish("no_body", true, null);
                }
            }
        } catch (Throwable ignored) { }
    }

    private void headersAvailable(Map<String, List<String>> headers) {
        if (headers == null) return;
        try {
            String status = header(headers, null);
            if (status.startsWith("HTTP/")) {
                String[] parts = status.split(" ", 3);
                if (parts.length >= 2) { method(); observe(Integer.parseInt(parts[1]), headers); }
            }
        } catch (Throwable ignored) { }
    }

    private void headerAvailable(Object value) {
        if (value != null) {
            try { headersAvailable(connection.getHeaderFields()); } catch (Throwable ignored) { }
        }
    }

    private void inputFailure(IOException error) {
        // A standard HTTP error may throw before handing its error body to the caller.
        // Do not obtain an input stream or read error data merely to complete metrics.
        // Android's standard implementation throws FileNotFoundException for HTTP >= 400.
        // Other I/O failures must not trigger an extra getErrorStream/getResponseCode operation.
        if (error instanceof java.io.FileNotFoundException) responseAvailable();
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (state.params.resourceStatus < 400) failure(error);
        }
    }

    void failure(IOException error) {
        method();
        state.finish("io_error", false, error);
    }

    private static String header(Map<String, List<String>> headers, String key) {
        if (headers != null) for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (key == null ? entry.getKey() == null : key.equalsIgnoreCase(entry.getKey())) {
                List<String> values = entry.getValue();
                return values == null || values.isEmpty() || values.get(0) == null ? "" : values.get(0);
            }
        }
        return "";
    }

    // Bound retained metadata independently of body length and custom header implementations.
    private static HashMap<String, List<String>> snapshot(Map<String, List<String>> headers) {
        HashMap<String, List<String>> result = new HashMap<>();
        if (headers == null) return result;
        int remaining = 16384;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            remaining -= entry.getKey().length();
            ArrayList<String> values = new ArrayList<>();
            for (String value : entry.getValue()) {
                if (value != null) {
                    remaining -= value.length();
                    if (remaining < 0) return result;
                    values.add(value);
                }
            }
            result.put(entry.getKey(), values);
        }
        return result;
    }

    long getLastModified() {
        begin();
        long value = connection.getLastModified();
        if (value != 0) headerAvailable(value);
        return value;
    }

    long getExpiration() {
        begin();
        long value = connection.getExpiration();
        if (value != 0) headerAvailable(value);
        return value;
    }

    String getHeaderField(int n) {
        begin();
        String value = connection.getHeaderField(n);
        if (value != null) headerAvailable(value);
        return value;
    }

    String getHeaderField(String name) {
        begin();
        String value = connection.getHeaderField(name);
        if (value != null) headerAvailable(value);
        return value;
    }

    long getHeaderFieldDate(String name, long defaultDate) {
        begin();
        long value = connection.getHeaderFieldDate(name, defaultDate);
        if (value != defaultDate) headerAvailable(value);
        return value;
    }

    int getHeaderFieldInt(String name, int defaultInt) {
        begin();
        int value = connection.getHeaderFieldInt(name, defaultInt);
        if (value != defaultInt) headerAvailable(value);
        return value;
    }

    long getHeaderFieldLong(String name, long defaultLong) {
        begin();
        long value = headerLong(name, defaultLong);
        if (value != defaultLong) headerAvailable(value);
        return value;
    }

    String getHeaderFieldKey(int n) {
        begin();
        String value = connection.getHeaderFieldKey(n);
        if (value != null) headerAvailable(value);
        return value;
    }

    String getContentEncoding() {
        begin();
        String value = connection.getContentEncoding();
        if (value != null) headerAvailable(value);
        return value;
    }

    int getContentLength() {
        begin();
        int value = connection.getContentLength();
        if (value != -1) headerAvailable(value);
        return value;
    }

    long getContentLengthLong() {
        begin();
        long value = android.os.Build.VERSION.SDK_INT >= 24
                ? connection.getContentLengthLong() : headerLong("Content-Length", -1);
        if (value != -1) headerAvailable(value);
        return value;
    }

    String getContentType() {
        begin();
        String value = connection.getContentType();
        if (value != null) headerAvailable(value);
        return value;
    }

    long getDate() {
        begin();
        long value = connection.getDate();
        if (value != 0) headerAvailable(value);
        return value;
    }

    private long headerLong(String name, long defaultValue) {
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            return connection.getHeaderFieldLong(name, defaultValue);
        }
        try { return Long.parseLong(connection.getHeaderField(name)); }
        catch (NumberFormatException ignored) { return defaultValue; }
    }

    Map<String, List<String>> getHeaderFields() {
        begin();
        Map<String, List<String>> headers = connection.getHeaderFields();
        headersAvailable(headers);
        return headers;
    }
}
