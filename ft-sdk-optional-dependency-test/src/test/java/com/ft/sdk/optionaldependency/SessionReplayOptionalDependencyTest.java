package com.ft.sdk.optionaldependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = OptionalDependencyApplication.class, sdk = 28)
public class SessionReplayOptionalDependencyTest {

    @After
    public void tearDown() {
        FTSdk.shutDown();
    }

    @Test
    public void sessionReplayClasspathMatchesFlavor() {
        assertEquals(BuildConfig.HAS_SESSION_REPLAY,
                isClassPresent("com.ft.sdk.sessionreplay.BuildConfig"));
        assertEquals(BuildConfig.HAS_SESSION_REPLAY,
                isClassPresent("com.ft.sdk.sessionreplay.FTSessionReplayConfig"));
    }

    @Test
    public void ftSdkStartsWithOrWithoutSessionReplayDependency() throws Exception {
        FTSdk.install(FTSDKConfig.builder()
                .setOnlySupportMainProcess(false)
                .setAutoSync(false)
                .setEnableAccessAndroidID(false));
        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId("rum-app-id")
                .setEnableTraceWebView(false));

        assertNotNull(FTSdk.get());

        if (BuildConfig.HAS_SESSION_REPLAY) {
            Object replayConfig = Class.forName("com.ft.sdk.sessionreplay.FTSessionReplayConfig")
                    .getConstructor()
                    .newInstance();

            FTSdk.initSessionReplayConfig(replayConfig);

            assertTrue(FTSdk.isSessionReplaySupport());
        } else {
            assertFalse(FTSdk.isSessionReplaySupport());

            FTSdk.initSessionReplayConfig(new Object());

            assertFalse(FTSdk.isSessionReplaySupport());
        }
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
