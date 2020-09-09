package com.ft.tests;

import android.content.Context;
import android.os.Looper;
import android.widget.Button;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.Main2Activity;
import com.ft.R;
import com.ft.application.MockApplication;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/27 15:33:15
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class FTAutoTrackTest extends BaseTest {
    Context context;
    @Rule
    public ActivityTestRule<Main2Activity> rule = new ActivityTestRule<>(Main2Activity.class);
    FTSDKConfig ftSDKConfig;

    private Map<String, String> eventAliasMap() {
        Map<String, String> aliasMap = new HashMap<String, String>();
        aliasMap.put("Main2Activity/ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatButton/#jump18",
                "仅用于测试用例测试点击产生的日志");
        return aliasMap;
    }
    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = MockApplication.getContext();
//        SyncTaskManager.get().setRunning(true);
        stopSyncTask();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setDescLog(true)
                .setGeoKey(true, AccountUtils.getProperty(context, AccountUtils.GEO_KEY))
                .enableAutoTrack(true)
                .setNeedBindUser(false)//是否需要绑定用户信息
                .setPageVtpDescEnabled(true)
                .setMonitorType(MonitorType.ALL);

    }

    @Test
    public void vtpWhiteTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type);
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();

        String expect = "Main2Activity/ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatButton/#jump18";
        onView(ViewMatchers.withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        for (RecordData recordData : recordDataList) {
            JSONObject jsonObject = new JSONObject(recordData.getOpdata());
            JSONObject tags = jsonObject.getJSONObject(Constants.TAGS);
            String vtp = tags.optString("vtp");
            if (expect.equals(vtp)) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }

    @Test
    public void vtpBlackTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type);
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();
        String expect = "Main2Activity/ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatButton/#jump18";
        onView(withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        try {
            for (RecordData recordData : recordDataList) {
                JSONObject jsonObject = new JSONObject(recordData.getOpdata());
                JSONObject tags = jsonObject.getJSONObject(Constants.TAGS);
                String vtp = tags.optString("vtp");
                if (expect.equals(vtp)) {
                    value = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertFalse(value);
    }

    @Test
    public void activityBlackTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type)
                .setBlackActivityClasses(Collections.singletonList(Main2Activity.class));
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();
        onView(withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        try {
            for (RecordData recordData : recordDataList) {
                if (recordData.getOp().equals("clk")) {
                    value = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertFalse(value);
    }

    @Test
    public void activityWhiteTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type | FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type)
                .setWhiteActivityClasses(Collections.singletonList(Main2Activity.class));
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();

        onView(withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(2000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        try {
            for (RecordData recordData : recordDataList) {
                if (recordData.getOp().equals("clk")) {
                    value = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(value);
    }

    @Test
    public void viewBlackTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type | FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type)
                .setWhiteActivityClasses(Collections.singletonList(Main2Activity.class))
                .setBlackViewClasses(Collections.singletonList(Button.class));
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();

        onView(withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        try {
            for (RecordData recordData : recordDataList) {
                if (recordData.getOp().equals("clk")) {
                    value = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertFalse(value);
    }

    @Test
    public void viewWhiteTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type | FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type)
                .setWhiteActivityClasses(Collections.singletonList(Main2Activity.class))
                .setWhiteViewClasses(Collections.singletonList(Button.class));
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();

        onView(withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(2000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        try {
            for (RecordData recordData : recordDataList) {
                if (recordData.getOp().equals("clk")) {
                    value = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(value);
    }

    @Test
    public void vtpDescTest() throws InterruptedException, JSONException {
        ftSDKConfig.setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                FTAutoTrackType.APP_END.type |
                FTAutoTrackType.APP_START.type)
        .setPageVtpDescEnabled(true)
        .addVtpDesc(eventAliasMap());
        FTSdk.install(ftSDKConfig);
        removeActivityLifeCycle();

        Thread.sleep(1000);
        String expect = "仅用于测试用例测试点击产生的日志";
        onView(withId(R.id.jump18)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
        boolean value = false;
        for (RecordData recordData : recordDataList) {
            JSONObject jsonObject = new JSONObject(recordData.getOpdata());
            JSONObject fields = jsonObject.getJSONObject(Constants.FIELDS);
            String vtp = fields.optString("vtp_desc");
            if (expect.equals(vtp)) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }
}
