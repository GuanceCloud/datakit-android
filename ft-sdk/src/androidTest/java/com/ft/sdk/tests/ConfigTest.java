package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;
import static com.ft.sdk.garble.utils.Constants.DEFAULT_LOG_SERVICE_NAME;

import android.os.Looper;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigTest extends FTBaseTest {


    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
    }

    @Test
    public void emptyServiceName() {
        serviceNameParamTest(null, DEFAULT_LOG_SERVICE_NAME);
    }

    @Test
    public void normalServiceName() {
        serviceNameParamTest("Test", "Test");
    }

    @Test
    public void emptyEnv() {
        envParamTest(null, EnvType.PROD);
    }

    @Test
    public void normalEnv() {
        envParamTest(EnvType.GRAY, EnvType.GRAY);
    }

    @Test
    public void rumAppId() {
        String appid = "appIdxxxxxx";
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(appid));
        Assert.assertEquals(appid, FTRUMConfigManager.get().getConfig().getRumAppId());
        Assert.assertTrue(FTRUMConfigManager.get().isRumEnable());
        Assert.assertEquals(FTRUMConfigManager.get().getConfig().getRumAppId(), appid);
    }

    private void serviceNameParamTest(String serviceName, String expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig();
        FTSdk.install(ftSDKConfig);
        FTSdk.initLogWithConfig(new FTLoggerConfig().setServiceName(serviceName));

        Assert.assertEquals(expected, FTLoggerConfigManager.get().getConfig().getServiceName());
    }

    private void envParamTest(EnvType env, EnvType expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setEnv(env);
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected, FTSdk.get().getBaseConfig().getEnv());
    }

    private FTSDKConfig getDefaultConfig() {
        return FTSDKConfig.builder(TEST_FAKE_URL);
    }

    @Override
    public void tearDown() {
        FTSdk.shutDown();
    }
}
