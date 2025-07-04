package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.DebugMainActivity;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.test.utils.CheckUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Launch Action data validation
 */
@RunWith(AndroidJUnit4.class)
public class RUMLaunchActionTest extends BaseTest {

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

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setEnableTrackAppCrash(true)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
        );
        //Plugin 1.2.0 and above versions require manual calling
        FTAutoTrack.startApp(null);
    }


    /**
     * Verify whether Action data is normally generated after application launch
     * @throws Exception
     */
    @Test
    public void rumActionLaunchTest() throws Exception {
        //Because data insertion is an asynchronous operation, an interval needs to be set to be able to query the data
        invokeGenerateRumData();
        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP, "launch"));
    }


}
