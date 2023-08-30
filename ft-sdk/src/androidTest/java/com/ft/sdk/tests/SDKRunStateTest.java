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
 * SDK 配置正确性验证
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
                .setDebug(true)//设置是否是 debug
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
     * 验证 {@link FTRUMConfig#extraMonitorTypeWithError} 配置正确性
     */
    @Test
    public void monitorErrorTypeTest() {
        Assert.assertTrue(FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.ALL));
    }

    /**
     * 验证 {@link FTRUMConfig#deviceMetricsMonitorType} 配置正确性
     */
    @Test
    public void monitorDeviceMetricsMonitorTYpe() {
        Assert.assertTrue(FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL));
    }

    /**
     * 验证 {@link FTTraceConfig#enableAutoTrace} 配置正确性
     */
    @Test
    public void networkTrackTest() {
        Assert.assertTrue(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    /**
     * 验证 {@link FTLoggerConfig#enableConsoleLog} 配置正确性
     */
    @Test
    public void trackConsoleLogTest() {
        Assert.assertTrue(FTLoggerConfigManager.get().getConfig().isEnableConsoleLog());
    }

    /**
     * 验证 {@link FTRUMConfig#enableTraceUserAction} 配置正确性
     */
    @Test
    public void traceUserActionTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserAction());
    }

    /**
     * 验证 {@link FTRUMConfig#enableTraceUserView} 配置正确性
     */
    @Test
    public void traceUserViewTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserView());
    }

    /**
     * 关闭 SDK 后
     * 验证 {@link FTRUMConfig#extraMonitorTypeWithError} 配置正确性
     */
    @Test
    public void showdownMonitorTypeTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.ALL));
    }
    /**
     * 关闭 SDK 后
     * 验证 {@link FTRUMConfig#enableAutoTrace} 配置正确性
     */
    @Test
    public void showdownNetworkTrackTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    /**
     * 关闭 SDK 后
     * 验证 Log 相关配置正确性
     */
    @Test
    public void showdownTrackConsoleLogTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTLoggerConfigManager.get().getConfig());
    }

    /**
     * 关闭 SDK 后
     * 验证 RUM 相关配置正确性
     */
    @Test
    public void showdownRUMTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTRUMConfigManager.get().getConfig());
    }
}
