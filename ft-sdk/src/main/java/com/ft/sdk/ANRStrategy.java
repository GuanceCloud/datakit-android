package com.ft.sdk;

final class ANRStrategy {
    private static final int HISTORICAL_ANR_MIN_API = 30;

    private ANRStrategy() {
    }

    static boolean shouldUseHistoricalAnr(int sdkInt) {
        return sdkInt >= HISTORICAL_ANR_MIN_API;
    }

    static boolean shouldEnableNativeAnr(boolean enabled, int sdkInt) {
        return enabled && !shouldUseHistoricalAnr(sdkInt);
    }
}
