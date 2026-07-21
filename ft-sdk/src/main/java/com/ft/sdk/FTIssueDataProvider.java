package com.ft.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Supplies custom fields while the SDK constructs an automatically collected crash or ANR Error.
 *
 * <p>The provider is invoked synchronously on the collection thread and may be invoked concurrently.
 * Implementations should be thread-safe, avoid I/O and return within 10 ms. A fatal crash restored on
 * the next application start is reported with {@link FTIssueInfo#isHistorical()} set to {@code true}.</p>
 *
 * <p>Returned values may contain strings, booleans and finite numbers. At most 50 fields are accepted. Keys may
 * contain at most 100 UTF-8 bytes, string values at most 4096 UTF-8 bytes, and all accepted custom fields together
 * at most 25 KiB. Invalid, oversized or SDK-reserved entries are ignored. Ordinary {@link Throwable} failures are
 * isolated, but implementations must not throw or terminate the process; the SDK cannot protect against
 * out-of-memory failures, {@code System.exit}, native aborts or similar process-ending behavior.</p>
 *
 * <p>Java example:</p>
 * <pre>{@code
 * FTRUMConfig config = new FTRUMConfig().setIssueDataProvider(issue -> {
 *     Map<String, Object> fields = new HashMap<>();
 *     fields.put("business_scene", sceneStore.current());
 *     fields.put("historical_issue", issue.isHistorical());
 *     return fields;
 * });
 * }</pre>
 *
 * <p>Kotlin example:</p>
 * <pre>{@code
 * val config = FTRUMConfig().setIssueDataProvider { issue ->
 *     mapOf(
 *         "business_scene" to sceneStore.current(),
 *         "historical_issue" to issue.isHistorical
 *     )
 * }
 * }</pre>
 */
@FunctionalInterface
public interface FTIssueDataProvider {
    /**
     * Returns custom RUM Error fields for {@code issue}, or {@code null} for no additional fields.
     */
    @Nullable
    Map<String, Object> provideAdditionalFields(@NonNull FTIssueInfo issue);
}
