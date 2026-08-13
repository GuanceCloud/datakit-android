package com.ft.sdk;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FTWebViewHandlerTest {

    @Test
    public void bridgeInstallationUsesIndependentRumAndLogSwitches() {
        FTRUMConfig disabledRum = new FTRUMConfig();
        FTRUMConfig enabledRum = new FTRUMConfig().setRumAppId("app-id");
        FTLoggerConfig disabledLog = new FTLoggerConfig();
        FTLoggerConfig enabledLog = new FTLoggerConfig().setEnableWebViewLog(true);

        assertFalse(FTWebViewHandler.shouldInstallWebViewBridge(disabledRum, disabledLog));
        assertTrue(FTWebViewHandler.shouldInstallWebViewBridge(enabledRum, disabledLog));
        assertTrue(FTWebViewHandler.shouldInstallWebViewBridge(disabledRum, enabledLog));
        assertFalse(FTWebViewHandler.shouldInstallWebViewBridge(
                enabledRum.setEnableTraceWebView(false), disabledLog));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void allowedHostsFollowRemoteBaseAndLegacyRumPrecedence() {
        String[] legacy = new String[]{"legacy.example.com"};
        String[] baseHosts = new String[]{"base.example.com"};
        String[] remote = new String[]{"remote.example.com"};
        FTRUMConfig rumConfig = new FTRUMConfig().setAllowWebViewHost(legacy);
        FTSDKConfig baseConfig = FTSDKConfig.builder();

        assertArrayEquals(legacy,
                FTWebViewHandler.resolveAllowedWebViewHosts(baseConfig, rumConfig));

        baseConfig.setAllowWebViewHost(baseHosts);
        assertArrayEquals(baseHosts,
                FTWebViewHandler.resolveAllowedWebViewHosts(baseConfig, rumConfig));

        baseConfig.setRemoteAllowWebViewHost(remote);
        assertArrayEquals(remote,
                FTWebViewHandler.resolveAllowedWebViewHosts(baseConfig, rumConfig));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void explicitNullBaseHostListAllowsAllAndOverridesLegacyRumList() {
        FTSDKConfig baseConfig = FTSDKConfig.builder().setAllowWebViewHost(null);
        FTRUMConfig rumConfig = new FTRUMConfig()
                .setAllowWebViewHost(new String[]{"legacy.example.com"});

        assertNull(FTWebViewHandler.resolveAllowedWebViewHosts(baseConfig, rumConfig));
    }

    @Test
    public void resolveContainerViewIdPrefersCurrentSlotBinding() {
        assertEquals("current-view",
                FTWebViewHandler.resolveContainerViewId("initial-view", "current-view"));
    }

    @Test
    public void resolveContainerViewIdFallsBackToInitialViewWithoutValidBinding() {
        assertEquals("initial-view",
                FTWebViewHandler.resolveContainerViewId("initial-view", null));
        assertEquals("initial-view",
                FTWebViewHandler.resolveContainerViewId("initial-view", ""));
        assertEquals("initial-view",
                FTWebViewHandler.resolveContainerViewId(
                        "initial-view", SessionReplayBridge.NULL_UUID));
    }
}
