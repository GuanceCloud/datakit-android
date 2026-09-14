package com.ft.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.garble.utils.Constants;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Pattern;

/** Process-scoped state; storage is consulted only at initialization and rotation. */
final class FTVisitorIdManager {
    static final String KEY = "visitor_id";
    private static final FTVisitorIdManager INSTANCE = new FTVisitorIdManager();
    private static final Pattern ID_PATTERN = Pattern.compile("[ps][0-9a-f]{32}");
    interface Store {
        String read();
        boolean write(String record);
    }
    interface Clock {
        long now();
    }
    private FTVisitorIdConfig config;
    private Store store;
    private Clock clock = System::currentTimeMillis;
    private String id;
    private long createdAt;

    static FTVisitorIdManager get() {
        return INSTANCE;
    }

    synchronized void init(FTVisitorIdConfig requested) {
        Store preferences = new Store() {
            private SharedPreferences preferences() {
                Context context = FTApplication.getApplication();
                // SharedPreferences is not a cross-process lock. Keep each process independent.
                return context.getSharedPreferences("ft_visitor_id_" + Utils.getCurrentProcessName(), Context.MODE_PRIVATE);
            }

            @Override
            public String read() {
                return preferences().getString("record", null);
            }

            @Override
            public boolean write(String record) {
                return preferences().edit().putString("record", record).commit();
            }
        };
        init(requested, preferences, System::currentTimeMillis);
    }

    synchronized void init(FTVisitorIdConfig requested, Store store, Clock clock) {
        FTVisitorIdConfig next;
        try {
            next = requested == null ? null : requested.validatedCopy();
        } catch (IllegalArgumentException e) {
            LogUtils.e("FTVisitorId", e.getMessage());
            next = null;
        }
        this.config = next;
        this.store = store;
        this.clock = clock;
        if (next == null) {
            id = null;
            return;
        }
        // Preserve in-memory fallback on repeated initialization; never resurrect an older disk ID.
        if (valid(id, createdAt, clock.now(), false)) return;
        id = null;
        loadOrCreate();
    }

    synchronized boolean isEnabled() {
        return config != null;
    }

    synchronized String getId() {
        if (config == null) return null;
        if (!valid(id, createdAt, clock.now(), false)) {
            // A live record that expired must rotate, rather than reload stale persistent state.
            if (id == null) loadOrCreate(); else rotate();
        }
        return id;
    }

    synchronized void reset() {
        if (config != null) rotate();
    }

    synchronized void stop() {
        config = null;
        store = null;
        id = null;
    }

    synchronized String getHeader() {
        return config == null ? null : config.getHeader();
    }

    synchronized FTVisitorIdConfig getConfigSnapshot() {
        return config;
    }

    boolean matches(String url) {
        FTVisitorIdConfig snapshot;
        synchronized (this) {
            snapshot = config;
        }
        FTHttpConfigManager http = FTHttpConfigManager.get();
        return snapshot != null
                && !isIntakeUrl(url, http.getDatakitUrl())
                && !isIntakeUrl(url, http.getDatawayUrl())
                && !isCustomIntakeUrl(url)
                && snapshot.matches(url);
    }

    private boolean isCustomIntakeUrl(String url) {
        try {
            FTInTakeUrlHandler handler = FTRUMConfigManager.get().getConfig().getResourceUrlHandler();
            return handler != null && handler.isInTakeUrl(url);
        } catch (RuntimeException ignored) {
            LogUtils.w("FTVisitorId", "Visitor ID intake exclusion failed; skipping injection");
            return true;
        }
    }

    private boolean isIntakeUrl(String url, String baseUrl) {
        if (!FTVisitorIdConfig.sameOrigin(url, baseUrl)) return false;
        try {
            String basePath = new URI(baseUrl).normalize().getRawPath();
            if (!basePath.endsWith("/")) basePath += "/";
            String path = new URI(url).normalize().getRawPath();
            // HttpBuilder appends these endpoints to the configured base path. Match
            // endpoint prefixes like Web, without excluding same-origin business APIs.
            for (String endpoint : new String[]{Constants.URL_MODEL_RUM, Constants.URL_MODEL_LOG,
                    Constants.URL_ENV_VARIABLE, Constants.URL_DATAKIT_PULL,
                    "v1/check/rum/replay_assets"}) {
                if (path.startsWith(basePath + endpoint)) return true;
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private void loadOrCreate() {
        try {
            String raw = store == null ? null : store.read();
            if (raw != null) {
                String[] parts = raw.split(":", -1);
                if (parts.length == 2) {
                    long time = Long.parseLong(parts[1]);
                    if (valid(parts[0], time, clock.now(), true)) {
                        id = parts[0];
                        createdAt = time;
                        return;
                    }
                }
            }
        } catch (RuntimeException ignored) {
            // Corrupt or inaccessible preferences heal with the next successful write.
        }
        rotate();
    }

    private void rotate() {
        String random = UUID.randomUUID().toString().replace("-", "");
        createdAt = clock.now();
        String persistentId = "p" + random;
        String record = persistentId + ":" + createdAt;
        boolean persisted = false;
        try {
            persisted = store != null && store.write(record) && record.equals(store.read());
        } catch (RuntimeException ignored) {
            // Keep a stable fallback until TTL/reset, even if storage recovers meanwhile.
        }
        id = (persisted ? "p" : "s") + random;
    }

    private boolean valid(String value, long time, long now, boolean persistent) {
        return value != null && ID_PATTERN.matcher(value).matches()
                && (!persistent || value.charAt(0) == 'p')
                && time >= 0 && time <= now && now - time < config.getTtlMillis();
    }
}
