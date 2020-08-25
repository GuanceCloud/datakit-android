package com.ft;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * author: huangDianHua
 * time: 2020/8/25 15:24:06
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class LogTrackObjectTraceTest {
    Context context;
    static boolean hasPrepare;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = DemoApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setDescLog(true)
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
        FTSdk.install(ftSDKConfig);
        FTDBManager.get().delete();
    }

    @Test
    public void logInsertDataTest() throws InterruptedException {
        SyncTaskManager.get().setRunning(true);
        FTTrack.getInstance().logBackground("TestLog", Status.CRITICAL);
        Thread.sleep(5000);
        int length = FTDBManager.get().queryDataByDescLimitLog(10).size();
        Assert.assertEquals(1, length);
    }

    @Test
    public void logUploadTest() throws InterruptedException, JSONException {
        //设置时间间隔防止多个测试用例请求数据后无法准确的判断返回值
        Thread.sleep(1000 * 60);
        String token = getLoginToken();
        SyncTaskManager.get().setRunning(false);
        FTTrack.getInstance().logBackground("TestLog", Status.CRITICAL);
        Thread.sleep(1000 * 70);
        queryUploadDataLog(token, 1);
    }

    @Test
    public void trackInsertDataTest() throws InterruptedException, JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", "fieldTest");
        SyncTaskManager.get().setRunning(true);
        FTTrack.getInstance().trackBackground("TestLog", tags, fields);
        Thread.sleep(5000);
        int length = FTDBManager.get().queryDataByDescLimitTrack(10).size();
        Assert.assertEquals(1, length);
    }

    @Test
    public void trackUploadTest() throws InterruptedException, JSONException {
        String measurement = "TrackLog";
        String field = "field-"+System.currentTimeMillis();
        //设置时间间隔防止多个测试用例请求数据后无法准确的判断返回值
        //Thread.sleep(1000 * 60);
        String token = getLoginToken();
        SyncTaskManager.get().setRunning(false);
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put(field, "testField");
        FTTrack.getInstance().trackBackground(measurement, tags, fields);
        Thread.sleep(1000 * 70);
        queryUploadDataTrack(measurement,token,field);
    }

    @Test
    public void objectInsertDataTest() throws InterruptedException, JSONException {
        SyncTaskManager.get().setRunning(true);
        ObjectBean objectBean = new ObjectBean("objectTest", "Test");
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(5000);
        int length = FTDBManager.get().queryDataByDescLimitObject(10).size();
        Assert.assertEquals(1, length);
    }

    @Test
    public void objectUploadTest() throws InterruptedException, JSONException {
        String clazz = "Test-"+System.currentTimeMillis();
        String token = getLoginToken();
        SyncTaskManager.get().setRunning(false);
        ObjectBean objectBean = new ObjectBean("objectTest", clazz);
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(1000 * 60);
        queryUploadDataObject(clazz,token, 1);
    }


    private void queryUploadDataLog(String token, int expect) throws JSONException {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("body", SyncDataUtils.buildLogBody());
        ResponseData responseData = HttpBuilder.Builder()
                .setHost("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/elasticsearch/query_data")
                .setHeadParams(SyncDataUtils.getQueryHead(token))
                .setMethod(RequestMethod.GET)
                .setParams(hashMap)
                .executeSync(ResponseData.class);

        JSONObject jsonObject = new JSONObject(responseData.getData());
        JSONObject content = jsonObject.optJSONObject("content");
        int length = content.optJSONArray("responses").optJSONObject(0).optJSONObject("hits")
                .optJSONArray("hits").length();
        Assert.assertEquals(expect, length);
    }

    private void queryUploadDataObject(String clazz,String token, int expect) throws JSONException {
        ResponseData responseData = HttpBuilder.Builder()
                .setHost("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/elasticsearch/msearch")
                .setHeadParams(SyncDataUtils.getQueryHead(token))
                .setMethod(RequestMethod.POST)
                .setBodyString(SyncDataUtils.buildObjectBody(clazz))
                .executeSync(ResponseData.class);

        JSONObject jsonObject = new JSONObject(responseData.getData());
        JSONObject content = jsonObject.optJSONObject("content");
        int length = content.optJSONArray("responses").optJSONObject(0).optJSONObject("hits")
                .optJSONObject("total").optInt("value");
        Assert.assertEquals(expect, length);
    }

    private void queryUploadDataTrack(String measurement,String token, String field) throws JSONException {
        ResponseData responseData = HttpBuilder.Builder()
                .setHost("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/influx/query_field_keys")
                .setHeadParams(SyncDataUtils.getQueryHead(token))
                .setMethod(RequestMethod.POST)
                .setBodyString(SyncDataUtils.buildTrackBody(measurement))
                .executeSync(ResponseData.class);
        boolean contain = responseData.getData().contains(field);
        Assert.assertTrue(contain);
    }

    /**
     * 获取登录token
     *
     * @return
     * @throws JSONException
     */
    private String getLoginToken() throws JSONException {
        ResponseData responseData = HttpBuilder.Builder()
                .setHost("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/auth-token/login")
                .setMethod(RequestMethod.POST)
                .setHeadParams(SyncDataUtils.getLoginHead())
                .setBodyString(SyncDataUtils.getLoginBody(context))
                .executeSync(ResponseData.class);
        JSONObject jsonObject = new JSONObject(responseData.getData());
        JSONObject content = jsonObject.optJSONObject("content");
        return content.optString("token");
    }
}
