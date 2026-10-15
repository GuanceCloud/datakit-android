package com.ft.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FTEarlyCrashTrackerTest {
    private static final long START_NS = TimeUnit.HOURS.toNanos(1);
    private static final long DEFAULT_DELAY_MS = 10000;
    private SharedPreferences preferences;

    @Before
    public void setUp() {
        preferences = RuntimeEnvironment.getApplication().getSharedPreferences(
                "early-crash-test", Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    @Test
    public void onlyCrashesWithinInclusiveFiveSecondWindowCreateMarker() {
        for (long elapsedNs : new long[]{0, TimeUnit.SECONDS.toNanos(5) - 1,
                TimeUnit.SECONDS.toNanos(5)}) {
            preferences.edit().clear().commit();
            FTEarlyCrashTracker tracker = start(START_NS);
            tracker.recordCrash(START_NS + elapsedNs);
            assertTrue("elapsed ns: " + elapsedNs, hasMarker());
        }
        for (long elapsedNs : new long[]{-1, TimeUnit.SECONDS.toNanos(5) + 1,
                TimeUnit.SECONDS.toNanos(30)}) {
            preferences.edit().clear().commit();
            FTEarlyCrashTracker tracker = start(START_NS);
            tracker.recordCrash(START_NS + elapsedNs);
            assertFalse("elapsed ns: " + elapsedNs, hasMarker());
        }
    }

    @Test
    public void normalStartupKeepsDefaultDelay() {
        FTEarlyCrashTracker tracker = start(START_NS);
        assertEquals(DEFAULT_DELAY_MS, delay(tracker));
        assertEquals(DEFAULT_DELAY_MS, delay(tracker));
        assertFalse(hasMarker());
    }

    @Test
    public void crashOnlyShortensFirstUploadInNextRun() {
        FTEarlyCrashTracker first = start(START_NS);
        first.recordCrash(START_NS + 1);
        assertEquals(DEFAULT_DELAY_MS, delay(first));

        FTEarlyCrashTracker second = start(START_NS + TimeUnit.MINUTES.toNanos(1));
        assertFalse(hasMarker());
        assertEquals(2000, delay(second));
        assertEquals(DEFAULT_DELAY_MS, delay(second));
        assertEquals(DEFAULT_DELAY_MS, delay(start(START_NS + TimeUnit.MINUTES.toNanos(2))));
    }

    @Test
    public void secondRunConsumesMarkerEvenWithoutAnyUpload() {
        start(START_NS).recordCrash(START_NS + 1);
        start(START_NS + TimeUnit.MINUTES.toNanos(1));
        assertFalse(hasMarker());
        // No scheduling, successful network request, or upload callback is needed to consume it.
        assertEquals(DEFAULT_DELAY_MS, delay(start(START_NS + TimeUnit.MINUTES.toNanos(2))));
    }

    @Test
    public void consecutiveEarlyCrashesRearmNextRun() {
        for (int run = 0; run < 5; run++) {
            long startedAt = START_NS + TimeUnit.MINUTES.toNanos(run);
            FTEarlyCrashTracker tracker = start(startedAt);
            assertEquals(run == 0 ? DEFAULT_DELAY_MS : 2000, delay(tracker));
            tracker.recordCrash(startedAt + TimeUnit.SECONDS.toNanos(5));
            assertTrue(hasMarker());
        }
        assertEquals(2000, delay(start(START_NS + TimeUnit.MINUTES.toNanos(5))));
        assertEquals(DEFAULT_DELAY_MS, delay(start(START_NS + TimeUnit.MINUTES.toNanos(6))));
    }

    @Test
    public void reinstallInSameProcessDoesNotRestartClockOrConsumeNewMarker() {
        FTEarlyCrashTracker tracker = start(START_NS);
        tracker.initialize(preferences, START_NS + TimeUnit.MINUTES.toNanos(1));
        tracker.recordCrash(START_NS + TimeUnit.MINUTES.toNanos(1));
        assertFalse(hasMarker());

        tracker = start(START_NS);
        tracker.recordCrash(START_NS + 1);
        tracker.initialize(preferences, START_NS + TimeUnit.MINUTES.toNanos(1));
        assertTrue(hasMarker());
        assertEquals(DEFAULT_DELAY_MS, delay(tracker));
    }

    @Test
    public void reinstallCannotRestoreAlreadyUsedShortDelay() {
        start(START_NS).recordCrash(START_NS + 1);
        FTEarlyCrashTracker second = start(START_NS + TimeUnit.MINUTES.toNanos(1));
        assertEquals(2000, delay(second));
        second.initialize(preferences, START_NS + TimeUnit.MINUTES.toNanos(2));
        assertEquals(DEFAULT_DELAY_MS, delay(second));
    }

    @Test
    public void crashBeforeInitializationDoesNotCreateMarker() {
        new FTEarlyCrashTracker().recordCrash(START_NS);
        assertFalse(hasMarker());
    }

    @Test
    public void missingOrCorruptStorageKeepsNormalDelay() {
        FTEarlyCrashTracker missing = new FTEarlyCrashTracker();
        missing.initialize((SharedPreferences) null, START_NS);
        missing.recordCrash(START_NS);
        assertEquals(DEFAULT_DELAY_MS, delay(missing));

        preferences.edit().putString(FTEarlyCrashTracker.EARLY_CRASH, "invalid").commit();
        FTEarlyCrashTracker corrupt = start(START_NS);
        assertEquals(DEFAULT_DELAY_MS, delay(corrupt));
        corrupt.recordCrash(START_NS);
        assertTrue(hasMarker());
    }

    @Test
    public void failedConsumptionCannotRepeatedlyShortenLaterRuns() {
        start(START_NS).recordCrash(START_NS);
        SharedPreferences failingStorage = failingCommits();
        for (int run = 0; run < 3; run++) {
            FTEarlyCrashTracker tracker = new FTEarlyCrashTracker();
            tracker.initialize(failingStorage, START_NS);
            assertEquals(DEFAULT_DELAY_MS, delay(tracker));
        }
        assertTrue(hasMarker());
    }

    @Test
    public void failedCrashWriteDoesNotCreateMarker() {
        FTEarlyCrashTracker tracker = new FTEarlyCrashTracker();
        tracker.initialize(failingCommits(), START_NS);
        tracker.recordCrash(START_NS);
        assertFalse(hasMarker());
    }

    private SharedPreferences failingCommits() {
        return (SharedPreferences) Proxy.newProxyInstance(
                SharedPreferences.class.getClassLoader(), new Class<?>[]{SharedPreferences.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("edit")) {
                        return method.invoke(preferences, args);
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    return Proxy.newProxyInstance(SharedPreferences.Editor.class.getClassLoader(),
                            new Class<?>[]{SharedPreferences.Editor.class}, (editorProxy, editMethod, editArgs) -> {
                                if (editMethod.getName().equals("commit")) {
                                    return false;
                                }
                                Object result = editMethod.invoke(editor, editArgs);
                                return result instanceof SharedPreferences.Editor ? editorProxy : result;
                            });
                });
    }

    private FTEarlyCrashTracker start(long startedAtNs) {
        FTEarlyCrashTracker tracker = new FTEarlyCrashTracker();
        tracker.initialize(preferences, startedAtNs);
        return tracker;
    }

    private long delay(FTEarlyCrashTracker tracker) {
        return tracker.consumeInitialUploadDelay(DEFAULT_DELAY_MS);
    }

    private boolean hasMarker() {
        return preferences.getBoolean(FTEarlyCrashTracker.EARLY_CRASH, false);
    }
}
