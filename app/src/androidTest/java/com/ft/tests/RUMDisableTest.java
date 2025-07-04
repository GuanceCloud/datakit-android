package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BuildConfig;
import com.ft.DebugMainActivity;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * No RUM configuration, data validation
 */
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

        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL)
                .setDebug(true)//Set whether it is debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

    }


}
