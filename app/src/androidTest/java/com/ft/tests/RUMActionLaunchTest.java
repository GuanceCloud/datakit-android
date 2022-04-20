package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.utils.CheckUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RUMActionLaunchTest extends BaseTest {

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


    @BeforeClass
    public static void settingBeforeLaunch() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        stopSyncTask();

        Context context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setEnableTrackAppCrash(true)
                .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID))
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
        );

    }


    @Test
    public void rumActionLaunchTest() throws Exception {
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        generateRumData();
        Thread.sleep(1000);

        Assert.assertTrue(CheckUtils.checkValue(DataType.RUM_APP, "launch", 0));
    }


}
