package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
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
public class LogGlobalContextTest extends BaseTest {
    public static final String CUSTOM_KEY = "custom_key";
    public static final String CUSTOM_VALUE = "custom_value";

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

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .addGlobalContext(CUSTOM_KEY, CUSTOM_VALUE)
                .setEnableConsoleLog(true)
        );

    }

    @Test
    public void logGlobalContextTest() throws Exception {
        Thread.sleep(1000);
        Log.d("TestLog", "log test");

        Thread.sleep(1000);
        Assert.assertTrue(CheckUtils.checkValue(DataType.LOG,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE}, 0));

    }
}