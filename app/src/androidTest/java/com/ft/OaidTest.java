package com.ft;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.manager.SyncTaskManager;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.Utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.ft.TestEntrance.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/27 13:42:32
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class OaidTest {
    Context context;
    FTSDKConfig ftSDKConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = MockApplication.getContext();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setGeoKey(true, AccountUtils.getProperty(context, AccountUtils.GEO_KEY));
        //关闭数据自动同步操作
        SyncTaskManager.get().setRunning(true);
    }

    @Test
    public void oaidTest(){
        FTSdk.install(ftSDKConfig);
        String oaid = OaidUtils.getOAID(context);
        Assert.assertFalse(Utils.isNullOrEmpty(oaid));
    }
}
