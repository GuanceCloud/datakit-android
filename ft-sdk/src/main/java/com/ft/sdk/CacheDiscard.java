package com.ft.sdk;

public enum CacheDiscard {
    /**
     * Discard new data when the cache limit is reached.
     */
    DISCARD,
    /**
     * Delete oldest cached data when the cache limit is reached.
     */
    DISCARD_OLDEST
}
