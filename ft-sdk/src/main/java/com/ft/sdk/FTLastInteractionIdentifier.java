package com.ft.sdk;

import androidx.annotation.NonNull;

/**
 * Validates the previous View's last accepted interaction for {@code interaction_to_next_view_time}.
 *
 * <p>The SDK first selects the previous View's last successfully stored tap, swipe, click or back,
 * ordered by accepted write callbacks. It does not search earlier interactions if this rule rejects
 * that candidate. Other Action types cannot be enabled through this rule.</p>
 *
 * <p>This rule replaces the default 3 second window; the SDK still requires a positive interval.
 * It runs outside timing locks on the SDK event queue, once for each candidate/next-View pair.
 * A later accepted candidate triggers a new evaluation. Results are cached, so View summaries,
 * retries, uploads and restored records do not re-evaluate external business state. Implementations
 * must be fast and non-blocking. Ordinary callback failures reject that candidate only.</p>
 */
@FunctionalInterface
public interface FTLastInteractionIdentifier {
    /**
     * @param context immutable facts about the selected interaction and next View; never {@code null}
     * @return whether to use this interaction for the next View's metric
     */
    boolean validate(@NonNull FTPreviousViewLastInteractionContext context);
}
