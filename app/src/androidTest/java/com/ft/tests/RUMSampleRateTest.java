package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static com.ft.AllTests.hasPrepare;

public class RUMSampleRateTest extends BaseTest {

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);

    static Context context;

    @BeforeClass
    public static void settingBeforeLaunch() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        stopSyncTask();

        context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig().setSamplingRate(0)
                .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID))

        );

    }

    @Test
    public void rumSampleRateZero() throws InterruptedException {

        Thread.sleep(5000);

        List<SyncJsonData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        Assert.assertEquals(0, recordDataList.size());
    }
}
