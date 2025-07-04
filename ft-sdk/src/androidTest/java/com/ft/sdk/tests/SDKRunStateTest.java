package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceConfigManager;
import com.ft.sdk.ErrorMonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.FTMonitorManager;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SDK configuration correctness verification
 */

@RunWith(AndroidJUnit4.class)
public class SDKRunStateTest extends FTBaseTest {


    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(TEST_FAKE_URL)
                .setDebug(true)//Set whether it is debug
                .setEnv(EnvType.GRAY);

        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue())
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
        );

        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.ZIPKIN_MULTI_HEADER));
    }

    /**
     * Verify {@link FTRUMConfig#extraMonitorTypeWithError} configuration correctness
     */
    @Test
    public void monitorErrorTypeTest() {
        Assert.assertTrue(FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.ALL));
    }

    /**
     * Verify {@link FTRUMConfig#deviceMetricsMonitorType} configuration correctness
     */
    @Test
    public void monitorDeviceMetricsMonitorTYpe() {
        Assert.assertTrue(FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL));
    }

    /**
     * Verify {@link FTTraceConfig#enableAutoTrace} configuration correctness
     */
    @Test
    public void networkTrackTest() {
        Assert.assertTrue(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    /**
     * Verify {@link FTLoggerConfig#enableConsoleLog} configuration correctness
     */
    @Test
    public void trackConsoleLogTest() {
        Assert.assertTrue(FTLoggerConfigManager.get().getConfig().isEnableConsoleLog());
    }

    /**
     * Verify {@link FTRUMConfig#enableTraceUserAction} configuration correctness
     */
    @Test
    public void traceUserActionTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserAction());
    }

    /**
     * Verify {@link FTRUMConfig#enableTraceUserView} configuration correctness
     */
    @Test
    public void traceUserViewTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserView());
    }

    /**
     * After shutting down SDK
     * Verify {@link FTRUMConfig#extraMonitorTypeWithError} configuration correctness
     */
    @Test
    public void showdownMonitorTypeTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.ALL));
    }
    /**
     * After shutting down SDK
     * Verify {@link FTRUMConfig#enableAutoTrace} configuration correctness
     */
    @Test
    public void showdownNetworkTrackTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    /**
     * After shutting down SDK
     * Verify Log related configuration correctness
     */
    @Test
    public void showdownTrackConsoleLogTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTLoggerConfigManager.get().getConfig());
    }

    /**
     * After shutting down SDK
     * Verify RUM related configuration correctness
     */
    @Test
    public void showdownRUMTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTRUMConfigManager.get().getConfig());
    }
}
