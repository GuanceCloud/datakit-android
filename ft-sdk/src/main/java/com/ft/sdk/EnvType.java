package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

/**
 * Data environment, generally used to distinguish and isolate data from different production lines. Data affects the <a href="https://docs.guance.com/logs/explorer/">Log Explorer</a> and <a href="https://docs.guance.com/real-user-monitoring/android/app-analysis/">Android App Analysis</a> in user access monitoring.
 * The filtering field used is {@link Constants#KEY_ENV } (environment)
 * <p>
 */
public enum EnvType {
    /**
     * Production environment, default environment
     */
    PROD,
    /**
     * Gray environment
     */
    GRAY,
    /**
     * Pre-release environment
     */
    PRE,
    /**
     * Daily environment
     */
    COMMON,
    /**
     * Local environment
     */
    LOCAL;


    /**
     * Used for line protocol parameter passing, all lowercase in the protocol
     *
     * @return Returns prod, gray, pre, common, local
     */
    @NonNull
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}

