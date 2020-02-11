package com.ft;

import android.content.Intent;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * BY huangDianHua
 * DATE:2019-12-23 17:27
 * Description:
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    /**
     * 在运行测试用例前先删除之前数据库中存在的数据
     */
    @Before
    public void deleteTableData() {
        FTDBManager.get().delete();
        //关闭数据自动同步操作
        SyncTaskManager.get().setRunning(true);
    }

    /**
     * 初始化 SDK 参数
     */
    @Before
    public void initSDK() {
        FTMonitorConfig.get().clear();
        FTSDKConfig ftSDKConfig = FTSDKConfig.Builder(Const.serverUrl,
                true,
                Const.accesskey_id,
                Const.accessKey_secret)
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setNeedBindUser(true)//是否需要绑定用户信息
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setWhiteActivityClasses(Arrays.asList(MainActivity.class, Main2Activity.class))//设置埋点页面的白名单
                .setWhiteViewClasses(Arrays.asList(Button.class, RadioGroup.class))
                .setMonitorType(MonitorType.BATTERY | MonitorType.NETWORK);//设置监控项
        FTSdk.install(ftSDKConfig, FTApplication.getApplication());
    }

    /**
     * 测试点击某个按钮
     *
     * @throws InterruptedException
     */
    @Test
    public void clickLambdaBtnTest() throws InterruptedException {
        onView(withId(R.id.btn_lam)).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试点击多个按钮
     *
     * @throws InterruptedException
     */
    @Test
    public void clickMoreBtnTest() throws InterruptedException {
        onView(withId(R.id.btn_lam)).perform(click());
        onView(withId(R.id.check)).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
    }

    /**
     * 测试打开某个Activity
     *
     * @throws InterruptedException
     */
    @Test
    public void jumpActivityTest() throws InterruptedException {
        rule.getActivity().startActivity(new Intent(rule.getActivity(), Main2Activity.class));
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试通过按钮点击打开Activity
     *
     * @throws InterruptedException
     */
    @Test
    public void clickJumpActivityTest() throws InterruptedException {
        onView(withId(R.id.showKotlinActivity)).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
    }

    /**
     * 测试自动同步操作（该测试用例需要间隔3分钟再执行！！！！）
     *
     * @throws InterruptedException
     */
    @Test
    public void clickMoreBtnAndSyncTest() throws InterruptedException, JSONException {
        //获取查询数据需要用到的token
        String token = getLoginToken();
        assertNotEquals("",token);
        SyncTaskManager.get().setRunning(false);
        onView(withId(R.id.btn_lam)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.check)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.checkbox)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.checkbox)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.radio1)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.radio2)).perform(click());
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        //上面点击了6个按钮，所以有6条数据被存入数据库
        assertEquals(6, recordDataList.size());
        //等待10s 后查询数据库的数据
        Thread.sleep(10 * 1000);
        List<RecordData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());

        //等待2分钟查询服务器中的数据是否和上传的一致
        Thread.sleep(1000*60*2);
        ResponseData responseData = HttpBuilder.Builder()
                .setUrl("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/influx/query_data")
                .setHeadParams(SyncDataUtils.getQueryHead(token))
                .setMethod(RequestMethod.POST)
                .setBodyString(SyncDataUtils.buildPostBody())
                .executeSync(ResponseData.class);

        JSONObject jsonObject = new JSONObject(responseData.getData());
        JSONObject content = jsonObject.optJSONObject("content");
        int length = content.optJSONArray("data").getJSONObject(0).optJSONArray("series")
                .getJSONObject(0).optJSONArray("values").length();
        assertEquals(length,6);

    }

    /**
     * 获取登录token
     * @return
     * @throws JSONException
     */
    private String getLoginToken() throws JSONException {
        ResponseData responseData = HttpBuilder.Builder()
                .setUrl("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/auth-token/login")
                .setMethod(RequestMethod.POST)
                .setHeadParams(SyncDataUtils.getLoginHead())
                .setBodyString(SyncDataUtils.getLoginBody(rule.getActivity()))
                .executeSync(ResponseData.class);
        JSONObject jsonObject = new JSONObject(responseData.getData());
        JSONObject content = jsonObject.optJSONObject("content");
        return content.optString("token");
    }
}