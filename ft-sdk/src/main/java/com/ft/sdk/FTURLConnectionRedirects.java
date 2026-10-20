package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * Follows redirects only for Visitor ID requests. Each hop remains a platform connection:
 * authentication, cookies, upload streaming, proxy negotiation and TLS stay platform-owned.
 * Mirrors Android HttpURLConnection's redirect policy (including no cross-scheme redirects).
 */
final class FTURLConnectionRedirects {
    private static final int MAX_REDIRECTS = 20;
    HttpURLConnection connection;
    private final Proxy proxy;
    private boolean prepared;
    private boolean active;
    private boolean followRedirects;
    private boolean resolved;
    private IOException failure;
    private int redirects;
    private String header;
    private String visitorId;
    private boolean injected;
    private Map<String, List<String>> headers;

    FTURLConnectionRedirects(HttpURLConnection connection, Proxy proxy) {
        this.connection = connection;
        this.proxy = proxy;
    }

    String prepare() {
        if (prepared) return visitorId;
        prepared = true;
        FTVisitorIdManager manager = FTVisitorIdManager.get();
        FTVisitorIdConfig policy = manager.getConfigSnapshot();
        if (policy == null) return null;
        try {
            // Snapshot before I/O. A custom matcher may reset/reconfigure the SDK.
            boolean matched = manager.matches(connection.getURL().toString());
            synchronized (manager) {
                if (policy != manager.getConfigSnapshot()) return null;
                header = manager.getHeader();
                visitorId = manager.getId();
            }
            if (header == null || visitorId == null) return null;
            followRedirects = connection.getInstanceFollowRedirects();
            // Read properties first: an already-connected connection must not gain a header.
            headers = copyHeaders(connection.getRequestProperties());
            connection.setInstanceFollowRedirects(false);
            active = true;
            if (matched) {
                if (hasHeader(headers, header)) {
                    LogUtils.w("FTVisitorId", "Existing Visitor ID header replaced with SDK-managed value");
                }
                connection.setRequestProperty(header, visitorId);
                injected = true;
            }
        } catch (RuntimeException ignored) {
            // Do not turn header/configuration failures into failed business requests.
            if (active) connection.setInstanceFollowRedirects(followRedirects);
            active = false;
        }
        return visitorId;
    }

    /** Include Trace and caller headers after all pre-I/O instrumentation has completed. */
    void captureHeaders() {
        if (active) headers = copyHeaders(connection.getRequestProperties());
    }

    boolean getFollowRedirects() {
        return active ? followRedirects : connection.getInstanceFollowRedirects();
    }

    void setFollowRedirects(boolean value) {
        if (active) {
            // The platform property stays false; the wrapper exposes the caller's policy.
            connection.setInstanceFollowRedirects(false);
            followRedirects = value;
        } else {
            connection.setInstanceFollowRedirects(value);
        }
    }

    void resolve() throws IOException {
        if (!active || resolved) return;
        if (failure != null) throw failure;
        try {
            while (true) {
                int code = connection.getResponseCode();
                URL target = redirectTarget(code);
                if (target == null) { resolved = true; return; }
                if (++redirects > MAX_REDIRECTS) {
                    throw new ProtocolException("Too many redirects: " + redirects);
                }
                HttpURLConnection previous = connection;
                HttpURLConnection next = open(target);
                try {
                    configureNext(previous, next);
                } catch (IOException | RuntimeException error) {
                    next.disconnect();
                    throw error;
                }
                connection = next;
                // No body buffering or replay: Android converts body methods to GET for
                // 300/301/302/303 and does not follow 307/308 for non-GET/HEAD requests.
                previous.disconnect();
            }
        } catch (IOException error) {
            failure = error;
            throw error;
        }
    }

    private URL redirectTarget(int code) {
        if (!followRedirects) return null;
        String method = connection.getRequestMethod();
        if (code == 307 || code == 308) {
            if (!"GET".equals(method) && !"HEAD".equals(method)) return null;
        } else if (code != 300 && code != 301 && code != 302 && code != 303) {
            return null;
        }
        String location = connection.getHeaderField("Location");
        if (location == null) return null;
        try {
            URL target = new URL(connection.getURL(), location);
            String protocol = target.getProtocol();
            if (!("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))
                    || !protocol.equalsIgnoreCase(connection.getURL().getProtocol())) return null;
            return target;
        } catch (IOException ignored) { return null; }
    }

    private HttpURLConnection open(URL url) throws IOException {
        URLConnection next = proxy == null ? url.openConnection() : url.openConnection(proxy);
        if (!(next instanceof HttpURLConnection)) {
            throw new ProtocolException("Redirect target is not an HTTP connection");
        }
        return (HttpURLConnection) next;
    }

    private void configureNext(HttpURLConnection previous, HttpURLConnection next) throws IOException {
        next.setInstanceFollowRedirects(false);
        next.setConnectTimeout(previous.getConnectTimeout());
        next.setReadTimeout(previous.getReadTimeout());
        next.setUseCaches(previous.getUseCaches());
        next.setAllowUserInteraction(previous.getAllowUserInteraction());
        next.setDoInput(previous.getDoInput());
        next.setIfModifiedSince(previous.getIfModifiedSince());
        if (previous instanceof HttpsURLConnection && next instanceof HttpsURLConnection) {
            HttpsURLConnection from = (HttpsURLConnection) previous;
            HttpsURLConnection to = (HttpsURLConnection) next;
            to.setSSLSocketFactory(from.getSSLSocketFactory());
            to.setHostnameVerifier(from.getHostnameVerifier());
        }
        String method = previous.getRequestMethod();
        boolean bodyMethod = !"GET".equals(method) && !"HEAD".equals(method) && !"TRACE".equals(method);
        next.setRequestMethod(bodyMethod ? "GET" : method);
        Map<String, List<String>> nextHeaders = copyHeaders(headers);
        if (bodyMethod) {
            removeHeader(nextHeaders, "Content-Length");
            removeHeader(nextHeaders, "Content-Type");
            removeHeader(nextHeaders, "Transfer-Encoding");
        }
        if (!FTVisitorIdConfig.sameOrigin(previous.getURL().toString(), next.getURL().toString())) {
            removeHeader(nextHeaders, "Authorization");
            // Let the new connection compute its target Host (including a new port).
            removeHeader(nextHeaders, "Host");
        }
        // Remove only an SDK-managed header; an unmatched caller-owned header stays intact.
        if (injected) removeHeader(nextHeaders, header);
        injected = false;
        FTVisitorIdManager manager = FTVisitorIdManager.get();
        FTVisitorIdConfig policy = manager.getConfigSnapshot();
        boolean matched = manager.matches(next.getURL().toString());
        synchronized (manager) {
            if (matched && policy == manager.getConfigSnapshot() && header.equals(manager.getHeader())) {
                ArrayList<String> values = new ArrayList<>();
                values.add(visitorId);
                removeHeader(nextHeaders, header);
                nextHeaders.put(header, values);
                injected = true;
            }
        }
        for (Map.Entry<String, List<String>> entry : nextHeaders.entrySet()) {
            for (String value : entry.getValue()) next.addRequestProperty(entry.getKey(), value);
        }
        headers = nextHeaders;
    }

    private static Map<String, List<String>> copyHeaders(Map<String, List<String>> source) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        return result;
    }

    private static boolean hasHeader(Map<String, List<String>> values, String name) {
        for (String key : values.keySet()) if (name.equalsIgnoreCase(key)) return true;
        return false;
    }

    private static void removeHeader(Map<String, List<String>> values, String name) {
        for (Iterator<String> keys = values.keySet().iterator(); keys.hasNext();) {
            if (name.equalsIgnoreCase(keys.next())) keys.remove();
        }
    }
}
