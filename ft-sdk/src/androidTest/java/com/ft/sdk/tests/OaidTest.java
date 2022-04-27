package com.ft.sdk.tests;

import static com.ft.sdk.FTSdkAllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.TestUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * time: 2020/8/27 13:42:32
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class OaidTest extends FTBaseTest {
    private static final String TAG = "OaidTest";

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(TEST_FAKE_URL)
                .setUseOAID(true);//设置 OAID 是否可用
        FTSdk.install(ftSDKConfig);

        //关闭数据自动同步操作
        FTBaseTest.stopSyncTask();

    }

    @Test
    public void oaidTest() {
        String oaid = OaidUtils.getOAID(getContext());
        if (!TestUtils.isEmulator()) {
            Assert.assertFalse(Utils.isNullOrEmpty(oaid));
        } else {
            Log.d(TAG, "OAID  unavailable in Emulator");
        }
    }
}
