package com.ft.sdk.tests;

import static com.ft.sdk.garble.utils.Constants.DEFAULT_SERVICE_NAME;
import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.SyncPageSize;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.Constants;
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
        envEnumParamTest(null, EnvType.PROD);
    }

    /**
     * env 参数验证
     */
    @Test
    public void enumEnv() {
        envEnumParamTest(EnvType.GRAY, EnvType.GRAY);
    }

    /**
     * env 参数验证
     */
    @Test
    public void customEnv() {
        envStringParamTest("custom_env", "custom_env");
    }

    /**
     * appId 设置验证
     */
    @Test
    public void rumAppId() {
        FTSdk.install(getDatakitConfig());
        String appId = "appIdxxxxxx";
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(appId));
        Assert.assertEquals(appId, FTRUMConfigManager.get().getConfig().getRumAppId());
        Assert.assertTrue(FTRUMConfigManager.get().isRumEnable());
        Assert.assertEquals(FTRUMConfigManager.get().getConfig().getRumAppId(), appId);
    }

    /**
     * Integer 兼容模式
     */
    @Test
    public void enableDataIntegerCompatible() {
        FTSdk.install(getDatakitConfig().enableDataIntegerCompatible());
        Assert.assertTrue(FTSdk.get().getBaseConfig().isEnableDataIntegerCompatible());
        Assert.assertTrue(getSDKConfigInSyncDataHelper().isEnableDataIntegerCompatible());
    }

    /**
     * 日志条缓存条目数配置验证，自定义数值，默认数值，和最小数值限制
     */
    @Test
    public void logCacheLimit() {
        FTSdk.install(getDatakitConfig());
        FTSdk.initLogWithConfig(new FTLoggerConfig());
        //default
        Assert.assertEquals(Constants.DEFAULT_DB_LOG_CACHE_NUM, FTDBCachePolicy.get().getLogLimitCount());

        FTSdk.initLogWithConfig(new FTLoggerConfig().setLogCacheLimitCount(0));
        Assert.assertEquals(Constants.MINI_DB_LOG_CACHE_NUM, FTDBCachePolicy.get().getLogLimitCount());

        int customCount = 10000;
        FTSdk.initLogWithConfig(new FTLoggerConfig().setLogCacheLimitCount(customCount));
        Assert.assertEquals(customCount, FTDBCachePolicy.get().getLogLimitCount());

    }

    /**
     * 验证 {@link FTSDKConfig#pageSize} 配置
     */
    @Test
    public void syncDataPageSize() {
        FTSDKConfig config = getDatakitConfig();
        FTSdk.install(config);
        //default SyncPageSize.MEDIUM
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                SyncPageSize.MEDIUM.getValue()));

        FTSdk.install(config.setSyncPageSize(SyncPageSize.MINI));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                SyncPageSize.MINI.getValue()));


        FTSdk.install(config.setSyncPageSize(SyncPageSize.LARGE));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                SyncPageSize.LARGE.getValue()));
    }

    /**
     * 验证 {@link FTSDKConfig#pageSize} 自定义配置，同时验证最小值
     */
    @Test
    public void customSyncDataPageSize() {
        FTSDKConfig config = getDatakitConfig().setCustomSyncPageSize(1000);
        FTSdk.install(config);
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                1000));

        //验证最小数值
        FTSdk.install(config.setCustomSyncPageSize(1));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                SyncPageSize.MINI.getValue()));
    }

    /**
     * 验证 {@link  FTSDKConfig#syncSleepTime}
     */
    @Test
    public void syncSleepTime() {
        FTSDKConfig config = getDatakitConfig().setSyncSleepTime(1000);
        FTSdk.install(config);
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "syncSleepTime",
                SyncTaskManager.SYNC_SLEEP_MAX_TIME_MS));

        FTSdk.install(config.setSyncSleepTime(50));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "syncSleepTime",
                50));

        FTSdk.install(config.setSyncSleepTime(-1));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "syncSleepTime",
                SyncTaskManager.SYNC_SLEEP_MINI_TIME_MS));
    }


    /**
     * 验证 {@link FTSDKConfig#autoSync} 配置
     */
    @Test
    public void autoSync() {
        FTSDKConfig config = getDatakitConfig();
        FTSdk.install(config);
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "autoSync", true));

        FTSdk.install(config.setAutoSync(false));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "autoSync", false));
    }

    /**
     * 验证 Datakit 配置
     */
    @Test
    public void datakitUrl() {
        FTSdk.install(getDatakitConfig());
        Assert.assertEquals(TEST_FAKE_URL, FTHttpConfigManager.get().getDatakitUrl());
    }

    /**
     * 验证 dataway 配置
     */
    @Test
    public void datawayConfig() {
        FTSdk.install(getDatawaytConfig());
        Assert.assertEquals(TEST_FAKE_URL, FTHttpConfigManager.get().getDatawayUrl());
        Assert.assertEquals(TEST_FAKE_CLIENT_TOKEN, FTHttpConfigManager.get().getClientToken());
    }


    /**
     * 验证 serviceName
     *
     * @param serviceName
     * @param expected
     */
    private void serviceNameParamTest(String serviceName, String expected) {
        FTSDKConfig ftSDKConfig = getDatakitConfig();
        FTSdk.install(ftSDKConfig.setServiceName(serviceName));
        Assert.assertEquals(expected, ftSDKConfig.getServiceName());
    }

    /**
     * 验证 env enum 参数
     *
     * @param env
     * @param expected
     */
    private void envEnumParamTest(EnvType env, EnvType expected) {
        FTSDKConfig ftSDKConfig = getDatakitConfig()
                .setEnv(env);
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected.toString(), FTSdk.get().getBaseConfig().getEnv());
    }

    /**
     * 验证 env 自定义 参数
     *
     * @param env
     * @param expected
     */
    private void envStringParamTest(String env, String expected) {
        FTSDKConfig ftSDKConfig = getDatakitConfig()
                .setEnv(env);
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected, FTSdk.get().getBaseConfig().getEnv());
    }

    @Override
    public void tearDown() {
        FTSdk.shutDown();
    }
}
