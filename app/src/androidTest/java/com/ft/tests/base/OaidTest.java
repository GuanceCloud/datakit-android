package com.ft.tests.base;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.utils.TestUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/27 13:42:32
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class OaidTest extends BaseTest {
    private static final String TAG = "OaidTest";
    Context context;
    FTSDKConfig ftSDKConfig;

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = MockApplication.getContext();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true);//设置 OAID 是否可用
        //关闭数据自动同步操作
        stopSyncTask();

    }

    @Test
    public void oaidTest() {
        FTSdk.install(ftSDKConfig);
        String oaid = OaidUtils.getOAID(context);
        if (TestUtils.isEmulator()) {
            Assert.assertFalse(Utils.isNullOrEmpty(oaid));
        } else {
            Log.d(TAG,"OAID  unavailable in Emulator");
        }
    }
}
