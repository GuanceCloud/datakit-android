package com.ft.sdk;

import androidx.annotation.NonNull;

/**
 * Selects Resources contributing to a native View's {@code network_settled_time}.
 *
 * <p>The SDK calls this synchronously once per Resource registration, outside its timing locks.
 * Registration may occur after the network request starts; use the original timestamps in the
 * context for time-based decisions. Calls may occur concurrently on collection or calling threads,
 * including the main thread. Implementations must be thread-safe, fast and non-blocking.</p>
 *
 * <p>This rule replaces the default 100 ms window. Returning {@code false}, or throwing an ordinary
 * exception, excludes the Resource only from this metric, without dropping its RUM event. Cache
 * writes, retries and uploads do not invoke this rule again. Selected Resources must still complete
 * before the View stops and satisfy the SDK's event-write requirements.</p>
 */
@FunctionalInterface
public interface FTInitialResourceIdentifier {
    /**
     * @param context immutable Resource registration facts; never {@code null}
     * @return whether this Resource contributes to network settled time
     */
    boolean validate(@NonNull FTNetworkSettledResourceContext context);
}
