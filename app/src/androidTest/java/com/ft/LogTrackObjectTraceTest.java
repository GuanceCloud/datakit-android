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
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;

import static com.ft.TestEntrance.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/25 15:24:06
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class LogTrackObjectTraceTest {
    Context context;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        SyncTaskManager.get().setRunning(true);
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
                .setTraceSamplingRate(1f)
                .setNetworkTrace(true)
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setTraceType(TraceType.SKYWALKING_V2)
                .setOnlySupportMainProcess(true);
        FTSdk.install(ftSDKConfig);
        FTDBManager.get().delete();
    }

    @After
    public void tearDown(){
        FTSdk.get().shutDown();
    }

    /**
     * 插入一条 log 数据测试
     * @throws InterruptedException
     */
    @Test
    public void logInsertDataTest() throws InterruptedException {
        FTTrack.getInstance().logBackground("TestLog0o0o0", Status.CRITICAL);
        Thread.sleep(5000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(10);
        int except = 0;
        if (recordDataList != null) {
            for (RecordData data : recordDataList) {
                if (data.getOpdata().contains("TestLog0o0o0")) {
                    except++;
                }
            }
        }
        Assert.assertEquals(1, except);
    }

    /**
     * 上传一条 log 数据测试
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void logUploadTest() throws InterruptedException, JSONException {
        //设置时间间隔防止多个测试用例请求数据后无法准确的判断返回值
        Thread.sleep(1000 * 60);
        String token = getLoginToken();
        SyncTaskManager.get().setRunning(false);
        FTTrack.getInstance().logBackground("TestLog11111", Status.CRITICAL);
        Thread.sleep(1000 * 70);
        queryUploadDataLog(token, "TestLog11111");
    }

    /**
     * 插入一条 track 数据测试
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void trackInsertDataTest() throws InterruptedException, JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", "fieldTest");
        FTTrack.getInstance().trackBackground("TestLog", tags, fields);
        Thread.sleep(5000);
        int length = FTDBManager.get().queryDataByDescLimitTrack(10).size();
        Assert.assertEquals(1, length);
    }

    /**
     * 上传一条 track 数据测试
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void trackUploadTest() throws InterruptedException, JSONException {
        String measurement = "TrackLog";
        String field = "field-" + System.currentTimeMillis();
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
        queryUploadDataTrack(measurement, token, field);
    }

    /**
     * 插入一条 object 数据测试
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void objectInsertDataTest() throws InterruptedException, JSONException {
        ObjectBean objectBean = new ObjectBean("objectTest", "Test");
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(5000);
        int length = FTDBManager.get().queryDataByDescLimitObject(10).size();
        Assert.assertEquals(1, length);
    }

    /**
     * 上传一条 object 数据测试
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void objectUploadTest() throws InterruptedException, JSONException {
        String clazz = "Test-" + System.currentTimeMillis();
        String token = getLoginToken();
        SyncTaskManager.get().setRunning(false);
        ObjectBean objectBean = new ObjectBean("objectTest", clazz);
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(1000 * 60);
        queryUploadDataObject(clazz, token, 1);
    }

    /**
     * trace 一个正常的网络
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadNormalTest() throws JSONException, InterruptedException {
        traceDataTest("http://www.weather.com.cn/data/sk/101010100.html","www.weather.com.cn");
    }

    /**
     * trace 网络超时
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadTimeOutTest() throws JSONException, InterruptedException {
        traceDataTest("https://www.google.com","www.google.com");
    }

    /**
     * trace 网络错误
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadErrorTest() throws JSONException, InterruptedException {
        traceDataTest("https://error.url","error.url");
    }
    /**
     * 上传一条正常的 trace 数据测试
     * @throws JSONException
     * @throws InterruptedException
     */
    private void traceDataTest(String url,String except) throws JSONException, InterruptedException {
        RequestUtil.requestUrl(url);
        String token = getLoginToken();
        SyncTaskManager.get().setRunning(false);
        Thread.sleep(1000 * 60);
        queryUploadDataTrace(token, except);
    }

    private void queryUploadDataLog(String token, String expect) throws JSONException {
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
        JSONArray array = content.optJSONArray("responses").optJSONObject(0).optJSONObject("hits")
                .optJSONArray("hits");
        int count = 0;
        for (int i = 0; i < array.length(); i++) {
            String str = array.getString(i);
            if(str.contains(expect)){
                count++;
            }
        }
        Assert.assertEquals(1, count);
    }

    private void queryUploadDataObject(String clazz, String token, int expect) throws JSONException {
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

    private void queryUploadDataTrack(String measurement, String token, String field) throws JSONException {
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

    private void queryUploadDataTrace(String token, String expect) throws JSONException {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("body", SyncDataUtils.buildLogBody());
        ResponseData responseData = HttpBuilder.Builder()
                .setHost("http://testing.api-ft2x.cloudcare.cn:10531")
                .setModel("api/v1/elasticsearch/query_data")
                .setHeadParams(SyncDataUtils.getQueryHead(token))
                .enableToken(false)
                .setMethod(RequestMethod.GET)
                .setParams(hashMap)
                .executeSync(ResponseData.class);
        boolean contain = responseData.getData().contains(expect);
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
