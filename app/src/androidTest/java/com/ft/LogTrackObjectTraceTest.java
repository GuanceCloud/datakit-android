package com.ft;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncDataManager;
import com.ft.sdk.garble.manager.SyncTaskManager;
import com.ft.sdk.garble.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.ft.TestEntrance.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/25 15:24:06
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class LogTrackObjectTraceTest extends BaseTest{
    Context context;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
//        SyncTaskManager.get().setRunning(true);
        stopSyncTask();

        context = MockApplication.getContext();
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

    /**
     * 插入一条 log 数据测试
     *
     * @throws InterruptedException
     */
    @Test
    public void logInsertDataTest() throws InterruptedException {
        //产生一条日志数据
        FTTrack.getInstance().logBackground("----logInsertDataTest----", Status.CRITICAL);
        //线程池中插入，有一定的时间延迟，这里设置5秒等待时间
        Thread.sleep(5000);
        //从数据库中查询是否有插入的数据
        int except = judgeDBContainTargetLog(DataType.LOG, "----logInsertDataTest----");
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
        FTTrack.getInstance().logBackground("----logUpdateDataTest----", Status.CRITICAL);
        //线程池中插入，有一定的时间延迟，这里设置5秒等待时间
        Thread.sleep(5000);
        uploadData(DataType.LOG);
    }


    /**
     * 同步删除测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void logSyncTest() throws InterruptedException, JSONException {
        FTTrack.getInstance().logBackground("----logUploadTest----", Status.CRITICAL);
        Thread.sleep(12000);
        int except = judgeDBContainTargetLog(DataType.LOG, "----logUploadTest----");
        Assert.assertEquals(0, except);
    }

    /**
     * 插入一条 track 数据测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void trackInsertDataTest() throws InterruptedException, JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", "----trackInsertDataTest----");
        FTTrack.getInstance().trackBackground("TestLog", tags, fields);
        Thread.sleep(5000);
        int except = judgeDBContainTargetLog(DataType.TRACK, "----trackInsertDataTest----");
        Assert.assertEquals(1, except);
    }

    /**
     * 上传一条 track 数据测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void trackUploadDataTest() throws InterruptedException, JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", "----trackUploadDataTest----");
        FTTrack.getInstance().trackBackground("TestLog", tags, fields);
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
    public void trackSyncTest() throws InterruptedException, JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", "----trackUploadTest----");
        FTTrack.getInstance().trackBackground("TestLog", tags, fields);
        Thread.sleep(12000);
        int except = judgeDBContainTargetLog(DataType.TRACK, "----trackUploadTest----");
        Assert.assertEquals(0, except);
    }

    /**
     * 插入一条 object 数据测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void objectInsertDataTest() throws InterruptedException, JSONException {
        ObjectBean objectBean = new ObjectBean("objectTest", "----objectInsertDataTest----");
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(5000);
        int except = judgeDBContainTargetLog(DataType.OBJECT, "----objectInsertDataTest----");
        Assert.assertEquals(1, except);
    }

    /**
     * 上传一条 object 数据测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void objectUploadDataTest() throws InterruptedException, JSONException {
        ObjectBean objectBean = new ObjectBean("objectTest", "----objectUploadDataTest----");
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(5000);
        uploadData(DataType.OBJECT);
    }

    /**
     * 同步删除测试
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void objectSyncTest() throws InterruptedException, JSONException {
        ObjectBean objectBean = new ObjectBean("objectTest", "----objectUploadTest----");
        FTTrackInner.getInstance().objectBackground(objectBean);
        Thread.sleep(12000);
        int except = judgeDBContainTargetLog(DataType.OBJECT, "----objectUploadTest----");
        Assert.assertEquals(0, except);
    }

    /**
     * trace 一个正常的网络
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadNormalTest() throws JSONException, InterruptedException {
        traceDataTest("http://www.weather.com.cn/data/sk/101010100.html", "www.weather.com.cn");
    }

    /**
     * trace 网络超时
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadTimeOutTest() throws JSONException, InterruptedException {
        traceDataTest("https://www.google.com", "www.google.com");
    }

    /**
     * trace 网络错误
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void traceUploadErrorTest() throws JSONException, InterruptedException {
        traceDataTest("https://error.url", "error.url");
    }

    /**
     * 上传一条正常的 trace 数据测试
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    private void traceDataTest(String url, String except) throws JSONException, InterruptedException {
        RequestUtil.requestUrl(url);
        Thread.sleep(5000);
        int except1 = judgeDBContainTargetLog(DataType.LOG, except);
        FTTrack.getInstance().logBackground("----traceDataTest----", Status.CRITICAL);
        Thread.sleep(12000);
        int except2 = judgeDBContainTargetLog(DataType.LOG, except);
        Assert.assertEquals(1, except1);
        Assert.assertEquals(0, except2);
    }

    /**
     * 上传数据测试
     *
     * @param dataType
     */
    private void uploadData(DataType dataType) {
        List<RecordData> recordDataList = null;
        switch (dataType) {
            case OBJECT:
                recordDataList = FTDBManager.get().queryDataByDescLimitObject(0);
                break;
            case LOG:
                recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
                break;
            case KEY_EVENT:
                recordDataList = FTDBManager.get().queryDataByDescLimitKeyEvent(0);
                break;
            case TRACK:
                recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
                break;
        }
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);
        SyncTaskManager.get().requestNet(dataType, body, (code, response) -> {
            Assert.assertEquals(200, code);
        });
    }

    /**
     * 判断数据库中包含几条目标数据
     *
     * @param target
     * @return
     */
    private int judgeDBContainTargetLog(DataType type, String target) {
        List<RecordData> recordDataList = null;
        switch (type) {
            case OBJECT:
                recordDataList = FTDBManager.get().queryDataByDescLimitObject(0);
                break;
            case LOG:
                recordDataList = FTDBManager.get().queryDataByDescLimitLog(0);
                break;
            case KEY_EVENT:
                recordDataList = FTDBManager.get().queryDataByDescLimitKeyEvent(0);
                break;
            case TRACK:
                recordDataList = FTDBManager.get().queryDataByDescLimitTrack(0);
                break;
        }
        int except = 0;
        if (recordDataList != null) {
            for (RecordData data : recordDataList) {
                if (data.getOpdata().contains(target)) {
                    except++;
                }
            }
        }
        return except;
    }
}
