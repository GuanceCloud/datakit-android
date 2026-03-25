package com.ft.sdk.tests;

import static com.ft.sdk.garble.utils.Constants.DEFAULT_SERVICE_NAME;
import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTRemoteConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTUIBlockManager;
import com.ft.sdk.SyncPageSize;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Configuration parameter debugging, to avoid low-level code errors causing configuration bugs
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
     * Validate default value for empty service name parameter
     */
    @Test
    public void emptyServiceName() {
        serviceNameParamTest(null, DEFAULT_SERVICE_NAME);
    }

    /**
     * Normal service name parameter
     */
    @Test
    public void normalServiceName() {
        serviceNameParamTest("Test", "Test");
    }

    /**
     * Validate default value for env
     */
    @Test
    public void emptyEnv() {
        envEnumParamTest(null, EnvType.PROD);
    }

    /**
     * Validate env parameter
     */
    @Test
    public void enumEnv() {
        envEnumParamTest(EnvType.GRAY, EnvType.GRAY);
    }

    /**
     * Validate env parameter
     */
    @Test
    public void customEnv() {
        envStringParamTest("custom_env", "custom_env");
    }

    /**
     * Validate appId setting
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
     * Integer compatible mode
     */
    @Test
    public void enableDataIntegerCompatible() {
        FTSdk.install(getDatakitConfig().enableDataIntegerCompatible());
        Assert.assertTrue(FTSdk.get().getBaseConfig().isEnableDataIntegerCompatible());
        Assert.assertTrue(getSDKConfigInSyncDataHelper().isEnableDataIntegerCompatible());
    }

    /**
     * Validate log cache entry count configuration, custom value, default value, and minimum value limit
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
     * Validate {@link FTSDKConfig#pageSize} configuration
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
     * Validate custom {@link FTSDKConfig#pageSize} configuration, and also validate the minimum value
     */
    @Test
    public void customSyncDataPageSize() {
        FTSDKConfig config = getDatakitConfig().setCustomSyncPageSize(1000);
        FTSdk.install(config);
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                1000));

        //Validate minimum value
        FTSdk.install(config.setCustomSyncPageSize(1));
        Assert.assertTrue(checkInnerFieldValue(SyncTaskManager.get(), "pageSize",
                SyncPageSize.MINI.getValue()));
    }

    /**
     * Validate {@link  FTSDKConfig#syncSleepTime}
     */
    @Test
    public void syncSleepTime() {
        FTSDKConfig config = getDatakitConfig().setSyncSleepTime(10000);
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
     * Validate {@link FTSDKConfig#autoSync} configuration
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
     * Validate Datakit configuration
     */
    @Test
    public void datakitUrl() {
        FTSdk.install(getDatakitConfig());
        Assert.assertEquals(TEST_FAKE_URL, FTHttpConfigManager.get().getDatakitUrl());
    }

    /**
     * Validate dataway configuration
     */
    @Test
    public void datawayConfig() {
        FTSdk.install(getDatawaytConfig());
        Assert.assertEquals(TEST_FAKE_URL, FTHttpConfigManager.get().getDatawayUrl());
        Assert.assertEquals(TEST_FAKE_CLIENT_TOKEN, FTHttpConfigManager.get().getClientToken());
    }

    /**
     * Validate dynamically switching to Datakit clears Dataway related configuration
     */
    @Test
    public void setDatakitUrl() {
        FTSdk.install(getDatawaytConfig());
        String datakitUrl = TEST_FAKE_URL;

        FTSdk.setDatakitUrl(datakitUrl);

        Assert.assertEquals(datakitUrl, FTHttpConfigManager.get().getDatakitUrl());
        Assert.assertEquals("", FTHttpConfigManager.get().getDatawayUrl());
        Assert.assertEquals("", FTHttpConfigManager.get().getClientToken());
    }

    /**
     * Validate dynamically switching to Dataway clears Datakit related configuration
     */
    @Test
    public void setDatawayUrl() {
        FTSdk.install(getDatakitConfig());
        String datawayUrl = TEST_FAKE_URL;
        String clientToken = TEST_FAKE_CLIENT_TOKEN;

        FTSdk.setDatawayUrl(datawayUrl, clientToken);

        Assert.assertEquals("", FTHttpConfigManager.get().getDatakitUrl());
        Assert.assertEquals(datawayUrl, FTHttpConfigManager.get().getDatawayUrl());
        Assert.assertEquals(clientToken, FTHttpConfigManager.get().getClientToken());
    }

    /**
     * Validate dynamically setting Datakit URL triggers pending remote config initialization
     */
    @Test
    public void setDatakitUrlTriggerPendingRemoteConfigInit() {
        FTSdk.install(getDatakitConfig());
        FTRemoteConfigManager remoteConfigManager = bindPendingRemoteConfig(TEST_PENDING_RUM_APP_ID);

        FTSdk.setDatakitUrl(TEST_FAKE_URL);

        Assert.assertEquals(TEST_PENDING_RUM_APP_ID, Whitebox.getInternalState(remoteConfigManager, "appId"));
        Assert.assertNull(getPendingRemoteConfigAppId());
    }

    /**
     * Validate dynamically setting Dataway URL triggers pending remote config initialization
     */
    @Test
    public void setDatawayUrlTriggerPendingRemoteConfigInit() {
        FTSdk.install(getDatakitConfig());
        FTRemoteConfigManager remoteConfigManager = bindPendingRemoteConfig(TEST_PENDING_RUM_APP_ID);

        FTSdk.setDatawayUrl(TEST_FAKE_URL, TEST_FAKE_CLIENT_TOKEN);

        Assert.assertEquals(TEST_PENDING_RUM_APP_ID, Whitebox.getInternalState(remoteConfigManager, "appId"));
        Assert.assertNull(getPendingRemoteConfigAppId());
    }


    /**
     * Validate serviceName
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
     * Validate env enum parameter
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
     * Validate custom env parameter
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

    private FTRemoteConfigManager bindPendingRemoteConfig(String appId) {
        FTRemoteConfigManager remoteConfigManager = new FTRemoteConfigManager(0, null);
        Whitebox.setInternalState(remoteConfigManager, "running", new AtomicBoolean(true));
        Whitebox.setInternalState(FTSdk.get(), "mRemoteConfigManager", remoteConfigManager);
        Whitebox.setInternalState(FTSdk.get(), "pendingRemoteConfigAppId", appId);
        return remoteConfigManager;
    }


    /**
     * Validate UIBlock parameter default value and
     */
    @Test
    public void rumUIBlockParamsTest() {
        FTSdk.install(getDatakitConfig());
        //Validate
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID)
                .setEnableTrackAppUIBlock(true));
        Assert.assertEquals(FTUIBlockManager.DEFAULT_TIME_BLOCK_MS, getLongTaskBlockDurationMS());

        //Set greater than 100 ms
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID)
                .setEnableTrackAppUIBlock(true,
                        1000));
        Assert.assertEquals(1000, getLongTaskBlockDurationMS());

        //Validate minimum setting
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID)
                .setEnableTrackAppUIBlock(true,
                        90));
        Assert.assertEquals(getUIBlockMiniBlockDurationMS(), getLongTaskBlockDurationMS());
    }


    @Override
    public void tearDown() {
        FTSdk.shutDown();
    }
}
