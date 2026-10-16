package com.ft.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * A crash in the first five seconds of SDK installation affects only the next
 * process run. This state intentionally survives SDK shutdown/reinstallation.
 */
final class FTEarlyCrashTracker {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTEarlyCrashTracker";
    static final String PREFERENCES_PREFIX = "ft_sdk_early_crash_";
    static final String EARLY_CRASH = "early_crash";
    private static final long EARLY_CRASH_WINDOW_NS = TimeUnit.SECONDS.toNanos(5);
    private static final long EARLY_UPLOAD_DELAY_MS = 2000;

    private static FTEarlyCrashTracker instance = new FTEarlyCrashTracker();

    private boolean initialized;
    private long sdkStartTimeNs;
    private SharedPreferences preferences;
    private boolean shortenFirstUpload;
    private boolean crashRecorded;

    static FTEarlyCrashTracker get() {
        return instance;
    }

    synchronized void initialize(Context context, long startedAtNs) {
        if (initialized) {
            return;
        }
        SharedPreferences storage = null;
        try {
            storage = context.getSharedPreferences(
                    PREFERENCES_PREFIX + Utils.getCurrentProcessName(), Context.MODE_PRIVATE);
        } catch (Exception e) {
            LogUtils.e(TAG, "Unable to open early crash preferences: " + e.getMessage());
        }
        initialize(storage, startedAtNs);
    }

    synchronized void initialize(SharedPreferences preferences, long startedAtNs) {
        if (initialized) {
            return;
        }
        initialized = true;
        sdkStartTimeNs = startedAtNs;
        this.preferences = preferences;
        if (preferences == null) {
            return;
        }
        try {
            boolean previousEarlyCrash = preferences.getBoolean(EARLY_CRASH, false);
            if (previousEarlyCrash) {
                // Consume before any upload or new crash, including offline/manual-only runs.
                // If consumption fails, keep the normal delay rather than repeatedly shortening it.
                shortenFirstUpload = preferences.edit().remove(EARLY_CRASH).commit();
                if (!shortenFirstUpload) {
                    LogUtils.e(TAG, "Unable to consume early crash marker");
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Unable to read early crash marker: " + e.getMessage());
        }
    }

    synchronized void recordCrash(long occurredAtNs) {
        long elapsedNs = occurredAtNs - sdkStartTimeNs;
        if (!initialized || crashRecorded || elapsedNs < 0
                || elapsedNs > EARLY_CRASH_WINDOW_NS || preferences == null) {
            return;
        }
        try {
            // apply() can lose the marker when the crashing process exits immediately.
            crashRecorded = preferences.edit().putBoolean(EARLY_CRASH, true).commit();
            if (!crashRecorded) {
                LogUtils.e(TAG, "Unable to persist early crash marker");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Unable to persist early crash marker: " + e.getMessage());
        }
    }

    synchronized long consumeInitialUploadDelay(long defaultDelayMs) {
        boolean shorten = shortenFirstUpload;
        shortenFirstUpload = false;
        return shorten ? EARLY_UPLOAD_DELAY_MS : defaultDelayMs;
    }
}
