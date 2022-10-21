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

@RunWith(AndroidJUnit4.class)
public class SDKRunStateTest extends FTBaseTest {


    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(TEST_FAKE_URL)
                .setXDataKitUUID("ft-dataKit-uuid-001")//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);

        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL)
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL)
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

    @Test
    public void monitorErrorTypeTest() {
        Assert.assertTrue(FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.ALL));
    }

    @Test
    public void monitorDeviceMetricsMonitorTYpe() {
        Assert.assertTrue(FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL));
    }

    @Test
    public void networkTrackTest() {
        Assert.assertTrue(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    @Test
    public void trackConsoleLogTest() {
        Assert.assertTrue(FTLoggerConfigManager.get().getConfig().isEnableConsoleLog());
    }

    @Test
    public void traceUserActionTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserAction());
    }

    @Test
    public void traceUserViewTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserView());
    }


    @Test
    public void showdownMonitorTypeTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.ALL));
    }

    @Test
    public void showdownNetworkTrackTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    @Test
    public void showdownTrackConsoleLogTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTLoggerConfigManager.get().getConfig());
    }

    @Test
    public void showdownEventFlowLogTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTRUMConfigManager.get().getConfig());
    }
}
