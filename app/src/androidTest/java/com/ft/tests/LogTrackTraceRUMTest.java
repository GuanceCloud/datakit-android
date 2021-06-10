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
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.RUMGlobalManager;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.manager.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.utils.RequestUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
 * time: 2020/8/25 15:24:06
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class LogTrackTraceRUMTest extends BaseTest {


    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);

    @Before
    public  void init() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        stopSyncTask();

        Context context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setMonitorType(MonitorType.ALL)//设置监控项
                .setEnableTrackAppCrash(true)
                .setEnv(EnvType.GRAY)
                .setNetworkTrace(true)
                .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID))
                .setTraceConsoleLog(true)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
                .setTraceType(TraceType.ZIPKIN);
        FTSdk.install(ftSDKConfig);
        FTDBManager.get().delete();
    }


    /**
     * 插入一条 log 数据测试
     *
     * @throws InterruptedException
     */
    @Test
    public void logInsertDataTest() throws InterruptedException {
        //产生一条日志数据
        FTLogger.getInstance().logBackground("----logInsertDataTest----", Status.CRITICAL);
        //线程池中插入，有一定的时间延迟，这里设置5秒等待时间
        Thread.sleep(5000);
        //从数据库中查询是否有插入的数据
        int except = countInDB(DataType.LOG, "----logInsertDataTest----");
        Assert.assertEquals(1, except);
    }

    /**
     * 上传一条 log 数据测试
     *
     * @throws InterruptedException
     */
    @Test
    public void logUpdateDataTest() throws InterruptedException {
        //产生一条日志数据
        FTLogger.getInstance().logBackground("----logUpdateDataTest----", Status.CRITICAL);
        //线程池中插入，有一定的时间延迟，这里设置5秒等待时间
        Thread.sleep(5000);
        uploadData(DataType.LOG);
    }


    /**
     * 同步删除测试
     *
     * @throws InterruptedException
     */
    @Test
    public void logSyncTest() throws Exception {
        resumeSyncTask();
        FTLogger.getInstance().logBackground("----logUploadTest----", Status.CRITICAL);
        Thread.sleep(12000);
        int except = countInDB(DataType.LOG, "----logUploadTest----");
        Assert.assertEquals(0, except);
    }


    /**
     * 数据存储过程中，浮点型是否会变为整型
     *
     * @throws JSONException
     * @throws InterruptedException
     */

    @Test
    public void trackFloatDoubleDataTest() throws JSONException, InterruptedException {
        JSONObject fields = new JSONObject();
        fields.put("floatValue", 0f);
        fields.put("doubleValue", 0d);
        FTTrack.getInstance().trackBackground("TestLog", null, fields);
        Thread.sleep(5000);

        List<SyncJsonData> list = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.TRACK);
        Assert.assertTrue(list.size() > 0);
        String content = list.get(0).getDataString();
        JSONObject json = new JSONObject(content);
        Assert.assertTrue(json.getJSONObject("fields").optString("floatValue").contains("."));
        Assert.assertTrue(json.getJSONObject("fields").optString("doubleValue").contains("."));
    }

    /**
     * 上传一条 track 数据测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void trackUploadDataTest() throws InterruptedException, JSONException {
        simpleTrackData();
        Thread.sleep(5000);
        uploadData(DataType.TRACK);
    }

    /**
     * 同步删除测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void trackSyncTest() throws Exception {
        simpleTrackData();
        Thread.sleep(5000);
        int except1 = countInDB(DataType.TRACK, CONTENT_SIMPLE_TEST);
        Assert.assertTrue(except1 > 0);
        resumeSyncTask();
        executeSyncTask();
        Thread.sleep(12000);
        int except2 = countInDB(DataType.TRACK, CONTENT_SIMPLE_TEST);
        Assert.assertEquals(0, except2);
    }

    @Test
    public void rumTest() throws Exception {
        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(5000);
        int except1 = countInDB(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_VIEW);
        Assert.assertTrue(except1 > 0);
        resumeSyncTask();
        executeSyncTask();
        Thread.sleep(12000);
        int except2 = countInDB(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_VIEW);
        Assert.assertEquals(0, except2);
    }


    @Test
    public void rumUserBindTest() throws InterruptedException {
        FTSdk.get().bindUserData("123456");
        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(5000);
        int except2 = countInDB(DataType.RUM_APP, "\"is_signin\":\"T\"");
        Assert.assertTrue(except2 > 0);


    }

    @Test
    public void rumUserUnBindTest() throws InterruptedException {
        FTSdk.get().bindUserData("123456");
        FTSdk.get().unbindUserData();
        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(5000);
        int except4 = countInDB(DataType.RUM_APP, "\"is_signin\":\"T\"");
        Assert.assertEquals(0, except4);
    }


    /**
     * 测试点击某个按钮
     *
     * @throws InterruptedException
     */
    @Test
    public void rumClickLambdaBtnTest() throws Exception {
        Thread.sleep(100);
        onView(ViewMatchers.withId(R.id.main_mock_click_btn)).perform(ViewActions.scrollTo()).perform(click());
        //第二次操作触发 action close
        onView(ViewMatchers.withId(R.id.main_mock_click_btn)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(1000);

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        boolean value = false;
        for (SyncJsonData recordData : recordDataList) {
            if (recordData.toString().contains("mock_click_btn")) {
                value = true;
                break;
            }
        }
        Assert.assertTrue(value);
    }


//    @Test
//    public void rumActionLaunchTest() throws InterruptedException {
//        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
//        Thread.sleep(1000);
//        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
//        boolean value = false;
//        for (SyncJsonData recordData : recordDataList) {
//            if (recordData.toString().contains("launch")) {
//                value = true;
//                break;
//            }
//        }
//        Assert.assertTrue(value);
//    }

    /**
     * trace 一个正常的网络
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadNormalTest() throws Exception {
        traceDataTest("http://www.weather.com.cn/data/sk/101010100.html", "www.weather.com.cn");
    }

    /**
     * trace 网络超时
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadTimeOutTest() throws Exception {
        traceDataTest("https://www.google.com", "www.google.com");
    }

    /**
     * trace 网络错误
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadErrorTest() throws Exception {
        traceDataTest("https://error.url", "error.url");
    }

    /**
     * 上传一条正常的 trace 数据测试
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    private void traceDataTest(String url, String except) throws Exception {
        RequestUtil.requestUrl(url);
        Thread.sleep(5000);
        resumeSyncTask();
        executeSyncTask();
        int except1 = countInDB(DataType.TRACE, except);
        Thread.sleep(12000);
        int except2 = countInDB(DataType.TRACE, except);
        Assert.assertTrue(except1 > 0);
        Assert.assertEquals(0, except2);
    }

    /**
     * 上传数据测试
     *
     * @param dataType
     */
    private void uploadData(DataType dataType) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);

        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "requestNet", dataType, body,
                    (AsyncCallback) (code, response) -> Assert.assertEquals(200, code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断数据库中包含几条目标数据
     *
     * @param target
     * @return
     */
    private int countInDB(DataType type, String target) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, type);
        int except = 0;
        if (recordDataList != null) {
            for (SyncJsonData data : recordDataList) {
                if (data.getDataString().contains(target)) {
                    except++;
                }
            }
        }
        return except;
    }
}
