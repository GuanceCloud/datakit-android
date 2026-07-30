package com.ft.sdk.garble.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class UtilsAppStartTimeTest {

    @Test
    public void appStartTimeCalculationUsesUptimeClock() {
        long currentNanoTime = TimeUnit.SECONDS.toNanos(100);
        long currentUptimeMs = TimeUnit.SECONDS.toMillis(90);
        long processStartUptimeMs = TimeUnit.SECONDS.toMillis(30);

        long appStartTime = Utils.calculateAppStartTimeNs(
                currentNanoTime,
                currentUptimeMs,
                processStartUptimeMs);

        Assert.assertEquals(TimeUnit.SECONDS.toNanos(40), appStartTime);
    }
}
