package com.ft.sdk.internal.anr.historical;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExitKeyFactoryTest {

    @Test
    public void canonicalKeyIncludesPackageProcessPidTimestampAndReason() {
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app:worker",
                321,
                1_700_000_000_123L,
                6,
                100,
                null);

        assertEquals(
                "834d214fd88bba724e8ebf3ed4aeacb4c57662933363900ee9c151836ec249c3",
                ExitKeyFactory.create("com.example.app", exit));
    }

    @Test
    public void samePidAndTimestampInDifferentProcessesDoNotCollide() {
        ProcessExitRecord main = new ProcessExitRecord(
                "com.example.app", 321, 1000L, 6, 100, null);
        ProcessExitRecord worker = new ProcessExitRecord(
                "com.example.app:worker", 321, 1000L, 6, 100, null);

        assertNotEquals(
                ExitKeyFactory.create("com.example.app", main),
                ExitKeyFactory.create("com.example.app", worker));
    }

    @Test
    public void reusedPidAtDifferentTimestampsDoesNotCollide() {
        ProcessExitRecord first = new ProcessExitRecord(
                "com.example.app", 321, 1000L, 6, 100, null);
        ProcessExitRecord second = new ProcessExitRecord(
                "com.example.app", 321, 1001L, 6, 100, null);

        assertNotEquals(
                ExitKeyFactory.create("com.example.app", first),
                ExitKeyFactory.create("com.example.app", second));
    }
}
