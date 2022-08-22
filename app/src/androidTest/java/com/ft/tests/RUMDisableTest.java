package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BuildConfig;
import com.ft.DebugMainActivity;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RUMDisableTest extends BaseNoRUMDataTest {

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
                .builder(BuildConfig.ACCESS_SERVER_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

    }


}
