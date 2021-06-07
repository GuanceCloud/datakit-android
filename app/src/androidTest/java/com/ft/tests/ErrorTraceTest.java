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
import com.ft.sdk.EnvType;
import com.ft.sdk.FTExceptionHandler;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
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
 * description: UIblock ANR nativeCrash 需要手动测试，测试用例无法模拟
 */
@RunWith(AndroidJUnit4.class)
public class ErrorTraceTest extends BaseTest {
    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


    Context context;
    FTSDKConfig ftSDKConfig;

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = MockApplication.getContext();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
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
        ftSDKConfig.setEnableTrackAppCrash(true);
        FTSdk.install(ftSDKConfig);
        avoidCrash();
        //产生一个崩溃信息
        onView(ViewMatchers.withId(R.id.main_mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        Assert.assertTrue(checkLogContent("ArithmeticException"));

    }

//    @Test
//    public void mockNativeException() throws InterruptedException {
//        ftSDKConfig.setEnableTrackAppCrash(true);
//        FTSdk.install(ftSDKConfig);
//        avoidCrash();
//        //产生一个崩溃信息
//        onView(ViewMatchers.withId(R.id.mock_crash_native_btn)).perform(ViewActions.scrollTo()).perform(click());
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(10000);
//                    countDownLatch.countDown();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//        countDownLatch.await();
//
//        Assert.assertTrue(checkLogContent("MyException"));
//
//        Assert.assertTrue(checkTrackWith(OP.CRASH));
//
//    }

//    @Test //
//    public void mockANRTest() throws InterruptedException {
//        ftSDKConfig.setEnableTrackAppANR(true);
//
//        FTSdk.install(ftSDKConfig);
//        onView(ViewMatchers.withId(R.id.mock_anr_btn)).perform(ViewActions.scrollTo()).perform(click());
//        Thread.sleep(2000);
//
//        Assert.assertTrue(checkLogContent("ANR"));
//    }

//    @Test
//    public void mockUIBlockTest() throws InterruptedException {
//        ftSDKConfig.setEnableTrackAppUIBlock(true);
//
//        FTSdk.install(ftSDKConfig);
//        onView(ViewMatchers.withId(R.id.mock_ui_block_btn)).perform(ViewActions.scrollTo()).perform(click());
//
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(10000);
//                    countDownLatch.countDown();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//        countDownLatch.await();
//        Assert.assertTrue(checkLogContent("UIBlock"));
//    }

    /**
     * 检验 Log 包含的内容
     *
     * @param content
     * @return
     */
    private boolean checkLogContent(String content) {
        List<SyncJsonData> recordDataTrackList = FTDBManager.get().queryDataByDataByTypeLimit(0, DataType.LOG);
        boolean isContainLog = false;
        for (SyncJsonData recordData : recordDataTrackList) {
            if (recordData.getDataString().contains(content)) {
                isContainLog = true;
                break;
            }
        }
        return isContainLog;
    }



}
