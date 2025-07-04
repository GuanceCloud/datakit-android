package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verify whether the version fields are assigned correctly
 */
@RunWith(AndroidJUnit4.class)
public class ConstantCheckTest extends BaseTest {

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTSDKConfig ftsdkConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL);
        FTSdk.install(ftsdkConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig());

    }


    /**
     * Verify ft-plugin version number
     *
     * {@link com.ft.sdk.garble.utils.Constants#KEY_RUM_SDK_PACKAGE_TRACK}
     */
    @Test
    public void pluginVersionTest() {
        Assert.assertFalse(FTSdk.PLUGIN_VERSION.isEmpty());
    }

    /**
     * Verify Application uuid
     *
     * {@link com.ft.sdk.garble.utils.Constants#KEY_APPLICATION_UUID}
     */
    @Test
    public void applicationUUIDTest() {
        Assert.assertFalse(FTSdk.PACKAGE_UUID.isEmpty());

    }

    /**
     * Verify ft-native version number
     * {@link com.ft.sdk.garble.utils.Constants#KEY_RUM_SDK_PACKAGE_NATIVE}
     */
    @Test
    public void nativeLibVersionTest() {
        Assert.assertFalse(FTSdk.NATIVE_VERSION.isEmpty());
    }
}
