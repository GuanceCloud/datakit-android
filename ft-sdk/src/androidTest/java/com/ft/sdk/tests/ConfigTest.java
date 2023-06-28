package com.ft.sdk.tests;

import static com.ft.sdk.garble.utils.Constants.DEFAULT_SERVICE_NAME;
import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 配置传参调试，目的避免避免代码低级错误，导致配置异常出现 bug
 *
 * @author Brandon
 */
public class ConfigTest extends FTBaseTest {


    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
    }

    /**
     * service name 空值参数，默认值验证
     */
    @Test
    public void emptyServiceName() {
        serviceNameParamTest(null, DEFAULT_SERVICE_NAME);
    }

    /**
     * service name 正常传参
     */
    @Test
    public void normalServiceName() {
        serviceNameParamTest("Test", "Test");
    }

    /**
     * env 默认值验证
     */
    @Test
    public void emptyEnv() {
        envParamTest(null, EnvType.PROD);
    }

    /**
     * env 参数验证
     */
    @Test
    public void normalEnv() {
        envParamTest(EnvType.GRAY, EnvType.GRAY);
    }

    /**
     * appid
     */
    @Test
    public void rumAppId() {
        String appid = "appIdxxxxxx";
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(appid));
        Assert.assertEquals(appid, FTRUMConfigManager.get().getConfig().getRumAppId());
        Assert.assertTrue(FTRUMConfigManager.get().isRumEnable());
        Assert.assertEquals(FTRUMConfigManager.get().getConfig().getRumAppId(), appid);
    }

    /**
     * 验证 serviceName
     *
     * @param serviceName
     * @param expected
     */
    private void serviceNameParamTest(String serviceName, String expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig();
        ftSDKConfig.setServiceName(serviceName);
        FTSdk.install(ftSDKConfig);

        Assert.assertEquals(expected, ftSDKConfig.getServiceName());
    }

    /**
     * 验证 env 参数
     *
     * @param env
     * @param expected
     */
    private void envParamTest(EnvType env, EnvType expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setEnv(env);
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected.toString(), FTSdk.get().getBaseConfig().getEnv());
    }

    private FTSDKConfig getDefaultConfig() {
        return FTSDKConfig.builder(TEST_FAKE_URL);
    }

    @Override
    public void tearDown() {
        FTSdk.shutDown();
    }
}
