package com.ft;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Device-level verification for the real loopback HTTP and OkHttp Dispatcher reproduction.
 */
@RunWith(AndroidJUnit4.class)
public class ResourceQueueTimingReproTest extends BaseTest {

    @Before
    public void setUp() throws Exception {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        stopSyncTask();

        FTSDKConfig sdkConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL)
                .setDebug(true)
                .setEnv(EnvType.GRAY);
        FTSdk.install(sdkConfig);
        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserResource(true));
        FTRUMGlobalManager.get().startView("resource-queue-timing-repro-test");
    }

    @Test
    public void identicalQueuedRequestsRecreateResourceBeanNearCompletion() throws Exception {
        CountDownLatch completion = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        ResourceQueueTimingRepro repro = new ResourceQueueTimingRepro();

        try {
            repro.start(1, new ResourceQueueTimingRepro.Listener() {
                @Override
                public void onProgress(String message) {
                    // Progress is displayed by the sample activity; the final evidence is asserted.
                }

                @Override
                public void onComplete(String value) {
                    result.set(value);
                    completion.countDown();
                }
            });

            assertTrue("Reproduction did not complete", completion.await(15, TimeUnit.SECONDS));
            assertNotNull(result.get());
            assertTrue(result.get(), result.get().startsWith("结论：已复现"));
            assertTrue(result.get(), result.get().contains("第一条结束后 ResourceBean 被重建：是"));
            assertTrue(result.get(), result.get().contains("ResourceBean.startTime 距 callStart："));
            assertTrue(result.get(), result.get().contains("推算 Unknown："));
        } finally {
            repro.close();
        }
    }
}
