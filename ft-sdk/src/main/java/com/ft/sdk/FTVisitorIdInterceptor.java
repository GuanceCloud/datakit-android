package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds SDK-managed anonymous visitor headers without enabling Trace or Resource collection.
 * Use {@link #install(OkHttpClient.Builder)} for manual integration, including redirect protection.
 */
public final class FTVisitorIdInterceptor implements Interceptor {
    static final class Snapshot {
        final String header;
        final String id;
        final boolean injected;

        Snapshot(String header, String id, boolean injected) {
            this.header = header;
            this.id = id;
            this.injected = injected;
        }
    }

    /**
     * Installs the application interceptor and redirect guard once. Call before build().
     * The application interceptor must precede FTResourceInterceptor; the guard runs after
     * other network interceptors so matched requests retain the SDK-managed value.
     */
    public static OkHttpClient.Builder install(OkHttpClient.Builder builder) {
        FTVisitorIdInterceptor application = null;
        for (Iterator<Interceptor> iterator = builder.interceptors().iterator(); iterator.hasNext();) {
            Interceptor interceptor = iterator.next();
            if (interceptor instanceof FTVisitorIdInterceptor) {
                application = (FTVisitorIdInterceptor) interceptor;
                iterator.remove();
            }
        }
        builder.interceptors().add(0, application == null ? new FTVisitorIdInterceptor() : application);
        for (Iterator<Interceptor> iterator = builder.networkInterceptors().iterator(); iterator.hasNext();) {
            if (iterator.next() instanceof RedirectGuard) iterator.remove();
        }
        builder.addNetworkInterceptor(new RedirectGuard());
        return builder;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Snapshot previous = request.tag(Snapshot.class);
        if (previous != null) {
            // A caller may reuse response.request() for a new Call after reset/shutdown.
            Request.Builder clean = request.newBuilder().tag(Snapshot.class, null);
            if (previous.injected && previous.id.equals(request.header(previous.header))) {
                clean.removeHeader(previous.header);
            }
            request = clean.build();
        }
        FTVisitorIdManager manager = FTVisitorIdManager.get();
        FTVisitorIdConfig policy = manager.getConfigSnapshot();
        if (policy != null
                && request.tag(FTWebSocketRequestTag.class) == null
                && !"websocket".equalsIgnoreCase(request.header("Upgrade"))) {
            boolean matched = manager.matches(request.url().toString());
            Snapshot snapshot;
            synchronized (manager) {
                // A matching callback can reinitialize the SDK. Never apply an old match
                // decision using a different policy's header or ID.
                snapshot = policy == manager.getConfigSnapshot()
                        ? new Snapshot(manager.getHeader(), manager.getId(), matched) : null;
            }
            if (snapshot != null && snapshot.header != null && snapshot.id != null) {
                if (matched && request.header(snapshot.header) != null) {
                    LogUtils.w("FTVisitorId", "Existing Visitor ID header replaced with SDK-managed value");
                }
                Request.Builder builder = request.newBuilder().tag(Snapshot.class, snapshot);
                if (matched) builder.header(snapshot.header, snapshot.id);
                request = builder.build();
            }
        }
        return chain.proceed(request);
    }

    /** Rechecks each redirect/retry destination, removing only the SDK-injected header. */
    private static final class RedirectGuard implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Snapshot snapshot = request.tag(Snapshot.class);
            if (snapshot != null) {
                Request.Builder builder = request.newBuilder();
                FTVisitorIdManager manager = FTVisitorIdManager.get();
                FTVisitorIdConfig policy = manager.getConfigSnapshot();
                boolean matched = manager.matches(request.url().toString());
                synchronized (manager) {
                    matched = matched && policy == manager.getConfigSnapshot()
                            && snapshot.header.equals(manager.getHeader());
                }
                if (matched) {
                    builder.header(snapshot.header, snapshot.id);
                    builder.tag(Snapshot.class, new Snapshot(snapshot.header, snapshot.id, true));
                } else if (snapshot.injected) {
                    builder.removeHeader(snapshot.header);
                    builder.tag(Snapshot.class, new Snapshot(snapshot.header, snapshot.id, false));
                }
                request = builder.build();
            }
            return chain.proceed(request);
        }
    }
}
