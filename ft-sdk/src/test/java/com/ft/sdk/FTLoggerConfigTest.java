package com.ft.sdk;

import com.ft.sdk.garble.bean.Status;

import org.junit.Assert;
import org.junit.Test;

public class FTLoggerConfigTest {

    @Test
    public void checkLogLevel_remoteWarnFilterMatchesWarningStatus() {
        FTLoggerConfig config = new FTLoggerConfig()
                .setLogLevelFilters(new String[]{"warn"});

        Assert.assertTrue(config.checkLogLevel("warning"));
        Assert.assertFalse(config.checkLogLevel("error"));
    }

    @Test
    public void checkLogLevel_warningFiltersAreEquivalent() {
        Assert.assertTrue(new FTLoggerConfig()
                .setLogLevelFilters(new String[]{"warn"})
                .checkLogLevel(Status.WARNING.name));
        Assert.assertTrue(new FTLoggerConfig()
                .setLogLevelFilters(new String[]{"warning"})
                .checkLogLevel("warn"));
        Assert.assertTrue(new FTLoggerConfig()
                .setLogLevelFilters(new Status[]{Status.WARNING})
                .checkLogLevel("warn"));
    }
}
