package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.R;
import com.ft.application.MockApplication;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/26 18:36:58
 * description:事件日志，验证页面是否生成对应的事件数据
 */
@RunWith(AndroidJUnit4.class)
public class LogEventTest extends BaseTest {
    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


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
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setGeoKey(true, AccountUtils.getProperty(context, AccountUtils.GEO_KEY))
                .setNeedBindUser(false)//是否需要绑定用户信息
                .setTraceSamplingRate(0.5f)
                .setNetworkTrace(true)
                .setEventFlowLog(true);
        //关闭数据自动同步操作
//        SyncTaskManager.get().setRunning(true);
        stopSyncTask();
        FTSdk.install(ftSDKConfig);
    }

    /**
     * 测试点击某个按钮
     *
     * @throws InterruptedException
     */
    @Test
    public void clickLambdaBtnTest() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.mock_click_btn)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
        boolean value = false;
        for (SyncJsonData recordData : recordDataList) {
            if (recordData.toString().contains("mock_click_btn")) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }

    @Test
    public void leaveTest() throws InterruptedException {
        onView(withId(R.id.mock_page_jump_btn)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
        boolean value = false;
        for (SyncJsonData recordData : recordDataList) {
            if (recordData.toString().contains("leave")) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }

    @Test
    public void enterTest() throws InterruptedException {
        onView(withId(R.id.mock_page_jump_btn)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
        boolean value = false;
        for (SyncJsonData recordData : recordDataList) {
            System.out.println(recordData.toString());
            if (recordData.toString().contains("enter")) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }

    @Test
    public void launchTest() throws InterruptedException {
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
        boolean value = false;
        for (SyncJsonData recordData : recordDataList) {
            if (recordData.toString().contains("launch")) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }
}