package com.ft.tests;

import static com.ft.AllTests.hasPrepare;
import static com.ft.utils.RequestUtil.requestUrl;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.garble.bean.DataType;
import com.ft.utils.CheckUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TraceGlobalContextTest extends BaseTest {
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

//        FTSdk.initTraceWithConfig(new FTTraceConfig().addGlobalContext(CUSTOM_KEY, CUSTOM_VALUE));

    }

    @Test
    public void globalContextTest() throws Exception {
        requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        Thread.sleep(3000);
        Assert.assertTrue(CheckUtils.checkValue(DataType.TRACE,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE}, 0));
    }
}
