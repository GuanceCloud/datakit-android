package com.ft.sdk.internal.issue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.FTIssueDataProvider;
import com.ft.sdk.FTIssueInfo;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Invokes an issue provider and owns all validation and SDK-wins merge rules.
 */
public final class FTIssueFieldEnricher {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTIssueFieldEnricher";

    public static final int MAX_FIELD_COUNT = 50;
    public static final int MAX_KEY_BYTES = 100;
    public static final int MAX_STRING_BYTES = 4096;
    public static final int MAX_TOTAL_BYTES = 25 * 1024;
    private static final long SLOW_PROVIDER_NANOS = 50_000_000L;
    private static final Object INVALID_VALUE = new Object();

    @Nullable
    private final FTIssueDataProvider provider;

    public FTIssueFieldEnricher(@Nullable FTIssueDataProvider provider) {
        this.provider = provider;
    }

    /**
     * Invokes the configured provider once and returns an isolated, validated map.
     */
    @NonNull
    public HashMap<String, Object> provideAdditionalFields(@NonNull FTIssueInfo issue) {
        if (provider == null) {
            return new HashMap<>();
        }

        Map<String, Object> supplied;
        long start = System.nanoTime();
        try {
            supplied = provider.provideAdditionalFields(issue);
        } catch (Throwable throwable) {
            LogUtils.e(TAG, "Issue data provider failed: " + throwable.getClass().getSimpleName());
            return new HashMap<>();
        } finally {
            long duration = System.nanoTime() - start;
            if (duration > SLOW_PROVIDER_NANOS) {
                LogUtils.w(TAG, "Issue data provider took " + (duration / 1_000_000L) + " ms");
            }
        }

        if (supplied == null) {
            return new HashMap<>();
        }

        final Iterator<? extends Map.Entry<?, ?>> iterator;
        try {
            iterator = supplied.entrySet().iterator();
        } catch (Throwable throwable) {
            LogUtils.e(TAG, "Issue data provider result could not be traversed");
            return new HashMap<>();
        }

        HashMap<String, Object> accepted = new HashMap<>();
        int scanned = 0;
        int totalBytes = 0;
        while (scanned < MAX_FIELD_COUNT) {
            final Map.Entry<?, ?> entry;
            try {
                if (!iterator.hasNext()) {
                    break;
                }
                entry = iterator.next();
            } catch (Throwable throwable) {
                LogUtils.e(TAG, "Issue data provider result could not be traversed");
                return new HashMap<>();
            }
            scanned++;

            try {
                Object rawKey = entry.getKey();
                if (!(rawKey instanceof String)) {
                    continue;
                }
                String key = (String) rawKey;
                Object value = normalizeValue(entry.getValue());
                if (!isValidKeySyntax(key) || value == INVALID_VALUE) {
                    continue;
                }

                int keyBytes = utf8LengthUpTo(key, MAX_KEY_BYTES);
                if (keyBytes > MAX_KEY_BYTES) {
                    continue;
                }
                int remainingBytes = MAX_TOTAL_BYTES - totalBytes - keyBytes;
                if (remainingBytes < 0) {
                    continue;
                }
                int valueByteLimit = value instanceof String
                        ? Math.min(MAX_STRING_BYTES, remainingBytes)
                        : remainingBytes;
                String serializedValue = value instanceof String
                        ? (String) value
                        : String.valueOf(value);
                int valueBytes = utf8LengthUpTo(serializedValue, valueByteLimit);
                if (valueBytes > valueByteLimit) {
                    continue;
                }

                accepted.put(key, value);
                totalBytes += keyBytes + valueBytes;
            } catch (Throwable ignored) {
                // Invalid entries are dropped individually without logging application values.
            }
        }
        return accepted;
    }

    /**
     * Adds custom entries while also protecting SDK tags and fields assembled later in the pipeline.
     */
    public void mergeAdditionalFields(@NonNull Map<String, Object> additionalFields,
                                      @NonNull Map<String, Object> sdkTags,
                                      @NonNull Map<String, Object> sdkFields,
                                      @NonNull Set<String> reservedKeys) {
        for (Map.Entry<String, Object> entry : additionalFields.entrySet()) {
            String key = entry.getKey();
            if (!sdkTags.containsKey(key)
                    && !sdkFields.containsKey(key)
                    && !reservedKeys.contains(key)) {
                sdkFields.put(key, entry.getValue());
            }
        }
    }

    private static boolean isValidKeySyntax(@Nullable String key) {
        return key != null
                && !key.isEmpty()
                && !key.startsWith("error.")
                && !key.startsWith("error_");
    }

    @NonNull
    private static Object normalizeValue(@Nullable Object value) {
        if (value instanceof String || value instanceof Boolean) {
            return value;
        }
        if (!(value instanceof Number)) {
            return INVALID_VALUE;
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return ((Number) value).intValue();
        }
        if (value instanceof Long) {
            return value;
        }
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            return !Double.isNaN(doubleValue) && !Double.isInfinite(doubleValue)
                    ? value
                    : INVALID_VALUE;
        }
        if (value instanceof Float) {
            float floatValue = (Float) value;
            return !Float.isNaN(floatValue) && !Float.isInfinite(floatValue)
                    ? value
                    : INVALID_VALUE;
        }
        double doubleValue = ((Number) value).doubleValue();
        return !Double.isNaN(doubleValue) && !Double.isInfinite(doubleValue)
                ? doubleValue
                : INVALID_VALUE;
    }

    private static int utf8LengthUpTo(@NonNull String value, int maxBytes) {
        int bytes = 0;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            int currentBytes;
            if (current <= 0x7F) {
                currentBytes = 1;
            } else if (current <= 0x7FF) {
                currentBytes = 2;
            } else if (Character.isHighSurrogate(current)) {
                if (i + 1 < value.length()
                        && Character.isLowSurrogate(value.charAt(i + 1))) {
                    currentBytes = 4;
                    i++;
                } else {
                    // Java's UTF-8 encoder replaces an unpaired surrogate with one-byte '?'.
                    currentBytes = 1;
                }
            } else if (Character.isLowSurrogate(current)) {
                // Java's UTF-8 encoder replaces an unpaired surrogate with one-byte '?'.
                currentBytes = 1;
            } else {
                currentBytes = 3;
            }
            if (bytes > maxBytes - currentBytes) {
                return maxBytes + 1;
            }
            bytes += currentBytes;
        }
        return bytes;
    }
}
