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
 * 启动 Action 数据校验
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
                .builder(BuildConfig.ACCESS_SERVER_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setEnableTrackAppCrash(true)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
        );
        //plugin 1.2.0 以上版本，需要手动调用
        FTAutoTrack.startApp(null);
    }


    /**
     * 验证应用启动后，是否正常生成 Action 数据
     * @throws Exception
     */
    @Test
    public void rumActionLaunchTest() throws Exception {
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        invokeGenerateRumData();
        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP, "launch"));
    }


}
