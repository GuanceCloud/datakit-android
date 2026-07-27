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
 * <p>Returned values may contain strings, booleans and finite numbers. Providers should return no more than
 * 50 entries. To bound processing cost, the SDK inspects at most the first 50 entries encountered in map iteration
 * order; invalid entries still consume this scan budget, and no entry-selection guarantee is made for larger maps.
 * Keys may contain at most 100 UTF-8 bytes, string values at most 4096 UTF-8 bytes, and all accepted custom fields
 * together at most 25 KiB. Invalid, oversized or SDK-reserved entries are ignored. Ordinary {@link Throwable}
 * failures are isolated, but implementations must not throw or terminate the process; the SDK cannot protect
 * against out-of-memory failures, {@code System.exit}, native aborts or similar process-ending behavior.</p>
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
     * Supplies custom fields while the SDK synchronously constructs an automatically collected
     * Crash or ANR Error.
     *
     * <p>This method may run concurrently on SDK collection threads. Follow the threading,
     * performance, type, entry-count, and size limits documented on this interface.</p>
     *
     * @param issue immutable facts about the issue being collected; never {@code null}
     * @return custom fields to merge into the RUM Error, or {@code null} or an empty map when no
     * additional fields should be added; the SDK validates and copies accepted entries
     */
    @Nullable
    Map<String, Object> provideAdditionalFields(@NonNull FTIssueInfo issue);
}
