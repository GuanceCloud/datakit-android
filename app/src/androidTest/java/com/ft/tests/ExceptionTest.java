package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.Main2Activity;
import com.ft.R;
import com.ft.application.MockApplication;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/27 11:21:02
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class ExceptionTest extends BaseTest {
    @Rule
    public ActivityScenarioRule<Main2Activity> rule = new ActivityScenarioRule<>(Main2Activity.class);


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
                .setDebug(true)//设置是否是 debug
                .setDescLog(true)
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setGeoKey(true, AccountUtils.getProperty(context, AccountUtils.GEO_KEY))
                .setNeedBindUser(false)//是否需要绑定用户信息
                .setPageVtpDescEnabled(true)
                .setMonitorType(MonitorType.ALL)//设置监控项
                .trackNetRequestTime(true)
                .setEnableTrackAppCrash(true)
                .setEnv("dev")
                .setTraceSamplingRate(0.5f)
                .setNetworkTrace(true)
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setTraceType(TraceType.SKYWALKING_V2)
                .setOnlySupportMainProcess(true);
        //关闭数据自动同步操作
//        SyncTaskManager.get().setRunning(true);
        stopSyncTask();
    }

    /**
     * 模拟崩溃，查看崩溃信息是否记录到数据库中
     *
     * @throws InterruptedException
     */
    @Test
    public void mockExceptionTest() throws InterruptedException {
        FTSdk.install(ftSDKConfig);
        avoidCrash();
        //产生一个崩溃信息
        onView(ViewMatchers.withId(R.id.mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
        boolean value = false;
        for (SyncJsonData recordData : recordDataList) {
            //查看数据库中数据是否有该条崩溃异常
            if (recordData.toString().contains("ArithmeticException")) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }

    private void avoidCrash() {
        try {
            Whitebox.setInternalState(FTExceptionHandler.get(),"isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
