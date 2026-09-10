package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfigManager;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;

/**
 * Binary entry point used by ft-plugin for URL.openConnection() and openConnection(Proxy).
 * Real I/O always belongs to the original connection. No Trace headers are injected.
 */
public final class FTURLConnectionInstrumentation {
    // Both sides are weak: a live original connection must not retain its wrapper through this table.
    private static final ArrayList<Entry> CONNECTIONS = new ArrayList<>();

    private FTURLConnectionInstrumentation() { }

    public static URLConnection wrap(URLConnection connection) {
        if (!(connection instanceof HttpURLConnection)
                || connection instanceof FTHttpURLConnection
                || connection instanceof FTHttpsURLConnection) {
            return connection;
        }
        try {
            synchronized (FTURLConnectionResourceReporter.LOCK) {
                if (!enabled(connection.getURL()) || isExcludedAdapter(connection.getClass())) {
                    return connection;
                }
                Iterator<Entry> iterator = CONNECTIONS.iterator();
                while (iterator.hasNext()) {
                    Entry entry = iterator.next();
                    URLConnection original = entry.original.get();
                    if (original == null) {
                        iterator.remove();
                    } else if (original == connection) {
                        URLConnection wrapper = entry.wrapper.get();
                        if (wrapper != null) return wrapper;
                        FTURLConnectionDelegate delegate = entry.delegate.get();
                        if (delegate == null) {
                            delegate = new FTURLConnectionDelegate((HttpURLConnection) connection, entry.state);
                            entry.delegate = new WeakReference<>(delegate);
                        }
                        wrapper = proxy((HttpURLConnection) connection, delegate);
                        entry.wrapper = new WeakReference<>(wrapper);
                        return wrapper;
                    }
                }
                if (CONNECTIONS.size() >= FTURLConnectionResourceReporter.MAX_REQUESTS) return connection;
                FTURLConnectionRequestState state = new FTURLConnectionRequestState(connection.getURL().toString(),
                        FTURLConnectionResourceReporter.generation());
                FTURLConnectionDelegate delegate = new FTURLConnectionDelegate((HttpURLConnection) connection, state);
                URLConnection wrapper = proxy((HttpURLConnection) connection, delegate);
                CONNECTIONS.add(new Entry(connection, wrapper, delegate, state));
                return wrapper;
            }
        } catch (Throwable ignored) {
            // Instrumentation failures must never replace a successfully created business connection.
            return connection;
        }
    }

    static boolean enabled(URL url) {
        if (!FTURLConnectionResourceReporter.isAccepting() || !FTSdk.checkInstallState() || !FTRUMConfigManager.get().isRumEnable()) return false;
        FTRUMConfig config = FTRUMConfigManager.get().getConfig();
        if (!config.isEnableTraceUserResource() || !config.isEnableHttpURLConnectionResource()) return false;
        FTHttpConfigManager http = FTHttpConfigManager.get();
        return !underEndpoint(url, http.getDatakitUrl()) && !underEndpoint(url, http.getDatawayUrl())
                && !config.getResourceUrlHandler().isInTakeUrl(url.toString());
    }

    private static boolean underEndpoint(URL url, String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) return false;
        try {
            URL base = new URL(endpoint);
            int port = url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
            int basePort = base.getPort() < 0 ? base.getDefaultPort() : base.getPort();
            String path = base.getPath();
            return base.getProtocol().equalsIgnoreCase(url.getProtocol())
                    && base.getHost().equalsIgnoreCase(url.getHost()) && port == basePort
                    && (url.getPath().equals(path) || url.getPath().startsWith(path.endsWith("/") ? path : path + "/"));
        } catch (Exception ignored) {
            return false;
        }
    }

    static boolean isExcludedAdapter(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            // okhttp-urlconnection 3.12.13. Deliberately does not match com.android.okhttp.
            String name = current.getName();
            if (name.equals("okhttp3.internal.huc.OkHttpURLConnection")
                    || name.equals("okhttp3.internal.huc.OkHttpsURLConnection")) return true;
        }
        return false;
    }

    private static URLConnection proxy(HttpURLConnection connection, FTURLConnectionDelegate delegate) {
        return connection instanceof HttpsURLConnection
                ? new FTHttpsURLConnection((HttpsURLConnection) connection, delegate)
                : new FTHttpURLConnection(connection, delegate);
    }

    static void clearConnections() {
        CONNECTIONS.clear();
    }

    private static final class Entry {
        final WeakReference<URLConnection> original;
        WeakReference<URLConnection> wrapper;
        WeakReference<FTURLConnectionDelegate> delegate;
        final FTURLConnectionRequestState state;
        Entry(URLConnection original, URLConnection wrapper, FTURLConnectionDelegate delegate,
              FTURLConnectionRequestState state) {
            this.original = new WeakReference<>(original);
            this.wrapper = new WeakReference<>(wrapper);
            this.delegate = new WeakReference<>(delegate);
            this.state = state;
        }
    }
}
