package com.ft.sdk;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FTVisitorIdManagerTest {
    @org.junit.Before public void disableAndroidLogging() {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
    }
    static class MemoryStore implements FTVisitorIdManager.Store {
        String record;
        boolean readable = true;
        boolean writable = true;
        int writes;

        @Override public String read() {
            if (!readable) throw new IllegalStateException("unavailable");
            return record;
        }
        @Override public boolean write(String value) {
            writes++;
            if (!writable) return false;
            record = value;
            return true;
        }
    }
    static class TestClock implements FTVisitorIdManager.Clock {
        long now = 1000000;
        @Override public long now() { return now; }
    }
    static FTVisitorIdConfig enabled() {
        return new FTVisitorIdConfig().setHeader("x-visitor-id")
                .addMatch("https://api.example.com/");
    }

    @Test public void defaultDisabledDoesNotTouchStorage() {
        MemoryStore store = new MemoryStore() {
            @Override public String read() { throw new AssertionError("Disabled Visitor ID read storage"); }
            @Override public boolean write(String value) { throw new AssertionError("Disabled Visitor ID wrote storage"); }
        };
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(new FTRUMConfig().getVisitorIdConfig(), store, new TestClock());
        manager.reset();
        assertFalse(manager.isEnabled());
        assertNull(manager.getHeader());
        assertNull(manager.getId());
        assertEquals(0, store.writes);
    }

    @Test public void settingConfigEnablesAndNullDisablesWithoutRotatingStoredId() {
        MemoryStore store = new MemoryStore();
        TestClock clock = new TestClock();
        FTRUMConfig rum = new FTRUMConfig().setVisitorIdConfig(enabled());
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(rum.getVisitorIdConfig(), store, clock);
        assertTrue(manager.isEnabled());
        String id = manager.getId();
        assertNotNull(id);

        rum.setVisitorIdConfig(null);
        manager.init(rum.getVisitorIdConfig(), store, clock);
        manager.reset();
        assertFalse(manager.isEnabled());
        assertNull(manager.getId());
        assertNull(manager.getConfigSnapshot());
        assertFalse(manager.matches("https://api.example.com/"));
        assertEquals(1, store.writes);

        manager.init(enabled(), store, clock);
        assertEquals(id, manager.getId());
        assertEquals(1, store.writes);
    }

    @Test public void incompleteConfigDisablesVisitorRatherThanEnablingInvalidHeaders() {
        FTVisitorIdManager manager = new FTVisitorIdManager();
        MemoryStore store = new MemoryStore();
        manager.init(new FTVisitorIdConfig(), store, new TestClock());
        assertFalse(manager.isEnabled());
        assertNull(manager.getId());
        assertEquals(0, store.writes);
    }

    @Test public void reusesPersistentIdAcrossRestartAndDoesNotSlideTtl() {
        MemoryStore store = new MemoryStore();
        TestClock clock = new TestClock();
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(enabled(), store, clock);
        String first = manager.getId();
        assertTrue(first.matches("p[0-9a-f]{32}"));
        clock.now += 86400000 - 1;
        FTVisitorIdManager restarted = new FTVisitorIdManager();
        restarted.init(enabled(), store, clock);
        assertEquals(first, restarted.getId());
        assertEquals(1, store.writes);
        clock.now++;
        assertNotEquals(first, restarted.getId());
        assertEquals(2, store.writes);
    }

    @Test public void resetWithFailedStorageStaysStableAndNeverReloadsOldIdInProcess() {
        MemoryStore store = new MemoryStore();
        TestClock clock = new TestClock();
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(enabled(), store, clock);
        String old = manager.getId();
        store.writable = false;
        manager.reset();
        String fallback = manager.getId();
        assertTrue(fallback.matches("s[0-9a-f]{32}"));
        assertNotEquals(old, fallback);
        store.writable = true;
        manager.init(enabled(), store, clock);
        assertEquals(fallback, manager.getId());
        clock.now += 86400000;
        String recovered = manager.getId();
        assertTrue(recovered.startsWith("p"));
        assertNotEquals(old, recovered);
        assertNotEquals(fallback, recovered);
    }

    @Test public void unreadableStorageFallsBackAndRepeatedReadsDoNotWrite() {
        MemoryStore store = new MemoryStore();
        store.readable = false;
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(enabled(), store, new TestClock());
        String id = manager.getId();
        assertTrue(id.startsWith("s"));
        for (int i = 0; i < 100; i++) assertEquals(id, manager.getId());
        assertEquals(1, store.writes);
    }

    @Test public void corruptFutureAndInvalidRecordsHeal() {
        for (String record : new String[]{"bad", "pbad:1", "s11111111111111111111111111111111:1",
                "p11111111111111111111111111111111:9999999", "p11111111111111111111111111111111:-1"}) {
            MemoryStore store = new MemoryStore();
            store.record = record;
            FTVisitorIdManager manager = new FTVisitorIdManager();
            manager.init(enabled(), store, new TestClock());
            assertTrue(manager.getId().startsWith("p"));
            assertEquals(1, store.writes);
            assertNotEquals(record, store.record);
        }
    }

    @Test public void clockRollbackRotatesAndRepeatedSameTimeResetsAreUnique() {
        MemoryStore store = new MemoryStore();
        TestClock clock = new TestClock();
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(enabled(), store, clock);
        String id = manager.getId();
        clock.now--;
        assertNotEquals(id, manager.getId());
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            manager.reset();
            assertTrue(ids.add(manager.getId()));
        }
    }

    @Test public void concurrentExpiredReadsGenerateOneNewId() throws Exception {
        MemoryStore store = new MemoryStore();
        TestClock clock = new TestClock();
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(enabled(), store, clock);
        String old = manager.getId();
        clock.now += 86400000;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < 100; i++) calls.add(manager::getId);
            Set<String> ids = new HashSet<>();
            for (Future<String> result : pool.invokeAll(calls)) ids.add(result.get());
            assertEquals(1, ids.size());
            assertFalse(ids.contains(old));
            assertEquals(2, store.writes);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test public void initializationCopiesMutableConfigurationAndInvalidConfigDisables() {
        FTVisitorIdConfig config = enabled();
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(config, new MemoryStore(), new TestClock());
        config.setHeader("other");
        assertTrue(manager.isEnabled());
        assertEquals("x-visitor-id", manager.getHeader());
        manager.init(enabled().setHeader("Authorization"), new MemoryStore(), new TestClock());
        assertFalse(manager.isEnabled());
        assertNull(manager.getId());
    }
}
